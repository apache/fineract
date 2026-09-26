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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.commands.domain.SavingsDepositCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsDepositOrigin;
import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.commands.domain.SavingsTransactionOrigin;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class SavingsWithdrawalEnvelopeTest {

    @ParameterizedTest
    @EnumSource(SavingsDepositOrigin.class)
    void depositVersionOneIsByteCompatible(SavingsDepositOrigin origin) {
        String expected = "{\"_serverCommand\":{\"version\":1,\"origin\":\"" + origin.name()
                + "\"},\"payload\":{\"transactionAmount\":50}}";
        assertThat(SavingsDepositCommandEnvelope.encode("{\"transactionAmount\":50}", origin)).isEqualTo(expected);
        assertThat(SavingsDepositCommandEnvelope.decode(expected).origin()).isEqualTo(origin);
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> SavingsTransactionCommandEnvelope.decode(expected, SavingsTransactionKind.WITHDRAWAL));
    }

    @ParameterizedTest
    @EnumSource(SavingsTransactionKind.class)
    void typedMetadataCannotCrossKinds(SavingsTransactionKind kind) {
        String stored = SavingsTransactionCommandEnvelope.encode("{}", kind, SavingsTransactionOrigin.STAFF_API);
        assertThat(SavingsTransactionCommandEnvelope.decode(stored, kind).payload().toString()).isEqualTo("{}");
        for (var other : SavingsTransactionKind.values()) {
            if (other != kind) {
                assertThrows(GeneralPlatformDomainRuleException.class, () -> SavingsTransactionCommandEnvelope.decode(stored, other));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "null", "[]", "{}", "bad json", "{\"_serverCommand\":null}",
            "{\"_serverCommand\":{\"version\":2,\"kind\":\"WITHDRAWAL\",\"origin\":\"UNKNOWN\"},\"payload\":{}}",
            "{\"_serverCommand\":{\"version\":3,\"kind\":\"WITHDRAWAL\",\"origin\":\"STAFF_API\"},\"payload\":{}}" })
    void malformedHistoryFailsClosedButDisplayRemainsReadable(String stored) {
        var failure = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> SavingsTransactionCommandEnvelope.decode(stored, SavingsTransactionKind.WITHDRAWAL));
        assertThat(failure.getDefaultUserMessage()).contains("Cancel", "resubmit");
        SavingsTransactionCommandEnvelope.forDisplay(stored);
    }

    @Test
    void legacyCompletedHistoryStaysFlat() {
        assertThat(SavingsTransactionCommandEnvelope.forDisplay("{\"transactionAmount\":50}")).isEqualTo("{\"transactionAmount\":50}");
    }
}
