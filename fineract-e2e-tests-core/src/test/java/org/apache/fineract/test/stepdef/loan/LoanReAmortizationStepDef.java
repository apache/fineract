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

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.LoanReAmortizeEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

public class LoanReAmortizationStepDef extends AbstractStepDef {

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private EventAssertion eventAssertion;

    @When("When Admin creates a Loan re-amortization transaction on current business date")
    public void createLoanReAmortization() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsRequest reAmortizationRequest = LoanRequestFactory.defaultLoanReAmortizationRequest();

        PostLoansLoanIdTransactionsResponse response = ok(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId,
                reAmortizationRequest, Map.<String, Object>of("command", "reAmortize")));
        testContext().set(TestContextKey.LOAN_REAMORTIZATION_RESPONSE, response);
    }

    @When("When Admin creates a Loan re-amortization transaction on current business date by loan external ID")
    public void createLoanReAmortizationByLoanExternalId() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        PostLoansLoanIdTransactionsRequest reAmortizationRequest = LoanRequestFactory.defaultLoanReAmortizationRequest();

        PostLoansLoanIdTransactionsResponse response = ok(() -> fineractClient.loanTransactions().executeLoanTransaction1(loanExternalId,
                reAmortizationRequest, Map.<String, Object>of("command", "reAmortize")));
        testContext().set(TestContextKey.LOAN_REAMORTIZATION_RESPONSE, response);
    }

    @When("When Admin undo Loan re-amortization transaction on current business date")
    public void undoLoanReAmortization() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsResponse response = ok(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId,
                new PostLoansLoanIdTransactionsRequest(), Map.<String, Object>of("command", "undoReAmortize")));
        testContext().set(TestContextKey.LOAN_REAMORTIZATION_UNDO_RESPONSE, response);
    }

    @Then("LoanReAmortizeBusinessEvent is created")
    public void checkLoanReAmortizeBusinessEventCreated() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        eventAssertion.assertEventRaised(LoanReAmortizeEvent.class, loanId);
    }
}
