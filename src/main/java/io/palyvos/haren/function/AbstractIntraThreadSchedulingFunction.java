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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * Abstract implementation of {@link SingleIntraThreadSchedulingFunction}, handling basic
 * functionality.
 */
public abstract class AbstractIntraThreadSchedulingFunction implements
    SingleIntraThreadSchedulingFunction {

  protected final SingleIntraThreadSchedulingFunction[] dependentFunctions;
  private final Feature[] requiredFeatures;
  private final String name;
  private boolean caching;

  /**
   * Construct.
   *
   * @param name The function's name.
   * @param requiredFeatures The features used by this function.
   */
  protected AbstractIntraThreadSchedulingFunction(String name, Features... requiredFeatures) {
    Validate.notEmpty(requiredFeatures, "Priority function has no features!");
    Validate.notEmpty(name);
    this.requiredFeatures = requiredFeatures;
    this.dependentFunctions = new SingleIntraThreadSchedulingFunction[0];
    this.name = name;
  }

  /**
   * Construct.
   *
   * @param name The functions name.
   * @param dependentFunctions Other {@link SingleIntraThreadSchedulingFunction}s used by this
   *     function.
   */
  protected AbstractIntraThreadSchedulingFunction(String name,
      SingleIntraThreadSchedulingFunction... dependentFunctions) {
    Validate.notEmpty(name);
    Validate.notEmpty(dependentFunctions, "Priority function depends on no other function!");
    Set<Feature> features = new HashSet<>();
    for (SingleIntraThreadSchedulingFunction function : dependentFunctions) {
      features.addAll(Arrays.asList(function.requiredFeatures()));
    }
    this.requiredFeatures = features.toArray(new Feature[0]);
    this.dependentFunctions = dependentFunctions;
    this.name = name;
  }

  @Override
  public SingleIntraThreadSchedulingFunction enableCaching(int nTasks) {
    caching = true;
    for (SingleIntraThreadSchedulingFunction function : dependentFunctions) {
      function.enableCaching(nTasks);
    }
    return this;
  }

  @Override
  public void clearCache() {
    for (SingleIntraThreadSchedulingFunction function : dependentFunctions) {
      function.clearCache();
    }
  }

  @Override
  public boolean cachingEnabled() {
    return caching;
  }

  @Override
  public final Feature[] requiredFeatures() {
    return requiredFeatures.clone();
  }

  @Override
  public final String name() {
    return name;
  }

  @Override
  public String toString() {
    return name();
  }
}
