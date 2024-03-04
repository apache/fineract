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
package org.apache.fineract.cob.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.conditions.LoanCOBEnabledCondition;
import org.apache.fineract.cob.data.LoanIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.cob.loan.RetrieveLoanIdService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.TaskExecutorConstant;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.JobParameterRepository;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetailRepository;
import org.apache.fineract.infrastructure.jobs.exception.JobNotFoundException;
import org.apache.fineract.infrastructure.jobs.service.JobStarter;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Conditional(LoanCOBEnabledCondition.class)
public class AsyncLoanCOBExecutorServiceImpl implements AsyncLoanCOBExecutorService {

    private final JobLocator jobLocator;
    private final ScheduledJobDetailRepository scheduledJobDetailRepository;
    private final JobStarter jobStarter;
    private final JobParameterRepository jobParameterRepository;
    private final RetrieveLoanIdService retrieveLoanIdService;

    @Override
    @Async(TaskExecutorConstant.LOAN_COB_CATCH_UP_TASK_EXECUTOR_BEAN_NAME)
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void executeLoanCOBCatchUpAsync(FineractContext context) {
        try {
            ThreadLocalContextUtil.init(context);
            LocalDate cobBusinessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
            List<LoanIdAndLastClosedBusinessDate> loanIdAndLastClosedBusinessDate = retrieveLoanIdService
                    .retrieveLoanIdsOldestCobProcessed(cobBusinessDate);

            LocalDate oldestCOBProcessedDate = !loanIdAndLastClosedBusinessDate.isEmpty()
                    ? loanIdAndLastClosedBusinessDate.get(0).getLastClosedBusinessDate()
                    : cobBusinessDate;
            if (DateUtils.isBefore(oldestCOBProcessedDate, cobBusinessDate)) {
                executeLoanCOBDayByDayUntilCOBBusinessDate(oldestCOBProcessedDate, cobBusinessDate);
            }
        } catch (NoSuchJobException e) {
            // Throwing an error here is useless as it will be swallowed hence it is async method
            log.error("", new JobNotFoundException(LoanCOBConstant.JOB_NAME, e));
        } catch (JobInstanceAlreadyCompleteException | JobRestartException | JobParametersInvalidException
                | JobExecutionAlreadyRunningException | JobExecutionException e) {
            // Throwing an error here is useless as it will be swallowed hence it is async method
            log.error("", e);
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }

    private void executeLoanCOBDayByDayUntilCOBBusinessDate(LocalDate oldestCOBProcessedDate, LocalDate cobBusinessDate)
            throws NoSuchJobException, JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, JobExecutionException {
        Job job = jobLocator.getJob(LoanCOBConstant.JOB_NAME);
        ScheduledJobDetail scheduledJobDetail = scheduledJobDetailRepository.findByJobName(LoanCOBConstant.JOB_HUMAN_READABLE_NAME);
        LocalDate executingBusinessDate = oldestCOBProcessedDate.plusDays(1);
        while (!DateUtils.isAfter(executingBusinessDate, cobBusinessDate)) {
            JobParameterDTO jobParameterDTO = new JobParameterDTO(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME,
                    executingBusinessDate.format(DateTimeFormatter.ISO_DATE));
            JobParameterDTO jobParameterCatchUpDTO = new JobParameterDTO(LoanCOBConstant.IS_CATCH_UP_PARAMETER_NAME, "true");
            Set<JobParameterDTO> jobParameters = new HashSet<>();
            Collections.addAll(jobParameters, jobParameterDTO, jobParameterCatchUpDTO);
            jobStarter.run(job, scheduledJobDetail, jobParameters);
            executingBusinessDate = executingBusinessDate.plusDays(1);
        }
    }
}
