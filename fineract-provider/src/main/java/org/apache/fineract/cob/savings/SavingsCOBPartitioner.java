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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.common.CommonPartitioner;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.data.COBPartition;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.lang.NonNull;

@Slf4j
public class SavingsCOBPartitioner extends CommonPartitioner implements Partitioner {

    private final PropertyService propertyService;
    private final COBBusinessStepService cobBusinessStepService;
    private final RetrieveSavingsIdService retrieveSavingsIdService;

    public SavingsCOBPartitioner(PropertyService propertyService, COBBusinessStepService cobBusinessStepService,
            RetrieveSavingsIdService retrieveSavingsIdService, JobOperator jobOperator, StepExecution stepExecution) {
        super(jobOperator, stepExecution, SavingsCOBConstant.NUMBER_OF_DAYS_BEHIND);
        this.propertyService = propertyService;
        this.cobBusinessStepService = cobBusinessStepService;
        this.retrieveSavingsIdService = retrieveSavingsIdService;
    }

    @Override
    protected String getCobParameterKey() {
        return SavingsCOBConstant.SAVINGS_COB_PARAMETER;
    }

    @Override
    protected List<COBPartition> retrievePartitions(Long numberOfDays, LocalDate businessDate, boolean isCatchUp, int partitionSize) {
        return retrieveSavingsIdService.retrieveSavingsCOBPartitions(numberOfDays, businessDate, isCatchUp, partitionSize);
    }

    @NonNull
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int partitionSize = propertyService.getPartitionSize(SavingsCOBConstant.JOB_NAME);
        Set<BusinessStepNameAndOrder> cobBusinessSteps = cobBusinessStepService.getCOBBusinessSteps(SavingsCOBBusinessStep.class,
                SavingsCOBConstant.SAVINGS_COB_JOB_NAME);
        return getPartitions(partitionSize, cobBusinessSteps);
    }
}
