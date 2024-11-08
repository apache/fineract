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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanChargeDataV1;
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
import org.apache.fineract.client.services.LoanChargesApi;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.data.ErrorMessageType;
import org.apache.fineract.test.factory.LoanChargeRequestFactory;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.loan.charge.LoanAddChargeEvent;
import org.apache.fineract.test.messaging.store.EventStore;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class LoanChargeStepDef extends AbstractStepDef {

    public static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy";
    public static final String DATE_FORMAT_EVENTS = "yyyy-MM-dd";
    public static final Double DEFAULT_CHARGE_FEE_FLAT = 10D;
    private static final Gson GSON = new JSON().getGson();

    @Autowired
    private LoanChargesApi loanChargesApi;
    @Autowired
    private LoanTransactionsApi loanTransactionsApi;
    @Autowired
    private LoansApi loansApi;
    @Autowired
    private EventAssertion eventAssertion;
    @Autowired
    private EventCheckHelper eventCheckHelper;
    @Autowired
    private EventStore eventStore;

    @When("Admin adds {string} due date charge with {string} due date and {double} EUR transaction amount")
    public void addChargeDueDate(String chargeType, String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeTypeId = chargeProductType.getValue();
        if (chargeTypeId.equals(ChargeProductType.LOAN_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                || chargeTypeId.equals(ChargeProductType.LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                || chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_PERCENTAGE_FEE.getValue())) {
            throw new IllegalStateException(String.format("The requested %s charge is NOT due date type, cannot be used here", chargeType));
        }

        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest().chargeId(chargeTypeId)
                .dueDate(transactionDate).amount(transactionAmount);

        Response<PostLoansLoanIdChargesResponse> loanChargeResponse = loanChargesApi.executeLoanCharge(loanId, loanIdChargesRequest, "")
                .execute();
        ErrorHelper.checkSuccessfulApiCall(loanChargeResponse);
        testContext().set(TestContextKey.ADD_DUE_DATE_CHARGE_RESPONSE, loanChargeResponse);

        addChargeEventCheck(loanChargeResponse);
    }

    @When("Admin adds {string} charge with {double} % of transaction amount")
    public void addChargePercentage(String chargeType, double transactionPercentageAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeTypeId = chargeProductType.getValue();
        if (!chargeTypeId.equals(ChargeProductType.LOAN_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                && !chargeTypeId.equals(ChargeProductType.LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                && !chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_PERCENTAGE_FEE.getValue())) {
            throw new IllegalStateException(String.format("The requested %s charge is due date type, cannot be used here", chargeType));
        }

        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest().chargeId(chargeTypeId)
                .amount(transactionPercentageAmount);

        Response<PostLoansLoanIdChargesResponse> loanChargeResponse = loanChargesApi.executeLoanCharge(loanId, loanIdChargesRequest, "")
                .execute();
        ErrorHelper.checkSuccessfulApiCall(loanChargeResponse);
        testContext().set(TestContextKey.ADD_DUE_DATE_CHARGE_RESPONSE, loanChargeResponse);
    }

    @Then("Admin is not able to add {string} due date charge with {string} due date and {double} EUR transaction amount because the of charged-off account")
    public void addChargeDueDateOnChargedOff(String chargeType, String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeTypeId = chargeProductType.getValue();

        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest().chargeId(chargeTypeId)
                .dueDate(transactionDate).amount(transactionAmount);

        Response<PostLoansLoanIdChargesResponse> loanChargeResponse = loanChargesApi.executeLoanCharge(loanId, loanIdChargesRequest, "")
                .execute();
        testContext().set(TestContextKey.ADD_DUE_DATE_CHARGE_RESPONSE, loanChargeResponse);
        ErrorResponse errorDetails = ErrorResponse.from(loanChargeResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.addChargeForChargeOffLoanCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.addChargeForChargeOffLoanFailure(loanId));
    }

    @And("Admin adds a {double} % Processing charge to the loan with {string} locale on date: {string}")
    public void addProcessingFee(double chargeAmount, String locale, String date) throws IOException {
        eventStore.reset();
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest()
                .chargeId(ChargeProductType.LOAN_PERCENTAGE_PROCESSING_FEE.value).amount(chargeAmount).dueDate(date)
                .dateFormat(DEFAULT_DATE_FORMAT).locale(locale);

        Response<PostLoansLoanIdChargesResponse> loanChargeResponse = loanChargesApi.executeLoanCharge(loanId, loanIdChargesRequest, "")
                .execute();
        ErrorHelper.checkSuccessfulApiCall(loanChargeResponse);
        testContext().set(TestContextKey.ADD_PROCESSING_FEE_RESPONSE, loanChargeResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @And("Admin adds an NSF fee because of payment bounce with {string} transaction date")
    public void addNSFfee(String date) throws IOException {
        eventStore.reset();
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest()
                .chargeId(ChargeProductType.LOAN_NSF_FEE.value).amount(DEFAULT_CHARGE_FEE_FLAT).dueDate(date)
                .dateFormat(DEFAULT_DATE_FORMAT);

        Response<PostLoansLoanIdChargesResponse> loanChargeResponse = loanChargesApi.executeLoanCharge(loanId, loanIdChargesRequest, "")
                .execute();
        ErrorHelper.checkSuccessfulApiCall(loanChargeResponse);
        testContext().set(TestContextKey.ADD_NSF_FEE_RESPONSE, loanChargeResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @And("Admin waives charge")
    public void waiveCharge() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdChargesResponse> loanChargeResponse = testContext().get(TestContextKey.ADD_NSF_FEE_RESPONSE);
        Long chargeId = Long.valueOf(loanChargeResponse.body().getResourceId());

        PostLoansLoanIdChargesChargeIdRequest waiveRequest = new PostLoansLoanIdChargesChargeIdRequest();

        Response<PostLoansLoanIdChargesChargeIdResponse> waiveResponse = loanChargesApi
                .executeLoanCharge2(loanId, chargeId, waiveRequest, "waive").execute();
        ErrorHelper.checkSuccessfulApiCall(waiveResponse);
        testContext().set(TestContextKey.WAIVE_CHARGE_RESPONSE, waiveResponse);
    }

    @And("Admin waives due date charge")
    public void waiveDueDateCharge() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdChargesResponse> loanChargeResponse = testContext().get(TestContextKey.ADD_DUE_DATE_CHARGE_RESPONSE);
        Long chargeId = Long.valueOf(loanChargeResponse.body().getResourceId());

        PostLoansLoanIdChargesChargeIdRequest waiveRequest = new PostLoansLoanIdChargesChargeIdRequest();

        Response<PostLoansLoanIdChargesChargeIdResponse> waiveResponse = loanChargesApi
                .executeLoanCharge2(loanId, chargeId, waiveRequest, "waive").execute();
        ErrorHelper.checkSuccessfulApiCall(waiveResponse);
        testContext().set(TestContextKey.WAIVE_CHARGE_RESPONSE, waiveResponse);
    }

    @And("Admin makes waive undone for charge")
    public void undoWaiveForCharge() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        List<GetLoansLoanIdTransactions> transactions = loanDetails.body().getTransactions();

        Long transactionId = 0L;
        for (GetLoansLoanIdTransactions f : transactions) {
            String code = f.getType().getCode();
            if (code.equals("loanTransactionType.waiveCharges")) {
                transactionId = f.getId();
            }
        }

        PutChargeTransactionChangesRequest undoWaiveRequest = new PutChargeTransactionChangesRequest();
        Response<PutChargeTransactionChangesResponse> undoWaiveResponse = loanTransactionsApi
                .undoWaiveCharge(loanId, transactionId, undoWaiveRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(undoWaiveResponse);
        testContext().set(TestContextKey.UNDO_WAIVE_RESPONSE, undoWaiveResponse);
    }

    @Then("Charge is successfully added to the loan")
    public void loanChargeStatus() throws IOException {
        Response<PostLoansLoanIdChargesResponse> response = testContext().get(TestContextKey.ADD_NSF_FEE_RESPONSE);

        assertThat(response.isSuccessful()).as(ErrorMessageHelper.requestFailed(response)).isTrue();
        assertThat(response.code()).as(ErrorMessageHelper.requestFailedWithCode(response)).isEqualTo(200);
    }

    @Then("Charge is successfully added to the loan with {float} EUR")
    public void checkLoanChargeAmount(float chargeAmount) throws IOException {
        Response<PostLoansLoanIdChargesResponse> response = testContext().get(TestContextKey.ADD_PROCESSING_FEE_RESPONSE);
        Response<GetLoansLoanIdChargesChargeIdResponse> loanChargeAmount = loanChargesApi
                .retrieveLoanCharge(response.body().getLoanId(), Long.valueOf(response.body().getResourceId())).execute();

        ErrorHelper.checkSuccessfulApiCall(response);
        assertThat(loanChargeAmount.body().getAmount()).as("Charge amount is wrong").isEqualTo(chargeAmount);
    }

    private void addChargeEventCheck(Response<PostLoansLoanIdChargesResponse> loanChargeResponse) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_EVENTS);
        Response<GetLoansLoanIdChargesChargeIdResponse> chargeDetails = loanChargesApi
                .retrieveLoanCharge(loanChargeResponse.body().getLoanId(), loanChargeResponse.body().getResourceId()).execute();
        GetLoansLoanIdChargesChargeIdResponse body = chargeDetails.body();

        eventAssertion.assertEvent(LoanAddChargeEvent.class, loanChargeResponse.body().getResourceId())
                .extractingData(LoanChargeDataV1::getName).isEqualTo(body.getName()).extractingBigDecimal(LoanChargeDataV1::getAmount)
                .isEqualTo(BigDecimal.valueOf(body.getAmount())).extractingData(LoanChargeDataV1::getDueDate)
                .isEqualTo(formatter.format(body.getDueDate()));
    }

    @Then("Loan charge transaction with the following data results a {int} error and {string} error message")
    public void chargeOffTransactionError(int errorCodeExpected, String errorMessageType, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> chargeData = data.get(1);
        String chargeType = chargeData.get(0);
        String transactionDate = chargeData.get(1);
        Double transactionAmount = Double.valueOf(chargeData.get(2));

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        ErrorMessageType errorMsgType = ErrorMessageType.valueOf(errorMessageType);
        String errorMessageExpectedRaw = errorMsgType.getValue();
        String errorMessageExpected = String.format(errorMessageExpectedRaw, loanId);

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeTypeId = chargeProductType.getValue();
        if (chargeTypeId.equals(ChargeProductType.LOAN_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                || chargeTypeId.equals(ChargeProductType.LOAN_TRANCHE_DISBURSEMENT_PERCENTAGE_FEE.getValue())
                || chargeTypeId.equals(ChargeProductType.LOAN_INSTALLMENT_PERCENTAGE_FEE.getValue())) {
            throw new IllegalStateException(String.format("The requested %s charge is NOT due date type, cannot be used here", chargeType));
        }

        PostLoansLoanIdChargesRequest loanIdChargesRequest = LoanChargeRequestFactory.defaultLoanChargeRequest().chargeId(chargeTypeId)
                .dueDate(transactionDate).amount(transactionAmount);

        Response<PostLoansLoanIdChargesResponse> loanChargeResponse = loanChargesApi.executeLoanCharge(loanId, loanIdChargesRequest, "")
                .execute();
        int errorCodeActual = loanChargeResponse.code();
        String errorBody = loanChargeResponse.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorBody, ErrorResponse.class);
        String errorMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
    }
}
