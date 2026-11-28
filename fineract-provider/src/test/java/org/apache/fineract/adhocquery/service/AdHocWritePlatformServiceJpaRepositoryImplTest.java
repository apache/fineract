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
package org.apache.fineract.adhocquery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.adhocquery.domain.AdHoc;
import org.apache.fineract.adhocquery.domain.AdHocRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdHocWritePlatformServiceJpaRepositoryImplTest {

    private static final Long ADHOC_ID = 7L;
    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private AdHocRepository adHocRepository;
    @Mock
    private AdHocDataValidator adHocDataValidator;
    @Mock
    private SqlValidator sqlValidator;

    @InjectMocks
    private AdHocWritePlatformServiceJpaRepositoryImpl service;

    @Test
    void createTakesItsValuesFromTheCommand() {
        service.createAdHocQuery(command("{\"name\":\"q\",\"query\":\"select 1\",\"tableName\":\"t\",\"tableFields\":\"a\","
                + "\"email\":\"ops@example.com\",\"reportRunFrequency\":2,\"reportRunEvery\":3,\"isActive\":true}"));

        final AdHoc saved = savedAdHoc();
        assertEquals("q", saved.getName());
        assertEquals("select 1", saved.getQuery());
        assertEquals("t", saved.getTableName());
        assertEquals("a", saved.getTableFields());
        assertEquals("ops@example.com", saved.getEmail());
        assertEquals(2L, saved.getReportRunFrequency());
        assertEquals(3L, saved.getReportRunEvery());
        assertTrue(saved.isActive());
        verify(sqlValidator).validate("adhoc", "select 1");
    }

    @Test
    void createStoresMissingAndEmptyValuesAsNullAndDefaultsToInactive() {
        service.createAdHocQuery(
                command("{\"name\":\"q\",\"query\":\"select 1\",\"tableName\":\"t\",\"tableFields\":\"a\",\"email\":\"\"}"));

        final AdHoc saved = savedAdHoc();
        assertNull(saved.getEmail());
        assertNull(saved.getReportRunFrequency());
        assertNull(saved.getReportRunEvery());
        assertFalse(saved.isActive());
    }

    @Test
    void updateReportsTheChangedValuesInOrder() {
        final AdHoc adHoc = existing();

        final CommandProcessingResult result = service.updateAdHocQuery(ADHOC_ID,
                command("{\"isActive\":false,\"reportRunEvery\":5,\"reportRunFrequency\":4,\"email\":\"new@example.com\","
                        + "\"tableFields\":\"b\",\"tableName\":\"u\",\"query\":\"select 2\",\"name\":\"renamed\"}"));

        assertEquals(List.of("name", "query", "tableName", "tableFields", "email", "reportRunFrequency", "reportRunEvery", "isActive"),
                new ArrayList<>(result.getChanges().keySet()));
        assertEquals(Map.of("name", "renamed", "query", "select 2", "tableName", "u", "tableFields", "b", "email", "new@example.com",
                "reportRunFrequency", 4L, "reportRunEvery", 5L, "isActive", false), result.getChanges());
        assertEquals("renamed", adHoc.getName());
        assertEquals("select 2", adHoc.getQuery());
        assertEquals("u", adHoc.getTableName());
        assertEquals("b", adHoc.getTableFields());
        assertEquals("new@example.com", adHoc.getEmail());
        assertEquals(4L, adHoc.getReportRunFrequency());
        assertEquals(5L, adHoc.getReportRunEvery());
        assertFalse(adHoc.isActive());
        assertEquals(ADHOC_ID, result.getResourceId());
        verify(adHocRepository).saveAndFlush(adHoc);
    }

    /**
     * Documents a known inconsistency, not intended behaviour: create stores an empty value as {@code null}, but update
     * stores it as an empty string. This test must change when both are made to behave the same.
     */
    @Test
    void updateStoresAnEmptyValueAsAnEmptyString() {
        final AdHoc adHoc = existing();

        final CommandProcessingResult result = service.updateAdHocQuery(ADHOC_ID, command("{\"email\":\"\"}"));

        assertEquals(Map.of("email", ""), result.getChanges());
        assertEquals("", adHoc.getEmail());
    }

    @Test
    void updateWithoutAChangeSavesNothing() {
        existing();

        final CommandProcessingResult result = service.updateAdHocQuery(ADHOC_ID,
                command("{\"name\":\"q\",\"query\":\"select 1\",\"reportRunFrequency\":2,\"isActive\":true}"));

        assertTrue(result.getChanges() == null || result.getChanges().isEmpty());
        verify(adHocRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateActivatesWithIsActive() {
        final AdHoc adHoc = existing().setActive(false);

        final CommandProcessingResult result = service.updateAdHocQuery(ADHOC_ID, command("{\"isActive\":true}"));

        assertTrue(adHoc.isActive());
        assertEquals(Map.of("isActive", true), result.getChanges());
    }

    @Test
    void updateDeactivatesWithIsActive() {
        final AdHoc adHoc = existing();

        final CommandProcessingResult result = service.updateAdHocQuery(ADHOC_ID, command("{\"isActive\":false}"));

        assertFalse(adHoc.isActive());
        assertEquals(Map.of("isActive", false), result.getChanges());
    }

    @Test
    void updateLeavesActiveUntouchedWhenIsActiveIsAbsent() {
        final AdHoc adHoc = existing();

        final CommandProcessingResult result = service.updateAdHocQuery(ADHOC_ID, command("{\"name\":\"renamed\"}"));

        assertTrue(adHoc.isActive());
        assertEquals(Map.of("name", "renamed"), result.getChanges());
    }

    private AdHoc existing() {
        final AdHoc adHoc = new AdHoc().setName("q").setQuery("select 1").setTableName("t").setTableFields("a").setEmail("ops@example.com")
                .setReportRunFrequency(2L).setReportRunEvery(3L).setActive(true);
        when(adHocRepository.findById(ADHOC_ID)).thenReturn(Optional.of(adHoc));
        return adHoc;
    }

    private AdHoc savedAdHoc() {
        final ArgumentCaptor<AdHoc> captor = ArgumentCaptor.forClass(AdHoc.class);
        verify(adHocRepository).saveAndFlush(captor.capture());
        return captor.getValue();
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }
}
