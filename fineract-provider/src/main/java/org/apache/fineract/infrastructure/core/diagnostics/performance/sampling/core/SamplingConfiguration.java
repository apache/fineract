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

import com.google.common.base.Splitter;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class SamplingConfiguration implements InitializingBean {

    private final Set<Class<?>> classesToSample = ConcurrentHashMap.newKeySet();

    private final FineractProperties properties;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!properties.getSampling().isEnabled()) {
            return;
        }
        String sampledClasses = properties.getSampling().getSampledClasses();
        Iterable<String> fqdns = Splitter.on(',').split(sampledClasses);
        for (String fqdn : fqdns) {
            String trimmedFqdn = fqdn.trim();
            try {
                Class<?> aClass = Class.forName(trimmedFqdn);
                classesToSample.add(aClass);
            } catch (Exception e) {
                // ignore exception and proceed with other classes to sample
                log.info("Cannot load class for sampling [{}]", trimmedFqdn);
            }
        }
    }

    public boolean isSamplingConfiguredForClass(Class<?> contextClass) {
        return classesToSample.stream().anyMatch(c -> c.isAssignableFrom(contextClass));
    }

    public boolean isSamplingEnabled() {
        return properties.getSampling().isEnabled();
    }

    public int getSamplingRate() {
        return properties.getSampling().getSamplingRate();
    }

    public Duration getResetPeriod() {
        return Duration.ofSeconds(properties.getSampling().getResetPeriodSec());
    }
}
