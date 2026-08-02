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
