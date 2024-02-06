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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.JobIsNotFoundOrNotEnabledException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetail;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobSchedulerServiceImpl implements ApplicationListener<ContextRefreshedEvent> {

    private final FineractProperties fineractProperties;
    private final SchedularWritePlatformService schedularWritePlatformService;
    private final TenantDetailsService tenantDetailsService;
    private final JobRegisterService jobRegisterService;
    private final BusinessDateReadPlatformService businessDateReadPlatformService;

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // If the instance is not Batch Enabled will not load the Jobs
        if (!fineractProperties.getMode().isBatchManagerEnabled()) {
            log.warn("Batch job scheduling is disabled since this instance is not a batch manager");
            return;
        }
        final List<FineractPlatformTenant> allTenants = tenantDetailsService.findAllTenants();
        for (final FineractPlatformTenant tenant : allTenants) {
            ThreadLocalContextUtil.setTenant(tenant);
            HashMap<BusinessDateType, LocalDate> businessDates = businessDateReadPlatformService.getBusinessDates();
            ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
            ThreadLocalContextUtil.setBusinessDates(businessDates);
            final List<ScheduledJobDetail> scheduledJobDetails = schedularWritePlatformService
                    .retrieveAllJobs(fineractProperties.getNodeId());
            for (final ScheduledJobDetail jobDetails : scheduledJobDetails) {
                try {
                    jobRegisterService.scheduleJob(jobDetails);
                } catch (JobIsNotFoundOrNotEnabledException e) {
                    log.warn("{}", e.getMessage());
                }
                jobDetails.setTriggerMisfired(false);
                schedularWritePlatformService.saveOrUpdate(jobDetails);
            }
            final SchedulerDetail schedulerDetail = schedularWritePlatformService.retriveSchedulerDetail();
            if (schedulerDetail.isResetSchedulerOnBootup()) {
                schedulerDetail.setSuspended(false);
                schedularWritePlatformService.updateSchedulerDetail(schedulerDetail);
            }
            ThreadLocalContextUtil.reset();
        }
        log.info("Scheduling batch jobs has finished");
    }
}
