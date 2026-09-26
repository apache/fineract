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

import com.google.gson.JsonObject;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;

/** Compatibility API for the unchanged deposit version-1 envelope. */
public final class SavingsDepositCommandEnvelope {

    public static final String METADATA = SavingsTransactionCommandEnvelope.METADATA;
    public static final String UNTRUSTED_ORIGIN = "error.msg.savings.deposit.untrusted.origin";

    private SavingsDepositCommandEnvelope() {}

    public static boolean appliesTo(String action, String entity) {
        return SavingsTransactionKind.fromCommand(action, entity) == SavingsTransactionKind.DEPOSIT;
    }

    public static String encode(String json, SavingsDepositOrigin origin) {
        return SavingsTransactionCommandEnvelope.encode(json, SavingsTransactionKind.DEPOSIT,
                origin == null ? null : SavingsTransactionOrigin.valueOf(origin.name()));
    }

    public static JsonObject clientPayload(String json) {
        return SavingsTransactionCommandEnvelope.clientPayload(json, SavingsTransactionKind.DEPOSIT);
    }

    public static Decoded decode(String json) {
        var decoded = SavingsTransactionCommandEnvelope.decode(json, SavingsTransactionKind.DEPOSIT);
        return new Decoded(SavingsDepositOrigin.valueOf(decoded.origin().name()), decoded.payload());
    }

    public static String forDisplay(String json) {
        return SavingsTransactionCommandEnvelope.forDisplay(json);
    }

    public static GeneralPlatformDomainRuleException untrustedOrigin() {
        return SavingsTransactionCommandEnvelope.untrustedOrigin(SavingsTransactionKind.DEPOSIT);
    }

    public record Decoded(SavingsDepositOrigin origin, JsonObject payload) {
    }
}
