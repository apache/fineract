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
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.BusinessDateManagementApi;
import org.apache.fineract.client.services.LoanChargesApi;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.store.EventStore;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class LoanChargeAdjustmentStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT = "dd MMMM yyyy";

    private static final Gson GSON = new JSON().getGson();

    @Autowired
    private LoanChargesApi loanChargesApi;
    @Autowired
    private LoansApi loansApi;
    @Autowired
    private LoanTransactionsApi loanTransactionsApi;
    @Autowired
    private BusinessDateManagementApi businessDateManagementApi;
    @Autowired
    private EventCheckHelper eventCheckHelper;
    @Autowired
    private EventStore eventStore;

    @When("Admin makes a charge adjustment for the last {string} type charge which is due on {string} with {double} EUR transaction amount and externalId {string}")
    public void makeLoanChargeAdjustment(String chargeTypeEnum, String date, Double transactionAmount, String externalId)
            throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "charges", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        Long transactionId = getTransactionIdForLastChargeMetConditions(chargeTypeEnum, date, loanDetailsResponse);
        makeChargeAdjustmentCall(loanId, transactionId, externalId, transactionAmount);
    }

    @Then("Charge adjustment for the last {string} type charge which is due on {string} with transaction amount {double} which is higher than the available charge amount results an ERROR")
    public void loanChargeAdjustmentFailedOnWrongAmount(String chargeTypeEnum, String date, double amount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "charges", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        Long transactionId = getTransactionIdForLastChargeMetConditions(chargeTypeEnum, date, loanDetailsResponse);
        PostLoansLoanIdChargesChargeIdRequest chargeAdjustmentRequest = LoanRequestFactory.defaultChargeAdjustmentRequest().amount(amount)
                .externalId("");

        Response<PostLoansLoanIdChargesChargeIdResponse> chargeAdjustmentResponseFail = loanChargesApi
                .executeLoanCharge2(loanId, transactionId, chargeAdjustmentRequest, "adjustment").execute();

        String string = chargeAdjustmentResponseFail.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(string, ErrorResponse.class);
        Integer httpStatusCodeActual = errorResponse.getHttpStatusCode();
        String developerMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

        Integer httpStatusCodeExpected = 403;
        String developerMessageExpected = "Transaction amount cannot be higher than the available charge amount for adjustment: 7.000000";

        assertThat(httpStatusCodeActual)
                .as(ErrorMessageHelper.wrongErrorCodeInFailedChargeAdjustment(httpStatusCodeActual, httpStatusCodeExpected))
                .isEqualTo(httpStatusCodeExpected);
        assertThat(developerMessageActual)
                .as(ErrorMessageHelper.wrongErrorMessageInFailedChargeAdjustment(developerMessageActual, developerMessageExpected))
                .isEqualTo(developerMessageExpected);

        log.debug("Error code: {}", httpStatusCodeActual);
        log.debug("Error message: {}", developerMessageActual);
    }

    @When("Admin reverts the charge adjustment which was raised on {string} with {double} EUR transaction amount")
    public void loanChargeAdjustmentUndo(String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        Long transactionId = getTransactionIdForTransactionMetConditions(transactionDate, transactionAmount, loanDetailsResponse);

        Response<List<BusinessDateResponse>> businessDateResponse = businessDateManagementApi.getBusinessDates().execute();
        LocalDate businessDate = businessDateResponse.body().get(0).getDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String businessDateActual = formatter.format(businessDate);

        PostLoansLoanIdTransactionsTransactionIdRequest chargeAdjustmentUndoRequest = LoanRequestFactory
                .defaultChargeAdjustmentTransactionUndoRequest().transactionDate(businessDateActual);

        Response<PostLoansLoanIdTransactionsResponse> chargeAdjustmentUndoResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, transactionId, chargeAdjustmentUndoRequest, "").execute();
        ErrorHelper.checkSuccessfulApiCall(chargeAdjustmentUndoResponse);
    }

    private Long getTransactionIdForTransactionMetConditions(String transactionDate, double transactionAmount,
            Response<GetLoansLoanIdResponse> loanDetailsResponse) {
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions transactionMetConditions = new GetLoansLoanIdTransactions();
        for (int i = 0; i < transactions.size(); i++) {
            LocalDate date = transactions.get(i).getDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            String dateActual = formatter.format(date);

            Double amountActual = transactions.get(i).getAmount();

            if (dateActual.equals(transactionDate) && amountActual.equals(transactionAmount)) {
                transactionMetConditions = transactions.get(i);
                break;
            }
        }
        return transactionMetConditions.getId();
    }

    private void makeChargeAdjustmentCall(Long loanId, Long transactionId, String externalId, double transactionAmount) throws IOException {
        eventStore.reset();
        PostLoansLoanIdChargesChargeIdRequest chargeAdjustmentRequest = LoanRequestFactory.defaultChargeAdjustmentRequest()
                .amount(transactionAmount).externalId(externalId);

        Response<PostLoansLoanIdChargesChargeIdResponse> chargeAdjustmentResponse = loanChargesApi
                .executeLoanCharge2(loanId, transactionId, chargeAdjustmentRequest, "adjustment").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_ADJUSTMENT_RESPONSE, chargeAdjustmentResponse);
        ErrorHelper.checkSuccessfulApiCall(chargeAdjustmentResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    private Long getTransactionIdForLastChargeMetConditions(String chargeTypeEnum, String date,
            Response<GetLoansLoanIdResponse> loanDetailsResponse) {
        List<GetLoansLoanIdLoanChargeData> charges = loanDetailsResponse.body().getCharges();

        ChargeProductType chargeType = ChargeProductType.valueOf(chargeTypeEnum);
        Long chargeProductId = chargeType.getValue();

        List<GetLoansLoanIdLoanChargeData> resultList = new ArrayList<>();
        charges.forEach(charge -> {
            Long chargeId = charge.getChargeId();
            LocalDate dueDate = charge.getDueDate();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            String chargeDueDate = formatter.format(dueDate);

            if (chargeId.equals(chargeProductId) && chargeDueDate.equals(date)) {
                resultList.add(charge);
            }
        });

        GetLoansLoanIdLoanChargeData lastChargeResult = resultList.get(resultList.size() - 1);
        return lastChargeResult.getId();
    }
}
