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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.assetexternalization.LoanAccountCustomSnapshotEvent;
import org.apache.fineract.test.messaging.event.loan.repayment.LoanRepaymentDueEvent;
import org.apache.fineract.test.messaging.event.loan.repayment.LoanRepaymentOverdueEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

public class InlineCOBStepDef extends AbstractStepDef {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private EventAssertion eventAssertion;

    @When("Admin runs inline COB job for Loan")
    public void runInlineCOB() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        InlineJobRequest inlineJobRequest = new InlineJobRequest().addLoanIdsItem(loanId);

        ok(() -> fineractClient.inlineJob().executeInlineJob("LOAN_COB", inlineJobRequest));
    }

    @Then("Loan Repayment Due Business Event is created")
    public void checkLoanRepaymentDueBusinessEventCreated() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        eventAssertion.assertEventRaised(LoanRepaymentDueEvent.class, loanId);
    }

    @Then("Loan Repayment Overdue Business Event is created")
    public void checkLoanRepaymentOverdueBusinessEventCreated() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        eventAssertion.assertEventRaised(LoanRepaymentOverdueEvent.class, loanId);
    }

    @Then("LoanAccountCustomSnapshotBusinessEvent is created with business date {string}")
    public void checkLoanRepaymentDueBusinessEventCreatedWithBusinessDate(String expectedBusinessDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        LocalDate expectedBusinessDateParsed = LocalDate.parse(expectedBusinessDate, FORMATTER);
        eventAssertion.assertEvent(LoanAccountCustomSnapshotEvent.class, loanId).isRaisedOnBusinessDate(expectedBusinessDateParsed);
    }
}
