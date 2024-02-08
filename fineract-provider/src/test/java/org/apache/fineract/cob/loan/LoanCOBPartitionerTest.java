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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.data.LoanCOBParameter;
import org.apache.fineract.cob.data.LoanCOBPartition;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
class LoanCOBPartitionerTest {

    private static final Set<BusinessStepNameAndOrder> BUSINESS_STEP_SET = Set.of(new BusinessStepNameAndOrder("Business step", 1L));
    private static final LocalDate BUSINESS_DATE = LocalDate.parse("2023-06-28");
    @Mock
    private PropertyService propertyService;
    @Mock
    private COBBusinessStepService cobBusinessStepService;
    @Mock
    private RetrieveLoanIdService retrieveLoanIdService;
    @Mock
    private JobOperator jobOperator;
    @Mock
    private JobExplorer jobExplorer;

    @Test
    public void testLoanCOBPartitioner() {
        //given
        when(propertyService.getPartitionSize(LoanCOBConstant.JOB_NAME)).thenReturn(5);
        when(cobBusinessStepService.getCOBBusinessSteps(LoanCOBBusinessStep.class, LoanCOBConstant.LOAN_COB_JOB_NAME))
                .thenReturn(BUSINESS_STEP_SET);
        when(retrieveLoanIdService.retrieveLoanCOBPartitions(1L, BUSINESS_DATE, false, 5))
                .thenReturn(List.of(new LoanCOBPartition(1L,10L, 1L, 5L), new LoanCOBPartition(11L,20L, 2L, 4L)));
        LoanCOBPartitioner loanCOBPartitioner = new LoanCOBPartitioner(propertyService, cobBusinessStepService, retrieveLoanIdService, jobOperator, jobExplorer, 1L);
        loanCOBPartitioner.setBusinessDate(BUSINESS_DATE);
        loanCOBPartitioner.setIsCatchUp(false);

        //when
        Map<String, ExecutionContext> partitions = loanCOBPartitioner.partition(1);

        //then
        Assertions.assertEquals(2, partitions.size());
        validatePartitions(partitions, 1, 1,  10);
        validatePartitions(partitions, 2, 11,  20);
    }

    @Test
    public void testLoanCOBPartitionerEmptyBusinessSteps() throws NoSuchJobExecutionException, JobExecutionNotRunningException {
        //given
        when(propertyService.getPartitionSize(LoanCOBConstant.JOB_NAME)).thenReturn(5);
        when(cobBusinessStepService.getCOBBusinessSteps(LoanCOBBusinessStep.class, LoanCOBConstant.LOAN_COB_JOB_NAME))
                .thenReturn(Set.of());
        JobExecution jobExecution = Mockito.mock(JobExecution.class);
        when(jobExecution.getId()).thenReturn(123L);
        when(jobExplorer.findRunningJobExecutions(JobName.LOAN_COB.name())).thenReturn(Set.of(jobExecution));
        LoanCOBPartitioner loanCOBPartitioner = new LoanCOBPartitioner(propertyService, cobBusinessStepService, retrieveLoanIdService, jobOperator, jobExplorer, 1L);
        loanCOBPartitioner.setBusinessDate(BUSINESS_DATE);
        loanCOBPartitioner.setIsCatchUp(false);

        //when
        Map<String, ExecutionContext> partitions = loanCOBPartitioner.partition(1);

        //then
        Assertions.assertEquals(0, partitions.size());
        verify(jobExplorer, times(1)).findRunningJobExecutions(JobName.LOAN_COB.name());
        verify(jobOperator, times(1)).stop(123L);
    }

    @Test
    public void testLoanCOBPartitionerNoLoansFound() {
        //given
        when(propertyService.getPartitionSize(LoanCOBConstant.JOB_NAME)).thenReturn(5);
        when(cobBusinessStepService.getCOBBusinessSteps(LoanCOBBusinessStep.class, LoanCOBConstant.LOAN_COB_JOB_NAME))
                .thenReturn(BUSINESS_STEP_SET);
        when(retrieveLoanIdService.retrieveLoanCOBPartitions(1L, BUSINESS_DATE, false, 5))
                .thenReturn(List.of());
        LoanCOBPartitioner loanCOBPartitioner = new LoanCOBPartitioner(propertyService, cobBusinessStepService, retrieveLoanIdService, jobOperator, jobExplorer, 1L);
        loanCOBPartitioner.setBusinessDate(BUSINESS_DATE);
        loanCOBPartitioner.setBusinessDate(BUSINESS_DATE);
        loanCOBPartitioner.setIsCatchUp(false);

        //when
        Map<String, ExecutionContext> partitions = loanCOBPartitioner.partition(1);

        //then
        Assertions.assertEquals(1, partitions.size());
        validatePartitions(partitions, 1, 0,  0);
    }

    private void validatePartitions(Map<String, ExecutionContext> partitions, int index, long min, long max) {
        Assertions.assertEquals(BUSINESS_STEP_SET,
                partitions.get(LoanCOBPartitioner.PARTITION_PREFIX + index).get(LoanCOBConstant.BUSINESS_STEPS));
        Assertions.assertEquals(new LoanCOBParameter(min, max),
                partitions.get(LoanCOBPartitioner.PARTITION_PREFIX + index).get(LoanCOBConstant.LOAN_COB_PARAMETER));
        Assertions.assertEquals("partition_" + index, partitions.get(LoanCOBPartitioner.PARTITION_PREFIX + index).get("partition"));
    }
}
