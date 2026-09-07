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

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.assertEqualBigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
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
 * The amortization schedule's discount fee, read back through the API, must account for itself exactly.
 *
 * <p>
 * The schedule normalizes a running total rather than each day, so what a day reports is the difference between two
 * rounded totals. That is what keeps the fee from drifting away from itself over a few hundred days, and what these
 * tests hold it to: the fee earned and the fee deferred always add up to the fee, the deferred balance only ever falls,
 * and it closes on nothing.
 *
 * <p>
 * Working capital loans amortize the deferred fee on money in, never on time passed, so the schedule shortens when the
 * borrower pays ahead and lengthens when a day goes by unpaid, and the fee closes either way.
 */
public class FeignWorkingCapitalLoanAmortizationNormalizationTest extends FeignIntegrationTest {

    private static final String DISBURSEMENT_DATE = "01 January 2026";
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal DISCOUNT_FEE = new BigDecimal("1000");
    private static final BigDecimal PAYABLE = new BigDecimal("10000");
    private static final BigDecimal RATE = new BigDecimal("17");

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    /**
     * A loan mostly cleared by one large backdated payment used to strand two cents of the fee unearned: the closing
     * day billed 3.99 against the 4.00 really owed, the deferred fee closed on 0.02, and the schedule accounted for
     * 9999.99 of the 10000 payable.
     */
    @Test
    void testLargeBackdatedPaymentEarnsTheWholeFeeAndTheClosingDayBillsWhatIsOwed() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = createAndDisburseLoan();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("46"), "03 January 2026"));

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("46"), "04 January 2026"));
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("9904"), "02 January 2026"));

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            final List<ProjectedAmortizationSchedulePaymentData> days = instalments(schedule);
            final ProjectedAmortizationSchedulePaymentData closing = days.getLast();

            assertEqualBigDecimal(new BigDecimal("4.00"), closing.getExpectedPaymentAmount(),
                    "the closing day must bill the 4.00 still outstanding, not 3.99");
            assertEqualBigDecimal(BigDecimal.ZERO, closing.getExpectedDiscountFeeBalance(),
                    "the expected deferred fee must close on nothing");
            assertEqualBigDecimal(BigDecimal.ZERO, lastKnown(days).getActualDiscountFeeBalance(),
                    "the actual deferred fee must close on nothing");
            assertEqualBigDecimal(DISCOUNT_FEE, totalActualAmortization(days), "the payments received must earn the whole fee");

            // The loan's own view of what is outstanding and the schedule's must be the same number.
            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(loan.getBalance(), "the loan has no balance");
            assertEqualBigDecimal(loan.getBalance().getPrincipalOutstanding(), lastKnown(days).getActualBalance(),
                    "the schedule's actual balance must be the loan's principal outstanding");

            // Collected plus what is still to be billed is the whole payable. This is the sum that landed on 9999.99.
            assertEqualBigDecimal(PAYABLE, totalCollected(days).add(closing.getExpectedPaymentAmount()),
                    "collected plus the closing bill must account for the whole payable");
        });
    }

    /**
     * On the schedule as written, the fee earned so far and the fee still deferred add up to the fee on every single
     * day - which is what says the reported column has not drifted from the balance it is carried on. Rounding each day
     * instead of the running total is what used to let it drift.
     */
    @Test
    void testOnAnUntouchedScheduleEarnedAndDeferredAddUpToTheFeeEveryDay() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final List<ProjectedAmortizationSchedulePaymentData> days = instalments(
                    wcLoanHelper.getAmortizationSchedule(createAndDisburseLoan()));

            BigDecimal earned = BigDecimal.ZERO;
            for (final ProjectedAmortizationSchedulePaymentData day : days) {
                earned = earned.add(day.getExpectedAmortizationAmount());
                assertEqualBigDecimal(DISCOUNT_FEE, earned.add(day.getExpectedDiscountFeeBalance()),
                        "day " + day.getPaymentNo() + ": the fee earned so far and the fee still deferred must add up to the fee");
            }
            assertEqualBigDecimal(BigDecimal.ZERO, days.getLast().getExpectedDiscountFeeBalance(),
                    "the deferred fee must close on nothing");
        });
    }

    /**
     * Once money has come in, the actual track is the one that must add up: the fee it has earned and the fee it still
     * defers are the same running total read from two ends, and unlike the expected track it is never restated. The
     * expected deferred balance is held to falling and closing on nothing.
     */
    @Test
    void testAfterAPaymentBothTracksStillAccountForTheWholeFee() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = createAndDisburseLoan();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("60"), "03 January 2026"));

            final List<ProjectedAmortizationSchedulePaymentData> days = instalments(wcLoanHelper.getAmortizationSchedule(loanId));

            BigDecimal earnedActual = BigDecimal.ZERO;
            BigDecimal previousDeferred = null;
            for (final ProjectedAmortizationSchedulePaymentData day : days) {
                final String where = "day " + day.getPaymentNo() + ": ";

                if (day.getActualAmortizationAmount() != null) {
                    earnedActual = earnedActual.add(day.getActualAmortizationAmount());
                    assertEqualBigDecimal(DISCOUNT_FEE, earnedActual.add(day.getActualDiscountFeeBalance()),
                            where + "the fee earned by the money in and the fee still deferred must add up to the fee");
                }

                final BigDecimal deferred = day.getExpectedDiscountFeeBalance();
                assertNotNull(deferred, where + "expectedDiscountFeeBalance");
                assertTrue(deferred.signum() >= 0, where + "the deferred fee went negative: " + deferred);
                if (previousDeferred != null) {
                    assertTrue(deferred.compareTo(previousDeferred) <= 0,
                            where + "the deferred fee rose from " + previousDeferred + " to " + deferred);
                }
                previousDeferred = deferred;
            }
            assertEqualBigDecimal(BigDecimal.ZERO, previousDeferred, "the deferred fee must close on nothing");
        });
    }

    /**
     * Repaying the whole payable on the first day earns the whole fee that day, and the schedule is one day long -
     * there is nothing left to schedule. This is the earned-on-money-in rule at its extreme.
     */
    @Test
    void testRepayingInFullOnTheFirstDayEarnsTheWholeFeeThatDay() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = createAndDisburseLoan();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(PAYABLE, "02 January 2026"));

            final List<ProjectedAmortizationSchedulePaymentData> days = instalments(wcLoanHelper.getAmortizationSchedule(loanId));
            assertEquals(1, days.size(), "the loan lasted one day, so the schedule is one day long");

            final ProjectedAmortizationSchedulePaymentData onlyDay = days.getFirst();
            assertEqualBigDecimal(DISCOUNT_FEE, onlyDay.getActualAmortizationAmount(),
                    "the money that closed the loan earned the whole fee");
            assertEqualBigDecimal(BigDecimal.ZERO, onlyDay.getActualDiscountFeeBalance(), "no fee is left deferred");
            assertEqualBigDecimal(BigDecimal.ZERO, onlyDay.getActualBalance(), "nothing is left owing");
        });
    }

    /**
     * Paying ahead takes a day off the end - that day was already covered. It falls out of the schedule running until
     * the loan is square rather than for a counted number of days, and the fee closes on nothing either way.
     */
    @Test
    void testPayingAheadShortensTheSchedule() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final int onSchedule = instalments(wcLoanHelper.getAmortizationSchedule(createAndDisburseLoan())).size();

            final Long paidAhead = createAndDisburseLoan();
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            // Two instalments in one, on the first day.
            wcLoanHelper.makeRepayment(paidAhead, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("94.44"), "02 January 2026"));
            final List<ProjectedAmortizationSchedulePaymentData> ahead = instalments(wcLoanHelper.getAmortizationSchedule(paidAhead));
            assertEquals(onSchedule - 1, ahead.size(), "an extra instalment up front must take a day off the end");
            assertEqualBigDecimal(BigDecimal.ZERO, ahead.getLast().getExpectedDiscountFeeBalance(),
                    "and the fee must still close on nothing");
        });
    }

    /**
     * A rate change re-prices what is still to come; it does not reach back into fee the borrower has already earned.
     * So the fee earned before the change is untouched by it, and the fee still closes on nothing afterwards.
     */
    @Test
    void testARateChangeLeavesEarnedFeeAloneAndTheFeeStillCloses() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = createAndDisburseLoan();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("150"), "03 January 2026"));
            final BigDecimal earnedBefore = totalActualAmortization(instalments(wcLoanHelper.getAmortizationSchedule(loanId)));
            assertTrue(earnedBefore.signum() > 0, "the payment must have earned some fee before the change");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(new BigDecimal("13"), "10 January 2026"));
            assertEqualBigDecimal(earnedBefore, totalActualAmortization(instalments(wcLoanHelper.getAmortizationSchedule(loanId))),
                    "a rate change must not restate fee the borrower has already earned");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-10");
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(new BigDecimal("22"), "10 February 2026"));
            final List<ProjectedAmortizationSchedulePaymentData> days = instalments(wcLoanHelper.getAmortizationSchedule(loanId));
            assertEqualBigDecimal(earnedBefore, totalActualAmortization(days), "nor must a second one");
            assertEqualBigDecimal(BigDecimal.ZERO, days.getLast().getExpectedDiscountFeeBalance(),
                    "the fee must still close on nothing across two rate changes");
        });
    }

    private static List<ProjectedAmortizationSchedulePaymentData> instalments(final ProjectedAmortizationScheduleData schedule) {
        assertNotNull(schedule.getPayments(), "the schedule has no payments");
        return schedule.getPayments().stream().filter(row -> row.getPaymentNo() != null && row.getPaymentNo() > 0).toList();
    }

    /** The last day the schedule knows anything actual about. */
    private static ProjectedAmortizationSchedulePaymentData lastKnown(final List<ProjectedAmortizationSchedulePaymentData> days) {
        return days.stream().filter(row -> row.getActualDiscountFeeBalance() != null).reduce((first, second) -> second).orElseThrow();
    }

    private static BigDecimal totalActualAmortization(final List<ProjectedAmortizationSchedulePaymentData> days) {
        return days.stream().map(ProjectedAmortizationSchedulePaymentData::getActualAmortizationAmount).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal totalCollected(final List<ProjectedAmortizationSchedulePaymentData> days) {
        return days.stream().map(ProjectedAmortizationSchedulePaymentData::getActualPaymentAmount).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long createAndDisburseLoan() {
        final Long clientId = clientHelper.createClient(DISBURSEMENT_DATE);
        final Long productId = createProduct();
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplicationWithDiscount(clientId,
                productId, NET_DISBURSEMENT, RATE, DISBURSEMENT_DATE, DISBURSEMENT_DATE, DISCOUNT_FEE));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approveWithDiscount(DISBURSEMENT_DATE, NET_DISBURSEMENT,
                DISBURSEMENT_DATE, DISCOUNT_FEE));
        wcLoanHelper.disburse(loanId,
                WorkingCapitalLoanRequestBuilders.disburseWithDiscount(DISBURSEMENT_DATE, NET_DISBURSEMENT, DISCOUNT_FEE));
        return loanId;
    }

    private Long createProduct() {
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder()
                .withName("WCL Norm " + unique()).withShortName(UUID.randomUUID().toString().replace("-", "").substring(0, 4))
                .withRepaymentEvery(1).withRepaymentFrequencyType("DAYS").withPeriodPaymentRate(RATE)
                .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private static String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
