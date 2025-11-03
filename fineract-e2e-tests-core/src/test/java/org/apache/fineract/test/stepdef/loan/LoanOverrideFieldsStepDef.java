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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.test.data.loanproduct.DefaultLoanProduct;
import org.apache.fineract.test.data.loanproduct.LoanProductResolver;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import retrofit2.Response;

@RequiredArgsConstructor
public class LoanOverrideFieldsStepDef extends AbstractStepDef {

    private final LoanRequestFactory loanRequestFactory;
    private final LoanProductResolver loanProductResolver;
    private final LoansApi loansApi;

    @Then("LoanDetails has {string} field with value: {string}")
    public void checkLoanDetailsFieldWithValue(final String fieldName, final String expectedValue) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);
        assertNotNull(loanDetails.body());

        verifyFieldValue(loanDetails.body(), fieldName, expectedValue);
    }

    private void verifyFieldValue(final GetLoansLoanIdResponse loanDetails, final String fieldName, final String expectedValue) {
        final Integer actualValue = getIntFieldValue(loanDetails, fieldName);
        final Integer expected = Integer.valueOf(expectedValue);
        assertThat(actualValue).as("Expected %s to be %d but was %s", fieldName, expected, actualValue).isEqualTo(expected);
    }

    private Integer getIntFieldValue(final GetLoansLoanIdResponse loanDetails, final String fieldName) {
        return switch (fieldName) {
            case "inArrearsTolerance" -> loanDetails.getInArrearsTolerance();
            case "graceOnPrincipalPayment" -> loanDetails.getGraceOnPrincipalPayment();
            case "graceOnInterestPayment" -> loanDetails.getGraceOnInterestPayment();
            case "graceOnArrearsAgeing" -> loanDetails.getGraceOnArrearsAgeing();
            default -> throw new IllegalArgumentException("Unknown override field: " + fieldName);
        };
    }

    @When("Admin creates a new Loan with the following override data:")
    public void createLoanWithOverrideData(final DataTable dataTable) throws IOException {
        final Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        assertNotNull(clientResponse.body());
        final Long clientId = clientResponse.body().getClientId();

        final Map<String, String> overrideData = dataTable.asMap(String.class, String.class);

        final String loanProductName = overrideData.get("loanProduct");
        if (loanProductName == null) {
            throw new IllegalArgumentException("loanProduct is required in override data");
        }

        final PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)
                .productId(loanProductResolver.resolve(DefaultLoanProduct.valueOf(loanProductName))).numberOfRepayments(6)
                .loanTermFrequency(180).interestRatePerPeriod(new BigDecimal(1));

        overrideData.forEach((fieldName, value) -> {
            if (!"loanProduct".equals(fieldName)) {
                applyOverrideField(loansRequest, fieldName, value);
            }
        });

        final Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    private void applyOverrideField(final PostLoansRequest request, final String fieldName, final String value) {
        final boolean isNull = "null".equals(value);

        switch (fieldName) {
            case "inArrearsTolerance" -> request.inArrearsTolerance(isNull ? null : new BigDecimal(value));
            case "graceOnInterestPayment" -> request.graceOnInterestPayment(isNull ? null : Integer.valueOf(value));
            case "graceOnPrincipalPayment" -> request.graceOnPrincipalPayment(isNull ? null : Integer.valueOf(value));
            case "graceOnArrearsAgeing" -> request.graceOnArrearsAgeing(isNull ? null : Integer.valueOf(value));
            default -> throw new IllegalArgumentException("Unknown override field: " + fieldName);
        }
    }

}
