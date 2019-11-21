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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * Default implementatin of {@link VectorIntraThreadSchedulingFunction}. Generates a priority
 * vector. The value of each element of the vector is derived by one {@link
 * SingleIntraThreadSchedulingFunction}.
 */
public class VectorIntraThreadSchedulingFunctionImpl
    implements VectorIntraThreadSchedulingFunction {

  protected final SingleIntraThreadSchedulingFunction[] functions;
  private final Feature[] requiredFeatures;
  private final String name;
  private boolean caching;

  /**
   * Construct.
   *
   * @param functions The {@link SingleIntraThreadSchedulingFunction}s that will generate the
   *     priority vector.
   */
  public VectorIntraThreadSchedulingFunctionImpl(SingleIntraThreadSchedulingFunction... functions) {
    Validate.notEmpty(functions, "At least one function is needed!");
    this.functions = functions;
    Set<Feature> functionFeatures = new HashSet<>();
    StringBuilder nameBuilder = new StringBuilder("Composite:");
    for (SingleIntraThreadSchedulingFunction function : functions) {
      functionFeatures.addAll(Arrays.asList(function.requiredFeatures()));
      nameBuilder.append(function.name()).append(",");
    }
    this.name = nameBuilder.toString();
    this.requiredFeatures = functionFeatures.toArray(new Feature[0]);
  }

  @Override
  public void apply(Task task, TaskIndexer indexer, double[][] features, double[] output) {
    Validate.isTrue(output.length == functions.length);
    for (int k = 0; k < output.length; k++) {
      output[k] = functions[k].apply(task, indexer, features);
    }
  }

  @Override
  public Feature[] requiredFeatures() {
    return requiredFeatures;
  }

  @Override
  public VectorIntraThreadSchedulingFunction enableCaching(int nTasks) {
    this.caching = true;
    for (SingleIntraThreadSchedulingFunction function : functions) {
      function.enableCaching(nTasks);
    }
    return this;
  }

  @Override
  public void clearCache() {
    for (SingleIntraThreadSchedulingFunction function : functions) {
      function.clearCache();
    }
  }

  @Override
  public void reset(int taskCapacity) {
    for (SingleIntraThreadSchedulingFunction function : functions) {
      function.reset(taskCapacity);
    }
  }

  @Override
  public boolean cachingEnabled() {
    return caching;
  }

  @Override
  public int dimensions() {
    return functions.length;
  }

  @Override
  public boolean isReverseOrder(int i) {
    return functions[i].isReverseOrder();
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
