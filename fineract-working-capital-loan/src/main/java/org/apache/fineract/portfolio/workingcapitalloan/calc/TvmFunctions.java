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
import java.util.List;

/**
 * Time Value of Money (TVM) utility functions for working capital loan calculations.
 *
 * <ul>
 * <li>{@link #irr} — internal rate of return via Newton-Raphson (Excel IRR equivalent), for an arbitrary cash-flow
 * series (e.g. a schedule whose final payment is a smaller remainder)</li>
 * <li>{@link #annualize} — compounds a periodic rate over a year: {@code (1 + r)^n − 1}</li>
 * <li>{@link #deannualize} — the inverse, solving the nth root by Newton-Raphson: {@code (1 + annual)^(1/n) − 1}</li>
 * <li>{@link #discountFactor} — present value discount factor: {@code 1 / (1 + r)^days}</li>
 * </ul>
 */
public final class TvmFunctions {

    private static final int MAX_ITERATIONS = 500;
    private static final BigDecimal TOLERANCE = new BigDecimal("1E-12");
    private static final BigDecimal DEFAULT_GUESS = new BigDecimal("0.01");
    private static final BigDecimal MIN_GUESS = new BigDecimal("1E-9");
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final int NTH_ROOT_MAX_ITERATIONS = 50;
    private static final BigDecimal NTH_ROOT_TOLERANCE = new BigDecimal("1E-15");

    private TvmFunctions() {}

    /**
     * Solves for the internal rate of return, deriving the initial Newton-Raphson guess from the cash-flow series via
     * {@link #estimateInitialGuess} (day-0 flow as the present value, the first payment as the representative periodic
     * payment). The linear estimate is a close, stable seed for the IRR search, so no separate uniform-annuity solve is
     * needed.
     *
     * @see #irr(List, BigDecimal, MathContext)
     */
    public static BigDecimal irr(final List<BigDecimal> cashFlows, final MathContext mc) {
        if (cashFlows == null || cashFlows.size() < 2) {
            throw new IllegalArgumentException("cashFlows must contain at least two entries");
        }
        final int nper = cashFlows.size() - 1;
        final BigDecimal pv = cashFlows.get(0).negate();
        final BigDecimal pmt = cashFlows.get(1).negate();
        return irr(cashFlows, estimateInitialGuess(nper, pmt, pv, mc), mc);
    }

    /**
     * Solves for the internal rate of return of an arbitrary cash-flow series. Equivalent to Excel
     * {@code IRR(values, guess)}.
     *
     * <p>
     * Finds {@code r} satisfying: {@code Σ cf[k] / (1 + r)^k = 0} for {@code k = 0 .. cashFlows.size()-1}, where
     * {@code cf[0]} is the day-0 flow (typically the negated outstanding balance).
     *
     * <p>
     * Does not assume a uniform payment, so it correctly handles a schedule whose final payment is a smaller remainder.
     * Seed the search with a linear rate estimate (see {@link #estimateInitialGuess}) for fast, stable convergence.
     *
     * @param cashFlows
     *            the cash-flow series, index 0 = day-0 flow; must contain at least one sign change
     * @param guess
     *            the initial Newton-Raphson guess (see {@link #estimateInitialGuess})
     * @param mc
     *            math context for precision
     * @return the periodic internal rate of return
     * @throws IllegalArgumentException
     *             if {@code cashFlows} is null or has fewer than two entries
     * @throws IllegalStateException
     *             if Newton-Raphson does not converge or hits a zero derivative
     */
    public static BigDecimal irr(final List<BigDecimal> cashFlows, final BigDecimal guess, final MathContext mc) {
        if (cashFlows == null || cashFlows.size() < 2) {
            throw new IllegalArgumentException("cashFlows must contain at least two entries");
        }

        // Zero-rate case: the r→0 limit of the NPV is the plain (undiscounted) sum of the cash flows. When that is
        // ≈ 0 the IRR is 0 (e.g. a no-discount loan whose payments exactly repay the balance). Short-circuit so the
        // result is exactly zero rather than a tiny Newton residual.
        BigDecimal undiscountedSum = BigDecimal.ZERO;
        for (final BigDecimal cf : cashFlows) {
            undiscountedSum = undiscountedSum.add(cf, mc);
        }
        if (undiscountedSum.abs().compareTo(TOLERANCE) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal r = guess;

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            if (r.signum() == 0) {
                r = TOLERANCE; // nudge away from zero to avoid division by zero
            }

            final BigDecimal onePlusR = BigDecimal.ONE.add(r, mc);

            // f(r) = Σ cf[k] / (1+r)^k
            // f'(r) = Σ −k·cf[k] / (1+r)^(k+1)
            BigDecimal f = BigDecimal.ZERO;
            BigDecimal fPrime = BigDecimal.ZERO;
            BigDecimal discount = BigDecimal.ONE; // (1+r)^-k, starts at k=0
            for (int k = 0; k < cashFlows.size(); k++) {
                final BigDecimal cf = cashFlows.get(k);
                f = f.add(cf.multiply(discount, mc), mc);
                if (k > 0) {
                    // −k·cf / (1+r)^(k+1) = −k·cf · discount / (1+r)
                    final BigDecimal term = BigDecimal.valueOf(k).multiply(cf, mc).multiply(discount, mc).divide(onePlusR, mc);
                    fPrime = fPrime.subtract(term, mc);
                }
                discount = discount.divide(onePlusR, mc);
            }

            if (fPrime.signum() == 0) {
                throw new IllegalStateException("IRR: zero derivative at iteration " + iter + ", r=" + r);
            }

            final BigDecimal correction = f.divide(fPrime, mc);
            r = r.subtract(correction, mc);

            if (correction.abs().compareTo(TOLERANCE) < 0) {
                return r;
            }
        }

        throw new IllegalStateException("IRR did not converge after " + MAX_ITERATIONS + " iterations");
    }

