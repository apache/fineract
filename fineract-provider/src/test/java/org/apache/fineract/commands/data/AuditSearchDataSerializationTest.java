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
package org.apache.fineract.commands.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.fineract.TestConfiguration;
import org.apache.fineract.useradministration.data.AppUserData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * The audit and maker-checker search templates return {@link AuditSearchData} directly, so it is written by the Jersey
 * Jackson object mapper rather than by the Gson based serializer the other endpoints use.
 */
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
public class AuditSearchDataSerializationTest {

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @Test
    public void appUsersAreSerializedWithIdAndUsername() throws Exception {
        AuditSearchData searchTemplate = new AuditSearchData(
                List.of(AppUserData.dropdown(1L, "mifos"), AppUserData.dropdown(2L, "checker")), List.of("CREATE"), List.of("CLIENT"),
                List.of(new ProcessingResultLookup(1L, "Processed")));

        JsonNode appUsers = objectMapper.readTree(objectMapper.writeValueAsString(searchTemplate)).get("appUsers");

        assertEquals(2, appUsers.size());
        assertAppUser(appUsers.get(0), 1L, "mifos");
        assertAppUser(appUsers.get(1), 2L, "checker");
    }

    private static void assertAppUser(JsonNode appUser, long expectedId, String expectedUsername) {
        Set<String> fieldNames = new TreeSet<>();
        appUser.fieldNames().forEachRemaining(fieldNames::add);
        assertEquals(Set.of("id", "username"), fieldNames);
        assertEquals(expectedId, appUser.get("id").asLong());
        assertEquals(expectedUsername, appUser.get("username").asText());
    }
}
