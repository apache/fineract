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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData.InterestCalculationType;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData.InterestCompoundingPeriodType;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData.InterestPostingPeriodType;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the {@code disallow-backdated-transactions} global configuration (FINERACT-1950).
 */
public class DisallowBackdatedTransactionsIntegrationTest {

    private final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
    private final FeignGlobalConfigurationHelper globalConfigurationHelper = new FeignGlobalConfigurationHelper(fineractClient);
    private final FeignSavingsHelper savingsHelper = new FeignSavingsHelper(fineractClient);
    private final FeignSavingsTransactionHelper savingsTransactionHelper = new FeignSavingsTransactionHelper(fineractClient);

    @Test
    public void backdatedTransactionsAreRejectedOnlyWhileConfigurationEnabled() {
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(CommonConstants.DATE_FORMAT, Locale.US);
        // the allowed window is anchored to the tenant's business date, so "today" must use the tenant timezone
        final LocalDate today = Utils.getLocalDateOfTenant();
        final String backdatedDate = dateFormatter.format(today.minusMonths(2));
        final String currentDate = dateFormatter.format(today);
        // opened six months ago rather than on the 2013 helper defaults: the server wide "Post Interest For Savings"
        // job replays the whole life of every active account, so a decade old account slows down unrelated tests
        final String openedOnDate = dateFormatter.format(today.minusMonths(6));

        final Long clientID = new FeignClientHelper(fineractClient).createClient();
        final Long savingsProductID = new FeignSavingsProductHelper(fineractClient).createSavingsProduct(
                SavingsRequestBuilders.savingsProduct(InterestCompoundingPeriodType.DAILY, InterestPostingPeriodType.QUARTERLY,
                        InterestCalculationType.DAILY_BALANCE).minRequiredOpeningBalance(new BigDecimal("100")))
                .getResourceId();
        final Long savingsId = savingsHelper.submitApplication(
                SavingsRequestBuilders.submitSavingsApplication(clientID, savingsProductID, openedOnDate).withdrawalFeeForTransfers(false))
                .getSavingsId();
        savingsHelper.approveSavings(savingsId, openedOnDate);
        savingsHelper.activateSavings(savingsId, openedOnDate);

        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.DISALLOW_BACKDATED_TRANSACTIONS,
                new PutGlobalConfigurationsRequest().enabled(true));
        try {
            // backdated deposit is rejected while the configuration is enabled
            final CallFailedRuntimeException error = savingsTransactionHelper.depositExpectingError(savingsId, "100", backdatedDate);
            assertEquals(403, error.getStatus());
            assertEquals("error.msg.transaction.backdated.not.allowed", FeignErrors.firstError(error).userMessageGlobalisationCode());

            // current-date deposit is still allowed
            assertNotNull(savingsTransactionHelper.deposit(savingsId, "100", currentDate).getResourceId());

            // a tolerance window (in days) allows backdating within it
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.DISALLOW_BACKDATED_TRANSACTIONS,
                    new PutGlobalConfigurationsRequest().enabled(true).value(90L));
            assertNotNull(savingsTransactionHelper.deposit(savingsId, "100", backdatedDate).getResourceId());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.DISALLOW_BACKDATED_TRANSACTIONS,
                    new PutGlobalConfigurationsRequest().enabled(false).value(0L));
        }

        // with the configuration disabled again, backdated deposits pass
        assertNotNull(savingsTransactionHelper.deposit(savingsId, "100", backdatedDate).getResourceId());

        savingsHelper.closeSavings(savingsId, currentDate, true);
    }
}
