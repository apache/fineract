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
package org.apache.fineract.cob.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;

/**
 * Runs COB worker setup and teardown without a {@link org.springframework.batch.core.job.flow.FlowStep}. A flow uses
 * {@link org.springframework.batch.core.job.SimpleStepHandler}, which persists the job execution context after every
 * sub-step and causes lock contention on {@code BATCH_JOB_EXECUTION_CONTEXT} during remote partitioning.
 */
@Slf4j
@RequiredArgsConstructor
public class CobWorkerStepListener implements StepExecutionListener {

    private final Tasklet initialisationTasklet;

    private final Tasklet applyLockTasklet;

    private final Tasklet resetContextTasklet;

    @Override
    public void beforeStep(@NonNull final StepExecution stepExecution) {
        runTasklet(initialisationTasklet, stepExecution);
        try {
            runTasklet(applyLockTasklet, stepExecution);
        } catch (Exception e) {
            log.error("Failed to apply lock in beforeStep for '{}'; resetting thread-local context.", stepExecution.getStepName(), e);
            runTasklet(resetContextTasklet, stepExecution);
            throw e;
        }
    }

    @Override
    public ExitStatus afterStep(@NonNull final StepExecution stepExecution) {
        runTasklet(resetContextTasklet, stepExecution);
        return stepExecution.getExitStatus();
    }

    private void runTasklet(final Tasklet tasklet, final StepExecution stepExecution) {
        RepeatStatus status = RepeatStatus.CONTINUABLE;
        while (RepeatStatus.CONTINUABLE.equals(status)) {
            final StepContribution contribution = new StepContribution(stepExecution);
            final ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));
            try {
                status = tasklet.execute(contribution, chunkContext);
                if (status == null) {
                    status = RepeatStatus.FINISHED;
                }
            } catch (Exception exception) {
                throw new IllegalStateException("COB worker step listener failed for step " + stepExecution.getStepName(), exception);
            }
        }
    }
}
