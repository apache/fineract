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
package org.apache.fineract.cob.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.cucumber.java8.En;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.exceptions.LockCannotBeAppliedException;
import org.apache.fineract.cob.service.RetrieveIdService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class ApplyLoanLockTaskletStepDefinitions implements En {

    @SuppressWarnings("unchecked")
    private final ArgumentCaptor<List<Long>> valueCaptor = ArgumentCaptor.forClass(List.class);
    private final ArgumentCaptor<LockOwner> lockOwnerValueCaptor = ArgumentCaptor.forClass(LockOwner.class);
    private LockingService loanLockingService = mock(LockingService.class);
    private FineractProperties fineractProperties = mock(FineractProperties.class);
    private FineractProperties.FineractQueryProperties fineractQueryProperties = mock(FineractProperties.FineractQueryProperties.class);
    private RetrieveIdService retrieveIdService = mock(RetrieveIdService.class);
    private TransactionTemplate requiresNewTransactionJdbcTemplate = spy(new TransactionTemplate(mock(PlatformTransactionManager.class)));

    private ApplyLoanLockTasklet applyLoanLockTasklet = new ApplyLoanLockTasklet(fineractProperties, loanLockingService, retrieveIdService,
            requiresNewTransactionJdbcTemplate);
    private RepeatStatus resultItem;
    private StepContribution stepContribution;

    public ApplyLoanLockTaskletStepDefinitions() {
        Given("/^The ApplyLoanLockTasklet.execute method with action (.*)$/", (String action) -> {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
            HashMap<BusinessDateType, LocalDate> businessDateMap = new HashMap<>();
            businessDateMap.put(BusinessDateType.COB_DATE, LocalDate.now(ZoneId.systemDefault()));
            ThreadLocalContextUtil.setBusinessDates(businessDateMap);
            JobExecution jobExecution = new JobExecution(1L, null);
            StepExecution stepExecution = new StepExecution("test", jobExecution);
            ExecutionContext executionContext = new ExecutionContext();
            COBParameter loanCOBParameter = new COBParameter(1L, 4L);
            executionContext.put(LoanCOBConstant.COB_PARAMETER, loanCOBParameter);
            lenient().when(retrieveIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter, false))
                    .thenReturn(List.of(1L, 2L, 3L, 4L));
            stepExecution.setExecutionContext(executionContext);
            stepContribution = new StepContribution(stepExecution);

            if ("error".equals(action)) {
                lenient().when(fineractProperties.getQuery()).thenReturn(fineractQueryProperties);
                lenient().when(fineractQueryProperties.getInClauseParameterSizeLimit()).thenReturn(65000);
                lenient().when(loanLockingService.findLockIdsByLoanIdIn(Mockito.anyList())).thenThrow(new RuntimeException("fail"));
            } else if ("db-error-first-try".equals(action)) {
                List<Long> accountLocks = List.of(1L, 3L);
                lenient().when(fineractProperties.getQuery()).thenReturn(fineractQueryProperties);
                lenient().when(fineractQueryProperties.getInClauseParameterSizeLimit()).thenReturn(65000);
                lenient().when(loanLockingService.findLockIdsByLoanIdIn(Mockito.anyList())).thenReturn(accountLocks);
                Mockito.doThrow(new RuntimeException("db error")).when(loanLockingService).applyLock(Mockito.anyList(), any());
            } else if ("db-error-not-recoverable".equals(action)) {
                Long lock1 = 1L;
                Long lock3 = 3L;
                List<Long> accountLocks = List.of(lock1, lock3);
                executionContext.putLong(LoanCOBConstant.COB_PARAMETER + ".apply-lock-attempts", 4);
                lenient().when(fineractProperties.getQuery()).thenReturn(fineractQueryProperties);
                lenient().when(fineractQueryProperties.getInClauseParameterSizeLimit()).thenReturn(65000);
                lenient().when(loanLockingService.findLockIdsByLoanIdIn(Mockito.anyList())).thenReturn(accountLocks);
                Mockito.doThrow(new RuntimeException("db error")).when(loanLockingService).applyLock(Mockito.anyList(), any());
            } else {
                Long lock1 = 1L;
                Long lock3 = 3L;
                List<Long> accountLocks = List.of(lock1, lock3);
                lenient().when(fineractProperties.getQuery()).thenReturn(fineractQueryProperties);
                lenient().when(fineractQueryProperties.getInClauseParameterSizeLimit()).thenReturn(65000);
                lenient().when(loanLockingService.findLockIdsByLoanIdIn(Mockito.anyList())).thenReturn(accountLocks);
            }
        });

        When("ApplyLoanLockTasklet.execute method executed", () -> {
            try {
                resultItem = applyLoanLockTasklet.execute(stepContribution, null);
            } finally {
                ThreadLocalContextUtil.reset();
            }
        });

        Then("ApplyLoanLockTasklet.execute result should match", () -> {
            assertEquals(RepeatStatus.FINISHED, resultItem);
            verify(loanLockingService, Mockito.times(1)).applyLock(valueCaptor.capture(), lockOwnerValueCaptor.capture());
            List<Long> values = valueCaptor.getValue();
            assertEquals(2L, values.get(0));
            assertEquals(LockOwner.LOAN_COB_CHUNK_PROCESSING, lockOwnerValueCaptor.getValue());
        });

        Then("throw exception ApplyLoanLockTasklet.execute method", () -> {
            assertThrows(RuntimeException.class, () -> {
                resultItem = applyLoanLockTasklet.execute(stepContribution, null);
            });
        });

        Then("throw LoanLockCannotBeAppliedException exception ApplyLoanLockTasklet.execute method", () -> {
            assertThrows(LockCannotBeAppliedException.class, () -> {
                resultItem = applyLoanLockTasklet.execute(stepContribution, null);
            });
        });

        Then("ApplyLoanLockTasklet.execute result should be retry", () -> {
            assertEquals(RepeatStatus.CONTINUABLE, resultItem);
        });
    }
}
