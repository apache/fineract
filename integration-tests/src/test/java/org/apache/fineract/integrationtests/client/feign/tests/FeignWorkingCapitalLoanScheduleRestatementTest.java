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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The projected schedule is restated from what the borrower actually owes rather than from the instalments the plan
 * assumed would arrive. These tests pin the three things that follow from that: a loan paid to schedule is projected
 * exactly as it was at disbursement, a loan running late has its remaining periods re-projected from its real position,
 * and a rate change only ever re-rates what is genuinely left to bill.
 *
 * <p>
 * The loan is deliberately tiny — 1000 net, 100 discount, 18% of a 100000 payment volume over 360 days, so 50 a day
 * over a 22 day term — which makes the whole schedule small enough to read in a failure message.
 */
public class FeignWorkingCapitalLoanScheduleRestatementTest extends FeignIntegrationTest {

    private static final BigDecimal PRINCIPAL = new BigDecimal("1000");
    private static final BigDecimal DISCOUNT = new BigDecimal("100");
    /** Everything the borrower ever owes: the net disbursement plus the discount fee. */
    private static final BigDecimal TOTAL_OWED = new BigDecimal("1100");
    private static final BigDecimal ORIGINAL_RATE = new BigDecimal("18");
    private static final BigDecimal RAISED_RATE = new BigDecimal("20");
    /** (100000 × 18%) / 360 — the daily payment the base schedule bills. */
    private static final BigDecimal DAILY_PAYMENT = new BigDecimal("50");
    /** (100000 × 20%) / 360 — what a period bills once the raised rate takes effect. */
    private static final BigDecimal RAISED_DAILY_PAYMENT = new BigDecimal("55.56");
    /** Rounding slack: each period is settled to the currency scale, so totals can land a few cents out. */
    private static final BigDecimal TOLERANCE = new BigDecimal("0.05");

    private static final String DISBURSEMENT_DATE = "01 January 2026";
    private static final int BASE_TERM = 22;

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    /**
     * The safety property the whole restatement rests on: while the borrower pays what was asked when it was asked,
     * reality and the plan agree, so the projection must come back byte for byte as it was at disbursement. Anything
     * that moves here is a regression for every healthy loan on the book.
     */
    @Test
    void testRepaymentsOnScheduleLeaveTheProjectionUntouched() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();
            final ProjectedAmortizationScheduleData atDisbursement = wcLoanHelper.getAmortizationSchedule(loanId);

