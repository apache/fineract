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
package org.apache.fineract.client.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;

public class FineractErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper objectMapper = ObjectMapperFactory.getShared();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.body() != null) {
                byte[] bodyData = readResponseBody(response);

                try {
                    JsonNode rootNode = objectMapper.readTree(bodyData);

                    String developerMessage = extractField(rootNode, "developerMessage");
                    String userMessage = extractField(rootNode, "userMessage");
                    String validationErrors = extractValidationErrors(rootNode);

                    if (developerMessage != null || userMessage != null || validationErrors != null) {
                        String enhancedDeveloperMessage = developerMessage;
                        if (validationErrors != null) {
                            enhancedDeveloperMessage = validationErrors;
                        }
                        return new FeignException(response.status(), userMessage != null ? userMessage : enhancedDeveloperMessage,
                                response.request(), bodyData, enhancedDeveloperMessage, userMessage);
                    }
                } catch (IOException e) {
                    return defaultDecoder.decode(methodKey, response);
                }
            }
        } catch (IOException e) {
            return defaultDecoder.decode(methodKey, response);
        }

        return defaultDecoder.decode(methodKey, response);
    }

    private byte[] readResponseBody(Response response) throws IOException {
        if (response.body() == null) {
            return new byte[0];
        }

        try (InputStream inputStream = response.body().asInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    private String extractField(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    private String extractValidationErrors(JsonNode rootNode) {
        JsonNode errorsNode = rootNode.get("errors");
        if (errorsNode != null && errorsNode.isArray() && errorsNode.size() > 0) {
            StringBuilder errors = new StringBuilder("Validation errors: ");
            for (JsonNode error : errorsNode) {
                String parameterName = extractField(error, "parameterName");
                String defaultUserMessage = extractField(error, "defaultUserMessage");
                String developerMessage = extractField(error, "developerMessage");

                if (errors.length() > "Validation errors: ".length()) {
                    errors.append("; ");
                }

                if (parameterName != null) {
                    errors.append("[").append(parameterName).append("] ");
                }

                if (defaultUserMessage != null) {
                    errors.append(defaultUserMessage);
                } else if (developerMessage != null) {
                    errors.append(developerMessage);
                }
            }
            return errors.length() > "Validation errors: ".length() ? errors.toString() : null;
        }
        return null;
    }
}
