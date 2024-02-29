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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultLoanLifecycleStateMachineTest {

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    private DefaultLoanLifecycleStateMachine underTest;

    private MockedStatic<MoneyHelper> moneyHelperStatic;

    @BeforeEach
    public void setUp() {

        moneyHelperStatic = Mockito.mockStatic(MoneyHelper.class);
        moneyHelperStatic.when(() -> MoneyHelper.getRoundingMode()).thenReturn(RoundingMode.UP);
        underTest = new DefaultLoanLifecycleStateMachine(businessEventNotifierService);
    }

    @AfterEach
    public void deregister() {
        ThreadLocalContextUtil.reset();
        moneyHelperStatic.close();
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanCreation() {
        // given
        Loan loan = createLoanWithStatus(null);
        // when
        underTest.transition(LoanEvent.LOAN_CREATED, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        verifyNoInteractions(businessEventNotifierService);
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanRejection() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        // when
        underTest.transition(LoanEvent.LOAN_REJECTED, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.REJECTED);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanApproval() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        // when
        underTest.transition(LoanEvent.LOAN_APPROVED, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.APPROVED);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanWithdraw() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        // when
        underTest.transition(LoanEvent.LOAN_WITHDRAWN, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.WITHDRAWN_BY_CLIENT);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanDisbursementWhenLoanIsApproved() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.APPROVED);
        // when
        underTest.transition(LoanEvent.LOAN_DISBURSED, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanDisbursementWhenLoanIsClosedObligationsMet() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);
        // when
        underTest.transition(LoanEvent.LOAN_DISBURSED, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanDisbursementWhenLoanIsOverpaid() {
        // given
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, null);
        Money zero = Money.of(currency, BigDecimal.ZERO);
        Money one = Money.of(currency, BigDecimal.ONE);
        Loan loan = Mockito.mock(Loan.class);
        LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
        Mockito.when(loan.getCurrency()).thenReturn(currency);
        Mockito.when(loan.getPlainStatus()).thenReturn(LoanStatus.OVERPAID.getValue());
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.OVERPAID);
        Mockito.when(loan.getTotalOverpaidAsMoney()).thenReturn(zero);
        Mockito.when(loan.getLoanSummary()).thenReturn(loanSummary);
        Mockito.when(loanSummary.getTotalOutstanding(eq(currency))).thenReturn(one);
        // when
        underTest.transition(LoanEvent.LOAN_DISBURSED, loan);
        // then
        verify(loan, Mockito.times(1)).setLoanStatus(LoanStatus.ACTIVE.getValue());
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanDisbursementWhenLoanIsOverpaidAndGotClosed() {
        // given
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, null);
        Money zero = Money.of(currency, BigDecimal.ZERO);
        Loan loan = Mockito.mock(Loan.class);
        LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
        Mockito.when(loan.getCurrency()).thenReturn(currency);
        Mockito.when(loan.getPlainStatus()).thenReturn(LoanStatus.OVERPAID.getValue());
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.OVERPAID);
        Mockito.when(loan.getTotalOverpaidAsMoney()).thenReturn(zero);
        Mockito.when(loan.getLoanSummary()).thenReturn(loanSummary);
        Mockito.when(loanSummary.getTotalOutstanding(currency)).thenReturn(zero);
        // when
        underTest.transition(LoanEvent.LOAN_DISBURSED, loan);
        // then
        verify(loan, Mockito.times(1)).setLoanStatus(LoanStatus.CLOSED_OBLIGATIONS_MET.getValue());
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanDisbursementWhenLoanIsOverpaidAndRemainsOverpaid() {
        // given
        Money overpayment = Money.of(new MonetaryCurrency("USD", 2, null), BigDecimal.TEN);
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getPlainStatus()).thenReturn(LoanStatus.OVERPAID.getValue());
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.OVERPAID);
        Mockito.when(loan.getTotalOverpaidAsMoney()).thenReturn(overpayment);
        // when
        underTest.transition(LoanEvent.LOAN_DISBURSED, loan);
        // then
        verify(loan, Mockito.never()).setLoanStatus(LoanStatus.ACTIVE.getValue());
        verify(businessEventNotifierService, Mockito.never()).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanApprovalUndo() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.APPROVED);
        // when
        underTest.transition(LoanEvent.LOAN_APPROVAL_UNDO, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanDisbursementUndo() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.ACTIVE);
        // when
        underTest.transition(LoanEvent.LOAN_DISBURSAL_UNDO, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.APPROVED);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanChargePaymentWhenClosedObligationsMet() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);
        // when
        underTest.transition(LoanEvent.LOAN_CHARGE_PAYMENT, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanChargePaymentWhenOverpaid() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.OVERPAID);
        // when
        underTest.transition(LoanEvent.LOAN_CHARGE_PAYMENT, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanRepaidInFullWhenActive() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.ACTIVE);
        // when
        underTest.transition(LoanEvent.REPAID_IN_FULL, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.CLOSED_OBLIGATIONS_MET);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanRepaidInFullWhenOverpaid() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.OVERPAID);
        // when
        underTest.transition(LoanEvent.REPAID_IN_FULL, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.CLOSED_OBLIGATIONS_MET);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanWriteOffOutstanding() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.ACTIVE);
        // when
        underTest.transition(LoanEvent.WRITE_OFF_OUTSTANDING, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.CLOSED_WRITTEN_OFF);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanRescheduled() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.ACTIVE);
        // when
        underTest.transition(LoanEvent.LOAN_RESCHEDULE, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanOverpaymentWhenActive() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.ACTIVE);
        // when
        underTest.transition(LoanEvent.LOAN_OVERPAYMENT, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.OVERPAID);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanOverpaymentWhenClosedObligationsMet() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);
        // when
        underTest.transition(LoanEvent.LOAN_OVERPAYMENT, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.OVERPAID);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanAdjustTransactionWhenClosedObligationsMet() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);
        // when
        underTest.transition(LoanEvent.LOAN_ADJUST_TRANSACTION, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanAdjustTransactionWhenClosedWrittenOff() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.CLOSED_WRITTEN_OFF);
        // when
        underTest.transition(LoanEvent.LOAN_ADJUST_TRANSACTION, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanAdjustTransactionWhenClosedRescheduleOutstandingAmount() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT);
        // when
        underTest.transition(LoanEvent.LOAN_ADJUST_TRANSACTION, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanInitiateTransfer() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.ACTIVE);
        // when
        underTest.transition(LoanEvent.LOAN_INITIATE_TRANSFER, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.TRANSFER_IN_PROGRESS);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanRejectTransfer() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.TRANSFER_IN_PROGRESS);
        // when
        underTest.transition(LoanEvent.LOAN_REJECT_TRANSFER, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.TRANSFER_ON_HOLD);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanWithdrawTransfer() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.TRANSFER_IN_PROGRESS);
        // when
        underTest.transition(LoanEvent.LOAN_WITHDRAW_TRANSFER, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanWriteOffOutstandingUndo() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.CLOSED_WRITTEN_OFF);
        // when
        underTest.transition(LoanEvent.WRITE_OFF_OUTSTANDING_UNDO, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanCreditBalanceRefund() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.OVERPAID);
        // when
        underTest.transition(LoanEvent.LOAN_CREDIT_BALANCE_REFUND, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.CLOSED_OBLIGATIONS_MET);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanChargeAdded() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);
        // when
        underTest.transition(LoanEvent.LOAN_CHARGE_ADDED, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanChargebackWhenClosedObligationsMet() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);
        // when
        underTest.transition(LoanEvent.LOAN_CHARGEBACK, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    @Test
    public void testTransitionShouldWorkProperlyForLoanChargebackWhenOverpaid() {
        // given
        Loan loan = createLoanWithStatus(LoanStatus.OVERPAID);
        // when
        underTest.transition(LoanEvent.LOAN_CHARGEBACK, loan);
        // then
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanStatusChangedBusinessEvent.class));
    }

    private Loan createLoanWithStatus(LoanStatus status) {
        Loan result = new Loan();
        if (status != null) {
            result.setLoanStatus(status.getValue());
        }
        return result;
    }
}
