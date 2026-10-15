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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.models.DeleteDataTablesDatatableAppTableIdResponse;
import org.apache.fineract.client.models.DeleteDataTablesResponse;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PagedLocalRequestAdvancedQueryData;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesAppTableIdResponse;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.PutDataTablesAppTableIdDatatableIdResponse;
import org.apache.fineract.client.models.PutDataTablesRequest;
import org.apache.fineract.client.models.PutDataTablesResponse;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignDatatableHelper {

    private static final String GENERIC_RESULT_SET = "genericResultSet";

    private final FineractFeignClient fineractClient;

    public FeignDatatableHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    /** A single-row PERSON datatable with a string, a number, a datetime and a date column. */
    public static PostDataTablesRequest testDatatableRequest(String apptableName) {
        return new PostDataTablesRequest().datatableName(Utils.uniqueRandomStringGenerator(apptableName + "_", 5))
                .apptableName(apptableName).entitySubType("PERSON").multiRow(false)
                .columns(List.of(new PostColumnHeaderData().name("Spouse Name").type("String").mandatory(true).length(25L),
                        new PostColumnHeaderData().name("Number of Dependents").type("Number").mandatory(true),
                        new PostColumnHeaderData().name("Time of Visit").type("DateTime").mandatory(false),
                        new PostColumnHeaderData().name("Date of Approval").type("Date").mandatory(false)));
    }

    /** An entry for {@link #testDatatableRequest(String)}, in the shape an entity creation request embeds it. */
    public static Map<String, Object> testDatatableEntry() {
        Map<String, Object> entry = new HashMap<>();
        entry.put("locale", "en");
        entry.put("Spouse Name", Utils.randomStringGenerator("Spouse_name", 4));
        entry.put("Number of Dependents", 5);
        entry.put("Time of Visit", "01 December 2016 04:03");
        entry.put("dateFormat", Utils.DATE_TIME_FORMAT);
        entry.put("Date of Approval", "02 December 2016 00:00");
        return entry;
    }

    public PostDataTablesResponse createDatatable(PostDataTablesRequest request) {
        return ok(() -> fineractClient.dataTables().createDatatable(request));
    }

    public PutDataTablesResponse updateDatatable(String datatableName, PutDataTablesRequest request) {
        return ok(() -> fineractClient.dataTables().updateDatatable(datatableName, request));
    }

    public GetDataTablesResponse getDatatable(String datatableName) {
        return ok(() -> fineractClient.dataTables().getDatatable(datatableName));
    }

    /** Drops the datatable itself. The server rejects this while the datatable still holds entries. */
    public DeleteDataTablesResponse deleteDatatable(String datatableName) {
        return ok(() -> fineractClient.dataTables().deleteDatatable(datatableName));
    }

    public PostDataTablesAppTableIdResponse createDatatableEntry(String datatableName, Long apptableId, Map<String, Object> entry) {
        return createDatatableEntry(datatableName, apptableId, asJson(entry));
    }

    /**
     * Creates one entry in the given datatable for the given application-table row. The entry columns are datatable
     * specific, so the payload is passed as raw JSON.
     */
    public PostDataTablesAppTableIdResponse createDatatableEntry(String datatableName, Long apptableId, String entryJson) {
        return ok(() -> fineractClient.dataTables().createDatatableEntry(datatableName, apptableId, entryJson));
    }

    public PutDataTablesAppTableIdDatatableIdResponse updateDatatableEntry(String datatableName, Long apptableId, Long datatableId,
            Map<String, Object> entry) {
        return ok(() -> fineractClient.dataTables().updateDatatableEntryOneToMany(datatableName, apptableId, datatableId, asJson(entry)));
    }

    /** Deletes every entry the given datatable holds for the given application-table row. */
    public DeleteDataTablesDatatableAppTableIdResponse deleteDatatableEntries(String datatableName, Long apptableId) {
        return ok(() -> fineractClient.dataTables().deleteDatatableEntries(datatableName, apptableId));
    }

    /**
     * A datatable's columns are defined at runtime, and the endpoint serves two different shapes depending on
     * {@code genericResultSet}, so the server declares its response as {@code String} on purpose. The entries are
     * therefore read as a tree rather than a generated model.
     */
    public JsonNode getDatatableEntries(String datatableName, Long apptableId) {
        String json = ok(
                () -> fineractClient.dataTables().getDatatableEntries(datatableName, apptableId, Map.of(GENERIC_RESULT_SET, Boolean.TRUE)));
        try {
            return ObjectMapperFactory.getShared().readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse the entries of datatable " + datatableName, e);
        }
    }

    /** The advanced query answers a page of rows whose columns the query itself picks, so it is read as a tree. */
    public JsonNode queryDatatable(String datatableName, PagedLocalRequestAdvancedQueryData query) {
        String json = ok(() -> fineractClient.dataTables().advancedQuery(datatableName, query));
        try {
            return ObjectMapperFactory.getShared().readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse the query result of datatable " + datatableName, e);
        }
    }

    private String asJson(Map<String, Object> entry) {
        try {
            return ObjectMapperFactory.getShared().writeValueAsString(entry);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise the datatable entry " + entry, e);
        }
    }
}
