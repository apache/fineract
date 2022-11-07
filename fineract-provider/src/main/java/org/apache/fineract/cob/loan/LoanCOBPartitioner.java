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
package org.apache.fineract.cob.loan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.util.CollectionUtils;

@Slf4j
@RequiredArgsConstructor
public class LoanCOBPartitioner implements Partitioner {

    public static final String PARTITION_PREFIX = "partition_";

    private final PropertyService propertyService;
    private final COBBusinessStepService cobBusinessStepService;
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    private final List<Long> loanIds;

    @NotNull
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int partitionSize = propertyService.getPartitionSize(LoanCOBConstant.JOB_NAME);
        TreeMap<Long, String> cobBusinessStepMap = cobBusinessStepService.getCOBBusinessStepMap(LoanCOBBusinessStep.class,
                LoanCOBConstant.LOAN_COB_JOB_NAME);
        if (cobBusinessStepMap.isEmpty()) {
            stopJobExecution();
            return Map.of();
        }
        return getPartitions(partitionSize, cobBusinessStepMap);
    }

    private Map<String, ExecutionContext> getPartitions(int partitionSize, TreeMap<Long, String> cobBusinessStepMap) {
        Map<String, ExecutionContext> partitions = new HashMap<>();

        if (CollectionUtils.isEmpty(loanIds)) {
            stopJobExecution();
            return Map.of();
        }
        int partitionIndex = 1;
        int remainingSpace = 0;
        createNewPartition(partitions, partitionIndex, cobBusinessStepMap);
        for (Long loanId : loanIds) {
            if (remainingSpace == partitionSize) {
                partitionIndex++;
                createNewPartition(partitions, partitionIndex, cobBusinessStepMap);
                remainingSpace = 0;
            }
            String key = PARTITION_PREFIX + partitionIndex;
            ExecutionContext executionContext = partitions.get(key);
            List<Long> data = (List<Long>) executionContext.get(LoanCOBConstant.LOAN_IDS);
            data.add(loanId);
            remainingSpace++;
        }
        return partitions;
    }

    private void createNewPartition(Map<String, ExecutionContext> partitions, int partitionIndex,
            TreeMap<Long, String> cobBusinessStepMap) {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.put(LoanCOBConstant.LOAN_IDS, new ArrayList<Long>());
        executionContext.put(LoanCOBConstant.BUSINESS_STEP_MAP, cobBusinessStepMap);
        executionContext.put("partition", PARTITION_PREFIX + partitionIndex);
        partitions.put(PARTITION_PREFIX + partitionIndex, executionContext);
    }

    private void stopJobExecution() {
        Set<JobExecution> runningJobExecutions = jobExplorer.findRunningJobExecutions(JobName.LOAN_COB.name());
        for (JobExecution jobExecution : runningJobExecutions) {
            try {
                jobOperator.stop(jobExecution.getId());
            } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
                log.error("There is no running execution for the given execution ID. Execution ID: {}", jobExecution.getId());
                throw new RuntimeException(e);
            }
        }
    }
}
