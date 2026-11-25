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
package org.apache.fineract.infrastructure.entityaccess.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityDataValidator;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelation;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelationRepositoryWrapper;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMapping;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingRepository;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingRepositoryWrapper;
import org.apache.fineract.infrastructure.entityaccess.exception.FineractEntityToEntityMappingDateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FineractEntityAccessWriteServiceImplTest {

    private static final Long MAP_ID = 6L;
    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();
    private static final String DATE_FORMAT = "\"dateFormat\":\"dd MMMM yyyy\"";

    @Mock
    private FineractEntityRelationRepositoryWrapper relationRepositoryWrapper;
    @Mock
    private FineractEntityToEntityMappingRepository mappingRepository;
    @Mock
    private FineractEntityToEntityMappingRepositoryWrapper mappingRepositoryWrapper;
    @Mock
    private FineractEntityDataValidator validator;

    @InjectMocks
    private FineractEntityAccessWriteServiceImpl service;

    @Test
    void updateAppliesTheChangedValuesAndReportsNoChanges() {
        final FineractEntityToEntityMapping mapping = existing();
        final String json = "{\"endDate\":\"30 June 2027\",\"startDate\":\"01 March 2027\",\"toId\":9,\"fromId\":2," + DATE_FORMAT
                + ",\"locale\":\"en\"}";

        final CommandProcessingResult result = service.updateEntityToEntityMapping(MAP_ID, command(json));

        assertEquals(2L, mapping.getFromId());
        assertEquals(9L, mapping.getToId());
        assertEquals(LocalDate.of(2027, 3, 1), mapping.getStartDate());
        assertEquals(LocalDate.of(2027, 6, 30), mapping.getEndDate());
        assertEquals(MAP_ID, result.getResourceId());
        // the changes are detected, but not returned
        assertTrue(result.getChanges() == null || result.getChanges().isEmpty());
        verify(validator).validateForUpdate(json);
        verify(validator).checkForEntity("1", 2L, 9L);
        verify(mappingRepository).saveAndFlush(mapping);
    }

    @Test
    void updateParsesTheDatesWithTheLocaleOfTheRequest() {
        final FineractEntityToEntityMapping mapping = existing();

        service.updateEntityToEntityMapping(MAP_ID,
                command("{\"fromId\":1,\"toId\":5,\"startDate\":\"01 März 2027\",\"endDate\":\"31 Dezember 2027\"," + DATE_FORMAT
                        + ",\"locale\":\"de\"}"));

        assertEquals(LocalDate.of(2027, 3, 1), mapping.getStartDate());
        assertEquals(LocalDate.of(2027, 12, 31), mapping.getEndDate());
        verify(mappingRepository).saveAndFlush(mapping);
    }

    @Test
    void updateWithoutAChangeSavesNothing() {
        final FineractEntityToEntityMapping mapping = existing();

        service.updateEntityToEntityMapping(MAP_ID,
                command("{\"fromId\":1,\"toId\":5,\"startDate\":\"01 January 2027\"," + DATE_FORMAT + ",\"locale\":\"en\"}"));

        assertEquals(LocalDate.of(2027, 1, 1), mapping.getStartDate());
        verify(mappingRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateOfOnlyTheEndDateKeepsTheOtherValues() {
        final FineractEntityToEntityMapping mapping = existing();

        service.updateEntityToEntityMapping(MAP_ID, command("{\"endDate\":\"31 December 2027\"," + DATE_FORMAT + ",\"locale\":\"en\"}"));

        assertEquals(1L, mapping.getFromId());
        assertEquals(5L, mapping.getToId());
        assertEquals(LocalDate.of(2027, 1, 1), mapping.getStartDate());
        assertEquals(LocalDate.of(2027, 12, 31), mapping.getEndDate());
        // the entity check gets the ids from the request, which are missing here
        verify(validator).checkForEntity("1", null, null);
        verify(mappingRepository).saveAndFlush(mapping);
    }

    @Test
    void updateWithAnEndDateBeforeTheStartDateIsRejected() {
        existing();

        assertThrows(FineractEntityToEntityMappingDateException.class, () -> service.updateEntityToEntityMapping(MAP_ID,
                command("{\"endDate\":\"31 December 2026\"," + DATE_FORMAT + ",\"locale\":\"en\"}")));
        verify(mappingRepository, never()).saveAndFlush(any());
    }

    @Test
    void clearingTheEndDateIsAppliedAsAChange() {
        final FineractEntityToEntityMapping mapping = existing().setEndDate(LocalDate.of(2027, 6, 30));

        service.updateEntityToEntityMapping(MAP_ID, command("{\"endDate\":null," + DATE_FORMAT + ",\"locale\":\"en\"}"));

        assertNull(mapping.getEndDate());
        verify(mappingRepository).saveAndFlush(mapping);
    }

    private FineractEntityToEntityMapping existing() {
        final FineractEntityRelation relation = new FineractEntityRelation();
        ReflectionTestUtils.setField(relation, "id", 1L);
        final FineractEntityToEntityMapping mapping = FineractEntityToEntityMapping.newMap(relation, 1L, 5L, LocalDate.of(2027, 1, 1),
                null);
        ReflectionTestUtils.setField(mapping, "id", MAP_ID);
        when(mappingRepositoryWrapper.findOneWithNotFoundDetection(MAP_ID)).thenReturn(mapping);
        return mapping;
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }
}
