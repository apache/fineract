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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import org.apache.fineract.infrastructure.core.persistence.FlushModeHandler;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class LoanBalanceServiceTest {

    private static final MonetaryCurrency USD = new MonetaryCurrency("USD", 2, 0);

    private static MockedStatic<MoneyHelper> moneyHelperMock;

    private final LoanBalanceService service = new LoanBalanceService(mock(CapitalizedIncomeBalanceService.class),
            mock(FlushModeHandler.class));

    @BeforeAll
    static void setUpStatics() {
        moneyHelperMock = mockStatic(MoneyHelper.class);
        moneyHelperMock.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelperMock.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    static void tearDownStatics() {
        moneyHelperMock.close();
    }

    @Test
    void totalRecoveredIncludesRecoveryPaymentNotYetSaved() {
        // The summary is refreshed before a new recovery payment is persisted, so the unsaved one must count too.
        LoanTransaction savedRecovery = transaction(true, "100");
        LoanTransaction newRecovery = transaction(true, "50");
        Loan loan = loanWith(savedRecovery, newRecovery);

        Money totalRecovered = service.calculateTotalRecoveredPayments(loan);

        assertThat(totalRecovered.getAmount()).isEqualByComparingTo("150");
    }

    @Test
    void totalRecoveredIgnoresOtherAndReversedTransactions() {
        // isRecoveryRepayment() is false for reversed recovery payments and for every other transaction type.
        LoanTransaction recovery = transaction(true, "25");
        LoanTransaction reversedRecoveryOrRepayment = transaction(false, "40");
        Loan loan = loanWith(recovery, reversedRecoveryOrRepayment);

        Money totalRecovered = service.calculateTotalRecoveredPayments(loan);

        assertThat(totalRecovered.getAmount()).isEqualByComparingTo("25");
    }

    @Test
    void totalRecoveredIsZeroWithoutRecoveryPayments() {
        Loan loan = loanWith();

        Money totalRecovered = service.calculateTotalRecoveredPayments(loan);

        assertThat(totalRecovered.isZero()).isTrue();
    }

    private static Loan loanWith(LoanTransaction... transactions) {
        Loan loan = mock(Loan.class);
        when(loan.getCurrency()).thenReturn(USD);
        when(loan.getLoanTransactions()).thenReturn(List.of(transactions));
        return loan;
    }

    private static LoanTransaction transaction(boolean recoveryRepayment, String amount) {
        Money money = Money.of(USD, new BigDecimal(amount));
        LoanTransaction transaction = mock(LoanTransaction.class);
        when(transaction.isRecoveryRepayment()).thenReturn(recoveryRepayment);
        when(transaction.getAmount(USD)).thenReturn(money);
        return transaction;
    }
}
