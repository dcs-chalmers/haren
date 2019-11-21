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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class that associates each task with the streaming query to which it belongs.
 */
public class QueryResolver {

  private static final Logger LOG = LogManager.getLogger();
  private final Map<Integer, List<Task>> queryToTasks = new HashMap<>();
  private final Map<Integer, Integer> taskToQuery = new HashMap<>();
  private int nQueries;

  public QueryResolver(List<Task> tasks) {
    resolveQueries(tasks);
  }

  public Collection<List<Task>> getQueries() {
    return queryToTasks.values();
  }

  private void resolveQueries(List<Task> tasks) {
    for (Task task : tasks) {
      traverseTaskGraph(task);
    }
    LOG.info("{} queries found", nQueries);
    for (int queryNumber : queryToTasks.keySet()) {
      LOG.debug("Query #{} -> {}", queryNumber, queryToTasks.get(queryNumber));
    }
  }

  private void traverseTaskGraph(Task task) {
    if (getOrZero(taskToQuery, task.getIndex()) > 0) { // Visited
      return;
    }
    nQueries += 1;
    final Deque<Task> q = new ArrayDeque<>();
    q.addLast(task);
    queryToTasks.put(nQueries, new ArrayList<>());
    while (!q.isEmpty()) {
      Task current = q.removeFirst();
      final int currentIndex = current.getIndex();
      if (getOrZero(taskToQuery, currentIndex) == 0) { // Not visited
        // Update both representations
        queryToTasks.get(nQueries).add(current);
        taskToQuery.put(currentIndex, nQueries);
        // Recursively for the rest of the graph
        current.getDownstream().forEach(t -> q.addLast(t));
        current.getUpstream().forEach(t -> q.addLast(t));
      }
    }
  }

  private int getOrZero(Map<Integer, Integer> map, int key) {
    Integer value = map.get(key);
    return value != null ? value : 0;
  }

}
