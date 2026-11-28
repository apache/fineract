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
package org.apache.fineract.portfolio.loanaccount.service.recalculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.jobs.exception.LoanIdsHardLockedException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualsProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanScheduleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanRecalculationServiceTest {

    private static final Long LOAN_ID = 42L;

    @Mock
    private LoanAssembler loanAssembler;
    @Mock
    private LoanAccountLockService loanAccountLockService;
    @Mock
    private LoanScheduleService loanScheduleService;
    @Mock
    private LoanAccrualsProcessingService loanAccrualsProcessingService;
    @Mock
    private LoanLifecycleStateMachine loanLifecycleStateMachine;
    @Mock
    private LoanAccountDomainService loanAccountDomainService;
    @Mock
    private LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private JsonCommand command;
    @Mock
    private Loan loan;

    @InjectMocks
    private LoanRecalculationService service;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 7, 29))));
        when(command.getLoanId()).thenReturn(LOAN_ID);
        when(loanAssembler.assembleFromInUserScope(LOAN_ID)).thenReturn(loan);
        when(loanRepositoryWrapper.saveAndFlush(loan)).thenReturn(loan);
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.isProgressiveSchedule()).thenReturn(true);
        when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void regeneratesScheduleAndReprocessesBeforeBringingDerivedStateInLine() {
        CommandProcessingResult result = service.recalculate(command);

        assertEquals(LOAN_ID, result.getLoanId());
        InOrder order = inOrder(loanScheduleService, loanAccrualsProcessingService, loanRepositoryWrapper, loanLifecycleStateMachine,
                loanAccountDomainService, businessEventNotifierService);
        order.verify(loanScheduleService).regenerateScheduleWithReprocessingTransactions(loan);
        order.verify(loanAccrualsProcessingService).reprocessExistingAccruals(loan, true);
        order.verify(loanRepositoryWrapper).saveAndFlush(loan);
        order.verify(loanLifecycleStateMachine).determineAndTransition(any(), any());
        order.verify(loanAccountDomainService).setLoanDelinquencyTag(any(), any());
        order.verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanBalanceChangedBusinessEvent.class));
    }

    @Test
    void rejectsLoanThatCloseOfBusinessHoldsLockOn() {
        when(loanAccountLockService.isAnyLoanHardLocked(List.of(LOAN_ID))).thenReturn(true);
        when(loanAccountLockService.isAnyLockOverrulable(List.of(LOAN_ID))).thenReturn(false);

        LoanIdsHardLockedException e = assertThrows(LoanIdsHardLockedException.class, () -> service.recalculate(command));

        assertEquals(LOAN_ID, e.getLoanIdFromRequest());
        verifyNoInteractions(loanAssembler, loanScheduleService);
    }

    @Test
    void acceptsLoanWithLockLeftBehindByFailedCloseOfBusiness() {
        when(loanAccountLockService.isAnyLoanHardLocked(List.of(LOAN_ID))).thenReturn(true);
        when(loanAccountLockService.isAnyLockOverrulable(List.of(LOAN_ID))).thenReturn(true);

        service.recalculate(command);

        verify(loanScheduleService).regenerateScheduleWithReprocessingTransactions(loan);
    }

    @Test
    void rejectsLoanWithCumulativeSchedule() {
        when(loan.isProgressiveSchedule()).thenReturn(false);

        GeneralPlatformDomainRuleException e = assertThrows(GeneralPlatformDomainRuleException.class, () -> service.recalculate(command));

        assertEquals("error.msg.loan.recalculation.not.progressive", e.getGlobalisationMessageCode());
        verify(loanScheduleService, never()).regenerateScheduleWithReprocessingTransactions(any());
    }

    @ParameterizedTest
    @EnumSource(value = LoanStatus.class, names = { "ACTIVE", "OVERPAID", "CLOSED_OBLIGATIONS_MET",
            "CLOSED_WRITTEN_OFF" }, mode = EnumSource.Mode.EXCLUDE)
    void rejectsLoanInStatusWithoutTransactionsToReplay(LoanStatus status) {
        when(loan.getStatus()).thenReturn(status);

        GeneralPlatformDomainRuleException e = assertThrows(GeneralPlatformDomainRuleException.class, () -> service.recalculate(command));

        assertEquals("error.msg.loan.recalculation.invalid.status", e.getGlobalisationMessageCode());
        verify(loanScheduleService, never()).regenerateScheduleWithReprocessingTransactions(any());
    }
}