    /**
     * Linear approximation for the initial Newton-Raphson guess: {@code r ≈ 2·(pmt·n + pv) / (pv·n)}.
     *
     * <p>
     * When total payments exceed the present value (typical for loans with origination fees), the formula yields a
     * negative estimate. Its <em>absolute</em> value is still a good approximation of the periodic rate — it equals
     * {@code 2·interest / (pv·n)} — so we return {@code |estimate|} instead of a fixed default. This avoids
     * catastrophic divergence in Newton-Raphson when {@code nper} is large (e.g., daily-payment loans with thousands of
     * periods), where the old default of 0.01 caused {@code (1+0.01)^nper} to explode.
     */
    private static BigDecimal estimateInitialGuess(final int nper, final BigDecimal pmt, final BigDecimal pv, final MathContext mc) {
        final BigDecimal n = BigDecimal.valueOf(nper);
        final BigDecimal pvTimesN = pv.multiply(n, mc);
        if (pvTimesN.signum() == 0) {
            return DEFAULT_GUESS;
        }
        final BigDecimal estimate = pmt.multiply(n, mc).add(pv, mc).multiply(TWO, mc).divide(pvTimesN, mc);
        if (estimate.signum() == 0) {
            return DEFAULT_GUESS;
        }
        return estimate.abs().max(MIN_GUESS);
    }

    /**
     * Compounds a periodic rate over a year: {@code (1 + periodicRate)^periodsPerYear − 1}.
     *
     * <p>
     * The exponent is the caller's day-count convention, not a fixed 365: a loan priced on 360 days has to annualise on
     * 360, or the rate it is reported at will not be the rate its schedule runs on.
     *
     * @param periodicRate
     *            the periodic rate to compound, typically an {@link #irr} result
     * @param periodsPerYear
     *            number of periods in a year, i.e. the day-count convention
     * @param mc
     *            math context for precision
     * @return the equivalent annual rate
     * @throws IllegalArgumentException
     *             if periodsPerYear is not positive, or periodicRate is not greater than -1
     */
    public static BigDecimal annualize(final BigDecimal periodicRate, final int periodsPerYear, final MathContext mc) {
        if (periodsPerYear <= 0) {
            throw new IllegalArgumentException("periodsPerYear must be positive, got: " + periodsPerYear);
        }
        final BigDecimal base = BigDecimal.ONE.add(periodicRate, mc);
        if (base.signum() <= 0) {
            throw new IllegalArgumentException("periodicRate must be greater than -1, got: " + periodicRate);
        }
        return base.pow(periodsPerYear, mc).subtract(BigDecimal.ONE, mc);
    }

