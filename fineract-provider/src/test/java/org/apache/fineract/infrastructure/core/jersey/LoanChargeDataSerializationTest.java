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
import com.google.gson.Gson;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.fineract.TestConfiguration;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Checks that Jackson renders {@link LoanChargeData} the way the legacy Gson serializer did.
 */
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
public class LoanChargeDataSerializationTest {

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @Test
    @SuppressWarnings("unchecked")
    public void testLoanChargeDataKeepsTheLegacyJsonShape() throws IOException {
        LoanChargeData loanCharge = LoanChargeData.builder().id(1L).name("fee").dueDate(LocalDate.of(2026, 3, 15))
                .externalId(ExternalId.empty()).externalLoanId(new ExternalId("loan-ext"))
                .chargeOptions(List.of(ChargeData.builder().id(2L).name("option").build())).build();

        Map<String, Object> json = new Gson().fromJson(objectMapper.writeValueAsString(loanCharge), Map.class);

        assertEquals(List.of(2026.0, 3.0, 15.0), json.get("dueDate"));
        assertFalse(json.containsKey("externalId"));
        assertEquals("loan-ext", json.get("externalLoanId"));
        assertEquals(false, json.get("chargePayable"));
        assertFalse(json.containsKey("installmentFee"));
        Map<String, Object> chargeOption = ((List<Map<String, Object>>) json.get("chargeOptions")).get(0);
        assertEquals("option", chargeOption.get("name"));
        assertFalse(chargeOption.containsKey("overdueInstallmentCharge"));
        assertTrue(json.containsKey("id"));
    }
}
