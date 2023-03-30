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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@RequiredArgsConstructor
public class SamplingStepExecutionListener implements StepExecutionListener {

    private final SamplingServiceFactory samplingServiceFactory;
    private final SamplingDataPrinter samplingDataPrinter;
    private final List<Class<?>> sampledClasses = new ArrayList<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        sampledClasses.stream().collect(toMap(identity(), contextClass -> samplingServiceFactory.forClass(contextClass).getSamplingData()))
                .forEach(samplingDataPrinter::printForClass);
        sampledClasses.stream().map(samplingServiceFactory::forClass).forEach(SamplingService::reset);
        return null;
    }

    public void setSampledClasses(Class<?>... classes) {
        sampledClasses.addAll(Arrays.asList(classes));
    }
}
