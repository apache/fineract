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
import org.apache.fineract.infrastructure.core.service.MathUtil;

/**
 * How much of the discount fee a given amount of money has earned.
 *
 * <p>
 * A working capital loan amortizes its deferred fee against money received: what matters is how much has come in, not
 * when. A loan of 900 plus a 100 fee that is repaid in full on its first day has earned the whole 100 that day, and its
 * schedule is one day long. A payment of double the instalment earns two days of fee and takes a day off the end. A day
 * that goes by unpaid earns nothing and pushes the end out by a day.
 *
 * <p>
 * So the fee earned is a function of one variable - the money collected so far - and that function is the schedule's
 * own declining-balance recursion, run on the instalments it plans to bill rather than on the payments that really
 * arrived. This class is that recursion, held as a cursor.
 *
 * <p>
 * It is a cursor rather than a precomputed table because it is only ever read at a total that has grown: money
 * collected never goes down. So it walks forward on demand, one plan instalment at a time, and over a whole schedule it
 * takes no more steps than the schedule has days. Reading it between two of its own steps interpolates within that
 * step, which is what earns a partly-covered day a proportional share of its fee.
 *
 * <p>
 * A rate change repositions it onto the day's reality rather than restarting it. Fee earned under the old rate stays
 * earned - the change rewrites what is still to come, not what already happened.
 */
final class PlanCursor {

    private final MathContext mc;
    private final BigDecimal totalPaymentVolume;
    private final int npvDayCount;
    private final int currencyScale;

    /** Balance the plan has drawn down to at the cursor's position. */
    private BigDecimal balance;
    /** Fee the plan has earned up to the cursor's position, in full precision. */
    private BigDecimal earned;
    /** Money the plan has billed up to the cursor's position. */
    private BigDecimal billed;

    /** Position and earnings at the cursor's previous step, so a read between the two can interpolate. */
    private BigDecimal previousEarned;
    private BigDecimal previousBilled;

    private AmortizationParams.Solved solved;
    /** Steps taken since the last solve, so the closing remainder is billed on the right one. */
    private int stepsInSolve;
    private boolean exhausted;

    PlanCursor(final BigDecimal netDisbursement, final BigDecimal discountFee, final BigDecimal totalPaymentVolume,
            final BigDecimal periodPaymentRate, final int npvDayCount, final int currencyScale, final MathContext mc) {
        this.mc = mc;
        this.totalPaymentVolume = totalPaymentVolume;
        this.npvDayCount = npvDayCount;
        this.currencyScale = currencyScale;
        this.balance = netDisbursement;
        this.earned = BigDecimal.ZERO;
        this.billed = BigDecimal.ZERO;
        this.previousEarned = BigDecimal.ZERO;
        this.previousBilled = BigDecimal.ZERO;
        this.solved = AmortizationParams.solve(netDisbursement, discountFee, totalPaymentVolume, periodPaymentRate, npvDayCount,
                currencyScale, mc);
        this.stepsInSolve = 0;
        this.exhausted = false;
    }

    /** The rate currently driving the plan, which is also the rate the schedule bills at. */
    AmortizationParams.Solved solved() {
        return solved;
    }

    /**
     * Repositions the cursor onto the schedule's real position and re-solves the plan from there at a new rate.
     *
     * <p>
     * The fee already earned is carried across untouched, and the cursor is set level with the money already collected,
     * so the read that follows starts where the last one finished. Nothing the borrower has earned is un-earned and
     * nothing is earned twice.
     */
    void changeRateTo(final BigDecimal periodPaymentRate, final BigDecimal balanceNow, final BigDecimal unearnedFee,
            final BigDecimal collectedSoFar) {
        if (balanceNow.signum() <= 0) {
            throw new IllegalArgumentException("balance at a rate change must be positive, got: " + balanceNow);
        }
        // Read the cursor before moving it. It sits at the end of whichever plan instalment the money collected reached
        // into, which is past the money itself, and the fee earned is the value interpolated back to the money - not
        // the
        // whole instalment the cursor happens to be standing on. Carrying the cursor's own position across as the new
        // baseline would hand the borrower the rest of that instalment's fee for free, and a rate change would appear
        // to
        // restate fee that was already earned.
        final BigDecimal earnedAtCollected = feeEarnedAt(collectedSoFar);
        this.balance = balanceNow;
        this.earned = earnedAtCollected;
        this.billed = collectedSoFar;
        this.previousEarned = earnedAtCollected;
        this.previousBilled = collectedSoFar;
        this.stepsInSolve = 0;
        this.exhausted = false;
        this.solved = AmortizationParams.solve(this.balance, MathUtil.negativeToZero(unearnedFee), totalPaymentVolume, periodPaymentRate,
                npvDayCount, currencyScale, mc);
    }

    /**
     * The fee earned once {@code collected} has come in.
     *
     * <p>
     * Steps the plan forward until it has billed at least as much as has been collected, then reads back to
     * {@code collected} within the step it landed in. Once the plan has nothing left to bill the fee stops growing:
     * money handed over beyond the payable earns nothing, because there is nothing left to earn.
     */
    BigDecimal feeEarnedAt(final BigDecimal collected) {
        if (collected.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        while (!exhausted && billed.compareTo(collected) < 0) {
            step();
        }
        if (billed.compareTo(collected) <= 0) {
            return earned;
        }
        // Landed past the money: the cursor stepped over it, so read back into the step it is standing in. The fee
        // that step earns is spread across the money it bills, and the money collected covers part of it.
        final BigDecimal span = billed.subtract(previousBilled, mc);
        if (span.signum() <= 0) {
            return earned;
        }
        final BigDecimal into = collected.subtract(previousBilled, mc);
        final BigDecimal fractionOfTheStepPaid = into.divide(span, mc);
        final BigDecimal feeTheStepEarns = earned.subtract(previousEarned, mc);
        return previousEarned.add(feeTheStepEarns.multiply(fractionOfTheStepPaid, mc), mc);
    }

    /** One plan instalment: accrue on the balance, bill what the plan asks for, and draw the balance down. */
    private void step() {
        stepsInSolve++;
        final BigDecimal grown = balance.multiply(BigDecimal.ONE.add(solved.eir(), mc), mc);
        // The instalment, capped at the balance there is to close - the same rule the schedule bills by, so a borrower
        // paying exactly to plan earns exactly what the plan projected rather than nearly it. The plan's last step
        // bills what is left on it, which the cap already gives; naming a separate closing amount here would have that
        // step bill the remainder the plan was solved for even once a repositioning had left it owing more.
        final BigDecimal instalment = MathUtil.negativeToZero(solved.dailyPayment()).min(MathUtil.negativeToZero(grown));
        if (instalment.signum() <= 0) {
            // Nothing left to bill: the plan has run out and the fee it holds is all there is to earn.
            exhausted = true;
            return;
        }
        previousEarned = earned;
        previousBilled = billed;
        earned = earned.add(grown.subtract(balance, mc), mc);
        billed = billed.add(instalment, mc);
        balance = grown.subtract(instalment, mc);
    }
}
