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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.junit.jupiter.api.Test;

public class WorkingCapitalBreachCRUDTest {

    private final WorkingCapitalBreachHelper breachHelper = new WorkingCapitalBreachHelper();

    @Test
    public void testTemplateEndpoint() {
        final String json = breachHelper.retrieveTemplateRaw();
        final JsonObject template = JsonParser.parseString(json).getAsJsonObject();
        assertTrue(template.has("breachFrequencyTypeOptions"));
        assertTrue(template.has("breachAmountCalculationTypeOptions"));
        assertFalse(template.getAsJsonArray("breachFrequencyTypeOptions").isEmpty());
        assertFalse(template.getAsJsonArray("breachAmountCalculationTypeOptions").isEmpty());
    }

    @Test
    public void testCreateRetrieveUpdateDeleteAndListEndpoints() {
        final JsonObject createBody = breachJson("Default WCL Breach", 15, "DAYS", "PERCENTAGE", BigDecimal.valueOf(7.5));
        final Long breachId = breachHelper.create(createBody);
        assertNotNull(breachId);

        final JsonObject created = JsonParser.parseString(breachHelper.retrieveOneRaw(breachId)).getAsJsonObject();
        assertEquals("Default WCL Breach", created.get("name").getAsString());
        assertEquals(15, created.get("breachFrequency").getAsInt());
        assertEquals("DAYS", created.getAsJsonObject("breachFrequencyType").get("id").getAsString());
        assertEquals("PERCENTAGE", created.getAsJsonObject("breachAmountCalculationType").get("id").getAsString());
        assertEquals(0, BigDecimal.valueOf(7.5).compareTo(created.get("breachAmount").getAsBigDecimal()));

        final JsonArray all = JsonParser.parseString(breachHelper.retrieveAllRaw()).getAsJsonArray();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getAsJsonObject().get("id").getAsLong() == breachId) {
                assertEquals("Default WCL Breach", all.get(i).getAsJsonObject().get("name").getAsString());
                found = true;
                break;
            }
        }
        assertTrue(found);

        final JsonObject updateBody = breachJson("Updated WCL Breach", 20, "MONTHS", "FLAT", BigDecimal.valueOf(111));
        final Long updatedId = breachHelper.update(breachId, updateBody);
        assertEquals(breachId, updatedId);

        final JsonObject updated = JsonParser.parseString(breachHelper.retrieveOneRaw(breachId)).getAsJsonObject();
        assertEquals("Updated WCL Breach", updated.get("name").getAsString());
        assertEquals(20, updated.get("breachFrequency").getAsInt());
        assertEquals("MONTHS", updated.getAsJsonObject("breachFrequencyType").get("id").getAsString());
        assertEquals("FLAT", updated.getAsJsonObject("breachAmountCalculationType").get("id").getAsString());
        assertEquals(0, BigDecimal.valueOf(111).compareTo(updated.get("breachAmount").getAsBigDecimal()));

        final Long deletedId = breachHelper.delete(breachId);
        assertEquals(breachId, deletedId);
    }

    private static JsonObject breachJson(final String name, final Integer frequency, final String frequencyType,
            final String amountCalculationType, final BigDecimal amount) {
        final JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("breachFrequency", frequency);
        json.addProperty("breachFrequencyType", frequencyType);
        json.addProperty("breachAmountCalculationType", amountCalculationType);
        json.addProperty("breachAmount", amount);
        return json;
    }
}
