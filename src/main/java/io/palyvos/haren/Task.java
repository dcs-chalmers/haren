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

import java.util.List;

/**
 * A streaming task that can be scheduled, e.g., an operator, a source or a sink.
 */
public interface Task extends Runnable {

  /**
   * Run this task for a maximum number of times, allowing preemption afterwards.
   *
   * @param times The maximum number of times that this task will be executed.
   * @return {@code true} if this task actually executed at least once. Might return false, for
   *     example, if there was nothing to process and the task immediately exited.
   */
  boolean runFor(final int times);

  /**
   * Check if there is any work that can be executed by this task.
   *
   * @return {@code true} if this task can be executed and perform meaningful work.
   */
  boolean canRun();

  /**
   * Update the features of this task
   *
   * @param features The specific features to be updated.
   * @param output The array where feature values will be written (indexes will be decided based
   *     on Feature{@link #getIndex()})
   */
  void updateFeatures(Feature[] features, double[] output);

  /**
   * Do an internal refresh of execution-dependent features. If an operator maintains an running
   * average of its throughput, a call to this function might trigger an update of that average.
   */
  void refreshFeatures();

  /**
   * @return The unique numerical index of this task.
   */
  int getIndex();

  /**
   * Get all the upstream tasks in the query graph.
   *
   * @return A list of the upstream tasks.
   */
  List<? extends Task> getUpstream();

  /**
   * Get all the downstream tasks in the query graph.
   *
   * @return A list of the downstream tasks.
   */
  List<? extends Task> getDownstream();

}
