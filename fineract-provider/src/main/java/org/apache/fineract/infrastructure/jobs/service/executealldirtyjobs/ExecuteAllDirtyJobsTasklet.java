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
package org.apache.fineract.infrastructure.jobs.service.executealldirtyjobs;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetailRepository;
import org.apache.fineract.infrastructure.jobs.service.JobRegisterService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ExecuteAllDirtyJobsTasklet implements Tasklet {

    private final JobRegisterService jobRegisterService;
    private final ScheduledJobDetailRepository scheduledJobDetailsRepository;
    private final FineractProperties fineractProperties;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<ScheduledJobDetail> jobDetails = scheduledJobDetailsRepository.findAllMismatchedJobs(true);

        for (ScheduledJobDetail scheduledJobDetail : jobDetails) {
            if (scheduledJobDetail.getNodeId().toString().equals(fineractProperties.getNodeId())) {
                jobRegisterService.executeJobWithParameters(scheduledJobDetail.getId(), null);
            }
        }
        return RepeatStatus.FINISHED;
    }
}
