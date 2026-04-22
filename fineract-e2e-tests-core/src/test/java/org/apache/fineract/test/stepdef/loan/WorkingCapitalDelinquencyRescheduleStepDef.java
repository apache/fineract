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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DelinquencyBucketRequest;
import org.apache.fineract.client.models.MinimumPaymentPeriodAndRule;
import org.apache.fineract.client.models.PostAllowAttributeOverrides;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyActionData;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.test.factory.WorkingCapitalRequestFactory;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalDelinquencyRescheduleStepDef extends AbstractStepDef {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private final FineractFeignClient fineractFeignClient;

    private final WorkingCapitalRequestFactory workingCapitalRequestFactory;

    @When("Admin creates a new Working Capital Loan Product with delinquency bucket")
    public void createProductWithDelinquencyBucket() {
        final Long bucketId = TestContext.GLOBAL.get(TestContextKey.DELINQUENCY_BUCKET_ID);
        assertThat(bucketId).isNotNull();

        final PostWorkingCapitalLoanProductsRequest request = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest()
                .name("WCLP-DLQ-" + Utils.randomStringGenerator(8)).delinquencyBucketId(bucketId)
                .allowAttributeOverrides(new PostAllowAttributeOverrides().discountDefault(true));
        final PostWorkingCapitalLoanProductsResponse response = ok(
                () -> fineractFeignClient.workingCapitalLoanProducts().createWorkingCapitalLoanProduct(request));
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST, request);
        log.info("Created WC product id={} with delinquency bucket id={}", response.getResourceId(), bucketId);
    }

    @When("Admin creates WC Delinquency Bucket with frequency {int} {word} and minimumPayment {int} {word}")
    public void createWcDelinquencyBucket(final int frequency, final String frequencyType, final int minimumPayment,
            final String minimumPaymentType) {
        final DelinquencyBucketRequest request = new DelinquencyBucketRequest().name("DB-WCL-" + Utils.randomStringGenerator(12))
                .bucketType("WORKING_CAPITAL").ranges(List.of(1L))
                .minimumPaymentPeriodAndRule(new MinimumPaymentPeriodAndRule().frequency(frequency).frequencyType(frequencyType)
                        .minimumPayment(new BigDecimal(minimumPayment)).minimumPaymentType(minimumPaymentType));

        final PostDelinquencyBucketResponse result = ok(
                () -> fineractFeignClient.delinquencyRangeAndBucketsManagement().createBucket(request));
        assertThat(result).isNotNull();
        assertThat(result.getResourceId()).isNotNull();
        TestContext.GLOBAL.set(TestContextKey.DELINQUENCY_BUCKET_ID, result.getResourceId());
        log.info("Created WC delinquency bucket id={} with frequency={} {} minimumPayment={} {}", result.getResourceId(), frequency,
                frequencyType, minimumPayment, minimumPaymentType);
    }

    @When("Admin creates WC delinquency reschedule action with the following parameters:")
    public void createRescheduleAction(final DataTable table) {
        final Map<String, String> params = table.asMaps().get(0);
        final PostWorkingCapitalLoansDelinquencyActionRequest request = new PostWorkingCapitalLoansDelinquencyActionRequest();
        request.setAction("reschedule");
        request.setLocale("en");
        Optional.ofNullable(params.get("minimumPayment")).ifPresent(v -> request.setMinimumPayment(new BigDecimal(v)));
        Optional.ofNullable(params.get("minimumPaymentType")).ifPresent(request::setMinimumPaymentType);
        Optional.ofNullable(params.get("frequency")).ifPresent(v -> request.setFrequency(Integer.parseInt(v)));
        Optional.ofNullable(params.get("frequencyType")).ifPresent(request::setFrequencyType);
        executeRescheduleAction(request);
    }

    @Then("Admin fails to create WC delinquency reschedule action with minimumPayment {int} {word} and frequency {int} {word}")
    public void failToCreateRescheduleAction(final int minimumPayment, final String minimumPaymentType, final int frequency,
            final String frequencyType) {
        final Long loanId = getLoanId();
        final PostWorkingCapitalLoansDelinquencyActionRequest request = buildRescheduleRequest(new BigDecimal(minimumPayment),
                minimumPaymentType, frequency, frequencyType);
        log.info("Attempting to create RESCHEDULE action for WC loan {} (expecting failure): minimumPayment={} {}, frequency={} {}", loanId,
                minimumPayment, minimumPaymentType, frequency, frequencyType);

        fail(() -> fineractFeignClient.workingCapitalLoanDelinquencyActions().createDelinquencyAction(loanId, request));
    }

    @Then("Admin fails to create WC delinquency reschedule action with minimumPayment {int} {word} and frequency {int} {word} with error containing {string}")
    public void failToCreateRescheduleActionWithMessage(final int minimumPayment, final String minimumPaymentType, final int frequency,
            final String frequencyType, final String expectedMessage) {
        final Long loanId = getLoanId();
        final PostWorkingCapitalLoansDelinquencyActionRequest request = buildRescheduleRequest(new BigDecimal(minimumPayment),
                minimumPaymentType, frequency, frequencyType);
        log.info(
                "Attempting to create RESCHEDULE action for WC loan {} (expecting HTTP 400 and message '{}'): minimumPayment={} {}, frequency={} {}",
                loanId, expectedMessage, minimumPayment, minimumPaymentType, frequency, frequencyType);

        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalLoanDelinquencyActions().createDelinquencyAction(loanId, request));
        assertThat(exception.getStatus()).as("HTTP status code").isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).as("Developer message").contains(expectedMessage);
    }

    @Then("Admin fails to create WC delinquency reschedule action with error containing {string} and the following parameters:")
    public void failToCreateRescheduleActionWithTableAndMessage(final String expectedMessage, final DataTable table) {
        final Long loanId = getLoanId();
        final Map<String, String> params = table.asMaps().get(0);
        final PostWorkingCapitalLoansDelinquencyActionRequest request = new PostWorkingCapitalLoansDelinquencyActionRequest();
        request.setAction("reschedule");
        request.setLocale("en");
        Optional.ofNullable(params.get("minimumPayment")).ifPresent(v -> request.setMinimumPayment(new BigDecimal(v)));
        Optional.ofNullable(params.get("minimumPaymentType")).ifPresent(request::setMinimumPaymentType);
        Optional.ofNullable(params.get("frequency")).ifPresent(v -> request.setFrequency(Integer.parseInt(v)));
        Optional.ofNullable(params.get("frequencyType")).ifPresent(request::setFrequencyType);
        log.info("Attempting to create RESCHEDULE action for WC loan {} (expecting HTTP 400 and message '{}'): {}", loanId, expectedMessage,
                params);

        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalLoanDelinquencyActions().createDelinquencyAction(loanId, request));
        assertThat(exception.getStatus()).as("HTTP status code").isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).as("Developer message").contains(expectedMessage);
    }

    @Then("Admin fails to create WC delinquency reschedule action with no parameters with error containing {string}")
    public void failToCreateEmptyRescheduleAction(final String expectedMessage) {
        final Long loanId = getLoanId();
        final PostWorkingCapitalLoansDelinquencyActionRequest request = new PostWorkingCapitalLoansDelinquencyActionRequest();
        request.setAction("reschedule");
        request.setLocale("en");
        log.info("Attempting to create empty RESCHEDULE action for WC loan {} (expecting HTTP 400 and message '{}')", loanId,
                expectedMessage);

        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalLoanDelinquencyActions().createDelinquencyAction(loanId, request));
        assertThat(exception.getStatus()).as("HTTP status code").isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).as("Developer message").contains(expectedMessage);
    }

    @Then("WC loan delinquency range schedule has the following periods:")
    public void verifyPeriods(final DataTable table) {
        final Long loanId = getLoanId();
        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periods = ok(
                () -> fineractFeignClient.workingCapitalLoanDelinquencyRangeSchedule().retrieveDelinquencyRangeSchedule(loanId));

        final List<Map<String, String>> expectedRows = table.asMaps();
        assertThat(periods).as("Number of periods").hasSize(expectedRows.size());

        for (int i = 0; i < expectedRows.size(); i++) {
            final WorkingCapitalLoanDelinquencyRangeScheduleData actual = periods.get(i);
            final int periodNumber = i + 1;
            expectedRows.get(i).forEach((field, value) -> verifyFullScheduleField(actual, field, value, periodNumber));
        }
    }

    @Then("WC loan delinquency actions contain {int} action(s)")
    public void verifyActionCount(final int count) {
        final Long loanId = getLoanId();
        assertThat(retrieveDelinquencyActions(loanId)).hasSize(count);
    }

    @Then("WC loan has both PAUSE and RESCHEDULE delinquency actions")
    public void verifyBothPauseAndRescheduleActions() {
        final Long loanId = getLoanId();
        final List<WorkingCapitalLoanDelinquencyActionData> actions = retrieveDelinquencyActions(loanId);
        assertThat(actions.stream().map(a -> a.getAction().name()).toList()).as("Should contain both PAUSE and RESCHEDULE")
                .contains("PAUSE", "RESCHEDULE");
    }

    @Then("WC loan last delinquency action has the following data:")
    public void verifyLastActionContent(final DataTable table) {
        final Long loanId = getLoanId();
        final List<WorkingCapitalLoanDelinquencyActionData> actions = retrieveDelinquencyActions(loanId);
        assertThat(actions).as("Actions should not be empty").isNotEmpty();

        final WorkingCapitalLoanDelinquencyActionData last = actions.get(actions.size() - 1);
        final List<Map<String, String>> rows = table.asMaps();
        assertThat(rows).as("Expected exactly 1 data row").hasSize(1);
        rows.get(0).forEach((field, value) -> verifyActionField(last, field, value));
    }

    @Then("WC loan delinquency range schedule periods have specific data:")
    public void verifySpecificPeriods(final DataTable table) {
        final Long loanId = getLoanId();
        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periods = ok(
                () -> fineractFeignClient.workingCapitalLoanDelinquencyRangeSchedule().retrieveDelinquencyRangeSchedule(loanId));

        for (final Map<String, String> expected : table.asMaps()) {
            final int periodNumber = Integer.parseInt(expected.get("periodNumber"));
            final WorkingCapitalLoanDelinquencyRangeScheduleData actual = periods.stream()
                    .filter(p -> p.getPeriodNumber().equals(periodNumber)).findFirst().orElse(null);
            assertThat(actual).as("Period %d should exist", periodNumber).isNotNull();
            expected.forEach((field, value) -> verifyFullScheduleField(actual, field, value, periodNumber));
        }
    }

    private void executeRescheduleAction(final PostWorkingCapitalLoansDelinquencyActionRequest request) {
        final Long loanId = getLoanId();
        log.info("Creating RESCHEDULE action for WC loan {}: {}", loanId, request);

        final PostWorkingCapitalLoansDelinquencyActionResponse result = ok(
                () -> fineractFeignClient.workingCapitalLoanDelinquencyActions().createDelinquencyAction(loanId, request));
        assertThat(result).isNotNull();
        assertThat(result.getResourceId()).isNotNull();
        log.info("RESCHEDULE action created with id={}", result.getResourceId());
    }

    private List<WorkingCapitalLoanDelinquencyActionData> retrieveDelinquencyActions(final Long loanId) {
        return ok(() -> fineractFeignClient.workingCapitalLoanDelinquencyActions().retrieveDelinquencyActions(loanId));
    }

    private void verifyActionField(final WorkingCapitalLoanDelinquencyActionData actual, final String field, final String expected) {
        switch (field) {
            case "action" -> assertThat(actual.getAction().name()).as("action").isEqualTo(expected);
            case "startDate" -> assertThat(actual.getStartDate()).as("startDate").isEqualTo(LocalDate.parse(expected, DATE_FORMAT));
            case "endDate" ->
                verifyOptionalField(expected, v -> assertThat(actual.getEndDate()).as("endDate").isEqualTo(LocalDate.parse(v, DATE_FORMAT)),
                        () -> assertThat(actual.getEndDate()).as("endDate").isNull());
            case "minimumPayment" ->
                assertThat(actual.getMinimumPayment()).as("minimumPayment").isEqualByComparingTo(new BigDecimal(expected));
            case "minimumPaymentType" ->
                verifyOptionalField(expected, v -> assertThat(actual.getMinimumPaymentType().name()).as("minimumPaymentType").isEqualTo(v),
                        () -> assertThat(actual.getMinimumPaymentType()).as("minimumPaymentType").isNull());
            case "frequency" -> assertThat(actual.getFrequency()).as("frequency").isEqualTo(Integer.parseInt(expected));
            case "frequencyType" -> assertThat(actual.getFrequencyType().name()).as("frequencyType").isEqualTo(expected);
            default -> throw new IllegalArgumentException("Unknown action field: " + field);
        }
    }

    private void verifyFullScheduleField(final WorkingCapitalLoanDelinquencyRangeScheduleData actual, final String field,
            final String expected, final int periodNumber) {
        final String label = "Period " + periodNumber + " " + field;
        switch (field) {
            case "periodNumber" -> assertThat(actual.getPeriodNumber()).as(label).isEqualTo(Integer.parseInt(expected));
            case "fromDate" -> assertThat(actual.getFromDate()).as(label).isEqualTo(LocalDate.parse(expected, DATE_FORMAT));
            case "toDate" -> assertThat(actual.getToDate()).as(label).isEqualTo(LocalDate.parse(expected, DATE_FORMAT));
            case "expectedAmount" -> assertThat(actual.getExpectedAmount()).as(label).isEqualByComparingTo(new BigDecimal(expected));
            case "paidAmount" -> assertThat(actual.getPaidAmount()).as(label).isEqualByComparingTo(new BigDecimal(expected));
            case "outstandingAmount" -> assertThat(actual.getOutstandingAmount()).as(label).isEqualByComparingTo(new BigDecimal(expected));
            case "minPaymentCriteriaMet" -> verifyOptionalField(expected,
                    v -> assertThat(actual.getMinPaymentCriteriaMet()).as(label).isEqualTo(Boolean.parseBoolean(v)),
                    () -> assertThat(actual.getMinPaymentCriteriaMet()).as(label).isNull());
            case "delinquentDays" ->
                verifyOptionalField(expected, v -> assertThat(actual.getDelinquentDays()).as(label).isEqualTo(Long.parseLong(v)),
                        () -> assertThat(actual.getDelinquentDays()).as(label).isNull());
            case "delinquentAmount" -> verifyOptionalField(expected,
                    v -> assertThat(actual.getDelinquentAmount()).as(label).isEqualByComparingTo(new BigDecimal(v)),
                    () -> assertThat(actual.getDelinquentAmount()).as(label).isNull());
            default -> throw new IllegalArgumentException("Unknown schedule field: " + field);
        }
    }

    private void verifyOptionalField(final String expected, final Consumer<String> whenPresent, final Runnable whenAbsent) {
        Optional.ofNullable(expected).filter(Predicate.not(String::isBlank)).ifPresentOrElse(whenPresent, whenAbsent);
    }

    private Long getLoanId() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertThat(loanResponse).isNotNull();
        return loanResponse.getLoanId();
    }

    private PostWorkingCapitalLoansDelinquencyActionRequest buildRescheduleRequest(final BigDecimal minimumPayment,
            final String minimumPaymentType, final int frequency, final String frequencyType) {
        final PostWorkingCapitalLoansDelinquencyActionRequest request = new PostWorkingCapitalLoansDelinquencyActionRequest();
        request.setAction("reschedule");
        request.setMinimumPayment(minimumPayment);
        request.setMinimumPaymentType(minimumPaymentType);
        request.setFrequency(frequency);
        request.setFrequencyType(frequencyType);
        request.setLocale("en");
        return request;
    }
}
