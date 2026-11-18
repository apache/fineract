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
package org.apache.fineract.integrationtests.savings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Test;


class SavingsAccountOverdraftFeeTest extends FeignSavingsTestBase {

    private static final String ACTIVATION_DATE = "01 January 2013";
    private static final String TRANSACTION_DATE = "02 January 2013";

    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("100000");
    private static final BigDecimal OVERDRAFT_FEE = new BigDecimal("40");
    private static final BigDecimal WITHDRAWAL_FEE = new BigDecimal("10");

    @Test
    void overdraftFeeShouldBeIncludedInAccountBalanceAfterWithdrawal() {
        runAt(TRANSACTION_DATE, () -> {
            final Long clientId = createClient(ACTIVATION_DATE);
            final Long chargeId = createOverdraftFee();
            final Long productId = createOverdraftSavingsProduct();
            final Long savingsId = createApproveActivateSavings(clientId, productId, ACTIVATION_DATE);

            final var chargeResponse = addSavingsAccountCharge(savingsId, new PostSavingsAccountsSavingsAccountIdChargesRequest()
                    .chargeId(chargeId).amount(OVERDRAFT_FEE.floatValue()).locale("en"));

            assertNotNull(chargeResponse.getResourceId());
            withdraw(savingsId, new BigDecimal("100").toPlainString(), TRANSACTION_DATE);
            final BigDecimal actualBalance = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

            assertEquals(0, new BigDecimal("-140").compareTo(actualBalance), "Account balance should include the overdraft fee");
        });
    }

    @Test
    void overdraftFeeIsChargedAgainOnEachNewOverdraft() {
        runAt(TRANSACTION_DATE, () -> {
            final Long clientId = createClient(ACTIVATION_DATE);
            final Long chargeId = createOverdraftFee();
            final Long productId = createOverdraftSavingsProduct();
            final Long savingsId = createApproveActivateSavings(clientId, productId, ACTIVATION_DATE);

            addSavingsAccountCharge(savingsId, new PostSavingsAccountsSavingsAccountIdChargesRequest().chargeId(chargeId)
                    .amount(OVERDRAFT_FEE.floatValue()).locale("en"));

            withdraw(savingsId, "150", TRANSACTION_DATE);
            assertEquals(0, new BigDecimal("-190").compareTo(savingsHelper.getSavingsSummary(savingsId).getAccountBalance()));

            deposit(savingsId, "300", TRANSACTION_DATE);
            assertEquals(0, new BigDecimal("110").compareTo(savingsHelper.getSavingsSummary(savingsId).getAccountBalance()));

            withdraw(savingsId, "200", TRANSACTION_DATE);
            assertEquals(0, new BigDecimal("-130").compareTo(savingsHelper.getSavingsSummary(savingsId).getAccountBalance()));
        });
    }

    @Test
    void overdraftFeeAlongWithWithdrawalFee() {
        runAt(TRANSACTION_DATE, () -> {
            final Long clientId = createClient(ACTIVATION_DATE);
            final Long chargeId = createOverdraftFee();
            final Long productId = createOverdraftSavingsProduct();
            final Long savingsId = createApproveActivateSavings(clientId, productId, ACTIVATION_DATE);

            final var chargeResponse1 = addSavingsAccountCharge(savingsId, new PostSavingsAccountsSavingsAccountIdChargesRequest()
                    .chargeId(chargeId).amount(OVERDRAFT_FEE.floatValue()).locale("en"));
            final var chargeResponse2 = addSavingsAccountCharge(savingsId, new PostSavingsAccountsSavingsAccountIdChargesRequest()
                    .chargeId(createWithdrawalFee()).amount(WITHDRAWAL_FEE.floatValue()).locale("en"));

            withdraw(savingsId, new BigDecimal("100").toPlainString(), TRANSACTION_DATE);
            final BigDecimal actualBalance = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

            assertNotNull(chargeResponse1.getResourceId());
            assertNotNull(chargeResponse2.getResourceId());
            assertEquals(0, new BigDecimal("-150").compareTo(actualBalance),
                    "Account balance should include the overdraft and withdrawal fee");

        });
    }

    @Test
    void noOverdraftForBalanceExactlyZeroAfterWithdrawal() {
        runAt(TRANSACTION_DATE, () -> {
            final Long clientId = createClient(ACTIVATION_DATE);
            final Long chargeId = createOverdraftFee();
            final Long productId = createOverdraftSavingsProduct();
            final Long savingsId = createApproveActivateSavings(clientId, productId, ACTIVATION_DATE);

            addSavingsAccountCharge(savingsId, new PostSavingsAccountsSavingsAccountIdChargesRequest().chargeId(chargeId)
                    .amount(OVERDRAFT_FEE.floatValue()).locale("en"));
            deposit(savingsId, "100", TRANSACTION_DATE);
            withdraw(savingsId, new BigDecimal("100").toPlainString(), TRANSACTION_DATE);
            final BigDecimal actualBalance = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

            assertEquals(0, new BigDecimal("0").compareTo(actualBalance), "Account balance should not include the overdraft fee");

        });
    }

