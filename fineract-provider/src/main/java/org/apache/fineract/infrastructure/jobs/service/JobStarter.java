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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.JobParameterRepository;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.service.jobname.JobNameService;
import org.apache.fineract.infrastructure.jobs.service.jobparameterprovider.JobParameterProvider;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobStarter {

    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;
    private final JobParameterRepository jobParameterRepository;
    private final List<JobParameterProvider> jobParameterProviders;
    private final JobNameService jobNameService;

    public void run(Job job, ScheduledJobDetail scheduledJobDetail, FineractContext fineractContext,
            Set<JobParameterDTO> jobParameterDTOSet) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException {
        ThreadLocalContextUtil.init(fineractContext);
        Map<String, JobParameter> jobParameterMap = getJobParameter(scheduledJobDetail);
        JobParameters jobParameters = new JobParametersBuilder(jobExplorer).getNextJobParameters(job)
                .addJobParameters(new JobParameters(jobParameterMap))
                .addJobParameters(new JobParameters(provideCustomJobParameters(
                        jobNameService.getJobByHumanReadableName(scheduledJobDetail.getJobName()).getEnumStyleName(), jobParameterDTOSet)))
                .toJobParameters();
        jobLauncher.run(job, jobParameters);
    }

    public Map<String, org.springframework.batch.core.JobParameter> getJobParameter(ScheduledJobDetail scheduledJobDetail) {
        List<org.apache.fineract.infrastructure.jobs.domain.JobParameter> jobParameterList = jobParameterRepository
                .findJobParametersByJobId(scheduledJobDetail.getId());
        Map<String, JobParameter> jobParameterMap = new HashMap<>();
        for (org.apache.fineract.infrastructure.jobs.domain.JobParameter jobParameter : jobParameterList) {
            jobParameterMap.put(jobParameter.getParameterName(), new JobParameter(jobParameter.getParameterValue()));
        }
        return jobParameterMap;
    }

    private Map<String, JobParameter> provideCustomJobParameters(String jobName, Set<JobParameterDTO> jobParameterDTOSet) {
        Optional<JobParameterProvider> jobParameterProvider = jobParameterProviders.stream()
                .filter(provider -> provider.canProvideParametersForJob(jobName)).findFirst();
        return jobParameterProvider.map(parameterProvider -> parameterProvider.provide(jobParameterDTOSet)).orElse(Collections.emptyMap());
    }
}
