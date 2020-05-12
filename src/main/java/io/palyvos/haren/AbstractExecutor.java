/*
 * Copyright 2018-2019
 *     Dimitris Palyvos-Giannas
 *     Vincenzo Gulisano
 *     Marina Papatriantafilou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contact:
 *     Dimitris Palyvos-Giannas palyvos@chalmers.se
 */

package io.palyvos.haren;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import net.openhft.affinity.Affinity;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class encompassing the exectuon logic of every worker thread of {@link HarenScheduler}. */
public abstract class AbstractExecutor implements Runnable {

  private static final int BACKOFF_MIN_MILLIS = 1;
  private static final AtomicInteger indexGenerator = new AtomicInteger();
  private static final Logger LOG = LogManager.getLogger();
  private static final int BACKOFF_RETRIES = 3;
  private static final int TASK_UPDATE_LIMIT_FACTOR = 2;
  protected final CyclicBarrier barrier;
  protected final SchedulerState state;
  private final int index;
  private final Set<Integer> runTasks = new HashSet<>();
  private final Set<TaskDependency> taskDependencies = new HashSet<>();
  private final SchedulerBackoff backoff;
  private final int cpuId;
  protected volatile List<Task> executorTasks = Collections.emptyList();

  public AbstractExecutor(SchedulerState state, CyclicBarrier barrier) {
    this(state, -1, barrier);
  }

  public AbstractExecutor(SchedulerState state, int cpuId, CyclicBarrier barrier) {
    this.barrier = barrier;
    this.state = state;
    this.backoff =
        new SchedulerBackoff(BACKOFF_MIN_MILLIS, state.schedulingPeriod() / 10, BACKOFF_RETRIES);
    this.index = indexGenerator.getAndIncrement();
    this.cpuId = cpuId;
    initTaskDependencies(state.variableFeaturesWithDependencies());
  }

  private void initTaskDependencies(Feature[] features) {
    // Merge feature dependencies, i.e. upstream, downstream, ...
    for (Feature feature : features) {
      for (FeatureDependency dependency : feature.dependencies()) {
        taskDependencies.addAll(Arrays.asList(dependency.dependencies));
      }
    }
  }

  public void setTasks(List<Task> tasks) {
    this.executorTasks = tasks;
  }

  @Override
  public void run() {
    if (cpuId > 0) {
      LOG.info("Setting affinity to CPU #{}", cpuId);
      Affinity.setAffinity(cpuId);
    }
    if (!updateTasks()) {
      return;
    }
    beginRound();
    while (!Thread.currentThread().isInterrupted()) {
      boolean didRun = runNextTask();
      adjustUtilization(didRun, state.remainingRoundTime());
      if (state.remainingRoundTime() <= 0) {
        if (!updateTasks()) {
          break;
        }
        beginRound();
      }
    }
    LOG.debug("Executor {} finished.", index);
  }

  /**
   * Update the start time of the new round, but count the sort time in the round time, to achieve
   * synchronized entry to the barrier between processing threads.
   *
   */
  private void beginRound() {
    calculatePriorities();
    sortTasks();
    onRoundStart();
    runLaggingTasks();
    backoff.reset();
    if (LOG.getLevel() == Level.TRACE) {
      printTasks();
    }
  }

  private void calculatePriorities() {
    for (Task task : executorTasks) {
      state
          .intraThreadSchedulingFunction()
          .apply(
              task,
              state.indexer(),
              state.taskFeatures,
              state.priorities[state.indexer().schedulerIndex(task)]);
    }
  }

  private void runLaggingTasks() {
    long timestamp = System.currentTimeMillis();
    for (int i = 0; i < executorTasks.size(); i++) {
      Task task = executorTasks.get(i);
      if (state.timeToUpdate(
          task, timestamp, TASK_UPDATE_LIMIT_FACTOR * state.schedulingPeriod())) {
        task.runFor(1);
        mark(task, i);
      }
    }
  }

  private void adjustUtilization(boolean didRun, long remainingTime) {
    if (remainingTime <= 0) {
      return;
    }
    if (!didRun) {
      backoff.backoff(remainingTime);
      return;
    }
    backoff.relax();
  }

  private boolean updateTasks() {
    try {
      markUpdated();
      long barrierEnterTime = System.currentTimeMillis();
      state.recordBarrierEnter(index, barrierEnterTime);
      barrier.await();
      long barrierExitTime = System.currentTimeMillis();
      state.recordBarrierExit(index, barrierExitTime);
      return true;
    } catch (InterruptedException | BrokenBarrierException e) {
      return false;
    }
  }

  private void sortTasks() {
    executorTasks.sort(state.comparator);
  }

  private void markUpdated() {
    long markTime = System.currentTimeMillis();
    // Refresh features, for example those who are recorded as moving averages
    for (Task task : executorTasks) {
      task.refreshFeatures();
    }
    for (int taskIndex : runTasks) {
      Task task = executorTasks.get(taskIndex);
      task.updateFeatures(
          state.variableFeaturesNoDependencies(),
          state.taskFeatures[state.indexer().schedulerIndex(task)]);
      state.markRun(task, markTime);
      for (TaskDependency taskDependency : taskDependencies) {
        for (Task dependent : taskDependency.dependents(task)) {
          state.markUpdated(dependent);
        }
      }
    }
    runTasks.clear();
  }

  /**
   * Mark the task with that has the given index in the <b>LOCAL task list</b> as executed. Do NOT
   * use {@link Task#getIndex()} in this function, except if it matches the local index (which it
   * usually does not).
   *
   * @param task The task to mark
   * @param localIndex The index of the task in executorTasks
   */
  protected final void mark(Task task, int localIndex) {
    runTasks.add(localIndex);
  }

  protected abstract boolean runNextTask();

  protected abstract void onRoundStart();

  private void printTasks() {
    synchronized (AbstractExecutor.class) {
      LOG.info("-----Thread assignment-----");
      for (Task task : executorTasks) {
        LOG.info(
            "[{}, {}] -> {}",
            task,
            Arrays.toString(state.priorities[state.indexer().schedulerIndex(task)]),
            Arrays.toString(state.taskFeatures[state.indexer().schedulerIndex(task)]));
      }
    }
  }

  @Override
  public String toString() {
    return "EXECUTOR: " + executorTasks + "\n";
  }
}
