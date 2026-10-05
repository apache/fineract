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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanAccrualAdjustmentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanUndoDisbursalTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkingCapitalLoanUndoDisbursalTest {

    private static final Long LOAN_ID = 21L;

    @Mock
    private WorkingCapitalLoanRepository loanRepository;
    @Mock
    private WorkingCapitalLoanDataValidator validator;
    @Mock
    private WorkingCapitalLoanLifecycleStateMachine stateMachine;
    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;
    @Mock
    private WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private WorkingCapitalLoanAccountingProcessor accountingProcessor;
    @Mock
    private WorkingCapitalLoanDiscountFeeAmortizationService discountFeeAmortizationService;
    @Mock
    private WorkingCapitalLoanAdjustTransactionEventPublisher adjustTransactionEventPublisher;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProductRelatedDetails loanProductRelatedDetails;
    @Mock
    private JsonCommand command;

    @InjectMocks
    private WorkingCapitalLoanWritePlatformServiceImpl writePlatformService;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        when(loan.getLoanProductRelatedDetails()).thenReturn(loanProductRelatedDetails);
        when(command.json()).thenReturn("{}");
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void undoDisbursalReversesEveryLiveTransactionAndItsJournalEntries() {
        final WorkingCapitalLoanTransaction disbursement = transaction(LoanTransactionType.DISBURSEMENT, false);
        final WorkingCapitalLoanTransaction discountFee = transaction(LoanTransactionType.DISCOUNT_FEE, false);
        final WorkingCapitalLoanTransaction discountFeeAdjustment = transaction(LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT, false);
        final WorkingCapitalLoanTransaction amortization = transaction(LoanTransactionType.DISCOUNT_FEE_AMORTIZATION, false);
        final WorkingCapitalLoanTransaction amortizationAdjustment = transaction(LoanTransactionType.DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT,
                false);
        final WorkingCapitalLoanTransaction accrual = transaction(LoanTransactionType.ACCRUAL, false);
        final List<WorkingCapitalLoanTransaction> live = List.of(disbursement, discountFee, discountFeeAdjustment, amortization,
                amortizationAdjustment, accrual);
        when(transactionRepository.findByWcLoan_IdOrderByTransactionDateAscIdAsc(LOAN_ID)).thenReturn(live);

        writePlatformService.undoDisbursal(LOAN_ID, command);

        for (final WorkingCapitalLoanTransaction txn : live) {
            verify(txn).setReversed(true);
            verify(accountingProcessor).postReversalJournalEntries(loan, txn);
        }
        verify(transactionRepository).saveAll(live);

        // Discount fee, its adjustment and the amortizations are announced as reversals; the disbursement and the
        // accrual have their own dedicated events.
        for (final WorkingCapitalLoanTransaction txn : List.of(discountFee, discountFeeAdjustment, amortization, amortizationAdjustment)) {
            verify(adjustTransactionEventPublisher).publishReversal(LOAN_ID, txn);
        }
        verify(adjustTransactionEventPublisher, never()).publishReversal(LOAN_ID, disbursement);
        verify(adjustTransactionEventPublisher, never()).publishReversal(LOAN_ID, accrual);

        final List<BusinessEvent<?>> events = publishedEvents();
        assertThat(events).filteredOn(WorkingCapitalLoanAccrualAdjustmentTransactionBusinessEvent.class::isInstance).hasSize(1);
        assertThat(events).filteredOn(WorkingCapitalLoanUndoDisbursalTransactionBusinessEvent.class::isInstance).hasSize(1);
    }

    @Test
    public void undoDisbursalLeavesPreviouslyReversedTransactionsUntouched() {
        final WorkingCapitalLoanTransaction disbursement = transaction(LoanTransactionType.DISBURSEMENT, false);
        final WorkingCapitalLoanTransaction undoneAmortization = transaction(LoanTransactionType.DISCOUNT_FEE_AMORTIZATION, true);
        final WorkingCapitalLoanTransaction undoneRepayment = transaction(LoanTransactionType.REPAYMENT, true);
        when(transactionRepository.findByWcLoan_IdOrderByTransactionDateAscIdAsc(LOAN_ID))
                .thenReturn(List.of(disbursement, undoneAmortization, undoneRepayment));

        writePlatformService.undoDisbursal(LOAN_ID, command);

        for (final WorkingCapitalLoanTransaction txn : List.of(undoneAmortization, undoneRepayment)) {
            // Its own reversal date and external id stay, and its journal entries were already cancelled by that undo.
            verify(txn, never()).setReversed(anyBoolean());
            verify(txn, never()).setReversedOnDate(any());
            verify(txn, never()).setReversalExternalId(any());
            verify(accountingProcessor, never()).postReversalJournalEntries(loan, txn);
            verify(adjustTransactionEventPublisher, never()).publishReversal(LOAN_ID, txn);
        }
        verify(accountingProcessor).postReversalJournalEntries(loan, disbursement);
        verify(transactionRepository).saveAll(List.of(disbursement));
    }

    private static WorkingCapitalLoanTransaction transaction(final LoanTransactionType type, final boolean reversed) {
        final WorkingCapitalLoanTransaction txn = mock(WorkingCapitalLoanTransaction.class);
        when(txn.getTypeOf()).thenReturn(type);
        when(txn.isReversed()).thenReturn(reversed);
        return txn;
    }

    private List<BusinessEvent<?>> publishedEvents() {
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<BusinessEvent<?>> captor = ArgumentCaptor.forClass(BusinessEvent.class);
        verify(businessEventNotifierService, atLeastOnce()).notifyPostBusinessEvent(captor.capture());
        return captor.getAllValues();
    }
}
