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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.fineract.TestConfiguration;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Checks that Jackson renders {@link DisbursementData} the way the legacy Gson serializer did.
 */
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
public class DisbursementDataSerializationTest {

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @Test
    public void testDisbursementDataKeepsTheLegacyJsonShape() throws IOException {
        DisbursementData disbursement = new DisbursementData(1L, 2L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 3),
                new BigDecimal("6000"), null, null, null, null);

        Map<String, Object> json = toMap(disbursement);

        assertEquals(List.of(2026, 2, 1), json.get("expectedDisbursementDate"));
        assertEquals(List.of(2026, 2, 3), json.get("actualDisbursementDate"));
        // Gson omitted a null waived charge amount; the getter would turn it into 0
        assertFalse(json.containsKey("waivedChargeAmount"));
        // computed getter, not a field
        assertFalse(json.containsKey("disbursed"));
    }

    @Test
    public void testWaivedChargeAmountIsWrittenWhenSet() throws IOException {
        DisbursementData disbursement = new DisbursementData(1L, 2L, LocalDate.of(2026, 2, 1), null, new BigDecimal("6000"), null, null,
                null, new BigDecimal("25"));

        Map<String, Object> json = toMap(disbursement);

        assertEquals(25, json.get("waivedChargeAmount"));
    }

    @Test
    public void testImportFieldsAreNotWritten() throws IOException {
        DisbursementData disbursement = DisbursementData.importInstance(LocalDate.of(2026, 2, 1), "link", 3, "en", "dd MMMM yyyy");

        Map<String, Object> json = toMap(disbursement);

        // transient fields, never written by Gson
        assertFalse(json.containsKey("rowIndex"));
        assertFalse(json.containsKey("linkAccountId"));
        assertEquals("en", json.get("locale"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(DisbursementData disbursement) throws IOException {
        return objectMapper.readValue(objectMapper.writeValueAsString(disbursement), Map.class);
    }
}
