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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargeWaiverDomainService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
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
 * Adjusting a transaction reverses it and books a replacement. When the reversal has to replay the history, the
 * replacement is booked into that same replay, so the loan is replayed - and later transactions restated and reported -
 * once, not once for the reversal and again for the replacement.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanAdjustByDeltaReplayTest {

    private static final Long LOAN_ID = 31L;
    private static final Long TRANSACTION_ID = 7L;
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 2, 20);
    private static final LocalDate ORIGINAL_DATE = LocalDate.of(2026, 2, 5);
    private static final BigDecimal ORIGINAL_AMOUNT = new BigDecimal("100.00");

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

    @InjectMocks
    private WorkingCapitalLoanWritePlatformServiceImpl writePlatformService;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
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
    void chargedLoanIsReplayedOnceWithTheReplacementAlreadyBooked() {
        givenDelta("40.00");
        givenLaterTransactionsExist();
        givenActiveCharges();

        writePlatformService.adjustByDeltaTransaction(LOAN_ID, TRANSACTION_ID, command);

        final WorkingCapitalLoanTransaction replacement = replayedReplacement();
        assertThat(replacement.getTransactionAmount()).isEqualByComparingTo("140.00");
        assertThat(replacement.getTransactionDate()).isEqualTo(ORIGINAL_DATE);

        final InOrder order = inOrder(transactionRepository, transactionReprocessingService);
        order.verify(transactionRepository).saveAndFlush(replacement);
        order.verify(transactionReprocessingService).reprocessTransactions(loan);
        verify(transactionReprocessingService, times(1)).reprocessTransactions(loan);
        verify(transactionReprocessingService, never()).reprocessChargeFreeSuffix(any(), any(), any());
        verify(transactionProcessor, never()).processRepaymentLikeTransaction(any(), any(), any(), any());
    }

    @Test
    void chargeFreeSuffixReplayStartsAtTheEarlierReplacementDate() {
        givenDelta("-40.00");
        final LocalDate earlierDate = ORIGINAL_DATE.minusDays(3);
        when(command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName)).thenReturn(earlierDate);
        givenLaterTransactionsExist();

        writePlatformService.adjustByDeltaTransaction(LOAN_ID, TRANSACTION_ID, command);

        final WorkingCapitalLoanTransaction replacement = replayedReplacement();
        assertThat(replacement.getTransactionAmount()).isEqualByComparingTo("60.00");
        assertThat(replacement.getTransactionDate()).isEqualTo(earlierDate);
        verify(transactionReprocessingService, times(1)).reprocessChargeFreeSuffix(loan, earlierDate, original);
        verify(transactionReprocessingService, never()).reprocessTransactions(any());
        verify(transactionProcessor, never()).processRepaymentLikeTransaction(any(), any(), any(), any());
    }

    @Test
    void chargeFreeSuffixReplayStartsAtTheOriginalDateWhenTheReplacementIsLater() {
        givenDelta("-40.00");
        when(command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName))
                .thenReturn(ORIGINAL_DATE.plusDays(3));
        givenLaterTransactionsExist();

        writePlatformService.adjustByDeltaTransaction(LOAN_ID, TRANSACTION_ID, command);

        replayedReplacement();
        verify(transactionReprocessingService, times(1)).reprocessChargeFreeSuffix(loan, ORIGINAL_DATE, original);
    }

    @Test
    void lastMonetaryActionIsUnwoundInPlaceAndTheReplacementBookedOnItsOwn() {
        givenDelta("40.00");
        when(transactionProcessor.isLastMonetaryAction(original)).thenReturn(true);

        writePlatformService.adjustByDeltaTransaction(LOAN_ID, TRANSACTION_ID, command);

        final ArgumentCaptor<WorkingCapitalLoanTransaction> booked = ArgumentCaptor.forClass(WorkingCapitalLoanTransaction.class);
        verify(transactionProcessor).processRepaymentLikeTransaction(eq(loan), booked.capture(), eq(ORIGINAL_DATE),
                eq(new BigDecimal("140.00")));
        assertThat(booked.getValue().getTransactionAmount()).isEqualByComparingTo("140.00");
        verify(transactionProcessor, never()).completeReplayedRepaymentLikeTransaction(any(), any(), any(), any());
        verify(transactionReprocessingService, never()).reprocessTransactions(any());
        verify(transactionReprocessingService, never()).reprocessChargeFreeSuffix(any(), any(), any());
    }

    @Test
    void adjustByDeltaLinksTheReplacementToTheReversedOriginalAsAnAdjustment() {
        givenDelta("40.00");
        givenLaterTransactionsExist();

        writePlatformService.adjustByDeltaTransaction(LOAN_ID, TRANSACTION_ID, command);

        assertThat(replayedReplacement().getLoanTransactionRelations()).singleElement().satisfies(relation -> {
            assertThat(relation.getRelationType()).isEqualTo(LoanTransactionRelationTypeEnum.ADJUSTMENT);
            assertThat(relation.getToTransaction()).isSameAs(original);
        });
        verify(adjustTransactionEventPublisher).publishAdjustment(eq(LOAN_ID), eq(original), any());
    }

    @Test
    void absoluteAdjustKeepsLinkingTheReplacementAsReplayed() {
        when(command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanConstants.transactionAmountParamName))
                .thenReturn(new BigDecimal("140.00"));
        givenLaterTransactionsExist();

        writePlatformService.adjustTransaction(LOAN_ID, TRANSACTION_ID, command);

        assertThat(replayedReplacement().getLoanTransactionRelations()).extracting(WorkingCapitalLoanTransactionRelation::getRelationType)
                .containsExactly(LoanTransactionRelationTypeEnum.REPLAYED);
    }

    @Test
    void fullDecreaseOnlyReversesTheOriginal() {
        givenDelta("-100.00");
        givenLaterTransactionsExist();

        writePlatformService.adjustByDeltaTransaction(LOAN_ID, TRANSACTION_ID, command);

        verify(original).setReversed(true);
        verify(transactionReprocessingService, times(1)).reprocessChargeFreeSuffix(loan, ORIGINAL_DATE, original);
        verify(adjustTransactionEventPublisher).publishReversal(LOAN_ID, original);
        verify(adjustTransactionEventPublisher, never()).publishAdjustment(anyLong(), any(), any());
        verify(transactionRepository, never()).saveAndFlush(any());
        verify(transactionProcessor, never()).processRepaymentLikeTransaction(any(), any(), any(), any());
        verify(transactionProcessor, never()).completeReplayedRepaymentLikeTransaction(any(), any(), any(), any());
    }

    private void givenDelta(final String delta) {
        when(fromApiJsonHelper.extractBigDecimalNamed(eq(WorkingCapitalLoanConstants.transactionAmountParamName), any(), anySet()))
                .thenReturn(new BigDecimal(delta));
    }

    private void givenLaterTransactionsExist() {
        when(transactionProcessor.isLastMonetaryAction(original)).thenReturn(false);
    }

    private void givenActiveCharges() {
        when(chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(LOAN_ID)).thenReturn(List.of(mock(WorkingCapitalLoanCharge.class)));
    }

    private WorkingCapitalLoanTransaction replayedReplacement() {
        final ArgumentCaptor<WorkingCapitalLoanTransaction> replacement = ArgumentCaptor.forClass(WorkingCapitalLoanTransaction.class);
        verify(transactionProcessor).completeReplayedRepaymentLikeTransaction(eq(loan), replacement.capture(), any(), any());
        return replacement.getValue();
    }
}
