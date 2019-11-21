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
 * A dependency relation towards a feature of the upstream of downstream tasks of another task.
 */
public final class FeatureDependency {

  final Feature feature;
  final TaskDependency[] dependencies;

  private FeatureDependency(Feature feature, TaskDependency... dependencies) {
    this.feature = feature;
    this.dependencies = dependencies == null ? new TaskDependency[0] : dependencies;
  }

  public static FeatureDependency of(Feature feature, TaskDependency... dependencies) {
    return new FeatureDependency(feature, dependencies);
  }

  public static FeatureDependency of(Feature feature) {
    return new FeatureDependency(feature, TaskDependency.UPSTREAM, TaskDependency.DOWNSTREAM);
  }
}
