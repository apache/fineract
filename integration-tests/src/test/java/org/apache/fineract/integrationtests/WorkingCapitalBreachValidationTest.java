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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.junit.jupiter.api.Test;

public class WorkingCapitalBreachValidationTest {

    private final WorkingCapitalBreachHelper breachHelper = new WorkingCapitalBreachHelper();

    @Test
    public void testCreateFailsWhenBreachFrequencyIsMissing() {
        final JsonObject body = validBreachJson();
        body.remove("breachFrequency");

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("breachFrequency"));
    }

    @Test
    public void testCreateFailsWhenNameIsMissing() {
        final JsonObject body = validBreachJson();
        body.remove("name");

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("name"));
    }

    @Test
    public void testCreateFailsWhenNameIsBlank() {
        final JsonObject body = validBreachJson();
        body.addProperty("name", "   ");

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("name"));
    }

    @Test
    public void testCreateFailsWhenNameTooLong() {
        final JsonObject body = validBreachJson();
        body.addProperty("name", "x".repeat(101));

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("name"));
    }

    @Test
    public void testCreateFailsWhenBreachFrequencyTypeIsMissing() {
        final JsonObject body = validBreachJson();
        body.remove("breachFrequencyType");

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("breachFrequencyType"));
    }

    @Test
    public void testCreateFailsWhenBreachAmountCalculationTypeIsMissing() {
        final JsonObject body = validBreachJson();
        body.remove("breachAmountCalculationType");

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("breachAmountCalculationType"));
    }

    @Test
    public void testCreateFailsWhenBreachAmountIsMissing() {
        final JsonObject body = validBreachJson();
        body.remove("breachAmount");

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("breachAmount"));
    }

    @Test
    public void testCreateFailsWhenBreachFrequencyIsZero() {
        final JsonObject body = validBreachJson();
        body.addProperty("breachFrequency", 0);

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("breachFrequency"));
    }

    @Test
    public void testCreateFailsWhenBreachFrequencyTypeIsInvalid() {
        final JsonObject body = validBreachJson();
        body.addProperty("breachFrequencyType", "HOURS");

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("breachFrequencyType"));
    }

    @Test
    public void testCreateFailsWhenBreachAmountCalculationTypeIsInvalid() {
        final JsonObject body = validBreachJson();
        body.addProperty("breachAmountCalculationType", "UNKNOWN");

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("breachAmountCalculationType"));
    }

    @Test
    public void testCreateFailsWhenBreachAmountIsNegative() {
        final JsonObject body = validBreachJson();
        body.addProperty("breachAmount", BigDecimal.valueOf(-1));

        final CallFailedRuntimeException ex = breachHelper.runCreateExpectingFailure(body);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("breachAmount"));
    }

    @Test
    public void testUpdateFailsWhenBreachFrequencyTypeIsInvalid() {
        final Long breachId = breachHelper.create(validBreachJson());
        final JsonObject invalidUpdate = validBreachJson();
        invalidUpdate.addProperty("breachFrequencyType", "INVALID");

        final CallFailedRuntimeException ex = breachHelper.runUpdateExpectingFailure(breachId, invalidUpdate);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("breachFrequencyType"));

        breachHelper.delete(breachId);
    }

    @Test
    public void testRetrieveOneFailsWhenBreachNotFound() {
        final Long nonExistingId = 9_999_999_999L;
        final CallFailedRuntimeException ex = breachHelper.runRetrieveOneExpectingFailure(nonExistingId);
        assertEquals(404, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
    }

    @Test
    public void testUpdateFailsWhenBreachNotFound() {
        final Long nonExistingId = 9_999_999_998L;
        final CallFailedRuntimeException ex = breachHelper.runUpdateExpectingFailure(nonExistingId, validBreachJson());
        assertEquals(404, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
    }

    @Test
    public void testDeleteFailsWhenBreachNotFound() {
        final Long nonExistingId = 9_999_999_997L;
        final CallFailedRuntimeException ex = breachHelper.runDeleteExpectingFailure(nonExistingId);
        assertEquals(404, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
    }

    private static JsonObject validBreachJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("name", "Default WCL Breach");
        json.addProperty("breachFrequency", 30);
        json.addProperty("breachFrequencyType", "DAYS");
        json.addProperty("breachAmountCalculationType", "PERCENTAGE");
        json.addProperty("breachAmount", BigDecimal.TEN);
        return json;
    }
}
