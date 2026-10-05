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
package org.apache.fineract.adhocquery.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.Test;

class AdHocTest {

    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    @Test
    void createHonoursIsActiveTrue() {
        String json = "{\"name\":\"q\",\"query\":\"select 1\",\"tableName\":\"t\",\"tableFields\":\"a\",\"isActive\":true}";
        AdHoc adHoc = AdHoc.fromJson(command(json));

        assertTrue(adHoc.isActive());
    }

    @Test
    void createDefaultsToInactiveWhenIsActiveAbsent() {
        AdHoc adHoc = AdHoc.fromJson(command("{\"name\":\"q\",\"query\":\"select 1\",\"tableName\":\"t\",\"tableFields\":\"a\"}"));

        assertFalse(adHoc.isActive());
    }

    @Test
    void updateActivatesWithIsActive() {
        AdHoc adHoc = new AdHoc().setActive(false);

        Map<String, Object> changes = adHoc.update(command("{\"isActive\":true}"));

        assertTrue(adHoc.isActive());
        assertEquals(Boolean.TRUE, changes.get("isActive"));
    }

    @Test
    void updateDeactivatesWithIsActive() {
        AdHoc adHoc = new AdHoc().setActive(true);

        Map<String, Object> changes = adHoc.update(command("{\"isActive\":false}"));

        assertFalse(adHoc.isActive());
        assertEquals(Boolean.FALSE, changes.get("isActive"));
    }

    @Test
    void updateLeavesActiveUntouchedWhenIsActiveAbsent() {
        AdHoc adHoc = new AdHoc().setActive(true);

        Map<String, Object> changes = adHoc.update(command("{\"name\":\"renamed\"}"));

        assertTrue(adHoc.isActive());
        assertFalse(changes.containsKey("isActive"));
    }
}
