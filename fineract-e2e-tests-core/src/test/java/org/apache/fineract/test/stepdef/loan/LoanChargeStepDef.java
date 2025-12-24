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
package org.apache.fineract.test.stepdef.loan;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanChargeDataV1;
import org.apache.fineract.client.feign.FeignException;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutChargeTransactionChangesRequest;
import org.apache.fineract.client.models.PutChargeTransactionChangesResponse;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.data.ErrorMessageType;
import org.apache.fineract.test.factory.LoanChargeRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.loan.charge.LoanAddChargeEvent;
import org.apache.fineract.test.messaging.store.EventStore;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class LoanChargeStepDef extends AbstractStepDef {

    public static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy";
    public static final String DATE_FORMAT_EVENTS = "yyyy-MM-dd";
    public static final Double DEFAULT_CHARGE_FEE_FLAT = 10D;

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private EventAssertion eventAssertion;
    @Autowired
    private EventCheckHelper eventCheckHelper;
    @Autowired
    private EventStore eventStore;

    @When("Admin adds {string} due date charge with {string} due date and {double} EUR transaction amount")
    public void addChargeDueDate(String chargeType, String transactionDate, double transactionAmount) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeTypeId = chargeProductType.getValue();
        if (chargeTypeId.equals(ChargeProductType.LOAN_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                || chargeTypeId.equals(ChargeProductType.LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                || chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST.getValue())) {
            throw new IllegalStateException(String.format("The requested %s charge is NOT due date type, cannot be used here", chargeType));
        }

        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest().chargeId(chargeTypeId)
                .dueDate(transactionDate).amount(transactionAmount);

        PostLoansLoanIdChargesResponse loanChargeResponse = ok(
                () -> fineractClient.loanCharges().executeLoanCharge(loanId, loanIdChargesRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.ADD_DUE_DATE_CHARGE_RESPONSE, loanChargeResponse);
        testContext().set(TestContextKey.ADD_NSF_FEE_RESPONSE, loanChargeResponse);

        addChargeEventCheck(loanChargeResponse);
    }

    @When("Admin adds {string} charge with {double} % of transaction amount")
    public void addChargePercentage(String chargeType, double transactionPercentageAmount) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeTypeId = chargeProductType.getValue();
        if (!chargeTypeId.equals(ChargeProductType.LOAN_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                && !chargeTypeId.equals(ChargeProductType.LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                && !chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT.getValue())
                && !chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST.getValue())) {
            throw new IllegalStateException(String.format("The requested %s charge is due date type, cannot be used here", chargeType));
        }

        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest().chargeId(chargeTypeId)
                .amount(transactionPercentageAmount);

        PostLoansLoanIdChargesResponse loanChargeResponse = ok(
                () -> fineractClient.loanCharges().executeLoanCharge(loanId, loanIdChargesRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.ADD_DUE_DATE_CHARGE_RESPONSE, loanChargeResponse);
    }

    @When("Admin adds {string} installment charge with {double} amount")
    public void addInstallmentFeeCharge(final String chargeType, final double amount) throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse != null;
        final long loanId = loanResponse.getLoanId();

        final ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        final Long chargeTypeId = chargeProductType.getValue();
        if (!chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_FEE_FLAT.getValue())
                && !chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT.getValue())
                && !chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST.getValue())
                && !chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST.getValue())) {
            throw new IllegalStateException(
                    String.format("The requested %s charge is not installment fee type, cannot be used here", chargeType));
        }

        final PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest()
                .chargeId(chargeTypeId).amount(amount);

        final PostLoansLoanIdChargesResponse loanChargeResponse = ok(
                () -> fineractClient.loanCharges().executeLoanCharge(loanId, loanIdChargesRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.ADD_INSTALLMENT_FEE_CHARGE_RESPONSE, loanChargeResponse);
    }

    @Then("Admin fails to add {string} installment charge with {double} amount because of wrong charge calculation type")
    public void addInstallmentFeeChargeFails(final String chargeType, final double amount) throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse != null;

        final long loanId = loanResponse.getLoanId();
        final ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        final Long chargeTypeId = chargeProductType.getValue();

        final PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest()
                .chargeId(chargeTypeId).amount(amount);

        try {
            fineractClient.loanCharges().executeLoanCharge(loanId, loanIdChargesRequest, Map.<String, Object>of());
            throw new AssertionError("Expected FeignException but request succeeded");
        } catch (FeignException e) {
            final ErrorResponse errorDetails = ErrorResponse.fromFeignException(e);
            assertThat(errorDetails.getHttpStatusCode()).isEqualTo(400);
            String expectedMessage = chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST.getValue())
                    ? ErrorMessageHelper.addInstallmentFeeInterestPercentageChargeFailure()
                    : ErrorMessageHelper.addInstallmentFeePrincipalPercentageChargeFailure();
            assertThat(errorDetails.getSingleError().getDeveloperMessage()).contains(expectedMessage);
        }
    }

    @Then("Admin is not able to add {string} due date charge with {string} due date and {double} EUR transaction amount because the of charged-off account")
    public void addChargeDueDateOnChargedOff(String chargeType, String transactionDate, double transactionAmount) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeTypeId = chargeProductType.getValue();

        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest().chargeId(chargeTypeId)
                .dueDate(transactionDate).amount(transactionAmount);

        try {
            fineractClient.loanCharges().executeLoanCharge(loanId, loanIdChargesRequest, Map.<String, Object>of());
            throw new AssertionError("Expected FeignException but request succeeded");
        } catch (FeignException e) {
            ErrorResponse errorDetails = ErrorResponse.fromFeignException(e);
            assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.addChargeForChargeOffLoanCodeMsg()).isEqualTo(403);
            assertThat(errorDetails.getSingleError().getDeveloperMessage())
                    .contains(ErrorMessageHelper.addChargeForChargeOffLoanFailure(loanId));
        }
    }

    @And("Admin adds a {double} % Processing charge to the loan with {string} locale on date: {string}")
    public void addProcessingFee(double chargeAmount, String locale, String date) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest()
                .chargeId(ChargeProductType.LOAN_PERCENTAGE_PROCESSING_FEE.value).amount(chargeAmount).dueDate(date)
                .dateFormat(DEFAULT_DATE_FORMAT).locale(locale);

        PostLoansLoanIdChargesResponse loanChargeResponse = ok(
                () -> fineractClient.loanCharges().executeLoanCharge(loanId, loanIdChargesRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.ADD_PROCESSING_FEE_RESPONSE, loanChargeResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @And("Admin adds an NSF fee because of payment bounce with {string} transaction date")
    public void addNSFfee(String date) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest()
                .chargeId(ChargeProductType.LOAN_NSF_FEE.value).amount(DEFAULT_CHARGE_FEE_FLAT).dueDate(date)
                .dateFormat(DEFAULT_DATE_FORMAT);

        PostLoansLoanIdChargesResponse loanChargeResponse = ok(
                () -> fineractClient.loanCharges().executeLoanCharge(loanId, loanIdChargesRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.ADD_NSF_FEE_RESPONSE, loanChargeResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @And("Admin waives charge")
    public void waiveCharge() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdChargesResponse loanChargeResponse = testContext().get(TestContextKey.ADD_NSF_FEE_RESPONSE);
        Long chargeId = Long.valueOf(loanChargeResponse.getResourceId());

        PostLoansLoanIdChargesChargeIdRequest waiveRequest = new PostLoansLoanIdChargesChargeIdRequest();

        PostLoansLoanIdChargesChargeIdResponse waiveResponse = ok(() -> fineractClient.loanCharges().executeLoanCharge2(loanId, chargeId,
                waiveRequest, Map.<String, Object>of("command", "waive")));
        testContext().set(TestContextKey.WAIVE_CHARGE_RESPONSE, waiveResponse);
    }

    @And("Admin waives due date charge")
    public void waiveDueDateCharge() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdChargesResponse loanChargeResponse = testContext().get(TestContextKey.ADD_DUE_DATE_CHARGE_RESPONSE);
        Long chargeId = Long.valueOf(loanChargeResponse.getResourceId());

        PostLoansLoanIdChargesChargeIdRequest waiveRequest = new PostLoansLoanIdChargesChargeIdRequest();

        PostLoansLoanIdChargesChargeIdResponse waiveResponse = ok(() -> fineractClient.loanCharges().executeLoanCharge2(loanId, chargeId,
                waiveRequest, Map.<String, Object>of("command", "waive")));
        testContext().set(TestContextKey.WAIVE_CHARGE_RESPONSE, waiveResponse);
    }

    @And("Admin makes waive undone for charge")
    public void undoWaiveForCharge() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.<String, Object>of("associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetails.getTransactions();

        final Long transactionId = transactions.stream().filter(t -> "loanTransactionType.waiveCharges".equals(t.getType().getCode()))
                .findFirst().map(GetLoansLoanIdTransactions::getId).orElse(0L);

        PutChargeTransactionChangesRequest undoWaiveRequest = new PutChargeTransactionChangesRequest();
        PutChargeTransactionChangesResponse undoWaiveResponse = ok(
                () -> fineractClient.loanTransactions().undoWaiveCharge(loanId, transactionId, undoWaiveRequest));
        testContext().set(TestContextKey.UNDO_WAIVE_RESPONSE, undoWaiveResponse);
    }

    @Then("Charge is successfully added to the loan")
    public void loanChargeStatus() throws IOException {
        PostLoansLoanIdChargesResponse response = testContext().get(TestContextKey.ADD_NSF_FEE_RESPONSE);

        assertThat(response).as("Charge response should not be null").isNotNull();
        assertThat(response.getResourceId()).as("Charge resource ID should be present").isNotNull();
    }

    @Then("Charge is successfully added to the loan with {float} EUR")
    public void checkLoanChargeAmount(float chargeAmount) throws IOException {
        PostLoansLoanIdChargesResponse response = testContext().get(TestContextKey.ADD_PROCESSING_FEE_RESPONSE);
        GetLoansLoanIdChargesChargeIdResponse loanChargeAmount = ok(
                () -> fineractClient.loanCharges().retrieveLoanCharge(response.getLoanId(), Long.valueOf(response.getResourceId())));
        assertThat(loanChargeAmount.getAmount()).as("Charge amount is wrong").isEqualByComparingTo(Double.valueOf(chargeAmount));
    }

    private void addChargeEventCheck(PostLoansLoanIdChargesResponse loanChargeResponse) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_EVENTS);
        GetLoansLoanIdChargesChargeIdResponse chargeDetails = ok(
                () -> fineractClient.loanCharges().retrieveLoanCharge(loanChargeResponse.getLoanId(), loanChargeResponse.getResourceId()));
        GetLoansLoanIdChargesChargeIdResponse body = chargeDetails;

        eventAssertion.assertEvent(LoanAddChargeEvent.class, loanChargeResponse.getResourceId()).extractingData(LoanChargeDataV1::getName)
                .isEqualTo(body.getName()).extractingBigDecimal(LoanChargeDataV1::getAmount).isEqualTo(BigDecimal.valueOf(body.getAmount()))
                .extractingData(LoanChargeDataV1::getDueDate).isEqualTo(formatter.format(body.getDueDate()));
    }

    @Then("Loan charge transaction with the following data results a {int} error and {string} error message")
    public void chargeOffTransactionError(int errorCodeExpected, String errorMessageType, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> chargeData = data.get(1);
        String chargeType = chargeData.get(0);
        String transactionDate = chargeData.get(1);
        Double transactionAmount = Double.valueOf(chargeData.get(2));

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        ErrorMessageType errorMsgType = ErrorMessageType.valueOf(errorMessageType);
        String errorMessageExpectedRaw = errorMsgType.getValue();
        String errorMessageExpected = String.format(errorMessageExpectedRaw, loanId);

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeTypeId = chargeProductType.getValue();
        if (chargeTypeId.equals(ChargeProductType.LOAN_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                || chargeTypeId.equals(ChargeProductType.LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                || chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST.getValue())) {
            throw new IllegalStateException(String.format("The requested %s charge is NOT due date type, cannot be used here", chargeType));
        }

        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest().chargeId(chargeTypeId)
                .dueDate(transactionDate).amount(transactionAmount);

        try {
            fineractClient.loanCharges().executeLoanCharge(loanId, loanIdChargesRequest, Map.<String, Object>of());
            throw new AssertionError("Expected FeignException but request succeeded");
        } catch (FeignException e) {
            ErrorResponse errorResponse = ErrorResponse.fromFeignException(e);
            int errorCodeActual = errorResponse.getHttpStatusCode();
            String errorMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

            assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected))
                    .isEqualTo(errorCodeExpected);
            assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                    .contains(errorMessageExpected);

            log.debug("ERROR CODE: {}", errorCodeActual);
            log.debug("ERROR MESSAGE: {}", errorMessageActual);
        }
    }
}
