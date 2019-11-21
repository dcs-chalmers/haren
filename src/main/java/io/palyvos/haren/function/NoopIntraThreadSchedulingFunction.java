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

import io.palyvos.haren.Features;
import io.palyvos.haren.Task;
import io.palyvos.haren.TaskIndexer;

public class NoopIntraThreadSchedulingFunction extends AbstractIntraThreadSchedulingFunction {

  public static final SingleIntraThreadSchedulingFunction INSTANCE =
      new NoopIntraThreadSchedulingFunction("NO-OP");

  private NoopIntraThreadSchedulingFunction(String name) {
    super(name, Features.COMPONENT_TYPE);
  }

  @Override
  public double apply(Task task, TaskIndexer indexer, double[][] features) {
    return 0;
  }

  @Override
  public void reset(int taskCapacity) {
  }
}
