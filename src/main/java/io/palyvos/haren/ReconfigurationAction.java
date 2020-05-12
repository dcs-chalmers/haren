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

import io.palyvos.haren.function.InterThreadSchedulingFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The functional object that performs the sequential part of what is referred to as the "scheduling
 * task" of {@link HarenScheduler}. Retrieves some of the features and runs the {@link
 * InterThreadSchedulingFunction} that assigns {@link Task}s to {@link
 * AbstractExecutor}s.
 */
class ReconfigurationAction implements Runnable {

  private static final Logger LOG = LogManager.getLogger();
  private final List<Task> tasks;
  private final List<AbstractExecutor> executors;
  private final SchedulerState state;
  private boolean firstUpdate = true;
  private final List<Task> tasksToAdd = new ArrayList<>();
  private final List<Task> tasksToRemove = new ArrayList<>();
  private final Set<Integer> taskIndexes = new HashSet<>();

  public ReconfigurationAction(
      List<Task> inputTasks, List<AbstractExecutor> executors, SchedulerState state) {
    this.tasks = new ArrayList<>(inputTasks);
    Collections.sort(tasks, Comparator.comparingInt(Task::getIndex));
    this.taskIndexes.addAll(HarenScheduler.taskIndexes(tasks));
    this.executors = executors;
    this.state = state;
    this.state.resetSchedulingFunctions(tasks);
  }


  @Override
  public void run() {
    Validate.isTrue(tasks.size() > 0, "No tasks given!");
    boolean configurationChanged = addRemoveTasks();
    if (firstUpdate || configurationChanged) {
      updateAllFeatures();
      firstUpdate = false;
    } else {
      updateFeaturesWithDependencies();
    }
    state.intraThreadSchedulingFunction().clearCache();
    List<List<Task>> assignments = deployTasks();
    assignTasks(assignments);
    state.updateRoundEndTime();
  }

  private void updateFeaturesWithDependencies() {
    for (Task task : tasks) {
      if (state.resetUpdated(task)) {
        task.updateFeatures(
            state.variableFeaturesWithDependencies(),
            state.taskFeatures[state.indexer().schedulerIndex(task)]);
      }
    }
  }

  private void updateAllFeatures() {
    for (Task task : tasks) {
      task.refreshFeatures();
      task.updateFeatures(
          state.constantFeatures(), state.taskFeatures[state.indexer().schedulerIndex(task)]);
      task.updateFeatures(
          state.variableFeaturesNoDependencies(),
          state.taskFeatures[state.indexer().schedulerIndex(task)]);
      task.updateFeatures(
          state.variableFeaturesWithDependencies(),
          state.taskFeatures[state.indexer().schedulerIndex(task)]);
    }
  }

  private List<List<Task>> deployTasks() {
    List<List<Task>> assignments =
        state.interThreadSchedulingFunction().getAssignment(executors.size());
    return assignments;
  }

  private void assignTasks(List<List<Task>> assignments) {
    Validate.isTrue(assignments.size() <= executors.size(), "#assignments > #threads");
    int taskCount = 0;
    for (int threadId = 0; threadId < executors.size(); threadId++) {
      // Give no work to executors with no assignment
      List<Task> assignment =
          threadId < assignments.size() ? assignments.get(threadId) : Collections.emptyList();
      executors.get(threadId).setTasks(assignment);
      taskCount += assignment.size();
    }
    Validate.isTrue(
        taskCount == tasks.size(),
        "It seems that the inter-thread function did not assign each task to exactly one executor!");
  }

  void stop() {
  }

  private synchronized boolean addRemoveTasks() {
    boolean configurationChanged = false;
    if (!tasksToRemove.isEmpty()) {
      validateTaskRemoval();
      LOG.info("Unregistering tasks: {}", tasksToRemove);
      state.unregisterTasks(tasksToRemove);
      configurationChanged = true;
    }
    if (!tasksToAdd.isEmpty()) {
      validateTaskAddition();
      LOG.info("Registering tasks: {}", tasksToAdd);
      state.registerTasks(tasksToAdd);
      configurationChanged = true;
    }
    if (configurationChanged) {
      tasks.removeAll(tasksToRemove);
      taskIndexes.removeAll(HarenScheduler.taskIndexes(tasksToRemove));
      LOG.debug("Removed tasks: {}", tasksToRemove);
      tasks.addAll(tasksToAdd);
      taskIndexes.addAll(HarenScheduler.taskIndexes(tasksToAdd));
      LOG.debug("Added tasks: {}", tasksToAdd);
      tasksToRemove.clear();
      tasksToAdd.clear();
      // Notify scheduling functions so that they can clear all their internal state
      state.resetSchedulingFunctions(tasks);
      LOG.debug("Reconfiguration complete.");
    }
    return configurationChanged;
  }

  private void validateTaskAddition() {
    for (Task task : tasksToAdd) {
      if (taskIndexes.contains(task.getIndex())) {
        throw new IllegalArgumentException(
            String.format(
                "Tried to add task %s which is already being scheduled!", task.toString()));
      }
    }
  }

  private void validateTaskRemoval() {
    for (Task task : tasksToRemove) {
      if (!taskIndexes.contains(task.getIndex())) {
        throw new IllegalArgumentException(
            String.format(
                "Tried to remove task %s which is not being scheduled!", task.toString()));
      }
    }
  }

  synchronized void addTasks(Collection<Task> tasks) {
    tasksToAdd.addAll(tasks);
  }

  synchronized void removeTasks(Collection<Task> tasks) {
    tasksToRemove.addAll(tasks);
  }
}
