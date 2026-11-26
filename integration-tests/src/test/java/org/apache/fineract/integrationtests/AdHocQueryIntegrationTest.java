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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AdHocData;
import org.apache.fineract.client.models.AdHocRequest;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class AdHocQueryIntegrationTest extends FeignIntegrationTest {

    private final List<Long> createdDefinitionIds = new ArrayList<>();

    @AfterEach
    void deleteCreatedDefinitions() {
        List<Long> idsToDelete = List.copyOf(createdDefinitionIds);
        createdDefinitionIds.clear();
        assertAll("Delete definitions created by this test", idsToDelete.stream().map(id -> (Executable) () -> {
            CommandProcessingResult result = ok(() -> fineractClient().adhocQuery().deleteAdHocQuery(id));
            assertEquals(id, result.getResourceId());
        }));
    }

    @Test
    void retrieveAllIncludesInactiveDefinitionWithNullOptionalFields() {
        AdHocRequest request = definition("sql061-minimal-" + UUID.randomUUID(), false);
        Long definitionId = createDefinition(request);

        List<AdHocData> matchingDefinitions = ok(() -> fineractClient().adhocQuery().retrieveAll1()).stream()
                .filter(adHoc -> definitionId.equals(adHoc.getId())).toList();

        assertEquals(1, matchingDefinitions.size(), "The inactive definition must appear exactly once");
        AdHocData listed = matchingDefinitions.get(0);
        AdHocData template = ok(() -> fineractClient().adhocQuery().template());
        assertDefinition(request, definitionId, listed, template);
        assertNull(listed.getLastRun());

        AdHocData individual = ok(() -> fineractClient().adhocQuery().retrieveAdHocQuery(definitionId));
        assertEquals(individual, listed, "List and individual retrieval must expose the same definition");
    }

    @Test
    void retrieveAllMapsConfiguredDefinitionsInIdOrder() {
        AdHocData template = ok(() -> fineractClient().adhocQuery().template());
        assertNotNull(template.getReportRunFrequencies());
        Long customFrequency = template.getReportRunFrequencies().stream()
                .filter(option -> "reportRunFrequency.custom".equals(option.getCode())).findFirst().orElseThrow().getId();

        String suffix = UUID.randomUUID().toString();
        // Names deliberately sort in the opposite order to the generated identifiers.
        AdHocRequest activeRequest = definition("Z-sql061-" + suffix, true).email("active-sql061@example.org")
                .reportRunFrequency(customFrequency).reportRunEvery(3L);
        AdHocRequest inactiveRequest = definition("A-sql061-" + suffix, false).email("inactive-sql061@example.org")
                .reportRunFrequency(customFrequency).reportRunEvery(5L);
        Long activeId = createDefinition(activeRequest);
        Long inactiveId = createDefinition(inactiveRequest);

        List<AdHocData> definitions = ok(() -> fineractClient().adhocQuery().retrieveAll1());
        List<Long> returnedIds = definitions.stream().map(AdHocData::getId).toList();
        assertEquals(returnedIds.stream().sorted().toList(), returnedIds, "The database result must be ordered by id");

        List<Long> expectedIds = List.of(activeId, inactiveId);
        List<AdHocData> matchingDefinitions = definitions.stream().filter(adHoc -> expectedIds.contains(adHoc.getId())).toList();
        assertEquals(expectedIds, matchingDefinitions.stream().map(AdHocData::getId).toList(),
                "Both active and inactive definitions must appear exactly once in id order");
        assertDefinition(activeRequest, activeId, matchingDefinitions.get(0), template);
        assertDefinition(inactiveRequest, inactiveId, matchingDefinitions.get(1), template);
        assertNull(matchingDefinitions.get(1).getLastRun());
    }

    private static AdHocRequest definition(String name, boolean active) {
        // Even if a scheduler executes the active fixture, this query cannot insert client data.
        return new AdHocRequest().name(name).query("SELECT id FROM m_client WHERE 1 = 0").tableName("m_client").tableFields("id")
                .isActive(active);
    }

    private Long createDefinition(AdHocRequest request) {
        CommandProcessingResult result = ok(() -> fineractClient().adhocQuery().createAdHocQuery(request));
        assertNotNull(result.getResourceId());
        createdDefinitionIds.add(result.getResourceId());
        return result.getResourceId();
    }

    private static void assertDefinition(AdHocRequest expected, Long definitionId, AdHocData actual, AdHocData template) {
        assertEquals(definitionId, actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getQuery(), actual.getQuery());
        assertEquals(expected.getTableName(), actual.getTableName());
        assertEquals(expected.getTableFields(), actual.getTableFields());
        assertEquals(expected.getIsActive(), actual.getIsActive());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getReportRunFrequency(), actual.getReportRunFrequency());
        assertEquals(expected.getReportRunEvery(), actual.getReportRunEvery());
        assertNotNull(actual.getCreatedById());
        assertEquals(ConfigProperties.Backend.USERNAME, actual.getCreatedBy());
        assertNotNull(actual.getCreatedOn());
        assertNotNull(actual.getUpdatedById());
        assertNotNull(actual.getUpdatedOn());
        assertNotNull(template.getReportRunFrequencies());
        assertFalse(template.getReportRunFrequencies().isEmpty());
        assertEquals(template.getReportRunFrequencies(), actual.getReportRunFrequencies());
    }
}
