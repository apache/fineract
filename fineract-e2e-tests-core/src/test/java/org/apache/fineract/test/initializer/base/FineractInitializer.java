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
package org.apache.fineract.test.initializer.base;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.test.helper.BusinessDateHelper;
import org.apache.fineract.test.initializer.global.FineractGlobalInitializerStep;
import org.apache.fineract.test.initializer.scenario.FineractScenarioInitializerStep;
import org.apache.fineract.test.initializer.suite.FineractSuiteInitializerStep;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
@RequiredArgsConstructor
public class FineractInitializer implements InitializingBean {

    public static final String DATE_FORMAT = "dd MMMM yyyy";

    private final List<FineractGlobalInitializerStep> globalInitializerSteps;
    private final List<FineractSuiteInitializerStep> suiteInitializerSteps;
    private final List<FineractScenarioInitializerStep> scenarioInitializerSteps;
    private final BusinessDateHelper businessDateHelper;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (log.isDebugEnabled()) {
            String globalInitializers = globalInitializerSteps.stream().map(Object::getClass).map(Class::getName)
                    .collect(Collectors.joining(", "));
            String suiteInitializers = suiteInitializerSteps.stream().map(Object::getClass).map(Class::getName)
                    .collect(Collectors.joining(", "));
            String scenarioInitializers = scenarioInitializerSteps.stream().map(Object::getClass).map(Class::getName)
                    .collect(Collectors.joining(", "));
            log.debug("""
                    The following initializers have been configured:
                    Global initializers: [{}]
                    Suite initializers: [{}]
                    Scenario initializers: [{}]
                    """, globalInitializers, suiteInitializers, scenarioInitializers);
        }
    }

    public void setupGlobalDefaults() throws Exception {
        for (FineractGlobalInitializerStep initializerStep : globalInitializerSteps) {
            initializerStep.initialize();
        }
        businessDateHelper.setBusinessDateToday();
    }

    public void setupDefaultsForSuite() throws Exception {
        for (FineractSuiteInitializerStep initializerStep : suiteInitializerSteps) {
            initializerStep.initializeForSuite();
        }
        businessDateHelper.setBusinessDateToday();
    }

    public void setupDefaultsForScenario() throws Exception {
        for (FineractScenarioInitializerStep scenarioInitializerStep : scenarioInitializerSteps) {
            scenarioInitializerStep.initializeForScenario();
        }
        businessDateHelper.setBusinessDateToday();
    }

    public void resetDefaultsAfterSuite() throws Exception {
        for (FineractSuiteInitializerStep initializerStep : suiteInitializerSteps) {
            initializerStep.resetAfterSuite();
        }
        businessDateHelper.setBusinessDateToday();
    }
}
