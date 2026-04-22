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

/**
 * Time Value of Money (TVM) utility functions for working capital loan calculations.
 *
 * <ul>
 * <li>{@link #rate} — periodic interest rate via Newton-Raphson (Excel RATE equivalent)</li>
 * <li>{@link #discountFactor} — present value discount factor: {@code 1 / (1 + r)^days}</li>
 * </ul>
 */
public final class TvmFunctions {

    private static final int MAX_ITERATIONS = 100;
    private static final BigDecimal TOLERANCE = new BigDecimal("1E-10");
    private static final BigDecimal DEFAULT_GUESS = new BigDecimal("0.01");
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private TvmFunctions() {}

    /**
     * Solves for the periodic interest rate in the present-value annuity equation. Equivalent to Excel
     * {@code RATE(nper, pmt, pv)} with {@code fv=0, type=0}.
     *
     * <p>
     * Finds {@code r} satisfying: {@code pv·(1+r)^n + pmt·((1+r)^n − 1)/r = 0}
     *
     * <p>
     * Initial guess is derived via linear approximation: {@code r ≈ 2·(pmt·n + pv) / (pv·n)}
     *
     * @param nper
     *            number of periods (must be positive)
     * @param pmt
     *            payment per period (negative = outgoing cash flow)
     * @param pv
     *            present value (positive = loan disbursement)
     * @param mc
     *            math context for precision
     * @return the periodic interest rate
     * @throws IllegalArgumentException
     *             if nper &lt;= 0
     * @throws IllegalStateException
     *             if Newton-Raphson does not converge
     */
    public static BigDecimal rate(final int nper, final BigDecimal pmt, final BigDecimal pv, final MathContext mc) {
        return rate(nper, pmt, pv, estimateInitialGuess(nper, pmt, pv, mc), mc);
    }

    /**
     * Solves for the periodic interest rate with an explicit initial guess.
     *
     * @see #rate(int, BigDecimal, BigDecimal, MathContext)
     */
    public static BigDecimal rate(final int nper, final BigDecimal pmt, final BigDecimal pv, final BigDecimal guess, final MathContext mc) {
        if (nper <= 0) {
            throw new IllegalArgumentException("nper must be positive, got: " + nper);
        }

        final BigDecimal n = BigDecimal.valueOf(nper);

        // Zero-rate case: pv + pmt·n ≈ 0
        if (pv.add(pmt.multiply(n, mc), mc).abs().compareTo(TOLERANCE) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal r = guess;

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            if (r.signum() == 0) {
                r = TOLERANCE; // nudge away from zero to avoid division by zero
            }

            final BigDecimal onePlusR = BigDecimal.ONE.add(r, mc);
            final BigDecimal compound = onePlusR.pow(nper, mc); // (1+r)^n
            final BigDecimal compoundMinusOne = compound.subtract(BigDecimal.ONE, mc); // (1+r)^n − 1

            // f(r) = pv·(1+r)^n + pmt·((1+r)^n − 1) / r
            final BigDecimal f = pv.multiply(compound, mc).add(pmt.multiply(compoundMinusOne, mc).divide(r, mc));

            // f'(r) = pv·n·(1+r)^(n−1) + pmt·[n·(1+r)^(n−1)·r − ((1+r)^n−1)] / r²
            final BigDecimal dCompound = n.multiply(onePlusR.pow(nper - 1, mc), mc); // n·(1+r)^(n−1)
            final BigDecimal rSquared = r.multiply(r, mc);
            final BigDecimal fPrime = pv.multiply(dCompound, mc)
                    .add(pmt.multiply(dCompound.multiply(r, mc).subtract(compoundMinusOne, mc), mc).divide(rSquared, mc));

            if (fPrime.signum() == 0) {
                throw new IllegalStateException("RATE: zero derivative at iteration " + iter + ", r=" + r);
            }

            final BigDecimal correction = f.divide(fPrime, mc);
            r = r.subtract(correction, mc);

            if (correction.abs().compareTo(TOLERANCE) < 0) {
                return r;
            }
        }

        throw new IllegalStateException("RATE did not converge after " + MAX_ITERATIONS + " iterations");
    }

    /**
     * Linear approximation for the initial Newton-Raphson guess: {@code r ≈ 2·(pmt·n + pv) / (pv·n)}. Falls back to
     * {@value #DEFAULT_GUESS} if the estimate is non-positive.
     */
    private static BigDecimal estimateInitialGuess(final int nper, final BigDecimal pmt, final BigDecimal pv, final MathContext mc) {
        final BigDecimal n = BigDecimal.valueOf(nper);
        final BigDecimal pvTimesN = pv.multiply(n, mc);
        if (pvTimesN.signum() == 0) {
            return DEFAULT_GUESS;
        }
        final BigDecimal estimate = pmt.multiply(n, mc).add(pv, mc).multiply(TWO, mc).divide(pvTimesN, mc);
        return estimate.signum() > 0 ? estimate : DEFAULT_GUESS;
    }

    /**
     * Computes the discount factor: {@code 1 / (1 + eir)^days}.
     *
     * @param eir
     *            effective interest rate per period
     * @param days
     *            number of periods to discount
     * @param mc
     *            math context for precision
     * @return the discount factor (1.0 when days=0)
     * @throws IllegalArgumentException
     *             if days is negative or exceeds {@link Integer#MAX_VALUE}
     */
    public static BigDecimal discountFactor(final BigDecimal eir, final long days, final MathContext mc) {
        if (days == 0) {
            return BigDecimal.ONE;
        }
        if (days < 0 || days > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("days must be in [0, " + Integer.MAX_VALUE + "], got: " + days);
        }
        return BigDecimal.ONE.divide(BigDecimal.ONE.add(eir, mc).pow((int) days, mc), mc);
    }
}
