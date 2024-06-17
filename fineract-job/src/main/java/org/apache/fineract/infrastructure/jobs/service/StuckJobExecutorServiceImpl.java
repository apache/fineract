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

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.jobs.data.partitionedjobs.PartitionedJob;
import org.apache.fineract.infrastructure.jobs.domain.JobExecutionRepository;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class StuckJobExecutorServiceImpl implements StuckJobExecutorService {

    private final JobExecutionRepository jobExecutionRepository;
    private final TransactionTemplate transactionTemplate;
    private final JobOperator jobOperator;

    @Override
    public void resumeStuckJob(String jobName) {
        List<Long> stuckJobIds = getStuckJobIds(jobName);
        if (isPartitionedJob(jobName) && areThereStuckJobs(jobName)) {
            restartPartitionedJobs(jobName, stuckJobIds);
        } else {
            restartTaskletJobs(stuckJobIds);
        }
    }

    private void restartTaskletJobs(List<Long> stuckJobIds) {
        stuckJobIds.forEach(this::handleStuckTaskletJob);
    }

    private void handleStuckTaskletJob(Long stuckJobId) {
        try {
            jobOperator.restart(stuckJobId);
        } catch (Exception e) {
            throw new RuntimeException("Exception while handling a stuck job", e);
        }
    }

    private void restartPartitionedJobs(String jobName, List<Long> stuckJobIds) {
        stuckJobIds.forEach(stuckJobId -> handleStuckPartitionedJob(stuckJobId, getPartitionerStepName(jobName)));
    }

    private boolean isPartitionedJob(String jobName) {
        return PartitionedJob.existsByJobName(jobName);
    }

    private String getPartitionerStepName(String name) {
        return PartitionedJob.valueOf(name).getPartitionerStepName();
    }

    private boolean areThereStuckJobs(String jobName) {
        Long stuckJobCount = jobExecutionRepository.getStuckJobCountByJobName(jobName);
        return stuckJobCount != 0L;
    }

    private List<Long> getStuckJobIds(String jobName) {
        return jobExecutionRepository.getStuckJobIdsByJobName(jobName);
    }

    private void handleStuckPartitionedJob(Long stuckJobId, String partitionerStepName) {
        try {
            waitUntilAllPartitionsFinished(stuckJobId, partitionerStepName);
            transactionTemplate.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    jobExecutionRepository.updateJobStatusToFailed(stuckJobId, partitionerStepName);
                }
            });
            jobOperator.restart(stuckJobId);
        } catch (Exception e) {
            throw new RuntimeException("Exception while handling a stuck job", e);
        }
    }

    private void waitUntilAllPartitionsFinished(Long stuckJobId, String partitionerStepName) throws InterruptedException {
        while (!areAllPartitionsCompleted(stuckJobId, partitionerStepName)) {
            log.info("Sleeping for a second to wait for the partitions to complete for job {}", stuckJobId);
            Thread.sleep(1000);
        }
    }

    private boolean areAllPartitionsCompleted(Long stuckJobId, String partitionerStepName) {
        Long notCompletedPartitions = jobExecutionRepository.getNotCompletedPartitionsCount(stuckJobId, partitionerStepName);
        return notCompletedPartitions == 0L;
    }
}
