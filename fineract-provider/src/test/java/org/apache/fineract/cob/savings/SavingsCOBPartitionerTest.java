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
package org.apache.fineract.cob.savings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.data.COBPartition;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

/**
 * Savings counterpart of {@code LoanCOBPartitionerTest} and {@code LoanCOBPartitionerRestartNamingTest}.
 */
@ExtendWith(MockitoExtension.class)
class SavingsCOBPartitionerTest {

    private static final Set<BusinessStepNameAndOrder> BUSINESS_STEPS = Set.of(new BusinessStepNameAndOrder("Business step", 1L));
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2024, 6, 1);
    private static final int PARTITION_SIZE = 5;

    @Mock
    private PropertyService propertyService;
    @Mock
    private COBBusinessStepService cobBusinessStepService;
    @Mock
    private RetrieveSavingsIdService retrieveSavingsIdService;
    @Mock
    private JobOperator jobOperator;
    @Mock
    private StepExecution stepExecution;
    @Mock
    private JobExecution jobExecution;

    @Test
    void shouldCreateOnePartitionPerPageReturnedByTheSavingsIdService() {
        givenBusinessSteps(BUSINESS_STEPS);
        givenJobContext(BUSINESS_DATE, false);
        when(stepExecution.getExecutionContext()).thenReturn(new ExecutionContext());
        when(retrieveSavingsIdService.retrieveSavingsCOBPartitions(SavingsCOBConstant.NUMBER_OF_DAYS_BEHIND, BUSINESS_DATE, false,
                PARTITION_SIZE)).thenReturn(List.of(new COBPartition(1L, 10L, 1L, 5L), new COBPartition(11L, 20L, 2L, 4L)));

        final Map<String, ExecutionContext> partitions = partitioner().partition(1);

        assertThat(partitions).containsOnlyKeys(COBConstant.PARTITION_PREFIX + 1, COBConstant.PARTITION_PREFIX + 2);
        validatePartition(partitions, 1, 1L, 10L, "false");
        validatePartition(partitions, 2, 11L, 20L, "false");
    }

    @Test
    void shouldPropagateTheCatchUpFlagToTheQueryAndThePartitions() {
        givenBusinessSteps(BUSINESS_STEPS);
        givenJobContext(BUSINESS_DATE, true);
        when(stepExecution.getExecutionContext()).thenReturn(new ExecutionContext());
        when(retrieveSavingsIdService.retrieveSavingsCOBPartitions(SavingsCOBConstant.NUMBER_OF_DAYS_BEHIND, BUSINESS_DATE, true,
                PARTITION_SIZE)).thenReturn(List.of(new COBPartition(1L, 10L, 0L, 10L)));

        final Map<String, ExecutionContext> partitions = partitioner().partition(1);

        assertThat(partitions).containsOnlyKeys(COBConstant.PARTITION_PREFIX + 0);
        validatePartition(partitions, 0, 1L, 10L, "true");
    }

    @Test
    void shouldCreateSingleEmptyPartitionWithPageZeroWhenNoSavingsAccountIsBehind() {
        givenBusinessSteps(BUSINESS_STEPS);
        givenJobContext(BUSINESS_DATE, false);
        when(stepExecution.getExecutionContext()).thenReturn(new ExecutionContext());
        when(retrieveSavingsIdService.retrieveSavingsCOBPartitions(SavingsCOBConstant.NUMBER_OF_DAYS_BEHIND, BUSINESS_DATE, false,
                PARTITION_SIZE)).thenReturn(List.of());

        final Map<String, ExecutionContext> partitions = partitioner().partition(1);

        // page 0, so that the name matches what getPartitionNames() rebuilds from the stored partition count
        assertThat(partitions).containsOnlyKeys(COBConstant.PARTITION_PREFIX + 0);
        validatePartition(partitions, 0, 0L, 0L, "false");
    }

    @Test
    void shouldStoreThePartitionBoundsUnderTheSavingsCobParameterKey() {
        givenBusinessSteps(BUSINESS_STEPS);
        givenJobContext(BUSINESS_DATE, false);
        when(stepExecution.getExecutionContext()).thenReturn(new ExecutionContext());
        when(retrieveSavingsIdService.retrieveSavingsCOBPartitions(SavingsCOBConstant.NUMBER_OF_DAYS_BEHIND, BUSINESS_DATE, false,
                PARTITION_SIZE)).thenReturn(List.of(new COBPartition(1L, 10L, 0L, 10L)));

        final ExecutionContext partition = partitioner().partition(1).get(COBConstant.PARTITION_PREFIX + 0);

        // the savings reader looks the bounds up under the savings key; the loan key must not be populated
        assertThat(partition.get(SavingsCOBConstant.SAVINGS_COB_PARAMETER)).isEqualTo(new COBParameter(1L, 10L));
        assertThat(partition.containsKey(COBConstant.COB_PARAMETER)).isFalse();
    }

    @Test
    void shouldStopTheJobAndCreateNoPartitionWhenNoBusinessStepIsConfigured() throws JobExecutionNotRunningException {
        givenBusinessSteps(Set.of());
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);

        final Map<String, ExecutionContext> partitions = partitioner().partition(1);

        assertThat(partitions).isEmpty();
        verify(jobOperator).stop(jobExecution);
        verify(retrieveSavingsIdService, never()).retrieveSavingsCOBPartitions(anyLong(), any(), anyBoolean(), anyInt());
    }

    @Test
    void shouldReproduceOriginalPartitionNamesOnRestartAfterTheRemainingWorkShrinks() {
        final ExecutionContext managerContext = new ExecutionContext();
        givenBusinessSteps(BUSINESS_STEPS);
        givenJobContext(BUSINESS_DATE, false);
        when(stepExecution.getExecutionContext()).thenReturn(managerContext);
        // original run: three partitions
        when(retrieveSavingsIdService
                .retrieveSavingsCOBPartitions(SavingsCOBConstant.NUMBER_OF_DAYS_BEHIND, BUSINESS_DATE, false, PARTITION_SIZE))
                .thenReturn(List.of(//
                        new COBPartition(1L, 100L, 0L, 100L), //
                        new COBPartition(101L, 200L, 1L, 100L), //
                        new COBPartition(201L, 300L, 2L, 100L)));

        final SavingsCOBPartitioner partitioner = partitioner();
        assertThat(partitioner.partition(3)).containsOnlyKeys(COBConstant.PARTITION_PREFIX + 0, COBConstant.PARTITION_PREFIX + 1,
                COBConstant.PARTITION_PREFIX + 2);

        // On restart the splitter asks for names instead of re-partitioning, so a shrunken re-query cannot drop
        // partition_2.
        assertThat(partitioner.getPartitionNames(3)).containsExactly(COBConstant.PARTITION_PREFIX + 0, COBConstant.PARTITION_PREFIX + 1,
                COBConstant.PARTITION_PREFIX + 2);
    }

    @Test
    void shouldReproduceTheNameOfTheFallbackPartitionOfAnEmptyRun() {
        final ExecutionContext managerContext = new ExecutionContext();
        givenBusinessSteps(BUSINESS_STEPS);
        givenJobContext(BUSINESS_DATE, false);
        when(stepExecution.getExecutionContext()).thenReturn(managerContext);
        when(retrieveSavingsIdService.retrieveSavingsCOBPartitions(SavingsCOBConstant.NUMBER_OF_DAYS_BEHIND, BUSINESS_DATE, false,
                PARTITION_SIZE)).thenReturn(List.of());

        final SavingsCOBPartitioner partitioner = partitioner();
        assertThat(partitioner.partition(1)).containsOnlyKeys(COBConstant.PARTITION_PREFIX + 0);

        assertThat(partitioner.getPartitionNames(1)).containsExactly(COBConstant.PARTITION_PREFIX + 0);
    }

    @Test
    void shouldFailLoudlyOnRestartWhenThePartitionCountWasNeverRecorded() {
        when(stepExecution.getExecutionContext()).thenReturn(new ExecutionContext());
        when(stepExecution.getStepName()).thenReturn(SavingsCOBConstant.SAVINGS_COB_PARTITIONER_STEP);

        assertThatThrownBy(() -> partitioner().getPartitionNames(3)) //
                .isInstanceOf(IllegalStateException.class) //
                .hasMessageContaining(SavingsCOBConstant.SAVINGS_COB_PARTITIONER_STEP) //
                .hasMessageContaining("partition count is missing") //
                .hasMessageContaining("catch-up");
    }

    private SavingsCOBPartitioner partitioner() {
        return new SavingsCOBPartitioner(propertyService, cobBusinessStepService, retrieveSavingsIdService, jobOperator, stepExecution);
    }

    private void givenBusinessSteps(Set<BusinessStepNameAndOrder> businessSteps) {
        when(propertyService.getPartitionSize(SavingsCOBConstant.JOB_NAME)).thenReturn(PARTITION_SIZE);
        when(cobBusinessStepService.getCOBBusinessSteps(SavingsCOBBusinessStep.class, SavingsCOBConstant.SAVINGS_COB_JOB_NAME))
                .thenReturn(businessSteps);
    }

    private void givenJobContext(LocalDate businessDate, boolean isCatchUp) {
        final ExecutionContext jobContext = new ExecutionContext();
        jobContext.put(COBConstant.BUSINESS_DATE_PARAMETER_NAME, businessDate);
        jobContext.put(COBConstant.IS_CATCH_UP_PARAMETER_NAME, isCatchUp);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getExecutionContext()).thenReturn(jobContext);
    }

    private static void validatePartition(Map<String, ExecutionContext> partitions, int pageNo, long minId, long maxId, String isCatchUp) {
        final ExecutionContext partition = partitions.get(COBConstant.PARTITION_PREFIX + pageNo);
        assertThat(partition.get(COBConstant.BUSINESS_STEPS)).isEqualTo(BUSINESS_STEPS);
        assertThat(partition.get(SavingsCOBConstant.SAVINGS_COB_PARAMETER)).isEqualTo(new COBParameter(minId, maxId));
        assertThat(partition.get(COBConstant.PARTITION_KEY)).isEqualTo(COBConstant.PARTITION_PREFIX + pageNo);
        assertThat(partition.get(COBConstant.BUSINESS_DATE_PARAMETER_NAME)).isEqualTo(BUSINESS_DATE.toString());
        assertThat(partition.get(COBConstant.IS_CATCH_UP_PARAMETER_NAME)).isEqualTo(isCatchUp);
    }
}
