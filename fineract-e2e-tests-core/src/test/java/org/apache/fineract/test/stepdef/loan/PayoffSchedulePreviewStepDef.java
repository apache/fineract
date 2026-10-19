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
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FeignException;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostPayoffSchedulePreviewInstallment;
import org.apache.fineract.client.models.PostPayoffSchedulePreviewRequest;
import org.apache.fineract.client.models.PostPayoffSchedulePreviewResponse;
import org.apache.fineract.test.api.FineractClientConfiguration;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@Slf4j
@RequiredArgsConstructor
public class PayoffSchedulePreviewStepDef extends AbstractStepDef {

    private static final String MAX_PAYOFF_PERIODS_CONFIG = "payoff-schedule-preview-max-payoff-periods";
    private static final String MAX_TERM_PERIODS_CONFIG = "payoff-schedule-preview-max-term-periods";
    private static final Long DEFAULT_MAX_PERIODS = 1200L;

    private final FineractFeignClient fineractClient;
    private final FineractClientConfiguration fineractClientConfiguration;

    private PostPayoffSchedulePreviewResponse lastResponse;
    private CallFailedRuntimeException lastError;

    /**
     * Restores both preview limits after the scenario that lowers them, even when it fails mid-way, so later scenarios
     * are not limited by them.
     */
    @After("@PayoffSchedulePreviewMaxPeriodsScenario")
    public void restoreMaxPeriods() {
        fineractClient.defaultApi().updateInternalGlobalConfiguration(MAX_PAYOFF_PERIODS_CONFIG, DEFAULT_MAX_PERIODS);
        fineractClient.defaultApi().updateInternalGlobalConfiguration(MAX_TERM_PERIODS_CONFIG, DEFAULT_MAX_PERIODS);
    }

    @When("Admin computes a payoff schedule preview with the following parameters:")
    public void computePayoffSchedulePreview(final DataTable table) {
        final PostPayoffSchedulePreviewRequest request = buildRequest(table.asMaps().getFirst());
        lastResponse = ok(() -> fineractClient.payoffSchedulePreview().calculatePayoffSchedulePreview(request));
    }

    @When("Admin attempts to compute a payoff schedule preview with the following parameters:")
    public void attemptToComputePayoffSchedulePreview(final DataTable table) {
        final PostPayoffSchedulePreviewRequest request = buildRequest(table.asMaps().getFirst());
        lastError = fail(() -> fineractClient.payoffSchedulePreview().calculatePayoffSchedulePreview(request));
    }

    @When("Created user computes a payoff schedule preview with the following parameters:")
    public void computePayoffSchedulePreviewAsCreatedUser(final DataTable table) {
        final PostPayoffSchedulePreviewRequest request = buildRequest(table.asMaps().getFirst());
        lastResponse = ok(() -> createdUserClient().payoffSchedulePreview().calculatePayoffSchedulePreview(request));
    }

    @When("Created user attempts to compute a payoff schedule preview with the following parameters:")
    public void attemptToComputePayoffSchedulePreviewAsCreatedUser(final DataTable table) {
        final PostPayoffSchedulePreviewRequest request = buildRequest(table.asMaps().getFirst());
        lastError = fail(() -> createdUserClient().payoffSchedulePreview().calculatePayoffSchedulePreview(request));
    }

    @Then("Payoff schedule preview response has the following summary:")
    public void verifySummary(final DataTable table) {
        assertThat(lastResponse).as("Payoff schedule preview response").isNotNull();
        table.asMaps().getFirst().forEach((field, value) -> {
            switch (field) {
                case "currencyCode" -> assertThat(lastResponse.getCurrencyCode()).as(field).isEqualTo(value);
                case "amount" -> assertThat(lastResponse.getAmount()).as(field).isEqualByComparingTo(new BigDecimal(value));
                case "annualNominalInterestRate" ->
                    assertThat(lastResponse.getAnnualNominalInterestRate()).as(field).isEqualByComparingTo(new BigDecimal(value));
                case "interestMethod" -> assertThat(lastResponse.getInterestMethod()).as(field).isEqualTo(value);
                case "term" -> assertThat(lastResponse.getTerm()).as(field).isEqualTo(nullableValue(value));
                case "frequency" -> assertThat(lastResponse.getFrequency()).as(field).isEqualTo(value);
                case "startDate" -> assertThat(lastResponse.getStartDate()).as(field).isEqualTo(value);
                case "numberOfInstallments" ->
                    assertThat(lastResponse.getNumberOfInstallments()).as(field).isEqualTo(Integer.valueOf(value));
                case "totalPrincipal" -> assertThat(lastResponse.getTotalPrincipal()).as(field).isEqualByComparingTo(new BigDecimal(value));
                case "totalInterest" -> assertThat(lastResponse.getTotalInterest()).as(field).isEqualByComparingTo(new BigDecimal(value));
                case "totalRepayment" -> assertThat(lastResponse.getTotalRepayment()).as(field).isEqualByComparingTo(new BigDecimal(value));
                default -> throw new IllegalArgumentException("Unsupported payoff schedule preview summary field: " + field);
            }
        });
    }

