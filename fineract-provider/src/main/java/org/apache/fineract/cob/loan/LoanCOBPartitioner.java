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
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.jetbrains.annotations.NotNull;
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

    private final LoanRepository loanRepository;
    private final COBPropertyService cobPropertyService;
    private final COBBusinessStepService cobBusinessStepService;
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    private static final String PARTITION_PREFIX = "partition";
    private static final String JOB_NAME = "LOAN_COB";
    private static final String LOAN_COB_JOB_NAME = "LOAN_CLOSE_OF_BUSINESS";

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int partitionCount = cobPropertyService.getPartitionSize(JOB_NAME);
        TreeMap<Long, String> cobBusinessStepMap = cobBusinessStepService.getCOBBusinessStepMap(LoanCOBBusinessStep.class,
                LOAN_COB_JOB_NAME);
        if (cobBusinessStepMap.isEmpty()) {
            return stopJobExecution();
        }
        return getPartitions(partitionCount, cobBusinessStepMap);
    }

    @NotNull
    private Map<String, ExecutionContext> getPartitions(int partitionCount, TreeMap<Long, String> cobBusinessStepMap) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        for (int i = 0; i < partitionCount; i++) {
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.put("loanIds", new ArrayList<Integer>());
            executionContext.put("BusinessStepMap", cobBusinessStepMap);
            partitions.put(PARTITION_PREFIX + i, executionContext);
        }

        List<Integer> allNonClosedLoanIds = loanRepository.findAllNonClosedLoanIds();
        for (int i = 0; i < allNonClosedLoanIds.size(); i++) {
            String key = PARTITION_PREFIX + (i % partitionCount);
            ExecutionContext executionContext = partitions.get(key);
            List<Integer> data = (List<Integer>) executionContext.get("loanIds");
            data.add(allNonClosedLoanIds.get(i));
        }
        return partitions;
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
