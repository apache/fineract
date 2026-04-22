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
package org.apache.fineract.test.helper;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.BusinessStepRequest;
import org.apache.fineract.client.models.ConfiguredJobNamesDTO;
import org.apache.fineract.client.models.JobBusinessStepConfigData;
import org.apache.fineract.client.models.JobBusinessStepDetail;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class WorkFlowJobHelper {

    private static final String WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS = "LOAN_CLOSE_OF_BUSINESS";

    private final FineractFeignClient fineractClient;

    public void setWorkflowJobs() {
        List<BusinessStep> businessSteps = List.of(new BusinessStep().stepName("APPLY_CHARGE_TO_OVERDUE_LOANS").order(1L), //
                new BusinessStep().stepName("LOAN_DELINQUENCY_CLASSIFICATION").order(2L), //
                new BusinessStep().stepName("CHECK_LOAN_REPAYMENT_DUE").order(3L), //
                new BusinessStep().stepName("CHECK_LOAN_REPAYMENT_OVERDUE").order(4L), //
                new BusinessStep().stepName("CHECK_DUE_INSTALLMENTS").order(5L), //
                new BusinessStep().stepName("UPDATE_LOAN_ARREARS_AGING").order(6L), //
                new BusinessStep().stepName("ADD_PERIODIC_ACCRUAL_ENTRIES").order(7L), //
                new BusinessStep().stepName("ACCRUAL_ACTIVITY_POSTING").order(8L), //
                new BusinessStep().stepName("CAPITALIZED_INCOME_AMORTIZATION").order(9L), //
                new BusinessStep().stepName("BUY_DOWN_FEE_AMORTIZATION").order(10L), //
                new BusinessStep().stepName("LOAN_INTEREST_RECALCULATION").order(11L), //
                new BusinessStep().stepName("EXTERNAL_ASSET_OWNER_TRANSFER").order(12L)//
        );
        BusinessStepRequest request = new BusinessStepRequest().businessSteps(businessSteps);
        executeVoid(() -> fineractClient.businessStepConfiguration().updateJobBusinessStepConfig(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS,
                request, Map.of()));
        logChanges();
    }

    /**
     * Returns all job names that have business step configuration registered.
     */
    public ConfiguredJobNamesDTO getConfiguredBusinessJobs() {
        return ok(() -> fineractClient.businessStepConfiguration().retrieveAllConfiguredBusinessJobs(Map.of()));
    }

    /**
     * Returns the currently configured business steps for the given job.
     *
     * @param jobName
     *            the job name, e.g. {@code LOAN_CLOSE_OF_BUSINESS}
     */
    public JobBusinessStepConfigData getConfiguredWorkflowSteps(String jobName) {
        return ok(() -> fineractClient.businessStepConfiguration().retrieveAllConfiguredBusinessStep(jobName, Map.of()));
    }

    /**
     * Returns all available (registered) business steps for the given job.
     *
     * @param jobName
     *            the job name, e.g. {@code LOAN_CLOSE_OF_BUSINESS}
     */
    public JobBusinessStepDetail getAvailableWorkflowSteps(String jobName) {
        return ok(() -> fineractClient.businessStepConfiguration().retrieveAllAvailableBusinessStep(jobName, Map.of()));
    }

    /**
     * Replaces the configured business steps for the given job.
     *
     * @param jobName
     *            the job name, e.g. {@code LOAN_CLOSE_OF_BUSINESS}
     * @param steps
     *            the ordered list of business steps to configure
     */
    public void updateWorkflowSteps(String jobName, List<BusinessStep> steps) {
        BusinessStepRequest request = new BusinessStepRequest().businessSteps(steps);
        executeVoid(() -> fineractClient.businessStepConfiguration().updateJobBusinessStepConfig(jobName, request, Map.of()));
    }

    private void logChanges() {
        JobBusinessStepConfigData changesResponse = ok(() -> fineractClient.businessStepConfiguration()
                .retrieveAllConfiguredBusinessStep(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS, Map.of()));
        List<BusinessStep> businessStepsChanged = changesResponse.getBusinessSteps();
        List<String> changes = businessStepsChanged//
                .stream()//
                .sorted(Comparator.comparingLong(BusinessStep::getOrder))//
                .map(BusinessStep::getStepName)//
                .collect(Collectors.toList());//

        log.debug("Business steps has been CHANGED to the following:");
        changes.forEach(e -> log.debug(e));
    }
}
