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
import java.util.List;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.UpdateBusinessStepConfigRequest;
import org.apache.fineract.client.services.BusinessStepConfigurationApi;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

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

    @Autowired
    private BusinessStepConfigurationApi businessStepConfigurationApi;

    @Given("Admin puts EXTERNAL_ASSET_OWNER_TRANSFER job into LOAN_CLOSE_OF_BUSINESS workflow")
    public void putExternalAssetOwnerTransferJobInCOB() throws IOException {
        BusinessStep applyChargeToOverdueLoans = new BusinessStep().stepName(BUSINESS_STEP_NAME_APPLY_CHARGE_TO_OVERDUE_LOANS).order(1L);
        BusinessStep loanDelinquencyClassification = new BusinessStep().stepName(BUSINESS_STEP_NAME_LOAN_DELINQUENCY_CLASSIFICATION)
                .order(2L);
        BusinessStep checkLoanRepaymentDue = new BusinessStep().stepName(BUSINESS_STEP_NAME_CHECK_LOAN_REPAYMENT_DUE).order(3L);
        BusinessStep checkLoanRepaymentOverdue = new BusinessStep().stepName(BUSINESS_STEP_NAME_CHECK_LOAN_REPAYMENT_OVERDUE).order(4L);
        BusinessStep updateLoanArrearsAging = new BusinessStep().stepName(BUSINESS_STEP_NAME_UPDATE_LOAN_ARREARS_AGING).order(5L);
        BusinessStep addPeriodicAccrualEntries = new BusinessStep().stepName(BUSINESS_STEP_NAME_ADD_PERIODIC_ACCRUAL_ENTRIES).order(6L);
        BusinessStep externalAssetOwnerTransfer = new BusinessStep().stepName(BUSINESS_STEP_NAME_EXTERNAL_ASSET_OWNER_TRANSFER).order(7L);

        List<BusinessStep> businessSteps = new ArrayList<>();
        businessSteps.add(applyChargeToOverdueLoans);
        businessSteps.add(loanDelinquencyClassification);
        businessSteps.add(checkLoanRepaymentDue);
        businessSteps.add(checkLoanRepaymentOverdue);
        businessSteps.add(updateLoanArrearsAging);
        businessSteps.add(addPeriodicAccrualEntries);
        businessSteps.add(externalAssetOwnerTransfer);

        UpdateBusinessStepConfigRequest request = new UpdateBusinessStepConfigRequest().businessSteps(businessSteps);

        Response<Void> response = businessStepConfigurationApi.updateJobBusinessStepConfig(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS, request)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    @Then("Admin removes EXTERNAL_ASSET_OWNER_TRANSFER job from LOAN_CLOSE_OF_BUSINESS workflow")
    public void removeExternalAssetOwnerTransferJobInCOB() throws IOException {
        BusinessStep applyChargeToOverdueLoans = new BusinessStep().stepName(BUSINESS_STEP_NAME_APPLY_CHARGE_TO_OVERDUE_LOANS).order(1L);
        BusinessStep loanDelinquencyClassification = new BusinessStep().stepName(BUSINESS_STEP_NAME_LOAN_DELINQUENCY_CLASSIFICATION)
                .order(2L);
        BusinessStep checkLoanRepaymentDue = new BusinessStep().stepName(BUSINESS_STEP_NAME_CHECK_LOAN_REPAYMENT_DUE).order(3L);
        BusinessStep checkLoanRepaymentOverdue = new BusinessStep().stepName(BUSINESS_STEP_NAME_CHECK_LOAN_REPAYMENT_OVERDUE).order(4L);
        BusinessStep updateLoanArrearsAging = new BusinessStep().stepName(BUSINESS_STEP_NAME_UPDATE_LOAN_ARREARS_AGING).order(5L);
        BusinessStep addPeriodicAccrualEntries = new BusinessStep().stepName(BUSINESS_STEP_NAME_ADD_PERIODIC_ACCRUAL_ENTRIES).order(6L);

        List<BusinessStep> businessSteps = new ArrayList<>();
        businessSteps.add(applyChargeToOverdueLoans);
        businessSteps.add(loanDelinquencyClassification);
        businessSteps.add(checkLoanRepaymentDue);
        businessSteps.add(checkLoanRepaymentOverdue);
        businessSteps.add(updateLoanArrearsAging);
        businessSteps.add(addPeriodicAccrualEntries);

        UpdateBusinessStepConfigRequest request = new UpdateBusinessStepConfigRequest().businessSteps(businessSteps);

        Response<Void> response = businessStepConfigurationApi.updateJobBusinessStepConfig(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS, request)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    @Given("Admin puts CHECK_DUE_INSTALLMENTS job into LOAN_CLOSE_OF_BUSINESS workflow")
    public void putCheckDueInstallmentsJobInCOB() throws IOException {
        BusinessStep applyChargeToOverdueLoans = new BusinessStep().stepName(BUSINESS_STEP_NAME_APPLY_CHARGE_TO_OVERDUE_LOANS).order(1L);
        BusinessStep loanDelinquencyClassification = new BusinessStep().stepName(BUSINESS_STEP_NAME_LOAN_DELINQUENCY_CLASSIFICATION)
                .order(2L);
        BusinessStep checkLoanRepaymentDue = new BusinessStep().stepName(BUSINESS_STEP_NAME_CHECK_LOAN_REPAYMENT_DUE).order(3L);
        BusinessStep checkLoanRepaymentOverdue = new BusinessStep().stepName(BUSINESS_STEP_NAME_CHECK_LOAN_REPAYMENT_OVERDUE).order(4L);
        BusinessStep updateLoanArrearsAging = new BusinessStep().stepName(BUSINESS_STEP_NAME_UPDATE_LOAN_ARREARS_AGING).order(5L);
        BusinessStep addPeriodicAccrualEntries = new BusinessStep().stepName(BUSINESS_STEP_NAME_ADD_PERIODIC_ACCRUAL_ENTRIES).order(6L);
        BusinessStep checkDueInstallments = new BusinessStep().stepName(BUSINESS_STEP_NAME_CHECK_DUE_INSTALLMENTS).order(7L);

        List<BusinessStep> businessSteps = new ArrayList<>();
        businessSteps.add(applyChargeToOverdueLoans);
        businessSteps.add(loanDelinquencyClassification);
        businessSteps.add(checkLoanRepaymentDue);
        businessSteps.add(checkLoanRepaymentOverdue);
        businessSteps.add(updateLoanArrearsAging);
        businessSteps.add(addPeriodicAccrualEntries);
        businessSteps.add(checkDueInstallments);

        UpdateBusinessStepConfigRequest request = new UpdateBusinessStepConfigRequest().businessSteps(businessSteps);

        Response<Void> response = businessStepConfigurationApi.updateJobBusinessStepConfig(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS, request)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    @Then("Admin removes CHECK_DUE_INSTALLMENTS job from LOAN_CLOSE_OF_BUSINESS workflow")
    public void removeCheckDueInstallmentsJobInCOB() throws IOException {
        BusinessStep applyChargeToOverdueLoans = new BusinessStep().stepName(BUSINESS_STEP_NAME_APPLY_CHARGE_TO_OVERDUE_LOANS).order(1L);
        BusinessStep loanDelinquencyClassification = new BusinessStep().stepName(BUSINESS_STEP_NAME_LOAN_DELINQUENCY_CLASSIFICATION)
                .order(2L);
        BusinessStep checkLoanRepaymentDue = new BusinessStep().stepName(BUSINESS_STEP_NAME_CHECK_LOAN_REPAYMENT_DUE).order(3L);
        BusinessStep checkLoanRepaymentOverdue = new BusinessStep().stepName(BUSINESS_STEP_NAME_CHECK_LOAN_REPAYMENT_OVERDUE).order(4L);
        BusinessStep updateLoanArrearsAging = new BusinessStep().stepName(BUSINESS_STEP_NAME_UPDATE_LOAN_ARREARS_AGING).order(5L);
        BusinessStep addPeriodicAccrualEntries = new BusinessStep().stepName(BUSINESS_STEP_NAME_ADD_PERIODIC_ACCRUAL_ENTRIES).order(6L);

        List<BusinessStep> businessSteps = new ArrayList<>();
        businessSteps.add(applyChargeToOverdueLoans);
        businessSteps.add(loanDelinquencyClassification);
        businessSteps.add(checkLoanRepaymentDue);
        businessSteps.add(checkLoanRepaymentOverdue);
        businessSteps.add(updateLoanArrearsAging);
        businessSteps.add(addPeriodicAccrualEntries);

        UpdateBusinessStepConfigRequest request = new UpdateBusinessStepConfigRequest().businessSteps(businessSteps);

        Response<Void> response = businessStepConfigurationApi.updateJobBusinessStepConfig(WORKFLOW_NAME_LOAN_CLOSE_OF_BUSINESS, request)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(response);
    }
}
