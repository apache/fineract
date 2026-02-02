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
package org.apache.fineract.portfolio.loanorigination.enricher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.fineract.avro.loan.v1.OriginatorDetailsV1;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanOriginatorAvroMapperTest {

    private LoanOriginatorAvroMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LoanOriginatorAvroMapper();
    }

    @Test
    void testToAvro_WithAllFields() {
        // Given
        final CodeValue originatorType = createCodeValue(1L, "MERCHANT", "Merchant");
        final CodeValue channelType = createCodeValue(2L, "ONLINE", "Online");
        final LoanOriginator originator = LoanOriginator.create(new ExternalId("test-external-id"), "Test Originator",
                LoanOriginatorStatus.ACTIVE, originatorType, channelType);
        originator.setId(100L);

        // When
        final OriginatorDetailsV1 result = mapper.toAvro(originator);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("test-external-id", result.getExternalId());
        assertEquals("Test Originator", result.getName());
        assertEquals("ACTIVE", result.getStatus());
        assertNotNull(result.getOriginatorType());
        assertEquals(1L, result.getOriginatorType().getId());
        assertEquals("MERCHANT", result.getOriginatorType().getName());
        assertNotNull(result.getChannelType());
        assertEquals(2L, result.getChannelType().getId());
        assertEquals("ONLINE", result.getChannelType().getName());
    }

    @Test
    void testToAvro_WithNullFields() {
        // Given
        final LoanOriginator originator = LoanOriginator.create(new ExternalId("test-external-id"), null, LoanOriginatorStatus.PENDING,
                null, null);
        originator.setId(200L);

        // When
        final OriginatorDetailsV1 result = mapper.toAvro(originator);

        // Then
        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertEquals("test-external-id", result.getExternalId());
        assertNull(result.getName());
        assertEquals("PENDING", result.getStatus());
        assertNull(result.getOriginatorType());
        assertNull(result.getChannelType());
    }

    @Test
    void testToAvro_NullOriginator() {
        // When
        final OriginatorDetailsV1 result = mapper.toAvro(null);

        // Then
        assertNull(result);
    }

    @Test
    void testToAvro_NullExternalId() {
        // Given
        final LoanOriginator originator = LoanOriginator.create(null, "Test", LoanOriginatorStatus.ACTIVE, null, null);
        originator.setId(300L);

        // When
        final OriginatorDetailsV1 result = mapper.toAvro(originator);

        // Then
        assertNotNull(result);
        assertEquals(300L, result.getId());
        assertNull(result.getExternalId());
    }

    private CodeValue createCodeValue(final Long id, final String label, final String description) {
        final CodeValue codeValue = new CodeValue();
        codeValue.setId(id);
        codeValue.setLabel(label);
        codeValue.setDescription(description);
        codeValue.setPosition(1);
        codeValue.setActive(true);
        codeValue.setMandatory(false);
        return codeValue;
    }
}
