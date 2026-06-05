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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.data.COBPartition;
import org.apache.fineract.cob.resolver.BusinessDateResolver;
import org.apache.fineract.cob.resolver.CatchUpFlagResolver;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.util.StopWatch;

@Slf4j
@RequiredArgsConstructor
public abstract class CommonPartitioner {

    private final JobOperator jobOperator;
    private final StepExecution stepExecution;
    private final Long numberOfDays;

    /**
     * Retrieves the account-id partitions to be processed by this COB job. Each account type supplies its own query
     * (loans, savings, ...).
     */
    protected abstract List<COBPartition> retrievePartitions(Long numberOfDays, LocalDate businessDate, boolean isCatchUp,
            int partitionSize);

    /**
     * Execution-context key under which the per-partition {@link COBParameter} (min/max account id) is stored. Defaults
     * to the loan key; other account types override it so reader/tasklet pick up the right partition bounds.
     */
    protected String getCobParameterKey() {
        return COBConstant.COB_PARAMETER;
    }

    public Map<String, ExecutionContext> getPartitions(int partitionSize, Set<BusinessStepNameAndOrder> cobBusinessSteps) {
        if (cobBusinessSteps.isEmpty()) {
            stopJobExecution();
            return Map.of();
        }
        LocalDate businessDate = BusinessDateResolver.resolve(stepExecution);
        boolean isCatchUp = CatchUpFlagResolver.resolve(stepExecution);
        StopWatch sw = new StopWatch();
        sw.start();
        List<COBPartition> partitions = new ArrayList<>(retrievePartitions(numberOfDays, businessDate, isCatchUp, partitionSize));
        sw.stop();
        // if there is no account to be closed, we still would like to create at least one partition

        if (partitions.isEmpty()) {
            partitions.add(new COBPartition(0L, 0L, 1L, 0L));
        }
        log.info(
                "{} found {} accounts to be processed as part of COB. {} partitions were created using partition size {}. Partition retrieval was executed in {} ms.",
                getClass().getName(), getAccountCount(partitions), partitions.size(), partitionSize, sw.getTotalTimeMillis());
        return partitions.stream().collect(Collectors.toMap(l -> COBConstant.PARTITION_PREFIX + l.getPageNo(),
                l -> createExecutionContextForPartition(cobBusinessSteps, l, businessDate, isCatchUp)));
    }

    private long getAccountCount(List<COBPartition> cobPartitions) {
        return cobPartitions.stream().map(COBPartition::getCount).reduce(0L, Long::sum);
    }

    private ExecutionContext createExecutionContextForPartition(Set<BusinessStepNameAndOrder> cobBusinessSteps, COBPartition cobPartition,
            LocalDate businessDate, boolean isCatchUp) {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.put(COBConstant.BUSINESS_STEPS, cobBusinessSteps);
        executionContext.put(getCobParameterKey(), new COBParameter(cobPartition.getMinId(), cobPartition.getMaxId()));
        executionContext.put(COBConstant.PARTITION_KEY, COBConstant.PARTITION_PREFIX + cobPartition.getPageNo());
        executionContext.put(COBConstant.BUSINESS_DATE_PARAMETER_NAME, businessDate.toString());
        executionContext.put(COBConstant.IS_CATCH_UP_PARAMETER_NAME, Boolean.toString(isCatchUp));
        return executionContext;
    }

    private void stopJobExecution() {
        Long jobId = stepExecution.getJobExecution().getId();
        try {
            jobOperator.stop(jobId);
        } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
            log.error("There is no running execution for the given execution ID. Execution ID: {}", jobId);
            throw new RuntimeException(e);
        }

    }
}
