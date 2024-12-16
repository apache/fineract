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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.teller.data.CashierTransactionDataValidator;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksAssembler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanWritePlatformServiceJpaRepositoryImplTest {

    @InjectMocks
    private LoanWritePlatformServiceJpaRepositoryImpl loanWritePlatformService;

    @Mock
    private LoanRepaymentScheduleTransactionProcessorFactory transactionProcessorFactory;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private LoanTransactionValidator loanTransactionValidator;
    @Mock
    private LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private LoanAccountDomainService loanAccountDomainService;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private LoanTransactionRelationRepository loanTransactionRelationRepository;
    @Mock
    private LoanAssembler loanAssembler;
    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;
    @Mock
    private CalendarInstanceRepository calendarInstanceRepository;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    @Mock
    private HolidayRepositoryWrapper holidayRepository;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private WorkingDaysRepositoryWrapper workingDaysRepository;
    @Mock
    private AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    @Mock
    private AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    @Mock
    private AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    @Mock
    private LoanReadPlatformService loanReadPlatformService;
    @Mock
    private FromJsonHelper fromApiJsonHelper;
    @Mock
    private CalendarRepository calendarRepository;
    @Mock
    private LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService;
    @Mock
    private LoanApplicationValidator loanApplicationCommandFromApiJsonHelper;
    @Mock
    private AccountAssociationsRepository accountAssociationRepository;
    @Mock
    private AccountTransferDetailRepository accountTransferDetailRepository;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private GuarantorDomainService guarantorDomainService;
    @Mock
    private LoanUtilService loanUtilService;
    @Mock
    private EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    @Mock
    private LoanRepaymentScheduleTransactionProcessorFactory transactionProcessingStrategy;
    @Mock
    private CodeValueRepositoryWrapper codeValueRepository;
    @Mock
    private CashierTransactionDataValidator cashierTransactionDataValidator;
    @Mock
    private GLIMAccountInfoRepository glimRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler;
    @Mock
    private PostDatedChecksRepository postDatedChecksRepository;
    @Mock
    private LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository;
    @Mock
    private LoanLifecycleStateMachine loanLifecycleStateMachine;
    @Mock
    private LoanAccountLockService loanAccountLockService;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService;
    @Mock
    private LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;
    @Mock
    private ErrorHandler errorHandler;
    @Mock
    private LoanDownPaymentHandlerService loanDownPaymentHandlerService;
    @Mock
    private AccountTransferRepository accountTransferRepository;
    @Mock
    private LoanTransactionAssembler loanTransactionAssembler;
    @Mock
    private LoanAccrualsProcessingService loanAccrualsProcessingService;
    @Mock
    private LoanChargeValidator loanChargeValidator;

    @Test
    void givenMerchantIssuedRefundTransactionWithRelatedTransactions_whenAdjustExistingTransaction_thenRelatedTransactionsAreReversedAndEventsTriggered() {
        // Arrange
        Loan loan = mock(Loan.class);
        LoanTransaction transactionForAdjustment = mock(LoanTransaction.class);
        LoanTransaction newTransactionDetail = mock(LoanTransaction.class);
        ScheduleGeneratorDTO scheduleGeneratorDTO = mock(ScheduleGeneratorDTO.class);
        ExternalId reversalExternalId = ExternalId.generate();

        List<Long> existingTransactionIds = new ArrayList<>();
        List<Long> existingReversedTransactionIds = new ArrayList<>();

        // Mock transaction type
        when(transactionForAdjustment.getTypeOf()).thenReturn(LoanTransactionType.MERCHANT_ISSUED_REFUND);
        when(transactionForAdjustment.isNotRepaymentLikeType()).thenReturn(false);

        // Mock transaction date
        when(transactionForAdjustment.getTransactionDate()).thenReturn(LocalDate.now(ZoneId.systemDefault()));

        // Mock transaction ID to prevent NullPointerException
        when(transactionForAdjustment.getId()).thenReturn(1L);

        // Mock loan transactions
        LoanTransaction relatedTransaction = mock(LoanTransaction.class);
        when(relatedTransaction.isNotReversed()).thenReturn(true);

        LoanTransactionRelation transactionRelation = mock(LoanTransactionRelation.class);
        when(transactionRelation.getRelationType()).thenReturn(LoanTransactionRelationTypeEnum.RELATED);
        when(transactionRelation.getToTransaction()).thenReturn(transactionForAdjustment);

        Set<LoanTransactionRelation> transactionRelations = new HashSet<>();
        transactionRelations.add(transactionRelation);

        when(relatedTransaction.getLoanTransactionRelations()).thenReturn(transactionRelations);

        List<LoanTransaction> loanTransactions = Arrays.asList(transactionForAdjustment, relatedTransaction);
        when(loan.getLoanTransactions()).thenReturn(loanTransactions);

        // Mock methods called inside adjustExistingTransaction
        when(loan.findExistingTransactionIds()).thenReturn(Collections.emptyList());
        when(loan.findExistingReversedTransactionIds()).thenReturn(Collections.emptyList());
        doNothing().when(loanTransactionValidator).validateActivityNotBeforeClientOrGroupTransferDate(any(), any(), any());
        when(loan.isClosedWrittenOff()).thenReturn(false);
        when(newTransactionDetail.isRepaymentLikeType()).thenReturn(true);

        // Act
        loanWritePlatformService.adjustExistingTransaction(loan, newTransactionDetail, loanLifecycleStateMachine, transactionForAdjustment,
                existingTransactionIds, existingReversedTransactionIds, scheduleGeneratorDTO, reversalExternalId);

        // Assert
        // Verify that related transaction is reversed and event is triggered
        verify(relatedTransaction).reverse();
        verify(relatedTransaction).manuallyAdjustedOrReversed();

        ArgumentCaptor<LoanAdjustTransactionBusinessEvent> eventCaptor = ArgumentCaptor.forClass(LoanAdjustTransactionBusinessEvent.class);
        verify(businessEventNotifierService).notifyPostBusinessEvent(eventCaptor.capture());

        LoanAdjustTransactionBusinessEvent event = eventCaptor.getValue();
        assertEquals(relatedTransaction, event.get().getTransactionToAdjust());
    }

    @Test
    void givenNonMerchantIssuedRefundTransaction_whenAdjustExistingTransaction_thenNoRelatedTransactionsReversed() {
        // Arrange
        Loan loan = mock(Loan.class);
        LoanTransaction transactionForAdjustment = mock(LoanTransaction.class);
        LoanTransaction newTransactionDetail = mock(LoanTransaction.class);
        ScheduleGeneratorDTO scheduleGeneratorDTO = mock(ScheduleGeneratorDTO.class);
        ExternalId reversalExternalId = ExternalId.generate();

        List<Long> existingTransactionIds = new ArrayList<>();
        List<Long> existingReversedTransactionIds = new ArrayList<>();

        // Mock transaction type
        when(transactionForAdjustment.getTypeOf()).thenReturn(LoanTransactionType.REPAYMENT);
        when(transactionForAdjustment.isNotRepaymentLikeType()).thenReturn(false);

        // Mock transaction date
        when(transactionForAdjustment.getTransactionDate()).thenReturn(LocalDate.now(ZoneId.systemDefault()));

        // Mock loan transactions
        LoanTransaction unrelatedTransaction = mock(LoanTransaction.class);

        // Mock methods called inside adjustExistingTransaction
        when(loan.findExistingTransactionIds()).thenReturn(Collections.emptyList());
        when(loan.findExistingReversedTransactionIds()).thenReturn(Collections.emptyList());
        doNothing().when(loanTransactionValidator).validateActivityNotBeforeClientOrGroupTransferDate(any(), any(), any());
        when(loan.isClosedWrittenOff()).thenReturn(false);
        when(newTransactionDetail.isRepaymentLikeType()).thenReturn(true);

        // Act
        loanWritePlatformService.adjustExistingTransaction(loan, newTransactionDetail, loanLifecycleStateMachine, transactionForAdjustment,
                existingTransactionIds, existingReversedTransactionIds, scheduleGeneratorDTO, reversalExternalId);

        // Assert
        // Verify that no related transactions are reversed
        verify(unrelatedTransaction, never()).reverse();
        verify(unrelatedTransaction, never()).manuallyAdjustedOrReversed();
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any(LoanAdjustTransactionBusinessEvent.class));
    }

}
