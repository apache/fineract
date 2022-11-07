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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.cucumber.java8.En;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.mockito.Mockito;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.item.ExecutionContext;

public class LoanCOBPartitionerStepDefinitions implements En {

    PropertyService propertyService = mock(PropertyService.class);
    COBBusinessStepService cobBusinessStepService = mock(COBBusinessStepService.class);
    JobOperator jobOperator = mock(JobOperator.class);
    JobExplorer jobExplorer = mock(JobExplorer.class);

    List<Long> loanIds;
    private LoanCOBPartitioner loanCOBPartitioner;

    private TreeMap<Long, String> cobBusinessMap = new TreeMap<>();

    private Map<String, ExecutionContext> resultItem;
    private String action;

    public LoanCOBPartitionerStepDefinitions() {
        Given("/^The LoanCOBPartitioner.partition method with action (.*)$/", (String action) -> {

            this.action = action;
            lenient().when(propertyService.getPartitionSize(LoanCOBConstant.JOB_NAME)).thenReturn(2);
            if ("empty steps".equals(action)) {
                lenient().when(cobBusinessStepService.getCOBBusinessStepMap(LoanCOBBusinessStep.class, LoanCOBConstant.LOAN_COB_JOB_NAME))
                        .thenReturn(new TreeMap<>());
                lenient().when(jobExplorer.findRunningJobExecutions(JobName.LOAN_COB.name())).thenReturn(Set.of(new JobExecution(3L)));
                lenient().when(jobOperator.stop(3L)).thenReturn(Boolean.TRUE);
            } else if ("empty loanIds".equals(action)) {
                cobBusinessMap.put(1L, "Business step");
                lenient().when(cobBusinessStepService.getCOBBusinessStepMap(LoanCOBBusinessStep.class, LoanCOBConstant.LOAN_COB_JOB_NAME))
                        .thenReturn(cobBusinessMap);
                loanIds = new ArrayList<>();
                lenient().when(jobExplorer.findRunningJobExecutions(JobName.LOAN_COB.name())).thenThrow(new RuntimeException("fail"));
            } else if ("good".equals(action)) {
                cobBusinessMap.put(1L, "Business step");
                lenient().when(cobBusinessStepService.getCOBBusinessStepMap(LoanCOBBusinessStep.class, LoanCOBConstant.LOAN_COB_JOB_NAME))
                        .thenReturn(cobBusinessMap);
                loanIds = List.of(1L, 2L, 3L);
            }
            loanCOBPartitioner = new LoanCOBPartitioner(propertyService, cobBusinessStepService, jobOperator, jobExplorer, loanIds);
        });

        When("LoanCOBPartitioner.partition method executed", () -> {
            resultItem = this.loanCOBPartitioner.partition(2);
        });

        Then("LoanCOBPartitioner.partition result should match", () -> {
            if ("empty steps".equals(action)) {
                verify(jobOperator, Mockito.times(1)).stop(3L);
                assertTrue(resultItem.isEmpty());
            } else if ("good".equals(action)) {
                verify(jobOperator, Mockito.times(0)).stop(Mockito.anyLong());
                assertEquals(2, resultItem.size());
                assertTrue(resultItem.containsKey(LoanCOBPartitioner.PARTITION_PREFIX + "1"));
                assertEquals(cobBusinessMap,
                        resultItem.get(LoanCOBPartitioner.PARTITION_PREFIX + "1").get(LoanCOBConstant.BUSINESS_STEP_MAP));
                assertEquals(2, ((List) resultItem.get(LoanCOBPartitioner.PARTITION_PREFIX + "1").get(LoanCOBConstant.LOAN_IDS)).size());
                assertEquals(1L, ((List) resultItem.get(LoanCOBPartitioner.PARTITION_PREFIX + "1").get(LoanCOBConstant.LOAN_IDS)).get(0));
                assertEquals(2L, ((List) resultItem.get(LoanCOBPartitioner.PARTITION_PREFIX + "1").get(LoanCOBConstant.LOAN_IDS)).get(1));
                assertTrue(resultItem.containsKey(LoanCOBPartitioner.PARTITION_PREFIX + "2"));
                assertEquals(cobBusinessMap,
                        resultItem.get(LoanCOBPartitioner.PARTITION_PREFIX + "2").get(LoanCOBConstant.BUSINESS_STEP_MAP));
                assertEquals(1, ((List) resultItem.get(LoanCOBPartitioner.PARTITION_PREFIX + "2").get(LoanCOBConstant.LOAN_IDS)).size());
                assertEquals(3L, ((List) resultItem.get(LoanCOBPartitioner.PARTITION_PREFIX + "2").get(LoanCOBConstant.LOAN_IDS)).get(0));
            }
        });

        Then("throw exception LoanCOBPartitioner.partition method", () -> {
            assertThrows(RuntimeException.class, () -> {
                resultItem = this.loanCOBPartitioner.partition(2);
            });
        });
    }
}
