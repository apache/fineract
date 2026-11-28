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
package org.apache.fineract.infrastructure.creditbureau.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class CreditBureauLoanProductMappingDataTest {

    @Test
    void testCreditBureauLoanProductMappingJsonProperties() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        CreditBureauLoanProductMappingData data = CreditBureauLoanProductMappingData.instance(1L, 1L, "test-alias", "test-summary",
                "test-loan", 10L, true, false, 30L, true);

        JsonNode json = objectMapper.valueToTree(data);

        // Verify important properties
        assertTrue(json.has("isCreditCheckMandatory"), "Missing isCreditCheckMandatory field");

        assertTrue(json.get("isCreditCheckMandatory").isBoolean(), "isCreditCheckMandatory should be a boolean");

        assertTrue(json.get("isCreditCheckMandatory").asBoolean(), "isCreditCheckMandatory should be true");

        assertTrue(json.has("creditbureauSummary"), "Missing creditbureauSummary field");

        assertEquals("test-summary", json.get("creditbureauSummary").asText());

        // Ensure incorrect duplicate property is absent
        assertFalse(json.has("creditCheckMandatory"), "Unexpected duplicate creditCheckMandatory field");
    }

}
