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
import io.palyvos.haren.Task;
import io.palyvos.haren.TaskIndexer;

/**
 * Decorator that reverses a {@link SingleIntraThreadSchedulingFunction}.
 */
class ReverseIntraThreadSchedulingFunction implements
    SingleIntraThreadSchedulingFunction {

  private static final double PREVENT_DIV_ZERO = Math.pow(10, -10);
  private final SingleIntraThreadSchedulingFunction original;

  ReverseIntraThreadSchedulingFunction(SingleIntraThreadSchedulingFunction original) {
    this.original = original;
  }

  @Override
  public double apply(Task task, TaskIndexer indexer, double[][] features) {
    return 1 / (original.apply(task, indexer, features) + PREVENT_DIV_ZERO);
  }

  @Override
  public Feature[] requiredFeatures() {
    return original.requiredFeatures();
  }

  @Override
  public SingleIntraThreadSchedulingFunction enableCaching(int nTasks) {
    return original.enableCaching(nTasks);
  }

  @Override
  public void clearCache() {
    original.clearCache();
  }

  @Override
  public void reset(int taskCapacity) {
    original.reset(taskCapacity);
  }

  @Override
  public boolean cachingEnabled() {
    return original.cachingEnabled();
  }

  @Override
  public String name() {
    return original.name() + "_reverse";
  }
}
