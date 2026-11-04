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
package org.apache.fineract.portfolio.loanaccount.jobs.recalculateinterestforloan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.RecalculateInterestPoster;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class RecalculateInterestForLoanTaskletFailureTest {

    private final LoanReadPlatformService loanReadPlatformService = mock(LoanReadPlatformService.class);
    private final LoanWritePlatformService loanWritePlatformService = mock(LoanWritePlatformService.class);
    private final ApplicationContext applicationContext = mock(ApplicationContext.class);
    private final OfficeReadPlatformService officeReadPlatformService = mock(OfficeReadPlatformService.class);
    private final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    private RecalculateInterestForLoanTasklet tasklet;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 10, 11))));
        taskExecutor.initialize();
        OfficeData headOffice = mock(OfficeData.class);
        when(headOffice.getHierarchy()).thenReturn(".");
        when(officeReadPlatformService.retrieveOffice(1L)).thenReturn(headOffice);
        when(applicationContext.getBean(RecalculateInterestPoster.class))
                .thenAnswer(invocation -> new RecalculateInterestPoster(loanWritePlatformService));
        // One page of two loans, then nothing more
        when(loanReadPlatformService.fetchLoansForInterestRecalculation(anyInt(), anyLong(), anyString()))
                .thenAnswer(invocation -> (long) invocation.getArgument(1) == 0L ? new ArrayList<>(List.of(1L, 2L)) : new ArrayList<>());
        tasklet = new RecalculateInterestForLoanTasklet(loanReadPlatformService, loanWritePlatformService, applicationContext,
                officeReadPlatformService, taskExecutor);
    }

    @AfterEach
    void tearDown() {
        taskExecutor.shutdown();
        ThreadLocalContextUtil.reset();
    }

    @Test
    void jobFailsWhenRecalculatingALoanFails() throws Exception {
        RuntimeException recalculationFailure = new IllegalStateException("recalculation failed for loan 2");
        doThrow(recalculationFailure).when(loanWritePlatformService).recalculateInterest(2L);

        JobExecutionException failure = catchThrowableOfType(JobExecutionException.class,
                () -> tasklet.execute(mock(StepContribution.class), chunkContext()));

        assertThat(failure).isNotNull();
        assertThat(failure.getCauses()).containsExactly(recalculationFailure);
        verify(loanWritePlatformService).recalculateInterest(1L);
    }

    @Test
    void jobFinishesWhenEveryLoanIsRecalculated() throws Exception {
        RepeatStatus status = tasklet.execute(mock(StepContribution.class), chunkContext());

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    private static ChunkContext chunkContext() {
        ChunkContext chunkContext = mock(ChunkContext.class, RETURNS_DEEP_STUBS);
        when(chunkContext.getStepContext().getStepExecution().getJobParameters()).thenReturn(new JobParametersBuilder()
                .addString("officeId", "1").addString("thread-pool-size", "2").addString("batch-size", "1").toJobParameters());
        return chunkContext;
    }
}
