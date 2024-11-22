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

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.GetBusinessStepConfigResponse;
import org.apache.fineract.client.models.UpdateBusinessStepConfigRequest;
import org.apache.fineract.client.services.BusinessStepConfigurationApi;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@RequiredArgsConstructor
@Component
@Slf4j
public class WorkFlowJobHelper {

    private static final String WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS = "LOAN_CLOSE_OF_BUSINESS";

    private final BusinessStepConfigurationApi businessStepConfigurationApi;

    public void setWorkflowJobs() throws IOException {
        List<BusinessStep> businessSteps = List.of(new BusinessStep().stepName("APPLY_CHARGE_TO_OVERDUE_LOANS").order(1L), //
                new BusinessStep().stepName("LOAN_DELINQUENCY_CLASSIFICATION").order(2L), //
                new BusinessStep().stepName("CHECK_LOAN_REPAYMENT_DUE").order(3L), //
                new BusinessStep().stepName("CHECK_LOAN_REPAYMENT_OVERDUE").order(4L), //
                new BusinessStep().stepName("CHECK_DUE_INSTALLMENTS").order(5L), //
                new BusinessStep().stepName("UPDATE_LOAN_ARREARS_AGING").order(6L), //
                new BusinessStep().stepName("ADD_PERIODIC_ACCRUAL_ENTRIES").order(7L), //
                new BusinessStep().stepName("ACCRUAL_ACTIVITY_POSTING").order(8L), //
                new BusinessStep().stepName("LOAN_INTEREST_RECALCULATION").order(9L), //
                new BusinessStep().stepName("EXTERNAL_ASSET_OWNER_TRANSFER").order(10L)//
        );
        UpdateBusinessStepConfigRequest request = new UpdateBusinessStepConfigRequest().businessSteps(businessSteps);
        Response<Void> response = businessStepConfigurationApi.updateJobBusinessStepConfig(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS, request)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        // --- log changes ---
        logChanges();
    }

    private void logChanges() throws IOException {
        // --- log changes ---
        Response<GetBusinessStepConfigResponse> changesResponse = businessStepConfigurationApi
                .retrieveAllConfiguredBusinessStep(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS).execute();
        List<BusinessStep> businessStepsChanged = changesResponse.body().getBusinessSteps();
        List<String> changes = businessStepsChanged//
                .stream()//
                .sorted(Comparator.comparingLong(BusinessStep::getOrder))//
                .map(BusinessStep::getStepName)//
                .collect(Collectors.toList());//

        log.debug("Business steps has been CHANGED to the following:");
        changes.forEach(e -> log.debug(e));
    }
}
