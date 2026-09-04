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
import static org.assertj.core.api.Assertions.within;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.WorkingCapitalLoansApi;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleGenerateRequest;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.test.helper.WorkingCapitalScheduleMatcher;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.assertj.core.api.SoftAssertions;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalAmortizationScheduleStepDef extends AbstractStepDef {

    private static final String WC_AMORT_SCHEDULE_KEY = "WC_AMORT_SCHEDULE_RESPONSE";
    private static final String WC_AMORT_SCHEDULE_REMEMBERED_SEGMENT_KEY = "WC_AMORT_SCHEDULE_REMEMBERED_SEGMENT";
    private static final String WC_AMORT_SCHEDULE_SNAPSHOT_KEY_PREFIX = "WC_AMORT_SCHEDULE_SNAPSHOT_";
    private static final String WC_AMORT_EXPECTED_PROJECTION_SNAPSHOT_KEY_PREFIX = "WC_AMORT_EXPECTED_PROJECTION_SNAPSHOT_";

    private final FineractFeignClient fineractFeignClient;

    @When("Admin generates a projected amortization schedule with discountFeeAmount {double}, netDisbursementAmount {double}, totalPaymentVolume {double}, periodPaymentRate {double}, npvDayCount {int}, expectedDisbursementDate {string}")
    public void generateAmortizationSchedule(final double discountFeeAmount, final double netDisbursementAmount,
            final double totalPaymentVolume, final double periodPaymentRate, final int npvDayCount, final String expectedDisbursementDate) {
        final Long loanId = extractLoanId();
        final WorkingCapitalLoansApi api = fineractFeignClient.create(WorkingCapitalLoansApi.class);

        final ProjectedAmortizationScheduleGenerateRequest request = new ProjectedAmortizationScheduleGenerateRequest();
        request.setDiscountFeeAmount(BigDecimal.valueOf(discountFeeAmount));
        request.setNetDisbursementAmount(BigDecimal.valueOf(netDisbursementAmount));
        request.setTotalPaymentVolume(BigDecimal.valueOf(totalPaymentVolume));
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

    @Then("Admin remembers the retrieved amortization schedule as {string}")
    public void rememberAmortizationSchedule(final String snapshotName) {
        final ProjectedAmortizationScheduleData response = getRetrievedSchedule();
        TestContext.INSTANCE.set(WC_AMORT_SCHEDULE_SNAPSHOT_KEY_PREFIX + snapshotName, response);
    }

    @Then("The retrieved amortization schedule exactly matches the remembered schedule {string}")
    public void verifyAmortizationScheduleMatchesRemembered(final String snapshotName) {
        final ProjectedAmortizationScheduleData actual = getRetrievedSchedule();
        final ProjectedAmortizationScheduleData expected = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_SNAPSHOT_KEY_PREFIX + snapshotName);

        assertThat(expected).as("remembered amortization schedule '%s'", snapshotName).isNotNull();
        assertThat(actual).as("amortization schedule after replay").usingRecursiveComparison() //
                .isEqualTo(expected);
    }

    @Then("Admin remembers the retrieved amortization schedule expected projection as {string}")
    public void rememberAmortizationScheduleExpectedProjection(final String snapshotName) {
        final ExpectedProjectionSnapshot snapshot = ExpectedProjectionSnapshot.from(getRetrievedSchedule());
        TestContext.INSTANCE.set(WC_AMORT_EXPECTED_PROJECTION_SNAPSHOT_KEY_PREFIX + snapshotName, snapshot);
    }

    @Then("The retrieved amortization schedule expected projection matches the remembered projection {string}")
    public void verifyAmortizationScheduleExpectedProjectionMatchesRemembered(final String snapshotName) {
        final ExpectedProjectionSnapshot actual = ExpectedProjectionSnapshot.from(getRetrievedSchedule());
        final ExpectedProjectionSnapshot expected = TestContext.INSTANCE
                .get(WC_AMORT_EXPECTED_PROJECTION_SNAPSHOT_KEY_PREFIX + snapshotName);

        assertThat(expected).as("remembered expected projection '%s'", snapshotName).isNotNull();
        assertThat(actual.withoutPayments()).as("expected projection summary after exact on-time repayments") //
                .usingRecursiveComparison().isEqualTo(expected.withoutPayments());
        assertThat(actual.payments()).as("expected projection payment count").hasSameSizeAs(expected.payments());
        for (int i = 0; i < actual.payments().size(); i++) {
            assertThat(actual.payments().get(i)).as("expected projection payment[%s] after exact on-time repayments", i) //
                    .usingRecursiveComparison().isEqualTo(expected.payments().get(i));
        }
    }

    @Then("Every fully paid amortization schedule period has actual amortization equal to expected amortization")
    public void verifyFullyPaidPeriodsHaveExpectedAmortization() {
        final List<ProjectedAmortizationSchedulePaymentData> fullyPaidPeriods = getRetrievedSchedule().getPayments().stream() //
                .filter(payment -> payment.getPaymentNo() != 0) //
                .filter(payment -> payment.getExpectedPaymentAmount() != null && payment.getActualPaymentAmount() != null) //
                .filter(payment -> payment.getActualPaymentAmount().compareTo(payment.getExpectedPaymentAmount()) == 0) //
                .toList();
        assertThat(fullyPaidPeriods).as("fully paid amortization periods").isNotEmpty();

        // Within one unit of the currency, not to the exact figure. Two things stop these being the same number.
        //
        // A period is picked out as fully paid by comparing the amounts as they are reported, and those are rounded to
        // the currency - so an instalment of 47.2222 collected as 47.22 looks exactly paid while it is a fraction
        // short. And the reported amortization is the step the running fee total took that period, where the total is
        // carried exactly and rounded once; the expected column rounds every period before adding it. The two
        // therefore sit up to a unit apart on any given period even though the running totals track each other, and
        // that is the intended trade: the total is what has to close on the fee exactly, so it is the total that is
        // measured accurately and the periods that are its differences.
        final SoftAssertions assertions = new SoftAssertions();
        for (final ProjectedAmortizationSchedulePaymentData payment : fullyPaidPeriods) {
            final BigDecimal expectedAmortization = payment.getExpectedAmortizationAmount();
            final BigDecimal oneCurrencyUnit = BigDecimal.ONE.movePointLeft(expectedAmortization.scale());
            assertions.assertThat(payment.getActualAmortizationAmount()) //
                    .as("payment[%s].actualAmortizationAmount", payment.getPaymentNo()) //
                    .isCloseTo(expectedAmortization, within(oneCurrencyUnit));
        }
        assertions.assertAll();
    }

    @Then("The retrieved amortization schedule has no negative monetary amounts")
    public void verifyAmortizationScheduleHasNoNegativeMonetaryAmounts() {
        final ProjectedAmortizationScheduleData response = getRetrievedSchedule();
        final SoftAssertions assertions = new SoftAssertions();

        for (final ProjectedAmortizationSchedulePaymentData payment : response.getPayments()) {
            final String prefix = "payment[" + payment.getPaymentNo() + "].";
            assertNonNegativeIfPresent(assertions, prefix + "expectedBalance", payment.getExpectedBalance());
            assertNonNegativeIfPresent(assertions, prefix + "actualBalance", payment.getActualBalance());
            assertNonNegativeIfPresent(assertions, prefix + "expectedAmortizationAmount", payment.getExpectedAmortizationAmount());
            assertNonNegativeIfPresent(assertions, prefix + "actualAmortizationAmount", payment.getActualAmortizationAmount());
            assertNonNegativeIfPresent(assertions, prefix + "expectedDiscountFeeBalance", payment.getExpectedDiscountFeeBalance());
            assertNonNegativeIfPresent(assertions, prefix + "actualDiscountFeeBalance", payment.getActualDiscountFeeBalance());
            assertNonNegativeIfPresent(assertions, prefix + "actualPaymentAmount", payment.getActualPaymentAmount());
            if (payment.getPaymentNo() != 0) {
                assertNonNegativeIfPresent(assertions, prefix + "expectedPaymentAmount", payment.getExpectedPaymentAmount());
            }
        }

        assertions.assertAll();
    }

    @Then("The retrieved amortization schedule actual amortization total is {string}")
    public void verifyActualAmortizationTotal(final String expectedTotal) {
        final BigDecimal actualTotal = getRetrievedSchedule().getPayments().stream() //
                .map(ProjectedAmortizationSchedulePaymentData::getActualAmortizationAmount) //
                .filter(amount -> amount != null) //
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(actualTotal).as("total actual discount-fee amortization").isEqualByComparingTo(expectedTotal);
    }

    @Then("The retrieved amortization schedule actual payments plus future expected payments total {string}")
    public void verifyActualAndFutureExpectedPaymentsTotal(final String expectedTotal) {
        final List<ProjectedAmortizationSchedulePaymentData> payments = getRetrievedSchedule().getPayments();
        final BigDecimal actualPayments = payments.stream() //
                .filter(payment -> payment.getPaymentNo() != 0) //
                .map(ProjectedAmortizationSchedulePaymentData::getActualPaymentAmount) //
                .filter(amount -> amount != null) //
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal futureExpectedPayments = payments.stream() //
                .filter(payment -> payment.getPaymentNo() != 0 && payment.getActualPaymentAmount() == null) //
                .map(ProjectedAmortizationSchedulePaymentData::getExpectedPaymentAmount) //
                .filter(amount -> amount != null) //
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(actualPayments.add(futureExpectedPayments)) //
                .as("actual payments (%s) plus future expected payments (%s)", actualPayments, futureExpectedPayments) //
                .isEqualByComparingTo(expectedTotal);
    }

    @Then("Admin remembers the retrieved amortization schedule payments before {string}")
    public void rememberAmortizationSchedulePaymentsBefore(final String cutoffDate) {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();
        final List<ProjectedAmortizationSchedulePaymentData> segment = paymentsBefore(response, cutoffDate);
        assertThat(segment).as("payments before %s", cutoffDate).isNotEmpty();
        TestContext.INSTANCE.set(WC_AMORT_SCHEDULE_REMEMBERED_SEGMENT_KEY, segment);
    }

    @Then("The retrieved amortization schedule payments before {string} match the previously remembered ones")
    public void verifyAmortizationSchedulePaymentsBeforeMatchRemembered(final String cutoffDate) {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();
        final List<ProjectedAmortizationSchedulePaymentData> actualSegment = paymentsBefore(response, cutoffDate);

        final List<ProjectedAmortizationSchedulePaymentData> rememberedSegment = TestContext.INSTANCE
                .get(WC_AMORT_SCHEDULE_REMEMBERED_SEGMENT_KEY);
        assertThat(rememberedSegment).as("previously remembered payments before %s", cutoffDate).isNotNull();

        // Only the projected ("expected*") columns are driven by the rate history being reconstructed; the "actual*"
        // columns reflect real transactions booked elsewhere on the loan and legitimately populate over time
        // regardless of which period they belong to, so they are excluded from this comparison.
        assertThat(actualSegment).as("projected payments before %s must be unchanged by reconstruction", cutoffDate)
                .usingRecursiveComparison()
                .ignoringFields("actualBalance", "actualPaymentAmount", "actualAmortizationAmount", "actualDiscountFeeBalance")
                .isEqualTo(rememberedSegment);
    }

    private static List<ProjectedAmortizationSchedulePaymentData> paymentsBefore(final ProjectedAmortizationScheduleData response,
            final String cutoffDate) {
        final LocalDate cutoff = LocalDate.parse(cutoffDate);
        return response.getPayments().stream()
                .filter(payment -> payment.getPaymentDate() != null && payment.getPaymentDate().isBefore(cutoff)).toList();
    }

    @Then("The retrieved amortization schedule has the following summary fields:")
    public void verifyRetrievedSummaryFields(final DataTable dataTable) {
        verifySummaryFields(dataTable);
    }

    @Then("The retrieved amortization schedule has the following summary fields with positive effectiveInterestRate value:")
    public void verifyRetrievedSummaryFieldsWithPositiveEffectiveInterestRate(final DataTable dataTable) {
        verifySummaryFieldsWithPositiveEffectiveInterestRate(dataTable);
    }

    @Then("The retrieved amortization schedule has payments with the following details:")
    public void verifyRetrievedPaymentDetails(final DataTable dataTable) {
        verifyPaymentDetails(dataTable, null);
    }

    @Then("The retrieved amortization schedule has payments with the following details in first {string} lines:")
    public void verifyRetrievedPaymentDetailsFirstNLines(final String firstNLines, final DataTable dataTable) {
        verifyPaymentDetails(dataTable, Integer.valueOf(firstNLines));
    }

    /**
     * Verifies only the payment rows whose {@code paymentNo} is listed in the table, and only the columns present in
     * the table header. Lets a scenario pin a curated subset of rows (e.g. the first few plus the final remainder row)
     * without restating the whole schedule.
     */
    private static final Set<String> LISTED_PAYMENT_COLUMNS = Set.of("paymentNo", "date", "expectedPaymentAmount", "expectedBalance",
            "expectedAmortizationAmount", "expectedDiscountFeeBalance", "actualPaymentAmount", "actualBalance", "actualAmortizationAmount",
            "actualDiscountFeeBalance");

    @Then("The retrieved amortization schedule has payments with the following details for the listed payment numbers:")
    public void verifyRetrievedPaymentDetailsByPaymentNo(final DataTable dataTable) {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();
        // Only the listed columns are compared, so a misspelt or unsupported header must fail loudly instead of
        // silently producing no assertion for that column.
        assertThat(dataTable.row(0)).as("table headers must all be supported columns").isSubsetOf(LISTED_PAYMENT_COLUMNS);

        final List<ProjectedAmortizationSchedulePaymentData> actualPayments = response.getPayments();
        assertThat(actualPayments).as("payments list").isNotNull();

        final Map<Integer, ProjectedAmortizationSchedulePaymentData> byPaymentNo = new HashMap<>();
        for (final ProjectedAmortizationSchedulePaymentData payment : actualPayments) {
            byPaymentNo.put(payment.getPaymentNo(), payment);
        }

        final SoftAssertions assertions = new SoftAssertions();
        for (final Map<String, String> expected : dataTable.asMaps()) {
            final int paymentNo = Integer.parseInt(expected.get("paymentNo"));
            final ProjectedAmortizationSchedulePaymentData actual = byPaymentNo.get(paymentNo);
            assertions.assertThat(actual).as("payment %s present", paymentNo).isNotNull();
            if (actual == null) {
                continue;
            }
            final String p = "payment[" + paymentNo + "].";
            if (expected.containsKey("date")) {
                assertDate(assertions, p + "date", actual.getPaymentDate(), expected.get("date"));
            }
            if (expected.containsKey("expectedPaymentAmount")) {
                assertNullableDecimal(assertions, p + "expectedPaymentAmount", actual.getExpectedPaymentAmount(),
                        expected.get("expectedPaymentAmount"));
            }
            if (expected.containsKey("expectedBalance")) {
                assertNullableDecimal(assertions, p + "expectedBalance", actual.getExpectedBalance(), expected.get("expectedBalance"));
            }
            if (expected.containsKey("expectedAmortizationAmount")) {
                assertNullableDecimal(assertions, p + "expectedAmortizationAmount", actual.getExpectedAmortizationAmount(),
                        expected.get("expectedAmortizationAmount"));
            }
            if (expected.containsKey("expectedDiscountFeeBalance")) {
                assertNullableDecimal(assertions, p + "expectedDiscountFeeBalance", actual.getExpectedDiscountFeeBalance(),
                        expected.get("expectedDiscountFeeBalance"));
            }
            if (expected.containsKey("actualPaymentAmount")) {
                assertNullableDecimal(assertions, p + "actualPaymentAmount", actual.getActualPaymentAmount(),
                        expected.get("actualPaymentAmount"));
            }
            if (expected.containsKey("actualBalance")) {
                assertNullableDecimal(assertions, p + "actualBalance", actual.getActualBalance(), expected.get("actualBalance"));
            }
            if (expected.containsKey("actualAmortizationAmount")) {
                assertNullableDecimal(assertions, p + "actualAmortizationAmount", actual.getActualAmortizationAmount(),
                        expected.get("actualAmortizationAmount"));
            }
            if (expected.containsKey("actualDiscountFeeBalance")) {
                assertNullableDecimal(assertions, p + "actualDiscountFeeBalance", actual.getActualDiscountFeeBalance(),
                        expected.get("actualDiscountFeeBalance"));
            }
        }

        assertions.assertAll();
    }

    @Then("The retrieved amortization schedule has no negative amounts")
    public void verifyRetrievedScheduleHasNoNegativeAmounts() {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();
        final SoftAssertions assertions = new SoftAssertions();
        for (final ProjectedAmortizationSchedulePaymentData payment : response.getPayments()) {
            if (payment.getPaymentNo() == 0) {
                continue; // disbursement row carries the negated disbursement by design
            }
            final String p = "payment[" + payment.getPaymentNo() + "].";
            assertNonNegative(assertions, p + "expectedPaymentAmount", payment.getExpectedPaymentAmount());
            assertNonNegative(assertions, p + "expectedBalance", payment.getExpectedBalance());
            assertNonNegative(assertions, p + "expectedAmortizationAmount", payment.getExpectedAmortizationAmount());
            assertNonNegative(assertions, p + "expectedDiscountFeeBalance", payment.getExpectedDiscountFeeBalance());
            assertNonNegative(assertions, p + "actualPaymentAmount", payment.getActualPaymentAmount());
            assertNonNegative(assertions, p + "actualAmortizationAmount", payment.getActualAmortizationAmount());
            assertNonNegative(assertions, p + "actualDiscountFeeBalance", payment.getActualDiscountFeeBalance());
        }
        assertions.assertAll();
    }

    @Then("The retrieved amortization schedule expected amortization sums to the discount fee and both expected balances close to zero")
    public void verifyRetrievedScheduleExpectedAmortizationClosesToDiscountFee() {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();
        final List<ProjectedAmortizationSchedulePaymentData> rows = response.getPayments().stream().filter(p -> p.getPaymentNo() > 0)
                .toList();
        assertThat(rows).as("payment rows").isNotEmpty();
        final BigDecimal amortizationSum = sum(rows, ProjectedAmortizationSchedulePaymentData::getExpectedAmortizationAmount);
        final BigDecimal paymentSum = sum(rows, ProjectedAmortizationSchedulePaymentData::getExpectedPaymentAmount);
        final ProjectedAmortizationSchedulePaymentData last = rows.getLast();
        final SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(amortizationSum).as("sum(expectedAmortizationAmount) == discountFeeAmount")
                .isEqualByComparingTo(response.getDiscountFeeAmount());
        assertions.assertThat(paymentSum).as("sum(expectedPaymentAmount) == netDisbursementAmount + discountFeeAmount")
                .isEqualByComparingTo(response.getNetDisbursementAmount().add(response.getDiscountFeeAmount()));
        assertions.assertThat(last.getExpectedBalance()).as("last expectedBalance").isEqualByComparingTo(BigDecimal.ZERO);
        assertions.assertThat(last.getExpectedDiscountFeeBalance()).as("last expectedDiscountFeeBalance")
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertions.assertAll();
    }

    /**
     * Ledger/schedule consistency guard (PS-3313 "amortization amounts + not yet amortized amounts matches discount
     * fee"): the actual amortization rows must add up to the realized discount-fee income booked on the loan, the last
     * paid row's actual discount fee balance must equal the loan's unrealized income, and realized + unrealized must
     * equal the discount fee the schedule was built with.
     *
     * <p>
     * Precondition: use it right after a close of business on a loan that is still active. The ledger only follows the
     * schedule once COB has posted the amortization for every repayment recorded so far; between an undo and the next
     * COB (the reversal is booked by COB) or on a loan settled by a lump-sum payoff (the closing recognition tops up
     * the fee regardless of the schedule) the two legitimately differ, so the step would be red for reasons unrelated
     * to the scenario. Missed instalments are fine.
     */
    @Then("The retrieved amortization schedule actual amortization is consistent with the loan realized and unrealized income after close of business")
    public void verifyRetrievedScheduleActualAmortizationMatchesLoanIncome() {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();
        final List<ProjectedAmortizationSchedulePaymentData> paidRows = response.getPayments().stream()
                .filter(p -> p.getPaymentNo() > 0 && p.getActualPaymentAmount() != null).toList();
        assertThat(paidRows).as("paid rows").isNotEmpty();
        final BigDecimal actualAmortizationSum = sum(paidRows, ProjectedAmortizationSchedulePaymentData::getActualAmortizationAmount);
        final ProjectedAmortizationSchedulePaymentData lastPaid = paidRows.getLast();

        final GetWorkingCapitalLoansLoanIdResponse loan = fineractFeignClient.workingCapitalLoans()
                .retrieveWorkingCapitalLoanById(extractLoanId());
        assertThat(loan.getBalance()).as("loan balance").isNotNull();
        final BigDecimal realized = loan.getBalance().getRealizedIncomeFromDiscountFee();
        final BigDecimal unrealized = loan.getBalance().getUnrealizedIncomeFromDiscountFee();

        final SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(actualAmortizationSum).as("sum(actualAmortizationAmount) == loan realizedIncomeFromDiscountFee")
                .isEqualByComparingTo(realized);
        assertions.assertThat(lastPaid.getActualDiscountFeeBalance())
                .as("last paid row actualDiscountFeeBalance == loan unrealizedIncomeFromDiscountFee").isEqualByComparingTo(unrealized);
        assertions.assertThat(realized.add(unrealized)).as("realized + unrealized == schedule discountFeeAmount")
                .isEqualByComparingTo(response.getDiscountFeeAmount());
        assertions.assertThat(realized).as("realized income must never exceed the discount fee")
                .isLessThanOrEqualTo(response.getDiscountFeeAmount());
        assertions.assertAll();
    }

    private static BigDecimal sum(final List<ProjectedAmortizationSchedulePaymentData> rows,
            final Function<ProjectedAmortizationSchedulePaymentData, BigDecimal> getter) {
        return rows.stream().map(getter).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void assertNonNegative(final SoftAssertions assertions, final String field, final BigDecimal value) {
        if (value != null) {
            assertions.assertThat(value).as(field + " must not be negative").isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }
    }

    public void checkSummaryFields(final SoftAssertions assertions, ProjectedAmortizationScheduleData response,
            Map<String, String> expected) {
        assertDecimal(assertions, "discountFeeAmount", response.getDiscountFeeAmount(), expected.get("discountFeeAmount"));
        assertDecimal(assertions, "netDisbursementAmount", response.getNetDisbursementAmount(), expected.get("netDisbursementAmount"));
        assertDecimal(assertions, "totalPaymentVolume", response.getTotalPaymentVolume(), expected.get("totalPaymentVolume"));
        assertDecimal(assertions, "periodPaymentRate", response.getPeriodPaymentRate(), expected.get("periodPaymentRate"));
        assertInt(assertions, "npvDayCount", response.getNpvDayCount(), expected.get("npvDayCount"));
        assertDecimal(assertions, "expectedPaymentAmount", response.getExpectedPaymentAmount(), expected.get("expectedPaymentAmount"));
        assertInt(assertions, "originalPaymentNumber", response.getOriginalPaymentNumber(), expected.get("originalPaymentNumber"));
    }

    private void verifySummaryFields(final DataTable dataTable) {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();
        final Map<String, String> expected = dataTable.asMaps().getFirst();

        final SoftAssertions assertions = new SoftAssertions();
        checkSummaryFields(assertions, response, expected);
        assertions.assertAll();
    }

    private void verifySummaryFieldsWithPositiveEffectiveInterestRate(final DataTable dataTable) {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();
        final Map<String, String> expected = dataTable.asMaps().getFirst();

        final SoftAssertions assertions = new SoftAssertions();
        checkSummaryFields(assertions, response, expected);
        assertDecimal(assertions, "effectiveInterestRate", response.getEffectiveInterestRate(), expected.get("effectiveInterestRate"));
        assertions.assertAll();
    }

    private void verifyPaymentDetails(final DataTable dataTable, final Integer firstNLines) {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();

        final List<ProjectedAmortizationSchedulePaymentData> actualPayments = firstNLines != null
                ? response.getPayments().subList(0, firstNLines)
                : response.getPayments();
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
            assertNullableDecimal(assertions, p + "expectedPaymentAmount", actual.getExpectedPaymentAmount(),
                    expected.get("expectedPaymentAmount"));
            assertNullableDecimal(assertions, p + "expectedBalance", actual.getExpectedBalance(), expected.get("expectedBalance"));
            assertNullableDecimal(assertions, p + "actualBalance", actual.getActualBalance(), expected.get("actualBalance"));
            assertNullableDecimal(assertions, p + "expectedAmortizationAmount", actual.getExpectedAmortizationAmount(),
                    expected.get("expectedAmortizationAmount"));
            assertNullableDecimal(assertions, p + "actualPaymentAmount", actual.getActualPaymentAmount(),
                    expected.get("actualPaymentAmount"));
            assertNullableDecimal(assertions, p + "actualAmortizationAmount", actual.getActualAmortizationAmount(),
                    expected.get("actualAmortizationAmount"));
            assertNullableDecimal(assertions, p + "expectedDiscountFeeBalance", actual.getExpectedDiscountFeeBalance(),
                    expected.get("expectedDiscountFeeBalance"));
            assertNullableDecimal(assertions, p + "actualDiscountFeeBalance", actual.getActualDiscountFeeBalance(),
                    expected.get("actualDiscountFeeBalance"));
        }

        assertions.assertAll();
    }

    private static void assertDecimal(final SoftAssertions assertions, final String field, final BigDecimal actual,
            final String expectedStr) {
        if (WorkingCapitalScheduleMatcher.isBlank(expectedStr)) {
            return;
        }
        assertions.assertThat(WorkingCapitalScheduleMatcher.matchesDecimal(actual, expectedStr))
                .as("%s: expected=%s actual=%s", field, expectedStr, actual).isTrue();
    }

    private static void assertNullableDecimal(final SoftAssertions assertions, final String field, final BigDecimal actual,
            final String expectedStr) {
        if (WorkingCapitalScheduleMatcher.isBlank(expectedStr)) {
            assertions.assertThat(actual).as(field + " should be null").isNull();
            return;
        }
        assertDecimal(assertions, field, actual, expectedStr);
    }

    private static void assertInt(final SoftAssertions assertions, final String field, final Integer actual, final String expectedStr) {
        if (WorkingCapitalScheduleMatcher.isBlank(expectedStr)) {
            return;
        }
        assertions.assertThat(WorkingCapitalScheduleMatcher.matchesInteger(actual, expectedStr)).as(field).isTrue();
    }

    private static void assertDate(final SoftAssertions assertions, final String field, final LocalDate actual, final String expectedStr) {
        if (WorkingCapitalScheduleMatcher.isBlank(expectedStr)) {
            return;
        }
        assertions.assertThat(WorkingCapitalScheduleMatcher.matchesDate(actual, expectedStr)).as(field).isTrue();
    }

    private static void assertNonNegativeIfPresent(final SoftAssertions assertions, final String field, final BigDecimal amount) {
        if (amount != null) {
            assertions.assertThat(amount).as(field).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }
    }

    private static ProjectedAmortizationScheduleData getRetrievedSchedule() {
        final ProjectedAmortizationScheduleData response = TestContext.INSTANCE.get(WC_AMORT_SCHEDULE_KEY);
        assertThat(response).as("Amortization schedule response").isNotNull();
        assertThat(response.getPayments()).as("Amortization schedule payments").isNotNull();
        return response;
    }

    private record ExpectedProjectionSnapshot(BigDecimal discountFeeAmount, BigDecimal effectiveInterestRate,
            LocalDate expectedDisbursementDate, BigDecimal expectedPaymentAmount, BigDecimal netDisbursementAmount, Integer npvDayCount,
            Integer originalPaymentNumber, BigDecimal periodPaymentRate, BigDecimal totalPaymentVolume,
            List<ExpectedPaymentSnapshot> payments) {

        private static ExpectedProjectionSnapshot from(final ProjectedAmortizationScheduleData schedule) {
            final List<ExpectedPaymentSnapshot> payments = schedule.getPayments().stream().map(ExpectedPaymentSnapshot::from).toList();
            return new ExpectedProjectionSnapshot(schedule.getDiscountFeeAmount(), schedule.getEffectiveInterestRate(),
                    schedule.getExpectedDisbursementDate(), schedule.getExpectedPaymentAmount(), schedule.getNetDisbursementAmount(),
                    schedule.getNpvDayCount(), schedule.getOriginalPaymentNumber(), schedule.getPeriodPaymentRate(),
                    schedule.getTotalPaymentVolume(), payments);
        }

        private ExpectedProjectionSnapshot withoutPayments() {
            return new ExpectedProjectionSnapshot(discountFeeAmount, effectiveInterestRate, expectedDisbursementDate, expectedPaymentAmount,
                    netDisbursementAmount, npvDayCount, originalPaymentNumber, periodPaymentRate, totalPaymentVolume, List.of());
        }
    }

    private record ExpectedPaymentSnapshot(Integer paymentNo, LocalDate paymentDate, BigDecimal expectedPaymentAmount,
            BigDecimal expectedBalance, BigDecimal expectedAmortizationAmount, BigDecimal expectedDiscountFeeBalance) {

        private static ExpectedPaymentSnapshot from(final ProjectedAmortizationSchedulePaymentData payment) {
            return new ExpectedPaymentSnapshot(payment.getPaymentNo(), payment.getPaymentDate(), payment.getExpectedPaymentAmount(),
                    payment.getExpectedBalance(), payment.getExpectedAmortizationAmount(), payment.getExpectedDiscountFeeBalance());
        }
    }

    private Long extractLoanId() {
        final PostWorkingCapitalLoansResponse response = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return response.getLoanId();
    }
}
