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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanTransactionWritePlatformServiceImplTest {

    @Mock
    private LoanTransactionValidator loanTransactionValidator;
    @Mock
    private LoanAccountDomainService loanAccountDomainService;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private LoanAssembler loanAssembler;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    @Mock
    private AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private LoanUtilService loanUtilService;
    @Mock
    private LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository;
    @Mock
    private LoanLifecycleStateMachine loanLifecycleStateMachine;
    @Mock
    private org.apache.fineract.infrastructure.core.service.ExternalIdFactory externalIdFactory;
    @Mock
    private LoanAccrualsProcessingService loanAccrualsProcessingService;
    @Mock
    private LoanScheduleService loanScheduleService;
    @Mock
    private ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    @Mock
    private LoanAccountService loanAccountService;
    @Mock
    private LoanJournalEntryPoster journalEntryPoster;
    @Mock
    private LoanTransactionProcessingService loanTransactionProcessingService;
    @Mock
    private LoanBalanceService loanBalanceService;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private LoanReadPlatformService loanReadPlatformService;
    @Mock
    private GLIMAccountInfoRepository glimRepository;
    @Mock
    private LoanAdjustmentService loanAdjustmentService;
    @Mock
    private GuarantorDomainService guarantorDomainService;
    @Mock
    private LoanTransactionAssembler loanTransactionAssembler;
    @Mock
    private LoanTransactionRelationRepository loanTransactionRelationRepository;
    @Mock
    private LoanTransactionService loanTransactionService;
    @Mock
    private LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator;
    @Mock
    private LoanDownPaymentHandlerService loanDownPaymentHandlerService;

    @InjectMocks
    private LoanTransactionWritePlatformServiceImpl underTest;

    @Test
    void recoverFromGuarantor_success() {
        Long loanId = 1L;
        Loan loan = mock(Loan.class);
        ExternalId externalId = ExternalId.generate();
        when(loanAssembler.assembleFromInUserScope(loanId)).thenReturn(loan);
        when(loan.getExternalId()).thenReturn(externalId);

        CommandProcessingResult result = underTest.recoverFromGuarantor(loanId);

        assertNotNull(result);
        assertEquals(loanId, result.getLoanId());
        assertEquals(loanId, result.getResourceId());
        assertEquals(externalId, result.getResourceExternalId());
        verify(guarantorDomainService).transferFundsFromGuarantor(loan);
    }

    @Test
    void handleChargebackTransaction_interestRecalculationEnabled_reprocessesTransactions() {
        Loan loan = mock(Loan.class);
        LoanTransaction chargebackTransaction = mock(LoanTransaction.class);
        LocalDate date = LocalDate.of(2025, 5, 20);

        when(chargebackTransaction.getTransactionDate()).thenReturn(date);
        when(loan.isInterestBearing()).thenReturn(true);
        when(loan.isInterestRecalculationEnabled()).thenReturn(true);

        underTest.handleChargebackTransaction(loan, chargebackTransaction);

        verify(loanTransactionValidator).validateIfTransactionIsChargeback(chargebackTransaction);
        verify(loan).addLoanTransaction(chargebackTransaction);
        verify(reprocessLoanTransactionsService).reprocessTransactions(loan);
        verify(loanLifecycleStateMachine).determineAndTransition(loan, date);
    }

    @Test
    void makeGLIMLoanRepayment_parentNotFound_throwsException() {
        Long loanId = 10L;
        JsonCommand command = mock(JsonCommand.class);
        when(glimRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> underTest.makeGLIMLoanRepayment(loanId, command));
    }
}
