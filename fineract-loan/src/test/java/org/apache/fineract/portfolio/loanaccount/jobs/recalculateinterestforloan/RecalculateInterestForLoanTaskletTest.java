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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.RecalculateInterestPoster;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class RecalculateInterestForLoanTaskletTest {

    private final LoanReadPlatformService loanReadPlatformService = mock(LoanReadPlatformService.class);
    private final LoanWritePlatformService loanWritePlatformService = mock(LoanWritePlatformService.class);
    private final ApplicationContext applicationContext = mock(ApplicationContext.class);
    private final OfficeReadPlatformService officeReadPlatformService = mock(OfficeReadPlatformService.class);
    private final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

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
    }

    @AfterEach
    void tearDown() {
        taskExecutor.shutdown();
        ThreadLocalContextUtil.reset();
    }

    @Test
    void everyEligibleLoanIsRecalculatedExactlyOnceAcrossPages() throws Exception {
        givenEligibleLoans(LongStream.rangeClosed(1, 7).boxed().toList());

        runForHeadOffice(1, 2);

        assertThat(recalculatedLoanIds()).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L);
    }

    @Test
    void sparseLoanIdsAreAllRecalculated() throws Exception {
        givenEligibleLoans(List.of(3L, 4L, 5L, 40L, 41L, 900L));

        runForHeadOffice(1, 2);

        assertThat(recalculatedLoanIds()).containsExactly(3L, 4L, 5L, 40L, 41L, 900L);
    }

    @Test
    void nothingIsRecalculatedWhenNoLoanIsEligible() throws Exception {
        givenEligibleLoans(List.of());

        runForHeadOffice(1, 2);

        verify(loanWritePlatformService, never()).recalculateInterest(anyLong());
    }

    // Mirrors the query: eligible loans with an id above the cursor, in id order, at most one page
    private void givenEligibleLoans(List<Long> eligibleLoanIds) {
        when(loanReadPlatformService.fetchLoansForInterestRecalculation(anyInt(), anyLong(), anyString())).thenAnswer(invocation -> {
            int pageSize = invocation.getArgument(0);
            long cursor = invocation.getArgument(1);
            return eligibleLoanIds.stream().filter(loanId -> loanId > cursor).limit(pageSize).toList();
        });
    }

    private void runForHeadOffice(int batchSize, int threadPoolSize) throws Exception {
        ChunkContext chunkContext = mock(ChunkContext.class, RETURNS_DEEP_STUBS);
        when(chunkContext.getStepContext().getStepExecution().getJobParameters()).thenReturn(
                new JobParametersBuilder().addString("officeId", "1").addString("thread-pool-size", String.valueOf(threadPoolSize))
                        .addString("batch-size", String.valueOf(batchSize)).toJobParameters());
        RecalculateInterestForLoanTasklet tasklet = new RecalculateInterestForLoanTasklet(loanReadPlatformService, loanWritePlatformService,
                applicationContext, officeReadPlatformService, taskExecutor);

        tasklet.execute(mock(StepContribution.class), chunkContext);
    }

    private List<Long> recalculatedLoanIds() {
        ArgumentCaptor<Long> loanIds = ArgumentCaptor.forClass(Long.class);
        verify(loanWritePlatformService, atLeastOnce()).recalculateInterest(loanIds.capture());
        return loanIds.getAllValues().stream().sorted().toList();
    }
}
