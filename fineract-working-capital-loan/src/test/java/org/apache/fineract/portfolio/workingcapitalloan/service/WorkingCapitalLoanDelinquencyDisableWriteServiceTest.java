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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanDelinquencyActionParseAndValidator;
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
 * Verifies that the disable and enable delinquency actions - both routed through the single create command like the
 * breach actions - trigger the expected side effects: a disable lifts the active classification, an enable closes the
 * disable, extends the disabled window like a pause and recomputes delinquency.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDelinquencyDisableWriteServiceTest {

    private static final Long LOAN_ID = 1L;

    @Mock
    private WorkingCapitalLoanRepository loanRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyActionRepository actionRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyActionParseAndValidator validator;
    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleService rangeScheduleService;
    @Mock
    private WorkingCapitalLoanDelinquencyClassificationService classificationService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private JsonCommand command;

    @InjectMocks
    private WorkingCapitalLoanDelinquencyActionWriteServiceImpl writeService;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        final HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        final LocalDate today = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, today);
        businessDates.put(BusinessDateType.COB_DATE, today.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        when(loan.getId()).thenReturn(LOAN_ID);
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        when(actionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID)).thenReturn(List.of());
        when(actionRepository.saveAndFlush(any(WorkingCapitalLoanDelinquencyAction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void disableLiftsActiveClassificationAsOfDisableDate() {
        final WorkingCapitalLoanDelinquencyAction action = disableAction(DateUtils.getBusinessLocalDate(), null);
        when(validator.validateAndParse(any(), any(), anyList())).thenReturn(action);

        writeService.createDelinquencyAction(LOAN_ID, command);

        verify(classificationService).liftDelinquencyClassification(loan, action.getStartDate());
        verify(classificationService, never()).classifyDelinquency(any(), any());
    }

    @Test
    void enablePersistsEnableRowClosesDisableAndReprocesses() {
        final LocalDate today = DateUtils.getBusinessLocalDate();
        // The enable is persisted as its own ENABLE row; the open disable (started 10 days ago) is closed by the
        // caller.
        final WorkingCapitalLoanDelinquencyAction enableRow = enableAction(today);
        final WorkingCapitalLoanDelinquencyAction activeDisable = disableAction(today.minusDays(10), null);
        when(validator.validateAndParse(any(), any(), anyList())).thenReturn(enableRow);
        when(validator.findActiveDisable(anyList())).thenReturn(activeDisable);
        when(rangeScheduleService.reprocessSchedulePeriods(loan)).thenReturn(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);

        writeService.createDelinquencyAction(LOAN_ID, command);

        // Mirror breach: the disable is closed at the day before the enable date, not the enable date itself.
        assertThat(activeDisable.getEndDate()).isEqualTo(today.minusDays(1));
        verify(actionRepository).saveAndFlush(enableRow);
        // Mirror breach: enable closes the disable, reprocesses the schedule (which re-evaluates it) and reclassifies.
        // It must NOT shift periods as a pause.
        final InOrder order = inOrder(rangeScheduleService, classificationService);
        order.verify(rangeScheduleService).reprocessSchedulePeriods(loan);
        order.verify(classificationService).classifyDelinquencyOnFinalState(loan, WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        verify(rangeScheduleService, never()).extendPeriodsForPause(any(), any(), any());
    }

    @Test
    void resumeClassifiesOnceAtTheEndWithTheModeTheResumeReturns() {
        final LocalDate today = DateUtils.getBusinessLocalDate();
        final WorkingCapitalLoanDelinquencyAction resume = action(DelinquencyAction.RESUME, today, null);
        final WorkingCapitalLoanDelinquencyAction activePause = action(DelinquencyAction.PAUSE, today.minusDays(5), today.plusDays(5));
        when(validator.validateAndParse(any(), any(), anyList())).thenReturn(resume);
        when(validator.findActivePauseForResume(anyList(), any())).thenReturn(activePause);
        when(rangeScheduleService.resumeActivePause(loan, activePause, resume))
                .thenReturn(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);

        writeService.createDelinquencyAction(LOAN_ID, command);

        final InOrder order = inOrder(rangeScheduleService, classificationService, businessEventNotifierService);
        order.verify(rangeScheduleService).resumeActivePause(loan, activePause, resume);
        order.verify(classificationService).classifyDelinquencyOnFinalState(loan, WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        order.verify(businessEventNotifierService).notifyPostBusinessEvent(any());
        verify(classificationService).classifyDelinquencyOnFinalState(any(), any());
        verify(classificationService, never()).classifyDelinquency(any(), any());
    }

    private WorkingCapitalLoanDelinquencyAction disableAction(final LocalDate startDate, final LocalDate endDate) {
        return action(DelinquencyAction.DISABLE, startDate, endDate);
    }

    private WorkingCapitalLoanDelinquencyAction enableAction(final LocalDate startDate) {
        return action(DelinquencyAction.ENABLE, startDate, null);
    }

    private WorkingCapitalLoanDelinquencyAction action(final DelinquencyAction type, final LocalDate startDate, final LocalDate endDate) {
        final WorkingCapitalLoanDelinquencyAction action = new WorkingCapitalLoanDelinquencyAction();
        action.setAction(type);
        action.setStartDate(startDate);
        action.setEndDate(endDate);
        return action;
    }

}
