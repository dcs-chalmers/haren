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
 * Utility class that assigns unique IDs to each feature type and provides some sane defaults.
 */
public final class FeatureHelper {

  public static final double NO_ARRIVAL_TIME = Double.MAX_VALUE;
  public static final long MAX_QUEUE_SIZE = 10000;
  public static int CTYPE_SOURCE = 0;
  public static int CTYPE_SINK = 1;
  public static int CTYPE_OPERATOR = 2;
  public static int CTYPE_ROUTER = 3;
  public static int CTYPE_UNION = 4;
  public static int CTYPE_JOIN = 5;

  private FeatureHelper() {
  }

  public static double getLatency(double arrivalTime, long currentTime) {
    return (arrivalTime < 0) ? 0 : currentTime - arrivalTime;
  }

  public static double getHeadLatency(double[] features, long currentTime) {
    return getLatency(features[Features.HEAD_ARRIVAL_TIME.index()], currentTime);
  }

  public static double getAverageLatency(double[] features, long currentTime) {
    return getLatency(features[Features.AVERAGE_ARRIVAL_TIME.index()], currentTime);
  }

  public static boolean noArrivalTime(double arrivalTime) {
    return arrivalTime < 0;
  }

  public static boolean noArrivalTime(long arrivalTime) {
    return arrivalTime < 0;
  }

}
