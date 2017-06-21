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
package org.apache.fineract.portfolio.savings.data;

import java.math.BigDecimal;

import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing Savings Account summary information.
 */
@SuppressWarnings("unused")
public class SavingsAccountSummaryData {

    private final CurrencyData currency;
    private final BigDecimal totalDeposits;
    private final BigDecimal totalWithdrawals;
    private final BigDecimal totalWithdrawalFees;
    private final BigDecimal totalAnnualFees;
    private final BigDecimal totalInterestEarned;
    private final BigDecimal totalInterestPosted;
    private final BigDecimal accountBalance;
    private final BigDecimal totalFeeCharge;
    private final BigDecimal totalPenaltyCharge;
    private final BigDecimal totalOverdraftInterestDerived;
    private final BigDecimal totalWithholdTax;
    private final BigDecimal interestNotPosted;
    private final LocalDate lastInterestCalculationDate;
    private final BigDecimal availableBalance;

    public SavingsAccountSummaryData(final CurrencyData currency, final BigDecimal totalDeposits, final BigDecimal totalWithdrawals,
            final BigDecimal totalWithdrawalFees, final BigDecimal totalAnnualFees, final BigDecimal totalInterestEarned,
            final BigDecimal totalInterestPosted, final BigDecimal accountBalance, final BigDecimal totalFeeCharge,
            final BigDecimal totalPenaltyCharge, final BigDecimal totalOverdraftInterestDerived,final BigDecimal totalWithholdTax,
            final BigDecimal interestNotPosted, final LocalDate lastInterestCalculationDate, final BigDecimal availableBalance) {
        this.currency = currency;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.totalWithdrawalFees = totalWithdrawalFees;
        this.totalAnnualFees = totalAnnualFees;
        this.totalInterestEarned = totalInterestEarned;
        this.totalInterestPosted = totalInterestPosted;
        this.accountBalance = accountBalance;
        this.totalFeeCharge = totalFeeCharge;
        this.totalPenaltyCharge = totalPenaltyCharge;
        this.totalOverdraftInterestDerived = totalOverdraftInterestDerived;
        this.totalWithholdTax = totalWithholdTax;
        this.interestNotPosted = interestNotPosted;
        this.lastInterestCalculationDate = lastInterestCalculationDate;
        this.availableBalance = availableBalance;
    }
}