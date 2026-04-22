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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.apache.fineract.avro.loan.v1.LoanChargeDataV1;
import org.apache.fineract.avro.loan.v1.OriginatorDetailsV1;
import org.apache.fineract.portfolio.loanorigination.helper.LoanOriginatorDetailsResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanChargeDataV1OriginatorEnricherTest {

    @Mock
    private LoanOriginatorDetailsResolver loanOriginatorDetailsResolver;

    @InjectMocks
    private LoanChargeDataV1OriginatorEnricher enricher;

    private LoanChargeDataV1 loanChargeData;
    private Long loanId;

    @BeforeEach
    void setUp() {
        loanId = 1L;
        loanChargeData = new LoanChargeDataV1();
        loanChargeData.setLoanId(loanId);
    }

    @Test
    void testIsDataTypeSupported() {
        assertTrue(enricher.isDataTypeSupported(LoanChargeDataV1.class));
    }

    @Test
    void testEnrich_WithOriginators() {
        // Given
        final OriginatorDetailsV1 originatorDetails = createOriginatorDetailsV1(1L, "test-originator-1", "Test Originator 1");
        when(loanOriginatorDetailsResolver.resolveOriginatorDetails(loanId)).thenReturn(List.of(originatorDetails));

        // When
        enricher.enrich(loanChargeData);

        // Then
        verify(loanOriginatorDetailsResolver).resolveOriginatorDetails(loanId);
        assertNotNull(loanChargeData.getOriginators());
        assertEquals(1, loanChargeData.getOriginators().size());
        assertEquals("test-originator-1", loanChargeData.getOriginators().getFirst().getExternalId());
    }

    @Test
    void testEnrich_WithMultipleOriginators() {
        // Given
        final OriginatorDetailsV1 details1 = createOriginatorDetailsV1(1L, "test-originator-1", "Test Originator 1");
        final OriginatorDetailsV1 details2 = createOriginatorDetailsV1(2L, "test-originator-2", "Test Originator 2");
        when(loanOriginatorDetailsResolver.resolveOriginatorDetails(loanId)).thenReturn(List.of(details1, details2));

        // When
        enricher.enrich(loanChargeData);

        // Then
        assertNotNull(loanChargeData.getOriginators());
        assertEquals(2, loanChargeData.getOriginators().size());
    }

    @Test
    void testEnrich_NoOriginators() {
        // Given
        when(loanOriginatorDetailsResolver.resolveOriginatorDetails(loanId)).thenReturn(Collections.emptyList());

        // When
        enricher.enrich(loanChargeData);

        // Then
        verify(loanOriginatorDetailsResolver).resolveOriginatorDetails(loanId);
        assertNull(loanChargeData.getOriginators());
    }

    @Test
    void testEnrich_NullLoanId() {
        // Given
        loanChargeData.setLoanId(null);

        // When
        enricher.enrich(loanChargeData);

        // Then
        verify(loanOriginatorDetailsResolver, never()).resolveOriginatorDetails(any());
        assertNull(loanChargeData.getOriginators());
    }

    @Test
    void testEnrich_NullData() {
        // When
        enricher.enrich(null);

        // Then
        verify(loanOriginatorDetailsResolver, never()).resolveOriginatorDetails(any());
    }

    private OriginatorDetailsV1 createOriginatorDetailsV1(final Long id, final String externalId, final String name) {
        return OriginatorDetailsV1.newBuilder().setId(id).setExternalId(externalId).setName(name).setStatus("ACTIVE")
                .setOriginatorType(null).setChannelType(null).build();
    }
}
