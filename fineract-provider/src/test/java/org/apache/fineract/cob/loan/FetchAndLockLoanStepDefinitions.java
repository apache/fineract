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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.cucumber.java8.En;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.mockito.Mockito;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.repeat.RepeatStatus;

public class FetchAndLockLoanStepDefinitions implements En {

    private final LoanAccountLockRepository loanAccountLockRepository = mock(LoanAccountLockRepository.class);

    private final RetrieveLoanIdService retrieveLoanIdService = mock(RetrieveLoanIdService.class);
    StepContribution contribution;
    private FetchAndLockLoanTasklet fetchAndLockLoanTasklet;
    private String action;
    private RepeatStatus result;

    public FetchAndLockLoanStepDefinitions() {
        Given("/^The FetchAndLockLoanTasklet.execute method with action (.*)$/", (String action) -> {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
            HashMap<BusinessDateType, LocalDate> businessDateMap = new HashMap<>();
            businessDateMap.put(BusinessDateType.COB_DATE, LocalDate.now(ZoneId.systemDefault()));
            ThreadLocalContextUtil.setBusinessDates(businessDateMap);
            this.action = action;

            if ("empty loanIds".equals(action)) {
                lenient().when(retrieveLoanIdService.retrieveLoanIdsNDaysBehind(anyLong(), any())).thenReturn(Collections.emptyList());
            } else if ("good".equals(action)) {
                lenient().when(retrieveLoanIdService.retrieveLoanIdsNDaysBehind(anyLong(), any())).thenReturn(List.of(1L, 2L, 3L));
                lenient().when(loanAccountLockRepository.findAllByLoanIdIn(Mockito.anyList())).thenReturn(Collections.emptyList());
            } else if ("soft lock".equals(action)) {
                lenient().when(retrieveLoanIdService.retrieveLoanIdsNDaysBehind(anyLong(), any())).thenReturn(List.of(1L, 2L, 3L));
                lenient().when(loanAccountLockRepository.findAllByLoanIdIn(Mockito.anyList()))
                        .thenReturn(List.of(new LoanAccountLock(1L, LockOwner.LOAN_COB_PARTITIONING)));
            } else if ("inline cob".equals(action)) {
                lenient().when(retrieveLoanIdService.retrieveLoanIdsNDaysBehind(anyLong(), any())).thenReturn(List.of(1L, 2L, 3L));
                lenient().when(loanAccountLockRepository.findAllByLoanIdIn(Mockito.anyList()))
                        .thenReturn(List.of(new LoanAccountLock(2L, LockOwner.LOAN_INLINE_COB_PROCESSING)));
            } else if ("chunk processing".equals(action)) {
                lenient().when(retrieveLoanIdService.retrieveLoanIdsNDaysBehind(anyLong(), any())).thenReturn(List.of(1L, 2L, 3L));
                lenient().when(loanAccountLockRepository.findAllByLoanIdIn(Mockito.anyList()))
                        .thenReturn(List.of(new LoanAccountLock(3L, LockOwner.LOAN_COB_CHUNK_PROCESSING)));
            }

            JobExecution jobExecution = new JobExecution(1L);
            StepExecution stepExecution = new StepExecution("step", jobExecution);
            contribution = new StepContribution(stepExecution);
            contribution.getStepExecution().getJobExecution().getExecutionContext().put(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME,
                    LocalDate.now(ZoneId.systemDefault()).toString());
            fetchAndLockLoanTasklet = new FetchAndLockLoanTasklet(loanAccountLockRepository, retrieveLoanIdService);
        });

        When("FetchAndLockLoanTasklet.execute method executed", () -> {
            result = this.fetchAndLockLoanTasklet.execute(contribution, null);
        });

        Then("FetchAndLockLoanTasklet.execute result should match", () -> {
            if ("empty steps".equals(action)) {
                assertEquals(RepeatStatus.FINISHED, result);
            } else if ("good".equals(action)) {
                verify(loanAccountLockRepository, Mockito.times(3)).save(Mockito.any());
                assertEquals(RepeatStatus.FINISHED, result);
                assertEquals(3,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .size());
                assertEquals(1L,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .get(0));
                assertEquals(2L,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .get(1));
                assertEquals(3L,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .get(2));
            } else if ("soft lock".equals(action)) {
                verify(loanAccountLockRepository, Mockito.times(2)).save(Mockito.any());
                assertEquals(RepeatStatus.FINISHED, result);
                assertEquals(3,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .size());
                assertEquals(1L,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .get(0));
                assertEquals(2L,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .get(1));
                assertEquals(3L,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .get(2));
            } else if ("inline cob".equals(action)) {
                verify(loanAccountLockRepository, Mockito.times(2)).save(Mockito.any());
                assertEquals(RepeatStatus.FINISHED, result);
                assertEquals(2,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .size());
                assertEquals(1L,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .get(0));
                assertEquals(3L,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .get(1));
            } else if ("chunk processing".equals(action)) {
                verify(loanAccountLockRepository, Mockito.times(2)).save(Mockito.any());
                assertEquals(RepeatStatus.FINISHED, result);
                assertEquals(2,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .size());
                assertEquals(1L,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .get(0));
                assertEquals(2L,
                        ((List) contribution.getStepExecution().getJobExecution().getExecutionContext().get(LoanCOBConstant.LOAN_IDS))
                                .get(1));
            }
        });

    }
}
