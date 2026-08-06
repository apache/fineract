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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.junit.jupiter.api.Test;

/**
 * Covers {@link ProjectedAmortizationScheduleModel#applyPrincipalAdjustment(LocalDate, BigDecimal)} — the principal an
 * over-refunding credit balance refund re-injects into the loan. It is due on the refund's own date but is deliberately
 * not amortized, so it must leave the whole projection (EIR, NPV, balances, discount fee) untouched.
 */
class ProjectedAmortizationSchedulePrincipalAdjustmentTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("EUR", 2, null);
    private static final BigDecimal TPV = new BigDecimal("100000");
    private static final BigDecimal RATE = new BigDecimal("18");
    private static final int DAY_COUNT = 360;
    private static final LocalDate DISBURSEMENT = LocalDate.of(2026, 1, 1);
    private static final LocalDate ADJUSTMENT_DATE = LocalDate.of(2026, 1, 10);
    private static final BigDecimal ADJUSTMENT = new BigDecimal("200");

    /** 9000 disbursed with a 500 discount fee, giving a 50 daily expected payment over a 190 day term. */
    private ProjectedAmortizationScheduleModel model() {
        return ProjectedAmortizationScheduleModel.generate(new BigDecimal("500"), new BigDecimal("9000"), TPV, RATE, DAY_COUNT,
                DISBURSEMENT, MC, CURRENCY, DISBURSEMENT);
    }

    private static ProjectedPayment paymentOn(final ProjectedAmortizationScheduleModel model, final LocalDate date) {
        return model.projectedPayments().stream().filter(payment -> payment.paymentNo() > 0 && date.equals(payment.date())).findFirst()
                .orElseThrow();
    }

    @Test
    void raisesTheExpectedPaymentOfItsOwnPeriodOnly() {
        final ProjectedAmortizationScheduleModel model = model();
        assertEquals(0, paymentOn(model, ADJUSTMENT_DATE).expectedPaymentAmount().getAmount().compareTo(new BigDecimal("50")));

        model.applyPrincipalAdjustment(ADJUSTMENT_DATE, ADJUSTMENT);

        assertEquals(0, paymentOn(model, ADJUSTMENT_DATE).expectedPaymentAmount().getAmount().compareTo(new BigDecimal("250")));
        model.projectedPayments().stream().filter(payment -> payment.paymentNo() > 0 && !ADJUSTMENT_DATE.equals(payment.date()))
                .forEach(payment -> assertEquals(0, payment.expectedPaymentAmount().getAmount().compareTo(new BigDecimal("50")),
                        "period " + payment.paymentNo() + " must keep the projected payment"));
    }

    /**
     * The re-injected principal is owned by the loan balance, not by this projection, so every column derived from the
     * disbursed amount must read exactly as it did before the adjustment.
     */
    @Test
    void leavesTheProjectionUntouched() {
        final ProjectedAmortizationScheduleModel unadjusted = model();
        final ProjectedAmortizationScheduleModel adjusted = model();

        adjusted.applyPrincipalAdjustment(ADJUSTMENT_DATE, ADJUSTMENT);

        assertEquals(unadjusted.effectiveInterestRate(), adjusted.effectiveInterestRate());
        assertEquals(unadjusted.projectedPayments().size(), adjusted.projectedPayments().size());
        for (int i = 0; i < unadjusted.projectedPayments().size(); i++) {
            final ProjectedPayment expected = unadjusted.projectedPayments().get(i);
            final ProjectedPayment actual = adjusted.projectedPayments().get(i);
            assertEquals(expected.date(), actual.date());
            assertSameAmount(expected.npvValue(), actual.npvValue(), "npv of period " + expected.paymentNo());
            assertSameAmount(expected.expectedBalance(), actual.expectedBalance(), "balance of period " + expected.paymentNo());
            assertSameAmount(expected.expectedAmortizationAmount(), actual.expectedAmortizationAmount(),
                    "amortization of period " + expected.paymentNo());
            assertSameAmount(expected.expectedDiscountFeeBalance(), actual.expectedDiscountFeeBalance(),
                    "discount fee balance of period " + expected.paymentNo());
        }
    }

    private static void assertSameAmount(final Money expected, final Money actual, final String message) {
        assertEquals(expected == null ? null : expected.getAmount(), actual == null ? null : actual.getAmount(), message);
    }

    /**
     * The trailing periods the loan will never see are dropped, but the one carrying an adjustment is due even with
     * nothing left to discount on it — and keeping it also keeps the periods before it, leaving no gap in the schedule.
     */
    @Test
    void keepsTheAdjustedPeriodThatWouldOtherwiseBeTrimmed() {
        final ProjectedAmortizationScheduleModel repaidInFull = model();
        repaidInFull.applyPayment(LocalDate.of(2026, 1, 2), new BigDecimal("9500"));
        final List<ProjectedPayment> trimmed = repaidInFull.projectedPayments();
        assertTrue(trimmed.stream().noneMatch(payment -> ADJUSTMENT_DATE.equals(payment.date())),
                "the scenario must trim the adjustment date away without the adjustment");

        final ProjectedAmortizationScheduleModel adjusted = model();
        adjusted.applyPayment(LocalDate.of(2026, 1, 2), new BigDecimal("9500"));
        adjusted.applyPrincipalAdjustment(ADJUSTMENT_DATE, ADJUSTMENT);

        final ProjectedPayment adjustedPayment = paymentOn(adjusted, ADJUSTMENT_DATE);
        assertNotNull(adjustedPayment);
        assertEquals(0, adjustedPayment.expectedPaymentAmount().getAmount().compareTo(new BigDecimal("250")));
        assertEquals(adjustedPayment.paymentNo(), adjusted.projectedPayments().getLast().paymentNo(),
                "the adjusted period must be the last one kept");
        for (int i = 0; i < adjusted.projectedPayments().size(); i++) {
            assertEquals(i, adjusted.projectedPayments().get(i).paymentNo(), "the schedule must have no gap in payment numbers");
        }
    }

    /**
     * An adjustment dated beyond the term is clamped onto the last period, the same way a late payment is. Dropping it
     * would leave the projection disagreeing with the loan balance, which still owes the amount - and a credit balance
     * refund lands on an overpaid loan, so a date at or past maturity is the normal case rather than the exotic one.
     */
    @Test
    void clampsAnAdjustmentDatedBeyondTheTermOntoTheLastPeriod() {
        final ProjectedAmortizationScheduleModel model = model();
        final List<ProjectedPayment> before = model.projectedPayments();

        model.applyPrincipalAdjustment(DISBURSEMENT.plusYears(5), ADJUSTMENT);

        assertEquals(before.size(), model.projectedPayments().size(), "clamping must not add a period");
        final ProjectedPayment lastPeriod = model.projectedPayments().getLast();
        assertEquals(0, lastPeriod.expectedPaymentAmount().getAmount().compareTo(new BigDecimal("50").add(ADJUSTMENT)),
                "the last period must carry the clamped adjustment");
        model.projectedPayments().stream().filter(payment -> payment.paymentNo() > 0).filter(payment -> payment != lastPeriod)
                .forEach(payment -> assertEquals(0, payment.expectedPaymentAmount().getAmount().compareTo(new BigDecimal("50")),
                        "period " + payment.paymentNo() + " must keep the projected payment"));
    }

    /** An adjustment dated before the first installment is clamped forward onto it, mirroring an early payment. */
    @Test
    void clampsAnAdjustmentDatedBeforeTheFirstInstallmentOntoTheFirstPeriod() {
        final ProjectedAmortizationScheduleModel model = model();

        model.applyPrincipalAdjustment(DISBURSEMENT.minusDays(10), ADJUSTMENT);

        final ProjectedPayment firstPeriod = model.projectedPayments().stream().filter(payment -> payment.paymentNo() == 1).findFirst()
                .orElseThrow();
        assertEquals(0, firstPeriod.expectedPaymentAmount().getAmount().compareTo(new BigDecimal("50").add(ADJUSTMENT)),
                "the first period must carry the clamped adjustment");
    }

    /**
     * A discount fee adjustment restates the schedule from a fresh model, re-applying the snapshotted payments and
     * adjustments. The over-refund CBR overlay must survive that restate rather than vanish.
     */
    @Test
    void snapshotReAppliedToAFreshModelKeepsTheAdjustmentOverlay() {
        final ProjectedAmortizationScheduleModel model = model();
        model.applyPrincipalAdjustment(ADJUSTMENT_DATE, ADJUSTMENT);
        assertEquals(1, model.snapshotPrincipalAdjustments().size());

        // Mirror WorkingCapitalLoanAmortizationScheduleWriteServiceImpl.applyDiscountFeeAdjustment: build a fresh model
        // and re-apply the snapshotted payments and adjustments.
        final ProjectedAmortizationScheduleModel restated = model();
        model.snapshotPrincipalAdjustments()
                .forEach(adjustment -> restated.applyPrincipalAdjustment(adjustment.date(), adjustment.amount().getAmount()));

        assertEquals(0, paymentOn(restated, ADJUSTMENT_DATE).expectedPaymentAmount().getAmount().compareTo(new BigDecimal("250")));
    }
}
