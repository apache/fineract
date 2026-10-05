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
package org.apache.fineract.cob.common;

import java.util.Set;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.partition.support.SimpleStepExecutionSplitter;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;

/** Persists the manager's partition metadata before any remote worker can start. */
public class COBStepExecutionSplitter extends SimpleStepExecutionSplitter {

    private final JobRepository jobRepository;

    public COBStepExecutionSplitter(JobRepository jobRepository, String workerStepName, Partitioner partitioner) {
        super(jobRepository, workerStepName, partitioner);
        this.jobRepository = jobRepository;
    }

    @Override
    public Set<StepExecution> split(StepExecution stepExecution, int gridSize) throws JobExecutionException {
        Set<StepExecution> partitions = super.split(stepExecution, gridSize);
        // The default splitter saves GRID_SIZE before invoking the partitioner. Save its additions too, after
        // all worker contexts exist and before the partition handler dispatches requests. Waiting for the manager
        // step to finish loses partitionCount if the manager dies while workers are processing.
        jobRepository.updateExecutionContext(stepExecution);
        return partitions;
    }
}
