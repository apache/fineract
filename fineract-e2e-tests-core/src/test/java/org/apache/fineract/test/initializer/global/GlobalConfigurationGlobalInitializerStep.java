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

import lombok.RequiredArgsConstructor;
import org.apache.fineract.test.helper.GlobalConfigurationHelper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalConfigurationGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String CONFIG_KEY_ENABLE_ADDRESS = "Enable-Address";
    public static final String CONFIG_KEY_ENABLE_INTEREST_CALCULATION = "interest-charged-from-date-same-as-disbursal-date";
    public static final String CONFIG_KEY_ENABLE_BUSINESS_DATE = "enable_business_date";
    public static final String CONFIG_KEY_ENABLE_RECALCULATE_COB_DATE = "enable_automatic_cob_date_adjustment";
    public static final String CONFIG_KEY_DAYS_BEFORE_REPAYMENT_IS_DUE = "days-before-repayment-is-due";
    public static final String CONFIG_KEY_DAYS_AFTER_REPAYMENT_IS_OVERDUE = "days-after-repayment-is-overdue";
    public static final String CONFIG_KEY_ENABLE_AUTO_GENERATED_EXTERNAL_ID = "enable-auto-generated-external-id";

    private final GlobalConfigurationHelper globalConfigurationHelper;

    @Override
    public void initialize() throws Exception {
        globalConfigurationHelper.disableGlobalConfiguration(CONFIG_KEY_ENABLE_ADDRESS, 0L);
        globalConfigurationHelper.enableGlobalConfiguration(CONFIG_KEY_ENABLE_INTEREST_CALCULATION, 0L);
        globalConfigurationHelper.enableGlobalConfiguration(CONFIG_KEY_ENABLE_BUSINESS_DATE, 0L);
        globalConfigurationHelper.enableGlobalConfiguration(CONFIG_KEY_ENABLE_RECALCULATE_COB_DATE, 0L);
        globalConfigurationHelper.enableGlobalConfiguration(CONFIG_KEY_DAYS_BEFORE_REPAYMENT_IS_DUE, 1L);
        globalConfigurationHelper.enableGlobalConfiguration(CONFIG_KEY_DAYS_AFTER_REPAYMENT_IS_OVERDUE, 2L);
        globalConfigurationHelper.enableGlobalConfiguration(CONFIG_KEY_ENABLE_AUTO_GENERATED_EXTERNAL_ID, 0L);
    }
}
