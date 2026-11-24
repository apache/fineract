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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargePaidByRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
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
 * A repayment-like transaction updates the delinquency range schedule without classifying it, then changes the loan
 * status, and hands the classification it owes back to its caller, which classifies once the loan is saved.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanTransactionProcessorDelinquencyTest {

    private static final Long LOAN_ID = 61L;
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 6, 1);
    private static final BigDecimal AMOUNT = new BigDecimal("40");

    @Mock
    private WorkingCapitalLoanChargeRepository chargeRepository;
    @Mock
    private WorkingCapitalLoanBalanceRepository balanceRepository;
    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;
    @Mock
    private WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    @Mock
    private WorkingCapitalLoanChargePaidByRepository chargePaidByRepository;
    @Mock
    private WorkingCapitalLoanTransactionAllocator transactionAllocator;
    @Mock
    private WorkingCapitalLoanTransactionReprocessingService transactionReprocessingService;
    @Mock
    private WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;
    @Mock
    private WorkingCapitalLoanBreachScheduleService breachScheduleService;
    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleService delinquencyRangeScheduleService;
    @Mock
    private WorkingCapitalLoanLifecycleStateMachine stateMachine;
    @Mock
    private WorkingCapitalLoanDiscountFeeAmortizationService discountFeeAmortizationService;
    @Mock
    private WorkingCapitalLoanChargeAccrualService chargeAccrualService;
    @Mock
    private WorkingCapitalLoanAccountingProcessor accountingProcessor;
    @Mock
    private WorkingCapitalLoanTransactionFinder transactionFinder;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanTransaction transaction;
    @Mock
    private WorkingCapitalLoanBalance balance;
    @InjectMocks
    private WorkingCapitalLoanTransactionProcessor transactionProcessor;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));

        when(loan.getId()).thenReturn(LOAN_ID);
        when(transaction.getWcLoan()).thenReturn(loan);
        when(transaction.getTransactionDate()).thenReturn(BUSINESS_DATE);
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));
        when(balance.getOverpaymentAmount()).thenReturn(BigDecimal.ZERO);
        when(balance.getPrincipalOutstanding()).thenReturn(new BigDecimal("100"));
        final WorkingCapitalLoanAllocationPlan plan = new WorkingCapitalLoanAllocationPlan(AMOUNT, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, List.of());
        when(transactionAllocator.allocate(eq(loan), eq(balance), any(), eq(transaction)))
                .thenReturn(new WorkingCapitalLoanTransactionAllocator.Result(mock(WorkingCapitalLoanTransactionAllocation.class),
                        List.of(), plan, true, false, BigDecimal.ZERO));
        when(delinquencyRangeScheduleService.applyRepaymentToPeriods(loan, BUSINESS_DATE, AMOUNT))
                .thenReturn(WorkingCapitalLoanDelinquencyClassificationMode.INSTANT);
        when(delinquencyRangeScheduleService.reprocessSchedulePeriods(loan))
                .thenReturn(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void lastRepaymentIsAppliedInPlaceAndOwesAnInstantClassification() {
        final WorkingCapitalLoanDelinquencyClassificationMode classification = transactionProcessor.processRepaymentLikeTransaction(loan,
                transaction, BUSINESS_DATE, AMOUNT);

        assertThat(classification).isEqualTo(WorkingCapitalLoanDelinquencyClassificationMode.INSTANT);
        final InOrder order = inOrder(delinquencyRangeScheduleService, stateMachine);
        order.verify(delinquencyRangeScheduleService).applyRepaymentToPeriods(loan, BUSINESS_DATE, AMOUNT);
        order.verify(stateMachine).determineAndTransition(loan, BUSINESS_DATE);
        verify(delinquencyRangeScheduleService, never()).reprocessSchedulePeriods(any());
    }

    @Test
    void backdatedRepaymentRebuildsTheScheduleAndOwesAnUnconditionalClassification() {
        when(transactionRepository.existsLaterTransaction(anyLong(), any(), any())).thenReturn(true);

        final WorkingCapitalLoanDelinquencyClassificationMode classification = transactionProcessor.processRepaymentLikeTransaction(loan,
                transaction, BUSINESS_DATE, AMOUNT);

        assertThat(classification).isEqualTo(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        final InOrder order = inOrder(delinquencyRangeScheduleService, stateMachine);
        order.verify(delinquencyRangeScheduleService).reprocessSchedulePeriods(loan);
        order.verify(stateMachine).determineAndTransition(loan, BUSINESS_DATE);
        verify(delinquencyRangeScheduleService, never()).applyRepaymentToPeriods(any(), any(), any());
    }
}
