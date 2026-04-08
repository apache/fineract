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

import static org.apache.fineract.infrastructure.core.diagnostics.performance.MeasuringUtil.measure;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public abstract class AbstractSamplingService implements SamplingService {

    private final Map<String, AtomicInteger> sampleCounters = new ConcurrentHashMap<>();
    private final int samplingRate;

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public AbstractSamplingService(int samplingRate) {
        if (samplingRate < 1) {
            throw new IllegalArgumentException("samplingRate cannot be less than 1");
        }
        this.samplingRate = samplingRate;
    }

    @Override
    public void sample(String key, Runnable r) {
        sample(key, () -> {
            r.run();
            return null;
        });
    }

    @Override
    public <T> T sample(String key, Supplier<T> s) {
        AtomicInteger sampleCounter = sampleCounters.computeIfAbsent(key, k -> new AtomicInteger(0));
        int sampleCount = sampleCounter.getAndAccumulate(1, (index, inc) -> ++index >= samplingRate ? 0 : index);
        boolean shouldTakeSample = sampleCount == 0;

        if (shouldTakeSample) {
            return measure(s, duration -> takeSample(key, duration));
        } else {
            return s.get();
        }
    }

    @Override
    public void reset() {
        sampleCounters.clear();
        doReset();
    }

    /**
     * Subclasses can override this to do specific cleanup.
     */
    protected void doReset() {}

    protected abstract void takeSample(String key, Duration duration);
}
