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
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SavingsDepositEnvelopeTest {

    @ParameterizedTest
    @ValueSource(strings = { "null", "{}", "[]", "not json", "{\"_serverCommand\":null}",
            "{\"_serverCommand\":{\"version\":\"1\",\"origin\":\"STAFF_API\"},\"payload\":{}}",
            "{\"_serverCommand\":{\"version\":1.5,\"origin\":\"STAFF_API\"},\"payload\":{}}",
            "{\"_serverCommand\":{\"version\":1,\"origin\":null},\"payload\":{}}",
            "{\"_serverCommand\":{\"version\":1,\"origin\":\"SPREADSHEET_IMPORT\"},\"payload\":null}",
            "{\"_serverCommand\":{\"version\":1,\"origin\":\"STAFF_API\"},\"payload\":{\"_serverCommand\":{}}}" })
    void malformedMetadataFailsClosed(String stored) {
        var failure = assertThrows(GeneralPlatformDomainRuleException.class, () -> SavingsDepositCommandEnvelope.decode(stored));
        assertThat(failure.getGlobalisationMessageCode()).isEqualTo(SavingsDepositCommandEnvelope.UNTRUSTED_ORIGIN);
    }

    @Test
    void originMustBeAssignedByServerAndEnvelopeDoesNotDuplicateMaker() {
        assertThrows(GeneralPlatformDomainRuleException.class, () -> SavingsDepositCommandEnvelope.encode("{}", null));
        String stored = SavingsDepositCommandEnvelope.encode("{\"transactionAmount\":100}", SavingsDepositOrigin.STAFF_API);
        assertThat(stored).doesNotContain("maker");
        var decoded = SavingsDepositCommandEnvelope.decode(stored);
        assertThat(decoded.origin()).isEqualTo(SavingsDepositOrigin.STAFF_API);
        assertThat(decoded.payload().get("transactionAmount").getAsInt()).isEqualTo(100);
    }
}
