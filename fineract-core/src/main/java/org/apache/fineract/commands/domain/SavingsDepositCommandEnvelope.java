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
package org.apache.fineract.commands.domain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;

/** Versioned server metadata in command_as_json, restricted to DEPOSIT_SAVINGSACCOUNT. */
public final class SavingsDepositCommandEnvelope {

    public static final String METADATA = "_serverCommand";
    public static final String UNTRUSTED_ORIGIN = "error.msg.savings.deposit.untrusted.origin";

    private SavingsDepositCommandEnvelope() {}

    public static boolean appliesTo(String action, String entity) {
        return "DEPOSIT".equals(action) && "SAVINGSACCOUNT".equals(entity);
    }

    public static String encode(String json, SavingsDepositOrigin origin) {
        JsonObject payload = clientPayload(json);
        if (origin == null) {
            throw untrustedOrigin();
        }
        JsonObject metadata = new JsonObject();
        metadata.addProperty("version", 1);
        metadata.addProperty("origin", origin.name());
        JsonObject envelope = new JsonObject();
        envelope.add(METADATA, metadata);
        envelope.add("payload", payload);
        return envelope.toString();
    }

    public static JsonObject clientPayload(String json) {
        final JsonObject payload;
        try {
            payload = JsonParser.parseString(json).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new InvalidJsonException();
        }
        if (payload.has(METADATA)) {
            throw new GeneralPlatformDomainRuleException("error.msg.savings.deposit.reserved.metadata",
                    "The _serverCommand property is reserved for server use and must not be supplied in a deposit request.");
        }
        return payload;
    }

    public static Decoded decode(String json) {
        try {
            JsonObject envelope = JsonParser.parseString(json).getAsJsonObject();
            JsonObject metadata = envelope.getAsJsonObject(METADATA);
            JsonElement version = metadata.get("version");
            JsonElement origin = metadata.get("origin");
            JsonObject payload = envelope.getAsJsonObject("payload");
            if (envelope.size() != 2 || metadata.size() != 2 || !version.isJsonPrimitive() || !version.getAsJsonPrimitive().isNumber()
                    || !"1".equals(version.getAsString()) || !origin.isJsonPrimitive() || !origin.getAsJsonPrimitive().isString()
                    || payload == null || payload.has(METADATA)) {
                throw untrustedOrigin();
            }
            return new Decoded(SavingsDepositOrigin.valueOf(origin.getAsString()), payload);
        } catch (RuntimeException exception) {
            throw untrustedOrigin();
        }
    }

    /** Read-only compatibility: legacy flat history remains readable without establishing execution trust. */
    public static String forDisplay(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        try {
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            if (!object.has(METADATA)) {
                return json;
            }
            JsonElement payload = object.get("payload");
            return payload != null && payload.isJsonObject() ? payload.toString() : "{}";
        } catch (RuntimeException exception) {
            return "{}";
        }
    }

    public static GeneralPlatformDomainRuleException untrustedOrigin() {
        return new GeneralPlatformDomainRuleException(UNTRUSTED_ORIGIN,
                "This deposit command has untrusted or unsupported origin metadata. Cancel it and resubmit the deposit.");
    }

    public record Decoded(SavingsDepositOrigin origin, JsonObject payload) {
    }
}
