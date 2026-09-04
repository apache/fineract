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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.cob.data.BusinessStep;
import org.apache.fineract.cob.data.JobBusinessStepConfigData;
import org.apache.fineract.cob.data.JobBusinessStepDetail;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.integrationtests.common.BusinessStepConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the per-job business step configuration endpoint ({@code /v1/jobs/{jobName}/steps}) also works for the
 * Savings COB, not only for the Loan COB. See FINERACT-2332.
 */
public class SavingsBusinessStepConfigurationApiTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    public static final String SAVINGS_JOB_NAME = "SAVINGS_CLOSE_OF_BUSINESS";
    public static final String SAVINGS_CATEGORY_NAME = "savings";
    public static final String POST_INTEREST_FOR_SAVINGS = "POST_INTEREST_FOR_SAVINGS";
    public static final String PAY_DUE_SAVINGS_CHARGES = "PAY_DUE_SAVINGS_CHARGES";
    // A step that belongs to the Loan COB, used to assert it cannot be configured on the Savings COB.
    public static final String APPLY_CHARGE_TO_OVERDUE_LOANS = "APPLY_CHARGE_TO_OVERDUE_LOANS";

    private List<BusinessStep> originalSteps;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        // Snapshot the seeded Savings COB step configuration so it can be restored after each test.
        this.originalSteps = BusinessStepConfigurationHelper
                .getConfiguredBusinessStepsByJobName(requestSpec, responseSpec, SAVINGS_JOB_NAME).getBusinessSteps();
    }

    @AfterEach
    public void restoreOriginalConfiguration() {
        ResponseSpecification updateResponseSpec = new ResponseSpecBuilder().expectStatusCode(204).build();
        BusinessStepConfigurationHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, SAVINGS_JOB_NAME,
                BusinessStepConfigurationHelper.toJsonString(originalSteps));
    }

    @Test
    public void shouldExposeSavingsCobAvailableBusinessSteps() {
        // Change A: the SAVINGS category must be registered, so available steps are resolvable by category name.
        JobBusinessStepDetail response = BusinessStepConfigurationHelper.getAvailableBusinessStepsByJobName(requestSpec, responseSpec,
                SAVINGS_CATEGORY_NAME);

        Assertions.assertNotNull(response);
        assertEquals(SAVINGS_CATEGORY_NAME, response.getJobName());
        assertTrue(response.getAvailableBusinessSteps().size() > 0);
        assertTrue(response.getAvailableBusinessSteps().stream()
                .anyMatch(businessStep -> POST_INTEREST_FOR_SAVINGS.equals(businessStep.getStepName())));
    }

    @Test
    public void shouldConfigureSavingsCobWithASingleStep() {
        // Change B: the Savings COB job must accept its own steps (here only POST_INTEREST_FOR_SAVINGS).
        ResponseSpecification updateResponseSpec = new ResponseSpecBuilder().expectStatusCode(204).build();

        List<BusinessStep> requestBody = new ArrayList<>();
        requestBody.add(getBusinessStep(1L, POST_INTEREST_FOR_SAVINGS));
        BusinessStepConfigurationHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, SAVINGS_JOB_NAME,
                BusinessStepConfigurationHelper.toJsonString(requestBody));

        JobBusinessStepConfigData newStepConfig = BusinessStepConfigurationHelper.getConfiguredBusinessStepsByJobName(requestSpec,
                responseSpec, SAVINGS_JOB_NAME);
        assertEquals(SAVINGS_JOB_NAME, newStepConfig.getJobName());
        assertEquals(1, newStepConfig.getBusinessSteps().size());
        BusinessStep postInterestStep = newStepConfig.getBusinessSteps().get(0);
        assertEquals(POST_INTEREST_FOR_SAVINGS, postInterestStep.getStepName());
        assertEquals(1L, postInterestStep.getOrder());
    }

    @Test
    public void shouldReorderSavingsCobSteps() {
        ResponseSpecification updateResponseSpec = new ResponseSpecBuilder().expectStatusCode(204).build();

        List<BusinessStep> requestBody = new ArrayList<>();
        requestBody.add(getBusinessStep(1L, PAY_DUE_SAVINGS_CHARGES));
        requestBody.add(getBusinessStep(2L, POST_INTEREST_FOR_SAVINGS));
        BusinessStepConfigurationHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, SAVINGS_JOB_NAME,
                BusinessStepConfigurationHelper.toJsonString(requestBody));

        JobBusinessStepConfigData newStepConfig = BusinessStepConfigurationHelper.getConfiguredBusinessStepsByJobName(requestSpec,
                responseSpec, SAVINGS_JOB_NAME);
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
        ResponseSpecification responseSpecForError = new ResponseSpecBuilder().expectStatusCode(400).build();
        List<BusinessStep> requestBody = new ArrayList<>();
        requestBody.add(getBusinessStep(1L, APPLY_CHARGE_TO_OVERDUE_LOANS));
        ApiParameterError response = BusinessStepConfigurationHelper.updateBusinessStepOrderWithError(requestSpec, responseSpecForError,
                SAVINGS_JOB_NAME, BusinessStepConfigurationHelper.toJsonString(requestBody));
        assertEquals("[" + APPLY_CHARGE_TO_OVERDUE_LOANS + "] Business steps are not configurable for this job.",
                response.getDeveloperMessage());
    }

    private BusinessStep getBusinessStep(Long order, String stepName) {
        BusinessStep businessStep = new BusinessStep();
        businessStep.setStepName(stepName);
        businessStep.setOrder(order);
        return businessStep;
    }
}
