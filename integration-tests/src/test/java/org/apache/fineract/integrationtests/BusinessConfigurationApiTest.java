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
import org.apache.fineract.integrationtests.common.BusinessStepConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BusinessConfigurationApiTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private static final String LOAN_JOB_NAME = "LOAN_CLOSE_OF_BUSINESS";
    private static final String LOAN_CATEGORY_NAME = "loan";
    private static final String APPLY_CHARGE_TO_OVERDUE_LOANS = "APPLY_CHARGE_TO_OVERDUE_LOANS";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void shouldReturnApplyChargeToOverdueLoanStepConfig() {
        JobBusinessStepConfigData response = BusinessStepConfigurationHelper.getConfiguredBusinessStepsByJobName(requestSpec, responseSpec,
                LOAN_JOB_NAME);

        Assertions.assertNotNull(response);
        assertEquals(LOAN_JOB_NAME, response.getJobName());
        assertTrue(response.getBusinessSteps().size() > 0);
        assertTrue(response.getBusinessSteps().stream()
                .anyMatch(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())));
    }

    @Test
    public void shouldReturnApplyChargeToOverdueLoanStepConfigByJobCategory() {
        JobBusinessStepDetail response = BusinessStepConfigurationHelper.getAvailableBusinessStepsByJobName(requestSpec, responseSpec,
                LOAN_CATEGORY_NAME);

        Assertions.assertNotNull(response);
        assertEquals(LOAN_CATEGORY_NAME, response.getJobName());
        assertTrue(response.getAvailableBusinessSteps().size() > 0);
        assertTrue(response.getAvailableBusinessSteps().stream()
                .anyMatch(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())));
    }

    @Test
    public void shouldUpdateStepOrder() {
        JobBusinessStepConfigData originalStepConfig = BusinessStepConfigurationHelper.getConfiguredBusinessStepsByJobName(requestSpec,
                responseSpec, LOAN_JOB_NAME);
        Long originalOrder = originalStepConfig.getBusinessSteps().stream() //
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())) //
                .findFirst().get().getOrder();

        List<BusinessStep> requestBody = getBusinessSteps(originalOrder + 1);
        BusinessStepConfigurationHelper.updateBusinessStepOrder(requestSpec, responseSpec, LOAN_JOB_NAME,
                BusinessStepConfigurationHelper.toJsonString(requestBody));

        JobBusinessStepConfigData newStepConfig = BusinessStepConfigurationHelper.getConfiguredBusinessStepsByJobName(requestSpec,
                responseSpec, LOAN_JOB_NAME);
        Long newOrder = newStepConfig.getBusinessSteps().stream() //
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())) //
                .findFirst().get().getOrder();
        assertEquals(originalOrder + 1, newOrder);

        List<BusinessStep> originalBusinessSteps = getBusinessSteps(originalOrder);
        BusinessStepConfigurationHelper.updateBusinessStepOrder(requestSpec, responseSpec, LOAN_JOB_NAME,
                BusinessStepConfigurationHelper.toJsonString(originalBusinessSteps));
    }

    private List<BusinessStep> getBusinessSteps(Long order) {
        List<BusinessStep> businessSteps = new ArrayList<>();
        BusinessStep businessStep = new BusinessStep();
        businessStep.setStepName(APPLY_CHARGE_TO_OVERDUE_LOANS);
        businessStep.setOrder(order);
        businessSteps.add(businessStep);
        return businessSteps;
    }
}
