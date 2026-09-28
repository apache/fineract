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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;

/**
 * The amortization parameters of a schedule: what the borrower is billed each period, how many periods that takes, what
 * the closing period bills instead, and the periodic rate that makes the three consistent.
 *
 * <p>
 * Derived identically for the original schedule and for every rate change, so a segment of the schedule is never a
 * different kind of thing from the schedule itself - it is the same solve run on whatever balance and unearned fee are
 * left at the period it starts.
 *
 * <p>
 * Under EIR the rate starts as the IRR of the exact cash flow the borrower will pay, closing remainder included, and is
 * then put through the annual rate the loan is published at: rounded to the reported scale a year and spread back over
 * the periods of that year. The schedule therefore discounts on the rate the loan reports rather than on one a few
 * digits away from it. A FLAT schedule earns a fixed share of every payment instead and solves no rate: it is sized the
 * same way and both its rates are {@code null}.
 */
final class AmortizationParams {

    /**
     * Six decimals of a <em>percentage</em>, so a {@code DECIMAL(19,6)} rate resolves to 1E-8 as a fraction. The
     * schedule's periodic rate is spread back from the rounded value, so the two digits the percentage buys are
     * amortization accuracy rather than presentation.
     */
    private static final int CALCULATED_ANNUAL_EIR_SCALE = 6;

    /**
     * Fixed rather than the tenant's money rounding mode: a rate is not money, and this one is spread back into the
     * rate the whole schedule discounts on, so configuration would amortize the same loan differently per tenant.
     */
    private static final RoundingMode CALCULATED_ANNUAL_EIR_ROUNDING = RoundingMode.HALF_EVEN;

    private AmortizationParams() {}

    /**
     * Single definition of the reported rate — the value stored on a schedule, the derive-on-read fallback for the
     * schedules written before it was stored, and the value a payment rate change records as its snapshot. The fraction
     * {@link TvmFunctions#annualize} returns is moved onto a percentage before rounding; see
     * {@link #CALCULATED_ANNUAL_EIR_SCALE}. Rounding here rather than in the {@code DECIMAL(19,6)} column is what makes
     * the API, the business event and the stored snapshot read the same digits.
     */
    static BigDecimal calculatedAnnualEir(final BigDecimal periodicRate, final int npvDayCount, final RepaymentFrequency frequency,
            final MathContext mc) {
        final BigDecimal unitRate = frequency.unitsPerPeriod() == 1 ? periodicRate
                : TvmFunctions.deannualize(periodicRate, frequency.unitsPerPeriod(), mc);
        return TvmFunctions.annualize(unitRate, frequency.unitsPerYear(npvDayCount), mc).movePointRight(2)
                .setScale(CALCULATED_ANNUAL_EIR_SCALE, CALCULATED_ANNUAL_EIR_ROUNDING);
    }

    /** Inverse of {@link #calculatedAnnualEir}: back onto a fraction, then spread over the period. */
    private static BigDecimal periodicRateFrom(final BigDecimal calculatedAnnualEir, final int npvDayCount,
            final RepaymentFrequency frequency, final MathContext mc) {
        return compoundOverPeriod(TvmFunctions.deannualize(calculatedAnnualEir.movePointLeft(2), frequency.unitsPerYear(npvDayCount), mc),
                frequency, mc);
    }

    /**
     * Compounds the rate of one unit (a day, or a month) over the units of a period. Rooting over the year first and
     * then compounding keeps both steps on whole exponents where {@code unitsPerYear / unitsPerPeriod} need not be one.
     */
    private static BigDecimal compoundOverPeriod(final BigDecimal unitRate, final RepaymentFrequency frequency, final MathContext mc) {
        return frequency.unitsPerPeriod() == 1 ? unitRate : TvmFunctions.annualize(unitRate, frequency.unitsPerPeriod(), mc);
    }

