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

/**
 * An abstraction of anything that characterizes a {@link Task}'s state.
 */
public interface Feature {

  /**
   * @return The unique, numerical index of this feature.
   */
  int index();

  /**
   * Check if this is a constant or variable feature.
   *
   * @return {@code true} if the feature is constant.
   */
  boolean isConstant();

  /**
   * Get the value of this feature.
   *
   * @param task The task that we want to retrieve the feature for.
   * @param indexer
   * @param features The complete feature matrix of the SPE.
   * @return The value of this feature.
   */
  double get(Task task, TaskIndexer indexer, double[][] features);

  /**
   * Get the dependencies of this feature to other features and tasks. It is possible for a feature
   * to depend on itself (for example, when a feature changes, the same feature needs to be
   * updated for upstream or downstream tasks).
   *
   * @return An array of {@link FeatureDependency} objects.
   */
  FeatureDependency[] dependencies();
}
