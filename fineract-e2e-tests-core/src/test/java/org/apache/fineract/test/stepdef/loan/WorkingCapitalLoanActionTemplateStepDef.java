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

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanCommandTemplateData;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.assertj.core.api.SoftAssertions;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalLoanActionTemplateStepDef extends AbstractStepDef {

    private final FineractFeignClient fineractClient;

    @When("Admin retrieves the working capital loan action template with templateType {string}")
    public void retrieveWcLoanActionTemplate(final String templateType) {
        final Long loanId = getCreatedLoanId();

        final WorkingCapitalLoanCommandTemplateData response = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanActionTemplate(loanId, templateType));
        testContext().set(TestContextKey.WC_LOAN_ACTION_TEMPLATE_RESPONSE, response);
        log.info("Retrieved WC loan action template for loan ID: {} with templateType: {}", loanId, templateType);
    }

    @Then("The working capital loan approve template has the following data:")
    public void verifyApproveTemplateData(final DataTable table) {
        verifyTemplateData(table);
    }

    @Then("The working capital loan disburse template has the following data:")
    public void verifyDisburseTemplateData(final DataTable table) {
        verifyTemplateData(table);
    }

    @Then("Retrieving WC loan action template with invalid templateType {string} results in an error")
    public void retrieveTemplateWithInvalidType(final String templateType) {
        final Long loanId = getCreatedLoanId();

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanActionTemplate(loanId, templateType));

        assertThat(exception.getStatus()).as("HTTP status code").isEqualTo(400);
        assertThat(exception.getMessage()).as("Error message should reference the invalid command").contains(templateType);
        log.info("Verified WC loan action template retrieval failed with invalid templateType: {}", templateType);
    }

    @Then("Retrieving WC loan action template without templateType results in an error")
    public void retrieveTemplateWithoutType() {
        final Long loanId = getCreatedLoanId();

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanActionTemplate(loanId, (String) null));

        assertThat(exception.getStatus()).as("HTTP status code").isEqualTo(400);
        assertThat(exception.getMessage()).as("Error message should reference unrecognized command").contains("command");
        log.info("Verified WC loan action template retrieval failed without templateType");
    }

    @Then("Retrieving WC loan action template for non-existent loan id {long} results in a 404 error")
    public void retrieveTemplateForNonExistentLoan(final Long loanId) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanActionTemplate(loanId, "approve"));

        assertThat(exception.getStatus()).as("HTTP status code").isEqualTo(404);
        assertThat(exception.getMessage()).as("Error message should indicate loan not found").contains("does not exist");
        log.info("Verified WC loan action template retrieval failed for non-existent loan ID: {}", loanId);
    }

    private void verifyTemplateData(final DataTable table) {
        final WorkingCapitalLoanCommandTemplateData response = testContext().get(TestContextKey.WC_LOAN_ACTION_TEMPLATE_RESPONSE);
        assertThat(response).as("Template response should not be null").isNotNull();

        final Map<String, String> expected = table.asMaps().get(0);

        SoftAssertions.assertSoftly(softly -> expected.forEach((field, value) -> {
            switch (field) {
                case "approvalAmount" ->
                    softly.assertThat(response.getApprovalAmount()).as(field).isNotNull().isEqualByComparingTo(new BigDecimal(value));
                case "approvalDate" ->
                    softly.assertThat(response.getApprovalDate()).as(field).isNotNull().isEqualTo(LocalDate.parse(value));
                case "expectedDisbursementDate" ->
                    softly.assertThat(response.getExpectedDisbursementDate()).as(field).isNotNull().isEqualTo(LocalDate.parse(value));
                case "expectedAmount" -> {
                    if ("null".equals(value)) {
                        softly.assertThat(response.getExpectedAmount()).as(field).isNull();
                    } else {
                        softly.assertThat(response.getExpectedAmount()).as(field).isNotNull().isEqualByComparingTo(new BigDecimal(value));
                    }
                }
                case "paymentTypeOptionsPresent" -> {
                    if (Boolean.parseBoolean(value)) {
                        softly.assertThat(response.getPaymentTypeOptions()).as(field).isNotNull().isNotEmpty();
                    } else {
                        softly.assertThat(response.getPaymentTypeOptions()).as(field).isNullOrEmpty();
                    }
                }
                default -> softly.fail("Unknown template field in DataTable: " + field);
            }
        }));

        log.info("Verified WC loan action template data");
    }

    private Long getCreatedLoanId() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertThat(loanResponse).as("No loan create response in context — did a previous loan creation step run?").isNotNull();
        return loanResponse.getLoanId();
    }
}
