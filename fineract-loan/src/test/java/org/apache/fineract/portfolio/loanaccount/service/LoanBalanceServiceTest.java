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
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.persistence.FlushModeHandler;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
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

    @Test
    void incomeForOverlappingPeriodProratesInterestByElapsedDays() {
        // 3.00 over 30 days, 10 days elapsed: 1.00, no rounding involved.
        Loan loan = loanWithInstallment(installment(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "3.00"));

        Money[] income = service.retrieveIncomeForOverlappingPeriod(loan, LocalDate.of(2026, 1, 11));

        assertThat(income[0].getAmount()).isEqualByComparingTo("1.00");
    }

    @Test
    void incomeForOverlappingPeriodRoundsTheExactProratedInterest() {
        // 1.83 over 30 days, 5 days elapsed: 1.83 * 5 / 30 = 0.305 exactly, which is 0.30 with HALF_EVEN.
        Loan loan = loanWithInstallment(installment(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), "1.83"));

        Money[] income = service.retrieveIncomeForOverlappingPeriod(loan, LocalDate.of(2026, 1, 6));

        assertThat(income[0].getAmount()).isEqualByComparingTo("0.30");
    }

    private static Loan loanWithInstallment(LoanRepaymentScheduleInstallment installment) {
        Loan loan = mock(Loan.class);
        when(loan.getCurrency()).thenReturn(USD);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(List.of(installment));
        return loan;
    }

    private static LoanRepaymentScheduleInstallment installment(LocalDate fromDate, LocalDate dueDate, String interestCharged) {
        Money interest = Money.of(USD, new BigDecimal(interestCharged));
        Money zero = Money.zero(USD);
        LoanRepaymentScheduleInstallment installment = mock(LoanRepaymentScheduleInstallment.class);
        when(installment.getInstallmentNumber()).thenReturn(1);
        when(installment.getFromDate()).thenReturn(fromDate);
        when(installment.getDueDate()).thenReturn(dueDate);
        when(installment.getInterestCharged(USD)).thenReturn(interest);
        when(installment.getInterestWaived(USD)).thenReturn(zero);
        when(installment.getInterestPaid(USD)).thenReturn(zero);
        return installment;
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
