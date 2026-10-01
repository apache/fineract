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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.JobBusinessStepConfigData;
import org.apache.fineract.client.models.JobBusinessStepDetail;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BusinessConfigurationApiTest extends FeignIntegrationTest {

    public static final String LOAN_JOB_NAME = "LOAN_CLOSE_OF_BUSINESS";
    public static final String LOAN_CATEGORY_NAME = "loan";
    public static final String APPLY_CHARGE_TO_OVERDUE_LOANS = "APPLY_CHARGE_TO_OVERDUE_LOANS";
    public static final String NOT_BELONGING_BUSINESS_STEP_NAME = "APPLY_CHARGE_TO_OVERDUE_LOANS_2";
    public static final String LOAN_DELINQUENCY_CLASSIFICATION = "LOAN_DELINQUENCY_CLASSIFICATION";

    private FeignBusinessStepHelper businessStepHelper;

    @BeforeAll
    public void setup() {
        businessStepHelper = new FeignBusinessStepHelper(fineractClient());
    }

    @Test
    public void shouldReturnApplyChargeToOverdueLoanStepConfig() {
        JobBusinessStepConfigData response = businessStepHelper.getConfiguredBusinessStepsByJobName(LOAN_JOB_NAME);

        Assertions.assertNotNull(response);
        assertEquals(LOAN_JOB_NAME, response.getJobName());
        assertTrue(response.getBusinessSteps().size() > 0);
        assertTrue(response.getBusinessSteps().stream()
                .anyMatch(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())));
    }

    @Test
    public void shouldReturnApplyChargeToOverdueLoanStepConfigByJobCategory() {
        JobBusinessStepDetail response = businessStepHelper.getAvailableBusinessStepsByJobName(LOAN_CATEGORY_NAME);

        Assertions.assertNotNull(response);
        assertEquals(LOAN_CATEGORY_NAME, response.getJobName());
        assertTrue(response.getAvailableBusinessSteps().size() > 0);
        assertTrue(response.getAvailableBusinessSteps().stream()
                .anyMatch(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())));
    }

    @Test
    public void shouldUpdateStepOrder() {
        JobBusinessStepConfigData originalStepConfig = businessStepHelper.getConfiguredBusinessStepsByJobName(LOAN_JOB_NAME);

        List<BusinessStep> requestBody = new ArrayList<>();
        requestBody.add(getBusinessSteps(1L, APPLY_CHARGE_TO_OVERDUE_LOANS));
        businessStepHelper.updateBusinessSteps(LOAN_JOB_NAME, requestBody);

        JobBusinessStepConfigData newStepConfig = businessStepHelper.getConfiguredBusinessStepsByJobName(LOAN_JOB_NAME);
        BusinessStep applyChargeStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())).findFirst().get();
        assertEquals(1, newStepConfig.getBusinessSteps().size());
        assertEquals(1L, applyChargeStep.getOrder());

        requestBody.add(getBusinessSteps(2L, LOAN_DELINQUENCY_CLASSIFICATION));

        businessStepHelper.updateBusinessSteps(LOAN_JOB_NAME, requestBody);
        newStepConfig = businessStepHelper.getConfiguredBusinessStepsByJobName(LOAN_JOB_NAME);
        applyChargeStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())).findFirst().get();
        BusinessStep loanDelinquencyStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> LOAN_DELINQUENCY_CLASSIFICATION.equals(businessStep.getStepName())).findFirst().get();
        assertEquals(2, newStepConfig.getBusinessSteps().size());
        assertEquals(1L, applyChargeStep.getOrder());
        assertEquals(2L, loanDelinquencyStep.getOrder());

        requestBody.remove(1);
        businessStepHelper.updateBusinessSteps(LOAN_JOB_NAME, requestBody);

        newStepConfig = businessStepHelper.getConfiguredBusinessStepsByJobName(LOAN_JOB_NAME);
        applyChargeStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())).findFirst().get();
        assertEquals(1, newStepConfig.getBusinessSteps().size());
        assertEquals(1L, applyChargeStep.getOrder());

        businessStepHelper.updateBusinessSteps(LOAN_JOB_NAME, originalStepConfig.getBusinessSteps());
    }

    @Test
    public void shouldThrowErrorWhenABusinessStepDoesNotBelongToTheGivenJob() {
        List<BusinessStep> requestBody = new ArrayList<>();
        requestBody.add(getBusinessSteps(1L, NOT_BELONGING_BUSINESS_STEP_NAME));
        CallFailedRuntimeException response = businessStepHelper.updateBusinessStepsExpectingError(LOAN_JOB_NAME, requestBody);
        assertEquals(400, response.getStatus());
        assertEquals("[APPLY_CHARGE_TO_OVERDUE_LOANS_2] Business steps are not configurable for this job.", response.getDeveloperMessage());
    }

    @Test
    public void shouldThrowErrorWhenBusinessStepListIsEmpty() {
        List<BusinessStep> requestBody = Collections.emptyList();
        CallFailedRuntimeException response = businessStepHelper.updateBusinessStepsExpectingError(LOAN_JOB_NAME, requestBody);
        assertEquals(400, response.getStatus());
        assertEquals("A job needs to have 1 business step at least.", response.getDeveloperMessage());
    }

    private BusinessStep getBusinessSteps(Long order, String stepName) {
        return new BusinessStep().stepName(stepName).order(order);
    }
}
