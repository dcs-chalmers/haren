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

import io.palyvos.haren.function.IntraThreadSchedulingFunction;
import io.palyvos.haren.function.SingleIntraThreadSchedulingFunction;
import io.palyvos.haren.function.VectorIntraThreadSchedulingFunction;
import io.palyvos.haren.function.VectorIntraThreadSchedulingFunctionImpl;
import io.palyvos.haren.function.InterThreadSchedulingFunction;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** The scheduler class, responsible for orchestrating the execution of streaming {@link Task}s. */
public class HarenScheduler implements Scheduler {

  private volatile boolean active;
  private static final Logger LOG = LogManager.getLogger();
  private final int nThreads;
  private final List<Task> tasks = new ArrayList<>();
  private final Set<Integer> currentTaskIndexes = new HashSet<>();
  private final List<Thread> threads = new ArrayList<>();
  private final int[] workerAffinity;
  private volatile ReconfigurationAction reconfigurationAction;
  private SchedulerState state;
  private final SchedulerStateBuilder stateBuilder = new SchedulerStateBuilder();

  /**
   * Construct.
   *  @param nThreads The number of worker threads that will be used by Haren.
   * @param intraThreadFunction The desired {@link
   *     IntraThreadSchedulingFunction}, responsible for prioritizing the
   *     tasks executed by each thread.
   * @param interThreadFunction The desired {@link InterThreadSchedulingFunction}, responsible for
 *     assigning tasks to worker threads.
   * @param caching Enable or disable caching (if supported by the chosen scheduling functions).
   * @param batchSize The maximum number of invocations of a scheduled tasks. Controls the
*     preemption granularity.
   * @param schedulingPeriod The duration between two invocations of the scheduler, in millisec.
   * @param workerAffinity Available CPU cores for the scheduler. Will be assigned to workers in a
   */
  public HarenScheduler(
      int nThreads,
      VectorIntraThreadSchedulingFunction intraThreadFunction,
      InterThreadSchedulingFunction interThreadFunction,
      boolean caching,
      int batchSize,
      long schedulingPeriod,
      BitSet workerAffinity) {
    Validate.isTrue(nThreads > 0);
    Validate.notNull(intraThreadFunction);
    Validate.notNull(interThreadFunction);
    Validate.isTrue(batchSize > 0);
    Validate.isTrue(schedulingPeriod > 0);
    this.nThreads = nThreads;
    stateBuilder
        .setThreadNumber(nThreads)
        .setInterThreadSchedulingFunction(interThreadFunction)
        .setIntraThreadSchedulingFunction(intraThreadFunction)
        .setPriorityCaching(caching)
        .setBatchSize(batchSize)
        .setSchedulingPeriod(schedulingPeriod);
    this.workerAffinity = workerAffinity.stream().toArray();
    if (this.workerAffinity.length < nThreads) {
      LOG.warn("#CPUs assigned is less than #threads! Performance might suffer.");
    }
  }

  /**
   * Helper constructor which accepts a {@link SingleIntraThreadSchedulingFunction} for convenience.
   *
   * @see #HarenScheduler(int, VectorIntraThreadSchedulingFunction, InterThreadSchedulingFunction, boolean, int, long, BitSet)
   */
  public HarenScheduler(
      int nThreads,
      SingleIntraThreadSchedulingFunction intraThreadFunction,
      InterThreadSchedulingFunction interThreadFunction,
      boolean priorityCaching,
      int batchSize,
      int schedulingPeriod,
      BitSet workerAffinity) {
    this(
        nThreads,
        new VectorIntraThreadSchedulingFunctionImpl(intraThreadFunction),
        interThreadFunction,
        priorityCaching,
        batchSize,
        schedulingPeriod,
        workerAffinity);
  }

  @Override
  public void start() {
    Validate.isTrue(tasks.size() >= nThreads, "Tasks less than threads!");
    active = true;
    stateBuilder.setTaskNumber(tasks.size()).setThreadNumber(nThreads);
    LOG.info("Starting Scheduler");
    LOG.info(stateBuilder.toString());
    state = stateBuilder.createSchedulerState();
    final List<AbstractExecutor> executors = new ArrayList<>();
    this.reconfigurationAction = new ReconfigurationAction(tasks, executors, state);
    CyclicBarrier barrier = new CyclicBarrier(nThreads, reconfigurationAction);
    for (int i = 0; i < nThreads; i++) {
      int cpuId = getAffinity(i);
      executors.add(new HighestPriorityExecutor(state, barrier, cpuId));
    }
    for (int i = 0; i < executors.size(); i++) {
      Thread t = new Thread(executors.get(i));
      t.setName(String.format("Scheduler-Worker-%d", i));
      threads.add(t);
      t.start();
    }
  }

  @Override
  public void stop() {
    reconfigurationAction.stop();
    for (Thread thread : threads) {
      thread.interrupt();
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    active = false;
  }

  public void setBatchSize(int batchSize) {
    state.setBatchSize(batchSize);
  }

  public void setSchedulingPeriod(long schedulingPeriod) {
    state.setSchedulingPeriod(schedulingPeriod);
  }

  public void setIntraThreadFunction(VectorIntraThreadSchedulingFunction intraThreadFunction) {
    state.setIntraThreadSchedulingFunction(intraThreadFunction);
  }

  @Override
  public void addTasks(Collection<Task> tasks) {
    if (active) {
      reconfigurationAction.addTasks(tasks);
    } else {
      Set<Integer> newTaskIndexes = taskIndexes(tasks);
      Validate.isTrue(
          Collections.disjoint(currentTaskIndexes, newTaskIndexes),
          "Tried to add tasks that have already been added!");
      this.tasks.addAll(tasks);
      this.currentTaskIndexes.addAll(newTaskIndexes);
    }
    LOG.info("{} reconfiguration. Adding: {} tasks", reconfigurationType(), tasks.size());
  }

  @Override
  public void removeTasks(Collection<Task> tasks) {
    if (active) {
      reconfigurationAction.removeTasks(tasks);
    } else {
      Set<Integer> newTaskIndexes = taskIndexes(tasks);
      Validate.isTrue(
          currentTaskIndexes.containsAll(newTaskIndexes),
          "Tried to remove tasks that are not currently being scheduled.");
      this.tasks.removeAll(tasks);
      this.currentTaskIndexes.removeAll(newTaskIndexes);
    }
    LOG.info("{} reconfiguration. Removing: {} tasks", reconfigurationType(), tasks.size());
  }

  private int getAffinity(int i) {
    return workerAffinity != null ? workerAffinity[i % workerAffinity.length] : -1;
  }

  public List<Task> tasks() {
    return tasks;
  }

  private String reconfigurationType() {
    return active ? "Live" : "Static";
  }

  static Set<Integer> taskIndexes(Collection<Task> tasks) {
    return tasks.stream().map(t -> t.getIndex()).collect(Collectors.toSet());
  }
}
