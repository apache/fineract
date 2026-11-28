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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.EntityDatatableChecksHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The raw JSON of the group and center endpoints, which the generated client models don't show in full: the datatables
 * of the templates keep their column headers, dates are written as arrays, and {@code fields} prunes the response.
 */
public class FeignGroupCenterTemplateJsonTest extends FeignIntegrationTest {

    private static final long STATUS_CREATE = 100L;

    private FeignDatatableHelper datatableHelper;
    private FeignGroupHelper groupHelper;
    private RawGroupCenterApi rawApi;

    @BeforeAll
    public void setup() {
        datatableHelper = new FeignDatatableHelper(fineractClient());
        groupHelper = new FeignGroupHelper(fineractClient());
        rawApi = fineractClient().create(RawGroupCenterApi.class);
    }

    @Test
    void groupTemplateKeepsTheColumnHeadersOfARequiredDatatable() {
        assertRequiredDatatableHasColumnHeaders("m_group", () -> ok(rawApi::groupTemplate));
    }

    @Test
    void centerTemplateKeepsTheColumnHeadersOfARequiredDatatable() {
        // The center template lists the checks on m_group, the only group-level entity a check can be set up for
        assertRequiredDatatableHasColumnHeaders("m_group", () -> ok(rawApi::centerTemplate));
    }

    @Test
    void groupDatesAreWrittenAsArrays() {
        Long groupId = groupHelper.createActiveGroup().getGroupId();

        Map<String, Object> group = ok(() -> rawApi.group(groupId));

        assertInstanceOf(List.class, group.get("activationDate"), String.valueOf(group.get("activationDate")));
        @SuppressWarnings("unchecked")
        Map<String, Object> timeline = (Map<String, Object>) group.get("timeline");
        assertInstanceOf(List.class, timeline.get("submittedOnDate"), String.valueOf(timeline.get("submittedOnDate")));
    }

    @Test
    void groupFieldsKeepTheRequestedFieldsWithTheirDatesAsArrays() {
        Long groupId = groupHelper.createActiveGroup().getGroupId();

        Map<String, Object> group = ok(() -> rawApi.group(groupId, "name,activationDate"));

        assertTrue(group.containsKey("name"), group.toString());
        assertInstanceOf(List.class, group.get("activationDate"), String.valueOf(group.get("activationDate")));
        assertFalse(group.containsKey("id"), "id is supported but was not requested: " + group);
        assertFalse(group.containsKey("officeId"), "officeId is supported but was not requested: " + group);
        assertFalse(group.containsKey("timeline"), "timeline is supported but was not requested: " + group);
    }

    @Test
    void groupFieldsRejectAnUnsupportedField() {
        Long groupId = groupHelper.createActiveGroup().getGroupId();

        CallFailedRuntimeException exception = fail(() -> rawApi.group(groupId, "bogus"));

        assertEquals(400, exception.getStatus(), exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void pagedGroupFieldsArePrunedInsideThePageItems() {
        groupHelper.createActiveGroup();

        Map<String, Object> page = ok(() -> rawApi.pagedGroups("name"));

        assertTrue(page.containsKey("totalFilteredRecords"), page.toString());
        List<Map<String, Object>> groups = (List<Map<String, Object>>) page.get("pageItems");
        assertFalse(groups.isEmpty());
        groups.forEach(group -> {
            assertTrue(group.containsKey("name"), group.toString());
            assertFalse(group.containsKey("id"), group.toString());
        });
    }

    @SuppressWarnings("unchecked")
    private void assertRequiredDatatableHasColumnHeaders(String appTable, java.util.function.Supplier<Map<String, Object>> template) {
        String datatableName = datatableHelper.createDatatable(new PostDataTablesRequest()//
                .datatableName(Utils.uniqueRandomStringGenerator("dt_required_", 5).toLowerCase(Locale.ROOT))//
                .apptableName(appTable)//
                .multiRow(false)//
                .columns(List.of(new PostColumnHeaderData().name("note").type("String").length(50L).mandatory(true))))
                .getResourceIdentifier();
        Long checkId = EntityDatatableChecksHelper.createEntityDatatableCheck(appTable, datatableName, STATUS_CREATE, null).getResourceId();
        try {
            List<Map<String, Object>> datatables = (List<Map<String, Object>>) template.get().get("datatables");
            Map<String, Object> datatable = datatables.stream().filter(entry -> datatableName.equals(entry.get("registeredTableName")))
                    .findFirst().orElseThrow();

            assertEquals(appTable, datatable.get("applicationTableName"));
            List<Map<String, Object>> columns = (List<Map<String, Object>>) datatable.get("columnHeaderData");
            assertNotNull(columns, "columnHeaderData missing: " + datatable);
            assertFalse(columns.isEmpty());
            assertEquals(1, columns.stream().filter(column -> "note".equals(column.get("columnName"))).count(), columns.toString());
        } finally {
            EntityDatatableChecksHelper.deleteEntityDatatableCheck(checkId);
            datatableHelper.deleteDatatable(datatableName);
        }
    }

    @Headers("Accept: application/json")
    interface RawGroupCenterApi {

        @RequestLine("GET /v1/groups/template")
        Map<String, Object> groupTemplate();

        @RequestLine("GET /v1/centers/template")
        Map<String, Object> centerTemplate();

        @RequestLine("GET /v1/groups/{groupId}")
        Map<String, Object> group(@Param("groupId") Long groupId);

        @RequestLine("GET /v1/groups/{groupId}?fields={fields}")
        Map<String, Object> group(@Param("groupId") Long groupId, @Param("fields") String fields);

        @RequestLine("GET /v1/groups?paged=true&limit=5&fields={fields}")
        Map<String, Object> pagedGroups(@Param("fields") String fields);
    }
}
