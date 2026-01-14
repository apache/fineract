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
package org.apache.fineract.test.initializer.global;

import static org.apache.fineract.test.initializer.global.GlobalConfigurationGlobalInitializerStep.CONFIG_KEY_ENABLE_BUSINESS_DATE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.test.helper.BusinessDateHelper;
import org.apache.fineract.test.helper.GlobalConfigurationHelper;
import org.apache.fineract.test.initializer.scenario.FineractScenarioInitializerStep;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BusinessDateInitializerStep implements FineractScenarioInitializerStep {

    private final BusinessDateHelper businessDateHelper;
    private final GlobalConfigurationHelper configurationHelper;

    @Override
    public void initializeForScenario() {
        configurationHelper.enableGlobalConfiguration(CONFIG_KEY_ENABLE_BUSINESS_DATE, 0L);
        businessDateHelper.setBusinessDateToday();
    }

    @Override
    public void resetAfterScenario() {
        configurationHelper.enableGlobalConfiguration(CONFIG_KEY_ENABLE_BUSINESS_DATE, 0L);
        businessDateHelper.setBusinessDateToday();
        configurationHelper.disableGlobalConfiguration(CONFIG_KEY_ENABLE_BUSINESS_DATE, 0L);
    }
}
