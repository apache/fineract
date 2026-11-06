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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;

class StandingInstructionBusinessDateListenerTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2023, 3, 31);

    private final StandingInstructionBusinessDateListener listener = new StandingInstructionBusinessDateListener();
    private final StepExecution stepExecution = new StepExecution(ExecuteStandingInstructionsConstant.WORKER_STEP,
            new JobExecution(1L, new JobInstance(1L, ExecuteStandingInstructionsConstant.JOB_NAME), new JobParameters()));

    @BeforeEach
    void setUp() {
        final HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE);
        businessDates.put(BusinessDateType.COB_DATE, BUSINESS_DATE.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);
        // What InputChannelInterceptor sets on every worker message.
        ThreadLocalContextUtil.setActionContext(ActionContext.COB);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void theStepRunsOnTheBusinessDateNotTheCobDate() {
        assertThat(ThreadLocalContextUtil.getBusinessDate()).isEqualTo(BUSINESS_DATE.minusDays(1));

        listener.beforeStep(stepExecution);

        assertThat(ThreadLocalContextUtil.getActionContext()).isEqualTo(ActionContext.DEFAULT);
        assertThat(ThreadLocalContextUtil.getBusinessDate()).isEqualTo(BUSINESS_DATE);
    }

    @Test
    void afterTheStepTheThreadIsBackInTheCobContext() {
        listener.beforeStep(stepExecution);

        listener.afterStep(stepExecution);

        assertThat(ThreadLocalContextUtil.getActionContext()).isEqualTo(ActionContext.COB);
    }
}
