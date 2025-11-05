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

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.LoanReAgeEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class LoanReAgingStepDef extends AbstractStepDef {

    @Autowired
    private LoanTransactionsApi loanTransactionsApi;

    @Autowired
    private EventAssertion eventAssertion;

    @When("Admin creates a Loan re-aging transaction with the following data:")
    public void createReAgingTransaction(DataTable table) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        List<List<String>> tableRows = table.asLists();
        List<String> headers = tableRows.get(0);
        List<String> values = tableRows.get(1);

        Map<String, String> rowData = new LinkedHashMap<>();
        int columnCount = Math.min(headers.size(), values.size());
        for (int i = 0; i < columnCount; i++) {
            rowData.put(headers.get(i), values.get(i));
        }

        int frequencyNumber = Integer.parseInt(resolveValue(rowData, values, 0, "frequencyNumber"));
        String frequencyType = resolveValue(rowData, values, 1, "frequencyType");
        String startDate = resolveValue(rowData, values, 2, "startDate");
        int numberOfInstallments = Integer.parseInt(resolveValue(rowData, values, 3, "numberOfInstallments"));

        PostLoansLoanIdTransactionsRequest reAgingRequest = LoanRequestFactory//
                .defaultReAgingRequest()//
                .frequencyNumber(frequencyNumber)//
                .frequencyType(frequencyType)//
                .startDate(startDate)//
                .numberOfInstallments(numberOfInstallments);//

        applyAdditionalFields(reAgingRequest, rowData, Set.of("frequencyNumber", "frequencyType", "startDate", "numberOfInstallments"));

        Response<PostLoansLoanIdTransactionsResponse> response = loanTransactionsApi.executeLoanTransaction(loanId, reAgingRequest, "reAge")
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.LOAN_REAGING_RESPONSE, response);
    }

    private void applyAdditionalFields(PostLoansLoanIdTransactionsRequest request, Map<String, String> rowData, Set<String> excludedKeys) {
        rowData.forEach((key, value) -> {
            if (!excludedKeys.contains(key)) {
                setRequestField(request, key, value);
            }
        });
    }

    private void setRequestField(PostLoansLoanIdTransactionsRequest request, String fieldName, String rawValue) {
        if (fieldName == null || fieldName.isBlank()) {
            return;
        }

        try {
            Method targetMethod = Arrays.stream(PostLoansLoanIdTransactionsRequest.class.getMethods())
                    .filter(method -> method.getParameterCount() == 1 && method.getName().equals(fieldName)).findFirst().orElse(null);

            if (targetMethod == null) {
                log.warn("No setter method found on PostLoansLoanIdTransactionsRequest for field {}", fieldName);
                return;
            }

            Class<?> parameterType = targetMethod.getParameterTypes()[0];
            Object convertedValue = convertValue(rawValue, parameterType);

            if (convertedValue == null && parameterType.isPrimitive()) {
                log.warn("Cannot assign null to primitive field {} on PostLoansLoanIdTransactionsRequest", fieldName);
                return;
            }

            targetMethod.invoke(request, convertedValue);
        } catch (Exception ex) {
            log.warn("Failed to set additional field {} on PostLoansLoanIdTransactionsRequest", fieldName, ex);
        }
    }

    private Object convertValue(String rawValue, Class<?> targetType) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        try {
            if (String.class.equals(targetType)) {
                return rawValue;
            }
            if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
                return Integer.valueOf(rawValue);
            }
            if (Long.class.equals(targetType) || long.class.equals(targetType)) {
                return Long.valueOf(rawValue);
            }
            if (Double.class.equals(targetType) || double.class.equals(targetType)) {
                return Double.valueOf(rawValue);
            }
            if (Float.class.equals(targetType) || float.class.equals(targetType)) {
                return Float.valueOf(rawValue);
            }
            if (Short.class.equals(targetType) || short.class.equals(targetType)) {
                return Short.valueOf(rawValue);
            }
            if (Byte.class.equals(targetType) || byte.class.equals(targetType)) {
                return Byte.valueOf(rawValue);
            }
            if (Boolean.class.equals(targetType) || boolean.class.equals(targetType)) {
                return Boolean.parseBoolean(rawValue);
            }
            if (BigDecimal.class.equals(targetType)) {
                return new BigDecimal(rawValue);
            }
        } catch (NumberFormatException ex) {
            log.warn("Unable to convert value '{}' to type {}. Falling back to raw string.", rawValue, targetType.getSimpleName(), ex);
            return rawValue;
        }

        return rawValue;
    }

    private String resolveValue(Map<String, String> rowData, List<String> values, int index, String key) {
        String value = rowData.get(key);
        if (value != null) {
            return value;
        }
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        }
        return null;
    }

    @When("Admin creates a Loan re-aging transaction by Loan external ID with the following data:")
    public void createReAgingTransactionByLoanExternalId(DataTable table) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.body().getResourceExternalId();

        List<String> data = table.asLists().get(1);
        int frequencyNumber = Integer.parseInt(data.get(0));
        String frequencyType = data.get(1);
        String startDate = data.get(2);
        int numberOfInstallments = Integer.parseInt(data.get(3));

        PostLoansLoanIdTransactionsRequest reAgingRequest = LoanRequestFactory//
                .defaultReAgingRequest()//
                .frequencyNumber(frequencyNumber)//
                .frequencyType(frequencyType)//
                .startDate(startDate)//
                .numberOfInstallments(numberOfInstallments);//

        Response<PostLoansLoanIdTransactionsResponse> response = loanTransactionsApi
                .executeLoanTransaction1(loanExternalId, reAgingRequest, "reAge").execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.LOAN_REAGING_RESPONSE, response);
    }

    @When("Admin successfully undo Loan re-aging transaction")
    public void undoReAgingTransaction() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdTransactionsResponse> response = loanTransactionsApi
                .executeLoanTransaction(loanId, new PostLoansLoanIdTransactionsRequest(), "undoReAge").execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.LOAN_REAGING_UNDO_RESPONSE, response);
    }

    @Then("LoanReAgeBusinessEvent is created")
    public void checkLoanReAmortizeBusinessEventCreated() {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        eventAssertion.assertEventRaised(LoanReAgeEvent.class, loanId);
    }

    @When("Admin fails to create a Loan re-aging transaction with error {string} and with the following data:")
    public void adminFailsToCreateReAgingTransactionWithError(final String expectedError, final DataTable table) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.body().getLoanId();

        final List<String> data = table.asLists().get(1);
        final int frequencyNumber = Integer.parseInt(data.get(0));
        final String frequencyType = data.get(1);
        final String startDate = data.get(2);
        final int numberOfInstallments = Integer.parseInt(data.get(3));

        final PostLoansLoanIdTransactionsRequest reAgingRequest = LoanRequestFactory//
                .defaultReAgingRequest()//
                .frequencyNumber(frequencyNumber)//
                .frequencyType(frequencyType)//
                .startDate(startDate)//
                .numberOfInstallments(numberOfInstallments);//

        final Response<PostLoansLoanIdTransactionsResponse> response = loanTransactionsApi
                .executeLoanTransaction(loanId, reAgingRequest, "reAge").execute();

        try (ResponseBody errorBody = response.errorBody()) {
            Assertions.assertNotNull(errorBody);
            assertThat(errorBody.string()).contains(expectedError);
        }

        ErrorHelper.checkFailedApiCall(response, 403);
    }

    @Then("Admin fails to create a Loan re-aging transaction with the following data because loan was charged-off:")
    public void reAgeChargedOffLoanFailure(final DataTable table) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse.body() != null;
        final long loanId = loanResponse.body().getLoanId();

        final List<String> data = table.asLists().get(1);

        final PostLoansLoanIdTransactionsRequest reAgingRequest = LoanRequestFactory//
                .defaultReAgingRequest()//
                .frequencyNumber(Integer.parseInt(data.get(0)))//
                .frequencyType(data.get(1))//
                .startDate(data.get(2))//
                .numberOfInstallments(Integer.parseInt(data.get(3)));//

        final Response<PostLoansLoanIdTransactionsResponse> response = loanTransactionsApi
                .executeLoanTransaction(loanId, reAgingRequest, "reAge").execute();
        testContext().set(TestContextKey.LOAN_REAGING_RESPONSE, response);
        final ErrorResponse errorDetails = ErrorResponse.from(response);
        final String developerMessage = errorDetails.getSingleError().getDeveloperMessage();

        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(developerMessage).matches(ErrorMessageHelper.reAgeChargedOffLoanFailure());
    }

    @Then("Admin fails to create a Loan re-aging transaction with the following data because loan was contract terminated:")
    public void reAgeContractTerminatedLoanFailure(final DataTable table) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse.body() != null;
        final long loanId = loanResponse.body().getLoanId();

        final List<String> data = table.asLists().get(1);

        final PostLoansLoanIdTransactionsRequest reAgingRequest = LoanRequestFactory//
                .defaultReAgingRequest()//
                .frequencyNumber(Integer.parseInt(data.get(0)))//
                .frequencyType(data.get(1))//
                .startDate(data.get(2))//
                .numberOfInstallments(Integer.parseInt(data.get(3)));//

        final Response<PostLoansLoanIdTransactionsResponse> response = loanTransactionsApi
                .executeLoanTransaction(loanId, reAgingRequest, "reAge").execute();
        testContext().set(TestContextKey.LOAN_REAGING_RESPONSE, response);
        final ErrorResponse errorDetails = ErrorResponse.from(response);
        final String developerMessage = errorDetails.getSingleError().getDeveloperMessage();

        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(developerMessage).matches(ErrorMessageHelper.reAgeContractTerminatedLoanFailure());
    }
}
