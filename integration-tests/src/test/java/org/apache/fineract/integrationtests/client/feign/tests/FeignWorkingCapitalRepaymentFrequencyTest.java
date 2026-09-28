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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.assertEqualBigDecimal;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.errorCodesOf;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.assertActualsNotYetKnown;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.assertNoNegativeAmounts;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.assertNoRowDated;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.assertSameExpectedRows;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.assertSameRows;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.paymentByNo;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.totalExpectedAmortization;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.validateActuals;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.validatePayment;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.validatePaymentDate;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.validateScheduleShape;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanPeriodPaymentRateChangeData;
import org.apache.fineract.integrationtests.client.feign.FeignWorkingCapitalTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.WorkingCapitalLoanCommandsApi;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The projected amortization schedule follows the product's (or the loan's) repayment frequency type and interval.
 * Reference loan throughout: net 9000, discount fee 1000, TPV 100000, 18 %, npv 360, disbursed 2026-01-01.
 * <p>
 * Non-daily amounts follow the per-period reading of the TPV payment and the EIR (payment and annualisation scale with
 * the period length: days over npvDayCount for DAYS/WEEKS, months over 12 for MONTHS).
 */
public class FeignWorkingCapitalRepaymentFrequencyTest extends FeignWorkingCapitalTestBase {

    private static final String BUSINESS_DATE = "2026-01-01";
    private static final String DISBURSE_DATE = "01 January 2026";
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal DISCOUNT_FEE = new BigDecimal("1000");
    private static final BigDecimal TPV_RATE = new BigDecimal("18");
    private static final int NPV_DAY_COUNT = 360;

    private static final String DAYS = "DAYS";
    private static final String WEEKS = "WEEKS";
    private static final String MONTHS = "MONTHS";
    private static final String YEARS = "YEARS";

    private static final String WCLP = "validation.msg.WORKINGCAPITALLOANPRODUCT.";
    private static final String WCL = "validation.msg.WORKINGCAPITALLOAN.";

    private static final String WEEKLY_ANNUAL_EIR = "45.146503";

    private final List<Long> createdLoanIds = new ArrayList<>();

