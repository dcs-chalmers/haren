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

/**
 * Entity responsible for assigning the correct indexes to the {@link Task}s scheduled by a {@link
 * Scheduler}, especially in case of live reconfigurations.
 */
public interface TaskIndexer {

  /**
   * Get the internal index for the given task, used for all scheduling purposes.
   *
   * @param task The {@link Task} to get the index for.
   * @return The scheduler index of the task.
   */
  int schedulerIndex(Task task);

  /**
   * Register new tasks to be handled by the indexer. Depending on the {@link TaskIndexer}
   * implementation, this might only need to be done for tasks added during a live reconfiguration.
   *
   * @param tasks The tasks to be registered in this {@link TaskIndexer}.
   */
  void registerTasks(Collection<Task> tasks);

  /**
   * Unregister existing tasks to will no longer be handled by the indexer. Depending on the {@link TaskIndexer}
   * implementation, this might only need to be done for tasks removed during a live reconfiguration.
   *
   * @param tasks The tasks to be unregistered in this {@link TaskIndexer}.
   */
  void unregisterTasks(Collection<Task> tasks);

  /**
   * @return The total number of tasks handled by this indexer.
   */
  int indexedTasks();
}
