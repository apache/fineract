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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.SavingsAccountSummaryData;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.junit.jupiter.api.Test;

public class SavingsAccountBalanceCheckAfterReversalTest extends FeignSavingsTestBase {

    private static final String START_DATE = "10 April 2022";
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("10000");
    private static final String INSUFFICIENT_BALANCE_ERROR = "error.msg.savingsaccount.transaction.insufficient.account.balance";
    private static final String POST_INTEREST_JOB_NAME = "Post Interest For Savings";

    @Test
    public void testSavingsBalanceAfterWithdrawal() {
        Long clientId = createClient(START_DATE);
        assertNotNull(clientId);

        Long savingsId = createSavingsAccountDailyPosting(clientId);

        Long depositTransactionId = deposit(savingsId, "10000", START_DATE).getResourceId();
        savingsTransactionHelper.reverseTransaction(savingsId, depositTransactionId);

        SavingsAccountTransactionData reversedDeposit = savingsTransactionHelper.getTransaction(savingsId, depositTransactionId);
        assertTrue(reversedDeposit.getReversed(), "Deposit transaction was not reversed");

        SavingsTestValidators.verifyAmount(BigDecimal.ZERO, savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying opening Balance is 0");

        CallFailedRuntimeException error = savingsTransactionHelper.withdrawExpectingError(savingsId, "100", START_DATE);
        SavingsTestValidators.verifyFirstErrorCode(INSUFFICIENT_BALANCE_ERROR, error);
    }

    @Test
    public void testSavingsBalanceWithOverDraftAfterWithdrawal() {
        Long clientId = createClient(START_DATE);
        assertNotNull(clientId);

        Long savingsId = createSavingsAccountDailyPostingWithOverDraft(clientId);

        Long withdrawalTransactionId = withdraw(savingsId, "1000", START_DATE).getResourceId();
        savingsTransactionHelper.reverseTransaction(savingsId, withdrawalTransactionId);

        SavingsAccountTransactionData reversedWithdrawal = savingsTransactionHelper.getTransaction(savingsId, withdrawalTransactionId);
        assertTrue(reversedWithdrawal.getReversed(), "Withdrawal transaction was not reversed");

        SavingsTestValidators.verifyAmount(BigDecimal.ZERO, savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying Balance is 0");

        withdraw(savingsId, "500", START_DATE);
        SavingsTestValidators.verifyAmount(new BigDecimal("-500"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying Balance is -500");
    }

    @Test
    public void testPostInterestJobKeepsReversedDepositReversed() {
        final Long clientId = createClient("01 March 2019");
        final Long productId = createSavingsProduct(
                SavingsRequestBuilders
                        .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.MONTHLY,
                                SavingsTestData.InterestPostingPeriodType.MONTHLY, SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                        .nominalAnnualInterestRate(11.0))
                .getResourceId();
        final Long savingsId = createApproveActivateSavings(clientId, productId, "01 March 2019");
        deposit(savingsId, "20013", "01 March 2019");
        final Long reversedDepositId = deposit(savingsId, "1000", "10 March 2019").getResourceId();
        runAt("12 March 2019", () -> savingsTransactionHelper.reverseTransaction(savingsId, reversedDepositId));

        runAt("01 April 2019", () -> schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME));

        final SavingsAccountSummaryData summary = savingsHelper.getSavingsSummary(savingsId);
        // 20013 x 11% x 31 / 365: the reversed deposit earns no interest
        SavingsTestValidators.verifyAmount(new BigDecimal("186.9708"), summary.getTotalInterestPosted(), "Interest posted by the job");
        SavingsTestValidators.verifyAmount(new BigDecimal("20013"), summary.getTotalDeposits(), "The reversed deposit must not count");
        SavingsTestValidators.verifyAmount(new BigDecimal("20199.9708"), summary.getAccountBalance(), "Balance after the job");
        final SavingsAccountTransactionData reversal = savingsTransactionHelper.getTransactions(savingsId).stream()
                .filter(transaction -> reversedDepositId.equals(transaction.getOriginalTransactionId())).findFirst().orElseThrow();
        assertTrue(Boolean.TRUE.equals(reversal.getIsReversal()), "The job must keep the reversal transaction of the deposit");
    }

    private Long createSavingsAccountDailyPosting(final Long clientId) {
        Long productId = createSavingsProduct(dailyPostingProduct()).getResourceId();
        assertNotNull(productId);
        return approveAndActivate(clientId, productId);
    }

    private Long createSavingsAccountDailyPostingWithOverDraft(final Long clientId) {
        Long productId = savingsProductHelper
                .createSavingsProduct(dailyPostingProduct().allowOverdraft(true).overdraftLimit(OVERDRAFT_LIMIT)).getResourceId();
        assertNotNull(productId);
        return approveAndActivate(clientId, productId);
    }

    private PostSavingsProductsRequest dailyPostingProduct() {
        return SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                SavingsTestData.InterestPostingPeriodType.DAILY, SavingsTestData.InterestCalculationType.DAILY_BALANCE);
    }

    private Long approveAndActivate(final Long clientId, final Long productId) {
        Long savingsId = submitSavingsApplication(clientId, productId, START_DATE).getSavingsId();
        assertNotNull(savingsId);

        approveSavings(savingsId, START_DATE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        activateSavings(savingsId, START_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }
}
