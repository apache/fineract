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
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanCapitalizedIncomeAmortizationTransactionCreatedEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class LoanCapitalizedIncomeStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    @Autowired
    LoansApi loansApi;

    @Autowired
    EventAssertion eventAssertion;

    @Then("Loan Capitalized Income Amortization Transaction Created Business Event is created on {string}")
    public void checkLoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEventCreated(String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions capitalizedIncomeAmortizationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Capitalized Income Amortization".equals(t.getType().getValue()))
                .reduce((first, second) -> second).orElseThrow(
                        () -> new IllegalStateException(String.format("No Capitalized Income Amortization transaction found on %s", date)));
        Long capitalizedIncomeAmortizationTransactionId = capitalizedIncomeAmortizationTransaction.getId();

        eventAssertion.assertEventRaised(LoanCapitalizedIncomeAmortizationTransactionCreatedEvent.class,
                capitalizedIncomeAmortizationTransactionId);
    }

}
