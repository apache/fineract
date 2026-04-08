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
package org.apache.fineract.integrationtests.common.workingcapitalloanbreach;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.services.WorkingCapitalBreachApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.WorkingCapitalBreachData;
import org.apache.fineract.client.models.WorkingCapitalBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalBreachTemplateResponse;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

public class WorkingCapitalBreachHelper {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getShared();
    private static final ObjectMapper RESPONSE_OBJECT_MAPPER = ObjectMapperFactory.getShared().copy()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private static WorkingCapitalBreachApi api() {
        return FineractFeignClientHelper.getFineractFeignClient().workingCapitalBreaches();
    }

    public Long create(final JsonObject body) {
        final WorkingCapitalBreachRequest request = fromJson(body, WorkingCapitalBreachRequest.class);
        final CommandProcessingResult response = FeignCalls.ok(() -> api().createWorkingCapitalBreach(request));
        return response.getResourceId();
    }

    public Long update(final Long breachId, final JsonObject body) {
        final WorkingCapitalBreachRequest request = fromJson(body, WorkingCapitalBreachRequest.class);
        final CommandProcessingResult response = FeignCalls.ok(() -> api().updateWorkingCapitalBreach(breachId, request));
        return response.getResourceId();
    }

    public Long delete(final Long breachId) {
        final CommandProcessingResult response = FeignCalls.ok(() -> api().deleteWorkingCapitalBreach(breachId));
        return response.getResourceId();
    }

    public String retrieveTemplateRaw() {
        final WorkingCapitalBreachTemplateResponse response = FeignCalls.ok(() -> api().retrieveWorkingCapitalBreachTemplate());
        return toJson(response);
    }

    public String retrieveAllRaw() {
        final java.util.List<WorkingCapitalBreachData> response = FeignCalls.ok(() -> api().retrieveAllWorkingCapitalBreaches());
        return toJson(response);
    }

    public String retrieveOneRaw(final Long breachId) {
        final WorkingCapitalBreachData response = FeignCalls.ok(() -> api().retrieveWorkingCapitalBreach(breachId));
        return toJson(response);
    }

    public CallFailedRuntimeException runCreateExpectingFailure(final JsonObject body) {
        final WorkingCapitalBreachRequest request = fromJson(body, WorkingCapitalBreachRequest.class);
        return FeignCalls.fail(() -> api().createWorkingCapitalBreach(request));
    }

    public CallFailedRuntimeException runUpdateExpectingFailure(final Long breachId, final JsonObject body) {
        final WorkingCapitalBreachRequest request = fromJson(body, WorkingCapitalBreachRequest.class);
        return FeignCalls.fail(() -> api().updateWorkingCapitalBreach(breachId, request));
    }

    public CallFailedRuntimeException runRetrieveOneExpectingFailure(final Long breachId) {
        return FeignCalls.fail(() -> api().retrieveWorkingCapitalBreach(breachId));
    }

    public CallFailedRuntimeException runDeleteExpectingFailure(final Long breachId) {
        return FeignCalls.fail(() -> api().deleteWorkingCapitalBreach(breachId));
    }

    private static <T> T fromJson(final JsonObject json, final Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json.toString(), type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid breach JSON for " + type.getSimpleName(), e);
        }
    }

    private static String toJson(final Object value) {
        try {
            return RESPONSE_OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize breach response", e);
        }
    }

}
