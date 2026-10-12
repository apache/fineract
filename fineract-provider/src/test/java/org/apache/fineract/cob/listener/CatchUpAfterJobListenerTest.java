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
package org.apache.fineract.cob.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.IsCatchUpRunningDTO;
import org.apache.fineract.cob.data.OldestCOBProcessedLoanDTO;
import org.apache.fineract.cob.service.COBCatchUpService;
import org.apache.fineract.cob.service.LoanCOBCatchUpService;
import org.apache.fineract.cob.service.WorkingCapitalLoanCOBCatchUpService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

class CatchUpAfterJobListenerTest {

    private static final LocalDate COB_DATE = LocalDate.of(2026, 10, 12);

    private FineractProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FineractProperties();
        final FineractProperties.FineractJobProperties jobProperties = new FineractProperties.FineractJobProperties();
        jobProperties.setExecuteMissedRuns(true);
        properties.setJob(jobProperties);
    }

    record Case(String name, Class<? extends COBCatchUpService> serviceType,
            BiFunction<FineractProperties, COBCatchUpService, CatchUpAfterJobListener> factory, String jobName) {

        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<Case> listeners() {
        return Stream.of(
                new Case("Loan COB", LoanCOBCatchUpService.class,
                        (properties, service) -> new LoanCOBCatchUpAfterJobListener(properties, (LoanCOBCatchUpService) service),
                        JobName.LOAN_COB.name()),
                new Case("Working Capital COB", WorkingCapitalLoanCOBCatchUpService.class,
                        (properties, service) -> new WorkingCapitalLoanCOBCatchUpAfterJobListener(properties,
                                (WorkingCapitalLoanCOBCatchUpService) service),
                        JobName.WORKING_CAPITAL_LOAN_COB_JOB.name()));
    }

    @ParameterizedTest
    @MethodSource("listeners")
    void listensToItsOwnJobOnly(Case listener) {
        assertThat(listener.factory().apply(properties, mock(listener.serviceType())).getJobName()).isEqualTo(listener.jobName());
    }

    @ParameterizedTest
    @MethodSource("listeners")
    void startsCatchUpWhenLoansLagBehindTheCobDate(Case listener) {
        final COBCatchUpService service = service(listener, false, COB_DATE.minusDays(3));

        listener.factory().apply(properties, service).afterJob(execution(BatchStatus.COMPLETED));

        verify(service).executeLoanCOBCatchUp();
    }

    @ParameterizedTest
    @MethodSource("listeners")
    void startsNothingWhenAllLoansAreUpToDate(Case listener) {
        final COBCatchUpService service = service(listener, false, COB_DATE);

        listener.factory().apply(properties, service).afterJob(execution(BatchStatus.COMPLETED));

        verify(service, never()).executeLoanCOBCatchUp();
    }

    @ParameterizedTest
    @MethodSource("listeners")
    void startsNothingWhenCatchUpIsAlreadyRunning(Case listener) {
        final COBCatchUpService service = service(listener, true, COB_DATE.minusDays(3));

        listener.factory().apply(properties, service).afterJob(execution(BatchStatus.COMPLETED));

        verify(service, never()).executeLoanCOBCatchUp();
        verify(service, never()).unlockHardLockedLoans();
    }

    /**
     * AbstractJob.execute saves the final status before afterJob, so the row of a catch-up day is no longer STARTED
     * here and isCatchUpRunning() is false. Every day of an N-day catch-up must still start nothing.
     */
    @ParameterizedTest
    @MethodSource("listeners")
    void startsNothingAtTheEndOfACatchUpRun(Case listener) {
        final COBCatchUpService service = service(listener, false, COB_DATE.minusDays(3));

        for (int day = 0; day < 3; day++) {
            listener.factory().apply(properties, service).afterJob(execution(BatchStatus.COMPLETED, "true"));
        }

        verify(service, never()).executeLoanCOBCatchUp();
        verify(service, never()).unlockHardLockedLoans();
    }

    @ParameterizedTest
    @MethodSource("listeners")
    void startsCatchUpAfterARegularRunWhoseCatchUpFlagIsFalse(Case listener) {
        final COBCatchUpService service = service(listener, false, COB_DATE.minusDays(3));

        listener.factory().apply(properties, service).afterJob(execution(BatchStatus.COMPLETED, "false"));

        verify(service).executeLoanCOBCatchUp();
    }

    @ParameterizedTest
    @MethodSource("listeners")
    void startsNothingWhenTheRunDidNotComplete(Case listener) {
        final COBCatchUpService service = service(listener, false, COB_DATE.minusDays(3));

        for (BatchStatus status : new BatchStatus[] { BatchStatus.FAILED, BatchStatus.STOPPED, BatchStatus.ABANDONED }) {
            listener.factory().apply(properties, service).afterJob(execution(status));
        }

        verify(service, never()).executeLoanCOBCatchUp();
        verify(service, never()).unlockHardLockedLoans();
    }

    @ParameterizedTest
    @MethodSource("listeners")
    void startsNothingWhenMissedRunsAreNotEnabled(Case listener) {
        properties.getJob().setExecuteMissedRuns(false);
        final COBCatchUpService service = service(listener, false, COB_DATE.minusDays(3));

        listener.factory().apply(properties, service).afterJob(execution(BatchStatus.COMPLETED));

        verify(service, never()).executeLoanCOBCatchUp();
        verify(service, never()).unlockHardLockedLoans();
    }

    private static COBCatchUpService service(Case listener, boolean catchUpRunning, LocalDate oldestProcessedDate) {
        final COBCatchUpService service = mock(listener.serviceType());
        when(service.isCatchUpRunning()).thenReturn(new IsCatchUpRunningDTO(catchUpRunning, catchUpRunning ? COB_DATE : null));
        final OldestCOBProcessedLoanDTO oldest = new OldestCOBProcessedLoanDTO();
        oldest.setCobProcessedDate(oldestProcessedDate);
        oldest.setCobBusinessDate(COB_DATE);
        when(service.getOldestCOBProcessedLoan()).thenReturn(oldest);
        return service;
    }

    private static JobExecution execution(BatchStatus status) {
        return execution(status, null);
    }

    private static JobExecution execution(BatchStatus status, String isCatchUp) {
        final JobExecution execution = mock(JobExecution.class);
        when(execution.getStatus()).thenReturn(status);
        final ExecutionContext context = new ExecutionContext();
        if (isCatchUp != null) {
            context.put(COBConstant.IS_CATCH_UP_PARAMETER_NAME, isCatchUp);
        }
        when(execution.getExecutionContext()).thenReturn(context);
        return execution;
    }
}
