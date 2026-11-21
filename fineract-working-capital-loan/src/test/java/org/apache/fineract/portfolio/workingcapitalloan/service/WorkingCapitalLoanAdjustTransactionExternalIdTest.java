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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargeWaiverDomainService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanTransactionAlreadyUndoneByExternalIdException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanTransactionNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanPeriodPaymentRateChangeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Adjusting by transaction external id: the replacement can inherit the adjusted transaction's own external id, and a
 * command sent by external id resolves it while it runs, failing with the retried exception when it finds the external
 * id on a reversed transaction.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanAdjustTransactionExternalIdTest {

    private static final Long LOAN_ID = 31L;
    private static final Long TRANSACTION_ID = 7L;
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 2, 20);
    private static final LocalDate ORIGINAL_DATE = LocalDate.of(2026, 2, 5);
    private static final BigDecimal ORIGINAL_AMOUNT = new BigDecimal("100.00");
    private static final ExternalId WC123 = new ExternalId("WC123");

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private WorkingCapitalLoanRepository loanRepository;
    @Mock
    private WorkingCapitalLoanDataValidator validator;
    @Mock
    private WorkingCapitalLoanLifecycleStateMachine stateMachine;
    @Mock
    private FromJsonHelper fromApiJsonHelper;
    @Mock
    private WorkingCapitalLoanNoteRepository noteRepository;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;
    @Mock
    private WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailService;
    @Mock
    private WorkingCapitalLoanBalanceRepository balanceRepository;
    @Mock
    private WorkingCapitalLoanRecoveryPaymentWriteService recoveryPaymentWriteService;
    @Mock
    private WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;
    @Mock
    private CodeValueRepository codeValueRepository;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private WorkingCapitalLoanAccountingProcessor accountingProcessor;
    @Mock
    private WorkingCapitalLoanTransactionRelationRepository relationRepository;
    @Mock
    private WorkingCapitalLoanPeriodPaymentRateChangeRepository rateChangeRepository;
    @Mock
    private WorkingCapitalLoanDiscountFeeAmortizationService discountFeeAmortizationService;
    @Mock
    private WorkingCapitalLoanTransactionReprocessingService transactionReprocessingService;
    @Mock
    private WorkingCapitalLoanAdjustTransactionEventPublisher adjustTransactionEventPublisher;
    @Mock
    private WorkingCapitalLoanChargeRepository chargeRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleService delinquencyRangeScheduleService;
    @Mock
    private WorkingCapitalLoanBreachScheduleService breachScheduleService;
    @Mock
    private WorkingCapitalLoanTransactionProcessor transactionProcessor;
    @Mock
    private WorkingCapitalLoanChargeAccrualService chargeAccrualService;
    @Mock
    private WorkingCapitalLoanTransactionFinder transactionFinder;
    @Mock
    private WorkingCapitalLoanChargeWaiverDomainService chargeWaiverDomainService;
    @Mock
    private WorkingCapitalLoanChargeWritePlatformService chargeWritePlatformService;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanTransaction original;
    @Mock
    private WorkingCapitalLoanTransactionAllocation originalAllocation;
    @Mock
    private WorkingCapitalLoanBalance balance;
    @Mock
    private JsonCommand command;

    @Mock
    private Office office;
    @InjectMocks
    private WorkingCapitalLoanWritePlatformServiceImpl writePlatformService;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        when(loan.getOffice()).thenReturn(office);
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        when(loan.getBalance()).thenReturn(balance);
        when(balance.getPrincipalPaid()).thenReturn(ORIGINAL_AMOUNT);
        when(balance.getFeePaid()).thenReturn(BigDecimal.ZERO);
        when(balance.getPenaltyPaid()).thenReturn(BigDecimal.ZERO);
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.ZERO);

        when(transactionRepository.findByIdAndWcLoan_Id(TRANSACTION_ID, LOAN_ID)).thenReturn(Optional.of(original));
        when(original.getId()).thenReturn(TRANSACTION_ID);
        when(original.getTypeOf()).thenReturn(LoanTransactionType.REPAYMENT);
        when(original.getTransactionType()).thenReturn(LoanTransactionType.REPAYMENT);
        when(original.getTransactionAmount()).thenReturn(ORIGINAL_AMOUNT);
        when(original.getTransactionDate()).thenReturn(ORIGINAL_DATE);
        when(original.getAllocation()).thenReturn(originalAllocation);
        when(originalAllocation.getPrincipalPortion()).thenReturn(ORIGINAL_AMOUNT);
        when(originalAllocation.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(originalAllocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(originalAllocation.getOverpaymentPortion()).thenReturn(BigDecimal.ZERO);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void ownExternalIdMovesToTheReplacementAndIsReleasedBeforeTheReplacementIsInserted() {
        givenDelta("-5.00");
        givenRequestedExternalId(WC123);
        when(original.isExternalIdInheritedBy(WC123)).thenReturn(true);
        givenLaterTransactionsExist();

        final CommandProcessingResult result = writePlatformService.adjustByDeltaTransaction(LOAN_ID, TRANSACTION_ID, command);

        final WorkingCapitalLoanTransaction replacement = replayedReplacement();
        assertThat(replacement.getExternalId()).isEqualTo(WC123);
        assertThat(replacement.getTransactionAmount()).isEqualByComparingTo("95.00");
        // The unique column only lets the replacement in once the release has reached the database.
        final InOrder order = inOrder(original, transactionRepository);
        order.verify(original).releaseExternalId();
        order.verify(transactionRepository).saveAndFlush(original);
        order.verify(transactionRepository).saveAndFlush(replacement);
        assertThat(result.getSubResourceId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.getChanges()).containsEntry("externalIdInherited", true);
    }

    @Test
    void absoluteAdjustAlsoPassesTheExternalIdOn() {
        when(command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanConstants.transactionAmountParamName))
                .thenReturn(new BigDecimal("90.00"));
        givenRequestedExternalId(WC123);
        when(original.isExternalIdInheritedBy(WC123)).thenReturn(true);
        givenLaterTransactionsExist();

        writePlatformService.adjustTransaction(LOAN_ID, TRANSACTION_ID, command);

        assertThat(replayedReplacement().getExternalId()).isEqualTo(WC123);
        verify(original).releaseExternalId();
    }

    @Test
    void differentExternalIdLeavesTheReversedTransactionsExternalIdAlone() {
        givenDelta("-5.00");
        final ExternalId other = new ExternalId("WC124");
        givenRequestedExternalId(other);
        givenLaterTransactionsExist();

        final CommandProcessingResult result = writePlatformService.adjustByDeltaTransaction(LOAN_ID, TRANSACTION_ID, command);

        assertThat(replayedReplacement().getExternalId()).isEqualTo(other);
        verify(original, never()).releaseExternalId();
        verify(transactionRepository, never()).saveAndFlush(original);
        assertThat(result.getChanges()).containsEntry("externalIdInherited", false);
    }

    @Test
    void fullReversalKeepsTheExternalIdOnTheReversedTransaction() {
        givenDelta("-100.00");
        givenRequestedExternalId(WC123);
        when(original.isExternalIdInheritedBy(WC123)).thenReturn(true);
        givenLaterTransactionsExist();

        final CommandProcessingResult result = writePlatformService.adjustByDeltaTransaction(LOAN_ID, TRANSACTION_ID, command);

        verify(original, never()).releaseExternalId();
        verify(adjustTransactionEventPublisher).publishReversal(LOAN_ID, original);
        assertThat(result.getResourceId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.getSubResourceId()).isNull();
    }

    @Test
    void commandSentByExternalIdResolvesItWhileItRuns() {
        givenDelta("-5.00");
        givenSentByExternalId();
        when(transactionRepository.findByWcLoan_IdAndExternalId(LOAN_ID, WC123)).thenReturn(Optional.of(original));
        givenLaterTransactionsExist();

        writePlatformService.adjustByDeltaTransaction(LOAN_ID, null, command);

        verify(transactionRepository, never()).findByIdAndWcLoan_Id(any(), any());
        verify(validator).validateAdjustByDelta(command, loan, original);
        replayedReplacement();
    }

    @Test
    void externalIdOnAReversedTransactionFailsWithTheRetriedException() {
        givenDelta("-5.00");
        givenSentByExternalId();
        when(original.isReversed()).thenReturn(true);
        when(transactionRepository.findByWcLoan_IdAndExternalId(LOAN_ID, WC123)).thenReturn(Optional.of(original));

        final WorkingCapitalLoanTransactionAlreadyUndoneByExternalIdException ex = assertThrows(
                WorkingCapitalLoanTransactionAlreadyUndoneByExternalIdException.class,
                () -> writePlatformService.adjustByDeltaTransaction(LOAN_ID, null, command));

        assertThat(ex.getGlobalisationMessageCode()).isEqualTo("error.msg.wc.loan.transaction.already.undone");
        verify(validator, never()).validateAdjustByDelta(any(), any(), any());
        verify(transactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void undoAndAdjustSentByExternalIdResolveItTheSameWay() {
        givenSentByExternalId();
        when(original.isReversed()).thenReturn(true);
        when(transactionRepository.findByWcLoan_IdAndExternalId(LOAN_ID, WC123)).thenReturn(Optional.of(original));

        assertThrows(WorkingCapitalLoanTransactionAlreadyUndoneByExternalIdException.class,
                () -> writePlatformService.undoTransaction(LOAN_ID, null, command));
        assertThrows(WorkingCapitalLoanTransactionAlreadyUndoneByExternalIdException.class,
                () -> writePlatformService.adjustTransaction(LOAN_ID, null, command));
    }

    @Test
    void unknownExternalIdIsNotFound() {
        givenSentByExternalId();
        when(transactionRepository.findByWcLoan_IdAndExternalId(LOAN_ID, WC123)).thenReturn(Optional.empty());

        assertThrows(WorkingCapitalLoanTransactionNotFoundException.class,
                () -> writePlatformService.adjustByDeltaTransaction(LOAN_ID, null, command));
    }

    private void givenDelta(final String delta) {
        when(fromApiJsonHelper.extractBigDecimalWithLocaleNamed(eq(WorkingCapitalLoanConstants.transactionAmountParamName), any()))
                .thenReturn(new BigDecimal(delta));
    }

    private void givenRequestedExternalId(final ExternalId externalId) {
        when(command.stringValueOfParameterNamedAllowingNull(WorkingCapitalLoanConstants.externalIdParameterName))
                .thenReturn(externalId.getValue());
        when(externalIdFactory.createFromCommand(command, WorkingCapitalLoanConstants.externalIdParameterName)).thenReturn(externalId);
    }

    private void givenSentByExternalId() {
        when(command.getTransactionId()).thenReturn(WC123.getValue());
    }

    private void givenLaterTransactionsExist() {
        when(transactionProcessor.isLastMonetaryAction(original)).thenReturn(false);
    }

    private WorkingCapitalLoanTransaction replayedReplacement() {
        final ArgumentCaptor<WorkingCapitalLoanTransaction> replacement = ArgumentCaptor.forClass(WorkingCapitalLoanTransaction.class);
        verify(transactionProcessor).completeReplayedRepaymentLikeTransaction(eq(loan), replacement.capture(), any(), any());
        return replacement.getValue();
    }
}
