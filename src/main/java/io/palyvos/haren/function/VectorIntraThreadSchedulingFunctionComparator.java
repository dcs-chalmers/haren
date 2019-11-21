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

import io.palyvos.haren.Task;
import io.palyvos.haren.TaskIndexer;
import java.util.Comparator;
import org.apache.commons.lang3.Validate;

public class VectorIntraThreadSchedulingFunctionComparator implements Comparator<Task> {

  private final double[][] priorities;
  private final boolean[] reverseOrder;
  private final TaskIndexer indexer;

  public VectorIntraThreadSchedulingFunctionComparator(VectorIntraThreadSchedulingFunction function,
      double[][] priorities, TaskIndexer indexer) {
    Validate.isTrue(function.dimensions() == priorities[0].length);
    this.priorities = priorities;
    this.indexer = indexer;
    this.reverseOrder = new boolean[function.dimensions()];
    for (int i = 0; i < function.dimensions(); i++) {
      reverseOrder[i] = function.isReverseOrder(i);
    }
  }

  @Override
  public int compare(Task task1, Task task2) {
    double[] p1 = priorities[indexer.schedulerIndex(task1)];
    double[] p2 = priorities[indexer.schedulerIndex(task2)];
    // Compare the priorities of all dimensions
    for (int k = 0; k < p1.length; k++) {
      int dimensionComparison = Double.compare(p1[k], p2[k]);
      if (dimensionComparison != 0) {
        // Reverse order means from LOW -> HIGH (so same as Double.compare)
        // Normal order for priorities is HIGH -> LOW
        return reverseOrder[k] ? dimensionComparison : -dimensionComparison;
      }
    }
    // If all dimensions had equal priorities, tasks are equal
    return 0;
  }
}
