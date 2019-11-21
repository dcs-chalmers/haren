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

package io.palyvos.haren.function;

import io.palyvos.haren.Feature;
import io.palyvos.haren.HarenScheduler;
import io.palyvos.haren.Task;
import io.palyvos.haren.TaskIndexer;
import java.util.List;

/**
 * Abstraction of a function that assigns {@link Task}s to {@link HarenScheduler}'s processing
 * threads.
 */
public interface InterThreadSchedulingFunction {

  /**
   * Initialize the function with the {@link Task}s that will be assigned and their {@link Feature}
   * matrix
   *
   * @param tasks The {@link Task}s that this function will assign.
   * @param taskCapacity The maximum number of tasks that must be supported (based on the IDs that
   *     will be provided by the {@link TaskIndexer}
   * @param indexer The {@link TaskIndexer} that assigns scheduler indexes to {@link Task}s
   * @param features The {@link Feature} matrix of these tasks.
   */
  void reset(List<Task> tasks, int taskCapacity, TaskIndexer indexer, double[][] features);

  /**
   * Get the assignment of {@link Task}s to processing threads.
   *
   * @param nThreads The number of available processing threads.
   * @return A {@link List}, each element of which is the {@link Task}s that are assigned to the
   *     thread with that index.
   */
  List<List<Task>> getAssignment(int nThreads);

  /**
   * Get the {@link Feature}s which are used to compute this function's value.
   *
   * @return An array of the {@link Feature}s required by this function.
   */
  Feature[] requiredFeatures();

  /** @return The name of this function. */
  String name();
}
