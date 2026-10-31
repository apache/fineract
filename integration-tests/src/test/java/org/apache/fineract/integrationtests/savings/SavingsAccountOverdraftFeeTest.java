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

import java.math.BigDecimal;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.junit.jupiter.api.Test;

class SavingsAccountOverdraftFeeTest extends FeignSavingsTestBase {

    private static final String ACTIVATION_DATE = "01 January 2013";
    private static final String TRANSACTION_DATE = "02 January 2013";

    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("100000");
    private static final BigDecimal OVERDRAFT_FEE = new BigDecimal("40");
    private static final BigDecimal WITHDRAWAL_AMOUNT = new BigDecimal("100");
    private static final BigDecimal EXPECTED_BALANCE = new BigDecimal("-140");

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

            // Withdraw enough to move the account into overdraft.
            withdraw(savingsId, WITHDRAWAL_AMOUNT.toPlainString(), TRANSACTION_DATE);

            // The overdraft fee should be paid and included in the balance.
            final BigDecimal actualBalance = savingsHelper.getSavingsSummary(savingsId).getAccountBalance());

            assertEquals(0, EXPECTED_BALANCE.compareTo(actualBalance), "Account balance should include the overdraft fee");
        });
    }

    private Long createOverdraftFee() {
        final ChargeRequest request = new ChargeRequest().name("Overdraft Fee").chargeAppliesTo(2) // Savings
                .chargeTimeType(10) // Overdraft fee
                .chargeCalculationType(1) // Flat
                .amount(OVERDRAFT_FEE.doubleValue()).currencyCode("USD").locale("en").active(true).penalty(false);

        final var response = chargesHelper.createCharge(request);
        assertNotNull(response.getResourceId());

        return response.getResourceId();
    }

    private Long createOverdraftSavingsProduct() {
        final PostSavingsProductsRequest request = SavingsRequestBuilders.defaultSavingsProduct().minRequiredOpeningBalance(BigDecimal.ZERO)
                .allowOverdraft(true).overdraftLimit(OVERDRAFT_LIMIT);

        return savingsProductHelper.createSavingsProduct(request).getResourceId();
    }
}
