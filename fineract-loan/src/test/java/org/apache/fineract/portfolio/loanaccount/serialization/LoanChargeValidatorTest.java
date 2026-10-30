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
package org.apache.fineract.portfolio.loanaccount.serialization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanChargeAdjustmentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanChargeValidatorTest {

    private final LoanChargeValidator underTest = new LoanChargeValidator(mock(LoanTransactionRepository.class));

    private LoanCharge loanCharge;
    private LoanCharge otherLoanCharge;
    private Loan loan;

    @BeforeEach
    void setUp() {
        loan = mock(Loan.class);
        loanCharge = mock(LoanCharge.class);
        otherLoanCharge = mock(LoanCharge.class);
        lenient().when(loanCharge.getLoan()).thenReturn(loan);
        lenient().when(loanCharge.amount()).thenReturn(BigDecimal.valueOf(100));
    }

    @Test
    void availableAmountSubtractsNonReversedChargeAdjustmentsOfTheSameCharge() {
        List<LoanTransaction> transactions = List.of(chargeAdjustment(1L, 30, loanCharge, true),
                chargeAdjustment(2L, 20, loanCharge, false), chargeAdjustment(3L, 15, otherLoanCharge, true), repayment(4L, 50));
        when(loan.getLoanTransactions()).thenReturn(transactions);

        assertEquals(0, BigDecimal.valueOf(70).compareTo(underTest.calculateAvailableAmountForChargeAdjustment(loanCharge, null)));
    }

    @Test
    void availableAmountIgnoresTheExcludedTransaction() {
        List<LoanTransaction> transactions = List.of(chargeAdjustment(1L, 30, loanCharge, true),
                chargeAdjustment(2L, 40, loanCharge, true));
        when(loan.getLoanTransactions()).thenReturn(transactions);

        assertEquals(0, BigDecimal.valueOf(70).compareTo(underTest.calculateAvailableAmountForChargeAdjustment(loanCharge, 2L)));
    }

    @Test
    void validateChargeAdjustmentAmountAllowsReusingTheExcludedTransactionAmount() {
        List<LoanTransaction> transactions = List.of(chargeAdjustment(1L, 30, loanCharge, true),
                chargeAdjustment(2L, 70, loanCharge, true));
        when(loan.getLoanTransactions()).thenReturn(transactions);

        assertDoesNotThrow(() -> underTest.validateChargeAdjustmentAmount(loanCharge, BigDecimal.valueOf(70), 2L));
        LoanChargeAdjustmentException exception = assertThrows(LoanChargeAdjustmentException.class,
                () -> underTest.validateChargeAdjustmentAmount(loanCharge, BigDecimal.valueOf(71), 2L));
        assertEquals("error.msg.loan.charge.adjustment.invalid.amount", exception.getGlobalisationMessageCode());
    }

    @Test
    void validateChargeAdjustmentAmountRejectsAmountAboveTheChargeAmount() {
        LoanChargeAdjustmentException exception = assertThrows(LoanChargeAdjustmentException.class,
                () -> underTest.validateChargeAdjustmentAmount(loanCharge, BigDecimal.valueOf(101), null));
        assertEquals("error.msg.loan.charge.adjustment.invalid.amount", exception.getGlobalisationMessageCode());
    }

    @Test
    void availableAmountRejectsChargeAdjustmentsWithoutAChargeLink() {
        List<LoanTransaction> transactions = List.of(chargeAdjustment(1L, 30, loanCharge, true), chargeAdjustment(2L, 40, null, true));
        when(loan.getLoanTransactions()).thenReturn(transactions);

        LoanChargeAdjustmentException exception = assertThrows(LoanChargeAdjustmentException.class,
                () -> underTest.calculateAvailableAmountForChargeAdjustment(loanCharge, null));
        assertEquals("error.msg.loan.charge.adjustment.charge.not.found", exception.getGlobalisationMessageCode());
    }

    @Test
    void availableAmountSkipsTheExcludedTransactionEvenWithoutAChargeLink() {
        List<LoanTransaction> transactions = List.of(chargeAdjustment(1L, 30, loanCharge, true), chargeAdjustment(2L, 40, null, true));
        when(loan.getLoanTransactions()).thenReturn(transactions);

        assertEquals(0, BigDecimal.valueOf(70).compareTo(underTest.calculateAvailableAmountForChargeAdjustment(loanCharge, 2L)));
    }

    @Test
    void validateChargeAdjustmentDateUsesTheEarlierOfSubmittedOnAndDueDate() {
        when(loanCharge.getSubmittedOnDate()).thenReturn(LocalDate.of(2024, 2, 15));
        when(loanCharge.getDueLocalDate()).thenReturn(LocalDate.of(2024, 1, 15));

        assertDoesNotThrow(() -> underTest.validateChargeAdjustmentDate(loanCharge, LocalDate.of(2024, 1, 15)));
        LoanChargeAdjustmentException exception = assertThrows(LoanChargeAdjustmentException.class,
                () -> underTest.validateChargeAdjustmentDate(loanCharge, LocalDate.of(2024, 1, 14)));
        assertEquals("error.msg.loan.charge.adjustment.date.before.charge.date", exception.getGlobalisationMessageCode());
    }

    @Test
    void validateChargeAdjustmentDateFallsBackToSubmittedOnDateWithoutDueDate() {
        when(loanCharge.getSubmittedOnDate()).thenReturn(LocalDate.of(2024, 1, 10));
        when(loanCharge.getDueLocalDate()).thenReturn(null);

        assertDoesNotThrow(() -> underTest.validateChargeAdjustmentDate(loanCharge, LocalDate.of(2024, 1, 10)));
        assertThrows(LoanChargeAdjustmentException.class,
                () -> underTest.validateChargeAdjustmentDate(loanCharge, LocalDate.of(2024, 1, 9)));
    }

    private LoanTransaction chargeAdjustment(Long id, double amount, LoanCharge charge, boolean notReversed) {
        LoanTransaction transaction = mock(LoanTransaction.class);
        lenient().when(transaction.getId()).thenReturn(id);
        lenient().when(transaction.isNotReversed()).thenReturn(notReversed);
        lenient().when(transaction.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_ADJUSTMENT);
        lenient().when(transaction.getAmount()).thenReturn(BigDecimal.valueOf(amount));
        LoanTransactionRelation relation = mock(LoanTransactionRelation.class);
        lenient().when(relation.getRelationType()).thenReturn(LoanTransactionRelationTypeEnum.CHARGE_ADJUSTMENT);
        lenient().when(relation.getToCharge()).thenReturn(charge);
        lenient().when(transaction.getLoanTransactionRelations()).thenReturn(Set.of(relation));
        return transaction;
    }

    private LoanTransaction repayment(Long id, double amount) {
        LoanTransaction transaction = mock(LoanTransaction.class);
        lenient().when(transaction.getId()).thenReturn(id);
        lenient().when(transaction.isNotReversed()).thenReturn(true);
        lenient().when(transaction.getTypeOf()).thenReturn(LoanTransactionType.REPAYMENT);
        lenient().when(transaction.getAmount()).thenReturn(BigDecimal.valueOf(amount));
        return transaction;
    }
}
