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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.data.LoanCOBParameter;
import org.apache.fineract.cob.data.LoanCOBPartition;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;

@Slf4j
@RequiredArgsConstructor
public class LoanCOBPartitioner implements Partitioner {

    public static final String PARTITION_PREFIX = "partition_";

    private final PropertyService propertyService;
    private final COBBusinessStepService cobBusinessStepService;
    private final RetrieveLoanIdService retrieveLoanIdService;
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    private final Long numberOfDays;

    @Value("#{jobExecutionContext['BusinessDate']}")
    @Setter
    private LocalDate businessDate;
    @Value("#{jobExecutionContext['IS_CATCH_UP']}")
    @Setter
    private Boolean isCatchUp;

    @NotNull
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int partitionSize = propertyService.getPartitionSize(LoanCOBConstant.JOB_NAME);
        Set<BusinessStepNameAndOrder> cobBusinessSteps = cobBusinessStepService.getCOBBusinessSteps(LoanCOBBusinessStep.class,
                LoanCOBConstant.LOAN_COB_JOB_NAME);
        return getPartitions(partitionSize, cobBusinessSteps);
    }

    private Map<String, ExecutionContext> getPartitions(int partitionSize, Set<BusinessStepNameAndOrder> cobBusinessSteps) {
        if (cobBusinessSteps.isEmpty()) {
            stopJobExecution();
            return Map.of();
        }
        StopWatch sw = new StopWatch();
        sw.start();
        List<LoanCOBPartition> loanCOBPartitions = new ArrayList<>(
                retrieveLoanIdService.retrieveLoanCOBPartitions(numberOfDays, businessDate, isCatchUp != null && isCatchUp, partitionSize));
        sw.stop();
        // if there is no loan to be closed, we still would like to create at least one partition

        if (loanCOBPartitions.size() == 0) {
            loanCOBPartitions.add(new LoanCOBPartition(0L, 0L, 1L, 0L));
        }
        log.info(
                "LoanCOBPartitioner found {} loans to be processed as part of COB. {} partitions were created using partition size {}. RetrieveLoanCOBPartitions was executed in {} ms.",
                getLoanCount(loanCOBPartitions), loanCOBPartitions.size(), partitionSize, sw.getTotalTimeMillis());
        return loanCOBPartitions.stream()
                .collect(Collectors.toMap(l -> PARTITION_PREFIX + l.getPageNo(), l -> createNewPartition(cobBusinessSteps, l)));
    }

    private long getLoanCount(List<LoanCOBPartition> loanCOBPartitions) {
        return loanCOBPartitions.stream().map(LoanCOBPartition::getCount).reduce(0L, Long::sum);
    }

    private ExecutionContext createNewPartition(Set<BusinessStepNameAndOrder> cobBusinessSteps, LoanCOBPartition loanCOBPartition) {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.put(LoanCOBConstant.BUSINESS_STEPS, cobBusinessSteps);
        executionContext.put(LoanCOBConstant.LOAN_COB_PARAMETER,
                new LoanCOBParameter(loanCOBPartition.getMinId(), loanCOBPartition.getMaxId()));
        executionContext.put("partition", PARTITION_PREFIX + loanCOBPartition.getPageNo());
        return executionContext;
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
