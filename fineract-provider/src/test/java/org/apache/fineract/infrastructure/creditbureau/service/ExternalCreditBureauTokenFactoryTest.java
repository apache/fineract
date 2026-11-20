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
package org.apache.fineract.infrastructure.creditbureau.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauToken;
import org.junit.jupiter.api.Test;

class ExternalCreditBureauTokenFactoryTest {

    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    @Test
    void buildTokenReadsTheTokenResponseAndParsesTheExpiryDate() {
        final CreditBureauToken token = ExternalCreditBureauIntegrationWritePlatformServiceImpl.buildToken(command("""
                {"userName": "user", "access_token": "abc123", "token_type": "bearer", "expires_in": "3599",
                 ".issued": "Mon, 01 Jan 2024 10:00:00 GMT", ".expires": "Tue, 02 Jan 2024 10:00:00 GMT"}
                """));

        assertEquals("user", token.getUserName());
        assertEquals("abc123", token.getAccessToken());
        assertEquals("bearer", token.getTokenType());
        assertEquals("3599", token.getExpiresIn());
        assertEquals("Mon, 01 Jan 2024 10:00:00 GMT", token.getIssued());
        assertEquals(LocalDate.of(2024, 1, 2), token.getExpires());
    }

    @Test
    void buildTokenFailsWhenTheExpiryDateIsMissing() {
        assertThrows(DateTimeParseException.class, () -> ExternalCreditBureauIntegrationWritePlatformServiceImpl.buildToken(command("""
                {"userName": "user", "access_token": "abc123", "token_type": "bearer", "expires_in": "3599"}
                """)));
    }
}
