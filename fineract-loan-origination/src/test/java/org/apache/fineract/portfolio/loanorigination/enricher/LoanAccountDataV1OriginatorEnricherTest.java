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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.avro.loan.v1.OriginatorDetailsV1;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMapping;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMappingRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanAccountDataV1OriginatorEnricherTest {

    @Mock
    private LoanOriginatorMappingRepository loanOriginatorMappingRepository;

    @Mock
    private LoanOriginatorAvroMapper loanOriginatorAvroMapper;

    @InjectMocks
    private LoanAccountDataV1OriginatorEnricher enricher;

    private LoanAccountDataV1 loanAccountData;
    private Long loanId;

    @BeforeEach
    void setUp() {
        loanId = 1L;
        loanAccountData = new LoanAccountDataV1();
        loanAccountData.setId(loanId);
    }

    @Test
    void testIsDataTypeSupported() {
        assertTrue(enricher.isDataTypeSupported(LoanAccountDataV1.class));
    }

    @Test
    void testEnrich_WithOriginators() {
        // Given
        final LoanOriginator originator = createTestOriginator(1L, "test-originator-1", "Test Originator 1");
        final LoanOriginatorMapping mapping = LoanOriginatorMapping.create(loanId, originator);
        final List<LoanOriginatorMapping> mappings = List.of(mapping);

        final OriginatorDetailsV1 originatorDetails = createOriginatorDetailsV1(1L, "test-originator-1", "Test Originator 1");

        when(loanOriginatorMappingRepository.findByLoanIdWithOriginatorDetails(loanId)).thenReturn(mappings);
        when(loanOriginatorAvroMapper.toAvro(originator)).thenReturn(originatorDetails);

        // When
        enricher.enrich(loanAccountData);

        // Then
        verify(loanOriginatorMappingRepository).findByLoanIdWithOriginatorDetails(loanId);
        verify(loanOriginatorAvroMapper).toAvro(originator);
        assertNotNull(loanAccountData.getOriginators());
        assertEquals(1, loanAccountData.getOriginators().size());
        assertEquals("test-originator-1", loanAccountData.getOriginators().getFirst().getExternalId());
    }

    @Test
    void testEnrich_WithMultipleOriginators() {
        // Given
        final LoanOriginator originator1 = createTestOriginator(1L, "test-originator-1", "Test Originator 1");
        final LoanOriginator originator2 = createTestOriginator(2L, "test-originator-2", "Test Originator 2");
        final List<LoanOriginatorMapping> mappings = List.of(LoanOriginatorMapping.create(loanId, originator1),
                LoanOriginatorMapping.create(loanId, originator2));

        final OriginatorDetailsV1 details1 = createOriginatorDetailsV1(1L, "test-originator-1", "Test Originator 1");
        final OriginatorDetailsV1 details2 = createOriginatorDetailsV1(2L, "test-originator-2", "Test Originator 2");

        when(loanOriginatorMappingRepository.findByLoanIdWithOriginatorDetails(loanId)).thenReturn(mappings);
        when(loanOriginatorAvroMapper.toAvro(originator1)).thenReturn(details1);
        when(loanOriginatorAvroMapper.toAvro(originator2)).thenReturn(details2);

        // When
        enricher.enrich(loanAccountData);

        // Then
        assertNotNull(loanAccountData.getOriginators());
        assertEquals(2, loanAccountData.getOriginators().size());
    }

    @Test
    void testEnrich_NoOriginators() {
        // Given
        when(loanOriginatorMappingRepository.findByLoanIdWithOriginatorDetails(loanId)).thenReturn(Collections.emptyList());

        // When
        enricher.enrich(loanAccountData);

        // Then
        verify(loanOriginatorMappingRepository).findByLoanIdWithOriginatorDetails(loanId);
        verify(loanOriginatorAvroMapper, never()).toAvro(any());
        assertNull(loanAccountData.getOriginators());
    }

    @Test
    void testEnrich_NullLoanId() {
        // Given
        loanAccountData.setId(null);

        // When
        enricher.enrich(loanAccountData);

        // Then
        verify(loanOriginatorMappingRepository, never()).findByLoanIdWithOriginatorDetails(any());
        assertNull(loanAccountData.getOriginators());
    }

    @Test
    void testEnrich_NullData() {
        // When
        enricher.enrich(null);

        // Then
        verify(loanOriginatorMappingRepository, never()).findByLoanIdWithOriginatorDetails(any());
    }

    @Test
    void testEnrich_NullMappings() {
        // Given
        when(loanOriginatorMappingRepository.findByLoanIdWithOriginatorDetails(loanId)).thenReturn(null);

        // When
        enricher.enrich(loanAccountData);

        // Then
        verify(loanOriginatorMappingRepository).findByLoanIdWithOriginatorDetails(loanId);
        verify(loanOriginatorAvroMapper, never()).toAvro(any());
        assertNull(loanAccountData.getOriginators());
    }

    @Test
    void testEnrich_NullOriginatorInMapping() {
        // Given
        final LoanOriginatorMapping mapping = mock(LoanOriginatorMapping.class);
        when(mapping.getOriginator()).thenReturn(null);
        final List<LoanOriginatorMapping> mappings = List.of(mapping);

        when(loanOriginatorMappingRepository.findByLoanIdWithOriginatorDetails(loanId)).thenReturn(mappings);

        // When
        enricher.enrich(loanAccountData);

        // Then
        verify(loanOriginatorMappingRepository).findByLoanIdWithOriginatorDetails(loanId);
        verify(loanOriginatorAvroMapper, never()).toAvro(any());
        assertNull(loanAccountData.getOriginators());
    }

    private LoanOriginator createTestOriginator(final Long id, final String externalId, final String name) {
        final LoanOriginator originator = LoanOriginator.create(new ExternalId(externalId), name, LoanOriginatorStatus.ACTIVE, null, null);
        originator.setId(id);
        return originator;
    }

    private OriginatorDetailsV1 createOriginatorDetailsV1(final Long id, final String externalId, final String name) {
        return OriginatorDetailsV1.newBuilder().setId(id).setExternalId(externalId).setName(name).setStatus("ACTIVE")
                .setOriginatorType(null).setChannelType(null).build();
    }
}
