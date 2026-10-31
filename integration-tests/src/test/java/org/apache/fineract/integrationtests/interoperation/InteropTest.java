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
package org.apache.fineract.integrationtests.interoperation;

import static org.apache.fineract.integrationtests.client.feign.modules.FeignErrors.errorGlobalisationCode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.InteropQuoteResponseData;
import org.apache.fineract.client.models.InteropTransactionRequestResponseData;
import org.apache.fineract.client.models.InteropTransferResponseData;
import org.apache.fineract.client.models.MoneyData;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignInteropHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsChargeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.interoperation.domain.InteropActionState;
import org.apache.fineract.interoperation.domain.InteropIdentifierType;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteropTest {

    private static final Logger LOG = LoggerFactory.getLogger(InteropTest.class);

    private static final BigDecimal MIN_OPENING_BALANCE = new BigDecimal("100000.0");
    private static final MathContext MATHCONTEXT = new MathContext(12, RoundingMode.HALF_EVEN);

    private final FeignClientHelper clientHelper = new FeignClientHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignAccountHelper accountHelper = new FeignAccountHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsProductHelper savingsProductHelper = new FeignSavingsProductHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsHelper savingsHelper = new FeignSavingsHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsChargeHelper savingsChargeHelper = new FeignSavingsChargeHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private FeignInteropHelper interopHelper;

    private Long clientId;
    private Long savingsProductId;
    private Long savingsId;
    private Long chargeId;

    @BeforeEach
    public void setup() {
        interopHelper = new FeignInteropHelper(FineractFeignClientHelper.getFineractFeignClient(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
    }

    @Test
    public void testValidateAction() {
        CallFailedRuntimeException exception = interopHelper.performTransferWithoutActionExpectingError(UUID.randomUUID().toString(),
                InteropTransactionRole.PAYER);
        Assertions.assertEquals(400, exception.getStatus());
        Assertions.assertEquals("validation.msg.InteropApi.action.cannot.be.blank", errorGlobalisationCode(exception));

        exception = interopHelper.performTransferExpectingError(UUID.randomUUID().toString(), "null", InteropTransactionRole.PAYER);
        Assertions.assertEquals(400, exception.getStatus());
        Assertions.assertEquals("validation.msg.InteropApi.action.is.not.one.of.expected.enumerations", errorGlobalisationCode(exception));
    }

    @Test
    public void testInteroperation() {
        createClient();
        createSavingsProduct();
        createCharge();
        openSavingsAccount();

        testParties();
        testRequests();
        testQuotes();
        testTransfers();
    }

    private void createClient() {
        clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
    }

    private void createSavingsProduct() {
        LOG.debug("------------------------------ Create Interoperable Saving Product ---------------------------------------");

        PostSavingsProductsRequest request = SavingsRequestBuilders.withCashBasedAccounting(
                SavingsRequestBuilders
                        .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                                SavingsTestData.InterestPostingPeriodType.MONTHLY, SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                        .currencyCode(interopHelper.getCurrency()).nominalAnnualInterestRate(0.0)
                        .minRequiredOpeningBalance(MIN_OPENING_BALANCE),
                accountHelper.createAssetAccount(), accountHelper.createLiabilityAccount(), accountHelper.createIncomeAccount(),
                accountHelper.createExpenseAccount());
        savingsProductId = savingsProductHelper.createSavingsProduct(request).getResourceId();
        Assertions.assertNotNull(savingsProductId);

        LOG.debug("Sucessfully created Interoperable Saving Product (id: {})", savingsProductId);
    }

    private void createCharge() {
        chargeId = savingsChargeHelper.createCharge(SavingsRequestBuilders.savingsWithdrawalFeeCharge()
                .amount(interopHelper.getFee().doubleValue()).currencyCode(interopHelper.getCurrency())).getResourceId();
        Assertions.assertNotNull(chargeId);
    }

    private void openSavingsAccount() {
        LOG.debug("------------------------------ Create Interoperable Saving Account ---------------------------------------");
        PostSavingsAccountsRequest request = SavingsRequestBuilders.submitSavingsApplication(clientId, savingsProductId, "08 January 2013")
                .externalId(interopHelper.getAccountExternalId()).withdrawalFeeForTransfers(true);
        savingsId = savingsHelper.submitApplication(request).getSavingsId();
        Assertions.assertNotNull(savingsId);

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));

        savingsHelper.approveSavings(savingsId, "09 January 2013");
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        savingsHelper.activateSavings(savingsId, "01 March 2013");
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));

        if (chargeId != null) {
            savingsChargeHelper.addChargeToSavings(savingsId, chargeId, interopHelper.getFee().floatValue());
        }

        LOG.debug("Sucessfully created Interoperable Saving Account (id: {})", savingsId);
    }

    private void testParties() {
        String idValue = UUID.randomUUID().toString();
        String accountId = interopHelper.registerParty(InteropIdentifierType.MSISDN, idValue).getAccountId();
        Assertions.assertEquals(interopHelper.getAccountExternalId(), accountId);

        Assertions.assertEquals(403, interopHelper.registerPartyExpectingError(InteropIdentifierType.MSISDN, idValue).getStatus());

        accountId = interopHelper.getParty(InteropIdentifierType.MSISDN, idValue).getAccountId();
        Assertions.assertEquals(interopHelper.getAccountExternalId(), accountId);

        accountId = interopHelper.deleteParty(InteropIdentifierType.MSISDN, idValue).getAccountId();
        Assertions.assertEquals(interopHelper.getAccountExternalId(), accountId);

        Assertions.assertEquals(404, interopHelper.getPartyExpectingError(InteropIdentifierType.MSISDN, idValue).getStatus());
    }

    private void testRequests() {
        String requestCode = UUID.randomUUID().toString();
        InteropTransactionRequestResponseData response = interopHelper.createTransactionRequest(requestCode, InteropTransactionRole.PAYER);
        Assertions.assertEquals(requestCode, response.getRequestCode());
        Assertions.assertEquals(InteropActionState.ACCEPTED.toString(), response.getState().getValue());

        Assertions.assertEquals(400,
                interopHelper.createTransactionRequestExpectingError(requestCode, InteropTransactionRole.PAYEE).getStatus());
    }

    private void testQuotes() {
        // payer
        String quoteCode = UUID.randomUUID().toString();
        InteropQuoteResponseData response = interopHelper.createQuote(quoteCode, InteropTransactionRole.PAYER);
        Assertions.assertEquals(quoteCode, response.getQuoteCode());
        Assertions.assertEquals(InteropActionState.ACCEPTED.toString(), response.getState().getValue());

        MoneyData fee = response.getFspFee();
        Assertions.assertNotNull(fee);
        BigDecimal feeAmount = fee.getAmount();
        Assertions.assertTrue(MathUtil.isEqualTo(interopHelper.getFee(), feeAmount),
                "Quote fee expected: " + interopHelper.getFee() + ", actual: " + feeAmount);
        Assertions.assertEquals(interopHelper.getCurrency(), fee.getCurrency());

        // payee
        response = interopHelper.createQuote(quoteCode, InteropTransactionRole.PAYEE);
        Assertions.assertEquals(quoteCode, response.getQuoteCode());
        Assertions.assertEquals(InteropActionState.ACCEPTED.toString(), response.getState().getValue());

        fee = response.getFspFee();
        if (fee != null) {
            feeAmount = fee.getAmount();
            Assertions.assertTrue(MathUtil.isZero(feeAmount), "PAYEE Quote fee expected: " + BigDecimal.ZERO + ", actual: " + feeAmount);
        }
    }

    private void testTransfers() {
        SavingsAccountData savings = savingsHelper.getSavingsDetails(savingsId);
        BigDecimal onHold = savings.getSavingsAmountOnHold();
        BigDecimal balance = savings.getSummary().getAvailableBalance();

        String transferCode = UUID.randomUUID().toString();
        InteropTransferResponseData response = interopHelper.prepareTransfer(transferCode);
        Assertions.assertEquals(transferCode, response.getTransferCode());
        Assertions.assertEquals(InteropActionState.ACCEPTED.toString(), response.getState().getValue());

        // prepare
        savings = savingsHelper.getSavingsDetails(savingsId);
        BigDecimal onHold2 = savings.getSavingsAmountOnHold();
        BigDecimal balance2 = savings.getSummary().getAvailableBalance();

        BigDecimal transferAmount = interopHelper.getTransferAmount();
        BigDecimal expectedHold = MathUtil.add(onHold, transferAmount, MATHCONTEXT);
        Assertions.assertTrue(MathUtil.isEqualTo(expectedHold, onHold2),
                "On hold amount expected: " + expectedHold + ", actual: " + onHold2);
        BigDecimal expectedBalance = MathUtil.subtract(balance, transferAmount, MATHCONTEXT);
        Assertions.assertTrue(MathUtil.isEqualTo(expectedBalance, balance2),
                "Balance amount expected: " + expectedBalance + ", actual: " + balance2);

        // payer
        response = interopHelper.createTransfer(transferCode, InteropTransactionRole.PAYER);
        Assertions.assertEquals(transferCode, response.getTransferCode());
        Assertions.assertEquals(InteropActionState.ACCEPTED.toString(), response.getState().getValue());

        savings = savingsHelper.getSavingsDetails(savingsId);
        BigDecimal onHold3 = savings.getSavingsAmountOnHold();
        BigDecimal balance3 = savings.getSummary().getAvailableBalance();
        Assertions.assertTrue(MathUtil.isEqualTo(onHold, onHold3), "On hold amount expected: " + onHold + ", actual: " + onHold3);
        Assertions.assertTrue(MathUtil.isEqualTo(expectedBalance, balance3),
                "Balance amount expected: " + expectedBalance + ", actual: " + balance3);

        // payee
        response = interopHelper.createTransfer(transferCode, InteropTransactionRole.PAYEE);
        Assertions.assertEquals(transferCode, response.getTransferCode());
        Assertions.assertEquals(InteropActionState.ACCEPTED.toString(), response.getState().getValue());

        savings = savingsHelper.getSavingsDetails(savingsId);
        BigDecimal onHold4 = savings.getSavingsAmountOnHold();
        BigDecimal balance4 = savings.getSummary().getAvailableBalance();
        expectedBalance = MathUtil.subtract(balance, interopHelper.getFee(), MATHCONTEXT);
        Assertions.assertTrue(MathUtil.isEqualTo(onHold, onHold4), "On hold amount expected: " + onHold + ", actual: " + onHold4);
        Assertions.assertTrue(MathUtil.isEqualTo(expectedBalance, balance4),
                "Balance amount expected: " + expectedBalance + ", actual: " + balance4);
    }
}
