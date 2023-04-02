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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SamplingServiceFactory {

    private final Map<Class<?>, SamplingService> services = new ConcurrentHashMap<>();
    private final SamplingConfiguration samplingConfiguration;

    public SamplingService forClass(Class<?> contextClass) {
        return services.computeIfAbsent(contextClass, (cc) -> {
            if (samplingConfiguration.isSamplingEnabled() && samplingConfiguration.isSamplingConfiguredForClass(contextClass)) {
                return new InMemorySamplingService(samplingConfiguration.getSamplingRate());
            } else {
                return new NoopSamplingService();
            }
        });
    }

    public void doWithAll(Consumer<Map<Class<?>, SamplingService>> c) {
        c.accept(Map.copyOf(services));
    }
}
