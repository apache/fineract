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

/**
 * The amortization parameters of a schedule: what the borrower is billed each day, how many days that takes, what the
 * closing day bills instead, and the periodic rate that makes the three consistent.
 *
 * <p>
 * Derived identically for the original schedule and for every rate change, so a segment of the schedule is never a
 * different kind of thing from the schedule itself - it is the same solve run on whatever balance and unearned fee are
 * left at the day it starts.
 *
 * <p>
 * The rate starts as the IRR of the exact cash flow the borrower will pay, closing remainder included, and is then put
 * through the annual rate the loan is published at: rounded to the reported scale a year and spread back over the day
 * count. The schedule therefore discounts on the rate the loan reports rather than on one a few digits away from it.
 */
final class AmortizationParams {

    private static final int ANNUAL_EIR_SCALE = 6;

    /**
     * Fixed rather than the tenant's money rounding mode: a rate is not money, and this one is spread back into the
     * rate the whole schedule discounts on, so configuration would amortize the same loan differently per tenant.
     */
    private static final RoundingMode ANNUAL_EIR_ROUNDING = RoundingMode.HALF_EVEN;

    private AmortizationParams() {}

    /**
     * Single definition of the reported rate — the value stored on a schedule and the derive-on-read fallback for the
     * schedules written before it was stored must round identically.
     */
    static BigDecimal annualRate(final BigDecimal periodicRate, final int npvDayCount, final MathContext mc) {
        return TvmFunctions.annualize(periodicRate, npvDayCount, mc).setScale(ANNUAL_EIR_SCALE, ANNUAL_EIR_ROUNDING);
    }

    /**
     * {@code (TPV x periodPaymentRate) / npvDayCount / 100}, rounded to the loan currency's decimal places. Rounding is
     * intrinsic: the daily payment is what the borrower is billed, so it must be an amount actually payable in the loan
     * currency.
     *
     * <p>
     * A positive payment rate always bills something, so when the exact amount is positive but too small to survive the
     * rounding it is raised to one minor currency unit rather than collapsing to zero. Zero is not a payment the
     * borrower can make: it would leave the schedule with no way to repay the balance, and dividing the gross payable
     * by it to derive the term is undefined.
     */
    static BigDecimal dailyPayment(final BigDecimal totalPaymentVolume, final BigDecimal periodPaymentRate, final int npvDayCount,
            final int currencyScale, final MathContext mc) {
        final BigDecimal exact = totalPaymentVolume.multiply(periodPaymentRate, mc).divide(BigDecimal.valueOf(npvDayCount), mc)
                .divide(BigDecimal.valueOf(100), mc);
        final BigDecimal rounded = exact.setScale(currencyScale, mc.getRoundingMode());
        if (rounded.signum() == 0 && exact.signum() > 0) {
            return BigDecimal.ONE.movePointLeft(currencyScale);
        }
        return rounded;
    }

    /**
     * Solves the parameters for a balance and the fee still unearned against it.
     *
     * @throws IllegalArgumentException
     *             when the inputs cannot produce a payable schedule
     */
    static Solved solve(final BigDecimal balance, final BigDecimal unearnedFee, final BigDecimal totalPaymentVolume,
            final BigDecimal periodPaymentRate, final int npvDayCount, final int currencyScale, final MathContext mc) {
        final BigDecimal daily = dailyPayment(totalPaymentVolume, periodPaymentRate, npvDayCount, currencyScale, mc);
        if (daily.signum() <= 0) {
            throw new IllegalArgumentException("daily payment must be positive (check totalPaymentVolume and periodPaymentRate)");
        }
        final BigDecimal grossPayable = balance.add(unearnedFee, mc);
        final BigDecimal fractionalTerm = grossPayable.divide(daily, mc);
        // Checked on the BigDecimal so int overflow cannot slip past the cap; the rate solver may still succeed on an
        // over-cap term via its zero-rate shortcut, so relying on that call to fail is not enough.
        if (fractionalTerm.compareTo(BigDecimal.valueOf(ProjectedAmortizationScheduleModel.MAX_CALCULABLE_TOTAL_DAYS)) > 0) {
            throw new IllegalStateException("schedule would run for " + fractionalTerm + " days, above the calculable cap of "
                    + ProjectedAmortizationScheduleModel.MAX_CALCULABLE_TOTAL_DAYS);
        }
        final int term = fractionalTerm.setScale(0, RoundingMode.UP).intValueExact();
        if (term <= 0) {
            throw new IllegalArgumentException("computed term must be positive, got: " + term);
        }
        // The closing day pays only the remainder of the gross payable after the (term - 1) full daily payments. When
        // the schedule divides evenly this equals the daily payment.
        final BigDecimal closing = grossPayable.subtract(daily.multiply(BigDecimal.valueOf(term - 1L), mc), mc);
        final BigDecimal solvedEir = TvmFunctions.irr(cashFlows(balance, daily, closing, term), mc);
        final BigDecimal annualEir = annualRate(solvedEir, npvDayCount, mc);
        return new Solved(daily, closing, term, TvmFunctions.deannualize(annualEir, npvDayCount, mc), annualEir);
    }

    /**
     * The cash-flow series the rate is solved against: {@code [-balance, daily x (term - 1), closing]}. The day-zero
     * flow is the negated balance; the periodic payments follow, the last being the remainder.
     */
    private static List<BigDecimal> cashFlows(final BigDecimal balance, final BigDecimal daily, final BigDecimal closing, final int term) {
        final List<BigDecimal> flows = new ArrayList<>(term + 1);
        flows.add(balance.negate());
        for (int i = 0; i < term - 1; i++) {
            flows.add(daily);
        }
        flows.add(closing);
        return flows;
    }

    /**
     * @param dailyPayment
     *            what every day but the last bills
     * @param closingPayment
     *            what the last day of the solve bills instead - the remainder of the gross payable
     * @param term
     *            how many days the solve takes to close, and so how long the rate is solved over
     * @param eir
     *            the periodic effective rate the schedule discounts on: {@code annualEir} spread back over the day
     *            count
     * @param annualEir
     *            the annual rate the schedule was priced at, compounded over the day count rather than a calendar year
     */
    record Solved(BigDecimal dailyPayment, BigDecimal closingPayment, int term, BigDecimal eir, BigDecimal annualEir) {
    }
}
