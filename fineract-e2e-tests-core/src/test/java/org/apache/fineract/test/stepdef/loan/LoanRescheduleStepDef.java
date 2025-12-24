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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostCreateRescheduleLoansResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.test.data.LoanRescheduleErrorMessage;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class LoanRescheduleStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT_HU = "yyyy-MM-dd";
    public static final String DATE_FORMAT_EN = "dd MMMM yyyy";
    public static final DateTimeFormatter FORMATTER_HU = DateTimeFormatter.ofPattern(DATE_FORMAT_HU);
    public static final DateTimeFormatter FORMATTER_EN = DateTimeFormatter.ofPattern(DATE_FORMAT_EN);

    @Autowired
    private FineractFeignClient fineractClient;

    @When("Admin creates and approves Loan reschedule with the following data:")
    public void createAndApproveLoanReschedule(DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        List<List<String>> data = table.asLists();
        List<String> rescheduleData = data.get(1);

        String rescheduleFromDate = rescheduleData.get(0);
        String submittedOnDate = rescheduleData.get(1);
        String adjustedDueDate = rescheduleData.get(2);
        Integer graceOfPrincipal = (rescheduleData.get(3) == null || "0".equals(rescheduleData.get(3))) ? null
                : Integer.valueOf(rescheduleData.get(3));
        Integer graceOnInterest = (rescheduleData.get(4) == null || "0".equals(rescheduleData.get(4))) ? null
                : Integer.valueOf(rescheduleData.get(4));
        Integer extraTerms = (rescheduleData.get(5) == null || "0".equals(rescheduleData.get(5))) ? null
                : Integer.valueOf(rescheduleData.get(5));
        BigDecimal newInterestRate = (rescheduleData.get(6) == null) ? null : new BigDecimal(rescheduleData.get(6));

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

        PostCreateRescheduleLoansResponse createResponse = ok(() -> fineractClient.rescheduleLoans().createLoanRescheduleRequest(request));

        Long scheduleId = createResponse.getResourceId();
        PostUpdateRescheduleLoansRequest approveRequest = new PostUpdateRescheduleLoansRequest()//
                .approvedOnDate(submittedOnDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");//

        ok(() -> fineractClient.rescheduleLoans().updateLoanRescheduleRequest(scheduleId, approveRequest,
                Map.<String, Object>of("command", "approve")));
    }

    @Then("Loan reschedule with the following data results a {int} error and {string} error message")
    public void createLoanRescheduleError(int errorCodeExpected, String errorMessageType, DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        List<List<String>> data = table.asLists();
        List<String> rescheduleData = data.get(1);
        String rescheduleFromDate = rescheduleData.get(0);
        String submittedOnDate = rescheduleData.get(1);
        String adjustedDueDate = rescheduleData.get(2);
        Integer graceOfPrincipal = (rescheduleData.get(3) == null || "0".equals(rescheduleData.get(3))) ? null
                : Integer.valueOf(rescheduleData.get(3));
        Integer graceOnInterest = (rescheduleData.get(4) == null || "0".equals(rescheduleData.get(4))) ? null
                : Integer.valueOf(rescheduleData.get(4));
        Integer extraTerms = (rescheduleData.get(5) == null || "0".equals(rescheduleData.get(5))) ? null
                : Integer.valueOf(rescheduleData.get(5));
        BigDecimal newInterestRate = rescheduleData.get(6) == null ? null : new BigDecimal(rescheduleData.get(6));

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

        LoanRescheduleErrorMessage loanRescheduleErrorMessage = LoanRescheduleErrorMessage.valueOf(errorMessageType);

        LocalDate localDate = LocalDate.parse(rescheduleFromDate, FORMATTER_EN);
        String rescheduleFromDateFormatted = localDate.format(FORMATTER_HU);
        String errorMessageExpected = "";
        int expectedParameterCount = loanRescheduleErrorMessage.getExpectedParameterCount();
        if (expectedParameterCount == 0) {
            errorMessageExpected = loanRescheduleErrorMessage.getMessageTemplate();
        } else if (expectedParameterCount == 1) {
            errorMessageExpected = loanRescheduleErrorMessage.getValue(loanId);
        } else if (expectedParameterCount == 2) {
            errorMessageExpected = loanRescheduleErrorMessage.getValue(rescheduleFromDateFormatted, loanId);
        } else {
            throw new IllegalStateException("Parameter count in Error message does not met the criteria");
        }

        CallFailedRuntimeException exception = fail(() -> fineractClient.rescheduleLoans().createLoanRescheduleRequest(request));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.wrongErrorCode(exception.getStatus(), errorCodeExpected))
                .isEqualTo(errorCodeExpected);
        assertThat(exception.getDeveloperMessage())
                .as(ErrorMessageHelper.wrongErrorMessage(exception.getDeveloperMessage(), errorMessageExpected))
                .contains(errorMessageExpected);

        log.debug("ERROR CODE: {}", exception.getStatus());
        log.debug("ERROR MESSAGE: {}", exception.getDeveloperMessage());
    }
}
