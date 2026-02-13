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
package org.apache.fineract.portfolio.account.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class to verify JSON serialization of StandingInstructionData, particularly focusing on the status field
 * visibility in API responses.
 */
public class StandingInstructionDataSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register JavaTimeModule for LocalDate support
    }

    /**
     * This test verifies that the status field is properly serialized to JSON.
     *
     * BACKGROUND: The backend soft-delete implementation marks standing instructions as deleted (status=3). The
     * database query correctly selects the status field, and the StandingInstructionMapper properly populates it.
     * However, without the @Getter annotation on the private status field, Jackson cannot serialize it to JSON, making
     * it impossible for frontend applications to: - Filter out deleted standing instructions (status=3) - Distinguish
     * between ACTIVE (1), DISABLED (2), and DELETED (3) states
     *
     * EXPECTED BEHAVIOR: The status field should be included in JSON responses from GET /standinginstructions
     */
    @Test
    public void testStatusFieldSerialization() throws Exception {
        // Create a status enum representing DELETED state (status=3)
        EnumOptionData deletedStatus = new EnumOptionData(3L, "DELETED", "Deleted");

        // Create a StandingInstructionData instance with required fields
        StandingInstructionData standingInstruction = StandingInstructionData.instance(1L, // id
                100L, // accountDetailId
                "Test Standing Instruction", // name
                null, // fromOffice
                null, // toOffice
                null, // fromClient
                null, // toClient
                null, // fromAccountType
                null, // fromAccount
                null, // toAccountType
                null, // toAccount
                null, // transferType
                null, // priority
                null, // instructionType
                deletedStatus, // status (THIS IS THE CRITICAL FIELD)
                BigDecimal.valueOf(1000), // amount
                LocalDate.of(2026, 1, 1), // validFrom
                LocalDate.of(2026, 12, 31), // validTill
                null, // recurrenceType
                null, // recurrenceFrequency
                null, // recurrenceInterval
                null // recurrenceOnMonthDay
        );

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(standingInstruction);

        // Parse the JSON to verify field presence
        JsonNode jsonNode = objectMapper.readTree(json);

        // Verify basic fields are present
        assertNotNull(jsonNode.get("id"), "id field should be present in JSON");
        assertEquals(1L, jsonNode.get("id").asLong());

        assertNotNull(jsonNode.get("name"), "name field should be present in JSON");
        assertEquals("Test Standing Instruction", jsonNode.get("name").asText());

        assertNotNull(jsonNode.get("amount"), "amount field should be present in JSON");
        assertEquals(1000, jsonNode.get("amount").asInt());

        assertNotNull(jsonNode.get("status"),
                "status field MUST be present in JSON response for frontend to filter deleted standing instructions");

        // Verify the status field contains the correct data
        JsonNode statusNode = jsonNode.get("status");
        assertNotNull(statusNode.get("id"), "status.id should be present");
        assertEquals(3L, statusNode.get("id").asLong(), "status.id should be 3 for DELETED state");

        assertNotNull(statusNode.get("code"), "status.code should be present");
        assertEquals("DELETED", statusNode.get("code").asText());

        assertNotNull(statusNode.get("value"), "status.value should be present");
        assertEquals("Deleted", statusNode.get("value").asText());
    }

    /**
     * Verify that the status field is properly serialized for different status values (ACTIVE, DISABLED, DELETED).
     */
    @Test
    public void testStatusFieldSerializationForDifferentStates() throws Exception {
        // Test ACTIVE state (status=1)
        EnumOptionData activeStatus = new EnumOptionData(1L, "ACTIVE", "Active");
        StandingInstructionData activeInstruction = createStandingInstructionWithStatus(activeStatus);

        String activeJson = objectMapper.writeValueAsString(activeInstruction);
        JsonNode activeNode = objectMapper.readTree(activeJson);

        assertNotNull(activeNode.get("status"), "status field should be present for ACTIVE state");
        assertEquals(1L, activeNode.get("status").get("id").asLong());
        assertEquals("ACTIVE", activeNode.get("status").get("code").asText());

        // Test DISABLED state (status=2)
        EnumOptionData disabledStatus = new EnumOptionData(2L, "DISABLED", "Disabled");
        StandingInstructionData disabledInstruction = createStandingInstructionWithStatus(disabledStatus);

        String disabledJson = objectMapper.writeValueAsString(disabledInstruction);
        JsonNode disabledNode = objectMapper.readTree(disabledJson);

        assertNotNull(disabledNode.get("status"), "status field should be present for DISABLED state");
        assertEquals(2L, disabledNode.get("status").get("id").asLong());
        assertEquals("DISABLED", disabledNode.get("status").get("code").asText());

        // Test DELETED state (status=3)
        EnumOptionData deletedStatus = new EnumOptionData(3L, "DELETED", "Deleted");
        StandingInstructionData deletedInstruction = createStandingInstructionWithStatus(deletedStatus);

        String deletedJson = objectMapper.writeValueAsString(deletedInstruction);
        JsonNode deletedNode = objectMapper.readTree(deletedJson);

        assertNotNull(deletedNode.get("status"), "status field should be present for DELETED state");
        assertEquals(3L, deletedNode.get("status").get("id").asLong());
        assertEquals("DELETED", deletedNode.get("status").get("code").asText());
    }

    /**
     * Helper method to create a StandingInstructionData with a specific status.
     */
    private StandingInstructionData createStandingInstructionWithStatus(EnumOptionData status) {
        return StandingInstructionData.instance(1L, // id
                100L, // accountDetailId
                "Test Standing Instruction", // name
                null, // fromOffice
                null, // toOffice
                null, // fromClient
                null, // toClient
                null, // fromAccountType
                null, // fromAccount
                null, // toAccountType
                null, // toAccount
                null, // transferType
                null, // priority
                null, // instructionType
                status, // status
                BigDecimal.valueOf(1000), // amount
                LocalDate.of(2026, 1, 1), // validFrom
                LocalDate.of(2026, 12, 31), // validTill
                null, // recurrenceType
                null, // recurrenceFrequency
                null, // recurrenceInterval
                null // recurrenceOnMonthDay
        );
    }
}
