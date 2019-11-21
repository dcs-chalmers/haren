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

import java.util.Collection;

/** The entity that is responsible for scheduling {@link io.palyvos.haren.Task}s in an SPE. */
public interface Scheduler {

  /**
   * Add tasks to be scheduled by the {@link Scheduler} instance. Each {@link Task} can only be
   * added once. The function can be called multiple times, to add different sets of tasks. If the
   * scheduler implementation supports live reconfigurations, this function can also be called when
   * the scheduler is running.
   *
   * @param tasks The tasks to be scheduled.
   * @throws IllegalStateException if the scheduler implementation does not support live
   *     reconfigurations and this function is called while the scheduler is running
   */
  void addTasks(Collection<Task> tasks);

  /**
   * Remove tasks that were to be scheduled by the {@link Scheduler} instance.This function can be
   * called multiple times, to remove different sets of tasks. If the scheduler implementation
   * supports live reconfigurations, this function can also be called when the scheduler is running.
   *
   * @param tasks The tasks to be removed.
   * @throws IllegalStateException if the scheduler implementation does not support live *
   *     reconfigurations and this function is called while the scheduler is running
   */
  void removeTasks(Collection<Task> tasks);

  /** Start scheduling. */
  void start();

  /** Stop scheduling. */
  void stop();
}
