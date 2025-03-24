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

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.JobParameterRepository;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.service.jobname.JobNameService;
import org.apache.fineract.infrastructure.jobs.service.jobparameterprovider.JobParameterProvider;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobStarter {

    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;
    private final JobParameterRepository jobParameterRepository;
    private final List<JobParameterProvider<?>> jobParameterProviders;
    private final JobNameService jobNameService;
    private final TenantDetailsService tenantDetailsService;
    private final AppUserRepositoryWrapper userRepository;
    private final BusinessDateReadPlatformService businessDateReadPlatformService;

    public static final List<BatchStatus> FAILED_STATUSES = List.of(BatchStatus.FAILED, BatchStatus.ABANDONED, BatchStatus.STOPPED,
            BatchStatus.STOPPING, BatchStatus.UNKNOWN);

    public JobExecution run(Job job, ScheduledJobDetail scheduledJobDetail, Set<JobParameterDTO> jobParameterDTOSet,
            String tenantIdentifier) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, JobExecutionException {

        boolean contextInitialized = false;
        final FineractPlatformTenant existingTenant = ThreadLocalContextUtil.getTenant();

        try {
            if (existingTenant == null) {
                contextInitialized = true;
                FineractPlatformTenant tenant = tenantDetailsService.loadTenantById(tenantIdentifier);
                ThreadLocalContextUtil.setTenant(tenant);
                AppUser user = this.userRepository.fetchSystemUser();
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(),
                        user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                HashMap<BusinessDateType, LocalDate> businessDates = businessDateReadPlatformService.getBusinessDates();
                ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
                ThreadLocalContextUtil.setBusinessDates(businessDates);
            }

            Map<String, JobParameter<?>> jobParameterMap = getJobParameter(scheduledJobDetail);
            JobParameters jobParameters = new JobParametersBuilder(jobExplorer).getNextJobParameters(job)
                    .addJobParameters(new JobParameters(jobParameterMap))
                    .addJobParameters(new JobParameters(provideCustomJobParameters(
                            jobNameService.getJobByHumanReadableName(scheduledJobDetail.getJobName()).getEnumStyleName(),
                            jobParameterDTOSet)))
                    .toJobParameters();
            JobExecution result = jobLauncher.run(job, jobParameters);
            if (FAILED_STATUSES.contains(result.getStatus())) {
                throw new JobExecutionException(result.getExitStatus().toString());
            }
            return result;
        } finally {
            if (contextInitialized) {
                ThreadLocalContextUtil.reset();
            }
        }
    }

    protected Map<String, org.springframework.batch.core.JobParameter<?>> getJobParameter(ScheduledJobDetail scheduledJobDetail) {
        List<org.apache.fineract.infrastructure.jobs.domain.JobParameter> jobParameterList = jobParameterRepository
                .findJobParametersByJobId(scheduledJobDetail.getId());
        Map<String, JobParameter<?>> jobParameterMap = new HashMap<>();
        for (org.apache.fineract.infrastructure.jobs.domain.JobParameter jobParameter : jobParameterList) {
            jobParameterMap.put(jobParameter.getParameterName(), new JobParameter<>(jobParameter.getParameterValue(), String.class));
        }
        return jobParameterMap;
    }

    protected Map<String, JobParameter<?>> provideCustomJobParameters(String jobName, Set<JobParameterDTO> jobParameterDTOSet) {
        Optional<JobParameterProvider<?>> jobParameterProvider = jobParameterProviders.stream()
                .filter(provider -> provider.canProvideParametersForJob(jobName)).findFirst();
        Map<String, ? extends JobParameter<?>> map = jobParameterProvider
                .map(parameterProvider -> parameterProvider.provide(jobParameterDTOSet)).orElse(Collections.emptyMap());
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
