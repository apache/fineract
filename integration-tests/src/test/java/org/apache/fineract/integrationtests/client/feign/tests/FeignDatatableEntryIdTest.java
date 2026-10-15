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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The datatable entry routes that take a second id act only on an entry of the application table row named first: a
 * one-to-one datatable refuses the entry id, and a one-to-many entry of another row is not found.
 */
public class FeignDatatableEntryIdTest extends FeignIntegrationTest {

    private static final String CLIENT_APP_TABLE_NAME = "m_client";
    private static final String ONE_TO_ONE_REFUSED = "error.msg.datatable.entry.id.not.allowed.for.one.to.one";
    private static final String NOT_FOUND = "error.msg.datatable.data.not.found";

    private static FeignClientHelper clientHelper;
    private static FeignDatatableHelper datatableHelper;

    @BeforeAll
    public static void setup() {
        FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        clientHelper = new FeignClientHelper(fineractClient);
        datatableHelper = new FeignDatatableHelper(fineractClient);
    }

    @Test
    void oneToOneUpdateWithEntryIdIsRejected() {
        String datatableName = createClientDatatable(false);
        Long x = createClient();
        Long b = createClient();
        try {
            datatableHelper.createDatatableEntry(datatableName, x, Map.of("note", "x"));
            datatableHelper.createDatatableEntry(datatableName, b, Map.of("note", "b"));

            assertRefused(datatableHelper.updateDatatableEntryExpectingError(datatableName, x, b, Map.of("note", "changed")), 403,
                    ONE_TO_ONE_REFUSED);
            assertRefused(datatableHelper.updateDatatableEntryExpectingError(datatableName, x, x, Map.of("note", "changed")), 403,
                    ONE_TO_ONE_REFUSED);

            assertEquals(List.of("x"), notes(datatableName, x));
            assertEquals(List.of("b"), notes(datatableName, b));
        } finally {
            dropDatatable(datatableName, x, b);
        }
    }

    @Test
    void oneToOneDeleteWithEntryIdIsRejected() {
        String datatableName = createClientDatatable(false);
        Long x = createClient();
        Long b = createClient();
        try {
            datatableHelper.createDatatableEntry(datatableName, x, Map.of("note", "x"));
            datatableHelper.createDatatableEntry(datatableName, b, Map.of("note", "b"));

            CallFailedRuntimeException failure = datatableHelper.deleteDatatableEntryExpectingError(datatableName, x, b);
            assertRefused(failure, 403, ONE_TO_ONE_REFUSED);
            assertFalse(failure.getResponseBody().contains("does not exist"), failure.getResponseBody());

            assertEquals(List.of("x"), notes(datatableName, x));
            assertEquals(List.of("b"), notes(datatableName, b));
        } finally {
            dropDatatable(datatableName, x, b);
        }
    }

    @Test
    void oneToManyDeleteOfAnotherEntitysEntryIsNotFound() {
        String datatableName = createClientDatatable(true);
        Long x = createClient();
        Long b = createClient();
        try {
            datatableHelper.createDatatableEntry(datatableName, x, Map.of("note", "x"));
            Long entryOfB = datatableHelper.createDatatableEntry(datatableName, b, Map.of("note", "b")).getResourceId();

            assertRefused(datatableHelper.deleteDatatableEntryExpectingError(datatableName, x, entryOfB), 404, NOT_FOUND);

            assertEquals(List.of("x"), notes(datatableName, x));
            assertEquals(List.of("b"), notes(datatableName, b));
        } finally {
            dropDatatable(datatableName, x, b);
        }
    }

    @Test
    void oneToManyDeleteOfUnknownEntryIsNotFound() {
        String datatableName = createClientDatatable(true);
        Long x = createClient();
        try {
            Long entry = datatableHelper.createDatatableEntry(datatableName, x, Map.of("note", "x")).getResourceId();

            assertRefused(datatableHelper.deleteDatatableEntryExpectingError(datatableName, x, entry + 1000), 404, NOT_FOUND);

            assertEquals(List.of("x"), notes(datatableName, x));
        } finally {
            dropDatatable(datatableName, x);
        }
    }

    @Test
    void oneToManyDeleteOfOwnEntryStillWorks() {
        String datatableName = createClientDatatable(true);
        Long x = createClient();
        try {
            Long first = datatableHelper.createDatatableEntry(datatableName, x, Map.of("note", "first")).getResourceId();
            datatableHelper.createDatatableEntry(datatableName, x, Map.of("note", "second"));

            assertEquals(first, datatableHelper.deleteDatatableEntry(datatableName, x, first).getResourceId());

            assertEquals(List.of("second"), notes(datatableName, x));
        } finally {
            dropDatatable(datatableName, x);
        }
    }

    private static void assertRefused(CallFailedRuntimeException failure, int status, String code) {
        assertEquals(status, failure.getStatus(), failure.getResponseBody());
        assertTrue(failure.getResponseBody().contains(code), failure.getResponseBody());
    }

    private Long createClient() {
        return clientHelper.createClientPending().getClientId();
    }

    private String createClientDatatable(boolean multiRow) {
        PostDataTablesRequest request = new PostDataTablesRequest()//
                .datatableName(Utils.uniqueRandomStringGenerator("dt_client_entry_id_", 5).toLowerCase())//
                .apptableName(CLIENT_APP_TABLE_NAME)//
                .entitySubType("PERSON")//
                .multiRow(multiRow)//
                .columns(List.of(new PostColumnHeaderData().name("note").type("String").length(20L).mandatory(false)));
        return datatableHelper.createDatatable(request).getResourceIdentifier();
    }

    private List<String> notes(String datatableName, Long clientId) {
        JsonNode entries = datatableHelper.getDatatableEntries(datatableName, clientId);
        int column = -1;
        for (int i = 0; i < entries.path("columnHeaders").size(); i++) {
            if ("note".equals(entries.path("columnHeaders").get(i).path("columnName").asText())) {
                column = i;
            }
        }
        List<String> notes = new ArrayList<>();
        for (JsonNode row : entries.path("data")) {
            notes.add(row.path("row").get(column).asText());
        }
        Collections.sort(notes);
        return notes;
    }

    private void dropDatatable(String datatableName, Long... clientIds) {
        for (Long clientId : clientIds) {
            datatableHelper.deleteDatatableEntries(datatableName, clientId);
        }
        datatableHelper.deleteDatatable(datatableName);
    }
}
