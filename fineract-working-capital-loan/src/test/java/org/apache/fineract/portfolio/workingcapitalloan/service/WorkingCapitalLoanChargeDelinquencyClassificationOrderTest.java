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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargeWaiverDomainService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanEvent;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargePaidByRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanChargeConstants;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanChargeDataValidator;
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
 * The charge transactions that can change a Working Capital loan's status classify delinquency once, after the status
 * transition and the loan save and before the business events, so {@code classifyDelinquency} sees the new status.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanChargeDelinquencyClassificationOrderTest {

    private static final Long LOAN_ID = 51L;
    private static final Long LOAN_CHARGE_ID = 6L;
    private static final Long CHARGE_DEFINITION_ID = 3L;
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 6, 1);

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private WorkingCapitalLoanChargeDataValidator loanChargeDataValidator;
    @Mock
    private WorkingCapitalLoanRepository workingCapitalLoanRepository;
    @Mock
    private ChargeRepositoryWrapper chargeRepository;
    @Mock
    private WorkingCapitalLoanChargeRepository loanChargeRepository;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private WorkingCapitalLoanBalanceRepository balanceRepository;
    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;
    @Mock
    private WorkingCapitalLoanTransactionRelationRepository relationRepository;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailService;
    @Mock
    private WorkingCapitalLoanNoteRepository noteRepository;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private WorkingCapitalLoanTransactionProcessor transactionProcessor;
    @Mock
    private WorkingCapitalLoanChargePaymentHandler chargePaymentHandler;
    @Mock
    private WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    @Mock
    private WorkingCapitalLoanChargePaidByRepository chargePaidByRepository;
    @Mock
    private WorkingCapitalLoanBalanceUpdater balanceUpdater;
    @Mock
    private WorkingCapitalLoanLifecycleStateMachine stateMachine;
    @Mock
    private WorkingCapitalLoanAllocationRequestFactory allocationRequestFactory;
    @Mock
    private WorkingCapitalLoanPaymentAllocationProcessor allocationProcessor;
    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleService delinquencyRangeScheduleService;
    @Mock
    private WorkingCapitalLoanDelinquencyClassificationService delinquencyClassificationService;
    @Mock
    private WorkingCapitalLoanBreachScheduleService breachScheduleService;
    @Mock
    private ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;
    @Mock
    private WorkingCapitalLoanChargeAccrualService chargeAccrualService;
    @Mock
    private WorkingCapitalLoanChargeWaiverDomainService chargeWaiverDomainService;
    @Mock
    private WorkingCapitalLoanAccountingProcessor accountingProcessor;
    @Mock
    private WorkingCapitalLoanTransactionFinder transactionFinder;
    @Mock
    private WorkingCapitalLoanTransactionReprocessingService transactionReprocessingService;
    @Mock
    private WorkingCapitalLoanAdjustTransactionEventPublisher adjustTransactionEventPublisher;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanCharge loanCharge;
    @Mock
    private WorkingCapitalLoanBalance balance;
    @Mock
    private JsonCommand command;
    @Mock
    private Office office;
    @InjectMocks
    private WorkingCapitalLoanChargeWritePlatformServiceImpl chargeWritePlatformService;

    private final AtomicReference<LoanStatus> status = new AtomicReference<>();
    private final List<LoanStatus> statusesSeenByClassification = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());

        when(workingCapitalLoanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        when(loan.getOffice()).thenReturn(office);
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.getLoanStatus()).thenAnswer(invocation -> status.get());
        when(loan.isOpen()).thenAnswer(invocation -> status.get() == LoanStatus.ACTIVE);
        when(loan.isClosedObligationsMet()).thenAnswer(invocation -> status.get() == LoanStatus.CLOSED_OBLIGATIONS_MET);
        when(loan.isOverpaid()).thenAnswer(invocation -> status.get() == LoanStatus.OVERPAID);
        when(externalIdFactory.createFromCommand(any(), any())).thenReturn(ExternalId.empty());
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));

        when(loanChargeRepository.findById(LOAN_CHARGE_ID)).thenReturn(Optional.of(loanCharge));
        when(loanCharge.getLoan()).thenReturn(loan);
        when(loanCharge.isActive()).thenReturn(true);
        when(loanCharge.getAmount()).thenReturn(BigDecimal.TEN);
        when(loanCharge.getAmountOutstanding()).thenReturn(BigDecimal.TEN);
        when(loanCharge.getAmountWaived()).thenReturn(BigDecimal.ZERO);
        when(loanCharge.getAmountPaid()).thenReturn(BigDecimal.ZERO);
        when(chargeAccrualService.getAccruedAmount(loanCharge)).thenReturn(BigDecimal.ZERO);

        doAnswer(invocation -> statusesSeenByClassification.add(status.get())).when(delinquencyClassificationService)
                .classifyDelinquencyOnFinalState(eq(loan), any());
    }

    @AfterEach
    void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    void chargeAdjustmentThatClosesTheLoanIsClassifiedAfterTheClosingAndTheSave() {
        status.set(LoanStatus.ACTIVE);
        when(command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanChargeConstants.amountParamName)).thenReturn(BigDecimal.TEN);
        when(transactionProcessor.processRepaymentLikeTransaction(eq(loan), any(), eq(BUSINESS_DATE), eq(BigDecimal.TEN)))
                .thenAnswer(invocation -> {
                    status.set(LoanStatus.CLOSED_OBLIGATIONS_MET);
                    return WorkingCapitalLoanDelinquencyClassificationMode.INSTANT;
                });

        chargeWritePlatformService.adjustmentForLoanCharge(LOAN_ID, LOAN_CHARGE_ID, command);

        final InOrder order = inOrder(transactionProcessor, workingCapitalLoanRepository, delinquencyClassificationService,
                businessEventNotifierService);
        order.verify(transactionProcessor).processRepaymentLikeTransaction(eq(loan), any(), eq(BUSINESS_DATE), eq(BigDecimal.TEN));
        order.verify(workingCapitalLoanRepository).saveAndFlush(loan);
        order.verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(loan,
                WorkingCapitalLoanDelinquencyClassificationMode.INSTANT);
        order.verify(businessEventNotifierService, atLeastOnce()).notifyPostBusinessEvent(any());
        assertThat(statusesSeenByClassification).containsExactly(LoanStatus.CLOSED_OBLIGATIONS_MET);
    }

    @Test
    void chargeWaiverThatClosesTheLoanIsClassifiedAfterTheClosingAndTheSave() {
        status.set(LoanStatus.ACTIVE);
        givenTransitionTo(LoanStatus.CLOSED_OBLIGATIONS_MET);

        chargeWritePlatformService.waiveLoanCharge(LOAN_ID, LOAN_CHARGE_ID, command);

        // Without a replay the delinquency schedule is untouched, but the closed loan leaves the COB scope, so it is
        // classified now, after the closing and the save.
        assertClassifiedOnceAfterTransitionAndSave(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        verify(delinquencyRangeScheduleService, never()).reprocessSchedulePeriods(any());
    }

    @Test
    void chargeWaiverThatLeavesTheLoanActiveOwesNoClassification() {
        status.set(LoanStatus.ACTIVE);

        chargeWritePlatformService.waiveLoanCharge(LOAN_ID, LOAN_CHARGE_ID, command);

        verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(loan,
                WorkingCapitalLoanDelinquencyClassificationMode.NONE);
        verify(delinquencyRangeScheduleService, never()).reprocessSchedulePeriods(any());
    }

    @Test
    void chargeWaiverBeforeAChargeOffIsClassifiedAfterTheReplayTheClosingAndTheSave() {
        status.set(LoanStatus.ACTIVE);
        givenTransitionTo(LoanStatus.CLOSED_OBLIGATIONS_MET);
        when(transactionFinder.isBeforeActiveChargeOff(eq(loan), any())).thenReturn(true);
        when(delinquencyRangeScheduleService.reprocessSchedulePeriods(loan))
                .thenReturn(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);

        chargeWritePlatformService.waiveLoanCharge(LOAN_ID, LOAN_CHARGE_ID, command);

        verify(delinquencyRangeScheduleService).reprocessSchedulePeriods(loan);
        assertClassifiedOnceAfterTransitionAndSave(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
    }

    @Test
    void chargeThatReopensAClosedLoanIsClassifiedAfterTheReopeningAndTheSave() {
        status.set(LoanStatus.CLOSED_OBLIGATIONS_MET);
        givenANewCharge();
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.ZERO);
        when(balance.getTotalOutstanding()).thenReturn(BigDecimal.TEN);
        doAnswer(invocation -> {
            status.set(LoanStatus.ACTIVE);
            return null;
        }).when(stateMachine).transition(eq(WorkingCapitalLoanEvent.LOAN_REOPENED), eq(loan), any());

        chargeWritePlatformService.createLoanCharge(LOAN_ID, command);

        final InOrder order = inOrder(stateMachine, workingCapitalLoanRepository, delinquencyClassificationService,
                businessEventNotifierService);
        order.verify(stateMachine).transition(WorkingCapitalLoanEvent.LOAN_REOPENED, loan, BUSINESS_DATE);
        order.verify(workingCapitalLoanRepository).saveAndFlush(loan);
        order.verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(loan,
                WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        order.verify(businessEventNotifierService, atLeastOnce()).notifyPostBusinessEvent(any());
        assertThat(statusesSeenByClassification).containsExactly(LoanStatus.ACTIVE);
    }

    @Test
    void chargeThatLeavesTheStatusAloneIsNotClassified() {
        status.set(LoanStatus.ACTIVE);
        givenANewCharge();
        // No due date: the charge cannot fall after the maturity, so the loan's lifecycle is not touched.
        when(command.dateValueOfParameterNamed("dueDate")).thenReturn(null);

        chargeWritePlatformService.createLoanCharge(LOAN_ID, command);

        verify(delinquencyClassificationService, never()).classifyDelinquencyOnFinalState(any(), any());
    }

    private void assertClassifiedOnceAfterTransitionAndSave(final WorkingCapitalLoanDelinquencyClassificationMode expectedMode) {
        final InOrder order = inOrder(stateMachine, workingCapitalLoanRepository, delinquencyClassificationService,
                businessEventNotifierService);
        order.verify(stateMachine).determineAndTransition(loan, BUSINESS_DATE);
        order.verify(workingCapitalLoanRepository).saveAndFlush(loan);
        order.verify(delinquencyClassificationService).classifyDelinquencyOnFinalState(loan, expectedMode);
        order.verify(businessEventNotifierService, atLeastOnce()).notifyPostBusinessEvent(any());
        assertThat(statusesSeenByClassification).containsExactly(LoanStatus.CLOSED_OBLIGATIONS_MET);
    }

    private void givenTransitionTo(final LoanStatus newStatus) {
        doAnswer(invocation -> {
            status.set(newStatus);
            return null;
        }).when(stateMachine).determineAndTransition(eq(loan), any());
    }

    /** A fee due on the business date, which is after the loan's scheduled maturity. */
    private void givenANewCharge() {
        when(command.bigDecimalValueOfParameterNamed("amount")).thenReturn(BigDecimal.TEN);
        when(command.dateValueOfParameterNamed("dueDate")).thenReturn(BUSINESS_DATE);
        when(command.longValueOfParameterNamed("chargeId")).thenReturn(CHARGE_DEFINITION_ID);
        final Charge chargeDefinition = mock(Charge.class);
        when(chargeDefinition.getChargeTimeType()).thenReturn(ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        when(chargeDefinition.getChargeCalculation()).thenReturn(1);
        when(chargeDefinition.getChargePaymentMode()).thenReturn(0);
        when(chargeRepository.findOneWithNotFoundDetection(CHARGE_DEFINITION_ID)).thenReturn(chargeDefinition);
        when(loanChargeRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(balance.getFee()).thenReturn(BigDecimal.ZERO);
    }
}
