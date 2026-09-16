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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.account.data.StandingInstructionPartition;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.lang.NonNull;

/**
 * Splits the instructions due today into partitions that workers process in parallel.
 *
 * <p>
 * Partitions are cut over distinct source accounts rather than over instructions, so every instruction debiting a given
 * account stays in one partition and runs sequentially there. Two partitions therefore never contend on the same
 * source-account row. Instructions crediting the same destination can still land in different partitions; that
 * contention is left to the step's retry, being far less common and much shorter-lived than the debit-side check.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class StandingInstructionPartitioner implements Partitioner {

    private final PropertyService propertyService;
    private final StandingInstructionReadPlatformService standingInstructionReadPlatformService;

    @NonNull
    @Override
    public Map<String, ExecutionContext> partition(final int gridSize) {
        final int partitionSize = propertyService.getPartitionSize(ExecuteStandingInstructionsConstant.JOB_NAME);
        final List<StandingInstructionPartition> partitions = new ArrayList<>(
                standingInstructionReadPlatformService.retrieveDuePartitions(StandingInstructionStatus.ACTIVE.getValue(), partitionSize));
        if (partitions.isEmpty()) {
            // A step needs at least one partition to run at all, so hand it an account range that matches nothing
            // rather than an empty map: the run then completes normally having done no work.
            log.info("No standing instruction is due today");
            partitions.add(new StandingInstructionPartition(0L, 0L, 0L, 0L));
        }
        log.info("{} standing instructions are due, split into {} partitions of at most {} source accounts",
                partitions.stream().mapToLong(StandingInstructionPartition::instructionCount).sum(), partitions.size(), partitionSize);
        return partitions.stream().collect(Collectors.toMap(
                partition -> ExecuteStandingInstructionsConstant.PARTITION_PREFIX + partition.pageNumber(), this::executionContextOf));
    }

    private ExecutionContext executionContextOf(final StandingInstructionPartition partition) {
        final ExecutionContext executionContext = new ExecutionContext();
        executionContext.putLong(ExecuteStandingInstructionsConstant.MIN_ACCOUNT_KEY, partition.minAccountKey());
        executionContext.putLong(ExecuteStandingInstructionsConstant.MAX_ACCOUNT_KEY, partition.maxAccountKey());
        executionContext.putString(ExecuteStandingInstructionsConstant.PARTITION_KEY,
                ExecuteStandingInstructionsConstant.PARTITION_PREFIX + partition.pageNumber());
        return executionContext;
    }
}
