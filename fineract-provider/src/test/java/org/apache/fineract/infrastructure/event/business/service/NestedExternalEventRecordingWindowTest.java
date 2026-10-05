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
package org.apache.fineract.infrastructure.event.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.fineract.cob.COBBusinessStep;
import org.apache.fineract.cob.COBBusinessStepServiceImpl;
import org.apache.fineract.cob.domain.BatchBusinessStepRepository;
import org.apache.fineract.cob.service.ReloaderService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.BulkBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanInterestRecalculationBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.ExternalEventService;
import org.apache.fineract.portfolio.loanaccount.data.TransactionChangeData;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.service.ReplayedTransactionBusinessEventServiceImpl;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanAdjustTransactionEventPublisher;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanAdjustTransactionEventPublisher.WorkingCapitalLoanTransactionAdjustment;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanTransactionDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * A COB run over one account must reach the consumer as exactly one bulk event, even when a business step opens an
 * external event recording window of its own part way through the chain.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NestedExternalEventRecordingWindowTest {

    private static final Long LOAN_ID = 1L;
    private static final String STEP_BEFORE = "stepBefore";
    private static final String STEP_INNER_WINDOW = "stepOpeningInnerWindow";
    private static final String STEP_AFTER = "stepAfter";

    @Mock
    private ExternalEventService externalEventService;
    @Mock
    private ExternalBusinessEventConfigurationService externalBusinessEventConfigurationService;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private TransactionHelper transactionHelper;
    @Mock
    private BatchBusinessStepRepository batchBusinessStepRepository;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ListableBeanFactory beanFactory;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private ReloaderService reloaderService;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private WorkingCapitalLoanTransactionDataFactory transactionDataFactory;

    private BusinessEventNotifierServiceImpl businessEventNotifierService;
    private COBBusinessStepServiceImpl cobBusinessStepService;
    private Loan loan;
    private WorkingCapitalLoan workingCapitalLoan;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));

        FineractProperties.FineractEventsProperties eventsProperties = new FineractProperties.FineractEventsProperties();
        FineractProperties.FineractExternalEventsProperties externalProperties = new FineractProperties.FineractExternalEventsProperties();
        eventsProperties.setExternal(externalProperties);
        externalProperties.setEnabled(true);
        given(fineractProperties.getEvents()).willReturn(eventsProperties);
        given(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(any())).willReturn(true);
        given(externalBusinessEventConfigurationService.isExternalEventTypeConfiguredForPosting(any())).willReturn(true);
        given(transactionHelper.hasTransaction()).willReturn(false);
        given(reloaderService.reload(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(configurationDomainService.isCOBBulkEventEnabled()).willReturn(true);

        // Every event a single COB run raises belongs to one aggregate root, which is what BulkBusinessEvent requires.
        loan = mock(Loan.class);
        given(loan.getId()).willReturn(LOAN_ID);
        workingCapitalLoan = mock(WorkingCapitalLoan.class);
        given(workingCapitalLoan.getId()).willReturn(LOAN_ID);

        businessEventNotifierService = new BusinessEventNotifierServiceImpl(externalEventService, fineractProperties, transactionHelper,
                externalBusinessEventConfigurationService);
        cobBusinessStepService = new COBBusinessStepServiceImpl(batchBusinessStepRepository, applicationContext, beanFactory,
                businessEventNotifierService, configurationDomainService, reloaderService);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void loanTransactionReplayInsideACOBRunShouldNotCloseTheCOBRecordingWindow() {
        // given
        LoanBalanceChangedBusinessEvent beforeEvent = new LoanBalanceChangedBusinessEvent(loan);
        LoanInterestRecalculationBusinessEvent afterEvent = new LoanInterestRecalculationBusinessEvent(loan);
        registerSteps(() -> businessEventNotifierService.notifyPostBusinessEvent(beforeEvent), this::replayLoanTransaction,
                () -> businessEventNotifierService.notifyPostBusinessEvent(afterEvent));

        // when
        cobBusinessStepService.run(executionMap(), loan);

        // then the whole COB run reaches the consumer as a single bulk event
        assertThat(capturePostedEvents()).hasSize(1);
        assertThat(capturedBulkEvent().get()).hasSize(3).element(0).isEqualTo(beforeEvent);
        assertThat(capturedBulkEvent().get()).element(2).isEqualTo(afterEvent);
    }

    @Test
    void workingCapitalReprocessingInsideACOBRunShouldNotCloseTheCOBRecordingWindow() {
        // given
        WorkingCapitalLoanBalanceChangedBusinessEvent beforeEvent = new WorkingCapitalLoanBalanceChangedBusinessEvent(workingCapitalLoan);
        WorkingCapitalLoanStatusChangedBusinessEvent afterEvent = new WorkingCapitalLoanStatusChangedBusinessEvent(workingCapitalLoan);
        registerSteps(() -> businessEventNotifierService.notifyPostBusinessEvent(beforeEvent), this::reprocessWorkingCapitalTransactions,
                () -> businessEventNotifierService.notifyPostBusinessEvent(afterEvent));

        // when
        cobBusinessStepService.run(executionMap(), workingCapitalLoan);

        // then
        assertThat(capturePostedEvents()).hasSize(1);
        assertThat(capturedBulkEvent().get()).hasSize(3).element(0).isEqualTo(beforeEvent);
        assertThat(capturedBulkEvent().get()).element(2).isEqualTo(afterEvent);
    }

    @Test
    void eventsRaisedAfterAnInnerWindowClosesShouldStillBeRecorded() {
        // given
        LoanInterestRecalculationBusinessEvent afterEvent = new LoanInterestRecalculationBusinessEvent(loan);
        registerSteps(() -> {
            // blank on purpose
        }, this::replayLoanTransaction, () -> businessEventNotifierService.notifyPostBusinessEvent(afterEvent));

        // when
        cobBusinessStepService.run(executionMap(), loan);

        // then the trailing event must not escape as a standalone external event
        assertThat(capturePostedEvents()).noneMatch(afterEvent::equals);
    }

    private void replayLoanTransaction() {
        LoanTransaction oldTransaction = mock(LoanTransaction.class);
        LoanTransaction newTransaction = mock(LoanTransaction.class);
        given(oldTransaction.getId()).willReturn(LOAN_ID);
        given(oldTransaction.getLoan()).willReturn(loan);
        given(newTransaction.isNotReversed()).willReturn(true);
        ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        changedTransactionDetail.addTransactionChange(new TransactionChangeData(oldTransaction, newTransaction));

        new ReplayedTransactionBusinessEventServiceImpl(businessEventNotifierService, loanTransactionRepository)
                .raiseTransactionReplayedEvents(changedTransactionDetail);
    }

    private void reprocessWorkingCapitalTransactions() {
        WorkingCapitalLoanTransactionData previousState = mock(WorkingCapitalLoanTransactionData.class);
        WorkingCapitalLoanTransactionData currentState = mock(WorkingCapitalLoanTransactionData.class);

        new WorkingCapitalLoanAdjustTransactionEventPublisher(transactionDataFactory, businessEventNotifierService)
                .publishReprocessed(LOAN_ID, List.of(new WorkingCapitalLoanTransactionAdjustment(previousState, currentState)));
    }

    private void registerSteps(Runnable before, Runnable innerWindow, Runnable after) {
        given(applicationContext.getBean(STEP_BEFORE)).willReturn(step(before));
        given(applicationContext.getBean(STEP_INNER_WINDOW)).willReturn(step(innerWindow));
        given(applicationContext.getBean(STEP_AFTER)).willReturn(step(after));
    }

    private <S extends AbstractPersistableCustom<Long>> COBBusinessStep<S> step(Runnable body) {
        return new COBBusinessStep<>() {

            @Override
            public S execute(S item) {
                body.run();
                return item;
            }

            @Override
            public String getEnumStyledName() {
                return "TEST_STEP";
            }

            @Override
            public String getHumanReadableName() {
                return "Test step";
            }
        };
    }

    private TreeMap<Long, String> executionMap() {
        TreeMap<Long, String> executionMap = new TreeMap<>();
        executionMap.put(1L, STEP_BEFORE);
        executionMap.put(2L, STEP_INNER_WINDOW);
        executionMap.put(3L, STEP_AFTER);
        return executionMap;
    }

    private List<BusinessEvent<?>> capturePostedEvents() {
        ArgumentCaptor<BusinessEvent<?>> captor = captor();
        verify(externalEventService, org.mockito.Mockito.atLeast(0)).postEvent(captor.capture());
        return captor.getAllValues();
    }

    private BulkBusinessEvent capturedBulkEvent() {
        return (BulkBusinessEvent) capturePostedEvents().stream().filter(BulkBusinessEvent.class::isInstance).findFirst()
                .orElseThrow(() -> new AssertionError("No BulkBusinessEvent was posted"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ArgumentCaptor<BusinessEvent<?>> captor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(BusinessEvent.class);
    }
}
