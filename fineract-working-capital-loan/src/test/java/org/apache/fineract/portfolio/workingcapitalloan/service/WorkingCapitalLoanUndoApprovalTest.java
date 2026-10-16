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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanEvent;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkingCapitalLoanUndoApprovalTest {

    private static final Long LOAN_ID = 22L;
    private static final BigDecimal PROPOSED_PRINCIPAL = new BigDecimal("500");

    @Mock
    private WorkingCapitalLoanRepository loanRepository;
    @Mock
    private WorkingCapitalLoanDataValidator validator;
    @Mock
    private WorkingCapitalLoanLifecycleStateMachine stateMachine;
    @Mock
    private WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProductRelatedDetails loanProductRelatedDetails;
    @Mock
    private WorkingCapitalLoanDisbursementDetails disbursementDetails;
    @Mock
    private JsonCommand command;

    @InjectMocks
    private WorkingCapitalLoanWritePlatformServiceImpl writePlatformService;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.APPROVED);
        when(loan.getProposedPrincipal()).thenReturn(PROPOSED_PRINCIPAL);
        when(loan.getLoanProductRelatedDetails()).thenReturn(loanProductRelatedDetails);
        when(loan.getDisbursementDetails()).thenReturn(new ArrayList<>(List.of(disbursementDetails)));
        when(command.json()).thenReturn("{}");
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void undoApprovalDeletesTheApprovalTimeProjectionInsteadOfReprojecting() {
        writePlatformService.undoApplicationApproval(LOAN_ID, command);

        // A submitted loan carries no projection, so the approval-time one is dropped (not rebuilt) once the submitted
        // values are restored, and before the flush that persists what the deletion clears on the loan.
        final InOrder order = inOrder(stateMachine, loan, loanProductRelatedDetails, disbursementDetails, loanRepository,
                amortizationScheduleWriteService);
        order.verify(stateMachine).transition(any(WorkingCapitalLoanEvent.class), any(WorkingCapitalLoan.class), any(LocalDate.class));
        order.verify(loan).setApprovedPrincipal(BigDecimal.ZERO);
        order.verify(loanProductRelatedDetails).setPrincipal(PROPOSED_PRINCIPAL);
        order.verify(disbursementDetails).setExpectedAmount(PROPOSED_PRINCIPAL);
        order.verify(amortizationScheduleWriteService).deleteAmortizationScheduleOnUndoApproval(loan);
        order.verify(loanRepository).saveAndFlush(loan);
        verify(amortizationScheduleWriteService, never()).generateAndSaveAmortizationScheduleOnApproval(loan);

        verify(validator).validateUndoApproval("{}");
    }
}