    /**
     * Inverts {@link #annualize}: {@code (1 + annualRate)^(1/periodsPerYear) − 1}, solved by Newton-Raphson from the
     * seed in {@link #nthRootSeed}.
     *
     * @param annualRate
     *            the annual rate to spread back across the year
     * @param periodsPerYear
     *            number of periods in a year, i.e. the day-count convention
     * @param mc
     *            math context for precision
     * @return the equivalent periodic rate, exactly zero when annualRate is zero
     * @throws IllegalArgumentException
     *             if periodsPerYear is not positive, or annualRate is not greater than -1
     * @throws IllegalStateException
     *             if Newton-Raphson does not converge
     */
    public static BigDecimal deannualize(final BigDecimal annualRate, final int periodsPerYear, final MathContext mc) {
        if (periodsPerYear <= 0) {
            throw new IllegalArgumentException("periodsPerYear must be positive, got: " + periodsPerYear);
        }
        if (annualRate.signum() == 0) {
            return BigDecimal.ZERO;
        }
        final BigDecimal base = BigDecimal.ONE.add(annualRate, mc);
        if (base.signum() <= 0) {
            throw new IllegalArgumentException("annualRate must be greater than -1, got: " + annualRate);
        }

        final BigDecimal n = BigDecimal.valueOf(periodsPerYear);
        final BigDecimal nMinusOne = BigDecimal.valueOf(periodsPerYear - 1L);
        BigDecimal root = nthRootSeed(base, periodsPerYear);

        for (int iter = 0; iter < NTH_ROOT_MAX_ITERATIONS; iter++) {
            final BigDecimal rootPower = root.pow(periodsPerYear - 1, mc);
            final BigDecimal next = nMinusOne.multiply(root, mc).add(base.divide(rootPower, mc), mc).divide(n, mc);
            final BigDecimal correction = next.subtract(root, mc);
            root = next;

            if (correction.abs().compareTo(NTH_ROOT_TOLERANCE) < 0) {
                return root.subtract(BigDecimal.ONE, mc);
            }
        }

        throw new IllegalStateException("nth root did not converge after " + NTH_ROOT_MAX_ITERATIONS + " iterations");
    }

    /**
     * Starting point for the nth-root search, taken from {@code base}'s decimal exponent rather than from
     * {@code base.doubleValue()}, which is Infinity once the annual rate outgrows a {@code double} — reachable on a
     * schedule solved off a tiny balance against an unearned fee. Splitting the exponent by the root keeps every
     * {@code double} step in range, and the whole part is applied exactly on the {@link BigDecimal}.
     */
    private static BigDecimal nthRootSeed(final BigDecimal base, final int periodsPerYear) {
        final int exponent = base.precision() - base.scale() - 1;
        final double mantissa = base.movePointLeft(exponent).doubleValue();
        final int wholePart = Math.floorDiv(exponent, periodsPerYear);
        final int remainder = Math.floorMod(exponent, periodsPerYear);
        final double head = Math.pow(mantissa, 1.0 / periodsPerYear) * Math.pow(10.0, (double) remainder / periodsPerYear);
        return BigDecimal.valueOf(head).scaleByPowerOfTen(wholePart);
    }

    /**
     * Computes the discount factor: {@code 1 / (1 + rate)^days}.
     *
     * @param rate
     *            the periodic rate to discount by, typically an {@link #irr} result
     * @param days
     *            number of periods to discount
     * @param mc
     *            math context for precision
     * @return the discount factor (1.0 when days=0)
     * @throws IllegalArgumentException
     *             if days is negative or exceeds {@link Integer#MAX_VALUE}
     */
    public static BigDecimal discountFactor(final BigDecimal rate, final long days, final MathContext mc) {
        if (days == 0) {
            return BigDecimal.ONE;
        }
        if (days < 0 || days > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("days must be in [0, " + Integer.MAX_VALUE + "], got: " + days);
        }
        return BigDecimal.ONE.divide(BigDecimal.ONE.add(rate, mc).pow((int) days, mc), mc);
    }
}