    @Test
    void undoingTheWithdrawalAlsoReversesOverdraftFee() {
        runAt(TRANSACTION_DATE, () -> {
            final Long clientId = createClient(ACTIVATION_DATE);
            final Long chargeId = createOverdraftFee();
            final Long productId = createOverdraftSavingsProduct();
            final Long savingsId = createApproveActivateSavings(clientId, productId, ACTIVATION_DATE);

            addSavingsAccountCharge(savingsId, new PostSavingsAccountsSavingsAccountIdChargesRequest().chargeId(chargeId)
                    .amount(OVERDRAFT_FEE.floatValue()).locale("en"));

            Long transactionId = withdraw(savingsId, new BigDecimal("100").toPlainString(), TRANSACTION_DATE).getResourceId();
            savingsTransactionHelper.undoTransaction(savingsId, transactionId);
            final BigDecimal actualBalance = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

            assertEquals(0, new BigDecimal("0").compareTo(actualBalance),
                    "Account balance should not include the overdraft and withdrawal fee");

        });
    }

    @Test
    void overdraftFeeCountsAgainstOverdraftLimit() {
        runAt(TRANSACTION_DATE, () -> {
            final Long clientId = createClient(ACTIVATION_DATE);
            final Long chargeId = createOverdraftFee();
            final Long productId = createOverdraftSavingsProduct(new BigDecimal("100"));
            final Long savingsId = createApproveActivateSavings(clientId, productId, ACTIVATION_DATE);

            addSavingsAccountCharge(savingsId, new PostSavingsAccountsSavingsAccountIdChargesRequest().chargeId(chargeId)
                    .amount(OVERDRAFT_FEE.floatValue()).locale("en"));

            assertThrows(CallFailedRuntimeException.class, () -> withdraw(savingsId, "100", TRANSACTION_DATE));
            assertEquals(0, BigDecimal.ZERO.compareTo(savingsHelper.getSavingsSummary(savingsId).getAccountBalance()));
        });
    }

    @Test
    void overdraftFeeIsChargedWithPivotDateConfig() {
        final String config = GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING;
        final boolean originallyEnabled = Boolean.TRUE.equals(globalConfigurationHelper.getGlobalConfigurationByName(config).getEnabled());
        try {
            globalConfigurationHelper.updateGlobalConfiguration(config, new PutGlobalConfigurationsRequest().enabled(false));
            runAt(TRANSACTION_DATE, () -> {
                final Long clientId = createClient(ACTIVATION_DATE);
                final Long chargeId = createOverdraftFee();
                final Long productId = createOverdraftSavingsProduct();
                final Long savingsId = createApproveActivateSavings(clientId, productId, ACTIVATION_DATE);
                addSavingsAccountCharge(savingsId, new PostSavingsAccountsSavingsAccountIdChargesRequest().chargeId(chargeId)
                        .amount(OVERDRAFT_FEE.floatValue()).locale("en"));

                withdraw(savingsId, "100", TRANSACTION_DATE);
                assertEquals(0, new BigDecimal("-140").compareTo(savingsHelper.getSavingsSummary(savingsId).getAccountBalance()));
            });
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(config, new PutGlobalConfigurationsRequest().enabled(originallyEnabled));
        }
    }

    private Long createCharge(final String name, final int chargeTimeType, final BigDecimal amount) {
        final ChargeRequest request = new ChargeRequest().name(name + " " + System.nanoTime()).chargeAppliesTo(2)
                .chargeTimeType(chargeTimeType).chargeCalculationType(1).amount(amount.doubleValue()).currencyCode("USD")
                .locale("en").active(true).penalty(false);
        final var response = chargesHelper.createCharge(request);
        assertNotNull(response.getResourceId());
        return response.getResourceId();
    }

    private Long createOverdraftFee() {
        return createCharge("Overdraft Fee", ChargeTimeType.OVERDRAFT_FEE.getValue(), OVERDRAFT_FEE);
    }

    private Long createWithdrawalFee() {
        return createCharge("Withdrawal Fee", ChargeTimeType.WITHDRAWAL_FEE.getValue(), WITHDRAWAL_FEE);
    }

    private Long createOverdraftSavingsProduct(final BigDecimal limit) {
        final PostSavingsProductsRequest request = SavingsRequestBuilders.defaultSavingsProduct().minRequiredOpeningBalance(BigDecimal.ZERO)
                .allowOverdraft(true).overdraftLimit(limit);
        return savingsProductHelper.createSavingsProduct(request).getResourceId();
    }

    private Long createOverdraftSavingsProduct() {
        return createOverdraftSavingsProduct(OVERDRAFT_LIMIT);
    }
}
