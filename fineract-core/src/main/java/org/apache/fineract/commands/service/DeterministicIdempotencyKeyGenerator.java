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
package org.apache.fineract.commands.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DeterministicIdempotencyKeyGenerator {

    // Plain ObjectMapper for canonicalization — must NOT use the application ObjectMapper
    // which has custom serializers/deserializers that cause failures on certain JSON payloads
    private static final ObjectMapper CANONICAL_MAPPER;

    static {
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        CANONICAL_MAPPER = new ObjectMapper(factory);
    }

    public String generate(String json, String context) {

        if (json == null || json.isBlank()) {
            // Shouldn't reach here after resolver guard, but defensive fallback
            return java.util.UUID.randomUUID().toString();
        }

        String canonical = toCanonicalString(json);
        String window = currentTimeWindow();
        return hash(canonical + ":" + context + ":" + window);
    }

    private String toCanonicalString(String json) {
        try {
            JsonNode node = CANONICAL_MAPPER.readTree(json);
            JsonNode canonical = canonicalize(node);
            return CANONICAL_MAPPER.writeValueAsString(canonical);
        } catch (Exception e) {
            throw new RuntimeException("Failed to canonicalize JSON", e);
        }
    }

    private JsonNode canonicalize(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = CANONICAL_MAPPER.createObjectNode();

            List<String> fieldNames = new ArrayList<>();
            node.fieldNames().forEachRemaining(fieldNames::add);
            Collections.sort(fieldNames);

            for (String field : fieldNames) {
                sorted.set(field, canonicalize(node.get(field))); // recursion to resolve nested obj
            }

            return sorted;
        }

        if (node.isArray()) {
            ArrayNode arrayNode = CANONICAL_MAPPER.createArrayNode();
            for (JsonNode element : node) {
                arrayNode.add(canonicalize(element)); // recursion inside array
            }
            return arrayNode;
        }

        return node; // primitives + null
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private String currentTimeWindow() {
        Instant now = Instant.now();
        long window = now.getEpochSecond() / (5 * 60);
        return String.valueOf(window);
    }
}
