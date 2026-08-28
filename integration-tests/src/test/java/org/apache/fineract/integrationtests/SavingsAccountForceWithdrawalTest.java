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
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsProductsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class SavingsAccountForceWithdrawalTest extends FeignSavingsTestBase {

    private static final String DEPOSIT_DATE = "04 March 2013";
    private static final String WITHDRAWAL_DATE = "05 March 2013";
    private static final long FORCE_WITHDRAWAL_LIMIT = 5000L;
    private static final long DISABLED_LIMIT = 0L;

    @Test
    public void testForceWithdrawal() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_WITHDRAWAL_ON_SAVINGS_ACCOUNT,
                new PutGlobalConfigurationsRequest().enabled(true));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_WITHDRAWAL_ON_SAVINGS_ACCOUNT_LIMIT,
                new PutGlobalConfigurationsRequest().value(FORCE_WITHDRAWAL_LIMIT).enabled(true));

        GlobalConfigurationPropertyData config = globalConfigurationHelper
                .getGlobalConfigurationByName(GlobalConfigurationConstants.FORCE_WITHDRAWAL_ON_SAVINGS_ACCOUNT_LIMIT);
        assertEquals(FORCE_WITHDRAWAL_LIMIT, config.getValue());

        Long clientId = createClient();
        assertNotNull(clientId);

        PostSavingsProductsResponse savingsProduct = createSavingsProduct(
                SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                        SavingsTestData.InterestPostingPeriodType.DAILY, SavingsTestData.InterestCalculationType.DAILY_BALANCE));
        assertNotNull(savingsProduct.getResourceId());

        Long savingsId = submitSavingsApplication(clientId, savingsProduct.getResourceId(), DEPOSIT_DATE).getSavingsId();
        approveSavings(savingsId, DEPOSIT_DATE);
        activateSavings(savingsId, DEPOSIT_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        deposit(savingsId, "100", DEPOSIT_DATE);

        PostSavingsAccountTransactionsResponse response = savingsTransactionHelper.forceWithdraw(savingsId, "200", WITHDRAWAL_DATE);

        assertNotNull(response.getResourceId(), "Force withdrawal did not create a transaction");
        SavingsTestValidators.verifyAmount(new BigDecimal("-100"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Balance after forcing a withdrawal past the available balance");
    }

    @AfterEach
    public void resetForceWithdrawalConfiguration() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_WITHDRAWAL_ON_SAVINGS_ACCOUNT,
                new PutGlobalConfigurationsRequest().enabled(false));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_WITHDRAWAL_ON_SAVINGS_ACCOUNT_LIMIT,
                new PutGlobalConfigurationsRequest().value(DISABLED_LIMIT).enabled(false));
    }
}
