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
package org.apache.fineract.infrastructure.springbatch.messagehandler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "fineract.mode.batch-worker-enabled", havingValue = "true")
public class StepExecutionRequestHandler {

    private final JobRepository jobRepository;
    private final StepLocator stepLocator;
    private final JobExplorer jobExplorer;

    public void handle(StepExecutionRequest request) {

        Long jobExecutionId = request.getJobExecutionId();
        Long stepExecutionId = request.getStepExecutionId();
        String stepName = request.getStepName();

        StepExecution stepExecution = jobExplorer.getStepExecution(jobExecutionId, stepExecutionId);
        if (stepExecution == null) {
            throw new IllegalStateException("stepExecution cannot be null");
        }

        /*
         * no need to check the status of the StepExecution because only a single worker can work on a particular
         * partition due to the fact that a JMS queue is used and not a topic (i.e. only one consumer receives a single
         * message)
         */
        Step step = stepLocator.getStep(stepName);
        try {
            step.execute(stepExecution);
        } catch (JobInterruptedException e) {
            // based on org.springframework.batch.core.step.AbstractStep.determineBatchStatus
            stepExecution.addFailureException(e);
            stepExecution.setStatus(BatchStatus.STOPPED);
        } catch (OptimisticLockingFailureException e) {
            // no need to do anything, just another worker picked up a partition that's being processed
            // since we're using queues instead of topics, only a single worker should receive the msg
        } catch (Exception e) {
            stepExecution.addFailureException(e);
            stepExecution.setStatus(BatchStatus.FAILED);
        } finally {
            jobRepository.update(stepExecution);
        }
    }
}
