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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.data.COBPartition;
import org.apache.fineract.cob.resolver.BusinessDateResolver;
import org.apache.fineract.cob.resolver.CatchUpFlagResolver;
import org.apache.fineract.cob.service.RetrieveIdService;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.partition.PartitionNameProvider;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.util.StopWatch;

@Slf4j
@RequiredArgsConstructor
public abstract class CommonPartitioner implements PartitionNameProvider {

    /**
     * Number of partitions created by the last successful {@link #getPartitions(int, Set)} call, stored on the manager
     * step's execution context so that {@link #getPartitionNames(int)} can reproduce the original names on restart.
     */
    static final String PARTITION_COUNT_KEY = "partitionCount";

    private final JobOperator jobOperator;
    private final StepExecution stepExecution;
    private final Long numberOfDays;
    private final RetrieveIdService retrieveIdService;

    public Map<String, ExecutionContext> getPartitions(int partitionSize, Set<BusinessStepNameAndOrder> cobBusinessSteps) {
        if (cobBusinessSteps.isEmpty()) {
            stopJobExecution();
            return Map.of();
        }
        LocalDate businessDate = BusinessDateResolver.resolve(stepExecution);
        boolean isCatchUp = CatchUpFlagResolver.resolve(stepExecution);
        StopWatch sw = new StopWatch();
        sw.start();
        List<COBPartition> partitions = new ArrayList<>(
                retrieveIdService.retrieveLoanCOBPartitions(numberOfDays, businessDate, isCatchUp, partitionSize));
        sw.stop();
        // if there is no loan to be closed, we still would like to create at least one partition

        if (partitions.isEmpty()) {
            partitions.add(new COBPartition(0L, 0L, 1L, 0L));
        }
        log.info(
                "{}} found {} loans to be processed as part of COB. {} partitions were created using partition size {}. RetrieveLoanCOBPartitions was executed in {} ms.",
                getClass().getName(), getLoanCount(partitions), partitions.size(), partitionSize, sw.getTotalTimeMillis());
        // Remembered so that a restart reuses these names instead of re-deriving them from a shrunken re-query - see
        // getPartitionNames(int). Persisted with the manager step's execution context by AbstractStep after doExecute.
        stepExecution.getExecutionContext().putInt(PARTITION_COUNT_KEY, partitions.size());

        return partitions.stream().collect(Collectors.toMap(l -> COBConstant.PARTITION_PREFIX + l.getPageNo(),
                l -> createExecutionContextForPartition(cobBusinessSteps, l, businessDate, isCatchUp)));
    }

    /**
     * Reproduces the partition names of the run being restarted.
     * <p>
     * {@code SimpleStepExecutionSplitter} matches partitions by name. Without this method it falls back to calling
     * {@code partition(..)} again, which re-queries the loans still behind - and because the completed partitions no
     * longer match that query, the page count shrinks and the names shift. A partition that failed at an index at or
     * above the new count is then never iterated, every name that does match resolves to an already-COMPLETED execution
     * and is skipped, and the restart reports COMPLETED having processed nothing. Those loans keep their old
     * {@code last_closed_business_date}, and since COB selects on exact equality with {@code COB_DATE - 1} they are
     * never picked up by a later run either - only by the manual catch-up endpoint.
     * <p>
     * Returning the original names keeps the failed partition in the split, where the splitter re-runs it (its last
     * execution is FAILED, so {@code shouldStart} returns true) with the id range preserved from its stored execution
     * context.
     *
     * @param gridSize
     *            ignored - the partition count is data-driven, not grid-driven
     */
    @Override
    public Collection<String> getPartitionNames(int gridSize) {
        ExecutionContext managerContext = stepExecution.getExecutionContext();
        if (!managerContext.containsKey(PARTITION_COUNT_KEY)) {
            // Only reachable for a step execution written before this key existed. Re-deriving the names here would
            // silently under-process, so fail loudly instead and let the operator run the catch-up endpoint.
            throw new IllegalStateException("Cannot restart partitioned step '" + stepExecution.getStepName()
                    + "': the partition count is missing from its execution context, so the original partition names "
                    + "cannot be reproduced. Run the loan COB catch-up endpoint instead of restarting this job.");
        }
        int partitionCount = managerContext.getInt(PARTITION_COUNT_KEY);
        return IntStream.range(0, partitionCount).mapToObj(pageNo -> COBConstant.PARTITION_PREFIX + pageNo).toList();
    }

    private long getLoanCount(List<COBPartition> loanCOBPartitions) {
        return loanCOBPartitions.stream().map(COBPartition::getCount).reduce(0L, Long::sum);
    }

    private ExecutionContext createExecutionContextForPartition(Set<BusinessStepNameAndOrder> cobBusinessSteps,
            COBPartition loanCOBPartition, LocalDate businessDate, boolean isCatchUp) {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.put(COBConstant.BUSINESS_STEPS, cobBusinessSteps);
        executionContext.put(COBConstant.COB_PARAMETER, new COBParameter(loanCOBPartition.getMinId(), loanCOBPartition.getMaxId()));
        executionContext.put(COBConstant.PARTITION_KEY, COBConstant.PARTITION_PREFIX + loanCOBPartition.getPageNo());
        executionContext.put(COBConstant.BUSINESS_DATE_PARAMETER_NAME, businessDate.toString());
        executionContext.put(COBConstant.IS_CATCH_UP_PARAMETER_NAME, Boolean.toString(isCatchUp));
        return executionContext;
    }

    private void stopJobExecution() {
        JobExecution jobExecution = stepExecution.getJobExecution();
        try {
            jobOperator.stop(jobExecution);
        } catch (JobExecutionNotRunningException e) {
            log.error("There is no running execution for the given execution ID. Execution ID: {}", jobExecution.getId());
            throw new RuntimeException(e);
        }

    }
}
