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

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForSavings;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForSavings;
import org.apache.fineract.accounting.common.SavingsInterestPostingLegs.Leg;
import org.junit.jupiter.api.Test;

class SavingsInterestPostingLegsTest {

    private static final int INTEREST_ON_SAVINGS = CashAccountsForSavings.INTEREST_ON_SAVINGS.getValue();
    private static final int INTEREST_PAYABLE = AccrualAccountsForSavings.INTEREST_PAYABLE.getValue();
    private static final int SAVINGS_CONTROL = CashAccountsForSavings.SAVINGS_CONTROL.getValue();
    private static final int OVERDRAFT_PORTFOLIO_CONTROL = CashAccountsForSavings.OVERDRAFT_PORTFOLIO_CONTROL.getValue();

    @Test
    void interestPostedToAnAccountInCreditGoesToSavingsControl() {
        assertThat(SavingsInterestPostingLegs.legsFor(true, new BigDecimal("50"), null))
                .containsExactly(new Leg(INTEREST_ON_SAVINGS, SAVINGS_CONTROL, new BigDecimal("50")));
        assertThat(SavingsInterestPostingLegs.legsFor(false, new BigDecimal("50"), BigDecimal.ZERO))
                .containsExactly(new Leg(INTEREST_PAYABLE, SAVINGS_CONTROL, new BigDecimal("50")));
    }

    @Test
    void interestPostedToAnOverdrawnAccountPaysDownTheOverdraftFirst() {
        assertThat(SavingsInterestPostingLegs.legsFor(true, new BigDecimal("19.18"), BigDecimal.TEN)).containsExactly(
                new Leg(INTEREST_ON_SAVINGS, OVERDRAFT_PORTFOLIO_CONTROL, BigDecimal.TEN),
                new Leg(INTEREST_ON_SAVINGS, SAVINGS_CONTROL, new BigDecimal("9.18")));
        assertThat(SavingsInterestPostingLegs.legsFor(false, new BigDecimal("19.18"), BigDecimal.TEN)).containsExactly(
                new Leg(INTEREST_PAYABLE, OVERDRAFT_PORTFOLIO_CONTROL, BigDecimal.TEN),
                new Leg(INTEREST_PAYABLE, SAVINGS_CONTROL, new BigDecimal("9.18")));
    }

    @Test
    void interestThatLeavesTheAccountOverdrawnGoesToOverdraftPortfolioControlOnly() {
        assertThat(SavingsInterestPostingLegs.legsFor(true, BigDecimal.TEN, BigDecimal.TEN))
                .containsExactly(new Leg(INTEREST_ON_SAVINGS, OVERDRAFT_PORTFOLIO_CONTROL, BigDecimal.TEN));
    }

    @Test
    void nothingIsBookedForNoInterest() {
        assertThat(SavingsInterestPostingLegs.legsFor(true, BigDecimal.ZERO, null)).isEmpty();
        assertThat(SavingsInterestPostingLegs.legsFor(false, null, null)).isEmpty();
    }
}