    @AfterAll
    void cleanupRepaymentFrequencyLoans() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
    }

    @Test
    @DisplayName("DAYS/1 keeps the daily schedule — 200 x 50.00 from 2026-01-02 to 2026-07-20, annual EIR 46.845102")
    void daily_everyOneDay_keepsTheDailySchedule() {
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqD1", DAYS, 1), DISBURSE_DATE);

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            validateScheduleShape(schedule, "50.00", 200);
            validatePayment(schedule, 0, "2026-01-01", "-9000.00", "9000.00", null, "1000.00");
            validatePayment(schedule, 1, "2026-01-02", "50.00", "8959.61", "9.61", "990.39");
            validatePayment(schedule, 2, "2026-01-03", "50.00", "8919.18", "9.57", "980.82");
            validatePayment(schedule, 199, "2026-07-19", "50.00", "49.95", "0.11", "0.05");
            validatePayment(schedule, 200, "2026-07-20", "50.00", "0.00", "0.05", "0.00");
            assertNoNegativeAmounts(schedule);

            validateLoanTerms(loanId, "50.00", 200, "46.845102");
        });
    }

    @Test
    @DisplayName("WEEKS/1 — 28 x 350.00 + 200.00 weekly from 2026-01-08 to 2026-07-23, annual EIR 45.146503")
    void weekly_everyOneWeek_datesAndAmountsFollowTheWeek() {
        assertApiOffersWeeklyFrequency();
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqW1", WEEKS, 1), DISBURSE_DATE);

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertWeeklyOneReferenceSchedule(schedule);
            validateLoanTerms(loanId, "350.00", 29, WEEKLY_ANNUAL_EIR);
        });
    }

    @Test
    @DisplayName("DAYS/7 schedules exactly like WEEKS/1 — same dates, payments, balances and annual EIR")
    void everySevenDays_isTheSameScheduleAsWeekly() {
        runAt(BUSINESS_DATE, () -> {
            final Long sevenDayLoanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqD7", DAYS, 7), DISBURSE_DATE);
            final ProjectedAmortizationScheduleData sevenDays = wcLoanHelper.getAmortizationSchedule(sevenDayLoanId);

            assertWeeklyOneReferenceSchedule(sevenDays);
            validateLoanTerms(sevenDayLoanId, "350.00", 29, WEEKLY_ANNUAL_EIR);

            assertApiOffersWeeklyFrequency();
            final Long weeklyLoanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqW1eq", WEEKS, 1), DISBURSE_DATE);
            assertSameExpectedRows(wcLoanHelper.getAmortizationSchedule(weeklyLoanId), sevenDays);
        });
    }

    @Test
    @DisplayName("WEEKS/2 — 14 x 700.00 + 200.00 fortnightly from 2026-01-15 to 2026-07-30, annual EIR 43.297294")
    void weekly_everyTwoWeeks_intervalMultipliesThePeriod() {
        assertApiOffersWeeklyFrequency();
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqW2", WEEKS, 2), DISBURSE_DATE);

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            validateScheduleShape(schedule, "700.00", 15);
            validatePayment(schedule, 1, "2026-01-15", "700.00", "8426.80", "126.80", "873.20");
            validatePayment(schedule, 2, "2026-01-29", "700.00", "7845.52", "118.72", "754.48");
            validatePayment(schedule, 15, "2026-07-30", "200.00", "0.00", "2.78", "0.00");
            assertEqualBigDecimal(DISCOUNT_FEE, totalExpectedAmortization(schedule), "the schedule must earn exactly the discount fee");
            assertNoNegativeAmounts(schedule);

            validateLoanTerms(loanId, "700.00", 15, "43.297294");
        });
    }

    @Test
    @DisplayName("MONTHS/1 — 6 x 1500.00 + 1000.00 monthly from 2026-02-01 to 2026-08-01, annual EIR 39.495353")
    void monthly_everyOneMonth_datesAndAmountsFollowTheMonth() {
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqM1", MONTHS, 1), DISBURSE_DATE);

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            validateScheduleShape(schedule, "1500.00", 7);
            validatePayment(schedule, 1, "2026-02-01", "1500.00", "7753.14", "253.14", "746.86");
            validatePayment(schedule, 2, "2026-03-01", "1500.00", "6471.21", "218.07", "528.79");
            validatePayment(schedule, 6, "2026-07-01", "1500.00", "972.64", "67.64", "27.36");
            validatePayment(schedule, 7, "2026-08-01", "1000.00", "0.00", "27.36", "0.00");
            assertEqualBigDecimal(DISCOUNT_FEE, totalExpectedAmortization(schedule), "the schedule must earn exactly the discount fee");
            assertNoNegativeAmounts(schedule);

            validateLoanTerms(loanId, "1500.00", 7, "39.495353");
        });
    }

    @Test
    @DisplayName("MONTHS/1 from 2026-01-31 keeps the month-end anchor — 02-28, 03-31, 04-30 ... 08-31, never drifting to the 28th")
    void monthly_disbursedOnMonthEnd_anchorsEveryDueDateToTheMonthEnd() {
        runAt("2026-01-31", () -> {
            final Long loanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqM1End", MONTHS, 1), "31 January 2026");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            validateScheduleShape(schedule, "1500.00", 7);
            validatePayment(schedule, 1, "2026-02-28", "1500.00", "7753.14", "253.14", "746.86");
            validatePayment(schedule, 2, "2026-03-31", "1500.00", "6471.21", "218.07", "528.79");
            validatePaymentDate(schedule, 3, "2026-04-30");
            validatePaymentDate(schedule, 4, "2026-05-31");
            validatePaymentDate(schedule, 5, "2026-06-30");
            validatePayment(schedule, 6, "2026-07-31", "1500.00", "972.64", "67.64", "27.36");
            validatePayment(schedule, 7, "2026-08-31", "1000.00", "0.00", "27.36", "0.00");
        });
    }

    @Test
    @DisplayName("a loan-level MONTHS/2 override on a DAYS/1 product drives the schedule — 3 x 3000.00 + 1000.00, 2026-03-01 .. 2026-09-01")
    void loanLevelOverride_monthsEveryTwo_isHonouredOverTheProductDefault() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createProduct(
                    tpvProductBuilder("FqOverride", DAYS, 1).withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE,
                            "periodPaymentFrequency", Boolean.TRUE, "periodPaymentFrequencyType", Boolean.TRUE)).build());
            final Long loanId = submitApproveAndDisburse(WorkingCapitalLoanRequestBuilders.submitApplicationWithFrequency(clientId,
                    productId, NET_DISBURSEMENT, TPV_RATE, DISBURSE_DATE, DISBURSE_DATE, DISCOUNT_FEE, 2, MONTHS), DISBURSE_DATE);

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(2, loan.getRepaymentEvery(), "loan repaymentEvery must carry the override");
            assertNotNull(loan.getRepaymentFrequencyType(), "loan repaymentFrequencyType");
            assertEquals(MONTHS, loan.getRepaymentFrequencyType().getCode(), "loan repaymentFrequencyType must carry the override");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            validateScheduleShape(schedule, "3000.00", 4);
            validatePayment(schedule, 1, "2026-03-01", "3000.00", "6446.32", "446.32", "553.68");
            validatePaymentDate(schedule, 2, "2026-05-01");
            validatePaymentDate(schedule, 3, "2026-07-01");
            validatePayment(schedule, 4, "2026-09-01", "1000.00", "0.00", "47.25", "0.00");

            validateLoanTerms(loanId, "3000.00", 4, "33.696456");
        });
    }

    @Test
    @DisplayName("PAYMENT_AMOUNT 350.00 on WEEKS/1 is the per-week payment — same schedule as TPV 18 % weekly, annual EIR 45.146503")
    void paymentAmountStrategy_weekly_paymentAmountIsPerPeriod() {
        assertApiOffersWeeklyFrequency();
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createProduct(baseProductBuilder("FqPaW1", WEEKS, 1).withPeriodPaymentRate(null)
                    .withPaymentAmountCalculationStrategy("PAYMENT_AMOUNT").withPaymentAmount(new BigDecimal("350.00")).build());
            final Long loanId = submitApproveAndDisburse(WorkingCapitalLoanRequestBuilders.submitPaymentAmountApplication(clientId,
                    productId, NET_DISBURSEMENT, DISCOUNT_FEE, null, DISBURSE_DATE, DISBURSE_DATE), DISBURSE_DATE);

            assertWeeklyOneReferenceSchedule(wcLoanHelper.getAmortizationSchedule(loanId));
            validateLoanTerms(loanId, "350.00", 29, WEEKLY_ANNUAL_EIR);
        });
    }

    @Test
    @DisplayName("ANNUAL_EIR 45.146503 on WEEKS/1 solves back to the per-week payment 350.00 over 29 weeks")
    void annualEirStrategy_weekly_solvesThePerPeriodPayment() {
        assertApiOffersWeeklyFrequency();
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final BigDecimal annualEir = new BigDecimal(WEEKLY_ANNUAL_EIR);
            final Long productId = createProduct(baseProductBuilder("FqEirW1", WEEKS, 1).withPeriodPaymentRate(null)
                    .withPaymentAmountCalculationStrategy("ANNUAL_EIR").withAnnualEir(annualEir).build());
            final Long loanId = submitApproveAndDisburse(WorkingCapitalLoanRequestBuilders.submitAnnualEirApplication(clientId, productId,
                    NET_DISBURSEMENT, annualEir, DISCOUNT_FEE, DISBURSE_DATE, DISBURSE_DATE), DISBURSE_DATE);

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            validateScheduleShape(schedule, "350.00", 29);
            validatePayment(schedule, 1, "2026-01-08", "350.00", "8715.44", "65.44", "934.56");
            validatePayment(schedule, 29, "2026-07-23", "200.00", "0.00", "1.44", "0.00");
        });
    }

    @Test
    @DisplayName("WEEKS/1 repayment of 350.00 on 2026-01-05 lands on the period due 2026-01-08; no row is dated 2026-01-05")
    void weekly_repaymentBetweenDueDates_isAllocatedToTheContainingPeriod() {
        assertApiOffersWeeklyFrequency();
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqW1Mid", WEEKS, 1), DISBURSE_DATE);

            setBusinessDate("2026-01-05");
            makeWcRepayment(loanId, new BigDecimal("350.00"), "05 January 2026");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertNoRowDated(schedule, "2026-01-05");
            validatePaymentDate(schedule, 1, "2026-01-08");
            validateActuals(schedule, 1, "350.00", "8715.44");
            validatePayment(schedule, 2, "2026-01-15", "350.00", "8428.81", "63.37", "871.19");
        });
    }

    @Test
    @DisplayName("WEEKS/1 after COB to 2026-01-16 — periods due 01-08 and 01-15 are missed (0.00), the period due 01-22 is not yet known")
    void weekly_cobAcrossTwoDueDates_seedsOnlyTheElapsedPeriods() {
        assertApiOffersWeeklyFrequency();
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqW1Cob", WEEKS, 1), DISBURSE_DATE);

            advanceBusinessDateWithCob(loanId, BUSINESS_DATE, "2026-01-16");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            validatePaymentDate(schedule, 1, "2026-01-08");
            validatePaymentDate(schedule, 2, "2026-01-15");
            validatePaymentDate(schedule, 3, "2026-01-22");
            validateActuals(schedule, 1, "0.00", null);
            validateActuals(schedule, 2, "0.00", null);
            assertActualsNotYetKnown(schedule, 3);
        });
    }

    @Test
    @DisplayName("a product with repaymentFrequencyType YEARS is rejected with invalid.period.frequency.type")
    void product_yearsFrequency_isRejected() {
        final CallFailedRuntimeException failure = productHelper
                .createWorkingCapitalLoanProductExpectingFailure(tpvProductBuilder("FqYears", YEARS, 1).build());
        assertEquals(400, failure.getStatus(), "HTTP status — " + failure.getResponseBody());
        assertEquals(List.of(WCLP + "repaymentFrequencyType.invalid.period.frequency.type"), errorCodesOf(failure),
                "YEARS is not a supported WC repayment frequency — " + failure.getResponseBody());
    }

    @Test
    @DisplayName("a product with repaymentEvery 0 is rejected with not.greater.than.zero")
    void product_repaymentEveryZero_isRejected() {
        final CallFailedRuntimeException failure = productHelper
                .createWorkingCapitalLoanProductExpectingFailure(tpvProductBuilder("FqEvery0", DAYS, 0).build());
        assertEquals(400, failure.getStatus(), "HTTP status — " + failure.getResponseBody());
        assertEquals(List.of(WCLP + "repaymentEvery.not.greater.than.zero"), errorCodesOf(failure),
                "repaymentEvery must be at least 1 — " + failure.getResponseBody());
    }

    @Test
    @DisplayName("a loan-level YEARS override is rejected with invalid.period.frequency.type")
    void loan_yearsFrequencyOverride_isRejected() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createProduct(
                    tpvProductBuilder("FqLoanYears", DAYS, 1).withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE,
                            "periodPaymentFrequency", Boolean.TRUE, "periodPaymentFrequencyType", Boolean.TRUE)).build());
            final CallFailedRuntimeException failure = wcLoanHelper
                    .submitApplicationExpectingFailure(WorkingCapitalLoanRequestBuilders.submitApplicationWithFrequency(clientId, productId,
                            NET_DISBURSEMENT, TPV_RATE, DISBURSE_DATE, DISBURSE_DATE, DISCOUNT_FEE, 1, YEARS));
            assertEquals(400, failure.getStatus(), "HTTP status — " + failure.getResponseBody());
            assertEquals(List.of(WCL + "repaymentFrequencyType.invalid.period.frequency.type"), errorCodesOf(failure),
                    "YEARS is not a supported WC repayment frequency — " + failure.getResponseBody());
        });
    }

    @Test
    @DisplayName("a frequency override on a product that does not allow it is rejected with override.not.allowed.by.product")
    void loan_frequencyOverrideNotAllowedByProduct_isRejected() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createTpvProduct("FqNoOverride", DAYS, 1);
            final CallFailedRuntimeException failure = wcLoanHelper
                    .submitApplicationExpectingFailure(WorkingCapitalLoanRequestBuilders.submitApplicationWithFrequency(clientId, productId,
                            NET_DISBURSEMENT, TPV_RATE, DISBURSE_DATE, DISBURSE_DATE, DISCOUNT_FEE, 2, MONTHS));
            assertEquals(400, failure.getStatus(), "HTTP status — " + failure.getResponseBody());
            assertEquals(
                    List.of(WCL + "repaymentEvery.override.not.allowed.by.product",
                            WCL + "repaymentFrequencyType.override.not.allowed.by.product"),
                    errorCodesOf(failure), "the product does not allow a frequency override — " + failure.getResponseBody());
        });
    }

    @Test
    @DisplayName("WEEKS/1 rate change 18 -> 24 effective 2026-01-16: paid weeks keep their rows, every week from the one due 01-22 bills 466.67 and the loan closes on 2026-06-04")
    void weekly_periodPaymentRateChange_reSolvesTheRemainingWeeksOnTheWeeklyGrid() {
        assertApiOffersWeeklyFrequency();
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqW1Rate", WEEKS, 1), DISBURSE_DATE);
            setBusinessDate("2026-01-08");
            makeWcRepayment(loanId, new BigDecimal("350.00"), "08 January 2026");
            setBusinessDate("2026-01-15");
            makeWcRepayment(loanId, new BigDecimal("350.00"), "15 January 2026");
            setBusinessDate("2026-01-16");

            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(new BigDecimal("24"), "16 January 2026"));

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            validatePayment(schedule, 0, "2026-01-01", "-9000.00", "9000.00", null, "1000.00");
            validatePayment(schedule, 1, "2026-01-08", "350.00", "8715.44", "65.44", "934.56");
            validatePayment(schedule, 2, "2026-01-15", "350.00", "8428.81", "63.37", "871.19");
            assertEquals(23, schedule.getPayments().size(), "rows: two paid weeks, 19 x 466.67 and the 433.27 closing week, plus row 0");
            for (int paymentNo = 1; paymentNo <= 22; paymentNo++) {
                validatePaymentDate(schedule, paymentNo, LocalDate.of(2026, 1, 1).plusWeeks(paymentNo).toString());
            }
            for (int paymentNo = 3; paymentNo <= 21; paymentNo++) {
                assertEqualBigDecimal(new BigDecimal("466.67"), paymentByNo(schedule, paymentNo).getExpectedPaymentAmount(),
                        "payment " + paymentNo + " bills 100000 x 24 % x 7 / 360");
            }
            final ProjectedAmortizationSchedulePaymentData closing = paymentByNo(schedule, 22);
            assertEqualBigDecimal(new BigDecimal("433.27"), closing.getExpectedPaymentAmount(), "closing week: 9300.00 - 19 x 466.67");
            assertEqualBigDecimal(BigDecimal.ZERO, closing.getExpectedBalance(), "closing week balance");
            assertEqualBigDecimal(BigDecimal.ZERO, closing.getExpectedDiscountFeeBalance(), "closing week discount fee balance");
            assertEqualBigDecimal(DISCOUNT_FEE, totalExpectedAmortization(schedule), "the schedule must earn exactly the discount fee");
            assertNoNegativeAmounts(schedule);

            final WorkingCapitalLoanPeriodPaymentRateChangeData change = wcLoanHelper.getRateChangeHistory(loanId).getFirst();
            assertEqualBigDecimal(new BigDecimal("466.67"), change.getDailyPaymentAmount(), "rate change payment per period");
            assertEquals(20, change.getSegmentTerm(), "rate change segment term in weeks");
        });
    }

    @Test
    @DisplayName("WEEKS/1 350.00 backdated to 2026-01-05 behind a 700.00 on 2026-01-20 equals booking both in date order; its undo equals the 700.00 alone")
    void weekly_backdatedMidPeriodRepaymentAndItsUndo_rebuildTheWeeklyScheduleOfTheRemainingHistory() {
        assertApiOffersWeeklyFrequency();
        runAt(BUSINESS_DATE, () -> {
            final Long productId = createTpvProduct("FqW1Back", WEEKS, 1);
            final Long backdatedLoanId = createApproveAndDisburseTpvLoan(productId, DISBURSE_DATE);
            final Long inDateOrderLoanId = createApproveAndDisburseTpvLoan(productId, DISBURSE_DATE);
            final Long laterOnlyLoanId = createApproveAndDisburseTpvLoan(productId, DISBURSE_DATE);

            setBusinessDate("2026-01-05");
            makeWcRepayment(inDateOrderLoanId, new BigDecimal("350.00"), "05 January 2026");
            setBusinessDate("2026-01-20");
            makeWcRepayment(inDateOrderLoanId, new BigDecimal("700.00"), "20 January 2026");
            makeWcRepayment(laterOnlyLoanId, new BigDecimal("700.00"), "20 January 2026");
            makeWcRepayment(backdatedLoanId, new BigDecimal("700.00"), "20 January 2026");
            final Long backdatedRepaymentId = makeWcRepayment(backdatedLoanId, new BigDecimal("350.00"), "05 January 2026");

            final ProjectedAmortizationScheduleData afterBackdating = wcLoanHelper.getAmortizationSchedule(backdatedLoanId);
            assertNoRowDated(afterBackdating, "2026-01-05");
            assertNoRowDated(afterBackdating, "2026-01-20");
            validatePaymentDate(afterBackdating, 1, "2026-01-08");
            validatePaymentDate(afterBackdating, 3, "2026-01-22");
            validateActuals(afterBackdating, 1, "350.00", "8715.44");
            assertSameRows(wcLoanHelper.getAmortizationSchedule(inDateOrderLoanId), afterBackdating);

            wcLoanHelper.undoTransaction(backdatedLoanId, backdatedRepaymentId);

            final ProjectedAmortizationScheduleData afterUndo = wcLoanHelper.getAmortizationSchedule(backdatedLoanId);
            final BigDecimal firstPeriodActual = paymentByNo(afterUndo, 1).getActualPaymentAmount();
            assertTrue(firstPeriodActual == null || firstPeriodActual.signum() == 0,
                    "the undone 350.00 must no longer be recorded against the period due 2026-01-08, was: " + firstPeriodActual);
            assertSameRows(wcLoanHelper.getAmortizationSchedule(laterOnlyLoanId), afterUndo);
        });
    }

    @Test
    @DisplayName("MONTHS/1 undo of the 1500.00 repayment due 2026-02-01 rebuilds the monthly schedule it replaced")
    void monthly_undoRepayment_rebuildsTheSameMonthlySchedule() {
        runAt(BUSINESS_DATE, () -> {
            final Long loanId = createApproveAndDisburseTpvLoan(createTpvProduct("FqM1Undo", MONTHS, 1), DISBURSE_DATE);
            setBusinessDate("2026-02-01");
            final ProjectedAmortizationScheduleData beforeRepayment = wcLoanHelper.getAmortizationSchedule(loanId);
            assertMonthlyOneReferenceRows(beforeRepayment);

            final Long repaymentId = makeWcRepayment(loanId, new BigDecimal("1500.00"), "01 February 2026");
            validateActuals(wcLoanHelper.getAmortizationSchedule(loanId), 1, "1500.00", "7753.14");

            wcLoanHelper.undoTransaction(loanId, repaymentId);

            final ProjectedAmortizationScheduleData afterUndo = wcLoanHelper.getAmortizationSchedule(loanId);
            assertSameExpectedRows(beforeRepayment, afterUndo);
            assertMonthlyOneReferenceRows(afterUndo);
        });
    }

    @Test
    @DisplayName("updating a product to repaymentFrequencyType YEARS is rejected with invalid.period.frequency.type")
    void productUpdate_yearsFrequency_isRejected() {
        final Long productId = createTpvProduct("FqUpdYears", DAYS, 1);
        final CallFailedRuntimeException failure = productHelper.updateWorkingCapitalLoanProductByIdExpectingFailure(productId,
                new PutWorkingCapitalLoanProductsProductIdRequest()
                        .repaymentFrequencyType(PutWorkingCapitalLoanProductsProductIdRequest.RepaymentFrequencyTypeEnum.YEARS)
                        .locale("en"));
        assertEquals(400, failure.getStatus(), "HTTP status — " + failure.getResponseBody());
        assertEquals(List.of(WCLP + "repaymentFrequencyType.invalid.period.frequency.type"), errorCodesOf(failure),
                failure.getResponseBody());
    }

    @Test
    @DisplayName("modifying a loan application to repaymentFrequencyType YEARS is rejected with invalid.period.frequency.type")
    void loanModify_yearsFrequency_isRejected() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createFrequencyOverridableProduct("FqModYears");
            final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplicationWithFrequency(clientId,
                    productId, NET_DISBURSEMENT, TPV_RATE, DISBURSE_DATE, DISBURSE_DATE, DISCOUNT_FEE, 1, DAYS));
            createdLoanIds.add(loanId);

            final CallFailedRuntimeException failure = wcLoanHelper.modifyApplicationExpectingFailure(loanId,
                    new PutWorkingCapitalLoansLoanIdRequest()
                            .repaymentFrequencyType(PutWorkingCapitalLoansLoanIdRequest.RepaymentFrequencyTypeEnum.YEARS).locale("en")
                            .dateFormat("dd MMMM yyyy"));
            assertEquals(400, failure.getStatus(), "HTTP status — " + failure.getResponseBody());
            assertEquals(List.of(WCL + "repaymentFrequencyType.invalid.period.frequency.type"), errorCodesOf(failure),
                    failure.getResponseBody());
        });
    }

    @Test
    @DisplayName("modifying a loan application to a blank repaymentFrequencyType is rejected with cannot.be.blank")
    void loanModify_blankFrequencyType_isRejected() {
        assertLoanModifyRejected("FqModBlankType", new WorkingCapitalLoanCommandsApi.BlankRepaymentFrequencyTypeRequest(),
                WCL + "repaymentFrequencyType.cannot.be.blank");
    }

    @Test
    @DisplayName("modifying a loan application to an explicit null repaymentEvery is rejected with cannot.be.blank")
    void loanModify_nullRepaymentEvery_isRejected() {
        assertLoanModifyRejected("FqModNullEvery", new WorkingCapitalLoanCommandsApi.NullRepaymentEveryRequest(),
                WCL + "repaymentEvery.cannot.be.blank");
    }

    private void assertLoanModifyRejected(final String label, final WorkingCapitalLoanCommandsApi.LocalizedRequest request,
            final String expectedCode) {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createFrequencyOverridableProduct(label);
            final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplicationWithFrequency(clientId,
                    productId, NET_DISBURSEMENT, TPV_RATE, DISBURSE_DATE, DISBURSE_DATE, DISCOUNT_FEE, 1, DAYS));
            createdLoanIds.add(loanId);

            final CallFailedRuntimeException failure = fail(() -> FineractFeignClientHelper.getFineractFeignClient()
                    .create(WorkingCapitalLoanCommandsApi.class).modifyApplication(loanId, request));
            assertEquals(400, failure.getStatus(), "HTTP status — " + failure.getResponseBody());
            assertEquals(List.of(expectedCode), errorCodesOf(failure), failure.getResponseBody());
        });
    }

    @Test
    @DisplayName("a loan-level repaymentEvery of 0 is rejected with not.greater.than.zero")
    void loan_repaymentEveryZero_isRejected() {
        assertLoanRepaymentEveryRejected("FqLoanEvery0", 0);
    }

    @Test
    @DisplayName("a loan-level repaymentEvery of -1 is rejected with not.greater.than.zero")
    void loan_repaymentEveryNegative_isRejected() {
        assertLoanRepaymentEveryRejected("FqLoanEveryNeg", -1);
    }

    private void assertLoanRepaymentEveryRejected(final String label, final int repaymentEvery) {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createFrequencyOverridableProduct(label);
            final CallFailedRuntimeException failure = wcLoanHelper
                    .submitApplicationExpectingFailure(WorkingCapitalLoanRequestBuilders.submitApplicationWithFrequency(clientId, productId,
                            NET_DISBURSEMENT, TPV_RATE, DISBURSE_DATE, DISBURSE_DATE, DISCOUNT_FEE, repaymentEvery, DAYS));
            assertEquals(400, failure.getStatus(), "HTTP status — " + failure.getResponseBody());
            assertEquals(List.of(WCL + "repaymentEvery.not.greater.than.zero"), errorCodesOf(failure), failure.getResponseBody());
        });
    }

    private Long createFrequencyOverridableProduct(final String label) {
        return createProduct(tpvProductBuilder(label, DAYS, 1).withAllowAttributeOverrides(
                Map.of("discountDefault", Boolean.TRUE, "periodPaymentFrequency", Boolean.TRUE, "periodPaymentFrequencyType", Boolean.TRUE))
                .build());
    }

    /**
     * MONTHS/1 reference rows: 6 x 1500.00 + 1000.00 from 2026-02-01, discounted at the monthly rate of 39.495353 %.
     */
    private static void assertMonthlyOneReferenceRows(final ProjectedAmortizationScheduleData schedule) {
        validateScheduleShape(schedule, "1500.00", 7);
        validatePayment(schedule, 1, "2026-02-01", "1500.00", "7753.14", "253.14", "746.86");
        validatePayment(schedule, 2, "2026-03-01", "1500.00", "6471.21", "218.07", "528.79");
        validatePayment(schedule, 6, "2026-07-01", "1500.00", "972.64", "67.64", "27.36");
        validatePayment(schedule, 7, "2026-08-01", "1000.00", "0.00", "27.36", "0.00");
    }

    /**
     * WEEKS/1 (and DAYS/7) reference schedule: 350.00 = 100000 x 18 % x 7 / 360, 28 full weeks plus a 200.00 closing
     * payment, discounted at the weekly rate of a 45.146503 % annual EIR.
     */
    private static void assertWeeklyOneReferenceSchedule(final ProjectedAmortizationScheduleData schedule) {
        validateScheduleShape(schedule, "350.00", 29);
        validatePayment(schedule, 0, "2026-01-01", "-9000.00", "9000.00", null, "1000.00");
        validatePayment(schedule, 1, "2026-01-08", "350.00", "8715.44", "65.44", "934.56");
        validatePayment(schedule, 2, "2026-01-15", "350.00", "8428.81", "63.37", "871.19");
        validatePayment(schedule, 28, "2026-07-16", "350.00", "198.56", "3.96", "1.44");
        validatePayment(schedule, 29, "2026-07-23", "200.00", "0.00", "1.44", "0.00");
        assertEqualBigDecimal(new BigDecimal("200.00"), paymentByNo(schedule, 29).getExpectedPaymentAmount(),
                "the closing payment is the 200.00 remainder of 10000 after 28 x 350.00");
        assertEqualBigDecimal(DISCOUNT_FEE, totalExpectedAmortization(schedule), "the schedule must earn exactly the discount fee");
        assertNoNegativeAmounts(schedule);
    }

    /**
     * Weekly must be configurable through the existing API fields. The generated client mirrors the Swagger
     * {@code allowableValues} of {@code repaymentFrequencyType}; without WEEKS there, no API client can send it.
     */
    private static void assertApiOffersWeeklyFrequency() {
        final List<String> productOptions = Arrays.stream(PostWorkingCapitalLoanProductsRequest.RepaymentFrequencyTypeEnum.values())
                .map(PostWorkingCapitalLoanProductsRequest.RepaymentFrequencyTypeEnum::getValue).toList();
        final List<String> loanOptions = Arrays.stream(PostWorkingCapitalLoansRequest.RepaymentFrequencyTypeEnum.values())
                .map(PostWorkingCapitalLoansRequest.RepaymentFrequencyTypeEnum::getValue).toList();
        assertTrue(productOptions.contains(WEEKS),
                "the WC product API contract must offer repaymentFrequencyType WEEKS, offered: " + productOptions);
        assertTrue(loanOptions.contains(WEEKS),
                "the WC loan API contract must offer repaymentFrequencyType WEEKS, offered: " + loanOptions);
    }

    private void validateLoanTerms(final Long loanId, final String expectedPaymentAmount, final int expectedTerm,
            final String expectedAnnualEir) {
        final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertEqualBigDecimal(new BigDecimal(expectedPaymentAmount), loan.getPeriodPaymentAmount(), "loan periodPaymentAmount");
        assertEquals(expectedTerm, loan.getNumberOfRepayments(), "loan numberOfRepayments");
        assertEqualBigDecimal(new BigDecimal(expectedAnnualEir), round6(loan.getCalculatedAnnualEir()), "loan calculatedAnnualEir");
    }

    private static WorkingCapitalLoanProductTestBuilder baseProductBuilder(final String label, final String frequencyType,
            final int repaymentEvery) {
        return new WorkingCapitalLoanProductTestBuilder().withName("WCL " + label + " " + Utils.uniqueRandomStringGenerator("", 8))
                .withShortName(Utils.uniqueRandomStringGenerator("", 4))
                .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).withNpvDayCount(NPV_DAY_COUNT)
                .withDiscount(DISCOUNT_FEE).withRepaymentFrequencyType(frequencyType).withRepaymentEvery(repaymentEvery);
    }

    private static WorkingCapitalLoanProductTestBuilder tpvProductBuilder(final String label, final String frequencyType,
            final int repaymentEvery) {
        return baseProductBuilder(label, frequencyType, repaymentEvery).withPaymentAmountCalculationStrategy("TPV")
                .withPeriodPaymentRate(TPV_RATE);
    }

    private Long createTpvProduct(final String label, final String frequencyType, final int repaymentEvery) {
        return createProduct(tpvProductBuilder(label, frequencyType, repaymentEvery).build());
    }

    private Long createProduct(final PostWorkingCapitalLoanProductsRequest request) {
        return productHelper.createWorkingCapitalLoanProduct(request).getResourceId();
    }

    private Long createApproveAndDisburseTpvLoan(final Long productId, final String date) {
        final Long clientId = createClient(date);
        return submitApproveAndDisburse(WorkingCapitalLoanRequestBuilders.submitApplicationWithDiscount(clientId, productId,
                NET_DISBURSEMENT, TPV_RATE, date, date, DISCOUNT_FEE), date);
    }

    private Long submitApproveAndDisburse(final PostWorkingCapitalLoansRequest application, final String date) {
        final Long loanId = wcLoanHelper.submitApplication(application);
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approveWithDiscount(date, NET_DISBURSEMENT, date, DISCOUNT_FEE));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount(date, NET_DISBURSEMENT, DISCOUNT_FEE));
        return loanId;
    }

    private static BigDecimal round6(final BigDecimal value) {
        return value == null ? null : value.setScale(6, RoundingMode.HALF_EVEN);
    }
}
