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
package org.apache.fineract.client.feign.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception thrown by {@link FeignCalls} utility when Feign calls fail.
 */
@Slf4j
@Getter
public class CallFailedRuntimeException extends RuntimeException {

    private final int status;
    private final String developerMessage;

    public CallFailedRuntimeException(FeignException cause) {
        super(createMessage(cause), cause);
        this.status = cause.status();
        this.developerMessage = extractDeveloperMessage(cause);
    }

    private static String createMessage(FeignException e) {
        StringBuilder sb = new StringBuilder("HTTP failed: status=").append(e.status());

        if (e.request() != null) {
            sb.append(", request=").append(e.request().url());
        }

        String contentString = e.contentUTF8();
        if (contentString != null && !contentString.isEmpty()) {
            sb.append(", errorBody=").append(contentString);
        }

        return sb.toString();
    }

    private static String extractDeveloperMessage(FeignException e) {
        try {
            byte[] content = e.content();
            if (content == null || content.length == 0) {
                return e.getMessage();
            }

            String contentString = new String(content, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(contentString);

            if (root.has("developerMessage")) {
                return root.get("developerMessage").asText();
            }

            if (root.has("errors")) {
                JsonNode errors = root.get("errors");
                if (errors.isArray() && errors.size() > 0) {
                    JsonNode firstError = errors.get(0);
                    if (firstError.has("developerMessage")) {
                        return firstError.get("developerMessage").asText();
                    }
                }
            }

            return contentString;
        } catch (IOException ex) {
            log.warn("Failed to extract developer message from error response", ex);
            return e.getMessage();
        }
    }
}
