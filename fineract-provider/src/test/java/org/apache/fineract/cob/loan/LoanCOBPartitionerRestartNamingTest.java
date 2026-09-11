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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.data.COBPartition;
import org.apache.fineract.cob.service.RetrieveIdService;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.partition.PartitionNameProvider;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

/**
 * Guards partition-name stability across a restart.
 * <p>
 * {@code SimpleStepExecutionSplitter} matches partitions by name. If the partitioner does not implement
 * {@link PartitionNameProvider}, the splitter re-runs {@code partition(..)}, which re-queries only the loans still
 * behind. Completed partitions no longer match that query, so the page count shrinks and the names shift: a partition
 * that failed at an index at or above the new count is never iterated, the names that do match resolve to
 * already-COMPLETED executions and are skipped, and the restart reports COMPLETED having processed nothing. Because COB
 * selects on exact equality with {@code COB_DATE - 1}, those loans are never picked up by a later run either.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoanCOBPartitionerRestartNamingTest {

    private static final Set<BusinessStepNameAndOrder> BUSINESS_STEPS = Set.of(new BusinessStepNameAndOrder("Business step", 1L));
    private static final LocalDate BUSINESS_DATE = LocalDate.parse("2023-06-28");

    @Mock
    private PropertyService propertyService;
    @Mock
    private COBBusinessStepService cobBusinessStepService;
    @Mock
    private RetrieveIdService retrieveIdService;
    @Mock
    private JobOperator jobOperator;
    @Mock
    private StepExecution stepExecution;
    @Mock
    private JobExecution jobExecution;

    @Test
    public void shouldReproduceOriginalPartitionNamesAfterTheRemainingWorkShrinks() {
        ExecutionContext managerContext = new ExecutionContext();
        when(propertyService.getPartitionSize(LoanCOBConstant.JOB_NAME)).thenReturn(100);
        when(cobBusinessStepService.getCOBBusinessSteps(LoanCOBBusinessStep.class, LoanCOBConstant.LOAN_COB_JOB_NAME))
                .thenReturn(BUSINESS_STEPS);
        // original run: three partitions
        when(retrieveIdService.retrieveLoanCOBPartitions(1L, BUSINESS_DATE, false, 100)).thenReturn(List.of(//
                new COBPartition(1L, 100L, 0L, 100L), //
                new COBPartition(101L, 200L, 1L, 100L), //
                new COBPartition(201L, 300L, 2L, 100L)));

        LoanCOBPartitioner partitioner = partitioner(managerContext);
        assertThat(partitioner.partition(3)).containsOnlyKeys(//
                COBConstant.PARTITION_PREFIX + 0, //
                COBConstant.PARTITION_PREFIX + 1, //
                COBConstant.PARTITION_PREFIX + 2);

        // On restart the splitter asks for names instead of re-partitioning. Only partition_2's loans are still
        // behind, so a re-query would yield a single page and partition_2 would be lost.
        assertThat(partitioner.getPartitionNames(3)).containsExactly(//
                COBConstant.PARTITION_PREFIX + 0, //
                COBConstant.PARTITION_PREFIX + 1, //
                COBConstant.PARTITION_PREFIX + 2);
    }

    @Test
    public void shouldFailLoudlyWhenThePartitionCountWasNeverRecorded() {
        LoanCOBPartitioner partitioner = partitioner(new ExecutionContext());
        when(stepExecution.getStepName()).thenReturn("Loan COB - Step");

        assertThatThrownBy(() -> partitioner.getPartitionNames(3)) //
                .isInstanceOf(IllegalStateException.class) //
                .hasMessageContaining("partition count is missing") //
                .hasMessageContaining("catch-up");
    }

    private LoanCOBPartitioner partitioner(ExecutionContext managerContext) {
        when(stepExecution.getExecutionContext()).thenReturn(managerContext);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        ExecutionContext jobContext = new ExecutionContext();
        jobContext.put(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME, BUSINESS_DATE);
        jobContext.put(LoanCOBConstant.IS_CATCH_UP_PARAMETER_NAME, false);
        when(jobExecution.getExecutionContext()).thenReturn(jobContext);
        return new LoanCOBPartitioner(propertyService, cobBusinessStepService, retrieveIdService, jobOperator, stepExecution, 1L);
    }
}