    /**
     * {@code (TPV x periodPaymentRate x unitsPerPeriod) / unitsPerYear / 100}, rounded to the loan currency's decimal
     * places; for a daily schedule that is {@code (TPV x periodPaymentRate) / npvDayCount / 100}. Rounding is
     * intrinsic: the payment is what the borrower is billed, so it must be an amount actually payable in the loan
     * currency.
     *
     * <p>
     * A positive payment rate always bills something, so when the exact amount is positive but too small to survive the
     * rounding it is raised to one minor currency unit rather than collapsing to zero. Zero is not a payment the
     * borrower can make: it would leave the schedule with no way to repay the balance, and dividing the gross payable
     * by it to derive the term is undefined.
     */
    static BigDecimal periodPayment(final BigDecimal totalPaymentVolume, final BigDecimal periodPaymentRate, final int npvDayCount,
            final RepaymentFrequency frequency, final int currencyScale, final MathContext mc) {
        BigDecimal volumeAtRate = totalPaymentVolume.multiply(periodPaymentRate, mc);
        if (frequency.unitsPerPeriod() != 1) {
            volumeAtRate = volumeAtRate.multiply(BigDecimal.valueOf(frequency.unitsPerPeriod()), mc);
        }
        final BigDecimal exact = volumeAtRate.divide(BigDecimal.valueOf(frequency.unitsPerYear(npvDayCount)), mc)
                .divide(BigDecimal.valueOf(100), mc);
        final BigDecimal rounded = exact.setScale(currencyScale, mc.getRoundingMode());
        if (rounded.signum() == 0 && exact.signum() > 0) {
            return BigDecimal.ONE.movePointLeft(currencyScale);
        }
        return rounded;
    }

    static boolean isFlat(final WorkingCapitalAmortizationType amortizationType) {
        return amortizationType != null && amortizationType.isFlat();
    }

    /**
     * {@code discountFee / (netDisbursement + discountFee)}: the share of every payment a FLAT schedule earns as fee;
     * {@code null} for any other type.
     */
    static BigDecimal flatRatio(final WorkingCapitalAmortizationType amortizationType, final BigDecimal netDisbursement,
            final BigDecimal discountFee, final MathContext mc) {
        if (!isFlat(amortizationType)) {
            return null;
        }
        final BigDecimal grossPayable = netDisbursement.add(discountFee, mc);
        return grossPayable.signum() == 0 ? BigDecimal.ZERO : discountFee.divide(grossPayable, mc);
    }

    /**
     * Solves the parameters for a balance and the fee still unearned against it from TPV and period payment rate.
     *
     * @throws IllegalArgumentException
     *             when the inputs cannot produce a payable schedule
     */
    static Solved solve(final WorkingCapitalAmortizationType amortizationType, final BigDecimal balance, final BigDecimal unearnedFee,
            final BigDecimal totalPaymentVolume, final BigDecimal periodPaymentRate, final int npvDayCount,
            final RepaymentFrequency frequency, final int currencyScale, final MathContext mc) {
        final BigDecimal payment = periodPayment(totalPaymentVolume, periodPaymentRate, npvDayCount, frequency, currencyScale, mc);
        return solveFromKnownPayment(amortizationType, balance, unearnedFee, payment, mc, npvDayCount, frequency, currencyScale);
    }

    /**
     * Solves term, closing payment and IRR from an already-known period payment.
     *
     * @throws IllegalArgumentException
     *             when the inputs cannot produce a payable schedule
     */
    static Solved solveFromKnownPayment(final WorkingCapitalAmortizationType amortizationType, final BigDecimal balance,
            final BigDecimal unearnedFee, final BigDecimal payment, final MathContext mc, final int npvDayCount,
            final RepaymentFrequency frequency, final int currencyScale) {
        if (payment == null || payment.signum() <= 0) {
            throw new IllegalArgumentException("period payment must be positive");
        }
        final boolean flat = isFlat(amortizationType);
        // A FLAT schedule closes on the period the money collected, rounded to the currency, reaches the gross
        // payable, so its term is sized from the same rounded figure: a residual in the last decimal place of an exact
        // balance must not claim a closing period that bills nothing.
        final BigDecimal exactGross = balance.add(unearnedFee, mc);
        final BigDecimal grossPayable = flat ? exactGross.setScale(currencyScale, mc.getRoundingMode()) : exactGross;
        final BigDecimal fractionalTerm = grossPayable.divide(payment, mc);
        // Checked on the BigDecimal so int overflow cannot slip past the cap; the rate solver may still succeed on an
        // over-cap term via its zero-rate shortcut, so relying on that call to fail is not enough.
        if (fractionalTerm.compareTo(BigDecimal.valueOf(ProjectedAmortizationScheduleModel.MAX_CALCULABLE_TOTAL_DAYS)) > 0) {
            throw new IllegalStateException("schedule would run for " + fractionalTerm + " periods, above the calculable cap of "
                    + ProjectedAmortizationScheduleModel.MAX_CALCULABLE_TOTAL_DAYS);
        }
        final int term = fractionalTerm.setScale(0, RoundingMode.UP).intValueExact();
        if (term <= 0) {
            throw new IllegalArgumentException("computed term must be positive, got: " + term);
        }
        // The closing period pays only the remainder of the gross payable after the (term - 1) full payments. When the
        // schedule divides evenly this equals the period payment.
        final BigDecimal closing = grossPayable.subtract(payment.multiply(BigDecimal.valueOf(term - 1L), mc), mc);
        if (flat) {
            return new Solved(payment, closing, term, null, null);
        }
        final BigDecimal solvedEir = TvmFunctions.irr(cashFlows(balance, payment, closing, term), mc);
        final BigDecimal calculatedAnnualEir = calculatedAnnualEir(solvedEir, npvDayCount, frequency, mc);
        return new Solved(payment, closing, term, periodicRateFrom(calculatedAnnualEir, npvDayCount, frequency, mc), calculatedAnnualEir);
    }

