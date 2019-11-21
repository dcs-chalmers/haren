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

import io.palyvos.haren.HarenScheduler;
import io.palyvos.haren.Task;
import io.palyvos.haren.TaskIndexer;

public interface SingleIntraThreadSchedulingFunction extends IntraThreadSchedulingFunction {

  /**
   * Get the value of this function.
   *
   * @param task The task to get the value for.
   * @param indexer The {@link TaskIndexer} that will provide the "scheduler index" for th
   * @param features The feature matrix of all tasks.
   * @return The value of the function for the given task and the current values of the features.
   */
  double apply(Task task, TaskIndexer indexer, double[][] features);

  /**
   * Usually if a {@link Task} has a higher value of a {@link SingleIntraThreadSchedulingFunction},
   * it means that it has a higher priority. If the <b>reverse</b> is true for this specific
   * function, this should return {@code true}, so that {@link HarenScheduler} can sort the tasks
   * correctly by their priority.
   *
   * @return {@code true} if lower values of priority imply higher priority
   */
  default boolean isReverseOrder() {
    return false;
  }

  default SingleIntraThreadSchedulingFunction reverse() {
    return new ReverseIntraThreadSchedulingFunction(this);
  }

  @Override
  SingleIntraThreadSchedulingFunction enableCaching(int nTasks);
}
