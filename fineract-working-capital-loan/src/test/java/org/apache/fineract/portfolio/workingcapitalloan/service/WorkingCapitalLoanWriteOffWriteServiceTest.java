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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanUndoWrittenOffBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanWrittenOffBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanWriteOffDomainService;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAccountingRuleType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Verifies what a write-off and its undo must do beyond recording the transaction.
 * <p>
 * The delinquency schedule is reprocessed and the balance-changed / status-changed events are published alongside the
 * transaction event. These matter because {@code CLOSED_WRITTEN_OFF} is outside the COB scope: without an explicit
 * reprocess here, a loan that was delinquent when it got written off would stay tagged delinquent forever, and the undo
 * would never re-derive the classification from the restored balance.
 * </p>
 * <p>
 * A loan without a balance row is rejected rather than written off for a zero amount, matching charge-off.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanWriteOffWriteServiceTest {

    private static final Long LOAN_ID = 11L;

    @Mock
    private WorkingCapitalLoanRepository loanRepository;
    @Mock
    private WorkingCapitalLoanDataValidator validator;
    @Mock
    private WorkingCapitalLoanWriteOffDomainService writeOffDomainService;
    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;
    @Mock
    private WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    @Mock
    private WorkingCapitalLoanChargeRepository chargeRepository;
    @Mock
    private WorkingCapitalLoanNoteRepository noteRepository;
    @Mock
    private CodeValueRepositoryWrapper codeValueRepository;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private WorkingCapitalLoanAccountingProcessor accountingProcessor;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleService delinquencyRangeScheduleService;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProduct loanProduct;
    @Mock
    private JsonCommand command;

    @InjectMocks
    private WorkingCapitalLoanWriteOffWriteServiceImpl writeOffService;

    private LocalDate businessDate;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        businessDate = LocalDate.now(ZoneId.systemDefault());
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(java.util.Map.of(BusinessDateType.BUSINESS_DATE, businessDate)));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.getBalance()).thenReturn(WorkingCapitalLoanBalance.createFor(loan));
        when(loan.getLoanProduct()).thenReturn(loanProduct);
        // Accounting off: this test is about the events and the delinquency reprocess, not the ledger.
        when(loanProduct.getAccountingRule()).thenReturn(WorkingCapitalAccountingRuleType.NONE);
        when(command.localDateValueOfParameterNamed(any())).thenReturn(businessDate);
    }

    @AfterEach
    void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    void writeOffReprocessesDelinquencyAndPublishesBalanceAndStatusEvents() {
        // The domain service transitions the loan, so the status read before and after the call differs.
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE, LoanStatus.CLOSED_WRITTEN_OFF);

        writeOffService.writeOff(LOAN_ID, command);

        verify(delinquencyRangeScheduleService).reprocessDelinquencySchedule(loan);
        final List<BusinessEvent<?>> events = publishedEvents(3);
        assertThat(events.get(0)).isInstanceOf(WorkingCapitalLoanWrittenOffBusinessEvent.class);
        assertThat(events.get(1)).isInstanceOf(WorkingCapitalLoanBalanceChangedBusinessEvent.class);
        assertThat(events.get(2)).isInstanceOf(WorkingCapitalLoanStatusChangedBusinessEvent.class);
    }

    @Test
    void undoWriteOffReprocessesDelinquencyAndPublishesBalanceAndStatusEvents() {
        when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_WRITTEN_OFF, LoanStatus.ACTIVE);
        when(transactionRepository.findActiveByTypeOrderByIdDesc(LOAN_ID, LoanTransactionType.WRITEOFF))
                .thenReturn(List.of(WorkingCapitalLoanTransaction.writeOff(loan, BigDecimal.TEN, businessDate, null)));

        writeOffService.undoWriteOff(LOAN_ID, command);

        verify(delinquencyRangeScheduleService).reprocessDelinquencySchedule(loan);
        final List<BusinessEvent<?>> events = publishedEvents(3);
        assertThat(events.get(0)).isInstanceOf(WorkingCapitalLoanUndoWrittenOffBusinessEvent.class);
        assertThat(events.get(1)).isInstanceOf(WorkingCapitalLoanBalanceChangedBusinessEvent.class);
        assertThat(events.get(2)).isInstanceOf(WorkingCapitalLoanStatusChangedBusinessEvent.class);
    }

    @Test
    void undoWriteOffToleratesARequestWithNoBody() {
        when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_WRITTEN_OFF, LoanStatus.ACTIVE);
        when(transactionRepository.findActiveByTypeOrderByIdDesc(LOAN_ID, LoanTransactionType.WRITEOFF))
                .thenReturn(List.of(WorkingCapitalLoanTransaction.writeOff(loan, BigDecimal.TEN, businessDate, null)));
        // A real command built from an empty request body: parsedCommand is null, which validateUndoWriteOff permits.
        // The service must not dereference the parsed JSON on this path.
        final JsonCommand bodilessCommand = JsonCommand.from(null, null, new FromJsonHelper(), "WORKINGCAPITALLOAN", LOAN_ID, null, null,
                null, LOAN_ID, null, null, null, null, null, null, null, null);

        final CommandProcessingResult result = writeOffService.undoWriteOff(LOAN_ID, bodilessCommand);

        assertThat(result.getLoanId()).isEqualTo(LOAN_ID);
    }

    @Test
    void writeOffIsRejectedWhenTheLoanHasNoBalanceRow() {
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        when(loan.getBalance()).thenReturn(null);

        final GeneralPlatformDomainRuleException ex = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> writeOffService.writeOff(LOAN_ID, command));

        assertThat(ex.getGlobalisationMessageCode()).isEqualTo("error.msg.wc.loan.balance.not.found");
        // Nothing was closed, posted or published on the way out.
        verify(writeOffDomainService, never()).writeOff(any(), any(), any(), any(), any());
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void undoWriteOffIsRejectedWhenTheLoanHasNoBalanceRow() {
        when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_WRITTEN_OFF);
        when(loan.getBalance()).thenReturn(null);

        final GeneralPlatformDomainRuleException ex = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> writeOffService.undoWriteOff(LOAN_ID, command));

        assertThat(ex.getGlobalisationMessageCode()).isEqualTo("error.msg.wc.loan.balance.not.found");
        verify(writeOffDomainService, never()).undoWriteOff(any(), any());
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    private List<BusinessEvent<?>> publishedEvents(final int expectedCount) {
        final ArgumentCaptor<BusinessEvent<?>> captor = ArgumentCaptor.forClass(BusinessEvent.class);
        verify(businessEventNotifierService, times(expectedCount)).notifyPostBusinessEvent(captor.capture());
        return captor.getAllValues();
    }
}
