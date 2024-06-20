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

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.jobs.data.partitionedjobs.PartitionedJob;
import org.apache.fineract.infrastructure.jobs.domain.JobExecutionRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
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
    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;

    private static final String EXIT_CODE_VALUE = "FAILED_AFTER_MANAGER_RESTARTED";

    @Override
    public void resumeStuckJob(String jobName) {
        if (areThereStuckJobs(jobName)) {
            // Exit Status (Tag) to know If the Job and Steps were marked as Failed
            final ExitStatus cleanupTaskExitStatus = new ExitStatus(EXIT_CODE_VALUE);
            final List<Long> stuckJobIds = getStuckJobIds(jobName);

            // Mark as Failed the Steps Executions (If exists) after Restart
            stuckJobIds.forEach(stuckJobId -> verifyBatchStepsExecutions(stuckJobId, cleanupTaskExitStatus));

            // Wait the other Job Partitions (If exists) to be completed
            if (isPartitionedJob(jobName)) {
                restartPartitionedJobs(jobName, stuckJobIds);
            } else {
                restartTaskletJobs(stuckJobIds);
            }

            // Mark as Failed the Job Executions (If exists) after Restart
            stuckJobIds.forEach(stuckJobId -> verifyBatchJob(stuckJobId, cleanupTaskExitStatus));
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
        Long notCompletedPartitions = jobExecutionRepository.getNotCompletedPartitionsCount(stuckJobId, partitionerStepName,
                EXIT_CODE_VALUE);
        return notCompletedPartitions == 0L;
    }

    private void verifyBatchJob(long executionId, ExitStatus exitStatus) {
        JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
        if (isRunning(jobExecution)) {
            log.info("Job Execution {} will be mark as Failed", executionId);
            final LocalDateTime now = DateUtils.getLocalDateTimeOfSystem();

            // Mark the Job Execution as Failed
            jobExecution.setStatus(BatchStatus.FAILED);
            jobExecution.setExitStatus(exitStatus);
            jobExecution.setEndTime(now);
            jobRepository.update(jobExecution);
        }
    }

    private void verifyBatchStepsExecutions(long executionId, ExitStatus exitStatus) {
        final JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
        if (isRunning(jobExecution)) {
            log.info("Step Execution {} will be mark as Failed", executionId);
            final LocalDateTime now = DateUtils.getLocalDateTimeOfSystem();

            // Mark the Step Executions as Failed
            for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                if (isRunning(stepExecution)) {
                    stepExecution.setStatus(BatchStatus.FAILED);
                    stepExecution.setExitStatus(exitStatus);
                    stepExecution.setEndTime(now);
                    jobRepository.update(stepExecution);
                }
            }
        }
    }

    private boolean isRunning(JobExecution jobExecution) {
        switch (jobExecution.getStatus()) {
            case STARTED:
            case STARTING:
            case STOPPING:
            case UNKNOWN:
                return true;

            default:
                return jobExecution.getEndTime() == null;
        }
    }

    private boolean isRunning(StepExecution stepExecution) {
        switch (stepExecution.getStatus()) {
            case STARTED:
            case STARTING:
            case STOPPING:
            case UNKNOWN:
                return true;

            default:
                return stepExecution.getEndTime() == null;
        }
    }

}
