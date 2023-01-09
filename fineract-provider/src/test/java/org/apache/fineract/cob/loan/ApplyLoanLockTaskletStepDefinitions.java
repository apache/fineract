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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.cucumber.java8.En;
import java.util.List;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

public class ApplyLoanLockTaskletStepDefinitions implements En {

    ArgumentCaptor<LoanAccountLock> valueCaptor = ArgumentCaptor.forClass(LoanAccountLock.class);
    private LoanAccountLockRepository accountLockRepository = mock(LoanAccountLockRepository.class);
    private ApplyLoanLockTasklet applyLoanLockTasklet = new ApplyLoanLockTasklet(accountLockRepository);
    private RepeatStatus resultItem;
    private StepContribution stepContribution;

    public ApplyLoanLockTaskletStepDefinitions() {
        Given("/^The ApplyLoanLockTasklet.execute method with action (.*)$/", (String action) -> {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
            StepExecution stepExecution = new StepExecution("test", null);
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.put(LoanCOBConstant.LOAN_IDS, List.of(1L, 2L, 3L, 4L));
            stepExecution.setExecutionContext(executionContext);
            this.stepContribution = new StepContribution(stepExecution);

            if ("error".equals(action)) {
                lenient().when(this.accountLockRepository.findAllByLoanIdIn(Mockito.anyList())).thenThrow(new RuntimeException("fail"));
            } else {
                LoanAccountLock lock1 = new LoanAccountLock(1L, LockOwner.LOAN_COB_CHUNK_PROCESSING);
                LoanAccountLock lock2 = new LoanAccountLock(2L, LockOwner.LOAN_COB_PARTITIONING);
                LoanAccountLock lock3 = new LoanAccountLock(3L, LockOwner.LOAN_INLINE_COB_PROCESSING);
                List<LoanAccountLock> accountLocks = List.of(lock1, lock2, lock3);
                lenient().when(this.accountLockRepository.findAllByLoanIdIn(Mockito.anyList())).thenReturn(accountLocks);
            }

        });

        When("ApplyLoanLockTasklet.execute method executed", () -> {
            resultItem = this.applyLoanLockTasklet.execute(stepContribution, null);
        });

        Then("ApplyLoanLockTasklet.execute result should match", () -> {
            assertEquals(RepeatStatus.FINISHED, resultItem);
            assertEquals(3L, ((List) stepContribution.getStepExecution().getExecutionContext()
                    .get(LoanCOBConstant.ALREADY_LOCKED_BY_INLINE_COB_OR_PROCESSED_LOAN_IDS)).get(0));
            assertEquals(4L, ((List) stepContribution.getStepExecution().getExecutionContext()
                    .get(LoanCOBConstant.ALREADY_LOCKED_BY_INLINE_COB_OR_PROCESSED_LOAN_IDS)).get(1));
            verify(this.accountLockRepository, Mockito.times(1)).save(valueCaptor.capture());
            List<LoanAccountLock> values = valueCaptor.getAllValues();
            assertEquals(2L, values.get(0).getLoanId());
            assertEquals(LockOwner.LOAN_COB_CHUNK_PROCESSING, values.get(0).getLockOwner());
        });

        Then("throw exception ApplyLoanLockTasklet.execute method", () -> {
            assertThrows(RuntimeException.class, () -> {
                resultItem = this.applyLoanLockTasklet.execute(stepContribution, null);
            });
        });
    }
}
