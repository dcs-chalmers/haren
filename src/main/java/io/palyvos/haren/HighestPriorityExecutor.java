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

import java.util.concurrent.CyclicBarrier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The default {@link AbstractExecutor} implementation in {@link HarenScheduler}, which always
 * excutes the first (assigned) task that can run.
 */
class HighestPriorityExecutor extends AbstractExecutor {

  private static final Logger LOG = LogManager.getLogger();
  private int localIndex;
  private boolean equalPhase;
  private boolean[] isNextEqual;

  public HighestPriorityExecutor(int batchSize, int schedulingPeriodMillis,
      CyclicBarrier barrier, SchedulerState state) {
    super(state, barrier);
  }

  public HighestPriorityExecutor(SchedulerState state, CyclicBarrier barrier,
      int cpuId) {
    super(state, cpuId, barrier);
  }

  protected boolean runNextTask() {
    boolean didRun = false;
    for (; localIndex < executorTasks.size(); localIndex++) {
      if (finishedEqualPhase()) {
        resetEqualPhase();
      }
      Task task = executorTasks.get(localIndex);
      if (task.canRun() && task.runFor(state.batchSize())) {
//        if (equalPhase) {
//         LOG.info("Executed equal {}", task);
//        }
        mark(task, localIndex);
        didRun = true;
        break;
      }
    }
    // If a task ran and the next task has equal priority, continue execution from that one
    // This is called an "equal phase"
    if (didRun && isNextEqual[localIndex]) {
      enterEqualPhase();
    } else {
      // else start execution from highest priority
      resetEqualPhase();
    }
    return didRun;
  }

  @Override
  protected void onRoundStart() {
    markEqualPriorities();
    resetEqualPhase();
  }

  protected void markEqualPriorities() {
    isNextEqual = new boolean[executorTasks.size()];
    for (int i = 0; i < executorTasks.size() - 1; i++) {
      Task current = executorTasks.get(i);
      Task next = executorTasks.get(i + 1);
      if (state.comparator.compare(current, next) == 0) {
        isNextEqual[i] = true;
      }
    }
  }

  private void resetEqualPhase() {
    localIndex = 0;
    equalPhase = false;
  }

  private void enterEqualPhase() {
    equalPhase = true;
    localIndex = localIndex + 1;
  }

  private boolean finishedEqualPhase() {
    return equalPhase && !isNextEqual[localIndex - 1];
  }

}
