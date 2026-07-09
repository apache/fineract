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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.JobBusinessStepConfigData;
import org.apache.fineract.infrastructure.core.exception.AbstractIdempotentCommandException;
import org.apache.fineract.integrationtests.common.IdempotencyHelper;
import org.junit.jupiter.api.Test;

public class IdempotencyTest {

    public static final String LOAN_JOB_NAME = "LOAN_CLOSE_OF_BUSINESS";
    public static final String LOAN_CATEGORY_NAME = "loan";
    public static final String APPLY_CHARGE_TO_OVERDUE_LOANS = "APPLY_CHARGE_TO_OVERDUE_LOANS";
    public static final String NOT_BELONGING_BUSINESS_STEP_NAME = "APPLY_CHARGE_TO_OVERDUE_LOANS_2";
    public static final String LOAN_DELINQUENCY_CLASSIFICATION = "LOAN_DELINQUENCY_CLASSIFICATION";

    @Test
    public void shouldUpdateStepOrder() {
        JobBusinessStepConfigData originalStepConfig = IdempotencyHelper.getConfiguredBusinessStepsByJobName(LOAN_JOB_NAME);

        String idempotencyKeyHeader = UUID.randomUUID().toString();

        List<BusinessStep> requestBody = new ArrayList<>();
        requestBody.add(getBusinessSteps(1L, APPLY_CHARGE_TO_OVERDUE_LOANS));
        ApiResponse<Void> response = IdempotencyHelper.updateBusinessStepOrder(LOAN_JOB_NAME, requestBody, idempotencyKeyHeader);
        ApiResponse<Void> responseSecond = IdempotencyHelper.updateBusinessStepOrder(LOAN_JOB_NAME, requestBody, idempotencyKeyHeader);
        assertFalse(hasHeader(response, AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertTrue(hasHeader(responseSecond, AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));

        idempotencyKeyHeader = UUID.randomUUID().toString();

        JobBusinessStepConfigData newStepConfig = IdempotencyHelper.getConfiguredBusinessStepsByJobName(LOAN_JOB_NAME);
        BusinessStep applyChargeStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())).findFirst().get();
        assertEquals(1, newStepConfig.getBusinessSteps().size());
        assertEquals(1L, applyChargeStep.getOrder());

        requestBody.add(getBusinessSteps(2L, LOAN_DELINQUENCY_CLASSIFICATION));

        ApiResponse<Void> update = IdempotencyHelper.updateBusinessStepOrder(LOAN_JOB_NAME, requestBody, idempotencyKeyHeader);
        ApiResponse<Void> updateSecond = IdempotencyHelper.updateBusinessStepOrder(LOAN_JOB_NAME, requestBody, idempotencyKeyHeader);
        assertFalse(hasHeader(update, AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertTrue(hasHeader(updateSecond, AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));

        newStepConfig = IdempotencyHelper.getConfiguredBusinessStepsByJobName(LOAN_JOB_NAME);
        applyChargeStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())).findFirst().get();
        BusinessStep loanDelinquencyStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> LOAN_DELINQUENCY_CLASSIFICATION.equals(businessStep.getStepName())).findFirst().get();
        assertEquals(2, newStepConfig.getBusinessSteps().size());
        assertEquals(1L, applyChargeStep.getOrder());
        assertEquals(2L, loanDelinquencyStep.getOrder());

        requestBody.remove(1);
        idempotencyKeyHeader = UUID.randomUUID().toString();
        update = IdempotencyHelper.updateBusinessStepOrder(LOAN_JOB_NAME, requestBody, idempotencyKeyHeader);
        updateSecond = IdempotencyHelper.updateBusinessStepOrder(LOAN_JOB_NAME, requestBody, idempotencyKeyHeader);

        assertFalse(hasHeader(update, AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertTrue(hasHeader(updateSecond, AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));

        newStepConfig = IdempotencyHelper.getConfiguredBusinessStepsByJobName(LOAN_JOB_NAME);
        applyChargeStep = newStepConfig.getBusinessSteps().stream()
                .filter(businessStep -> APPLY_CHARGE_TO_OVERDUE_LOANS.equals(businessStep.getStepName())).findFirst().get();
        assertEquals(1, newStepConfig.getBusinessSteps().size());
        assertEquals(1L, applyChargeStep.getOrder());

        idempotencyKeyHeader = UUID.randomUUID().toString();

        update = IdempotencyHelper.updateBusinessStepOrder(LOAN_JOB_NAME, originalStepConfig.getBusinessSteps(), idempotencyKeyHeader);
        updateSecond = IdempotencyHelper.updateBusinessStepOrder(LOAN_JOB_NAME, originalStepConfig.getBusinessSteps(),
                idempotencyKeyHeader);

        assertFalse(hasHeader(update, AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertTrue(hasHeader(updateSecond, AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
    }

    @Test
    public void shouldTheSecondRequestWithSameIdempotencyKeyWillFailureToo() {
        List<BusinessStep> requestBody = new ArrayList<>();
        String idempotencyKey = UUID.randomUUID().toString();

        CallFailedRuntimeException exception1 = IdempotencyHelper.updateBusinessStepOrderExpectingFailure(LOAN_JOB_NAME, requestBody,
                idempotencyKey);
        assertEquals(400, exception1.getStatus());
        assertFalse(hasHeader(exception1.getHeaders(), AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertNotNull(exception1.getResponseBody());

        CallFailedRuntimeException exception2 = IdempotencyHelper.updateBusinessStepOrderExpectingFailure(LOAN_JOB_NAME, requestBody,
                idempotencyKey);
        assertEquals(400, exception2.getStatus());
        assertTrue(hasHeader(exception2.getHeaders(), AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER));
        assertEquals(exception1.getResponseBody(), exception2.getResponseBody());
    }

    private boolean hasHeader(ApiResponse<?> response, String headerName) {
        return hasHeader(response.getHeaders(), headerName);
    }

    private boolean hasHeader(Map<String, Collection<String>> headers, String headerName) {
        return headers.keySet().stream().anyMatch(headerName::equalsIgnoreCase);
    }

    private BusinessStep getBusinessSteps(Long order, String stepName) {
        return new BusinessStep().stepName(stepName).order(order);
    }
}
