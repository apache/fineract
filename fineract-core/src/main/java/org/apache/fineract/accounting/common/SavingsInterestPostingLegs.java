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
package org.apache.fineract.accounting.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForSavings;
import org.apache.fineract.infrastructure.core.service.MathUtil;

/**
 * The debit and credit legs of an interest posting on a savings account. The accounting processors and the Post
 * Interest For Savings job both book interest postings from these rules, so the two can't drift apart.
 */
public final class SavingsInterestPostingLegs {

    /**
     * One debit and one credit of the same amount. The account types are the savings product's GL account mapping types
     * ({@link CashAccountsForSavings} or {@link AccrualAccountsForSavings}).
     */
    public record Leg(int accountTypeToDebit, int accountTypeToCredit, BigDecimal amount) {
    }

    private SavingsInterestPostingLegs() {}

    public static List<Leg> legsFor(final boolean cashBasedAccounting, final BigDecimal amount, final BigDecimal overdraftAmount) {
        if (!MathUtil.isGreaterThanZero(amount)) {
            return List.of();
        }
        // a cash-based product expenses the interest when it is posted, an accrual product expensed it when it accrued
        final int accountTypeToDebit = cashBasedAccounting ? CashAccountsForSavings.INTEREST_ON_SAVINGS.getValue()
                : AccrualAccountsForSavings.INTEREST_PAYABLE.getValue();
        final int overdraftPortfolioControl = cashBasedAccounting ? CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue()
                : AccrualAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue();
        final int savingsControl = cashBasedAccounting ? CashAccountsForSavings.SAVINGS_CONTROL.getValue()
                : AccrualAccountsForSavings.SAVINGS_CONTROL.getValue();

        final List<Leg> legs = new ArrayList<>();
        BigDecimal remainder = amount;
        // interest posted to an overdrawn account pays down the overdraft first
        if (MathUtil.isGreaterThanZero(overdraftAmount)) {
            legs.add(new Leg(accountTypeToDebit, overdraftPortfolioControl, overdraftAmount));
            remainder = amount.subtract(overdraftAmount);
        }
        if (MathUtil.isGreaterThanZero(remainder)) {
            legs.add(new Leg(accountTypeToDebit, savingsControl, remainder));
        }
        return legs;
    }
}
