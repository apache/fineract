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
package org.apache.fineract.test.stepdef.common;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ExternalEventConfigurationItemResponse;
import org.apache.fineract.client.models.ExternalEventConfigurationUpdateRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanAdjustTransactionBusinessEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class ExternalEventConfigurationStepDef extends AbstractStepDef {

    private final FineractFeignClient fineractClient;
    private final EventAssertion eventAssertion;

    private Map<String, Boolean> savedExternalEventConfiguration;

    @Before("@ExternalEventConfigurationScenario")
    public void saveExternalEventConfiguration() {
        savedExternalEventConfiguration = ok(() -> fineractClient.externalEventConfiguration().getExternalEventConfigurations(Map.of()))
                .getExternalEventConfiguration().stream().collect(Collectors.toMap(ExternalEventConfigurationItemResponse::getType,
                        ExternalEventConfigurationItemResponse::getEnabled));
    }

    // the configuration is tenant-wide: restore it even when a scenario fails mid-way, so a disabled event cannot leak
    // into other scenarios
    @After("@ExternalEventConfigurationScenario")
    public void restoreExternalEventConfiguration() {
        updateExternalEventConfiguration(savedExternalEventConfiguration);
    }

    @When("Admin disables external business event {string}")
    public void disableExternalBusinessEvent(final String eventType) {
        updateExternalEventConfiguration(Map.of(eventType, false));
    }

    @When("Admin enables external business event {string}")
    public void enableExternalBusinessEvent(final String eventType) {
        updateExternalEventConfiguration(Map.of(eventType, true));
    }

    // Matched by loan and transaction type, not by transaction id: the API no longer returns a transaction reversed by
    // a
    // replay. The loan id is resolved before the assertion because the test context is bound to the scenario thread.
    @Then("LoanAdjustTransactionBusinessEvent is not raised for any {string} transaction of the loan")
    public void loanAdjustTransactionEventIsNotRaisedForTransactionType(final String transactionType) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();
        eventAssertion.assertEventNotRaised(LoanAdjustTransactionBusinessEvent.class,
                em -> loanId.equals(em.getData().getTransactionToAdjust().getLoanId())
                        && transactionType.equals(em.getData().getTransactionToAdjust().getType().getValue()));
    }

    @Then("a Working Capital Loan Adjust Transaction business event is not raised for any {string} transaction")
    public void workingCapitalLoanAdjustTransactionEventIsNotRaisedForTransactionType(final String transactionType) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();
        final String expectedCode = "loanTransactionType." + transactionType;
        eventAssertion.assertEventNotRaised(WorkingCapitalLoanAdjustTransactionBusinessEvent.class,
                em -> loanId.equals(em.getData().getTransactionToAdjust().getWcLoanId())
                        && expectedCode.equals(em.getData().getTransactionToAdjust().getType().getCode()));
    }

    private void updateExternalEventConfiguration(final Map<String, Boolean> configurations) {
        executeVoid(() -> fineractClient.externalEventConfiguration().updateExternalEventConfigurations(
                new ExternalEventConfigurationUpdateRequest().externalEventConfigurations(configurations), Map.of()));
    }
}
