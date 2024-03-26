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

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.LoanReAgeEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
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

        List<String> data = table.asLists().get(1);
        int frequencyNumber = Integer.parseInt(data.get(0));
        String frequencyType = data.get(1);
        String startDate = data.get(2);
        int numberOfInstallments = Integer.parseInt(data.get(3));

        PostLoansLoanIdTransactionsRequest reAgingRequest = LoanRequestFactory.defaultReAgingRequest().frequencyNumber(frequencyNumber)
                .frequencyType(frequencyType).startDate(startDate).numberOfInstallments(numberOfInstallments);

        Response<PostLoansLoanIdTransactionsResponse> response = loanTransactionsApi.executeLoanTransaction(loanId, reAgingRequest, "reAge")
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        testContext().set(TestContextKey.LOAN_REAGING_RESPONSE, response);
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

        PostLoansLoanIdTransactionsRequest reAgingRequest = LoanRequestFactory.defaultReAgingRequest().frequencyNumber(frequencyNumber)
                .frequencyType(frequencyType).startDate(startDate).numberOfInstallments(numberOfInstallments);

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
}