    /**
     * {@code solved}, provided the annual EIR it carries is within
     * {@link ProjectedAmortizationScheduleModel#MAX_CALCULABLE_ANNUAL_EIR}.
     *
     * <p>
     * Applied only where the loan's EIR is set - the plan written at creation and the re-rating at a rate change - and
     * deliberately not inside the solver. The walk also solves a projection rate after an off-plan payment, purely so
     * the days still to come bill what is owed; that rate is never the loan's EIR, is neither stored nor published, and
     * capping it would only leave those days billing off what the borrower owes.
     *
     * @throws IllegalStateException
     *             when the annual EIR is above the cap
     */
    static Solved requireAnnualEirWithinCap(final Solved solved) {
        final BigDecimal calculatedAnnualEir = solved.calculatedAnnualEir();
        if (calculatedAnnualEir != null
                && calculatedAnnualEir.abs().compareTo(ProjectedAmortizationScheduleModel.MAX_CALCULABLE_ANNUAL_EIR) > 0) {
            throw new IllegalStateException("schedule solves to an annual EIR of " + calculatedAnnualEir
                    + " %, above the calculable cap of " + ProjectedAmortizationScheduleModel.MAX_CALCULABLE_ANNUAL_EIR + " %");
        }
        return solved;
    }

    /**
     * Finds the currency-rounded period payment whose NPV at the periodic rate compounded from {@code annualEirPercent}
     * equals {@code netDisbursement}, then derives term / closing / IRR exactly as TPV does for the same payment — so
     * the walk produces the same schedule as an equivalent period-payment-rate product. FLAT amortization still sizes
     * the payment from the target annual EIR, then earns fee via {@link #flatRatio}.
     */
    static Solved solveFromAnnualEir(final WorkingCapitalAmortizationType amortizationType, final BigDecimal netDisbursement,
            final BigDecimal discountFee, final BigDecimal annualEirPercent, final int npvDayCount, final RepaymentFrequency frequency,
            final int currencyScale, final MathContext mc) {
        if (discountFee == null || discountFee.signum() <= 0) {
            throw new IllegalArgumentException("discountFeeAmount must be positive for annual EIR strategy");
        }
        if (netDisbursement == null || netDisbursement.signum() <= 0) {
            throw new IllegalArgumentException("netDisbursementAmount must be positive");
        }
        if (npvDayCount <= 0) {
            throw new IllegalArgumentException("npvDayCount must be positive");
        }
        if (annualEirPercent == null || annualEirPercent.signum() <= 0) {
            throw new IllegalArgumentException("annualEir must be positive");
        }
        final BigDecimal periodicRate = compoundOverPeriod(
                TvmFunctions.periodicRateFromAnnualEir(annualEirPercent, frequency.unitsPerYear(npvDayCount), mc), frequency, mc);
        final BigDecimal payment = computePaymentFromAnnualEir(netDisbursement, discountFee, periodicRate, currencyScale, mc);
        return solveFromKnownPayment(amortizationType, netDisbursement, discountFee, payment, mc, npvDayCount, frequency, currencyScale);
    }

