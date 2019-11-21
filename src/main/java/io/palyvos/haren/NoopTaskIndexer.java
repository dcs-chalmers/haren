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

import java.util.Collection;

/**
 * Task indexer that does not support live reconfigurations and instead assigns every task its
 * default index.
 */
public class NoopTaskIndexer implements TaskIndexer {

  @Override
  public int schedulerIndex(Task task) {
    return task.getIndex();
  }

  @Override
  public void registerTasks(Collection<Task> tasks) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unregisterTasks(Collection<Task> tasks) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexedTasks() {
    throw new UnsupportedOperationException();
  }
}