            for (int dayOfMonth = 2; dayOfMonth <= 6; dayOfMonth++) {
                businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-0" + dayOfMonth);
                wcLoanHelper.makeRepayment(loanId,
                        WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "0" + dayOfMonth + " January 2026"));
            }

            final ProjectedAmortizationScheduleData afterPaying = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEquals(periods(atDisbursement).size(), periods(afterPaying).size(), "paying to schedule must not change its length");
            assertProjectionMatchesThrough(atDisbursement, afterPaying, periods(atDisbursement).size(), "after five on-time repayments");
        });
    }

    /**
     * Two days elapse with nothing paid and the third is settled. The periods that fell due before the payment keep the
     * schedule they were written with — nothing was known about them at the time, and a payment arriving later says
     * nothing about them — while the periods after it are re-projected from the balance really outstanding. The loan is
     * two instalments behind, so it simply runs two days later than planned.
     */
    @Test
    void testRepaymentRestatesTheProjectionFromWhatIsReallyOwed() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();
            final ProjectedAmortizationScheduleData atDisbursement = wcLoanHelper.getAmortizationSchedule(loanId);

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "04 January 2026"));
            final ProjectedAmortizationScheduleData restated = wcLoanHelper.getAmortizationSchedule(loanId);

            assertProjectionMatchesThrough(atDisbursement, restated, 3, "up to and including the day the payment landed");

            // Being two instalments behind, every period from here repeats what the period two days earlier said.
            assertCloseTo(balanceOf(atDisbursement, 2), balanceOf(restated, 4), "period 4 must repeat the balance of period 2");
            assertCloseTo(feeBalanceOf(atDisbursement, 2), feeBalanceOf(restated, 4), "period 4 must repeat the fee balance of period 2");

            assertTrue(periods(restated).size() > BASE_TERM, "two missed instalments must push the schedule out");
            assertScheduleClosesCleanly(restated, "after a repayment two days late");
        });
    }

    /**
     * A rate change re-rates what is still outstanding and nothing more. Raising the rate pays the loan down faster, so
     * the schedule shortens, but the money billed across it is the same debt — it cannot re-bill the periods that came
     * before the change.
     */
    @Test
    void testInTermRateChangeBillsOnlyWhatIsLeftToBill() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();
            assertBillsWhatIsOwed(wcLoanHelper.getAmortizationSchedule(loanId), "before any rate change");

            // 15 January is period 14 of a 22 day term, so the change lands with eight periods still to run.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(RAISED_RATE, "15 January 2026"));

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEquals(0, DAILY_PAYMENT.compareTo(paymentOf(schedule, 13)), "the day before must still bill the original rate");
            assertEquals(0, RAISED_DAILY_PAYMENT.compareTo(paymentOf(schedule, 14)), "the new rate must take effect on its own date");
            assertBillsWhatIsOwed(schedule, "after an in-term rate change");
            assertScheduleClosesCleanly(schedule, "after an in-term rate change");
        });
    }

    /**
     * A repayment made after a rate change. The rate change rewrote the plan but moved no money, so the actual balance
     * carries straight through it and the repayment comes off what the borrower really owed — not off the much smaller
     * figure the plan had assumed by then. The periods after the repayment are then re-projected from that real
     * balance.
     */
    @Test
    void testRepaymentAfterRateChangeComesOffTheRealBalance() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "04 January 2026"));
            final BigDecimal owedBeforeRateChange = actualBalanceOf(wcLoanHelper.getAmortizationSchedule(loanId), 3);

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-20");
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(RAISED_RATE, "20 January 2026"));

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-21");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(RAISED_DAILY_PAYMENT, "21 January 2026"));

            // Read once at the end: a period only reports an actual balance after the current date has passed it, so
            // 20 January has nothing to say until the 21st has been recorded.
            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertCloseTo(owedBeforeRateChange, actualBalanceOf(schedule, 19),
                    "a rate change moves no money, so the actual balance must carry through it");
            final ProjectedAmortizationSchedulePaymentData paidPeriod = period(schedule, 20);
            // What is left is what was owed, less the money handed over, plus the fee that payment earned back onto it.
            final BigDecimal expected = owedBeforeRateChange.subtract(RAISED_DAILY_PAYMENT).add(paidPeriod.getActualAmortizationAmount());
            assertCloseTo(expected, paidPeriod.getActualBalance(), "the repayment must come off the balance really outstanding");

            assertScheduleClosesCleanly(schedule, "after a repayment following a rate change");
        });
    }

    /**
     * Everything the schedule plans to bill adds up to the debt. Only meaningful while no instalment has been missed:
     * once periods fall due unpaid the schedule re-presents that money on later dates, and the total legitimately
     * exceeds the debt by the arrears.
     */
    private void assertBillsWhatIsOwed(final ProjectedAmortizationScheduleData schedule, final String context) {
        BigDecimal billed = BigDecimal.ZERO;
        for (final ProjectedAmortizationSchedulePaymentData period : periods(schedule)) {
            if (period.getExpectedPaymentAmount() != null) {
                billed = billed.add(period.getExpectedPaymentAmount());
            }
        }
        assertTrue(TOTAL_OWED.subtract(billed).abs().compareTo(TOLERANCE) <= 0, "the schedule must bill the " + TOTAL_OWED + " owed "
                + context + ", but billed " + billed + " over " + periods(schedule).size() + " periods\n" + render(schedule));
    }

    /**
     * The schedule runs out exactly when the debt does: the last period clears the balance and the deferred fee
     * together, and nothing is billed beyond that. A period billing against a balance already at zero is asking for
     * money with nothing standing behind it.
     */
    private void assertScheduleClosesCleanly(final ProjectedAmortizationScheduleData schedule, final String context) {
        final List<ProjectedAmortizationSchedulePaymentData> periods = periods(schedule);
        boolean cleared = false;
        for (final ProjectedAmortizationSchedulePaymentData period : periods) {
            if (cleared) {
                final BigDecimal billed = period.getExpectedPaymentAmount();
                assertTrue(billed == null || billed.signum() == 0, "period " + period.getPaymentNo() + " bills " + billed
                        + " although the balance already reached zero " + context + "\n" + render(schedule));
            }
            cleared = cleared || (period.getExpectedBalance() != null && period.getExpectedBalance().signum() == 0);
        }
        final ProjectedAmortizationSchedulePaymentData last = periods.getLast();
        assertEquals(0, last.getExpectedBalance().signum(),
                "the schedule must end on a cleared balance " + context + "\n" + render(schedule));
        assertEquals(0, last.getExpectedDiscountFeeBalance().signum(),
                "the fee must be fully earned by the time the balance clears " + context + "\n" + render(schedule));
    }

    /** Every projected column of periods 1..{@code throughPeriod} must be identical in both schedules. */
    private void assertProjectionMatchesThrough(final ProjectedAmortizationScheduleData before,
            final ProjectedAmortizationScheduleData after, final int throughPeriod, final String context) {
        for (int periodNo = 1; periodNo <= throughPeriod; periodNo++) {
            final ProjectedAmortizationSchedulePaymentData expected = period(before, periodNo);
            final ProjectedAmortizationSchedulePaymentData actual = period(after, periodNo);
            assertEquals(0, expected.getExpectedPaymentAmount().compareTo(actual.getExpectedPaymentAmount()),
                    "period " + periodNo + " payment must not move " + context + "\n" + render(after));
            assertEquals(0, expected.getExpectedBalance().compareTo(actual.getExpectedBalance()),
                    "period " + periodNo + " balance must not move " + context + "\n" + render(after));
            assertEquals(0, expected.getExpectedDiscountFeeBalance().compareTo(actual.getExpectedDiscountFeeBalance()),
                    "period " + periodNo + " fee balance must not move " + context + "\n" + render(after));
        }
    }

    private static void assertCloseTo(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertTrue(actual != null && expected.subtract(actual).abs().compareTo(TOLERANCE) <= 0,
                message + " — expected " + expected + " but was " + actual);
    }

    private static ProjectedAmortizationSchedulePaymentData period(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return periods(schedule).stream().filter(payment -> payment.getPaymentNo() == periodNo).findFirst()
                .orElseThrow(() -> new AssertionError("no period " + periodNo + " in\n" + render(schedule)));
    }

    private static BigDecimal balanceOf(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return period(schedule, periodNo).getExpectedBalance();
    }

    private static BigDecimal feeBalanceOf(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return period(schedule, periodNo).getExpectedDiscountFeeBalance();
    }

    private static BigDecimal paymentOf(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return period(schedule, periodNo).getExpectedPaymentAmount();
    }

    private static BigDecimal actualBalanceOf(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return period(schedule, periodNo).getActualBalance();
    }

    /** Repayment periods only — the disbursement row carries a negative amount and the opening balance. */
    private static List<ProjectedAmortizationSchedulePaymentData> periods(final ProjectedAmortizationScheduleData schedule) {
        return schedule.getPayments().stream().filter(payment -> payment.getPaymentNo() != null && payment.getPaymentNo() > 0).toList();
    }

    private static String render(final ProjectedAmortizationScheduleData schedule) {
        final StringBuilder out = new StringBuilder("schedule:\n");
        for (final ProjectedAmortizationSchedulePaymentData payment : schedule.getPayments()) {
            out.append("  #").append(payment.getPaymentNo()).append(' ').append(payment.getPaymentDate()).append(" payment=")
                    .append(payment.getExpectedPaymentAmount()).append(" balance=").append(payment.getExpectedBalance())
                    .append(" discountFeeBalance=").append(payment.getExpectedDiscountFeeBalance()).append(" actualPayment=")
                    .append(payment.getActualPaymentAmount()).append(" actualBalance=").append(payment.getActualBalance()).append('\n');
        }
        return out.toString();
    }

    private Long disburseLoan() {
        final Long clientForTest = clientHelper.createClient(DISBURSEMENT_DATE);
        final Long productId = createProductAllowingDiscount();
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders
                .submitApplication(clientForTest, productId, PRINCIPAL, ORIGINAL_RATE, DISBURSEMENT_DATE, DISBURSEMENT_DATE)
                .discount(DISCOUNT));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId,
                WorkingCapitalLoanRequestBuilders.approveWithDiscount(DISBURSEMENT_DATE, PRINCIPAL, DISBURSEMENT_DATE, DISCOUNT));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount(DISBURSEMENT_DATE, PRINCIPAL, DISCOUNT));
        return loanId;
    }

    private Long createProductAllowingDiscount() {
        final Long productId = productHelper.createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName("WCL Restate " + UUID.randomUUID().toString().substring(0, 8))
                        .withShortName(UUID.randomUUID().toString().replace("-", "").substring(0, 4))
                        .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
