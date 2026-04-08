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

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.WorkingCapitalLoansApi;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleGenerateRequest;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.assertj.core.api.SoftAssertions;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalAmortizationScheduleStepDef extends AbstractStepDef {

    private static final String WC_AMORT_SCHEDULE_KEY = "WC_AMORT_SCHEDULE_RESPONSE";

    private final FineractFeignClient fineractFeignClient;

    @When("Admin generates a projected amortization schedule with originationFeeAmount {double}, netDisbursementAmount {double}, totalPaymentValue {double}, periodPaymentRate {double}, npvDayCount {int}, expectedDisbursementDate {string}")
    public void generateAmortizationSchedule(final double originationFeeAmount, final double netDisbursementAmount,
            final double totalPaymentValue, final double periodPaymentRate, final int npvDayCount, final String expectedDisbursementDate) {
        final Long loanId = extractLoanId();
        final WorkingCapitalLoansApi api = fineractFeignClient.create(WorkingCapitalLoansApi.class);

        final ProjectedAmortizationScheduleGenerateRequest request = new ProjectedAmortizationScheduleGenerateRequest();
        request.setOriginationFeeAmount(BigDecimal.valueOf(originationFeeAmount));
        request.setNetDisbursementAmount(BigDecimal.valueOf(netDisbursementAmount));
        request.setTotalPaymentValue(BigDecimal.valueOf(totalPaymentValue));
        request.setPeriodPaymentRate(BigDecimal.valueOf(periodPaymentRate));
        request.setNpvDayCount(npvDayCount);
        request.setExpectedDisbursementDate(LocalDate.parse(expectedDisbursementDate));

        api.generateAmortizationSchedule(loanId, request);
        log.info("Generated amortization schedule for loan {}", loanId);
    }

    @When("Admin retrieves the projected amortization schedule")
    public void retrieveAmortizationSchedule() {
        final Long loanId = extractLoanId();
        final WorkingCapitalLoansApi api = fineractFeignClient.create(WorkingCapitalLoansApi.class);

        final ProjectedAmortizationScheduleData response = api.retrieveAmortizationSchedule(loanId);
        log.info("Retrieved amortization schedule for loan {}: netDisbursementAmount={}", loanId, response.getNetDisbursementAmount());
        TestContext.INSTANCE.set(WC_AMORT_SCHEDULE_KEY, response);
    }

    @Then("The retrieved amortization schedule has the following summary fields:")
    public void verifyRetrievedSummaryFields(final DataTable dataTable) {
        verifySummaryFields(dataTable);
    }

    @Then("The retrieved amortization schedule has payments with the following details:")
    public void verifyRetrievedPaymentDetails(final DataTable dataTable) {
        verifyPaymentDetails(dataTable);
    }

    private void verifySummaryFields(final DataTable dataTable) {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();

        final Map<String, String> expected = dataTable.asMaps().getFirst();
        final SoftAssertions assertions = new SoftAssertions();

        assertDecimal(assertions, "originationFeeAmount", response.getOriginationFeeAmount(), expected.get("originationFeeAmount"));
        assertDecimal(assertions, "netDisbursementAmount", response.getNetDisbursementAmount(), expected.get("netDisbursementAmount"));
        assertDecimal(assertions, "totalPaymentValue", response.getTotalPaymentValue(), expected.get("totalPaymentValue"));
        assertDecimal(assertions, "periodPaymentRate", response.getPeriodPaymentRate(), expected.get("periodPaymentRate"));
        assertInt(assertions, "npvDayCount", response.getNpvDayCount(), expected.get("npvDayCount"));
        assertDecimal(assertions, "expectedPaymentAmount", response.getExpectedPaymentAmount(), expected.get("expectedPaymentAmount"));
        assertInt(assertions, "loanTerm", response.getLoanTerm(), expected.get("loanTerm"));

        assertions.assertAll();
    }

    private void verifyPaymentDetails(final DataTable dataTable) {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();

        final List<ProjectedAmortizationSchedulePaymentData> actualPayments = response.getPayments();
        assertThat(actualPayments).as("payments list").isNotNull();

        final List<Map<String, String>> expectedRows = dataTable.asMaps();
        assertThat(actualPayments).as("payment count").hasSameSizeAs(expectedRows);

        final SoftAssertions assertions = new SoftAssertions();
        for (int i = 0; i < expectedRows.size(); i++) {
            final Map<String, String> expected = expectedRows.get(i);
            final ProjectedAmortizationSchedulePaymentData actual = actualPayments.get(i);
            final String p = "payment[" + i + "].";

            assertInt(assertions, p + "paymentNo", actual.getPaymentNo(), expected.get("paymentNo"));
            assertDate(assertions, p + "date", actual.getPaymentDate(), expected.get("date"));
            assertLong(assertions, p + "paymentsLeft", actual.getPaymentsLeft(), expected.get("paymentsLeft"));
            assertNullableDecimal(assertions, p + "expectedPaymentAmount", actual.getExpectedPaymentAmount(),
                    expected.get("expectedPaymentAmount"));
            assertNullableDecimal(assertions, p + "forecastPaymentAmount", actual.getForecastPaymentAmount(),
                    expected.get("forecastPaymentAmount"));
            assertOptionalDecimal(assertions, p + "discountFactor", actual.getDiscountFactor(), expected.get("discountFactor"));
            assertNullableDecimal(assertions, p + "npvValue", actual.getNpvValue(), expected.get("npvValue"));
            assertNullableDecimal(assertions, p + "balance", actual.getBalance(), expected.get("balance"));
            assertNullableDecimal(assertions, p + "expectedAmortizationAmount", actual.getExpectedAmortizationAmount(),
                    expected.get("expectedAmortizationAmount"));
            assertNullableDecimal(assertions, p + "netAmortizationAmount", actual.getNetAmortizationAmount(),
                    expected.get("netAmortizationAmount"));
            assertNullableDecimal(assertions, p + "actualPaymentAmount", actual.getActualPaymentAmount(),
                    expected.get("actualPaymentAmount"));
            assertNullableDecimal(assertions, p + "actualAmortizationAmount", actual.getActualAmortizationAmount(),
                    expected.get("actualAmortizationAmount"));
            assertNullableDecimal(assertions, p + "incomeModification", actual.getIncomeModification(), expected.get("incomeModification"));
            assertNullableDecimal(assertions, p + "deferredBalance", actual.getDeferredBalance(), expected.get("deferredBalance"));
        }

        assertions.assertAll();
    }

    private static void assertDecimal(final SoftAssertions assertions, final String field, final BigDecimal actual,
            final String expectedStr) {
        if (expectedStr == null || expectedStr.isBlank()) {
            return;
        }
        final BigDecimal expected = new BigDecimal(expectedStr);
        assertions.assertThat(actual).as(field).isNotNull();
        if (actual != null) {
            assertions.assertThat(actual.compareTo(expected)).as("%s: expected=%s actual=%s", field, expected, actual).isEqualTo(0);
        }
    }

    private static void assertNullableDecimal(final SoftAssertions assertions, final String field, final BigDecimal actual,
            final String expectedStr) {
        if (expectedStr == null || expectedStr.isBlank()) {
            assertions.assertThat(actual).as(field + " should be null").isNull();
            return;
        }
        assertDecimal(assertions, field, actual, expectedStr);
    }

    private static void assertInt(final SoftAssertions assertions, final String field, final Integer actual, final String expectedStr) {
        if (expectedStr == null || expectedStr.isBlank()) {
            return;
        }
        assertions.assertThat(actual).as(field).isEqualTo(Integer.parseInt(expectedStr));
    }

    private static void assertLong(final SoftAssertions assertions, final String field, final Long actual, final String expectedStr) {
        if (expectedStr == null || expectedStr.isBlank()) {
            return;
        }
        assertions.assertThat(actual).as(field).isEqualTo(Long.parseLong(expectedStr));
    }

    private static void assertDate(final SoftAssertions assertions, final String field, final LocalDate actual, final String expectedStr) {
        if (expectedStr == null || expectedStr.isBlank()) {
            return;
        }
        assertions.assertThat(actual).as(field).isEqualTo(LocalDate.parse(expectedStr));
    }

    private static void assertOptionalDecimal(final SoftAssertions assertions, final String field, final BigDecimal actual,
            final String expectedStr) {
        if (expectedStr == null || expectedStr.isBlank()) {
            assertions.assertThat(actual).as(field + " should not be null").isNotNull();
            return;
        }
        assertDecimal(assertions, field, actual, expectedStr);
    }

    private Long extractLoanId() {
        final PostWorkingCapitalLoansResponse response = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return response.getLoanId();
    }
}
