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
package org.apache.fineract.cob.workingcapitalloan;

import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_JOB_NAME;
import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_JOB_NAME;

import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.common.CommonPartitioner;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.workingcapitalloan.businessstep.WorkingCapitalLoanCOBBusinessStep;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.lang.NonNull;

@Slf4j
public class WorkingCapitalLoanCOBPartitioner extends CommonPartitioner implements Partitioner {

    private final COBBusinessStepService cobBusinessStepService;
    private final PropertyService propertyService;

    public WorkingCapitalLoanCOBPartitioner(JobOperator jobOperator, StepExecution stepExecution, Long numberOfDays,
            WorkingCapitalLoanRetrieveIdService retrieveIdService, COBBusinessStepService cobBusinessStepService,
            PropertyService propertyService) {
        super(jobOperator, stepExecution, numberOfDays, retrieveIdService);
        this.cobBusinessStepService = cobBusinessStepService;
        this.propertyService = propertyService;
    }

    @NonNull
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int partitionSize = propertyService.getPartitionSize(WORKING_CAPITAL_JOB_NAME);
        Set<BusinessStepNameAndOrder> cobBusinessSteps = cobBusinessStepService.getCOBBusinessSteps(WorkingCapitalLoanCOBBusinessStep.class,
                WORKING_CAPITAL_LOAN_COB_JOB_NAME);
        return getPartitions(partitionSize, cobBusinessSteps);
    }

}
