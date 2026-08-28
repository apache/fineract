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
package org.apache.fineract.integrationtests;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
@Order(1)
public class WorkingCapitalLoanDiscountFeeAmortizationDriftTest {

    private static final DateTimeFormatter BUSINESS_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final String STATUS_ACTIVE = "loanStatusType.active";

    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal TOTAL_PAYMENT_VOLUME = new BigDecimal("100000");
    private static final BigDecimal PERIOD_PAYMENT_RATE = new BigDecimal("17");
    private static final BigDecimal DISCOUNT_FEE = new BigDecimal("1000");
    private static final BigDecimal DAILY_PAYMENT = new BigDecimal("47.22");
    private static final int TERM_DAYS = 212;
    private static final int MAX_DAYS = TERM_DAYS + 15;
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 1, 1);

    /**
     * The fee earned by each of the first six repayments of the over-payment case. Each is the increment of the
     * running, capped amortization total, which is what keeps the loan and the schedule on the same number.
     *
     * <p>
     * Close of business settles the previous day, so the fee a repayment earns is observed on the day after it was made
     * and the first of these lands on day two. The values do not depend on that ordering - a repayment earns the same
     * fee whenever the day it belongs to is closed.
     *
     * <p>
     * A cent may sit on a different day than in a schedule that rounds the cumulative total instead of accumulating
     * rounded periods - 8.92 / 8.88 here rather than 8.91 / 8.89 - but the two conventions agree on the cumulative
     * figure by day 6 (54.41), and every property asserted below holds under either. Do not "correct" a single day here
     * without checking the cumulative column along with it.
     */
    private static final String[] EXPECTED_AMORTIZATION_OVERPAY = { "9.61", "9.04", "9.00", "8.96", "8.92", "8.88" };
    private static final String[] EXPECTED_AMORTIZATION_UNDERPAY = { "7.69", "9.05", "9.00", "8.97", "8.93", "8.89" };

    private final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private final Long createdClientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

    @AfterEach
    void cleanupEntities() {
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            bestEffort(() -> loanHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest()));
            bestEffort(() -> loanHelper.undoApprovalById(loanId, new PostWorkingCapitalLoansLoanIdRequest()));
            bestEffort(() -> loanHelper.deleteById(loanId));
        }
        createdLoanIds.clear();
        for (final Long productId : createdProductIds) {
            if (productId == null) {
                continue;
            }
            bestEffort(() -> productHelper.deleteWorkingCapitalLoanProductById(productId));
        }
        createdProductIds.clear();
    }

    private static void bestEffort(final Runnable action) {
        try {
            action.run();
        } catch (final CallFailedRuntimeException ignored) {
            // cleanup only
        }
    }

    /** A first repayment 2.78 over the daily payment, then the daily payment until the loan is settled. */
    @Test
    public void testAmortizationStaysInStepWhenFirstRepaymentExceedsTheDailyPayment() {
        repayDailyAndAssertNoDrift(new BigDecimal("50"), EXPECTED_AMORTIZATION_OVERPAY);
    }

    /** The same in the opposite direction: a first repayment 7.22 short of the daily payment. */
    @Test
    public void testAmortizationStaysInStepWhenFirstRepaymentIsBelowTheDailyPayment() {
        repayDailyAndAssertNoDrift(new BigDecimal("40"), EXPECTED_AMORTIZATION_UNDERPAY);
    }

    private void repayDailyAndAssertNoDrift(final BigDecimal firstRepayment, final String[] expectedFirstAmortizations) {
        final Long loanId = createDisbursedLoan();

        BigDecimal previousRealized = BigDecimal.ZERO;
        int lastRepaidDay = 0;
        boolean settled = false;

        for (int day = 1; day <= MAX_DAYS && !settled; day++) {
            final LocalDate businessDate = DISBURSEMENT_DATE.plusDays(day);

            // Close of business is the first thing that happens on a business date: it settles the day before. What it
            // posts is therefore the fee earned by the repayment made yesterday, and on day one there is no yesterday.
            runCloseOfBusiness(loanId, businessDate);

            final LocalDate lastRepaymentDate = day > 1 ? DISBURSEMENT_DATE.plusDays(day - 1L) : null;
            final BigDecimal realized = assertLoanAndScheduleAgree(loanId, lastRepaymentDate, day);
            assertTrue(realized.compareTo(previousRealized) >= 0,
                    "day " + day + ": realized income moved backwards (from " + previousRealized + " to " + realized + ")");
            final int repaymentDay = day - 1;
            if (repaymentDay == 0) {
                assertEqual(BigDecimal.ZERO, realized, "day 1: nothing had been repaid, so nothing can have been earned");
            } else if (repaymentDay <= expectedFirstAmortizations.length) {
                assertEqual(new BigDecimal(expectedFirstAmortizations[repaymentDay - 1]), realized.subtract(previousRealized),
                        "day " + day + ": the fee earned by the repayment of day " + repaymentDay);
            }
            previousRealized = realized;

            // Never pay more than is owed: the subject here is the amortization column, not overpayment handling.
            final BigDecimal repayment = (day == 1 ? firstRepayment : DAILY_PAYMENT).min(totalOutstanding(loanId));
            assertTrue(repayment.signum() > 0, "day " + day + ": nothing left to repay although the loan is not settled");
            final int currentDay = day;
            BusinessDateHelper.runAt(businessDate.format(BUSINESS_DATE),
                    () -> loanHelper.makeRepaymentByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder
                            .buildRepaymentRequest(businessDate, repayment, null, "drift-day-" + currentDay, 1, "repayment-account")));
            lastRepaidDay = day;

            final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId);
            assertNotNull(loanData.getStatus(), "day " + day + ": loan has no status");
            // The repayment that settles the loan is the last one COB will ever see it for, so no close of business is
            // left to post what that repayment earned. The closing recognition does it, and is asserted below.
            settled = !STATUS_ACTIVE.equals(loanData.getStatus().getCode());
        }

        assertTrue(settled, "the loan was still active after " + lastRepaidDay + " daily repayments");
        assertFinalStateIsSettled(loanId, previousRealized, lastRepaidDay);
    }

    private void runCloseOfBusiness(final Long loanId, final LocalDate businessDate) {
        BusinessDateHelper.runAt(businessDate.format(BUSINESS_DATE), () -> ok(() -> FineractFeignClientHelper.getFineractFeignClient()
                .inlineJob().executeInlineJob("WC_LOAN_COB", new InlineJobRequest().addLoanIdsItem(loanId))));
    }

    private BigDecimal assertLoanAndScheduleAgree(final Long loanId, final LocalDate lastRepaymentDate, final int day) {
        final GetBalance balance = balanceOf(loanId);
        final BigDecimal realized = balance.getRealizedIncomeFromDiscountFee();
        final BigDecimal unrealized = balance.getUnrealizedIncomeFromDiscountFee();
        assertNotNull(realized, "day " + day + ": realizedIncomeFromDiscountFee");
        assertNotNull(unrealized, "day " + day + ": unrealizedIncomeFromDiscountFee");

        assertTrue(realized.compareTo(DISCOUNT_FEE) <= 0,
                "day " + day + ": realized income " + realized + " exceeds the discount fee " + DISCOUNT_FEE);
        assertEqual(DISCOUNT_FEE, realized.add(unrealized),
                "day " + day + ": realized (" + realized + ") + unrealized (" + unrealized + ") must be the discount fee");

        final ProjectedAmortizationScheduleData schedule = loanHelper.retrieveAmortizationScheduleByLoanIdRaw(loanId);
        assertNotNull(schedule.getPayments(), "day " + day + ": schedule has no payments");

        final BigDecimal scheduleAmortizationTotal = instalments(schedule)
                .map(ProjectedAmortizationSchedulePaymentData::getActualAmortizationAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEqual(realized, scheduleAmortizationTotal,
                "day " + day + ": the schedule's amortization column must sum to the income the loan recognized");

        assertEqual(DISCOUNT_FEE.subtract(realized), lastActualDiscountFeeBalance(schedule),
                "day " + day + ": the schedule's deferred discount fee balance must be the fee less what the loan recognized");

        // The last repaid day must carry an actual amortization of its own by now.
        if (lastRepaymentDate != null) {
            assertTrue(
                    instalments(schedule)
                            .anyMatch(row -> lastRepaymentDate.equals(row.getPaymentDate()) && row.getActualAmortizationAmount() != null),
                    "day " + day + ": no schedule row carries an actual amortization for " + lastRepaymentDate);
        }

        return realized;
    }

    /** The deferred fee as the schedule reports it: the balance carried by the last row that has actual figures. */
    private static BigDecimal lastActualDiscountFeeBalance(final ProjectedAmortizationScheduleData schedule) {
        return instalments(schedule).filter(row -> row.getActualDiscountFeeBalance() != null).reduce((first, second) -> second)
                .map(ProjectedAmortizationSchedulePaymentData::getActualDiscountFeeBalance).orElse(DISCOUNT_FEE);
    }

    /** The real periods of the schedule, i.e. everything but the disbursement row. */
    private static Stream<ProjectedAmortizationSchedulePaymentData> instalments(final ProjectedAmortizationScheduleData schedule) {
        assertNotNull(schedule.getPayments());
        return schedule.getPayments().stream().filter(row -> row.getPaymentNo() != null && row.getPaymentNo() > 0);
    }

    /**
     * Once the loan is settled the whole fee - and only the fee - has been recognized, and the last few cents were
     * settled by an amortization rather than by an adjustment of something that was never adjusted.
     *
     * <p>
     * The adjustment is caught by the direction of the move rather than by scanning the transaction list, which is
     * paged at fifty rows with no page parameter on the generated client and would only ever show the first few weeks.
     * The service posts an amortization when the amount it needs to recognize is positive and an adjustment when it is
     * negative, so realized income moving forward on every day of the loan's life - asserted for each active day, and
     * here across closure - is what says no adjustment was ever generated. {@code totalDiscountFeeAdjustment} confirms
     * the premise: there was no discount fee adjustment that could have justified one.
     */
    private void assertFinalStateIsSettled(final Long loanId, final BigDecimal realizedOnLastActiveDay, final int lastRepaidDay) {
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId);
        assertNotNull(loanData.getStatus());
        assertEquals("loanStatusType.closed.obligations.met", loanData.getStatus().getCode(),
                "the loan should be settled after " + lastRepaidDay + " daily repayments");

        final GetBalance balance = balanceOf(loanId);
        final BigDecimal realized = balance.getRealizedIncomeFromDiscountFee();
        assertEqual(DISCOUNT_FEE, realized,
                "the whole discount fee must be recognized at closure (the schedule had it at " + realizedOnLastActiveDay + ")");
        assertEqual(BigDecimal.ZERO, balance.getUnrealizedIncomeFromDiscountFee(), "nothing may stay deferred at closure");
        assertEqual(BigDecimal.ZERO, zeroIfNull(balance.getTotalDiscountFeeAdjustment()),
                "no discount fee adjustment was made, so nothing could justify an amortization adjustment");
        assertNotNull(realized);
        assertTrue(realized.compareTo(realizedOnLastActiveDay) >= 0, "closing the loan took recognized income backwards, from "
                + realizedOnLastActiveDay + " to " + realized + ", which means it posted an amortization adjustment");
    }

    private static BigDecimal zeroIfNull(final BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal totalOutstanding(final Long loanId) {
        final BigDecimal outstanding = balanceOf(loanId).getTotalOutstanding();
        return outstanding == null ? BigDecimal.ZERO : outstanding;
    }

    private GetBalance balanceOf(final Long loanId) {
        final GetBalance balance = loanHelper.retrieveById(loanId).getBalance();
        assertNotNull(balance, "loan " + loanId + " has no balance");
        return balance;
    }

    private Long createDisbursedLoan() {
        final AtomicLong loanIdRef = new AtomicLong();
        BusinessDateHelper.runAt(DISBURSEMENT_DATE.format(BUSINESS_DATE), () -> {
            final Long productId = createProduct();
            final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                    .withProductId(productId).withPrincipal(NET_DISBURSEMENT).withPeriodPaymentRate(PERIOD_PAYMENT_RATE)
                    .withTotalPaymentVolume(TOTAL_PAYMENT_VOLUME).withDiscount(DISCOUNT_FEE).withSubmittedOnDate(DISBURSEMENT_DATE)
                    .buildSubmitRequest());
            loanHelper.approveById(loanId,
                    WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(DISBURSEMENT_DATE, NET_DISBURSEMENT, DISCOUNT_FEE));
            loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(DISBURSEMENT_DATE,
                    NET_DISBURSEMENT, DISCOUNT_FEE, null, null, null, null, null, null, null));
            loanIdRef.set(loanId);
        });

        final Long loanId = loanIdRef.get();
        final ProjectedAmortizationScheduleData schedule = loanHelper.retrieveAmortizationScheduleByLoanIdRaw(loanId);
        assertEqual(DISCOUNT_FEE, schedule.getDiscountFeeAmount(), "discount fee on the schedule");
        assertEqual(NET_DISBURSEMENT, schedule.getNetDisbursementAmount(), "net disbursement on the schedule");
        assertEqual(TOTAL_PAYMENT_VOLUME, schedule.getTotalPaymentVolume(), "total payment volume on the schedule");
        assertEqual(PERIOD_PAYMENT_RATE, schedule.getPeriodPaymentRate(), "period payment rate on the schedule");
        assertEqual(DAILY_PAYMENT, schedule.getExpectedPaymentAmount(), "daily payment on the schedule");
        assertEquals(360, schedule.getNpvDayCount(), "NPV day count on the schedule");
        assertEquals(TERM_DAYS, schedule.getOriginalPaymentNumber(), "term of the schedule");
        return loanId;
    }

    private Long createProduct() {
        final String uniqueName = "WCL Drift " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withRepaymentEvery(1).withRepaymentFrequencyType("DAYS")
                .withPeriodPaymentRate(PERIOD_PAYMENT_RATE).withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long submitAndTrack(final PostWorkingCapitalLoansRequest request) {
        final Long loanId = loanHelper.submit(request);
        createdLoanIds.add(loanId);
        return loanId;
    }

    private static void assertEqual(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertNotNull(actual, message + " (was null, expected " + expected + ")");
        assertEquals(0, expected.compareTo(actual), message + " - expected " + expected + " but was " + actual);
    }
}
