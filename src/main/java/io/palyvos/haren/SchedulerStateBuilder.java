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

import io.palyvos.haren.function.VectorIntraThreadSchedulingFunction;
import io.palyvos.haren.function.InterThreadSchedulingFunction;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Builder class for {@link SchedulerState}.
 */
public class SchedulerStateBuilder {

  private int nTasks;
  private VectorIntraThreadSchedulingFunction intraThreadSchedulingFunction;
  private InterThreadSchedulingFunction interThreadSchedulingFunction;
  private boolean priorityCaching;
  private int nThreads;
  private long schedulingPeriod;
  private int batchSize;

  public SchedulerStateBuilder setTaskNumber(int nTasks) {
    this.nTasks = nTasks;
    return this;
  }

  public SchedulerStateBuilder setIntraThreadSchedulingFunction(
      VectorIntraThreadSchedulingFunction intraThreadSchedulingFunction) {
    this.intraThreadSchedulingFunction = intraThreadSchedulingFunction;
    return this;
  }

  public SchedulerStateBuilder setInterThreadSchedulingFunction(
      InterThreadSchedulingFunction interThreadSchedulingFunction) {
    this.interThreadSchedulingFunction = interThreadSchedulingFunction;
    return this;
  }

  public SchedulerStateBuilder setPriorityCaching(boolean priorityCaching) {
    this.priorityCaching = priorityCaching;
    return this;
  }

  public SchedulerStateBuilder setThreadNumber(int nThreads) {
    this.nThreads = nThreads;
    return this;
  }

  public SchedulerStateBuilder setSchedulingPeriod(long schedulingPeriod) {
    this.schedulingPeriod = schedulingPeriod;
    return this;
  }

  public SchedulerStateBuilder setBatchSize(int batchSize) {
    this.batchSize = batchSize;
    return this;
  }

  public SchedulerState createSchedulerState() {
    return new SchedulerState(nTasks, intraThreadSchedulingFunction, interThreadSchedulingFunction,
        priorityCaching, nThreads, schedulingPeriod, batchSize);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("nTasks", nTasks)
        .append("nThreads", nThreads)
        .append("priorityCaching", priorityCaching)
        .append("intraThreadSchedulingFunction", intraThreadSchedulingFunction)
        .append("interThreadSchedulingFunction", interThreadSchedulingFunction)
        .append("schedulingPeriod", schedulingPeriod)
        .append("batchSize", batchSize)
        .toString();
  }
}