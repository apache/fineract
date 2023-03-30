/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.service.performance.sampling;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.StatUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SamplingDataPrinter {

    public void printForClass(Class<?> clazz, SamplingData samplingData) {
        if (clazz != null && samplingData != null) {
            Map<String, List<Duration>> timings = samplingData.getTimings();
            if (!timings.isEmpty()) {
                log.info("""

                        Sampling data for {}
                        -------------
                        {}
                        """, clazz.getName(), getTimingsLog(timings));
            }
        }
    }

    private String getTimingsLog(Map<String, List<Duration>> timings) {
        return timings.entrySet().stream() //
                .map(e -> getSingleTimingLog(e.getKey(), e.getValue())) //
                .collect(Collectors.joining(System.lineSeparator())); //
    }

    private String getSingleTimingLog(String key, List<Duration> durations) {
        double[] millis = durations.stream().mapToLong(Duration::toMillis).asDoubleStream().toArray();
        double highest = StatUtils.max(millis);
        double average = Arrays.stream(millis).average().orElse(Double.NaN);
        double median = StatUtils.percentile(millis, 50);
        double lowest = StatUtils.min(millis);
        return "%s with %d data points -> highest: %.0fms, average: %.0fms, median: %.0fms, lowest: %.0fms".formatted(key, millis.length,
                highest, average, median, lowest);
    }
}
