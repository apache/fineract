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
package org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.output;

import static java.lang.System.lineSeparator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core.SamplingData;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SamplingDataPrinter {

    @SuppressFBWarnings({ "SLF4J_FORMAT_SHOULD_BE_CONST" })
    public void print(Map<Class<?>, SamplingData> data) {
        if (log.isInfoEnabled()) {
            if (MapUtils.isNotEmpty(data)) {
                String logMsg = data.entrySet() //
                        .stream() //
                        .map(e -> getFormattedSamplingData(e.getKey(), e.getValue())) //
                        .filter(Objects::nonNull).collect(Collectors.joining(lineSeparator())); //
                if (StringUtils.isNotBlank(logMsg)) {
                    log.info(logMsg);
                }
            }
        }
    }

    @SuppressFBWarnings({ "VA_FORMAT_STRING_USES_NEWLINE" })
    private String getFormattedSamplingData(Class<?> clazz, SamplingData samplingData) {
        if (clazz != null && samplingData != null) {
            Map<String, List<Duration>> timings = samplingData.getTimings();
            if (!timings.isEmpty()) {
                return """

                        Sampling data for %s
                        -------------
                        %s
                        """.formatted(clazz.getName(), getTimingsLog(timings));
            }
        }
        return null;
    }

    private String getTimingsLog(Map<String, List<Duration>> timings) {
        return timings.entrySet().stream() //
                .map(e -> getSingleTimingLog(e.getKey(), e.getValue())) //
                .collect(Collectors.joining(lineSeparator())); //
    }

    private String getSingleTimingLog(String key, List<Duration> durations) {
        double[] millis = durations.stream().mapToLong(Duration::toMillis).asDoubleStream().toArray();
        double percentile99 = StatUtils.percentile(millis, 99);
        double average = Arrays.stream(millis).average().orElse(Double.NaN);
        double median = StatUtils.percentile(millis, 50);
        double lowest = StatUtils.min(millis);
        return "%s with %d data points -> 99th percentile: %.0fms, average: %.0fms, median: %.0fms, lowest: %.0fms".formatted(key,
                millis.length, percentile99, average, median, lowest);
    }
}
