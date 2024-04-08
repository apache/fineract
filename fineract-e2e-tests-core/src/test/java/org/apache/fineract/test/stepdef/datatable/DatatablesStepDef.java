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
package org.apache.fineract.test.stepdef.datatable;

import static java.util.function.Function.identity;
import static org.apache.fineract.test.helper.ErrorHelper.checkSuccessfulApiCall;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.services.DataTablesApi;
import org.apache.fineract.test.data.datatable.DatatableColumnType;
import org.apache.fineract.test.data.datatable.DatatableEntityType;
import org.apache.fineract.test.data.datatable.DatatableNameGenerator;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class DatatablesStepDef extends AbstractStepDef {

    public static final String CREATE_DATATABLE_RESULT_KEY = "CreateDatatableResult";
    public static final String DATATABLE_NAME = "DatatableId";
    public static final String DATATABLE_QUERY_RESPONSE = "DatatableQueryResponse";

    @Autowired
    private DataTablesApi dataTablesApi;

    @Autowired
    private DatatableNameGenerator datatableNameGenerator;

    @When("A datatable for {string} is created")
    public void whenDatatableCreated(String entityTypeStr) throws IOException {
        DatatableEntityType entityType = DatatableEntityType.fromString(entityTypeStr);
        List<PostColumnHeaderData> columns = createRandomDatatableColumnsRequest();
        PostDataTablesRequest request = createDatatableRequest(entityType, columns);

        Response<PostDataTablesResponse> response = dataTablesApi.createDatatable(request).execute();
        checkSuccessfulApiCall(response);

        PostDataTablesResponse responseBody = response.body();
        testContext().set(CREATE_DATATABLE_RESULT_KEY, responseBody);
        testContext().set(DATATABLE_NAME, responseBody.getResourceIdentifier());
    }

    @When("A datatable for {string} is created with the following extra columns:")
    public void whenDatatableCreatedWithFollowingExtraColumns(String entityTypeStr, DataTable dataTable) throws IOException {
        DatatableEntityType entityType = DatatableEntityType.fromString(entityTypeStr);
        List<List<String>> rows = dataTable.asLists();
        List<List<String>> rowsWithoutHeader = rows.subList(1, rows.size());
        List<PostColumnHeaderData> columns = createDatatableColumnsRequest(rowsWithoutHeader);
        PostDataTablesRequest request = createDatatableRequest(entityType, columns);

        Response<PostDataTablesResponse> response = dataTablesApi.createDatatable(request).execute();
        checkSuccessfulApiCall(response);

        PostDataTablesResponse responseBody = response.body();
        testContext().set(CREATE_DATATABLE_RESULT_KEY, responseBody);
        testContext().set(DATATABLE_NAME, responseBody.getResourceIdentifier());
    }

    private List<PostColumnHeaderData> createDatatableColumnsRequest(List<List<String>> rowsWithoutHeader) {
        return rowsWithoutHeader.stream().map(row -> {
            String columnName = row.get(0);
            DatatableColumnType columnType = DatatableColumnType.fromTypeString(row.get(1));
            long columnLength = Long.parseLong(row.get(2));
            boolean unique = BooleanUtils.toBoolean(row.get(3));
            boolean indexed = BooleanUtils.toBoolean(row.get(4));

            PostColumnHeaderData postColumnHeaderData = new PostColumnHeaderData();
            postColumnHeaderData.setName(columnName);
            postColumnHeaderData.setType(columnType.getTypeString());
            postColumnHeaderData.setLength(columnLength);
            postColumnHeaderData.setUnique(unique);
            postColumnHeaderData.setIndexed(indexed);
            return postColumnHeaderData;
        }).collect(Collectors.toList());
    }

    @When("A multirow datatable for {string} is created")
    public void whenMultirowDatatableCreated(String entityTypeStr) throws IOException {
        DatatableEntityType entityType = DatatableEntityType.fromString(entityTypeStr);
        List<PostColumnHeaderData> columns = createRandomDatatableColumnsRequest();
        PostDataTablesRequest request = createDatatableRequest(entityType, columns, true);

        Response<PostDataTablesResponse> response = dataTablesApi.createDatatable(request).execute();
        checkSuccessfulApiCall(response);

        PostDataTablesResponse responseBody = response.body();
        testContext().set(CREATE_DATATABLE_RESULT_KEY, responseBody);
        testContext().set(DATATABLE_NAME, responseBody.getResourceIdentifier());
    }

    private List<PostColumnHeaderData> createRandomDatatableColumnsRequest() {
        PostColumnHeaderData columnDef = new PostColumnHeaderData();
        columnDef.setName("col");
        columnDef.setType(DatatableColumnType.NUMBER.getTypeString());
        columnDef.setMandatory(false);
        columnDef.setLength(10L);
        columnDef.setCode("");
        columnDef.setUnique(false);
        columnDef.setIndexed(false);
        return List.of(columnDef);
    }

    private PostDataTablesRequest createDatatableRequest(DatatableEntityType entityType, List<PostColumnHeaderData> columns) {
        return createDatatableRequest(entityType, columns, false);
    }

    private PostDataTablesRequest createDatatableRequest(DatatableEntityType entityType, List<PostColumnHeaderData> columns,
            boolean multiRow) {
        PostDataTablesRequest request = new PostDataTablesRequest();
        String datatableName = datatableNameGenerator.generate(entityType);
        request.setDatatableName(datatableName);
        request.setApptableName(entityType.getReferencedTableName());
        request.setMultiRow(multiRow);
        request.setColumns(columns);
        return request;
    }

    @Then("The following column definitions match:")
    public void thenColumnsMatch(DataTable dataTable) throws IOException {
        String datatableName = testContext().get(DATATABLE_NAME);
        Response<GetDataTablesResponse> httpResponse = dataTablesApi.getDatatable(datatableName).execute();
        checkSuccessfulApiCall(httpResponse);

        GetDataTablesResponse response = httpResponse.body();
        Map<String, ResultsetColumnHeaderData> columnMap = response.getColumnHeaderData().stream()
                .collect(Collectors.toMap(ResultsetColumnHeaderData::getColumnName, identity()));

        List<List<String>> rows = dataTable.asLists();
        List<List<String>> rowsWithoutHeader = rows.subList(1, rows.size());

        for (List<String> row : rowsWithoutHeader) {
            String columnName = row.get(0);
            boolean primaryKey = BooleanUtils.toBoolean(row.get(1));
            boolean unique = BooleanUtils.toBoolean(row.get(2));
            boolean indexed = BooleanUtils.toBoolean(row.get(3));

            ResultsetColumnHeaderData columnMetadata = columnMap.get(columnName);
            assertThat(columnMetadata).withFailMessage("Column [%s] not found on datatable", columnName).isNotNull();

            assertThat(columnMetadata.getIsColumnPrimaryKey())
                    .withFailMessage("Primary key definition for column [%s] does not match", columnName).isEqualTo(primaryKey);
            assertThat(columnMetadata.getIsColumnUnique())
                    .withFailMessage("Unique constraint definition for column [%s] does not match", columnName).isEqualTo(unique);
            assertThat(columnMetadata.getIsColumnIndexed()).withFailMessage("Index definition for column [%s] does not match", columnName)
                    .isEqualTo(indexed);
        }
    }

    @When("The client calls the query endpoint for the created datatable with {string} column filter, and {string} value filter")
    public void thenColum23nsMatch(String columnFilter, String valueFilter) throws IOException {
        Response<String> response = dataTablesApi.queryValues(testContext().get(DATATABLE_NAME), columnFilter, valueFilter, columnFilter)
                .execute();
        testContext().set(DATATABLE_QUERY_RESPONSE, response);
    }

    @Then("The status of the HTTP response should be {int}")
    public void thenStatusCodeMatch(int statusCode) {
        Response<String> response = testContext().get(DATATABLE_QUERY_RESPONSE);
        assertThat(response.code()).isEqualTo(statusCode);
    }

    @Then("The response body should contain the following message: {string}")
    public void thenColumnsMatch(String json) throws IOException {
        Response<String> response = testContext().get(DATATABLE_QUERY_RESPONSE);
        String jsonResponse = response.errorBody().string();
        assertThat(jsonResponse).contains(json);
    }
}