    /**
     * Solves for the currency-rounded period payment whose discounted repayment stream has NPV equal to
     * {@code netDisbursement}, using binary search over whole-cent candidates and a final three-cent tie-break.
     */
    static BigDecimal computePaymentFromAnnualEir(final BigDecimal netDisbursement, final BigDecimal discountFee,
            final BigDecimal periodicRate, final int currencyScale, final MathContext mc) {
        final BigDecimal totalRepayment = netDisbursement.add(discountFee, mc);

        BigDecimal lower = BigDecimal.ONE.movePointLeft(currencyScale);
        BigDecimal upper = totalRepayment;
        BigDecimal candidate;

        while (lower.compareTo(upper) < 0) {
            candidate = roundDownToCent(lower.add(upper, mc).divide(BigDecimal.valueOf(2), mc), currencyScale);
            final BigDecimal candidateNpv = npvForPayment(candidate, totalRepayment, periodicRate, mc);
            if (candidateNpv.compareTo(netDisbursement) < 0) {
                lower = candidate.add(BigDecimal.ONE.movePointLeft(currencyScale), mc);
            } else {
                upper = candidate;
            }
        }

        candidate = roundDownToCent(lower.add(upper, mc).divide(BigDecimal.valueOf(2), mc), currencyScale);
        final BigDecimal cent = BigDecimal.ONE.movePointLeft(currencyScale);
        BigDecimal bestPayment = candidate;
        BigDecimal bestError = npvError(candidate, totalRepayment, netDisbursement, periodicRate, mc);
        for (final BigDecimal neighbour : List.of(candidate.subtract(cent, mc), candidate.add(cent, mc))) {
            if (neighbour.compareTo(cent) >= 0 && neighbour.compareTo(totalRepayment) <= 0) {
                final BigDecimal error = npvError(neighbour, totalRepayment, netDisbursement, periodicRate, mc);
                if (error.compareTo(bestError) < 0) {
                    bestError = error;
                    bestPayment = neighbour;
                }
            }
        }
        return bestPayment.setScale(currencyScale, mc.getRoundingMode());
    }

    private static BigDecimal npvError(final BigDecimal payment, final BigDecimal totalRepayment, final BigDecimal netDisbursement,
            final BigDecimal periodicRate, final MathContext mc) {
        return npvForPayment(payment, totalRepayment, periodicRate, mc).subtract(netDisbursement, mc).abs();
    }

    private static BigDecimal npvForPayment(final BigDecimal payment, final BigDecimal totalRepayment, final BigDecimal periodicRate,
            final MathContext mc) {
        final int fullPaymentCount = totalRepayment.divide(payment, mc).setScale(0, RoundingMode.FLOOR).intValueExact();
        final BigDecimal totalRegularPayments = payment.multiply(BigDecimal.valueOf(fullPaymentCount), mc);
        final BigDecimal remainder = totalRepayment.subtract(totalRegularPayments, mc);

        if (periodicRate.signum() == 0) {
            return totalRegularPayments.add(remainder, mc);
        }

        final BigDecimal onePlusRate = BigDecimal.ONE.add(periodicRate, mc);
        final BigDecimal discountBase = BigDecimal.ONE.divide(onePlusRate.pow(fullPaymentCount, mc), mc);
        final BigDecimal pvRegular = payment.multiply(BigDecimal.ONE.subtract(discountBase, mc), mc).divide(periodicRate, mc);

        if (remainder.signum() == 0) {
            return pvRegular;
        }
        final BigDecimal pvRemainder = remainder.divide(onePlusRate.pow(fullPaymentCount + 1, mc), mc);
        return pvRegular.add(pvRemainder, mc);
    }

    private static BigDecimal roundDownToCent(final BigDecimal value, final int currencyScale) {
        return value.setScale(currencyScale, RoundingMode.DOWN);
    }

    /**
     * The cash-flow series the rate is solved against: {@code [-balance, payment x (term - 1), closing]}. The
     * period-zero flow is the negated balance; the periodic payments follow, the last being the remainder.
     */
    private static List<BigDecimal> cashFlows(final BigDecimal balance, final BigDecimal payment, final BigDecimal closing,
            final int term) {
        final List<BigDecimal> flows = new ArrayList<>(term + 1);
        flows.add(balance.negate());
        for (int i = 0; i < term - 1; i++) {
            flows.add(payment);
        }
        flows.add(closing);
        return flows;
    }

    /**
     * @param periodPayment
     *            what every period but the last bills
     * @param closingPayment
     *            what the last period of the solve bills instead - the remainder of the gross payable
     * @param term
     *            how many periods the solve takes to close, and so how long the rate is solved over
     * @param eir
     *            the periodic effective rate the schedule discounts on: {@code calculatedAnnualEir} spread back over
     *            the periods of a year; {@code null} for a FLAT schedule
     * @param calculatedAnnualEir
     *            the annual rate the schedule was priced at, compounded over the npv day count (or twelve months)
     *            rather than a calendar year and held as a percentage; {@code null} for a FLAT schedule
     */
    record Solved(BigDecimal periodPayment, BigDecimal closingPayment, int term, BigDecimal eir, BigDecimal calculatedAnnualEir) {
    }
}
