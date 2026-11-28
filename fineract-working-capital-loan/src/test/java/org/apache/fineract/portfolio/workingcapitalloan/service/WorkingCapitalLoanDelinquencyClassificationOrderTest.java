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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
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
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargeWaiverDomainService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanEvent;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanPeriodPaymentRateChangeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Every Working Capital loan transaction that can change the loan status classifies delinquency once, at the end: after
 * the status transition and after the loan has been saved and flushed, and before the business events. An extension of
 * {@code classifyDelinquency} therefore always sees the status the transaction leaves the loan in.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDelinquencyClassificationOrderTest {

    private static final Long LOAN_ID = 41L;
    private static final Long TRANSACTION_ID = 9L;
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 6, 1);

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
    private WorkingCapitalLoanDelinquencyClassificationService delinquencyClassificationService;
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
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanTransaction transaction;
    @Mock
    private WorkingCapitalLoanBalance balance;
    @Mock
    private JsonCommand command;
    @Mock
    private Office office;
    @Mock
    private PlatformSecurityContext context;
    @InjectMocks
    private WorkingCapitalLoanWritePlatformServiceImpl writePlatformService;

    private final AtomicReference<LoanStatus> status = new AtomicReference<>();
    private final List<LoanStatus> statusesSeenByClassification = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        when(loan.getOffice()).thenReturn(office);
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.getLoanStatus()).thenAnswer(invocation -> status.get());
        when(loan.getStatus()).thenAnswer(invocation -> status.get());
        when(loan.isOverpaid()).thenAnswer(invocation -> status.get() == LoanStatus.OVERPAID);
        when(loan.isOpen()).thenAnswer(invocation -> status.get() == LoanStatus.ACTIVE);
        when(externalIdFactory.createFromCommand(any(), any())).thenReturn(ExternalId.empty());
        when(externalIdFactory.create(any())).thenReturn(ExternalId.empty());
        when(command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName)).thenReturn(BUSINESS_DATE);
        when(fromApiJsonHelper.extractBigDecimalWithLocaleNamed(eq(WorkingCapitalLoanConstants.transactionAmountParamName), any()))
                .thenReturn(BigDecimal.TEN);
        when(transactionRepository.findByIdAndWcLoan_Id(TRANSACTION_ID, LOAN_ID)).thenReturn(Optional.of(transaction));
        // No classification code value on any of the commands.
        when(fromApiJsonHelper.extractLongNamed(eq(WorkingCapitalLoanConstants.classificationIdParamName), any())).thenReturn(null);
        when(command.longValueOfParameterNamed(WorkingCapitalLoanConstants.classificationIdParamName)).thenReturn(null);
        when(transaction.getTransactionAmount()).thenReturn(BigDecimal.TEN);
        when(transaction.getTransactionDate()).thenReturn(BUSINESS_DATE);
        when(transaction.getReversedOnDate()).thenReturn(BUSINESS_DATE);
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));
        when(delinquencyRangeScheduleService.reprocessSchedulePeriods(loan))
                .thenReturn(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        doAnswer(invocation -> statusesSeenByClassification.add(status.get())).when(delinquencyClassificationService)
                .classifyDelinquencyOnFinalState(eq(loan), any());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @ParameterizedTest
    @EnumSource(value = LoanTransactionType.class, names = { "REPAYMENT", "GOODWILL_CREDIT", "PAYOUT_REFUND" })
    void repaymentLikeTransactionThatClosesTheLoanIsClassifiedAfterTheClosingAndTheSave(final LoanTransactionType type) {
        status.set(LoanStatus.ACTIVE);
        // The processor owns the status transition; it reports that the in-place schedule update owes an instant
        // classification.
        when(transactionProcessor.processRepaymentLikeTransaction(eq(loan), any(), eq(BUSINESS_DATE), eq(BigDecimal.TEN)))
                .thenAnswer(invocation -> {
                    status.set(LoanStatus.CLOSED_OBLIGATIONS_MET);
                    return WorkingCapitalLoanDelinquencyClassificationMode.INSTANT;
                });

        switch (type) {
            case REPAYMENT -> writePlatformService.makeRepayment(LOAN_ID, command);
            case GOODWILL_CREDIT -> writePlatformService.makeGoodwillCredit(LOAN_ID, command);
            default -> writePlatformService.makePayoutRefund(LOAN_ID, command);
        }

        final InOrder order = inOrder(transactionProcessor, loanRepository, delinquencyClassificationService, businessEventNotifierService);
        order.verify(transactionProcessor).processRepaymentLikeTransaction(eq(loan), any(), eq(BUSINESS_DATE), eq(BigDecimal.TEN));
        order.verify(loanRepository).saveAndFlush(loan);
        order.verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(loan,
                WorkingCapitalLoanDelinquencyClassificationMode.INSTANT);
        order.verify(businessEventNotifierService, atLeastOnce()).notifyPostBusinessEvent(any());
        assertThat(statusesSeenByClassification).containsExactly(LoanStatus.CLOSED_OBLIGATIONS_MET);
    }

    @Test
    void undoThatReopensAClosedLoanIsClassifiedAfterTheReopeningAndTheSave() {
        status.set(LoanStatus.CLOSED_OBLIGATIONS_MET);
        givenARepaymentToUndo();

        writePlatformService.undoTransaction(LOAN_ID, TRANSACTION_ID, command);

        assertClassifiedOnceAfterTransitionAndSave(LoanStatus.ACTIVE);
        verify(adjustTransactionEventPublisher).publishReversal(LOAN_ID, transaction);
    }

    @Test
    void undoThatReopensAnOverpaidLoanIsClassifiedAfterTheReopeningAndTheSave() {
        status.set(LoanStatus.OVERPAID);
        givenARepaymentToUndo();

        writePlatformService.undoTransaction(LOAN_ID, TRANSACTION_ID, command);

        assertClassifiedOnceAfterTransitionAndSave(LoanStatus.ACTIVE);
    }

    @Test
    void undoChargeWaiverIsClassifiedAfterTheReopeningAndTheSave() {
        status.set(LoanStatus.CLOSED_OBLIGATIONS_MET);
        when(transaction.getTypeOf()).thenReturn(LoanTransactionType.WAIVE_CHARGES);
        final WorkingCapitalLoanTransactionRelation relation = mock(WorkingCapitalLoanTransactionRelation.class);
        when(relation.getToCharge()).thenReturn(new WorkingCapitalLoanCharge());
        when(transaction.getLoanTransactionRelations()).thenReturn(Set.of(relation));
        givenTransitionTo(LoanStatus.ACTIVE);

        writePlatformService.undoTransaction(LOAN_ID, TRANSACTION_ID, command);

        assertClassifiedOnceAfterTransitionAndSave(LoanStatus.ACTIVE);
    }

    @Test
    void undoDiscountFeeAdjustmentIsClassifiedAfterTheReopeningAndTheSave() {
        status.set(LoanStatus.CLOSED_OBLIGATIONS_MET);
        when(transaction.getTypeOf()).thenReturn(LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT);
        givenDiscountDetails();
        givenTransitionTo(LoanStatus.ACTIVE);

        writePlatformService.undoTransaction(LOAN_ID, TRANSACTION_ID, command);

        assertClassifiedOnceAfterTransitionAndSave(LoanStatus.ACTIVE);
    }

    @Test
    void discountFeeAdjustmentThatClosesTheLoanIsClassifiedAfterTheClosingAndTheSave() {
        status.set(LoanStatus.ACTIVE);
        givenDiscountDetails();
        when(fromApiJsonHelper.extractLongNamed(eq(WorkingCapitalLoanConstants.relatedResourceIdParamName), any()))
                .thenReturn(TRANSACTION_ID);
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));
        when(transaction.getTypeOf()).thenReturn(LoanTransactionType.DISCOUNT_FEE);
        when(command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName)).thenReturn(null);
        givenTransitionTo(LoanStatus.CLOSED_OBLIGATIONS_MET);

        writePlatformService.makeDiscountFeeAdjustment(LOAN_ID, command);

        assertClassifiedOnceAfterTransitionAndSave(LoanStatus.CLOSED_OBLIGATIONS_MET);
    }

    @Test
    void discountFeeIsClassifiedAfterTheSave() {
        status.set(LoanStatus.ACTIVE);
        givenDiscountDetails();
        when(fromApiJsonHelper.extractLongNamed(eq(WorkingCapitalLoanConstants.relatedResourceIdParamName), any()))
                .thenReturn(TRANSACTION_ID);
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));
        when(transaction.getTypeOf()).thenReturn(LoanTransactionType.DISBURSEMENT);
        when(command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName)).thenReturn(null);
        when(balance.getTotalDiscountFee()).thenReturn(BigDecimal.ZERO);

        writePlatformService.makeDiscountFee(LOAN_ID, command);

        final InOrder order = inOrder(delinquencyRangeScheduleService, loanRepository, delinquencyClassificationService,
                businessEventNotifierService);
        order.verify(delinquencyRangeScheduleService).reprocessSchedulePeriods(loan);
        order.verify(loanRepository).saveAndFlush(loan);
        order.verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(loan,
                WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        order.verify(businessEventNotifierService, atLeastOnce()).notifyPostBusinessEvent(any());
        verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(any(), any());
    }

    @Test
    void creditBalanceRefundThatClosesTheOverpaidLoanIsClassifiedAfterTheClosingAndTheSave() {
        status.set(LoanStatus.OVERPAID);
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.TEN);
        // After the refund the loan owes nothing and holds no overpayment, so the refund closes it.
        final WorkingCapitalLoanBalance settledBalance = mock(WorkingCapitalLoanBalance.class);
        when(settledBalance.getPrincipalOutstanding()).thenReturn(BigDecimal.ZERO);
        when(settledBalance.getOverpaymentAmount()).thenReturn(BigDecimal.ZERO);
        when(loan.getBalance()).thenReturn(settledBalance);
        doAnswer(invocation -> {
            status.set(LoanStatus.CLOSED_OBLIGATIONS_MET);
            return null;
        }).when(stateMachine).transition(eq(WorkingCapitalLoanEvent.LOAN_CREDIT_BALANCE_REFUND_IN_FULL), eq(loan), any());

        writePlatformService.creditBalanceRefund(LOAN_ID, command);

        final InOrder order = inOrder(stateMachine, loanRepository, delinquencyClassificationService, businessEventNotifierService);
        order.verify(stateMachine).transition(WorkingCapitalLoanEvent.LOAN_CREDIT_BALANCE_REFUND_IN_FULL, loan, BUSINESS_DATE);
        order.verify(loanRepository).saveAndFlush(loan);
        order.verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(loan,
                WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        order.verify(businessEventNotifierService, atLeastOnce()).notifyPostBusinessEvent(any());
        assertThat(statusesSeenByClassification).containsExactly(LoanStatus.CLOSED_OBLIGATIONS_MET);
    }

    @Test
    void partialCreditBalanceRefundThatLeavesTheLoanOverpaidIsNotClassified() {
        status.set(LoanStatus.OVERPAID);
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.TEN);
        // After the refund part of the overpayment is still held, so the loan stays overpaid.
        final WorkingCapitalLoanBalance stillOverpaidBalance = mock(WorkingCapitalLoanBalance.class);
        when(stillOverpaidBalance.getPrincipalOutstanding()).thenReturn(BigDecimal.ZERO);
        when(stillOverpaidBalance.getOverpaymentAmount()).thenReturn(BigDecimal.ONE);
        when(loan.getBalance()).thenReturn(stillOverpaidBalance);

        writePlatformService.creditBalanceRefund(LOAN_ID, command);

        verify(stateMachine, never()).transition(any(), any(), any());
        verify(delinquencyClassificationService, never()).classifyDelinquencyOnFinalState(any(), any());
    }

    @Test
    void disbursementIsClassifiedAfterTheTransitionAndTheSave() {
        givenADisbursementOn(BUSINESS_DATE);
        // Nothing has ended on the starting period of an on-time disbursement.
        when(delinquencyRangeScheduleService.reprocessSchedulePeriodsIfAnyEnded(loan))
                .thenReturn(WorkingCapitalLoanDelinquencyClassificationMode.NONE);

        writePlatformService.disburseLoan(LOAN_ID, command);

        final InOrder order = inOrder(stateMachine, loanRepository, delinquencyClassificationService, businessEventNotifierService);
        order.verify(stateMachine).transition(WorkingCapitalLoanEvent.LOAN_DISBURSED, loan, BUSINESS_DATE);
        order.verify(loanRepository).saveAndFlush(loan);
        order.verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(loan,
                WorkingCapitalLoanDelinquencyClassificationMode.NONE);
        order.verify(businessEventNotifierService, atLeastOnce()).notifyPostBusinessEvent(any());
        assertThat(statusesSeenByClassification).containsExactly(LoanStatus.ACTIVE);
    }

    @Test
    void backdatedDisbursementBringsTheScheduleUpToDateBeforeItIsClassified() {
        final LocalDate disbursementDate = BUSINESS_DATE.minusYears(1);
        givenADisbursementOn(disbursementDate);
        // The starting period ended long ago: the schedule is rebuilt as the COB would, and that rebuild owes ALWAYS.
        when(delinquencyRangeScheduleService.reprocessSchedulePeriodsIfAnyEnded(loan))
                .thenReturn(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);

        writePlatformService.disburseLoan(LOAN_ID, command);

        final InOrder order = inOrder(stateMachine, delinquencyRangeScheduleService, loanRepository, delinquencyClassificationService);
        order.verify(stateMachine).transition(WorkingCapitalLoanEvent.LOAN_DISBURSED, loan, disbursementDate);
        order.verify(delinquencyRangeScheduleService).reprocessSchedulePeriodsIfAnyEnded(loan);
        order.verify(loanRepository).saveAndFlush(loan);
        order.verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(loan,
                WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(any(), any());
        assertThat(statusesSeenByClassification).containsExactly(LoanStatus.ACTIVE);
    }

    private void givenADisbursementOn(final LocalDate disbursementDate) {
        status.set(LoanStatus.APPROVED);
        when(stateMachine.canTransition(WorkingCapitalLoanEvent.LOAN_DISBURSED, loan)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.actualDisbursementDateParamName))
                .thenReturn(disbursementDate);
        when(loan.getLoanProductRelatedDetails()).thenReturn(new WorkingCapitalLoanProductRelatedDetails());
        when(loan.getLoanProduct()).thenReturn(mock(WorkingCapitalLoanProduct.class, Answers.RETURNS_DEEP_STUBS));
        doAnswer(invocation -> {
            status.set(LoanStatus.ACTIVE);
            return null;
        }).when(stateMachine).transition(eq(WorkingCapitalLoanEvent.LOAN_DISBURSED), eq(loan), any());
    }

    private void assertClassifiedOnceAfterTransitionAndSave(final LoanStatus expectedStatus) {
        final InOrder order = inOrder(stateMachine, loanRepository, delinquencyClassificationService, businessEventNotifierService,
                adjustTransactionEventPublisher);
        order.verify(stateMachine).determineAndTransition(eq(loan), any());
        order.verify(loanRepository).saveAndFlush(loan);
        order.verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(loan,
                WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        order.verify(businessEventNotifierService, atLeastOnce()).notifyPostBusinessEvent(any());
        assertThat(statusesSeenByClassification).containsExactly(expectedStatus);
    }

    private void givenARepaymentToUndo() {
        when(transaction.getTypeOf()).thenReturn(LoanTransactionType.REPAYMENT);
        when(transaction.getTransactionType()).thenReturn(LoanTransactionType.REPAYMENT);
        // Not the last monetary action, so the undo replays the charge-free suffix and rebuilds the delinquency
        // schedule.
        when(transactionProcessor.isLastMonetaryAction(transaction)).thenReturn(false);
        givenTransitionTo(LoanStatus.ACTIVE);
    }

    private void givenTransitionTo(final LoanStatus newStatus) {
        doAnswer(invocation -> {
            status.set(newStatus);
            return null;
        }).when(stateMachine).determineAndTransition(eq(loan), any());
    }

    private void givenDiscountDetails() {
        final WorkingCapitalLoanProductRelatedDetails details = new WorkingCapitalLoanProductRelatedDetails();
        details.setDiscount(BigDecimal.valueOf(50));
        when(loan.getLoanProductRelatedDetails()).thenReturn(details);
        when(balance.getTotalDiscountFeeAdjustment()).thenReturn(BigDecimal.ZERO);
        when(balance.getPrincipal()).thenReturn(BigDecimal.valueOf(100));
        when(balance.getTotalPrincipalDue()).thenReturn(BigDecimal.valueOf(100));
        when(balance.getPrincipalPaid()).thenReturn(BigDecimal.ZERO);
        when(transaction.getTransactionAmount()).thenReturn(BigDecimal.valueOf(50));
    }
}
