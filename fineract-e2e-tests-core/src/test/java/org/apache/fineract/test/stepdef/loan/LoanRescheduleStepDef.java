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
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostCreateRescheduleLoansResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansResponse;
import org.apache.fineract.client.services.RescheduleLoansApi;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.test.data.LoanRescheduleErrorMessage;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class LoanRescheduleStepDef extends AbstractStepDef {

    private static final Gson GSON = new JSON().getGson();

    @Autowired
    private RescheduleLoansApi rescheduleLoansApi;

    @When("Admin creates and approves Loan reschedule with the following data:")
    public void createAndApproveLoanReschedule(DataTable table) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        List<List<String>> data = table.asLists();
        List<String> rescheduleData = data.get(1);
        String rescheduleFromDate = rescheduleData.get(0);
        String submittedOnDate = rescheduleData.get(1);
        String adjustedDueDate = rescheduleData.get(2);
        Integer graceOfPrincipal = rescheduleData.get(3) != null ? Integer.parseInt(rescheduleData.get(3)) : null;
        Integer graceOnInterest = rescheduleData.get(4) != null ? Integer.parseInt(rescheduleData.get(4)) : null;
        Integer extraTerms = rescheduleData.get(5) != null ? Integer.parseInt(rescheduleData.get(5)) : null;
        BigDecimal newInterestRate = rescheduleData.get(6) != null ? new BigDecimal(rescheduleData.get(6)) : null;

        PostCreateRescheduleLoansRequest request = new PostCreateRescheduleLoansRequest()//
                .loanId(loanId)//
                .rescheduleFromDate(rescheduleFromDate)//
                .submittedOnDate(submittedOnDate)//
                .adjustedDueDate(adjustedDueDate)//
                .graceOnPrincipal(graceOfPrincipal)//
                .graceOnInterest(graceOnInterest)//
                .extraTerms(extraTerms)//
                .newInterestRate(newInterestRate)//
                .rescheduleReasonId(54L)//
                .rescheduleReasonComment("")//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");//

        Response<PostCreateRescheduleLoansResponse> createResponse = rescheduleLoansApi.createLoanRescheduleRequest(request).execute();
        ErrorHelper.checkSuccessfulApiCall(createResponse);

        Long scheduleId = createResponse.body().getResourceId();
        PostUpdateRescheduleLoansRequest approveRequest = new PostUpdateRescheduleLoansRequest()//
                .approvedOnDate(submittedOnDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");//

        Response<PostUpdateRescheduleLoansResponse> approveResponse = rescheduleLoansApi
                .updateLoanRescheduleRequest(scheduleId, approveRequest, "approve").execute();
        ErrorHelper.checkSuccessfulApiCall(approveResponse);
    }

    @Then("Loan reschedule with the following data results a {int} error and {string} error message")
    public void createLoanRescheduleError(int errorCodeExpected, String errorMessageType, DataTable table) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        List<List<String>> data = table.asLists();
        List<String> rescheduleData = data.get(1);
        String rescheduleFromDate = rescheduleData.get(0);
        String submittedOnDate = rescheduleData.get(1);
        String adjustedDueDate = rescheduleData.get(2);
        Integer graceOfPrincipal = rescheduleData.get(3) != null ? Integer.parseInt(rescheduleData.get(3)) : null;
        Integer graceOnInterest = rescheduleData.get(4) != null ? Integer.parseInt(rescheduleData.get(4)) : null;
        Integer extraTerms = rescheduleData.get(5) != null ? Integer.parseInt(rescheduleData.get(5)) : null;
        BigDecimal newInterestRate = rescheduleData.get(6) != null ? new BigDecimal(rescheduleData.get(6)) : null;

        PostCreateRescheduleLoansRequest request = new PostCreateRescheduleLoansRequest()//
                .loanId(loanId)//
                .rescheduleFromDate(rescheduleFromDate)//
                .submittedOnDate(submittedOnDate)//
                .adjustedDueDate(adjustedDueDate)//
                .graceOnPrincipal(graceOfPrincipal)//
                .graceOnInterest(graceOnInterest)//
                .extraTerms(extraTerms)//
                .newInterestRate(newInterestRate)//
                .rescheduleReasonId(54L)//
                .rescheduleReasonComment("")//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");//

        Response<PostCreateRescheduleLoansResponse> createResponse = rescheduleLoansApi.createLoanRescheduleRequest(request).execute();

        LoanRescheduleErrorMessage loanRescheduleErrorMessage = LoanRescheduleErrorMessage.valueOf(errorMessageType);
        String errorMessageExpected = loanRescheduleErrorMessage.getValue(loanId);

        String errorToString = createResponse.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorToString, ErrorResponse.class);
        String errorMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();
        int errorCodeActual = createResponse.code();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.info("ERROR CODE: {}", errorCodeActual);
        log.info("ERROR MESSAGE: {}", errorMessageActual);
    }
}
