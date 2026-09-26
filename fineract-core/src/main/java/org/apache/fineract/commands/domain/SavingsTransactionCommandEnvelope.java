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

/** Shared codec. Deposit version 1 is unchanged; other kinds require version 2 and an explicit kind. */
public final class SavingsTransactionCommandEnvelope {

    public static final String METADATA = "_serverCommand";

    private SavingsTransactionCommandEnvelope() {}

    public static boolean appliesTo(String action, String entity) {
        return SavingsTransactionKind.fromCommand(action, entity) != null;
    }

    public static String encode(String json, SavingsTransactionKind kind, SavingsTransactionOrigin origin) {
        JsonObject payload = clientPayload(json, kind);
        if (!kind.accepts(origin)) {
            throw untrustedOrigin(kind);
        }
        JsonObject metadata = new JsonObject();
        metadata.addProperty("version", kind == SavingsTransactionKind.DEPOSIT ? 1 : 2);
        metadata.addProperty("origin", origin.name());
        if (kind != SavingsTransactionKind.DEPOSIT) {
            metadata.addProperty("kind", kind.name());
        }
        JsonObject envelope = new JsonObject();
        envelope.add(METADATA, metadata);
        envelope.add("payload", payload);
        return envelope.toString();
    }

    public static JsonObject clientPayload(String json, SavingsTransactionKind kind) {
        final JsonObject payload;
        try {
            payload = JsonParser.parseString(json).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new InvalidJsonException();
        }
        if (payload.has(METADATA)) {
            throw new GeneralPlatformDomainRuleException(errorPrefix(kind) + ".reserved.metadata", kind == SavingsTransactionKind.DEPOSIT
                    ? "The _serverCommand property is reserved for server use and must not be supplied in a deposit request."
                    : "The _serverCommand property is reserved for server use and must not be supplied in a savings transaction request.");
        }
        return payload;
    }

    public static Decoded decode(String json, SavingsTransactionKind kind) {
        try {
            JsonObject envelope = JsonParser.parseString(json).getAsJsonObject();
            JsonObject metadata = envelope.getAsJsonObject(METADATA);
            JsonElement version = metadata.get("version");
            JsonElement origin = metadata.get("origin");
            JsonObject payload = envelope.getAsJsonObject("payload");
            if (envelope.size() != 2 || metadata.size() != (kind == SavingsTransactionKind.DEPOSIT ? 2 : 3) || !version.isJsonPrimitive()
                    || !version.getAsJsonPrimitive().isNumber()
                    || !(kind == SavingsTransactionKind.DEPOSIT ? "1" : "2").equals(version.getAsString()) || !origin.isJsonPrimitive()
                    || !origin.getAsJsonPrimitive().isString() || payload == null || payload.has(METADATA)) {
                throw untrustedOrigin(kind);
            }
            if (kind != SavingsTransactionKind.DEPOSIT && (!metadata.get("kind").isJsonPrimitive()
                    || !metadata.get("kind").getAsJsonPrimitive().isString() || !kind.name().equals(metadata.get("kind").getAsString()))) {
                throw untrustedOrigin(kind);
            }
            SavingsTransactionOrigin trustedOrigin = SavingsTransactionOrigin.valueOf(origin.getAsString());
            if (!kind.accepts(trustedOrigin)) {
                throw untrustedOrigin(kind);
            }
            return new Decoded(trustedOrigin, payload);
        } catch (RuntimeException exception) {
            throw untrustedOrigin(kind);
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

    public static GeneralPlatformDomainRuleException untrustedOrigin(SavingsTransactionKind kind) {
        return new GeneralPlatformDomainRuleException(errorPrefix(kind) + ".untrusted.origin", kind == SavingsTransactionKind.DEPOSIT
                ? "This deposit command has untrusted or unsupported origin metadata. Cancel it and resubmit the deposit."
                : "This savings withdrawal command has untrusted or unsupported origin metadata. Cancel it and resubmit the operation.");
    }

    private static String errorPrefix(SavingsTransactionKind kind) {
        return kind == SavingsTransactionKind.DEPOSIT ? "error.msg.savings.deposit" : "error.msg.savings.withdrawal";
    }

    public record Decoded(SavingsTransactionOrigin origin, JsonObject payload) {
    }
}
