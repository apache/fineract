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
package org.apache.fineract.integrationtests;

import java.time.LocalDate;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountSubStatusEnumData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Validates the end-to-end execution of the partitioned Savings Close of Business (SAVINGS_COB) job triggered through
 * the scheduler (manager -> partitioner -> worker -> business steps), as opposed to the inline path covered by
 * {@link SavingsInlineCOBIntegrationTest}.
 */
public class SavingsCOBIntegrationTest extends FeignSavingsTestBase {

    private static final String SAVINGS_COB_JOB_DISPLAY_NAME = "Savings COB";

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    @Test
    public void partitionedSavingsCOBMovesAccountToInactiveSubStatus() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));

        final LocalDate activationDate = LocalDate.of(2024, 1, 1);
        // Start with the business/COB date around activation; the account is funded and not yet processed by COB.
        updateBusinessDate(BusinessDateType.BUSINESS_DATE, activationDate);
        updateBusinessDate(BusinessDateType.COB_DATE, activationDate.minusDays(1));

        final Long clientId = createClient();
        Assertions.assertNotNull(clientId);

        final Long savingsProductId = createDormancyTrackingSavingsProduct();
        Assertions.assertNotNull(savingsProductId);

        final Long savingsId = submitSavingsApplication(clientId, savingsProductId, "01 January 2024").getSavingsId();
        Assertions.assertNotNull(savingsId);

        approveSavings(savingsId, "01 January 2024");
        activateSavings(savingsId, "01 January 2024");
        deposit(savingsId, "5000", "01 January 2024");

        SavingsAccountSubStatusEnumData subStatus = savingsHelper.getSavingsSubStatus(savingsId);
        Assertions.assertTrue(subStatus.getNone(), "Savings sub-status should be NONE right after activation");

        // Advance the COB date past the product inactivity threshold (30 days) and run the full partitioned Savings COB
        // job through the scheduler.
        final LocalDate cobDate = activationDate.plusDays(45);
        updateBusinessDate(BusinessDateType.BUSINESS_DATE, cobDate.plusDays(1));
        updateBusinessDate(BusinessDateType.COB_DATE, cobDate);

        schedulerHelper.executeAndAwaitJob(SAVINGS_COB_JOB_DISPLAY_NAME);

        subStatus = savingsHelper.getSavingsSubStatus(savingsId);
        Assertions.assertTrue(subStatus.getInactive(), "Savings sub-status should be INACTIVE after the partitioned Savings COB");
    }

    private void updateBusinessDate(final BusinessDateType type, final LocalDate date) {
        businessDateHelper.updateBusinessDate(type.name(), date.toString());
    }

    private Long createDormancyTrackingSavingsProduct() {
        // defaultSavingsProduct() already configures daily compounding, monthly posting and daily-balance calculation;
        // here we only add the dormancy thresholds the UpdateSavingsDormantStatusBusinessStep relies on.
        final PostSavingsProductsRequest request = SavingsRequestBuilders.defaultSavingsProduct() //
                .isDormancyTrackingActive(true) //
                .daysToInactive(30L) //
                .daysToDormancy(60L) //
                .daysToEscheat(90L);
        return createSavingsProduct(request).getResourceId();
    }
}
