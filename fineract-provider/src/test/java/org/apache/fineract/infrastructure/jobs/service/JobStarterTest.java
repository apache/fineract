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
package org.apache.fineract.infrastructure.jobs.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.JobParameter;
import org.apache.fineract.infrastructure.jobs.domain.JobParameterRepository;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.service.jobname.JobNameData;
import org.apache.fineract.infrastructure.jobs.service.jobname.JobNameService;
import org.apache.fineract.infrastructure.jobs.service.jobparameterprovider.JobParameterProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JobStarterTest {

    @Mock
    private JobExplorer jobExplorer;
    @Mock
    private JobLauncher jobLauncher;
    @Mock
    private JobParameterRepository jobParameterRepository;
    @Mock
    private List<JobParameterProvider<?>> jobParameterProviders;
    @Mock
    private JobNameService jobNameService;
    @Captor
    private ArgumentCaptor<Set<JobParameterDTO>> jobParameterDTOCaptor;

    @InjectMocks
    private JobStarter underTest;

    @Test
    public void getJobParameterTest() {
        ScheduledJobDetail scheduledJobDetail = Mockito.mock(ScheduledJobDetail.class);
        when(scheduledJobDetail.getId()).thenReturn(1L);
        when(jobParameterRepository.findJobParametersByJobId(1L))
                .thenReturn(List.of(new JobParameter().setJobId(1L).setParameterName("testParamKey").setParameterValue("testParamValue")));
        Map<String, org.springframework.batch.core.JobParameter<?>> result = underTest.getJobParameter(scheduledJobDetail);
        Assertions.assertEquals("testParamValue", result.get("testParamKey").getValue());
    }

    @Test
    public void provideCustomJobParameters() {
        JobParameterProvider<?> jobParameterProvider = Mockito.mock(JobParameterProvider.class);
        when(jobParameterProvider.canProvideParametersForJob("testJobName")).thenReturn(true);
        when(jobParameterProviders.stream()).thenReturn(Stream.of(jobParameterProvider));
        underTest.provideCustomJobParameters("testJobName", Set.of(new JobParameterDTO("testKey", "testValue")));
        verify(jobParameterProvider, times(1)).provide(jobParameterDTOCaptor.capture());
    }

    @Test
    public void runWithComplete() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, JobExecutionException {
        JobExecution jobExecution = Mockito.mock(JobExecution.class);
        Job job = Mockito.mock(Job.class);
        ScheduledJobDetail scheduledJobDetail = Mockito.mock(ScheduledJobDetail.class);
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        setupMocks(jobExecution, job, scheduledJobDetail);
        JobExecution result = underTest.run(job, scheduledJobDetail, Set.of());
        Assertions.assertEquals(jobExecution, result);
    }

    @Test
    public void runWithFailed() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, JobExecutionException {
        JobExecution jobExecution = Mockito.mock(JobExecution.class);
        Job job = Mockito.mock(Job.class);
        ScheduledJobDetail scheduledJobDetail = Mockito.mock(ScheduledJobDetail.class);

        for (BatchStatus failedStatus : JobStarter.FAILED_STATUSES) {
            setupMocks(jobExecution, job, scheduledJobDetail);
            when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);
            when(jobExecution.getExitStatus()).thenReturn(new ExitStatus(failedStatus.name(), "testException"));
            JobExecutionException exception = Assertions.assertThrows(JobExecutionException.class,
                    () -> underTest.run(job, scheduledJobDetail, Set.of()));
            Assertions.assertEquals(String.format("exitCode=%s;exitDescription=%s", failedStatus.name(), "testException"),
                    exception.getMessage());
        }
    }

    private void setupMocks(JobExecution jobExecution, Job job, ScheduledJobDetail scheduledJobDetail) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        when(scheduledJobDetail.getId()).thenReturn(1L);
        when(scheduledJobDetail.getJobName()).thenReturn("testJobName");
        when(jobParameterRepository.findJobParametersByJobId(1L)).thenReturn(List.of(new JobParameter().setJobId(1L).setParameterName("testParamKey").setParameterValue("testParamValue")));
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);
        JobParametersIncrementer jobParametersIncrementer = Mockito.mock(JobParametersIncrementer.class);
        when(jobParametersIncrementer.getNext(any(JobParameters.class))).thenReturn(new JobParameters());
        when(job.getJobParametersIncrementer()).thenReturn(jobParametersIncrementer);
        JobParameterProvider<?> jobParameterProvider = Mockito.mock(JobParameterProvider.class);
        when(jobParameterProvider.canProvideParametersForJob("testJobName")).thenReturn(true);
        when(jobParameterProviders.stream()).thenReturn(Stream.of(jobParameterProvider));
        when(jobNameService.getJobByHumanReadableName(any(String.class))).thenReturn(new JobNameData("testEnumstyleName", "testHumanReadableName"));
    }
}
