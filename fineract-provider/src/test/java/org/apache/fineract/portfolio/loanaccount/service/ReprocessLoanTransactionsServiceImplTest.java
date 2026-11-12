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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionFeeRefundPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReprocessLoanTransactionsServiceImplTest {

    @Mock
    private LoanAccountService loanAccountService;
    @Mock
    private LoanTransactionProcessingService loanTransactionProcessingService;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private LoanTransactionService loanTransactionService;
    @Mock
    private LoanJournalEntryPoster loanJournalEntryPoster;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private LoanFeeRefundService loanFeeRefundService;
    @Mock
    private ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService;
    @Mock
    private LoanBalanceService loanBalanceService;
    @Mock
    private LoanAccrualActivityProcessingService loanAccrualActivityProcessingService;
    @InjectMocks
    private ReprocessLoanTransactionsServiceImpl underTest;

    private final Loan loan = mock(Loan.class);
    private final LoanTransaction restoredFeeRefund = mock(LoanTransaction.class);
    private final List<LoanTransaction> afterReplay = List.of(mock(LoanTransaction.class), restoredFeeRefund);

    @BeforeEach
    public void setUp() {
        when(loan.getLoanTransactions()).thenReturn(new ArrayList<>());
        when(loanTransactionProcessingService.reprocessLoanTransactions(any(), any(), anyList(), any(), any(), any()))
                .thenReturn(new ChangedTransactionDetail());
    }

    @Test
    public void testReprocessTransactionsRestoresTheMissingFeeRefundsAfterTheReplayAndReplaysThem() {
        final List<LoanTransaction> beforeReplay = List.of(mock(LoanTransaction.class));
        when(loanFeeRefundService.createMissingFeeRefunds(loan)).thenReturn(List.of(restoredFeeRefund));
        when(loanTransactionService.retrieveListOfTransactionsForReprocessing(loan)).thenReturn(beforeReplay)
                .thenReturn(new ArrayList<>(afterReplay));

        underTest.reprocessTransactions(loan);

        verifyReplayedThenRestoredAndReplayedAgain(beforeReplay, afterReplay);
    }

    @Test
    public void testReprocessTransactionsWithNewTransactionsRestoresTheMissingFeeRefundsAndReplaysThemWithTheNewTransactions() {
        final LoanTransaction persisted = mock(LoanTransaction.class);
        final LoanTransaction newTransaction = mock(LoanTransaction.class);
        when(loanTransactionRepository.findNonReversedTransactionsForReprocessingByLoan(loan))
                .thenReturn(new ArrayList<>(List.of(persisted)));
        when(loanFeeRefundService.createMissingFeeRefunds(loan)).thenReturn(List.of(restoredFeeRefund));
        when(loanTransactionService.retrieveListOfTransactionsForReprocessing(loan)).thenReturn(new ArrayList<>(afterReplay));

        underTest.reprocessTransactions(loan, List.of(newTransaction));

        // The new transaction is not on the loan yet, so the second replay must get it as the first one did
        final List<LoanTransaction> secondReplay = new ArrayList<>(afterReplay);
        secondReplay.add(newTransaction);
        verifyReplayedThenRestoredAndReplayedAgain(List.of(persisted, newTransaction), secondReplay);
    }

    @Test
    public void testReprocessTransactionsReplaysOnceWhenNoFeeRefundIsMissing() {
        when(loanFeeRefundService.createMissingFeeRefunds(loan)).thenReturn(List.of());
        when(loanTransactionService.retrieveListOfTransactionsForReprocessing(loan)).thenReturn(new ArrayList<>());

        underTest.reprocessTransactions(loan);

        verify(loanTransactionProcessingService, times(1)).reprocessLoanTransactions(any(), any(), anyList(), any(), any(), any());
        verify(loanAccountService, never()).saveLoanTransactionWithDataIntegrityViolationChecks(any());
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    private void verifyReplayedThenRestoredAndReplayedAgain(final List<LoanTransaction> firstReplay,
            final List<LoanTransaction> secondReplay) {
        final InOrder inOrder = inOrder(loanFeeRefundService, loanTransactionProcessingService, loanAccountService, loanJournalEntryPoster,
                businessEventNotifierService);
        inOrder.verify(loanTransactionProcessingService).reprocessLoanTransactions(any(), any(), eq(firstReplay), any(), any(), any());
        inOrder.verify(loanFeeRefundService).createMissingFeeRefunds(loan);
        inOrder.verify(loanTransactionProcessingService).reprocessLoanTransactions(any(), any(), eq(secondReplay), any(), any(), any());
        inOrder.verify(loanAccountService).saveLoanTransactionWithDataIntegrityViolationChecks(restoredFeeRefund);
        inOrder.verify(loanJournalEntryPoster).postJournalEntriesForLoanTransaction(restoredFeeRefund, false, false);
        inOrder.verify(businessEventNotifierService)
                .notifyPostBusinessEvent(argThat(event -> event instanceof LoanTransactionFeeRefundPostBusinessEvent feeRefundEvent
                        && restoredFeeRefund.equals(feeRefundEvent.get())));
    }
}
