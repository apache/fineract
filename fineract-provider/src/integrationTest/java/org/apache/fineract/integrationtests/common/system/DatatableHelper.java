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
package org.apache.fineract.integrationtests.common.system;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class DatatableHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String DATATABLE_URL = "/fineract-provider/api/v1/datatables";

    public DatatableHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public String createDatatable(final String apptableName, final boolean multiRow) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, DATATABLE_URL + "?" + Utils.TENANT_IDENTIFIER,
                getTestDatatableAsJSON(apptableName, multiRow), "resourceIdentifier");
    }

    public String deleteDatatable(final String datatableName) {
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, DATATABLE_URL + "/" + datatableName + "?" + Utils.TENANT_IDENTIFIER,
                "resourceIdentifier");
    }

    public Integer deleteDatatableEntries(final String datatableName, final Integer apptableId, String jsonAttributeToGetBack) {
        final String deleteEntryUrl = DATATABLE_URL + "/" + datatableName + "/" + apptableId + "?genericResultSet=true" + "&"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, deleteEntryUrl, jsonAttributeToGetBack);
    }

    public static void verifyDatatableCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String generatedDatatableName) {
        System.out.println("------------------------------CHECK DATATABLE DETAILS------------------------------------\n");
        final String responseRegisteredTableName = Utils.performServerGet(requestSpec, responseSpec, DATATABLE_URL + "/"
                + generatedDatatableName + "?" + Utils.TENANT_IDENTIFIER, "registeredTableName");
        assertEquals("ERROR IN CREATING THE DATATABLE", generatedDatatableName, responseRegisteredTableName);
    }

    public static String getTestDatatableAsJSON(final String apptableName, final boolean multiRow) {
        final HashMap<String, Object> map = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();
        map.put("datatableName", Utils.randomNameGenerator(apptableName + "_", 5));
        map.put("apptableName", apptableName);
        map.put("multiRow", multiRow);
        addDatatableColumns(datatableColumnsList, "Spouse Name", "String", true, 25);
        addDatatableColumns(datatableColumnsList, "Number of Dependents", "Number", true, null);
        addDatatableColumns(datatableColumnsList, "Time of Visit", "DateTime", false, null);
        addDatatableColumns(datatableColumnsList, "Date of Approval", "Date", false, null);
        map.put("columns", datatableColumnsList);
        String requestJsonString = new Gson().toJson(map);
        System.out.println("map : " + requestJsonString);
        return requestJsonString;
    }

    public static List<HashMap<String, Object>> addDatatableColumns(List<HashMap<String, Object>> datatableColumnsList, String columnName,
            String columnType, boolean isMandatory, Integer length) {

        final HashMap<String, Object> datatableColumnMap = new HashMap<>();

        datatableColumnMap.put("name", columnName);
        datatableColumnMap.put("type", columnType);
        datatableColumnMap.put("mandatory", isMandatory);
        if (length != null) {
            datatableColumnMap.put("length", length);
        }

        datatableColumnsList.add(datatableColumnMap);
        return datatableColumnsList;
    }

}
