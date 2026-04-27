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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloannearbreach.WorkingCapitalNearBreachHelper;
import org.junit.jupiter.api.Test;

public class WorkingCapitalNearBreachCRUDTest {

    private final WorkingCapitalNearBreachHelper nearBreachHelper = new WorkingCapitalNearBreachHelper();

    @Test
    public void testCreateRetrieveUpdateDeleteAndListEndpoints() {
        String nearBreachName = Utils.randomStringGenerator("NearBreach", 20);
        final JsonObject createBody = nearBreachHelper.nearBreachJson(nearBreachName, 15, "DAYS", BigDecimal.valueOf(7.5));
        final Long nearBreachId = nearBreachHelper.create(createBody);
        assertNotNull(nearBreachId);

        final JsonObject created = JsonParser.parseString(nearBreachHelper.retrieveOneRaw(nearBreachId)).getAsJsonObject();
        assertEquals(nearBreachName, created.get("name").getAsString());
        assertEquals(15, created.get("frequency").getAsInt());
        assertEquals("DAYS", created.getAsJsonObject("frequencyType").get("id").getAsString());
        assertEquals(0, BigDecimal.valueOf(7.5).compareTo(created.get("threshold").getAsBigDecimal()));

        final JsonArray all = JsonParser.parseString(nearBreachHelper.retrieveAllRaw()).getAsJsonArray();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getAsJsonObject().get("id").getAsLong() == nearBreachId) {
                found = true;
                break;
            }
        }
        assertTrue(found);

        nearBreachName = Utils.randomStringGenerator("NearBreach", 20);
        final JsonObject updateBody = nearBreachHelper.nearBreachJson(nearBreachName, 20, "MONTHS", BigDecimal.valueOf(80));
        final Long updatedId = nearBreachHelper.update(nearBreachId, updateBody);
        assertEquals(nearBreachId, updatedId);

        final JsonObject updated = JsonParser.parseString(nearBreachHelper.retrieveOneRaw(nearBreachId)).getAsJsonObject();
        assertEquals(nearBreachName, updated.get("name").getAsString());
        assertEquals(20, updated.get("frequency").getAsInt());
        assertEquals("MONTHS", updated.getAsJsonObject("frequencyType").get("id").getAsString());
        assertEquals(0, BigDecimal.valueOf(80).compareTo(updated.get("threshold").getAsBigDecimal()));

        final Long deletedId = nearBreachHelper.delete(nearBreachId);
        assertEquals(nearBreachId, deletedId);
    }

    @Test
    public void testNegativeCreateNearBreachWithSameName() {
        // Given
        final String nearBreachName = Utils.randomStringGenerator("NearBreach", 20);
        final JsonObject createBody = nearBreachHelper.nearBreachJson(nearBreachName, 15, "DAYS", BigDecimal.valueOf(7.5));
        final Long nearBreachId = nearBreachHelper.create(createBody);
        assertNotNull(nearBreachId);
        final JsonObject createBodyDuplicated = nearBreachHelper.nearBreachJson(nearBreachName, 20, "DAYS", BigDecimal.valueOf(9.5));

        // When
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> nearBreachHelper.create(createBodyDuplicated));

        // Then
        assertThat(exception.getStatus()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains("Data integrity issue with resource");
    }
}
