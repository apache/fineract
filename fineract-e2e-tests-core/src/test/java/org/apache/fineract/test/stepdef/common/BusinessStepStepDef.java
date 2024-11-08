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
package org.apache.fineract.test.stepdef.common;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.GetBusinessStepConfigResponse;
import org.apache.fineract.client.models.UpdateBusinessStepConfigRequest;
import org.apache.fineract.client.services.BusinessStepConfigurationApi;
import org.apache.fineract.test.data.CobBusinessStep;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class BusinessStepStepDef extends AbstractStepDef {

    private static final String WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS = "LOAN_CLOSE_OF_BUSINESS";
    private static final String BUSINESS_STEP_NAME_APPLY_CHARGE_TO_OVERDUE_LOANS = "APPLY_CHARGE_TO_OVERDUE_LOANS";
    private static final String BUSINESS_STEP_NAME_LOAN_DELINQUENCY_CLASSIFICATION = "LOAN_DELINQUENCY_CLASSIFICATION";
    private static final String BUSINESS_STEP_NAME_CHECK_LOAN_REPAYMENT_DUE = "CHECK_LOAN_REPAYMENT_DUE";
    private static final String BUSINESS_STEP_NAME_CHECK_LOAN_REPAYMENT_OVERDUE = "CHECK_LOAN_REPAYMENT_OVERDUE";
    private static final String BUSINESS_STEP_NAME_UPDATE_LOAN_ARREARS_AGING = "UPDATE_LOAN_ARREARS_AGING";
    private static final String BUSINESS_STEP_NAME_ADD_PERIODIC_ACCRUAL_ENTRIES = "ADD_PERIODIC_ACCRUAL_ENTRIES";
    private static final String BUSINESS_STEP_NAME_EXTERNAL_ASSET_OWNER_TRANSFER = "EXTERNAL_ASSET_OWNER_TRANSFER";
    private static final String BUSINESS_STEP_NAME_CHECK_DUE_INSTALLMENTS = "CHECK_DUE_INSTALLMENTS";
    private static final String BUSINESS_STEP_NAME_ACCRUAL_ACTIVITY_POSTING = "ACCRUAL_ACTIVITY_POSTING";
    private static final List<BusinessStep> ORIGINAL_COB_BUSINESS_STEPS = TestContext.GLOBAL
            .get(TestContextKey.ORIGINAL_COB_WORKFLOW_JOB_BUSINESS_STEP_LIST);

    @Autowired
    private BusinessStepConfigurationApi businessStepConfigurationApi;

    @Given("Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow")
    public void putExternalAssetOwnerTransferJobInCOB() throws IOException {
        List<BusinessStep> businessSteps = new ArrayList<>(ORIGINAL_COB_BUSINESS_STEPS);
        Long lastOrder = businessSteps.get(businessSteps.size() - 1).getOrder();

        BusinessStep externalAssetOwnerTransfer = new BusinessStep().stepName(BUSINESS_STEP_NAME_EXTERNAL_ASSET_OWNER_TRANSFER)
                .order(lastOrder + 1);
        businessSteps.add(externalAssetOwnerTransfer);

        UpdateBusinessStepConfigRequest request = new UpdateBusinessStepConfigRequest().businessSteps(businessSteps);

        Response<Void> response = businessStepConfigurationApi.updateJobBusinessStepConfig(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS, request)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);

        // --- log changes ---
        logChanges();
    }

    @Then("Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow")
    public void removeExternalAssetOwnerTransferJobInCOB() throws IOException {
        setBackBusinessStepsToOriginal();
    }

    @Given("Admin puts CHECK_DUE_INSTALLMENTS job into LOAN_CLOSE_OF_BUSINESS workflow")
    public void putCheckDueInstallmentsJobInCOB() throws IOException {
        List<BusinessStep> businessSteps = new ArrayList<>(ORIGINAL_COB_BUSINESS_STEPS);
        Long lastOrder = businessSteps.get(businessSteps.size() - 1).getOrder();

        BusinessStep checkDueInstallments = new BusinessStep().stepName(BUSINESS_STEP_NAME_CHECK_DUE_INSTALLMENTS).order(lastOrder + 1);
        businessSteps.add(checkDueInstallments);

        UpdateBusinessStepConfigRequest request = new UpdateBusinessStepConfigRequest().businessSteps(businessSteps);

        Response<Void> response = businessStepConfigurationApi.updateJobBusinessStepConfig(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS, request)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);

        // --- log changes ---
        logChanges();
    }

    @Then("Admin removes CHECK_DUE_INSTALLMENTS job from LOAN_CLOSE_OF_BUSINESS workflow")
    public void removeCheckDueInstallmentsJobInCOB() throws IOException {
        setBackBusinessStepsToOriginal();
    }

    @Given("Admin puts {string} business step into LOAN_CLOSE_OF_BUSINESS workflow")
    public void putGivenJobInCOB(String businessStepName) throws IOException {
        List<BusinessStep> businessSteps = new ArrayList<>(ORIGINAL_COB_BUSINESS_STEPS);
        Long lastOrder = businessSteps.get(businessSteps.size() - 1).getOrder();

        CobBusinessStep cobBusinessStep = CobBusinessStep.valueOf(businessStepName);
        String stepName = cobBusinessStep.getValue();

        BusinessStep businessStepToAdd = new BusinessStep().stepName(stepName).order(lastOrder + 1);
        businessSteps.add(businessStepToAdd);

        UpdateBusinessStepConfigRequest request = new UpdateBusinessStepConfigRequest().businessSteps(businessSteps);

        Response<Void> response = businessStepConfigurationApi.updateJobBusinessStepConfig(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS, request)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);

        // --- log changes ---
        logChanges();
    }

    @Then("Admin sets back LOAN_CLOSE_OF_BUSINESS workflow to its initial state")
    public void setBackCOBToInitialState() throws IOException {
        setBackBusinessStepsToOriginal();
    }

    @Then("Admin removes {string} business step into LOAN_CLOSE_OF_BUSINESS workflow")
    public void removeGivenJobInCOB(String businessStepName) throws IOException {
        List<BusinessStep> businessSteps = new ArrayList<>(ORIGINAL_COB_BUSINESS_STEPS);

        CobBusinessStep cobBusinessStep = CobBusinessStep.valueOf(businessStepName);
        String stepName = cobBusinessStep.getValue();

        businessSteps.removeIf(businessStep -> businessStep.getStepName().equals(stepName));

        UpdateBusinessStepConfigRequest request = new UpdateBusinessStepConfigRequest().businessSteps(businessSteps);

        Response<Void> response = businessStepConfigurationApi.updateJobBusinessStepConfig(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS, request)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);

        // --- log changes ---
        logChanges();
    }

    private void setBackBusinessStepsToOriginal() throws IOException {
        log.debug("Setting back Business steps to original...");
        UpdateBusinessStepConfigRequest request = new UpdateBusinessStepConfigRequest().businessSteps(ORIGINAL_COB_BUSINESS_STEPS);

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
