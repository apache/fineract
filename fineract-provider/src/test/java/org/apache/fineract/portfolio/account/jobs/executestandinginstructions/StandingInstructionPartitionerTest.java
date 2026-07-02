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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.account.data.StandingInstructionPartition;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
class StandingInstructionPartitionerTest {

    private static final int ACTIVE = StandingInstructionStatus.ACTIVE.getValue();

    @Mock
    private PropertyService propertyService;
    @Mock
    private StandingInstructionReadPlatformService readService;

    private StandingInstructionPartitioner partitioner;

    @BeforeEach
    void setUp() {
        when(propertyService.getPartitionSize(ExecuteStandingInstructionsConstant.JOB_NAME)).thenReturn(100);
        partitioner = new StandingInstructionPartitioner(propertyService, readService);
    }

    @Test
    void eachPartitionCarriesItsOwnAccountRange() {
        when(readService.retrieveDuePartitions(ACTIVE, 100)).thenReturn(List.of(new StandingInstructionPartition(1L, 400L, 0L, 100L),
                new StandingInstructionPartition(401L, 900L, 1L, 60L)));

        final Map<String, ExecutionContext> partitions = partitioner.partition(2);

        assertThat(partitions).containsOnlyKeys("partition_0", "partition_1");
        assertThat(partitions.get("partition_0").getLong(ExecuteStandingInstructionsConstant.MIN_ACCOUNT_KEY)).isEqualTo(1L);
        assertThat(partitions.get("partition_0").getLong(ExecuteStandingInstructionsConstant.MAX_ACCOUNT_KEY)).isEqualTo(400L);
        assertThat(partitions.get("partition_1").getLong(ExecuteStandingInstructionsConstant.MIN_ACCOUNT_KEY)).isEqualTo(401L);
        assertThat(partitions.get("partition_1").getLong(ExecuteStandingInstructionsConstant.MAX_ACCOUNT_KEY)).isEqualTo(900L);
    }

    /**
     * A step with no partitions has nothing to run and cannot report a clean finish, so a day with nothing due still
     * gets one partition — over an account range that matches nothing.
     */
    @Test
    void aDayWithNothingDueStillProducesOnePartition() {
        when(readService.retrieveDuePartitions(ACTIVE, 100)).thenReturn(List.of());

        final Map<String, ExecutionContext> partitions = partitioner.partition(1);

        assertThat(partitions).containsOnlyKeys("partition_0");
        assertThat(partitions.get("partition_0").getLong(ExecuteStandingInstructionsConstant.MIN_ACCOUNT_KEY)).isZero();
        assertThat(partitions.get("partition_0").getLong(ExecuteStandingInstructionsConstant.MAX_ACCOUNT_KEY)).isZero();
    }
}
