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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.TestConfiguration;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Pins how Jackson renders the {@link CommandProcessingResult} of the loan charge write endpoints.
 */
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
public class LoanChargeCommandResultSerializationTest {

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @Test
    @SuppressWarnings("unchecked")
    public void testChangesAreRendered() throws IOException {
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("amount", new BigDecimal("10.000000"));
        CommandProcessingResult result = new CommandProcessingResultBuilder().withEntityId(1L).withLoanId(2L).with(changes).build();

        Map<String, Object> json = toMap(result);

        assertEquals(10.0, ((Map<String, Object>) json.get("changes")).get("amount"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEmptyExternalIdInChanges() throws IOException {
        // waiveLoanCharge and adjustmentForLoanCharge put ExternalId.empty() into changes when no external id is set
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("externalId", ExternalId.empty());
        changes.put("amount", new BigDecimal("10.000000"));
        CommandProcessingResult result = new CommandProcessingResultBuilder().withEntityId(1L).with(changes).build();

        Map<String, Object> json = toMap(result);

        Map<String, Object> renderedChanges = (Map<String, Object>) json.get("changes");
        assertEquals(10.0, renderedChanges.get("amount"));
        // legacy Gson response: the entry was omitted
        assertTrue(renderedChanges.containsKey("externalId"), "Jackson renders the empty external id as null");
        assertEquals(null, renderedChanges.get("externalId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLocalDateInChanges() throws IOException {
        // adjustmentForLoanCharge puts the transaction date into changes as a LocalDate
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("transactionDate", LocalDate.of(2026, 11, 19));
        CommandProcessingResult result = new CommandProcessingResultBuilder().withEntityId(1L).with(changes).build();

        Map<String, Object> json = toMap(result);

        // same as the legacy Gson response: "transactionDate":[2026,11,19]. Values inside a map do not go through
        // JacksonLocalDateBeanSerializerModifier, so the mapper's default LocalDate serializer writes an array.
        assertEquals(List.of(2026, 11, 19), ((Map<String, Object>) json.get("changes")).get("transactionDate"));
    }

    @Test
    public void testEmptyChanges() throws IOException {
        // an update that changes nothing
        CommandProcessingResult result = new CommandProcessingResultBuilder().withEntityId(1L).with(new HashMap<>()).build();

        Map<String, Object> json = toMap(result);

        // legacy Gson response: "changes":{}
        assertFalse(json.containsKey("changes"), "Jackson omits empty changes because getChanges() returns null for them");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(CommandProcessingResult result) throws IOException {
        return objectMapper.readValue(objectMapper.writeValueAsString(result), Map.class);
    }
}
