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

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.conditions.LoanCOBEnabledCondition;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.infrastructure.core.config.TaskExecutorConstant;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetailRepository;
import org.apache.fineract.infrastructure.jobs.service.JobStarter;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Conditional(LoanCOBEnabledCondition.class)
public class AsyncLoanCOBExecutorServiceImpl extends AsyncCommonCOBExecutorService implements AsyncLoanCOBExecutorService {

    public AsyncLoanCOBExecutorServiceImpl(JobLocator jobLocator, ScheduledJobDetailRepository scheduledJobDetailRepository,
            JobStarter jobStarter, RetrieveLoanIdService retrieveIdService) {
        super(jobLocator, scheduledJobDetailRepository, jobStarter, retrieveIdService);
    }

    @Override
    @Async(TaskExecutorConstant.LOAN_COB_CATCH_UP_TASK_EXECUTOR_BEAN_NAME)
    public void executeLoanCOBCatchUpAsync(FineractContext context) {
        super.executeLoanCOBCatchUpAsync(context);
    }

    @Override
    public String getJobName() {
        return LoanCOBConstant.JOB_NAME;
    }

    @Override
    public String getJobHumanReadableName() {
        return LoanCOBConstant.JOB_HUMAN_READABLE_NAME;
    }
}
