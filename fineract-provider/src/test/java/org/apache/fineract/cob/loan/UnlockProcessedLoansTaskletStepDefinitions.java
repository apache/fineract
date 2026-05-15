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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.cucumber.java8.En;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
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

public class UnlockProcessedLoansTaskletStepDefinitions implements En {

    private LoanAccountLockRepository loanAccountLockRepository = mock(LoanAccountLockRepository.class);
    private UnlockProcessedLoansTasklet tasklet = new UnlockProcessedLoansTasklet(loanAccountLockRepository);
    private RepeatStatus resultItem;
    private StepContribution stepContribution;
    private LocalDate cobDate;

    public UnlockProcessedLoansTaskletStepDefinitions() {
        Given("The UnlockProcessedLoansTasklet.execute method is called", () -> {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
            HashMap<BusinessDateType, LocalDate> businessDateMap = new HashMap<>();
            cobDate = LocalDate.now(ZoneId.systemDefault());
            businessDateMap.put(BusinessDateType.COB_DATE, cobDate);
            ThreadLocalContextUtil.setBusinessDates(businessDateMap);
            JobExecution jobExecution = new JobExecution(1L, null);
            StepExecution stepExecution = new StepExecution("test", jobExecution);
            stepContribution = new StepContribution(stepExecution);
        });

        When("UnlockProcessedLoansTasklet.execute method executed", () -> {
            try {
                resultItem = tasklet.execute(stepContribution, null);
            } finally {
                ThreadLocalContextUtil.reset();
            }
        });

        Then("UnlockProcessedLoansTasklet.execute result should match", () -> {
            assertEquals(RepeatStatus.FINISHED, resultItem);
            verify(loanAccountLockRepository, Mockito.times(1))
                    .deleteByLockOwnerAndErrorIsNullAndLockPlacedOnCobBusinessDate(LockOwner.LOAN_COB_CHUNK_PROCESSING, cobDate);
        });
    }
}
