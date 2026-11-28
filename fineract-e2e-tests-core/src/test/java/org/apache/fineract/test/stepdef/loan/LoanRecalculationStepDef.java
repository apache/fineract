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

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class LoanRecalculationStepDef extends AbstractStepDef {

    private static final String RECALCULATE_COMMAND = "recalculate";

    private final FineractFeignClient fineractClient;
    private final EventCheckHelper eventCheckHelper;

    @When("Admin runs loan recalculation for Loan")
    public void runLoanRecalculation() {
        final long loanId = createdLoanId();
        ok(() -> fineractClient.loans().handleCommandsLoan(loanId, new PostLoansLoanIdRequest(), Map.of("command", RECALCULATE_COMMAND)));
    }

    @Then("Admin fails to run loan recalculation for Loan with status code {int} and error {string}")
    public void runLoanRecalculationFails(final int statusCode, final String expectedError) {
        final long loanId = createdLoanId();
        final CallFailedRuntimeException exception = fail(() -> fineractClient.loans().handleCommandsLoan(loanId,
                new PostLoansLoanIdRequest(), Map.of("command", RECALCULATE_COMMAND)));
        assertThat(exception.getStatus()).isEqualTo(statusCode);
        assertThat(exception.getResponseBody()).contains(expectedError);
    }

    @Then("LoanBalanceChangedBusinessEvent is raised for the Loan")
    public void loanBalanceChangedEventRaised() {
        eventCheckHelper.loanBalanceChangedEventCheck(createdLoanId());
    }

    @When("Admin takes a snapshot of the Loan repayment schedule and transactions")
    public void takeLoanSnapshot() {
        testContext().set(TestContextKey.LOAN_RECALCULATION_SNAPSHOT, loanSnapshot(createdLoanId()));
    }

    @Then("Loan repayment schedule and transactions are the same as in the snapshot")
    public void loanMatchesSnapshot() {
        final List<String> expected = testContext().get(TestContextKey.LOAN_RECALCULATION_SNAPSHOT);
        assertThat(expected).as("a snapshot has to be taken before it is compared").isNotNull();
        assertThat(loanSnapshot(createdLoanId())).as("repayment schedule and transactions compared to the snapshot")
                .containsExactlyElementsOf(expected);
    }

    @Then("Loan is NPA")
    public void loanIsNpa() {
        assertThat(retrieveLoan(createdLoanId()).getIsNPA()).as("loan is a non performing asset").isTrue();
    }

    private long createdLoanId() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return loanResponse.getLoanId();
    }

    private GetLoansLoanIdResponse retrieveLoan(final long loanId) {
        return ok(() -> fineractClient.loans().retrieveOneLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "repaymentSchedule,transactions")));
    }

    private List<String> loanSnapshot(final long loanId) {
        final GetLoansLoanIdResponse loan = retrieveLoan(loanId);
        final List<String> snapshot = new ArrayList<>();
        snapshot.add("status=" + loan.getStatus().getCode());
        for (final GetLoansLoanIdRepaymentPeriod period : loan.getRepaymentSchedule().getPeriods()) {
            snapshot.add(String.join("|", "period", String.valueOf(period.getPeriod()), String.valueOf(period.getDueDate()),
                    String.valueOf(period.getPrincipalDue()), String.valueOf(period.getInterestDue()),
                    String.valueOf(period.getFeeChargesDue()), String.valueOf(period.getPenaltyChargesDue()),
                    String.valueOf(period.getTotalDueForPeriod()), String.valueOf(period.getTotalPaidForPeriod()),
                    String.valueOf(period.getTotalOutstandingForPeriod()), String.valueOf(period.getObligationsMetOnDate())));
        }
        for (final GetLoansLoanIdTransactions transaction : loan.getTransactions()) {
            snapshot.add(String.join("|", "transaction", String.valueOf(transaction.getId()), String.valueOf(transaction.getDate()),
                    String.valueOf(transaction.getType().getValue()), String.valueOf(transaction.getAmount()),
                    String.valueOf(transaction.getPrincipalPortion()), String.valueOf(transaction.getInterestPortion()),
                    String.valueOf(transaction.getFeeChargesPortion()), String.valueOf(transaction.getPenaltyChargesPortion()),
                    String.valueOf(transaction.getOutstandingLoanBalance()), String.valueOf(transaction.getManuallyReversed())));
        }
        return snapshot;
    }
}
