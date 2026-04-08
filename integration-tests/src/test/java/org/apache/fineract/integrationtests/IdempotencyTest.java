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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.cob.data.BusinessStep;
import org.apache.fineract.cob.data.JobBusinessStepConfigData;
import org.apache.fineract.infrastructure.core.exception.AbstractIdempotentCommandException;
import org.apache.fineract.integrationtests.common.IdempotencyHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IdempotencyTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    public static final String LOAN_JOB_NAME = "LOAN_CLOSE_OF_BUSINESS";
    public static final String LOAN_CATEGORY_NAME = "loan";
    public static final String APPLY_CHARGE_TO_OVERDUE_LOANS = "APPLY_CHARGE_TO_OVERDUE_LOANS";
    public static final String NOT_BELONGING_BUSINESS_STEP_NAME = "APPLY_CHARGE_TO_OVERDUE_LOANS_2";
    public static final String LOAN_DELINQUENCY_CLASSIFICATION = "LOAN_DELINQUENCY_CLASSIFICATION";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void shouldUpdateStepOrder() {
        ResponseSpecification updateResponseSpec = new ResponseSpecBuilder().expectStatusCode(204).build();
        JobBusinessStepConfigData originalStepConfig = IdempotencyHelper.getConfiguredBusinessStepsByJobName(requestSpec, responseSpec,
                LOAN_JOB_NAME);

        String idempotencyKeyHeader = UUID.randomUUID().toString();

        List<BusinessStep> requestBody = new ArrayList<>();
        requestBody.add(getBusinessSteps(1L, APPLY_CHARGE_TO_OVERDUE_LOANS));
        Response response = IdempotencyHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, LOAN_JOB_NAME,
                IdempotencyHelper.toJsonString(requestBody), idempotencyKeyHeader);
        Response responseSecond = IdempotencyHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, LOAN_JOB_NAME,
                IdempotencyHelper.toJsonString(requestBody), idempotencyKeyHeader);
        assertEquals(response.getBody().asString(), responseSecond.getBody().asString());
        assertNull(response.header(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertNotNull(responseSecond.header(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));

        idempotencyKeyHeader = UUID.randomUUID().toString();

        JobBusinessStepConfigData newStepConfig = IdempotencyHelper.getConfiguredBusinessStepsByJobName(requestSpec, responseSpec,
                LOAN_JOB_NAME);
        BusinessStep applyChargeStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())).findFirst().get();
        assertEquals(1, newStepConfig.getBusinessSteps().size());
        assertEquals(1L, applyChargeStep.getOrder());

        requestBody.add(getBusinessSteps(2L, LOAN_DELINQUENCY_CLASSIFICATION));

        Response update = IdempotencyHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, LOAN_JOB_NAME,
                IdempotencyHelper.toJsonString(requestBody), idempotencyKeyHeader);
        Response updateSecond = IdempotencyHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, LOAN_JOB_NAME,
                IdempotencyHelper.toJsonString(requestBody), idempotencyKeyHeader);
        assertNull(update.header(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertNotNull(updateSecond.header(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertEquals(update.getBody().asString(), updateSecond.getBody().asString());

        newStepConfig = IdempotencyHelper.getConfiguredBusinessStepsByJobName(requestSpec, responseSpec, LOAN_JOB_NAME);
        applyChargeStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())).findFirst().get();
        BusinessStep loanDelinquencyStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> LOAN_DELINQUENCY_CLASSIFICATION.equals(businessStep.getStepName())).findFirst().get();
        assertEquals(2, newStepConfig.getBusinessSteps().size());
        assertEquals(1L, applyChargeStep.getOrder());
        assertEquals(2L, loanDelinquencyStep.getOrder());

        requestBody.remove(1);
        idempotencyKeyHeader = UUID.randomUUID().toString();
        update = IdempotencyHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, LOAN_JOB_NAME,
                IdempotencyHelper.toJsonString(requestBody), idempotencyKeyHeader);
        updateSecond = IdempotencyHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, LOAN_JOB_NAME,
                IdempotencyHelper.toJsonString(requestBody), idempotencyKeyHeader);

        assertNull(update.header(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertNotNull(updateSecond.header(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertEquals(update.getBody().asString(), updateSecond.getBody().asString());

        newStepConfig = IdempotencyHelper.getConfiguredBusinessStepsByJobName(requestSpec, responseSpec, LOAN_JOB_NAME);
        applyChargeStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())).findFirst().get();
        assertEquals(1, newStepConfig.getBusinessSteps().size());
        assertEquals(1L, applyChargeStep.getOrder());

        idempotencyKeyHeader = UUID.randomUUID().toString();

        update = IdempotencyHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, LOAN_JOB_NAME,
                IdempotencyHelper.toJsonString(originalStepConfig.getBusinessSteps()), idempotencyKeyHeader);
        updateSecond = IdempotencyHelper.updateBusinessStepOrder(requestSpec, updateResponseSpec, LOAN_JOB_NAME,
                IdempotencyHelper.toJsonString(originalStepConfig.getBusinessSteps()), idempotencyKeyHeader);

        assertNull(update.header(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertNotNull(updateSecond.header(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertEquals(update.getBody().asString(), updateSecond.getBody().asString());

    }

    @Test
    public void shouldTheSecondRequestWithSameIdempotencyKeyWillFailureToo() {
        ResponseSpecification responseSpecForError = new ResponseSpecBuilder().expectStatusCode(400).build();
        List<BusinessStep> requestBody = new ArrayList<>();
        String idempotencyKey = UUID.randomUUID().toString();
        // IdempotencyHelper.configuredApiParameterErrorFromJsonString(response.getBody().asString())

        Response response1 = IdempotencyHelper.updateBusinessStepOrderWithError(requestSpec, responseSpecForError, LOAN_JOB_NAME,
                IdempotencyHelper.toJsonString(requestBody), idempotencyKey);
        assertNull(response1.getHeader(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        ResponseBody body1 = response1.getBody();
        assertNotNull(body1);

        Response response2 = IdempotencyHelper.updateBusinessStepOrderWithError(requestSpec, responseSpecForError, LOAN_JOB_NAME,
                IdempotencyHelper.toJsonString(requestBody), idempotencyKey);
        assertNotNull(response2.getHeader(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertEquals((Map) body1.jsonPath().get(""), response2.getBody().jsonPath().get(""));
    }

    private BusinessStep getBusinessSteps(Long order, String stepName) {
        BusinessStep businessStep = new BusinessStep();
        businessStep.setStepName(stepName);
        businessStep.setOrder(order);
        return businessStep;
    }
}
