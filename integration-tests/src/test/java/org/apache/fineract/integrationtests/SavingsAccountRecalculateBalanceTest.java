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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class SavingsAccountRecalculateBalanceTest extends FeignSavingsTestBase {

    private static final String SUBMITTED_ON_DATE = "08 January 2013";
    private static final String APPROVED_ON_DATE = "09 January 2013";
    private static final String TRANSACTION_DATE = "01 March 2013";

    private static final BigDecimal TRANSACTION_AMOUNT = new BigDecimal("100");
    private static final BigDecimal HOLD_AMOUNT = new BigDecimal("50");
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("500.0");
    private static final String HOLD_REASON = "unUsualActivity";
    private static final Double NO_INTEREST = 0.0;

    @Test
    public void testSavingsAccountDepositAfterNegativeHoldAmount() {
        final Long savingsId = createActiveOverdraftSavingsAccount(null);

        BigDecimal balance = BigDecimal.ZERO;

        deposit(savingsId, TRANSACTION_AMOUNT.toPlainString(), TRANSACTION_DATE);
        balance = balance.add(TRANSACTION_AMOUNT);
        verifyAvailableBalance(savingsId, balance, "Verifying Balance after deposit");

        withdraw(savingsId, TRANSACTION_AMOUNT.toPlainString(), TRANSACTION_DATE);
        balance = balance.subtract(TRANSACTION_AMOUNT);
        verifyAvailableBalance(savingsId, balance, "Verifying Balance after withdrawal");

        final Long holdTransactionId = holdAmount(savingsId);
        balance = balance.subtract(HOLD_AMOUNT);
        verifyAvailableBalance(savingsId, balance, "Verifying Balance after hold amount");

        savingsTransactionHelper.releaseAmount(savingsId, holdTransactionId);
        balance = balance.add(HOLD_AMOUNT);
        verifyAvailableBalance(savingsId, balance, "Verifying Balance after release amount");

        deposit(savingsId, TRANSACTION_AMOUNT.toPlainString(), TRANSACTION_DATE);
        balance = balance.add(TRANSACTION_AMOUNT);
        verifyAvailableBalance(savingsId, balance, "Verifying Balance after hold-release-deposit");
    }

    @Test
    public void testSavingsAccountDepositAfterNegativeHoldAmountNoInterest() {
        final Long savingsId = createActiveOverdraftSavingsAccount(NO_INTEREST);

        BigDecimal balance = BigDecimal.ZERO;

        Long depositTransactionId = deposit(savingsId, TRANSACTION_AMOUNT.toPlainString(), TRANSACTION_DATE).getResourceId();
        assertNotNull(depositTransactionId);
        balance = balance.add(TRANSACTION_AMOUNT);
        verifyAvailableBalance(savingsId, balance, "Verifying Balance after deposit");
        verifyRunningBalance(savingsId, depositTransactionId, balance, "Verifying Running Balance of deposit");

        final Long withdrawalTransactionId = withdraw(savingsId, TRANSACTION_AMOUNT.toPlainString(), TRANSACTION_DATE).getResourceId();
        assertNotNull(withdrawalTransactionId);
        balance = balance.subtract(TRANSACTION_AMOUNT);
        verifyAvailableBalance(savingsId, balance, "Verifying Balance after withdrawal");
        verifyRunningBalance(savingsId, withdrawalTransactionId, balance, "Verifying Running Balance of withdraw");

        final Long holdTransactionId = holdAmount(savingsId);
        balance = balance.subtract(HOLD_AMOUNT);
        verifyAvailableBalance(savingsId, balance, "Verifying Balance after hold amount");

        final Long releaseTransactionId = savingsTransactionHelper.releaseAmount(savingsId, holdTransactionId).getResourceId();
        assertNotNull(releaseTransactionId);
        balance = balance.add(HOLD_AMOUNT);
        verifyAvailableBalance(savingsId, balance, "Verifying Balance after release amount");

        depositTransactionId = deposit(savingsId, TRANSACTION_AMOUNT.toPlainString(), TRANSACTION_DATE).getResourceId();
        assertNotNull(depositTransactionId);
        balance = balance.add(TRANSACTION_AMOUNT);
        verifyAvailableBalance(savingsId, balance, "Verifying Balance after hold-release-deposit");
        // this is a backdated transaction and so listed before the release transaction
        verifyRunningBalance(savingsId, depositTransactionId, balance.subtract(HOLD_AMOUNT),
                "Verifying Running Balance of deposit negative balance");

        final SavingsAccountTransactionData releaseTransaction = savingsTransactionHelper.getTransaction(savingsId, releaseTransactionId);
        assertFalse(releaseTransaction.getReversed(), "Verifying release transaction with overdraft is not reversed");
        SavingsTestValidators.verifyAmount(balance, releaseTransaction.getRunningBalance(), "Verifying Running Balance");
    }

    private Long holdAmount(final Long savingsId) {
        final Long holdTransactionId = savingsTransactionHelper
                .holdAmount(savingsId, HOLD_AMOUNT.toPlainString(), TRANSACTION_DATE, HOLD_REASON).getResourceId();
        assertNotNull(holdTransactionId);
        return holdTransactionId;
    }

    private void verifyAvailableBalance(final Long savingsId, final BigDecimal expected, final String message) {
        SavingsTestValidators.verifyAmount(expected, savingsHelper.getSavingsSummary(savingsId).getAvailableBalance(), message);
    }

    private void verifyRunningBalance(final Long savingsId, final Long transactionId, final BigDecimal expected, final String message) {
        SavingsTestValidators.verifyAmount(expected, savingsTransactionHelper.getTransaction(savingsId, transactionId).getRunningBalance(),
                message);
    }

    private Long createActiveOverdraftSavingsAccount(final Double nominalAnnualInterestRate) {
        final Long clientId = createClient();
        assertNotNull(clientId);

        final Long productId = createOverdraftSavingsProduct(nominalAnnualInterestRate);
        assertNotNull(productId);

        final Long savingsId = submitSavingsApplication(clientId, productId, SUBMITTED_ON_DATE).getSavingsId();
        assertNotNull(savingsId);

        approveSavings(savingsId, APPROVED_ON_DATE);
        activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private Long createOverdraftSavingsProduct(final Double nominalAnnualInterestRate) {
        final PostSavingsProductsRequest request = SavingsRequestBuilders
                .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY, SavingsTestData.InterestPostingPeriodType.MONTHLY,
                        SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                .minRequiredOpeningBalance(BigDecimal.ZERO)//
                .allowOverdraft(true);
        if (nominalAnnualInterestRate != null) {
            request.nominalAnnualInterestRate(nominalAnnualInterestRate);
        }
        return savingsProductHelper.createSavingsProduct(request.overdraftLimit(OVERDRAFT_LIMIT)).getResourceId();
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }
}
