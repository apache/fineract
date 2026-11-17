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
package org.apache.fineract.infrastructure.core.jersey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.core.annotation.LegacyApiResponse;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.LegacyApiResponseSerializer;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.data.RoleData;
import org.junit.jupiter.api.Test;

class UserTemplateResponseSerializationTest {

    private final LegacyApiResponseSerializer serializer = new LegacyApiResponseSerializer(
            SecurityApiSerializationTestSupport.newObjectMapper());
    private final Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

    @LegacyApiResponse
    public void unfiltered() {}

    @Test
    void preservesPrivateUserFieldsAndNestedTemplateData() throws Exception {
        RoleData role = new RoleData(7L, "Officer", "Office role", false);
        AppUserData user = AppUserData.instance(12L, "officer", "officer@example.org", 2L, "Branch", "First", "Last", List.of(role),
                List.of(role), null, false);
        OfficeData office = new OfficeData(2L, "Branch", "Branch", new ExternalId("branch-id"), LocalDate.of(2026, 11, 9), ".1.2.", 1L,
                "Head office", null);
        office.setImportFields(99, "en", "yyyy-MM-dd");
        AppUserData template = AppUserData.template(user, List.of(office));
        assertLegacyJson(template);
        assertLegacyJson(List.of(template));
        assertLegacyJson(AppUserData.template(user,
                List.of(OfficeData.importInstance("Branch", 1L, LocalDate.of(2026, 11, 9), ExternalId.empty()))));
        String json = write(template, "unfiltered", Set.of());
        assertFalse(json.contains("rowIndex"));
        assertTrue(json.contains("passwordNeverExpires"));
        assertTrue(json.contains("description"));
        assertTrue(json.contains("\"openingDate\":[2026,11,9]"));
    }

    @Test
    void preservesUserDropdownsInExistingTypedAuditTemplates() throws Exception {
        // AuditSearchData already returns these dropdowns through the shared mapper.
        String json = SecurityApiSerializationTestSupport.newObjectMapper()
                .writeValueAsString(List.of(AppUserData.dropdown(1L, "mifos"), AppUserData.dropdown(2L, "checker")));
        assertEquals(JsonParser.parseString("[{\"id\":1,\"username\":\"mifos\"},{\"id\":2,\"username\":\"checker\"}]"),
                JsonParser.parseString(json));
    }

    private String write(Object data, String method, Set<String> fields) throws Exception {
        return serializer.writer(getClass().getMethod(method).getAnnotation(LegacyApiResponse.class), fields).writeValueAsString(data);
    }

    private void assertLegacyJson(Object data) throws Exception {
        assertEquals(JsonParser.parseString(gson.toJson(data)), JsonParser.parseString(write(data, "unfiltered", Set.of())));
    }
}
