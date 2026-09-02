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
import java.time.Month;
import java.util.List;
import org.apache.fineract.client.models.PostSavingsProductsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class FlexibleSavingsInterestPostingIntegrationTest extends FeignSavingsTestBase {

    private static final String START_DATE = "01 December 2013";
    private static final long APRIL = 4L;

    // 1st Dec 13 to 31st March 14 - 365 days, daily compounding using daily balance
    // 33.7016 obtained from formula in excel provided by Subramanya
    private static final BigDecimal EXPECTED_INTEREST_POSTED = new BigDecimal("33.7016");
    private static final LocalDate EXPECTED_INTEREST_POSTING_DATE = LocalDate.of(2014, Month.MARCH, 31);

    @Test
    public void testSavingsInterestPostingAtPeriodEnd() {
        Long clientId = createClient(START_DATE);
        assertNotNull(clientId);

        configureInterestPosting(true, APRIL);

        Long savingsId = createSavingsAccount(clientId, START_DATE);

        deposit(savingsId, "1000", START_DATE);

        savingsHelper.postInterest(savingsId);

        List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
        SavingsAccountTransactionData interestPostingTransaction = transactions.get(transactions.size() - 2);
        SavingsTestValidators.verifyIsInterestPosting(interestPostingTransaction);

        SavingsTestValidators.verifyAmount(EXPECTED_INTEREST_POSTED, interestPostingTransaction.getAmount(),
                "Equality check for interest posted amount");
        assertEquals(EXPECTED_INTEREST_POSTING_DATE, interestPostingTransaction.getDate(), "Date check for Interest Posting transaction");
    }

    private Long createSavingsAccount(final Long clientId, final String startDate) {
        PostSavingsProductsResponse savingsProduct = createSavingsProductAnnualPosting();
        assertNotNull(savingsProduct.getResourceId());

        Long savingsId = submitSavingsApplication(clientId, savingsProduct.getResourceId(), startDate).getSavingsId();
        assertNotNull(savingsId);

        approveSavings(savingsId, startDate);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        activateSavings(savingsId, startDate);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private void configureInterestPosting(final Boolean periodEndEnable, final Long financialYearBeginningMonth) {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.SAVINGS_INTEREST_POSTING_CURRENT_PERIOD_END,
                new PutGlobalConfigurationsRequest().enabled(periodEndEnable));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FINANCIAL_YEAR_BEGINNING_MONTH,
                new PutGlobalConfigurationsRequest().value(financialYearBeginningMonth));
    }

    private PostSavingsProductsResponse createSavingsProductAnnualPosting() {
        return createSavingsProduct(SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                SavingsTestData.InterestPostingPeriodType.ANNUAL, SavingsTestData.InterestCalculationType.DAILY_BALANCE));
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }
}
