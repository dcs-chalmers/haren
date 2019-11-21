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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public enum Features implements Feature {
  TOPOLOGICAL_ORDER(0, true),
  SELECTIVITY(1, false),
  COST(2, false),
  HEAD_ARRIVAL_TIME(3, false),
  AVERAGE_ARRIVAL_TIME(4, false),
  COMPONENT_TYPE(5, true),
  RATE(6, false),
  USER_PRIORITY(7, true),
  INPUT_QUEUE_SIZE(8, false),
  OUTPUT_QUEUE_SIZE(9, false);

  public static final Map<Feature, FeatureDependency[]> dependencies;
  private static final FeatureDependency[] NO_DEPENDENCIES = new FeatureDependency[0];

  static {
    Map<Feature, FeatureDependency[]> deps = new HashMap<>();
    deps.put(HEAD_ARRIVAL_TIME,
        new FeatureDependency[]{FeatureDependency.of(HEAD_ARRIVAL_TIME),
            FeatureDependency.of(AVERAGE_ARRIVAL_TIME)});
    deps.put(AVERAGE_ARRIVAL_TIME,
        new FeatureDependency[]{FeatureDependency.of(HEAD_ARRIVAL_TIME),
            FeatureDependency.of(AVERAGE_ARRIVAL_TIME)});
    deps.put(INPUT_QUEUE_SIZE,
        new FeatureDependency[]{FeatureDependency.of(INPUT_QUEUE_SIZE, TaskDependency.DOWNSTREAM),
        });
    deps.put(OUTPUT_QUEUE_SIZE,
        new FeatureDependency[]{FeatureDependency.of(INPUT_QUEUE_SIZE, TaskDependency.DOWNSTREAM),
        });
    dependencies = Collections.unmodifiableMap(deps);
  }

  private final int index;
  private final boolean constant;

  Features(int index, boolean constant) {
    this.index = index;
    this.constant = constant;
  }

  public static int length() {
    return Features.values().length;
  }

  public static double[] createArray() {
    return new double[Features.length()];
  }

  @Override
  public int index() {
    return index;
  }

  @Override
  public boolean isConstant() {
    return constant;
  }

  @Override
  public double get(Task task, TaskIndexer indexer, double[][] features) {
    return features[indexer.schedulerIndex(task)][index];
  }

  @Override
  public FeatureDependency[] dependencies() {
    return dependencies.getOrDefault(this, NO_DEPENDENCIES);
  }
}
