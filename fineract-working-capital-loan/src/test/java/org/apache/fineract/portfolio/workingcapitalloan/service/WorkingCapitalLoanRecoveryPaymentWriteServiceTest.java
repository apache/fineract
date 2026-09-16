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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanRecoveryPaymentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
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
 * Verifies what a recovery payment and its undo must do beyond recording the transaction: the running
 * {@code totalRecovered} moves by exactly the transaction amount, journal entries are posted only when the product's
 * accounting rule asks for them, and the business events are published - the undo alongside the generic
 * adjust-transaction reversal event, so a consumer reconciling reversals generically does not miss recovery ones.
 * <p>
 * A loan without a balance row is rejected on both paths, and an undo whose amount exceeds the running total fails
 * loudly rather than clamping the figure at zero - both signal an account that became inconsistent.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanRecoveryPaymentWriteServiceTest {

    private static final Long LOAN_ID = 12L;
    private static final BigDecimal AMOUNT = new BigDecimal("40");

    @Mock
    private WorkingCapitalLoanRepository loanRepository;
    @Mock
    private WorkingCapitalLoanDataValidator validator;
    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;
    @Mock
    private WorkingCapitalLoanBalanceRepository balanceRepository;
    @Mock
    private WorkingCapitalLoanNoteRepository noteRepository;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailService;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private FromJsonHelper fromApiJsonHelper;
    @Mock
    private WorkingCapitalLoanAccountingProcessor accountingProcessor;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private WorkingCapitalLoanAdjustTransactionEventPublisher adjustTransactionEventPublisher;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProduct loanProduct;
    @Mock
    private JsonCommand command;

    @InjectMocks
    private WorkingCapitalLoanRecoveryPaymentWriteServiceImpl recoveryPaymentService;

    private LocalDate businessDate;
    private WorkingCapitalLoanBalance balance;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        businessDate = LocalDate.now(ZoneId.systemDefault());
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(java.util.Map.of(BusinessDateType.BUSINESS_DATE, businessDate)));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());

        balance = WorkingCapitalLoanBalance.createFor(loan);
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.getBalance()).thenReturn(balance);
        when(loan.getLoanProduct()).thenReturn(loanProduct);
        // Accounting off by default: most tests are about the balance mutation and the events, not the ledger.
        when(loanProduct.getAccountingRule()).thenReturn(WorkingCapitalAccountingRuleType.NONE);
        when(command.localDateValueOfParameterNamed(any())).thenReturn(businessDate);
        when(fromApiJsonHelper.extractBigDecimalNamed(eq(WorkingCapitalLoanConstants.transactionAmountParamName), any(), anySet()))
                .thenReturn(AMOUNT);
    }

    @AfterEach
    void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    void recoveryPaymentIncrementsTotalRecoveredAndPublishesTransactionEvent() {
        final CommandProcessingResult result = recoveryPaymentService.recoveryPayment(LOAN_ID, command);

        assertThat(balance.getTotalRecovered()).isEqualByComparingTo(AMOUNT);
        verify(balanceRepository).saveAndFlush(balance);
        assertThat(result.getLoanId()).isEqualTo(LOAN_ID);
        final List<BusinessEvent<?>> events = publishedEvents(1);
        assertThat(events.get(0)).isInstanceOf(WorkingCapitalLoanRecoveryPaymentTransactionBusinessEvent.class);
        verify(accountingProcessor, never()).postJournalEntries(any(), any(), any(), anyBoolean());
    }

    @Test
    void recoveryPaymentPostsJournalEntriesForAccrualDeferredAccounting() {
        when(loanProduct.getAccountingRule()).thenReturn(WorkingCapitalAccountingRuleType.ACC_DEF_REV_AM);
        when(loan.isChargedOff()).thenReturn(false);

        recoveryPaymentService.recoveryPayment(LOAN_ID, command);

        // No allocation travels with a recovery payment: the whole amount is recovery income.
        verify(accountingProcessor).postJournalEntries(eq(loan), any(WorkingCapitalLoanTransaction.class), isNull(), eq(false));
    }

    @Test
    void undoRecoveryPaymentRestoresTotalRecoveredAndPublishesSpecificAndGenericEvents() {
        when(loanProduct.getAccountingRule()).thenReturn(WorkingCapitalAccountingRuleType.ACC_DEF_REV_AM);
        balance.setTotalRecovered(new BigDecimal("100"));
        final WorkingCapitalLoanTransaction transaction = WorkingCapitalLoanTransaction.recoveryPayment(loan, AMOUNT, null, businessDate,
                null);

        recoveryPaymentService.undoRecoveryPayment(loan, transaction, command);

        assertThat(transaction.isReversed()).isTrue();
        assertThat(transaction.getReversedOnDate()).isEqualTo(businessDate);
        assertThat(balance.getTotalRecovered()).isEqualByComparingTo("60");
        verify(balanceRepository).saveAndFlush(balance);
        verify(accountingProcessor).postReversalJournalEntries(loan, transaction);
        verify(adjustTransactionEventPublisher).publishReversal(LOAN_ID, transaction);
    }

    @Test
    void undoRecoveryPaymentToleratesARequestWithNoBody() {
        balance.setTotalRecovered(AMOUNT);
        final WorkingCapitalLoanTransaction transaction = WorkingCapitalLoanTransaction.recoveryPayment(loan, AMOUNT, null, businessDate,
                null);
        // A real command built from an empty request body: parsedCommand is null, which validateUndoRecoveryPayment
        // permits. The service must not dereference the parsed JSON on this path.
        final JsonCommand bodilessCommand = JsonCommand.from(null, null, new FromJsonHelper(), "WORKINGCAPITALLOAN", LOAN_ID, null, null,
                null, LOAN_ID, null, null, null, null, null, null, null, null);

        final CommandProcessingResult result = recoveryPaymentService.undoRecoveryPayment(loan, transaction, bodilessCommand);

        assertThat(result.getLoanId()).isEqualTo(LOAN_ID);
        assertThat(balance.getTotalRecovered()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void undoRecoveryPaymentFailsWhenTotalRecoveredIsLessThanTheReversedAmount() {
        balance.setTotalRecovered(BigDecimal.TEN);
        final WorkingCapitalLoanTransaction transaction = WorkingCapitalLoanTransaction.recoveryPayment(loan, AMOUNT, null, businessDate,
                null);

        final GeneralPlatformDomainRuleException ex = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> recoveryPaymentService.undoRecoveryPayment(loan, transaction, command));

        assertThat(ex.getGlobalisationMessageCode()).isEqualTo("error.msg.wc.loan.total.recovered.less.than.reversed.amount");
        // The inconsistent balance was left untouched and nothing was published on the way out.
        verify(balanceRepository, never()).saveAndFlush(any());
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void recoveryPaymentIsRejectedWhenTheLoanHasNoBalanceRow() {
        when(loan.getBalance()).thenReturn(null);

        final GeneralPlatformDomainRuleException ex = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> recoveryPaymentService.recoveryPayment(LOAN_ID, command));

        assertThat(ex.getGlobalisationMessageCode()).isEqualTo("error.msg.wc.loan.balance.not.found");
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void undoRecoveryPaymentIsRejectedWhenTheLoanHasNoBalanceRow() {
        when(loan.getBalance()).thenReturn(null);
        final WorkingCapitalLoanTransaction transaction = WorkingCapitalLoanTransaction.recoveryPayment(loan, AMOUNT, null, businessDate,
                null);

        final GeneralPlatformDomainRuleException ex = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> recoveryPaymentService.undoRecoveryPayment(loan, transaction, command));

        assertThat(ex.getGlobalisationMessageCode()).isEqualTo("error.msg.wc.loan.balance.not.found");
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    private List<BusinessEvent<?>> publishedEvents(final int expectedCount) {
        final ArgumentCaptor<BusinessEvent<?>> captor = ArgumentCaptor.forClass(BusinessEvent.class);
        verify(businessEventNotifierService, times(expectedCount)).notifyPostBusinessEvent(captor.capture());
        return captor.getAllValues();
    }
}
