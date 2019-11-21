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
import io.palyvos.haren.Features;
import io.palyvos.haren.Task;
import io.palyvos.haren.TaskIndexer;
import java.util.List;
import org.apache.commons.lang3.Validate;

/**
 * Abstract implementation of {@link InterThreadSchedulingFunction}, handling basic functionality.
 */
public abstract class AbstractInterThreadSchedulingFunction
    implements InterThreadSchedulingFunction {

  private final Features[] requiredFeatures;
  private final String name;
  protected double[][] features;
  protected List<Task> tasks;
  protected TaskIndexer indexer;

  /**
   * Construct.
   *
   * @param name The function's name.
   * @param requiredFeatures The features used by this function.
   */
  protected AbstractInterThreadSchedulingFunction(String name, Features... requiredFeatures) {
    Validate.notEmpty(name);
    this.requiredFeatures = requiredFeatures;
    this.name = name;
  }

  @Override
  public void reset(List<Task> tasks, int taskCapacity, TaskIndexer indexer, double[][] features) {
    this.features = features;
    this.tasks = tasks;
    this.indexer = indexer;
  }

  public Feature[] requiredFeatures() {
    return requiredFeatures.clone();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return name();
  }
}
