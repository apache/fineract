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
package org.apache.fineract.integrationtests;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.BusinessStepRequest;
import org.apache.fineract.client.models.JobBusinessStepConfigData;
import org.apache.fineract.client.models.JobBusinessStepDetail;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the per-job business step configuration endpoint ({@code /v1/jobs/{jobName}/steps}) also works for the
 * Savings COB, not only for the Loan COB. See FINERACT-2332.
 */
public class SavingsBusinessStepConfigurationApiTest extends FeignIntegrationTest {

    public static final String SAVINGS_JOB_NAME = "SAVINGS_CLOSE_OF_BUSINESS";
    public static final String SAVINGS_CATEGORY_NAME = "savings";
    public static final String POST_INTEREST_FOR_SAVINGS = "POST_INTEREST_FOR_SAVINGS";
    public static final String PAY_DUE_SAVINGS_CHARGES = "PAY_DUE_SAVINGS_CHARGES";
    // A step that belongs to the Loan COB, used to assert it cannot be configured on the Savings COB.
    public static final String APPLY_CHARGE_TO_OVERDUE_LOANS = "APPLY_CHARGE_TO_OVERDUE_LOANS";

    private List<BusinessStep> originalSteps;

    @BeforeEach
    public void setup() {
        // Snapshot the seeded Savings COB step configuration so it can be restored after each test.
        this.originalSteps = getConfiguredSavingsCobSteps().getBusinessSteps();
    }

    @AfterEach
    public void restoreOriginalConfiguration() {
        updateSavingsCobSteps(originalSteps);
    }

    @Test
    public void shouldExposeSavingsCobAvailableBusinessSteps() {
        // Change A: the SAVINGS category must be registered, so available steps are resolvable by category name.
        JobBusinessStepDetail response = ok(
                () -> fineractClient().businessStepConfiguration().retrieveAllAvailableBusinessStep(SAVINGS_CATEGORY_NAME));

        Assertions.assertNotNull(response);
        assertEquals(SAVINGS_CATEGORY_NAME, response.getJobName());
        assertTrue(response.getAvailableBusinessSteps().size() > 0);
        assertTrue(response.getAvailableBusinessSteps().stream()
                .anyMatch(businessStep -> POST_INTEREST_FOR_SAVINGS.equals(businessStep.getStepName())));
    }

    @Test
    public void shouldConfigureSavingsCobWithASingleStep() {
        // Change B: the Savings COB job must accept its own steps (here only POST_INTEREST_FOR_SAVINGS).
        List<BusinessStep> requestBody = new ArrayList<>();
        requestBody.add(getBusinessStep(1L, POST_INTEREST_FOR_SAVINGS));
        updateSavingsCobSteps(requestBody);

        JobBusinessStepConfigData newStepConfig = getConfiguredSavingsCobSteps();
        assertEquals(SAVINGS_JOB_NAME, newStepConfig.getJobName());
        assertEquals(1, newStepConfig.getBusinessSteps().size());
        BusinessStep postInterestStep = newStepConfig.getBusinessSteps().get(0);
        assertEquals(POST_INTEREST_FOR_SAVINGS, postInterestStep.getStepName());
        assertEquals(1L, postInterestStep.getOrder());
    }

    @Test
    public void shouldReorderSavingsCobSteps() {
        List<BusinessStep> requestBody = new ArrayList<>();
        requestBody.add(getBusinessStep(1L, PAY_DUE_SAVINGS_CHARGES));
        requestBody.add(getBusinessStep(2L, POST_INTEREST_FOR_SAVINGS));
        updateSavingsCobSteps(requestBody);

        JobBusinessStepConfigData newStepConfig = getConfiguredSavingsCobSteps();
        assertEquals(2, newStepConfig.getBusinessSteps().size());
        BusinessStep payDueStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> PAY_DUE_SAVINGS_CHARGES.equals(businessStep.getStepName())).findFirst().orElseThrow();
        BusinessStep postInterestStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> POST_INTEREST_FOR_SAVINGS.equals(businessStep.getStepName())).findFirst().orElseThrow();
        assertEquals(1L, payDueStep.getOrder());
        assertEquals(2L, postInterestStep.getOrder());
    }

    @Test
    public void shouldRejectALoanStepOnTheSavingsCob() {
        // Validation must be per-job: a Loan COB step is not configurable on the Savings COB.
        List<BusinessStep> requestBody = new ArrayList<>();
        requestBody.add(getBusinessStep(1L, APPLY_CHARGE_TO_OVERDUE_LOANS));
        CallFailedRuntimeException exception = fail(() -> fineractClient().businessStepConfiguration()
                .updateJobBusinessStepConfigWithHttpInfo(SAVINGS_JOB_NAME, new BusinessStepRequest().businessSteps(requestBody), Map.of()));
        assertEquals(400, exception.getStatus());
        assertEquals("[" + APPLY_CHARGE_TO_OVERDUE_LOANS + "] Business steps are not configurable for this job.",
                exception.getDeveloperMessage());
    }

    private JobBusinessStepConfigData getConfiguredSavingsCobSteps() {
        return ok(() -> fineractClient().businessStepConfiguration().retrieveAllConfiguredBusinessStep(SAVINGS_JOB_NAME));
    }

    private void updateSavingsCobSteps(List<BusinessStep> businessSteps) {
        // The update answers 204 with an empty body, so the status is read off the raw response.
        ApiResponse<Void> response = ok(() -> fineractClient().businessStepConfiguration().updateJobBusinessStepConfigWithHttpInfo(
                SAVINGS_JOB_NAME, new BusinessStepRequest().businessSteps(businessSteps), Map.of()));
        assertEquals(204, response.getStatusCode());
    }

    private BusinessStep getBusinessStep(Long order, String stepName) {
        BusinessStep businessStep = new BusinessStep();
        businessStep.setStepName(stepName);
        businessStep.setOrder(order);
        return businessStep;
    }
}
