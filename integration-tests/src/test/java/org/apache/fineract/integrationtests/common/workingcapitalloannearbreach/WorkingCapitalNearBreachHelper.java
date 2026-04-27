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
package org.apache.fineract.integrationtests.common.workingcapitalloannearbreach;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.services.WorkingCapitalNearBreachApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.WorkingCapitalNearBreachData;
import org.apache.fineract.client.models.WorkingCapitalNearBreachRequest;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

public class WorkingCapitalNearBreachHelper {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getShared();
    private static final ObjectMapper RESPONSE_OBJECT_MAPPER = ObjectMapperFactory.getShared().copy()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private static WorkingCapitalNearBreachApi api() {
        return FineractFeignClientHelper.getFineractFeignClient().workingCapitalNearBreaches();
    }

    public Long create(final JsonObject body) {
        final WorkingCapitalNearBreachRequest request = fromJson(body, WorkingCapitalNearBreachRequest.class);
        final CommandProcessingResult response = FeignCalls.ok(() -> api().createWorkingCapitalNearBreach(request));
        return response.getResourceId();
    }

    public Long update(final Long breachId, final JsonObject body) {
        final WorkingCapitalNearBreachRequest request = fromJson(body, WorkingCapitalNearBreachRequest.class);
        final CommandProcessingResult response = FeignCalls.ok(() -> api().updateWorkingCapitalNearBreach(breachId, request));
        return response.getResourceId();
    }

    public Long delete(final Long breachId) {
        final CommandProcessingResult response = FeignCalls.ok(() -> api().deleteWorkingCapitalNearBreach(breachId));
        return response.getResourceId();
    }

    public String retrieveAllRaw() {
        final java.util.List<WorkingCapitalNearBreachData> response = FeignCalls.ok(() -> api().retrieveAllWorkingCapitalNearBreaches());
        return toJson(response);
    }

    public String retrieveOneRaw(final Long breachId) {
        final WorkingCapitalNearBreachData response = FeignCalls.ok(() -> api().retrieveWorkingCapitalNearBreach(breachId));
        return toJson(response);
    }

    public CallFailedRuntimeException runCreateExpectingFailure(final JsonObject body) {
        final WorkingCapitalNearBreachRequest request = fromJson(body, WorkingCapitalNearBreachRequest.class);
        return FeignCalls.fail(() -> api().createWorkingCapitalNearBreach(request));
    }

    public CallFailedRuntimeException runUpdateExpectingFailure(final Long breachId, final JsonObject body) {
        final WorkingCapitalNearBreachRequest request = fromJson(body, WorkingCapitalNearBreachRequest.class);
        return FeignCalls.fail(() -> api().updateWorkingCapitalNearBreach(breachId, request));
    }

    public CallFailedRuntimeException runRetrieveOneExpectingFailure(final Long breachId) {
        return FeignCalls.fail(() -> api().retrieveWorkingCapitalNearBreach(breachId));
    }

    public CallFailedRuntimeException runDeleteExpectingFailure(final Long breachId) {
        return FeignCalls.fail(() -> api().deleteWorkingCapitalNearBreach(breachId));
    }

    public WorkingCapitalNearBreachData retrieveWorkingCapitalNearBreach(final Long breachId) {
        return FeignCalls.ok(() -> api().retrieveWorkingCapitalNearBreach(breachId));
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

    public JsonObject nearBreachJson(final String name, final Integer frequency, final String frequencyType, final BigDecimal threshold) {
        final JsonObject json = new JsonObject();
        json.addProperty("nearBreachName", name);
        json.addProperty("nearBreachFrequency", frequency);
        json.addProperty("nearBreachFrequencyType", frequencyType);
        json.addProperty("nearBreachThreshold", threshold);
        return json;
    }

}
