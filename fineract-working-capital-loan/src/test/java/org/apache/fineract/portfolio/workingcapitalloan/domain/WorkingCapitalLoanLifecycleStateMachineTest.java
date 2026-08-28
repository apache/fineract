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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WorkingCapitalLoanLifecycleStateMachineTest {

    @Mock
    private PlatformSecurityContext platformSecurityContext;

    @InjectMocks
    private WorkingCapitalLoanLifecycleStateMachine stateMachine;

    public WorkingCapitalLoanLifecycleStateMachineTest() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @MethodSource("validTransitions")
    void testValidTransitions(final WorkingCapitalLoanEvent event, final LoanStatus from, final LoanStatus expected) {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        when(loan.getLoanStatus()).thenReturn(from);

        stateMachine.transition(event, loan, LocalDate.of(2026, 8, 30));

        verify(loan).setLoanStatus(expected);
    }

    private static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(WorkingCapitalLoanEvent.LOAN_APPROVED, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, LoanStatus.APPROVED),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_APPROVAL_UNDO, LoanStatus.APPROVED, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_REJECTED, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, LoanStatus.REJECTED),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_DISBURSED, LoanStatus.APPROVED, LoanStatus.ACTIVE),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_DISBURSAL_UNDO, LoanStatus.ACTIVE, LoanStatus.APPROVED),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_REPAID_IN_FULL, LoanStatus.ACTIVE, LoanStatus.CLOSED_OBLIGATIONS_MET),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_REPAID_IN_FULL, LoanStatus.OVERPAID, LoanStatus.CLOSED_OBLIGATIONS_MET),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_OVERPAID, LoanStatus.ACTIVE, LoanStatus.OVERPAID),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_OVERPAID, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.OVERPAID),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_OVERPAID, LoanStatus.OVERPAID, LoanStatus.OVERPAID),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_REOPENED, LoanStatus.OVERPAID, LoanStatus.ACTIVE),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_REOPENED, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.ACTIVE),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_CREDIT_BALANCE_REFUND_IN_FULL, LoanStatus.OVERPAID,
                        LoanStatus.CLOSED_OBLIGATIONS_MET),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_WRITTEN_OFF, LoanStatus.ACTIVE, LoanStatus.CLOSED_WRITTEN_OFF),
                Arguments.of(WorkingCapitalLoanEvent.LOAN_WRITTEN_OFF_UNDO, LoanStatus.CLOSED_WRITTEN_OFF, LoanStatus.ACTIVE));
    }

    @Test
    void testOverpaidTransitionFromActive() {
        // Arrange
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);

        LocalDate transitionDate = LocalDate.of(2026, 8, 30);
        WorkingCapitalLoanEvent event = WorkingCapitalLoanEvent.LOAN_OVERPAID;

        // Act
        stateMachine.transition(event, loan, transitionDate);

        // Assert
        verify(loan).setLoanStatus(LoanStatus.OVERPAID);
        verify(loan).setOverpaidOnDate(transitionDate);
    }

    @Test
    void testOverpaidTransitionFromClosedObligationsMet() {
        // Arrange
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_OBLIGATIONS_MET);

        LocalDate transitionDate = LocalDate.of(2026, 8, 30);
        WorkingCapitalLoanEvent event = WorkingCapitalLoanEvent.LOAN_OVERPAID;

        // Act
        stateMachine.transition(event, loan, transitionDate);

        // Assert
        verify(loan).setLoanStatus(LoanStatus.OVERPAID);
        verify(loan).setOverpaidOnDate(transitionDate);
    }

    @Test
    void testInvalidOverpaidTransitionThrowsException() {
        // Arrange
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.APPROVED);

        LocalDate transitionDate = LocalDate.of(2026, 8, 30);
        WorkingCapitalLoanEvent event = WorkingCapitalLoanEvent.LOAN_OVERPAID;

        // Act & Assert
        assertThrows(PlatformApiDataValidationException.class, () -> stateMachine.transition(event, loan, transitionDate));
    }

    @Test
    void testRepaymentSetsClosureAuditFieldsAndClearsOverpaidDate() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        AppUser user = mock(AppUser.class);
        LocalDate transitionDate = LocalDate.of(2026, 8, 30);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        when(platformSecurityContext.getAuthenticatedUserIfPresent()).thenReturn(user);

        stateMachine.transition(WorkingCapitalLoanEvent.LOAN_REPAID_IN_FULL, loan, transitionDate);

        verify(loan).setLoanStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);
        verify(loan).setClosedOnDate(transitionDate);
        verify(loan).setClosedBy(user);
        verify(loan).setOverpaidOnDate(null);
    }

    @Test
    void testTransitionToActiveClearsClosureAndOverpaidFields() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.OVERPAID);

        stateMachine.transition(WorkingCapitalLoanEvent.LOAN_REOPENED, loan, LocalDate.of(2026, 8, 30));

        verify(loan).setLoanStatus(LoanStatus.ACTIVE);
        verify(loan).setClosedOnDate(null);
        verify(loan).setClosedBy(null);
        verify(loan).setOverpaidOnDate(null);
    }

    @Test
    void testOverpaidTransitionPreservesExistingOverpaidDate() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        LocalDate originalDate = LocalDate.of(2026, 8, 28);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.OVERPAID);
        when(loan.getOverpaidOnDate()).thenReturn(originalDate);

        stateMachine.transition(WorkingCapitalLoanEvent.LOAN_OVERPAID, loan, LocalDate.of(2026, 8, 30));

        verify(loan).setLoanStatus(LoanStatus.OVERPAID);
        verify(loan, never()).setOverpaidOnDate(LocalDate.of(2026, 8, 30));
    }

    @Test
    void testDetermineAndTransitionDoesNothingWhenBalanceIsNull() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        when(loan.getBalance()).thenReturn(null);

        stateMachine.determineAndTransition(loan, LocalDate.of(2026, 8, 30));

        verify(loan, never()).setLoanStatus(org.mockito.ArgumentMatchers.any());
        verify(loan, never()).setMaturedOnDate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testDetermineAndTransitionOverpaymentSetsOverpaidAndMaturedDate() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        WorkingCapitalLoanBalance balance = mock(WorkingCapitalLoanBalance.class);
        LocalDate transactionDate = LocalDate.of(2026, 8, 30);
        when(loan.getBalance()).thenReturn(balance);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.ONE);
        when(balance.getTotalOutstanding()).thenReturn(BigDecimal.ZERO);
        when(loan.getMaturedOnDate()).thenReturn(null);

        stateMachine.determineAndTransition(loan, transactionDate);

        verify(loan).setLoanStatus(LoanStatus.OVERPAID);
        verify(loan).setMaturedOnDate(transactionDate);
    }

    @Test
    void testDetermineAndTransitionPreservesExistingMaturedDate() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        WorkingCapitalLoanBalance balance = mock(WorkingCapitalLoanBalance.class);
        LocalDate maturedDate = LocalDate.of(2026, 8, 20);
        when(loan.getBalance()).thenReturn(balance);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.OVERPAID);
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.TEN);
        when(balance.getTotalOutstanding()).thenReturn(BigDecimal.ZERO);
        when(loan.getMaturedOnDate()).thenReturn(maturedDate);

        stateMachine.determineAndTransition(loan, LocalDate.of(2026, 8, 30));

        verify(loan, never()).setMaturedOnDate(LocalDate.of(2026, 8, 30));
    }

    @Test
    void testDetermineAndTransitionRepaymentSetsMaturedDate() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        WorkingCapitalLoanBalance balance = mock(WorkingCapitalLoanBalance.class);
        LocalDate transactionDate = LocalDate.of(2026, 8, 30);
        when(loan.getBalance()).thenReturn(balance);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.ZERO);
        when(balance.getTotalOutstanding()).thenReturn(BigDecimal.ZERO);
        when(loan.getMaturedOnDate()).thenReturn(null);

        stateMachine.determineAndTransition(loan, transactionDate);

        verify(loan).setLoanStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);
        verify(loan).setMaturedOnDate(transactionDate);
    }

    @Test
    void testDetermineAndTransitionTreatsNullBalanceAmountsAsZero() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        WorkingCapitalLoanBalance balance = mock(WorkingCapitalLoanBalance.class);
        LocalDate transactionDate = LocalDate.of(2026, 8, 30);
        when(loan.getBalance()).thenReturn(balance);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        when(balance.getOverpaymentAmount()).thenReturn(null);
        when(balance.getTotalOutstanding()).thenReturn(null);
        when(loan.getMaturedOnDate()).thenReturn(null);

        stateMachine.determineAndTransition(loan, transactionDate);

        verify(loan).setLoanStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);
        verify(loan).setMaturedOnDate(transactionDate);
    }

    @Test
    void testDetermineAndTransitionDoesNothingWhenOutstandingLoanIsNotMatured() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        WorkingCapitalLoanBalance balance = mock(WorkingCapitalLoanBalance.class);
        when(loan.getBalance()).thenReturn(balance);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.ZERO);
        when(balance.getTotalOutstanding()).thenReturn(BigDecimal.ONE);
        when(loan.getMaturedOnDate()).thenReturn(null);

        stateMachine.determineAndTransition(loan, LocalDate.of(2026, 8, 30));

        verify(loan, never()).setLoanStatus(org.mockito.ArgumentMatchers.any());
        verify(loan, never()).setMaturedOnDate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testDetermineAndTransitionDoesNotRepeatAlreadyAppliedRepaymentTransition() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        WorkingCapitalLoanBalance balance = mock(WorkingCapitalLoanBalance.class);
        when(loan.getBalance()).thenReturn(balance);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_OBLIGATIONS_MET);
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.ZERO);
        when(balance.getTotalOutstanding()).thenReturn(BigDecimal.ZERO);
        when(loan.getMaturedOnDate()).thenReturn(LocalDate.of(2026, 8, 20));

        stateMachine.determineAndTransition(loan, LocalDate.of(2026, 8, 30));

        verify(loan, never()).setLoanStatus(org.mockito.ArgumentMatchers.any());
        verify(loan, never()).setMaturedOnDate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testDetermineAndTransitionReopensMaturedLoanWhenOutstandingReturns() {
        WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        WorkingCapitalLoanBalance balance = mock(WorkingCapitalLoanBalance.class);
        LocalDate transactionDate = LocalDate.of(2026, 8, 30);
        when(loan.getBalance()).thenReturn(balance);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_OBLIGATIONS_MET);
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.ZERO);
        when(balance.getTotalOutstanding()).thenReturn(BigDecimal.ONE);
        when(loan.getMaturedOnDate()).thenReturn(LocalDate.of(2026, 8, 20));

        stateMachine.determineAndTransition(loan, transactionDate);

        verify(loan).setLoanStatus(LoanStatus.ACTIVE);
        verify(loan).setMaturedOnDate(null);
    }

}
