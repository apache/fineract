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
package org.apache.fineract.portfolio.workingcapitalloan.calc;

import static org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType.DAYS;
import static org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType.MONTHS;
import static org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType.WEEKS;
import static org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType.YEARS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.junit.jupiter.api.Test;

class ProjectedAmortizationScheduleRepaymentFrequencyTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("USD", 2, null);
    private static final MonetaryCurrency USD = new MonetaryCurrency("USD", 2, null);
    private static final BigDecimal DISCOUNT_FEE = new BigDecimal("1000");
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal TPV = new BigDecimal("100000");
    private static final BigDecimal RATE = new BigDecimal("18");
    private static final int DAY_COUNT = 360;
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 1, 1);

    private static ProjectedAmortizationScheduleModel tpv(final WorkingCapitalLoanPeriodFrequencyType type, final int every,
            final LocalDate disbursementDate) {
        return ProjectedAmortizationScheduleModel.generate(WorkingCapitalAmortizationType.EIR, DISCOUNT_FEE, NET_DISBURSEMENT, TPV, RATE,
                DAY_COUNT, type, every, disbursementDate, MC, CURRENCY, disbursementDate);
    }

    @Test
    void dailyEveryOneDay_isTheScheduleOfALoanThatRecordsNoFrequency() {
        final ProjectedAmortizationScheduleModel daily = tpv(DAYS, 1, DISBURSEMENT_DATE);
        final ProjectedAmortizationScheduleModel legacy = ProjectedAmortizationScheduleModel.generate(WorkingCapitalAmortizationType.EIR,
                DISCOUNT_FEE, NET_DISBURSEMENT, TPV, RATE, DAY_COUNT, null, null, DISBURSEMENT_DATE, MC, CURRENCY, DISBURSEMENT_DATE);

        assertEquals(200, daily.originalPaymentNumber());
        assertEquals(new BigDecimal("46.845102"), daily.calculatedAnnualEir());
        assertEquals(legacy.effectiveInterestRate(), daily.effectiveInterestRate());
        assertEquals(rows(legacy), rows(daily));
    }

    @Test
    void weekly_billsAWeekOfPaymentDiscountedAtTheWeeklyRate() {
        final ProjectedAmortizationScheduleModel model = tpv(WEEKS, 1, DISBURSEMENT_DATE);

        assertEquals(29, model.originalPaymentNumber());
        assertEquals(new BigDecimal("350.00"), model.expectedPaymentAmount().getAmount());
        assertEquals(new BigDecimal("200.00"), model.finalPaymentAmount().getAmount());
        assertEquals(new BigDecimal("45.146503"), model.calculatedAnnualEir());
        assertRow(model, 1, "2026-01-08", "8715.44", "934.56");
        assertRow(model, 2, "2026-01-15", "8428.81", "871.19");
        assertRow(model, 28, "2026-07-16", "198.56", "1.44");
        assertRow(model, 29, "2026-07-23", "0.00", "0.00");
    }

    @Test
    void everySevenDays_isTheSameScheduleAsEveryWeek() {
        assertEquals(rows(tpv(WEEKS, 1, DISBURSEMENT_DATE)), rows(tpv(DAYS, 7, DISBURSEMENT_DATE)));
    }

    @Test
    void everyTwoWeeks_doublesThePeriod() {
        final ProjectedAmortizationScheduleModel model = tpv(WEEKS, 2, DISBURSEMENT_DATE);

        assertEquals(15, model.originalPaymentNumber());
        assertEquals(new BigDecimal("700.00"), model.expectedPaymentAmount().getAmount());
        assertEquals(new BigDecimal("43.297294"), model.calculatedAnnualEir());
        assertRow(model, 1, "2026-01-15", "8426.80", "873.20");
        assertRow(model, 2, "2026-01-29", "7845.52", "754.48");
        assertRow(model, 15, "2026-07-30", "0.00", "0.00");
    }

    @Test
    void monthly_billsAMonthOfPaymentAgainstAYearOfTwelve() {
        final ProjectedAmortizationScheduleModel model = tpv(MONTHS, 1, DISBURSEMENT_DATE);

        assertEquals(7, model.originalPaymentNumber());
        assertEquals(new BigDecimal("1500.00"), model.expectedPaymentAmount().getAmount());
        assertEquals(new BigDecimal("1000.00"), model.finalPaymentAmount().getAmount());
        assertEquals(new BigDecimal("39.495353"), model.calculatedAnnualEir());
        assertRow(model, 1, "2026-02-01", "7753.14", "746.86");
        assertRow(model, 2, "2026-03-01", "6471.21", "528.79");
        assertRow(model, 6, "2026-07-01", "972.64", "27.36");
        assertRow(model, 7, "2026-08-01", "0.00", "0.00");
    }

    @Test
    void monthly_disbursedOnAMonthEnd_fallsDueOnEveryMonthEndWithoutDrifting() {
        final ProjectedAmortizationScheduleModel model = tpv(MONTHS, 1, LocalDate.of(2026, 1, 31));

        assertEquals(
                List.of("2026-01-31", "2026-02-28", "2026-03-31", "2026-04-30", "2026-05-31", "2026-06-30", "2026-07-31", "2026-08-31"),
                model.projectedPayments().stream().map(payment -> payment.date().toString()).toList());
        assertRow(model, 1, "2026-02-28", "7753.14", "746.86");
    }

    @Test
    void everyTwoMonths_onAPeriodOverrideDoublesTheMonthlyPayment() {
        final ProjectedAmortizationScheduleModel model = tpv(MONTHS, 2, DISBURSEMENT_DATE);

        assertEquals(4, model.originalPaymentNumber());
        assertEquals(new BigDecimal("3000.00"), model.expectedPaymentAmount().getAmount());
        assertEquals(new BigDecimal("33.696456"), model.calculatedAnnualEir());
        assertRow(model, 1, "2026-03-01", "6446.32", "553.68");
        assertRow(model, 4, "2026-09-01", "0.00", "0.00");
    }

    @Test
    void paymentAmountAndAnnualEir_areTakenPerPeriod() {
        final ProjectedAmortizationScheduleModel weeklyTpv = tpv(WEEKS, 1, DISBURSEMENT_DATE);
        final ProjectedAmortizationScheduleModel paymentAmount = ProjectedAmortizationScheduleModel.generateFromPaymentAmount(
                WorkingCapitalAmortizationType.EIR, DISCOUNT_FEE, NET_DISBURSEMENT, new BigDecimal("350.00"), DAY_COUNT, WEEKS, 1,
                DISBURSEMENT_DATE, MC, CURRENCY, DISBURSEMENT_DATE);
        final ProjectedAmortizationScheduleModel annualEir = ProjectedAmortizationScheduleModel.generateFromAnnualEir(
                WorkingCapitalAmortizationType.EIR, DISCOUNT_FEE, NET_DISBURSEMENT, new BigDecimal("45.146503"), DAY_COUNT, WEEKS, 1,
                DISBURSEMENT_DATE, MC, CURRENCY, DISBURSEMENT_DATE);

        assertEquals(rows(weeklyTpv), rows(paymentAmount));
        assertEquals(new BigDecimal("45.146503"), paymentAmount.calculatedAnnualEir());
        assertEquals(new BigDecimal("350.00"), annualEir.expectedPaymentAmount().getAmount());
        assertEquals(29, annualEir.originalPaymentNumber());
        assertRow(annualEir, 1, "2026-01-08", "8715.44", "934.56");
    }

    @Test
    void weekly_paymentBetweenDueDates_isAllocatedToThePeriodFallingDueNext() {
        final ProjectedAmortizationScheduleModel model = tpv(WEEKS, 1, DISBURSEMENT_DATE);
        model.applyPayment(LocalDate.of(2026, 1, 5), new BigDecimal("350.00"));

        final ProjectedPayment first = model.projectedPayments().get(1);
        assertEquals(LocalDate.of(2026, 1, 8), first.date());
        assertEquals(new BigDecimal("350.00"), first.actualPaymentAmount().getAmount());
        assertEquals(new BigDecimal("8715.44"), first.actualBalance().getAmount());
        assertFalse(model.projectedPayments().stream().anyMatch(payment -> payment.date().equals(LocalDate.of(2026, 1, 5))));
        assertRow(model, 2, "2026-01-15", "8428.81", "871.19");

        model.undoPayment(LocalDate.of(2026, 1, 5), new BigDecimal("350.00"));
        assertNull(model.projectedPayments().get(1).actualPaymentAmount());
    }

    @Test
    void weekly_elapsedTime_seedsOnlyTheDueDatesThatWentBy() {
        final ProjectedAmortizationScheduleModel model = tpv(WEEKS, 1, DISBURSEMENT_DATE);
        model.acknowledgeElapsedPeriods(LocalDate.of(2026, 1, 16));

        final List<ProjectedPayment> payments = model.projectedPayments();
        assertEquals(BigDecimal.ZERO.setScale(2), payments.get(1).actualPaymentAmount().getAmount());
        assertEquals(BigDecimal.ZERO.setScale(2), payments.get(2).actualPaymentAmount().getAmount());
        assertEquals(LocalDate.of(2026, 1, 22), payments.get(3).date());
        assertNull(payments.get(3).actualPaymentAmount());
    }

    @Test
    void periodsUntil_countsFromTheAnchorAcrossMonthEndsAndLeapDays() {
        final RepaymentFrequency monthly = new RepaymentFrequency(MONTHS, 1);
        final LocalDate anchor = LocalDate.of(2028, 1, 29);

        assertEquals(LocalDate.of(2028, 2, 29), monthly.dueDate(anchor, 1));
        assertEquals(LocalDate.of(2028, 3, 29), monthly.dueDate(anchor, 2));
        assertEquals(1, monthly.periodsUntil(anchor, LocalDate.of(2028, 2, 29)));
        assertEquals(2, monthly.periodsUntil(anchor, LocalDate.of(2028, 3, 1)));
        assertEquals(0, monthly.periodsUntil(anchor, anchor));
        assertEquals(1, new RepaymentFrequency(WEEKS, 1).periodsUntil(anchor, anchor.plusDays(1)));
        assertEquals(10, RepaymentFrequency.DAILY.periodsUntil(anchor, anchor.plusDays(10)));
        assertEquals(-3, RepaymentFrequency.DAILY.periodsUntil(anchor, anchor.minusDays(3)));
    }

    @Test
    void periodsUntil_fromTheThirtyFirst_mapsEachDateToTheShortMonthDueDateOnOrAfterIt() {
        final RepaymentFrequency monthly = new RepaymentFrequency(MONTHS, 1);
        final LocalDate anchor = LocalDate.of(2026, 1, 31);

        assertEquals(LocalDate.of(2026, 2, 28), monthly.dueDate(anchor, 1));
        assertEquals(LocalDate.of(2026, 3, 31), monthly.dueDate(anchor, 2));
        assertEquals(LocalDate.of(2026, 4, 30), monthly.dueDate(anchor, 3));
        assertEquals(1, monthly.periodsUntil(anchor, LocalDate.of(2026, 2, 27)));
        assertEquals(1, monthly.periodsUntil(anchor, LocalDate.of(2026, 2, 28)));
        assertEquals(2, monthly.periodsUntil(anchor, LocalDate.of(2026, 3, 1)));
        assertEquals(2, monthly.periodsUntil(anchor, LocalDate.of(2026, 3, 30)));
        assertEquals(2, monthly.periodsUntil(anchor, LocalDate.of(2026, 3, 31)));
        assertEquals(3, monthly.periodsUntil(anchor, LocalDate.of(2026, 4, 1)));
        assertEquals(3, monthly.periodsUntil(anchor, LocalDate.of(2026, 4, 30)));
        assertEquals(4, monthly.periodsUntil(anchor, LocalDate.of(2026, 5, 1)));
    }

    @Test
    void monthly_fromTheThirtyFirst_paymentsAfterAShortMonthEndBelongToTheNextDueDate() {
        final LocalDate anchor = LocalDate.of(2026, 1, 31);
        final ProjectedAmortizationScheduleModel onMonthEnd = tpv(MONTHS, 1, anchor);
        onMonthEnd.applyPayment(LocalDate.of(2026, 2, 28), new BigDecimal("1500.00"));
        final ProjectedAmortizationScheduleModel dayAfter = tpv(MONTHS, 1, anchor);
        dayAfter.applyPayment(LocalDate.of(2026, 3, 1), new BigDecimal("1500.00"));

        assertEquals(LocalDate.of(2026, 2, 28), onMonthEnd.projectedPayments().get(1).date());
        assertEquals(LocalDate.of(2026, 3, 31), onMonthEnd.projectedPayments().get(2).date());
        assertEquals(new BigDecimal("1500.00"), onMonthEnd.projectedPayments().get(1).actualPaymentAmount().getAmount());
        assertNull(onMonthEnd.projectedPayments().get(2).actualPaymentAmount());
        assertEquals(new BigDecimal("1500.00"), dayAfter.projectedPayments().get(2).actualPaymentAmount().getAmount());
    }

    @Test
    void monthlyIntervalPastTheCalendar_failsAsArithmeticSoCallersReportItNotCalculable() {
        final RepaymentFrequency monthly = new RepaymentFrequency(MONTHS, 1_500_000_000);

        assertEquals(DISBURSEMENT_DATE.plusMonths(1_500_000_000L), monthly.dueDate(DISBURSEMENT_DATE, 1));
        final ArithmeticException outOfRange = assertThrows(ArithmeticException.class, () -> monthly.dueDate(DISBURSEMENT_DATE, 8));
        assertInstanceOf(DateTimeException.class, outOfRange.getCause());
        assertThrows(ArithmeticException.class,
                () -> ProjectedAmortizationScheduleModel.generateFromPaymentAmount(WorkingCapitalAmortizationType.FLAT, DISCOUNT_FEE,
                        NET_DISBURSEMENT, new BigDecimal("1000"), DAY_COUNT, MONTHS, 1_500_000_000, DISBURSEMENT_DATE, MC, CURRENCY,
                        DISBURSEMENT_DATE));
    }

    @Test
    void yearsOrAnIntervalBelowOne_cannotBeScheduled() {
        assertThrows(IllegalArgumentException.class, () -> RepaymentFrequency.of(YEARS, 1));
        assertThrows(IllegalArgumentException.class, () -> RepaymentFrequency.of(DAYS, 0));
        assertFalse(ProjectedAmortizationScheduleModel.isScheduleCalculable(WorkingCapitalAmortizationType.EIR, DISCOUNT_FEE,
                NET_DISBURSEMENT, TPV, RATE, DAY_COUNT, YEARS, 1, USD, MC));
    }

    @Test
    void aNegativeInterval_isRejectedByTheFrequencyItself() {
        final IllegalArgumentException negative = assertThrows(IllegalArgumentException.class, () -> new RepaymentFrequency(DAYS, -1));
        assertEquals("repaymentEvery must be at least 1, got: -1", negative.getMessage());
        final IllegalArgumentException viaFactory = assertThrows(IllegalArgumentException.class, () -> RepaymentFrequency.of(WEEKS, -1));
        assertEquals("repaymentEvery must be at least 1, got: -1", viaFactory.getMessage());
    }

    @Test
    void monthly_repaymentOnTheDisbursementDate_movesEveryDueDateOnePeriodEarlier() {
        final ProjectedAmortizationScheduleModel model = tpv(MONTHS, 1, DISBURSEMENT_DATE);
        model.applyPayment(DISBURSEMENT_DATE, new BigDecimal("1500.00"));

        assertEquals(
                List.of("2026-01-01", "2026-01-01", "2026-02-01", "2026-03-01", "2026-04-01", "2026-05-01", "2026-06-01", "2026-07-01"),
                model.projectedPayments().stream().map(payment -> payment.date().toString()).toList());
        assertEquals(new BigDecimal("1500.00"), model.projectedPayments().get(1).actualPaymentAmount().getAmount());
    }

    @Test
    void frequencyTypeAndInterval_areEitherBothSetOrBothAbsent() {
        assertEquals(RepaymentFrequency.DAILY, RepaymentFrequency.of(null, null));
        assertEquals(new RepaymentFrequency(WEEKS, 2), RepaymentFrequency.of(WEEKS, 2));
        assertThrows(IllegalArgumentException.class, () -> RepaymentFrequency.of(WEEKS, null));
        assertThrows(IllegalArgumentException.class, () -> RepaymentFrequency.of(null, 7));
    }

    @Test
    void largeInterval_overflowsLoudlyInsteadOfWrappingAround() {
        final RepaymentFrequency weekly = new RepaymentFrequency(WEEKS, Integer.MAX_VALUE);
        final RepaymentFrequency daily = new RepaymentFrequency(DAYS, Integer.MAX_VALUE);

        assertThrows(ArithmeticException.class, weekly::unitsPerPeriod);
        assertThrows(ArithmeticException.class, () -> daily.dueDate(DISBURSEMENT_DATE, Long.MAX_VALUE / 2));
        assertEquals(DISBURSEMENT_DATE.plusDays(Integer.MAX_VALUE), daily.dueDate(DISBURSEMENT_DATE, 1));
        assertFalse(ProjectedAmortizationScheduleModel.isScheduleCalculable(WorkingCapitalAmortizationType.EIR, DISCOUNT_FEE,
                NET_DISBURSEMENT, TPV, RATE, DAY_COUNT, WEEKS, Integer.MAX_VALUE, USD, MC));
    }

    private static List<List<Object>> rows(final ProjectedAmortizationScheduleModel model) {
        return model.projectedPayments().stream()
                .map(payment -> List.<Object>of(payment.paymentNo(), payment.date(), payment.expectedPaymentAmount().getAmount(),
                        payment.discountFactor(), payment.expectedBalance().getAmount(), payment.expectedDiscountFeeBalance().getAmount()))
                .toList();
    }

    private static void assertRow(final ProjectedAmortizationScheduleModel model, final int paymentNo, final String date,
            final String expectedBalance, final String expectedDiscountFeeBalance) {
        final ProjectedPayment payment = model.projectedPayments().get(paymentNo);
        assertEquals(paymentNo, payment.paymentNo());
        assertEquals(LocalDate.parse(date), payment.date(), "payment " + paymentNo + " date");
        assertEquals(new BigDecimal(expectedBalance), payment.expectedBalance().getAmount(), "payment " + paymentNo + " balance");
        assertEquals(new BigDecimal(expectedDiscountFeeBalance), payment.expectedDiscountFeeBalance().getAmount(),
                "payment " + paymentNo + " discount fee balance");
    }
}
