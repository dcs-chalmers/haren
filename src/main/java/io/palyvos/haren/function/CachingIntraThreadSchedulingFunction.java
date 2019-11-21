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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base implementation of an {@link AbstractIntraThreadSchedulingFunction} with caching support.
 */
public abstract class CachingIntraThreadSchedulingFunction extends
    AbstractIntraThreadSchedulingFunction {

  private static final Logger LOG = LogManager.getLogger();
  private double[] cache;
  private boolean caching;

  /**
   * Construct.
   *
   * @param name The function's name.
   * @param features The features used by this function.
   */
  public CachingIntraThreadSchedulingFunction(String name, Features... features) {
    super(name, features);
  }

  /**
   * Construct.
   *
   * @param name The functions name.
   * @param dependentFunctions Other {@link SingleIntraThreadSchedulingFunction}s used by this
   *     function.
   */
  public CachingIntraThreadSchedulingFunction(String name,
      SingleIntraThreadSchedulingFunction... dependentFunctions) {
    super(name, dependentFunctions);
  }

  /**
   * Template method in place of {@link SingleIntraThreadSchedulingFunction#apply(Task, TaskIndexer, double[][])}, which automatically enforces
   * caching.
   *
   * @param task
   * @param indexer
   * @param features
   * @return
   */
  protected abstract double applyWithCachingSupport(Task task, TaskIndexer indexer,
      double[][] features);

  @Override
  public final double apply(Task task, TaskIndexer indexer, double[][] features) {
    if (caching) {
      if (cache[indexer.schedulerIndex(task)] < 0) {
        double result = applyWithCachingSupport(task, indexer, features);
        cache[indexer.schedulerIndex(task)] = result;
        return result;
      }
      return cache[indexer.schedulerIndex(task)];
    }
    return applyWithCachingSupport(task, indexer, features);
  }

  @Override
  public SingleIntraThreadSchedulingFunction enableCaching(int nTasks) {
    LOG.info("Caching enabled for {}", name());
    super.enableCaching(nTasks);
    this.cache = new double[nTasks];
    this.caching = true;
    return this;
  }

  @Override
  public void clearCache() {
    super.clearCache();
    if (caching) {
      for (int i = 0; i < cache.length; i++) {
        cache[i] = -1;
      }
    }
  }

  @Override
  public void reset(int taskCapacity) {
    if (caching) {
      enableCaching(taskCapacity);
    }
  }

  @Override
  public boolean cachingEnabled() {
    return caching;
  }
}
