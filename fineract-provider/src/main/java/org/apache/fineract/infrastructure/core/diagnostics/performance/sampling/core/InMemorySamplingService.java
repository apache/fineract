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
package org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InMemorySamplingService extends AbstractSamplingService {

    private final Map<String, List<Duration>> timings = new ConcurrentHashMap<>();

    InMemorySamplingService(int samplingRate) {
        super(samplingRate);
    }

    @Override
    public SamplingData getSamplingData() {
        return new SamplingData(timings);
    }

    @Override
    protected void takeSample(String key, Duration duration) {
        try {
            timings.merge(key, new CopyOnWriteArrayList<>(List.of(duration)), (oldValues, newValue) -> {
                oldValues.addAll(newValue);
                return oldValues;
            });
        } catch (RuntimeException e) {
            log.error("Error while sampling for key [{}]", key, e);
        }
    }

    @Override
    protected void doReset() {
        timings.clear();
    }
}
