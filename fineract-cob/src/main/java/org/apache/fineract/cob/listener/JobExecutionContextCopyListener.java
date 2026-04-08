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

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;

/**
 * {@link StepExecutionListener} to copy values from Job execution context into Step execution context.
 */
@Slf4j
public class JobExecutionContextCopyListener implements StepExecutionListener {

    private final List<String> stepExecutionKeys;

    public JobExecutionContextCopyListener(List<String> stepExecutionKeys) {
        this.stepExecutionKeys = stepExecutionKeys;
    }

    /**
     * Method to copy values from Job execution context into Step execution context before step execution.
     *
     * @param stepExecution
     *            the step to be executed.
     */
    @Override
    public void beforeStep(final StepExecution stepExecution) {
        log.debug("Before step: copying job execution context to step [{}]", stepExecution.getStepName());

        final ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
        final ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();

        jobExecutionContext.entrySet().forEach(jobExecutionContextEntry -> {
            if (stepExecutionKeys.contains(jobExecutionContextEntry.getKey())
                    && BooleanUtils.isFalse(stepExecutionContext.containsKey(jobExecutionContextEntry.getKey()))) {
                stepExecutionContext.put(jobExecutionContextEntry.getKey(), jobExecutionContextEntry.getValue());
            }
        });
    }
}
