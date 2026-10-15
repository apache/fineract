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

import static org.apache.fineract.integrationtests.common.Utils.initializeDefaultRequestSpecification;
import static org.apache.fineract.integrationtests.common.Utils.initializeDefaultResponseSpecification;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.PutDataTablesRequest;
import org.apache.fineract.client.models.PutDataTablesResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatatableHelper {

    private static final Gson GSON = new JSON().getGson();

    private static final Logger LOG = LoggerFactory.getLogger(DatatableHelper.class);
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String DATATABLE_URL = "/fineract-provider/api/v1/datatables";

    public DatatableHelper() {
        this(initializeDefaultRequestSpecification(), initializeDefaultResponseSpecification());
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public DatatableHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public <T> T createDatatable(final String json, final String jsonAttributeToGetBack) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, DATATABLE_URL + "?" + Utils.TENANT_IDENTIFIER, json,
                jsonAttributeToGetBack);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String createDatatable(final String apptableName, final boolean multiRow) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, DATATABLE_URL + "?" + Utils.TENANT_IDENTIFIER,
                getTestDatatableAsJSON(apptableName, multiRow), "resourceIdentifier");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public PostDataTablesResponse createDatatable(final String json) {
        final String response = Utils.performServerPost(this.requestSpec, this.responseSpec, DATATABLE_URL + "?" + Utils.TENANT_IDENTIFIER,
                json);
        return GSON.fromJson(response, PostDataTablesResponse.class);
    }

    public PostDataTablesResponse createDatatable(PostDataTablesRequest request) {
        return Calls.ok(FineractClientHelper.getFineractClient().dataTables.createDatatable(request));
    }

    public GetDataTablesResponse getDataTableDetails(final String dataTableName) {
        return Calls.ok(FineractClientHelper.getFineractClient().dataTables.getDatatable(dataTableName));
    }

    public PutDataTablesResponse updateDatatable(String dataTableName, PutDataTablesRequest request) {
        return Calls.ok(FineractClientHelper.getFineractClient().dataTables.updateDatatable(dataTableName, request));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public PutDataTablesResponse updateDatatable(String dataTableName, final String json) {
        final String response = Utils.performServerPut(this.requestSpec, this.responseSpec,
                DATATABLE_URL + "/" + dataTableName + "?" + Utils.TENANT_IDENTIFIER, json);
        return GSON.fromJson(response, PutDataTablesResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public <T> T createDatatableEntry(final String datatableName, final Integer apptableId, final boolean genericResultSet,
            final String json) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, DATATABLE_URL + "/" + datatableName + "/" + apptableId
                + "?genericResultSet=" + genericResultSet + "&" + Utils.TENANT_IDENTIFIER, json, "");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Integer createDatatableEntry(final String apptableName, final String datatableName, final Integer apptableId,
            final boolean genericResultSet, final String dateFormat, final String jsonAttributeToGetBack) {
        return Utils.performServerPost(
                this.requestSpec, this.responseSpec, DATATABLE_URL + "/" + datatableName + "/" + apptableId + "?genericResultSet="
                        + Boolean.toString(genericResultSet) + "&" + Utils.TENANT_IDENTIFIER,
                getTestDatatableEntryAsJSON(dateFormat), jsonAttributeToGetBack);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String readDatatableEntry(final String datatableName, final Integer resourceId, final boolean genericResultset) {
        return Utils.performServerGet(this.requestSpec, this.responseSpec, DATATABLE_URL + "/" + datatableName + "/" + resourceId
                + "?genericResultSet=" + String.valueOf(genericResultset) + "&" + Utils.TENANT_IDENTIFIER);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public <T> T readDatatableEntry(final String datatableName, final Integer resourceId, final boolean genericResultset,
            final Integer datatableResourceId, final String jsonAttributeToGetBack) {
        if (datatableResourceId == null) {
            return Utils.performServerGet(this.requestSpec, this.responseSpec, DATATABLE_URL + "/" + datatableName + "/" + resourceId
                    + "?genericResultSet=" + String.valueOf(genericResultset) + "&" + Utils.TENANT_IDENTIFIER, jsonAttributeToGetBack);
        } else {
            return Utils.performServerGet(
                    this.requestSpec, this.responseSpec, DATATABLE_URL + "/" + datatableName + "/" + resourceId + "/" + datatableResourceId
                            + "?genericResultSet=" + String.valueOf(genericResultset) + "&" + Utils.TENANT_IDENTIFIER,
                    jsonAttributeToGetBack);
        }
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Date readDatatableEntry(final String datatableName, final Integer resourceId, final boolean genericResultset, final int position,
            final String jsonAttributeToGetBack) {
        final JsonElement jsonElement = Utils.performServerGetArray(this.requestSpec, this.responseSpec, DATATABLE_URL + "/" + datatableName
                + "/" + resourceId + "?genericResultSet=" + String.valueOf(genericResultset) + "&" + Utils.TENANT_IDENTIFIER, position,
                jsonAttributeToGetBack);
        return Utils.convertJsonElementAsDate(jsonElement);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestDatatableAsJSON(final String apptableName, final String datatableName, final String codeName,
            final boolean multiRow) {
        final HashMap<String, Object> map = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();
        map.put("datatableName",
                Objects.requireNonNullElseGet(datatableName, () -> Utils.uniqueRandomStringGenerator(apptableName + "_", 5)));
        map.put("apptableName", apptableName);
        if ("m_client".equalsIgnoreCase(apptableName)) {
            map.put("entitySubType", "PERSON");
        } else {
            map.put("entitySubType", "");
        }
        map.put("multiRow", multiRow);
        addDatatableColumn(datatableColumnsList, "itsABoolean", "Boolean", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADate", "Date", true, null, null);
        addDatatableColumn(datatableColumnsList, "itsADatetime", "Datetime", true, null, null);
        addDatatableColumn(datatableColumnsList, "itsADecimal", "Decimal", true, null, null);
        addDatatableColumn(datatableColumnsList, "itsADropdown", "Dropdown", false, null, codeName);
        addDatatableColumn(datatableColumnsList, "itsANumber", "Number", true, null, null);
        addDatatableColumn(datatableColumnsList, "itsAString", "String", true, 10, null);
        addDatatableColumn(datatableColumnsList, "itsAText", "Text", true, null, null);
        map.put("columns", datatableColumnsList);
        String requestJsonString = new Gson().toJson(map);
        LOG.info("map : {}", requestJsonString);
        return requestJsonString;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestDatatableAsJSON(final String apptableName, final boolean multiRow) {
        final HashMap<String, Object> map = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();
        map.put("datatableName", Utils.uniqueRandomStringGenerator(apptableName + "_", 5));
        map.put("apptableName", apptableName);
        map.put("entitySubType", "PERSON");
        map.put("multiRow", multiRow);
        addDatatableColumn(datatableColumnsList, "Spouse Name", "String", true, 25, null);
        addDatatableColumn(datatableColumnsList, "Number of Dependents", "Number", true, null, null);
        addDatatableColumn(datatableColumnsList, "Time of Visit", "DateTime", false, null, null);
        addDatatableColumn(datatableColumnsList, "Date of Approval", "Date", false, null, null);
        map.put("columns", datatableColumnsList);
        String requestJsonString = new Gson().toJson(map);
        LOG.info("map : {}", requestJsonString);
        return requestJsonString;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestDatatableEntryAsJSON(final String dateFormat) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("Spouse Name", Utils.randomStringGenerator("Spouse_Name_", 5));
        map.put("Number of Dependents", Utils.randomNumberGenerator(1));
        map.put("Date of Approval", Utils.convertDateToURLFormat(Calendar.getInstance(), dateFormat));
        map.put("locale", "en");
        map.put("dateFormat", dateFormat);

        String requestJsonString = new Gson().toJson(map);
        LOG.info("map : {}", requestJsonString);
        return requestJsonString;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static HashMap<String, Object> addDatatableColumn(List<HashMap<String, Object>> datatableColumnsList, String columnName,
            String columnType, boolean isMandatory, Integer length, String codeName) {
        final HashMap<String, Object> datatableColumnMap = new HashMap<>();

        datatableColumnMap.put("name", columnName);
        if (columnType != null) {
            datatableColumnMap.put("type", columnType);
        }
        datatableColumnMap.put("mandatory", isMandatory);
        if (length != null) {
            datatableColumnMap.put("length", length);
        }
        if (codeName != null) {
            datatableColumnMap.put("code", codeName);
        }

        datatableColumnsList.add(datatableColumnMap);
        return datatableColumnMap;
    }
}
