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
 * The one day of the declining-balance recursion that {@link AmortizationWalk} bills by and {@link PlanCursor} earns
 * by, so the two types differ here and nowhere else.
 */
final class AmortizationStep {

    private AmortizationStep() {}

    /**
     * One day of the declining-balance recursion: what it bills, what fee it earns and what it leaves owing.
     *
     * <p>
     * Every day asks for the instalment, and no day can ask for more than the balance it has to close. That second
     * clause is what closes the loan: on the day it runs out the balance is less than an instalment, so the day bills
     * the balance and nothing is left. It needs no separate closing amount to do it - the rate was solved so that the
     * balance reaches exactly that remainder on exactly that day, and where a payment has restated the balance since,
     * what is owed is what the day should bill rather than what the plan once predicted would be.
     *
     * <p>
     * Under EIR the balance grows by the rate and the day earns the growth. Under FLAT the day earns {@code ratio} of
     * what it bills and the balance falls by the rest, so the payment that closes it is the balance grossed back up by
     * its own fee share; a day billing that much is snapped to nothing owed rather than left with the residual the
     * division leaves in the last decimal place.
     */
    static DayStep project(final BigDecimal balance, final BigDecimal dailyPayment, final BigDecimal eir, final BigDecimal flatRatio,
            final MathContext mc) {
        final BigDecimal owed = MathUtil.negativeToZero(balance);
        final BigDecimal asked = MathUtil.negativeToZero(dailyPayment);
        if (flatRatio != null) {
            final BigDecimal clearingPayment = owed.divide(BigDecimal.ONE.subtract(flatRatio, mc), mc);
            if (asked.compareTo(clearingPayment) >= 0) {
                return new DayStep(clearingPayment, clearingPayment.subtract(owed, mc), BigDecimal.ZERO);
            }
            final BigDecimal fee = asked.multiply(flatRatio, mc);
            return new DayStep(asked, fee, balance.subtract(asked, mc).add(fee, mc));
        }
        final BigDecimal grown = balance.multiply(BigDecimal.ONE.add(eir, mc), mc);
        final BigDecimal instalment = asked.min(MathUtil.negativeToZero(grown));
        return new DayStep(instalment, grown.subtract(balance, mc), grown.subtract(instalment, mc));
    }

    /** What a day bills, the fee it earns and the balance it leaves, all in full precision. */
    record DayStep(BigDecimal instalment, BigDecimal fee, BigDecimal balanceAfter) {
    }
}
