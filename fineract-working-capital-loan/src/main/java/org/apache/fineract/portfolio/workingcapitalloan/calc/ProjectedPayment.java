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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.domain.Money;

/**
 * Single payment of a Working Capital loan's projected amortization schedule.
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class ProjectedPayment {

    /** 1-based payment number (0 = disbursement row). */
    private final int paymentNo;

    private final LocalDate date;

    /** Remaining periods for calculation: {@code loanTerm + appliedPaymentCount - paymentNo}. */
    private final long count;

    /** Exponent for discount factor: {@code DF = 1/(1+EIR)^paymentsLeft}. Zero for paid periods. */
    private final long paymentsLeft;

    /** {@code (TPV × periodRate) / dayCount}; negated disbursement for row 0. */
    private final Money expectedPaymentAmount;

    /**
     * Running expected payment: always the expected amount, adjusted for excess from the tail; {@code null} for row 0.
     */
    private final Money forecastPaymentAmount;

    /** {@code 1 / (1 + EIR)^paymentsLeft} */
    private final BigDecimal discountFactor;

    /** {@code forecastPayment × discountFactor} */
    private final Money npvValue;

    /** {@code balance[i] = balance[i-1] × (1+EIR) - expectedPayment} */
    private final Money balance;

    /** {@code balance[i] + expectedPayment - balance[i-1]} (equivalent to {@code prevBalance × EIR}) */
    private final Money expectedAmortizationAmount;

    /** First paid: sum of all NPV values; subsequent: {@code netAmort[i-1] - actualAmort[i-1]}. */
    private final Money netAmortizationAmount;

    private final Money actualPaymentAmount;

    /** Cursor-based consumption of expected amortization proportional to payment ratio. */
    private final Money actualAmortizationAmount;

    /** {@code actualAmortization - expectedAmortization} */
    private final Money incomeModification;

    /** {@code deferredBalance[i-1] - actualAmortization[i]} */
    private final Money deferredBalance;
}