    @Then("Payoff schedule preview installment {int} has the following data:")
    public void verifyInstallment(final int installmentNumber, final DataTable table) {
        assertThat(lastResponse).as("Payoff schedule preview response").isNotNull();
        final PostPayoffSchedulePreviewInstallment installment = lastResponse.getInstallments().stream()
                .filter(candidate -> candidate.getInstallmentNumber().equals(installmentNumber)).findFirst()
                .orElseThrow(() -> new AssertionError("No installment found with installmentNumber " + installmentNumber));

        table.asMaps().getFirst().forEach((field, value) -> {
            switch (field) {
                case "fromDate" -> assertThat(installment.getFromDate()).as(field).isEqualTo(value);
                case "dueDate" -> assertThat(installment.getDueDate()).as(field).isEqualTo(value);
                case "principalDue" -> assertThat(installment.getPrincipalDue()).as(field).isEqualByComparingTo(new BigDecimal(value));
                case "interestDue" -> assertThat(installment.getInterestDue()).as(field).isEqualByComparingTo(new BigDecimal(value));
                case "totalDue" -> assertThat(installment.getTotalDue()).as(field).isEqualByComparingTo(new BigDecimal(value));
                case "outstandingPrincipalBalance" ->
                    assertThat(installment.getOutstandingPrincipalBalance()).as(field).isEqualByComparingTo(new BigDecimal(value));
                default -> throw new IllegalArgumentException("Unsupported payoff schedule preview installment field: " + field);
            }
        });
    }

    @Then("Computing the payoff schedule preview fails with the following validation errors:")
    public void verifyValidationErrors(final DataTable table) {
        assertThat(lastError).as("Expected the payoff schedule preview call to fail").isNotNull();
        assertThat(lastError.getStatus()).as("HTTP status code").isEqualTo(400);

        final ErrorResponse errorResponse = ErrorResponse.fromFeignException((FeignException) lastError.getCause());
        final List<String> actual = errorResponse.getErrors().stream()
                .map(error -> error.getParameterName() + " " + error.getUserMessageGlobalisationCode()).toList();
        final List<String> expected = table.asMaps().stream().map(row -> row.get("parameterName") + " " + row.get("code")).toList();
        assertThat(actual).as("Validation errors").containsExactlyElementsOf(expected);
    }

    @Then("Computing the payoff schedule preview is rejected with HTTP {int} and error code {string}")
    public void verifyRejectedWithStatusAndCode(final int status, final String code) {
        assertThat(lastError).as("Expected the payoff schedule preview call to fail").isNotNull();
        assertThat(lastError.getStatus()).as("HTTP status code").isEqualTo(status);

        final ErrorResponse errorResponse = ErrorResponse.fromFeignException((FeignException) lastError.getCause());
        assertThat(errorResponse.getErrors()).extracting(ErrorResponse.ErrorDetail::getUserMessageGlobalisationCode).as("Error codes")
                .containsExactly(code);
    }

    /**
     * Client authenticated as the user created by {@code Admin creates new user with ... given permissions}.
     */
    private FineractFeignClient createdUserClient() {
        final String username = testContext().get(TestContextKey.CREATED_SIMPLE_USER_USERNAME);
        final String password = testContext().get(TestContextKey.CREATED_SIMPLE_USER_PASSWORD);
        return fineractClientConfiguration.fineractFeignClientForUser(username, password);
    }

    private static String nullableValue(final String value) {
        return "null".equals(value) ? null : value;
    }

    private static BigDecimal nullableDecimal(final String value) {
        return "null".equals(value) ? null : new BigDecimal(value);
    }

    private static PostPayoffSchedulePreviewRequest buildRequest(final Map<String, String> params) {
        final PostPayoffSchedulePreviewRequest request = new PostPayoffSchedulePreviewRequest();
        params.forEach((key, value) -> {
            final String resolvedValue = nullableValue(value);
            switch (key) {
                case "amount" -> request.setAmount(nullableDecimal(value));
                case "term" -> request.setTerm(resolvedValue);
                case "fixedPaymentAmount" -> request.setFixedPaymentAmount(nullableDecimal(value));
                case "frequency" -> request.setFrequency(resolvedValue);
                case "annualNominalInterestRate" -> request.setAnnualNominalInterestRate(nullableDecimal(value));
                case "interestMethod" -> request.setInterestMethod(resolvedValue);
                case "currencyCode" -> request.setCurrencyCode(resolvedValue);
                case "decimalPlaces" -> request.setDecimalPlaces(resolvedValue == null ? null : Integer.valueOf(resolvedValue));
                case "startDate" -> request.setStartDate(resolvedValue);
                case "dateFormat" -> request.setDateFormat(resolvedValue);
                case "locale" -> request.setLocale(resolvedValue);
                case "daysInYearType" -> request.setDaysInYearType(resolvedValue == null ? null : Integer.valueOf(resolvedValue));
                case "daysInMonthType" -> request.setDaysInMonthType(resolvedValue == null ? null : Integer.valueOf(resolvedValue));
                default -> throw new IllegalArgumentException("Unsupported payoff schedule preview request field: " + key);
            }
        });
        return request;
    }
}
