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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAccountingRuleType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * A charge due at disbursement is settled and recognized when the loan is disbursed, so no accrual sweep may pick it up
 * - the closure sweep in particular, whose idempotency guard only sees ACCRUAL transactions and would otherwise
 * recognize the income a second time.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanChargeAccrualServiceDisbursementChargeTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 3, 1);

    @Mock
    private GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    @Mock
    private WorkingCapitalLoanChargeRepository chargeRepository;
    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;
    @Mock
    private WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    @Mock
    private WorkingCapitalLoanTransactionRelationRepository relationRepository;
    @Mock
    private WorkingCapitalLoanAccountingProcessor accountingProcessor;
    @Mock
    private WorkingCapitalLoanTransactionFinder transactionFinder;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProduct product;

    private WorkingCapitalLoanChargeAccrualService service;
    private MockedStatic<DateUtils> dateUtils;

    @BeforeEach
    void setUp() {
        // The transaction factory stamps submittedOnDate from the business date, which lives in the tenant ThreadLocal.
        dateUtils = mockStatic(DateUtils.class);
        dateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(BUSINESS_DATE);
        service = new WorkingCapitalLoanChargeAccrualService(globalConfigurationRepository, chargeRepository, transactionRepository,
                allocationRepository, relationRepository, accountingProcessor, transactionFinder, businessEventNotifierService);
        when(loan.getId()).thenReturn(1L);
        when(loan.getLoanProduct()).thenReturn(product);
        when(product.getAccountingRule()).thenReturn(WorkingCapitalAccountingRuleType.ACC_DEF_REV_AM);
        final GlobalConfigurationProperty dueDateMode = mock(GlobalConfigurationProperty.class);
        when(dueDateMode.getStringValue()).thenReturn("due-date");
        when(globalConfigurationRepository.findOneByNameWithNotFoundDetection(anyString())).thenReturn(dueDateMode);
        when(relationRepository.findAllByToChargeAndFromTransactionReversedAndFromTransactionTransactionType(any(), anyBoolean(), any()))
                .thenReturn(List.of());
        when(transactionRepository.saveAndFlush(any(WorkingCapitalLoanTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void releaseStaticMock() {
        dateUtils.close();
    }

    private static WorkingCapitalLoanCharge charge(final ChargeTimeType timeType, final String amount, final LocalDate dueDate) {
        final WorkingCapitalLoanCharge charge = new WorkingCapitalLoanCharge();
        charge.setChargeTimeType(timeType);
        charge.setAmount(new BigDecimal(amount));
        charge.setDueDate(dueDate);
        charge.setSubmittedOnDate(BUSINESS_DATE.minusDays(10));
        charge.setActive(true);
        return charge;
    }

    @Test
    @DisplayName("The closure sweep accrues the specified-due-date charge and skips the disbursement charge")
    void closureSweepSkipsDisbursementCharge() {
        when(chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(1L)).thenReturn(List.of(
                charge(ChargeTimeType.DISBURSEMENT, "100", BUSINESS_DATE.minusDays(10)),
                charge(ChargeTimeType.SPECIFIED_DUE_DATE, "50", BUSINESS_DATE.minusDays(1))));

        service.processClosureAccruals(loan, BUSINESS_DATE);

        verify(transactionRepository, times(1)).saveAndFlush(any(WorkingCapitalLoanTransaction.class));
    }

    @Test
    @DisplayName("The COB sweep in due-date mode skips a disbursement charge even when its due date is in the past")
    void cobSweepSkipsDisbursementCharge() {
        when(chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(1L))
                .thenReturn(List.of(charge(ChargeTimeType.DISBURSEMENT, "100", BUSINESS_DATE.minusDays(10))));

        service.processChargeAccrualsOnCOB(loan, BUSINESS_DATE);

        verify(transactionRepository, never()).saveAndFlush(any(WorkingCapitalLoanTransaction.class));
    }

    @Test
    @DisplayName("Adding a disbursement charge never posts a real-time accrual")
    void addingDisbursementChargeNeverAccrues() {
        final GlobalConfigurationProperty realTime = mock(GlobalConfigurationProperty.class);
        when(realTime.getStringValue()).thenReturn("real-time");
        when(globalConfigurationRepository.findOneByNameWithNotFoundDetection(anyString())).thenReturn(realTime);

        service.processOnChargeAdded(loan, charge(ChargeTimeType.DISBURSEMENT, "100", null));

        verify(transactionRepository, never()).saveAndFlush(any(WorkingCapitalLoanTransaction.class));
    }
}
