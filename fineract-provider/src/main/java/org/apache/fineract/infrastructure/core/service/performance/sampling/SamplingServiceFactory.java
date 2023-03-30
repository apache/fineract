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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SamplingServiceFactory implements InitializingBean {

    private final Map<Class<?>, SamplingService> services = new ConcurrentHashMap<>();
    private final Set<String> classesToSample = ConcurrentHashMap.newKeySet();

    private final FineractProperties properties;

    @Override
    public void afterPropertiesSet() throws Exception {
        String sampledClasses = properties.getSampling().getSampledClasses();
        String[] fqdns = sampledClasses.split(",");
        Arrays.stream(fqdns).map(String::trim).forEach(classesToSample::add);
    }

    public SamplingService forClass(Class<?> contextClass) {
        return services.computeIfAbsent(contextClass, (cc) -> {
            if (isSamplingEnabled() && isSamplingConfiguredForClass(contextClass)) {
                return new InMemorySamplingService(getSamplingRate());
            } else {
                return new NoopSamplingService();
            }
        });
    }

    private boolean isSamplingConfiguredForClass(Class<?> contextClass) {
        return classesToSample.contains(contextClass.getName());
    }

    private boolean isSamplingEnabled() {
        return properties.getSampling().isEnabled();
    }

    private int getSamplingRate() {
        return properties.getSampling().getSamplingRate();
    }
}
