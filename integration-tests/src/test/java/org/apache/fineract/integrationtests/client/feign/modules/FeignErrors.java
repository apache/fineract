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
package org.apache.fineract.integrationtests.client.feign.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FeignException;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;

/** Reads the error a failed call actually reported. */
public final class FeignErrors {

    private static final String VALIDATION_WRAPPER = "validation.msg.validation.errors.exist";
    private static final String DOMAIN_RULE_WRAPPER = "validation.msg.domain.rule.violation";

    private FeignErrors() {}

    /**
     * The globalisation code of the first reported error.
     * <p>
     * A validation failure answers with a generic wrapper code at the top level and the code that names the actual
     * problem one level down in {@code errors[0]}, so reading only the top level tells every validation failure apart
     * from every other one -- which is to say, not at all.
     */
    public static String errorGlobalisationCode(CallFailedRuntimeException exception) {
        if (!(exception.getCause() instanceof FeignException feignException)) {
            return exception.getUserMessageGlobalisationCode();
        }
        String topLevelCode = feignException.getUserMessageGlobalisationCode();
        if (topLevelCode != null && !topLevelCode.equals(VALIDATION_WRAPPER) && !topLevelCode.equals(DOMAIN_RULE_WRAPPER)) {
            return topLevelCode;
        }
        try {
            Map<String, Object> body = ObjectMapperFactory.getShared().readValue(feignException.responseBodyAsString(),
                    new TypeReference<Map<String, Object>>() {});
            Object errors = body.get("errors");
            if (errors instanceof List<?> errorList && !errorList.isEmpty() && errorList.get(0) instanceof Map<?, ?> firstError) {
                return (String) firstError.get("userMessageGlobalisationCode");
            }
        } catch (Exception ignored) {
            // an error body that is not the standard envelope leaves the top-level code as the best answer
        }
        return topLevelCode;
    }

    /** The entries of the error envelope's {@code errors} list. */
    public static List<ReportedError> reportedErrors(CallFailedRuntimeException exception) {
        try {
            List<ReportedError> reported = new ArrayList<>();
            for (JsonNode error : ObjectMapperFactory.getShared().readTree(exception.getResponseBody()).path("errors")) {
                reported.add(new ReportedError(error.path("userMessageGlobalisationCode").asText(null),
                        error.path("defaultUserMessage").asText(null), error.path("developerMessage").asText(null),
                        error.path("parameterName").asText(null)));
            }
            return reported;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error body is not the standard envelope: " + exception.getResponseBody(), e);
        }
    }

    /** The first entry of the error envelope's {@code errors} list. */
    public static ReportedError firstError(CallFailedRuntimeException exception) {
        return reportedErrors(exception).get(0);
    }

    /**
     * The rejection an error body reports at its top level. A request body that cannot be read names the offending
     * field and value there, with no {@code errors} list beneath it.
     */
    public static RejectedValue rejectedValue(CallFailedRuntimeException exception) {
        try {
            JsonNode body = ObjectMapperFactory.getShared().readTree(exception.getResponseBody());
            return new RejectedValue(body.path("userMessageGlobalisationCode").asText(null), body.path("parameterName").asText(null),
                    body.path("value").asText(null));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error body is not the standard envelope: " + exception.getResponseBody(), e);
        }
    }

    public record ReportedError(String userMessageGlobalisationCode, String defaultUserMessage, String developerMessage,
            String parameterName) {
    }

    public record RejectedValue(String userMessageGlobalisationCode, String parameterName, String value) {
    }
}
