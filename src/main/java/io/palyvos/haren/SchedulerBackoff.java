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

import static java.lang.Thread.sleep;

import java.util.Random;
import org.apache.commons.lang3.Validate;

/**
 * Backoff utility class that sleeps for exponentially increasing times every time backoff is
 * called.
 *
 * <p>This is a stateful and <emph>NOT thread-safe</emph> object.
 *
 * <p>Every time {@link SchedulerBackoff#backoff(long)} is called, the calling thread sleeps for a
 * random duration in the interval {@code [0, limit]} milliseconds, respecting the passed maximum
 * delay. The range of the limit is defined in the constructor (parameters {@code min, max} and it
 * is exponentially increased for every {@code retries} consecutive calls for the backoff function.
 * A similar exponential reduction of the limit happens when consecutive calls to the relax function
 * happen.
 */
class SchedulerBackoff {

  private final long min, max;
  private final int retries;
  private final Random rand = new Random();
  private long currentLimit, currentRetries;

  /**
   * Construct a new {@link SchedulerBackoff} object with the given configuration. The parameters
   * given in this constructor will control the behavior of the algorithm in this specific instance.
   *
   * @param min The minimum backoff limit in millisec when a {@link SchedulerBackoff#backoff(long)}
   *     is called.
   * @param max The maximum backoff limit, in millisec when a {@link SchedulerBackoff#backoff(long)}
   *     is called.
   * @param retries The number of consecutive calls to {@link SchedulerBackoff#backoff(long)} or
   *     {@link SchedulerBackoff#relax()} that will actually cause a change in the backoff time.
   */
  public SchedulerBackoff(long min, long max, int retries) {
    this.min = Math.max(min, 1);
    this.max = Math.max(max, 1);
    Validate.isTrue(this.min > 0);
    Validate.isTrue(this.min <= this.max);
    this.retries = retries;
    this.currentLimit = min;
    this.currentRetries = retries;
  }

  /**
   * Backoff, i.e., put the calling thread to sleep for an exponentially increasing number of time.
   *
   * @param maxDelayMillis The maximum sleep time for this call, in milliseconds
   */
  public void backoff(long maxDelayMillis) {
    long delay = rand.nextLong() % currentLimit;
    currentRetries--;
    if (currentRetries == 0) {
      currentLimit = Math.min(2 * currentLimit, max);
      currentRetries = retries;
    }
    final long sleepTime = Math.min(delay, maxDelayMillis);
    if (sleepTime > 0) {
      try {
        sleep(sleepTime);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  /** Reduce the backoff limit. */
  public void relax() {
    if (currentRetries < retries) {
      currentRetries++;
      if (currentRetries == retries) {
        currentLimit = Math.max(currentLimit / 2, min);
      }
    }
  }

  public void reset() {
    currentLimit = min;
    currentRetries = retries;
  }
}
