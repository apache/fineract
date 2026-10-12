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
package org.apache.fineract.accounting.rule.data.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import org.apache.fineract.accounting.rule.serialization.AccountingRuleCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.Test;

class AccountRuleRequestSerializationTest {

    private final AccountingRuleCommandFromApiJsonDeserializer deserializer = new AccountingRuleCommandFromApiJsonDeserializer(
            new FromJsonHelper());

    @Test
    void tagBasedRequestKeepsTagsAndMultipleEntryFlagsThroughReserialization() {
        final AccountRuleRequest request = new AccountRuleRequest("Compound Cash-Bank Rule", 1L, null, null, "test compound rule",
                List.of(22L, 23L), List.of(24L, 205L), true, true);

        final String json = serialize(request);
        final JsonObject object = JsonParser.parseString(json).getAsJsonObject();

        assertThat(object.getAsJsonArray("creditTags")).hasSize(2);
        assertThat(object.getAsJsonArray("debitTags")).hasSize(2);
        assertThat(object.get("allowMultipleCreditEntries").getAsBoolean()).isTrue();
        assertThat(object.get("allowMultipleDebitEntries").getAsBoolean()).isTrue();
        assertThat(object.has("accountToDebit")).isFalse();
        assertThat(object.has("accountToCredit")).isFalse();

        assertThatCode(() -> deserializer.validateForCreate(json)).doesNotThrowAnyException();
        assertThatCode(() -> deserializer.validateForUpdate(json)).doesNotThrowAnyException();
    }

    @Test
    void accountBasedRequestStillValidates() {
        final AccountRuleRequest request = new AccountRuleRequest("Account Rule", 1L, 10L, 11L, null, null, null, null, null);

        final String json = serialize(request);

        assertThat(JsonParser.parseString(json).getAsJsonObject().has("creditTags")).isFalse();
        assertThatCode(() -> deserializer.validateForCreate(json)).doesNotThrowAnyException();
    }

    @Test
    void requestWithoutAccountsOrTagsIsStillRejected() {
        final AccountRuleRequest request = new AccountRuleRequest("Empty Rule", 1L, null, null, null, null, null, null, null);

        final String json = serialize(request);

        assertThatThrownBy(() -> deserializer.validateForCreate(json)).isInstanceOf(PlatformApiDataValidationException.class);
    }

    private static String serialize(final AccountRuleRequest request) {
        return new ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson().serialize(request);
    }
}
