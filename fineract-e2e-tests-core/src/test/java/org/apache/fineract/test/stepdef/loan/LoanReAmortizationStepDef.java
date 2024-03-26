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

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.LoanReAmortizeEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class LoanReAmortizationStepDef extends AbstractStepDef {

    @Autowired
    private LoanTransactionsApi loanTransactionsApi;

    @Autowired
    private EventAssertion eventAssertion;

    @When("When Admin creates a Loan re-amortization transaction on current business date")
    public void createLoanReAmortization() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansLoanIdTransactionsRequest reAmortizationRequest = LoanRequestFactory.defaultLoanReAmortizationRequest();

        Response<PostLoansLoanIdTransactionsResponse> response = loanTransactionsApi
                .executeLoanTransaction(loanId, reAmortizationRequest, "reAmortize").execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.LOAN_REAMORTIZATION_RESPONSE, response);
    }

    @When("When Admin creates a Loan re-amortization transaction on current business date by loan external ID")
    public void createLoanReAmortizationByLoanExternalId() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.body().getResourceExternalId();

        PostLoansLoanIdTransactionsRequest reAmortizationRequest = LoanRequestFactory.defaultLoanReAmortizationRequest();

        Response<PostLoansLoanIdTransactionsResponse> response = loanTransactionsApi
                .executeLoanTransaction1(loanExternalId, reAmortizationRequest, "reAmortize").execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.LOAN_REAMORTIZATION_RESPONSE, response);
    }

    @When("When Admin undo Loan re-amortization transaction on current business date")
    public void undoLoanReAmortization() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdTransactionsResponse> response = loanTransactionsApi
                .executeLoanTransaction(loanId, new PostLoansLoanIdTransactionsRequest(), "undoReAmortize").execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.LOAN_REAMORTIZATION_UNDO_RESPONSE, response);
    }

    @Then("LoanReAmortizeBusinessEvent is created")
    public void checkLoanReAmortizeBusinessEventCreated() {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        eventAssertion.assertEventRaised(LoanReAmortizeEvent.class, loanId);
    }
}
