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
package org.apache.fineract.integrationtests.common.workingcapitalloan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.services.WorkingCapitalLoansApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

/**
 * Integration-test helper for Working Capital Loan applications using the generated Feign client.
 */
public class WorkingCapitalLoanApplicationHelper {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getShared();

    public WorkingCapitalLoanApplicationHelper() {}

    private static WorkingCapitalLoansApi api() {
        return FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoans();
    }

    public Long submit(final String jsonBody) {
        PostWorkingCapitalLoansRequest request = fromJson(jsonBody, PostWorkingCapitalLoansRequest.class);
        PostWorkingCapitalLoansResponse response = FeignCalls.ok(() -> api().submitWorkingCapitalLoanApplication(request));
        return response.getResourceId();
    }

    public Long modifyById(final Long loanId, final String jsonBody) {
        PutWorkingCapitalLoansLoanIdRequest request = fromJson(jsonBody, PutWorkingCapitalLoansLoanIdRequest.class);
        return FeignCalls.ok(() -> api().modifyWorkingCapitalLoanApplicationById(loanId, request, Map.of())).getResourceId();
    }

    public Long modifyByExternalId(final String externalId, final String jsonBody) {
        PutWorkingCapitalLoansLoanIdRequest request = fromJson(jsonBody, PutWorkingCapitalLoansLoanIdRequest.class);
        return FeignCalls.ok(() -> api().modifyWorkingCapitalLoanApplicationByExternalId(externalId, request, Map.of())).getResourceId();
    }

    public Long deleteById(final Long loanId) {
        return FeignCalls.ok(() -> api().deleteWorkingCapitalLoanApplication(loanId)).getResourceId();
    }

    public Long deleteByExternalId(final String externalId) {
        return FeignCalls.ok(() -> api().deleteWorkingCapitalLoanApplicationByExternalId(externalId)).getResourceId();
    }

    public String retrieveById(final Long loanId) {
        GetWorkingCapitalLoansLoanIdResponse response = FeignCalls.ok(() -> api().retrieveWorkingCapitalLoanById(loanId));
        return toJson(response);
    }

    public String retrieveByExternalId(final String externalId) {
        GetWorkingCapitalLoansLoanIdResponse response = FeignCalls.ok(() -> api().retrieveWorkingCapitalLoanByExternalId(externalId));
        return toJson(response);
    }

    public String retrieveAllPagedRaw(final Map<String, Object> queryParams) {
        Map<String, Object> params = queryParams != null ? queryParams : Map.of();
        Object response = FeignCalls.ok(() -> api().retrieveAllWorkingCapitalLoans(params));
        return toJson(response);
    }

    public String retrieveTemplateRaw(final Map<String, Object> queryParams) {
        Map<String, Object> params = queryParams != null ? queryParams : Map.of();
        Object response = FeignCalls.ok(() -> api().retrieveWorkingCapitalLoanTemplate(params));
        return toJson(response);
    }

    /**
     * For validation tests: run submit expecting failure.
     */
    public CallFailedRuntimeException runSubmitExpectingFailure(final String jsonBody) {
        PostWorkingCapitalLoansRequest request = fromJson(jsonBody, PostWorkingCapitalLoansRequest.class);
        return FeignCalls.fail(() -> api().submitWorkingCapitalLoanApplication(request));
    }

    /**
     * For validation tests: run modify expecting failure.
     */
    public CallFailedRuntimeException runModifyExpectingFailure(final Long loanId, final String jsonBody) {
        PutWorkingCapitalLoansLoanIdRequest request = fromJson(jsonBody, PutWorkingCapitalLoansLoanIdRequest.class);
        return FeignCalls.fail(() -> api().modifyWorkingCapitalLoanApplicationById(loanId, request, Map.of()));
    }

    private static <T> T fromJson(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON for " + type.getSimpleName(), e);
        }
    }

    private static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize response", e);
        }
    }
}
