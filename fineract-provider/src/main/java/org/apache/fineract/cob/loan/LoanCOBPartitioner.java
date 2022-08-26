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
import org.apache.fineract.cob.COBPropertyService;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.jetbrains.annotations.Nullable;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

@Slf4j
@RequiredArgsConstructor
public class LoanCOBPartitioner implements Partitioner {

    private final COBPropertyService cobPropertyService;
    private final COBBusinessStepService cobBusinessStepService;
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;
    private final RetrieveLoanIdService retrieveLoanIdService;

    private static final String PARTITION_PREFIX = "partition_";
    private static final String JOB_NAME = "LOAN_COB";
    private static final String LOAN_COB_JOB_NAME = "LOAN_CLOSE_OF_BUSINESS";

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int partitionSize = cobPropertyService.getPartitionSize(JOB_NAME);
        TreeMap<Long, String> cobBusinessStepMap = cobBusinessStepService.getCOBBusinessStepMap(LoanCOBBusinessStep.class,
                LOAN_COB_JOB_NAME);
        if (cobBusinessStepMap.isEmpty()) {
            return stopJobExecution();
        }
        return getPartitions(partitionSize, cobBusinessStepMap);
    }

    private Map<String, ExecutionContext> getPartitions(int partitionSize, TreeMap<Long, String> cobBusinessStepMap) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        List<Integer> allNonClosedLoanIds = retrieveLoanIdService.retrieveLoanIds();
        if (allNonClosedLoanIds.isEmpty()) {
            return stopJobExecution();
        }
        int partitionIndex = 0;
        createNewPartition(partitions, partitionIndex, cobBusinessStepMap);
        for (Integer allNonClosedLoanId : allNonClosedLoanIds) {
            if (partitions.get(PARTITION_PREFIX + partitionIndex).size() == partitionSize) {
                partitionIndex++;
                createNewPartition(partitions, partitionIndex, cobBusinessStepMap);
            }
            String key = PARTITION_PREFIX + partitionIndex;
            ExecutionContext executionContext = partitions.get(key);
            List<Integer> data = (List<Integer>) executionContext.get("loanIds");
            data.add(allNonClosedLoanId);
        }
        return partitions;
    }

    private void createNewPartition(Map<String, ExecutionContext> partitions, int partitionIndex,
            TreeMap<Long, String> cobBusinessStepMap) {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.put("loanIds", new ArrayList<Integer>());
        executionContext.put("BusinessStepMap", cobBusinessStepMap);
        partitions.put(PARTITION_PREFIX + partitionIndex, executionContext);
    }

    @Nullable
    private Map<String, ExecutionContext> stopJobExecution() {
        Set<JobExecution> runningJobExecutions = jobExplorer.findRunningJobExecutions(JobName.LOAN_COB.name());
        for (JobExecution jobExecution : runningJobExecutions) {
            try {
                jobOperator.stop(jobExecution.getId());
            } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
                log.error("There is no running execution for the given execution ID. Execution ID: {}", jobExecution.getId());
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
