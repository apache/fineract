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
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class LoanScheduleDueDatesStepDef extends AbstractStepDef {

    private final FineractFeignClient fineractClient;

    @When("Admin remembers the Loan repayment schedule due dates")
    public void rememberDueDates() {
        testContext().set(TestContextKey.LOAN_SCHEDULE_DUE_DATES, retrieveDueDates());
    }

    @Then("Loan repayment schedule due dates are the same as remembered")
    public void verifyDueDatesAreTheSame() {
        final List<String> expected = testContext().get(TestContextKey.LOAN_SCHEDULE_DUE_DATES);
        assertThat(expected).as("due dates have to be remembered before they are compared").isNotNull();
        assertThat(retrieveDueDates()).as("repayment schedule due dates compared to the remembered ones")
                .containsExactlyElementsOf(expected);
    }

    private List<String> retrieveDueDates() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final GetLoansLoanIdResponse loan = ok(() -> fineractClient.loans().retrieveOneLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "repaymentSchedule")));
        return loan.getRepaymentSchedule().getPeriods().stream().map(GetLoansLoanIdRepaymentPeriod::getDueDate).map(String::valueOf)
                .toList();
    }
}
