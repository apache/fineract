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

import static java.util.stream.Collectors.toMap;

import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core.SamplingConfiguration;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core.SamplingData;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core.SamplingService;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core.SamplingServiceFactory;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.support.SamplingEnabledCondition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Conditional(SamplingEnabledCondition.class)
@Slf4j
public class SamplingScheduler implements InitializingBean {

    private final SamplingServiceFactory samplingServiceFactory;
    private final SamplingDataPrinter printer;
    private final TaskScheduler taskScheduler;
    private final SamplingConfiguration samplingConfiguration;

    @SuppressWarnings({ "FutureReturnValueIgnored" })
    @Override
    public void afterPropertiesSet() throws Exception {
        long resetPeriodInSec = samplingConfiguration.getResetPeriod().toSeconds();
        if (resetPeriodInSec < 10) {
            log.warn("Reset period for sampling cannot be smaller than 10 seconds, setting back the minimum 10");
            resetPeriodInSec = 10;
        }
        Duration resetPeriodInSecDuration = Duration.ofSeconds(resetPeriodInSec);
        PeriodicTrigger trigger = new PeriodicTrigger(resetPeriodInSecDuration);
        trigger.setInitialDelay(resetPeriodInSecDuration);
        taskScheduler.schedule(this::printAndResetPeriodically, trigger);
    }

    private void printAndResetPeriodically() {
        samplingServiceFactory.doWithAll((serviceMap) -> {
            Map<Class<?>, SamplingData> samplingDataMap = serviceMap.entrySet() //
                    .stream() //
                    .collect(toMap(Map.Entry::getKey, e -> e.getValue().getSamplingData())); //
            printer.print(samplingDataMap);
            serviceMap.values().forEach(SamplingService::reset);
        });
    }
}
