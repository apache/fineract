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
package org.apache.fineract.portfolio.savings.jobs.postinterestforsavings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsSchedularInterestPosterTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class PostInterestForSavingTaskletTest {

    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService = mock(SavingsAccountReadPlatformService.class);
    private final ApplicationContext applicationContext = mock(ApplicationContext.class);
    private final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    private PostInterestForSavingTasklet tasklet;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 10, 11))));
        taskExecutor.initialize();
        tasklet = new PostInterestForSavingTasklet(savingsAccountReadPlatformService, mock(ConfigurationDomainService.class),
                applicationContext, taskExecutor);
        List<SavingsAccountData> page = List.of(account(1L), account(2L));
        when(savingsAccountReadPlatformService.retrieveAllSavingsDataForInterestPosting(anyBoolean(), anyInt(), anyInt(), eq(0L)))
                .thenReturn(new ArrayList<>(page));
        when(savingsAccountReadPlatformService.retrieveAllSavingsDataForInterestPosting(anyBoolean(), anyInt(), anyInt(), eq(2L)))
                .thenReturn(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        taskExecutor.shutdown();
        ThreadLocalContextUtil.reset();
    }

    @Test
    void jobFailsWhenAnInterestPosterFails() throws Exception {
        RuntimeException postingFailure = new IllegalStateException("posting failed for savings account 2");
        givenPosterTasks(null, new JobExecutionException(List.of(postingFailure)));

        JobExecutionException failure = catchThrowableOfType(JobExecutionException.class,
                () -> tasklet.execute(mock(StepContribution.class), chunkContext()));

        assertThat(failure).isNotNull();
        assertThat(failure.getCauses()).containsExactly(postingFailure);
    }

    @Test
    void jobFinishesWhenAllInterestPostersSucceed() throws Exception {
        givenPosterTasks(null, null);

        RepeatStatus status = tasklet.execute(mock(StepContribution.class), chunkContext());

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    // Two accounts on two threads give one poster task per account
    private void givenPosterTasks(JobExecutionException firstFailure, JobExecutionException secondFailure) throws Exception {
        SavingsSchedularInterestPosterTask firstTask = posterTask(firstFailure);
        SavingsSchedularInterestPosterTask secondTask = posterTask(secondFailure);
        when(applicationContext.getBean(SavingsSchedularInterestPosterTask.class)).thenReturn(firstTask, secondTask);
    }

    private static SavingsSchedularInterestPosterTask posterTask(JobExecutionException failure) throws Exception {
        SavingsSchedularInterestPosterTask posterTask = mock(SavingsSchedularInterestPosterTask.class);
        if (failure != null) {
            when(posterTask.call()).thenThrow(failure);
        }
        return posterTask;
    }

    private static SavingsAccountData account(Long id) {
        SavingsAccountData account = mock(SavingsAccountData.class);
        when(account.getId()).thenReturn(id);
        return account;
    }

    private static ChunkContext chunkContext() {
        ChunkContext chunkContext = mock(ChunkContext.class, RETURNS_DEEP_STUBS);
        when(chunkContext.getStepContext().getJobParameters()).thenReturn(Map.of("thread-pool-size", "2", "batch-size", "1"));
        return chunkContext;
    }
}
