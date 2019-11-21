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

/**
 * Abstraction of a function that prioritizes {@link Task}s assigned to one of
 * {@link HarenScheduler}'s processing threads.
 */
public interface IntraThreadSchedulingFunction {

  /**
   * Get the {@link Feature}s which are used to compute this function's value.
   *
   * @return An array of the {@link Feature}s required by this function.
   */
  Feature[] requiredFeatures();

  /**
   * Enable caching for a scheduling round. Caching enables a function implementation to maintain
   * values that might be reused in the same scheduling period (if, for example, priorities of one
   * task depend on the priority of some other task).
   *
   * @param nTasks The (maximum) number of tasks.
   * @return {@code this} for chaining
   */
  IntraThreadSchedulingFunction enableCaching(int nTasks);

  /** Clear the cache (if caching is enabled). Called at the end of every scheduling period. */
  void clearCache();

  /**
   * Reset the state of the function, and adjust the state to support the (new) given number of
   * tasks.
   *
   * @param taskCapacity The (new) maximum number of tasks to be supported by this function.
   */
  default void reset(int taskCapacity) {
    if (cachingEnabled()) {
      enableCaching(taskCapacity);
    }
  }

  /**
   * Check if caching is enabled.
   *
   * @return {@code true} if caching is enabled.
   */
  boolean cachingEnabled();

  /** @return The name of this function. */
  String name();
}
