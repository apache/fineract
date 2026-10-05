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
import java.time.LocalDate;

/**
 * One day of the amortization walk, in full precision and before anything is rounded for display.
 *
 * <p>
 * Every figure the finished schedule shows is either one of these fields or a subtraction of two of them, so the walk
 * is the single account of what happened on a day and the row built from it adds no arithmetic of its own.
 *
 * <p>
 * Two tracks run side by side. The <em>expected</em> track is the plan as it currently stands - restated from reality
 * wherever reality is known. The <em>actual</em> track is what the money that has come in has really earned. Each keeps
 * a high-precision running total and a normalized one; the normalized total is the rounded form of the high-precision
 * one, which is what stops the fee drifting away from itself over a few hundred days.
 *
 * @param dayIndex
 *            1-based day of the schedule
 * @param eir
 *            the periodic rate in force on this day
 * @param paymentsLeft
 *            discount-factor exponent, counted within the rate currently in force
 * @param discountFactor
 *            {@code 1 / (1 + eir) ^ paymentsLeft}
 * @param billedInstalment
 *            what the day asks for: the instalment in force, capped at the balance it has to close
 * @param npvValue
 *            present value of what the day contributes - real money where it is known, the billed instalment where it
 *            is still a forecast, and nothing for a day that elapsed unpaid
 * @param paidAmount
 *            money recorded against this day; {@code null} when the day carries no record at all
 * @param collected
 *            every payment recorded up to and including this day
 * @param balance
 *            what is still owed after the day, carried in full precision
 * @param actualBalance
 *            the same, driven by money that really moved rather than by instalments assumed. Never below nothing: a
 *            borrower who has overpaid owes nothing, and the excess is the loan's overpayment rather than a negative
 *            balance
 * @param normExpected
 *            the day's share of the fee on the expected track, already a whole number of minor units
 * @param aggNormExpected
 *            expected fee earned up to and including this day, normalized
 * @param normActual
 *            the day's share of the fee earned by money actually received
 * @param aggNormActual
 *            actual fee earned up to and including this day, normalized
 * @param hasPayment
 *            a positive payment landed on this day
 * @param elapsed
 *            the day is behind the date the schedule has been calculated to, so its actual columns are known even if
 *            nothing was paid
 */
record AmortizationDay(int dayIndex, LocalDate date, BigDecimal eir, long paymentsLeft, BigDecimal discountFactor,
        BigDecimal billedInstalment, BigDecimal npvValue, BigDecimal paidAmount, BigDecimal collected, BigDecimal balance,
        BigDecimal actualBalance, BigDecimal normExpected, BigDecimal aggNormExpected, BigDecimal normActual, BigDecimal aggNormActual,
        boolean hasPayment, boolean elapsed) {
}
