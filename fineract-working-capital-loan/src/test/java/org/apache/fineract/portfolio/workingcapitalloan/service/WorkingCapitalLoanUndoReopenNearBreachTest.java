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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargeWaiverDomainService;
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
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * An undo that restores an outstanding amount can reopen a loan the reversed transaction had closed. The near breach is
 * not evaluated while the loan is closed, so the reopening undo has to re-derive it for the open period.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanUndoReopenNearBreachTest {

    private static final Long LOAN_ID = 31L;
    private static final Long TRANSACTION_ID = 7L;
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

    @InjectMocks
    private WorkingCapitalLoanWritePlatformServiceImpl writePlatformService;

    private final AtomicReference<LoanStatus> status = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.getLoanStatus()).thenAnswer(invocation -> status.get());
        when(loan.isOpen()).thenAnswer(invocation -> status.get() == LoanStatus.ACTIVE);
        when(transactionRepository.findByIdAndWcLoan_Id(TRANSACTION_ID, LOAN_ID)).thenReturn(Optional.of(transaction));
        when(transaction.getTransactionAmount()).thenReturn(BigDecimal.TEN);
        when(transaction.getReversedOnDate()).thenReturn(BUSINESS_DATE);
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));
        doAnswer(invocation -> {
            status.set(LoanStatus.ACTIVE);
            return null;
        }).when(stateMachine).determineAndTransition(eq(loan), any());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void undoingTheWaiverThatClosedTheLoanRederivesTheNearBreachAfterTheReopening() {
        status.set(LoanStatus.CLOSED_OBLIGATIONS_MET);
        givenAChargeWaiver();

        writePlatformService.undoTransaction(LOAN_ID, TRANSACTION_ID, command);

        final InOrder order = inOrder(stateMachine, breachScheduleService);
        order.verify(stateMachine).determineAndTransition(loan, BUSINESS_DATE);
        order.verify(breachScheduleService).rederiveNearBreachIfReopened(loan, LoanStatus.CLOSED_OBLIGATIONS_MET);
    }

    @Test
    void undoingTheDiscountFeeAdjustmentThatClosedTheLoanRederivesTheNearBreachAfterTheReopening() {
        status.set(LoanStatus.CLOSED_OBLIGATIONS_MET);
        givenADiscountFeeAdjustment();

        writePlatformService.undoTransaction(LOAN_ID, TRANSACTION_ID, command);

        final InOrder order = inOrder(stateMachine, breachScheduleService);
        order.verify(stateMachine).determineAndTransition(loan, BUSINESS_DATE);
        order.verify(breachScheduleService).rederiveNearBreachIfReopened(loan, LoanStatus.CLOSED_OBLIGATIONS_MET);
    }

    @Test
    void undoingAWaiverOnALoanThatStayedActivePassesTheActiveStatusOn() {
        status.set(LoanStatus.ACTIVE);
        givenAChargeWaiver();

        writePlatformService.undoTransaction(LOAN_ID, TRANSACTION_ID, command);

        final InOrder order = inOrder(stateMachine, breachScheduleService);
        order.verify(stateMachine).determineAndTransition(loan, BUSINESS_DATE);
        order.verify(breachScheduleService).rederiveNearBreachIfReopened(loan, LoanStatus.ACTIVE);
    }

    private void givenAChargeWaiver() {
        when(transaction.getTypeOf()).thenReturn(LoanTransactionType.WAIVE_CHARGES);
        final WorkingCapitalLoanTransactionRelation relation = mock(WorkingCapitalLoanTransactionRelation.class);
        when(relation.getToCharge()).thenReturn(new WorkingCapitalLoanCharge());
        when(transaction.getLoanTransactionRelations()).thenReturn(Set.of(relation));
    }

    private void givenADiscountFeeAdjustment() {
        when(transaction.getTypeOf()).thenReturn(LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT);
        final WorkingCapitalLoanProductRelatedDetails details = new WorkingCapitalLoanProductRelatedDetails();
        details.setDiscount(BigDecimal.valueOf(50));
        when(loan.getLoanProductRelatedDetails()).thenReturn(details);
        when(balance.getTotalDiscountFeeAdjustment()).thenReturn(BigDecimal.TEN);
        when(balance.getPrincipal()).thenReturn(BigDecimal.valueOf(100));
        when(balance.getTotalPrincipalDue()).thenReturn(BigDecimal.valueOf(100));
        when(balance.getPrincipalPaid()).thenReturn(BigDecimal.ZERO);
    }
}
