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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.apache.fineract.client.models.AccountTransferRequest;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutSavingsAccountsAccountIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

public class AccountTransferWithdrawalFeeTest extends FeignSavingsTestBase {

    private static final String ACCOUNT_OPENING_DATE = "01 January 2013";
    private static final String TRANSFER_DATE = "01 March 2013";
    private static final String OPENING_DEPOSIT_AMOUNT = "30000";
    private static final String ACCOUNT_TRANSFER_AMOUNT = "15000.0";

    private static final String SAVINGS_ACCOUNT_TYPE = "2";
    private static final String HEAD_OFFICE_ID = "1";

    @Test
    public void testFromSavingsToSavingsAccountTransferWithWithdrawalFee() {
        final PostChargesResponse withdrawalCharge = savingsChargeHelper.createWithdrawalFeeCharge();
        assertNotNull(withdrawalCharge.getResourceId());

        final Long productId = createSavingsProduct(withdrawalFeeProduct()).getResourceId();

        final Long fromClientId = createClient();
        final Long fromSavingsId = createAccountChargingTransferFees(fromClientId, productId, withdrawalCharge.getResourceId());

        final Long toClientId = createClient();
        final Long toSavingsId = createApproveActivateSavings(toClientId, productId, ACCOUNT_OPENING_DATE);

        transfer(fromClientId, fromSavingsId, toClientId, toSavingsId);

        // 30000 - 15000 transferred - 100 withdrawal fee
        verifyBalance("14900", fromSavingsId, "Verifying From Savings Account Balance after Account Transfer with Withdrawal Fee");
        verifyBalance("15000", toSavingsId, "Verifying To Savings Account Balance after Account Transfer");
    }

    @Test
    public void testFromSavingsToSavingsAccountTransferWithPaymentTypeWithdrawalFee() {
        final Long paymentTypeId = paymentTypeHelper.createPaymentType(bankTransferPaymentType()).getResourceId();
        assertNotNull(paymentTypeId);

        final PostChargesResponse withdrawalCharge = savingsChargeHelper
                .createCharge(SavingsRequestBuilders.savingsWithdrawalFeeCharge().enablePaymentType(true).paymentTypeId(paymentTypeId));
        assertNotNull(withdrawalCharge.getResourceId());

        final Long productId = createSavingsProduct(withdrawalFeeProduct()).getResourceId();

        final Long fromClientId = createClient();
        final Long fromSavingsId = createAccountChargingTransferFees(fromClientId, productId, withdrawalCharge.getResourceId());

        final Long toClientId = createClient();
        final Long toSavingsId = createApproveActivateSavings(toClientId, productId, ACCOUNT_OPENING_DATE);

        transfer(fromClientId, fromSavingsId, toClientId, toSavingsId);

        // A transfer carries no payment detail to match the charge's payment type against, so the fee is skipped.
        verifyBalance("15000", fromSavingsId,
                "Verifying From Savings Account Balance after Account Transfer with Payment Type Withdrawal Fee");
        verifyBalance("15000", toSavingsId, "Verifying To Savings Account Balance after Account Transfer");
    }

    /** The transfer-fee flag can only be set while the application is still pending approval. */
    private Long createAccountChargingTransferFees(final Long clientId, final Long productId, final Long chargeId) {
        final Long savingsId = submitSavingsApplication(clientId, productId, ACCOUNT_OPENING_DATE).getSavingsId();
        assertNotNull(savingsId);
        savingsHelper.updateSavingsAccount(savingsId, new PutSavingsAccountsAccountIdRequest().withdrawalFeeForTransfers(true));

        approveSavings(savingsId, ACCOUNT_OPENING_DATE);
        activateSavings(savingsId, ACCOUNT_OPENING_DATE);
        deposit(savingsId, OPENING_DEPOSIT_AMOUNT, ACCOUNT_OPENING_DATE);

        assertNotNull(savingsChargeHelper.addChargeToSavings(savingsId, chargeId, SavingsTestData.DEFAULT_CHARGE_AMOUNT.floatValue())
                .getResourceId());
        return savingsId;
    }

    private void transfer(final Long fromClientId, final Long fromSavingsId, final Long toClientId, final Long toSavingsId) {
        accountTransferHelper.createAccountTransfer(new AccountTransferRequest()//
                .fromClientId(String.valueOf(fromClientId))//
                .fromAccountId(String.valueOf(fromSavingsId))//
                .fromAccountType(SAVINGS_ACCOUNT_TYPE)//
                .fromOfficeId(HEAD_OFFICE_ID)//
                .toClientId(String.valueOf(toClientId))//
                .toAccountId(String.valueOf(toSavingsId))//
                .toAccountType(SAVINGS_ACCOUNT_TYPE)//
                .toOfficeId(HEAD_OFFICE_ID)//
                .transferDate(TRANSFER_DATE)//
                .transferAmount(ACCOUNT_TRANSFER_AMOUNT)//
                .transferDescription("Transfer")//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE));
    }

    private void verifyBalance(final String expected, final Long savingsId, final String message) {
        SavingsTestValidators.verifyAmount(new BigDecimal(expected), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                message);
    }

    private static PostSavingsProductsRequest withdrawalFeeProduct() {
        return SavingsRequestBuilders
                .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY, SavingsTestData.InterestPostingPeriodType.MONTHLY,
                        SavingsTestData.InterestCalculationType.DAILY_BALANCE)//
                .digitsAfterDecimal(2)//
                .description("Savings product for withdrawal fee test");
    }

    private static PaymentTypeCreateRequest bankTransferPaymentType() {
        return new PaymentTypeCreateRequest()//
                .name(Utils.uniqueRandomStringGenerator("Bank_Transfer_", 6))//
                .description("Payment via bank transfer")//
                .isCashPayment(false)//
                .position(1L);
    }
}
