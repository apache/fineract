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
package org.apache.fineract.integrationtests.datatable;

import static org.apache.fineract.integrationtests.common.system.DatatableHelper.addDatatableColumn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PostDataTablesAppTableIdResponse;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.PutDataTablesAppTableIdDatatableIdResponse;
import org.apache.fineract.client.models.PutDataTablesResponse;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(LoanTestLifecycleExtension.class)
public class DatatableIntegrationTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(DatatableIntegrationTest.class);

    private static final String CLIENT_APP_TABLE_NAME = "m_client";
    private static final String CLIENT_PERSON_SUBTYPE_NAME = "Person";
    private static final String LOAN_APP_TABLE_NAME = "m_loan";

    private static final Float LP_PRINCIPAL = 10000.0f;
    private static final String LP_REPAYMENTS = "5";
    private static final String LP_REPAYMENT_PERIOD = "2";
    private static final String LP_INTEREST_RATE = "1";
    private static final String EXPECTED_DISBURSAL_DATE = "14 March 2011";
    private static final String LOAN_APPLICATION_SUBMISSION_DATE = "13 March 2011";
    private static final String LOAN_TERM_FREQUENCY = "10";
    private static final String INDIVIDUAL_LOAN = "individual";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String DEPOSIT_AMOUNT = "7000";
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private DatatableHelper datatableHelper;

    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
    }

    @Test
    public void validateCreateReadDeleteDatatable() throws ParseException {
        // Fetch / Create tst code
        String tst_tst_tst = "TST_TST_TST".toLowerCase();
        HashMap<String, Object> codeResponse = CodeHelper.getCodeByName(this.requestSpec, this.responseSpec, tst_tst_tst);

        Integer createdCodeId = (Integer) codeResponse.get("id");
        Integer createdCodeValueId;
        Integer createdCodeValueIdSecond;
        if (createdCodeId == null) {
            createdCodeId = (Integer) CodeHelper.createCode(this.requestSpec, this.responseSpec, tst_tst_tst, "resourceId");

            createdCodeValueId = CodeHelper.createCodeValue(this.requestSpec, this.responseSpec, createdCodeId,
                    Utils.randomStringGenerator("cv_", 8), 1);
            createdCodeValueIdSecond = CodeHelper.createCodeValue(this.requestSpec, this.responseSpec, createdCodeId,
                    Utils.randomStringGenerator("cv_", 8), 2);
        } else {
            List<HashMap<String, Object>> codeValuesForCode = CodeHelper.getCodeValuesForCode(this.requestSpec, this.responseSpec,
                    createdCodeId, "");
            createdCodeValueId = (Integer) codeValuesForCode.get(0).get("id");
            createdCodeValueIdSecond = (Integer) codeValuesForCode.get(1).get("id");
        }

        // creating datatable for client entity
        final HashMap<String, Object> columnMap = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();
        columnMap.put("datatableName", Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_", 5).toLowerCase().toLowerCase());
        columnMap.put("entitySubType", "PERSON");
        columnMap.put("multiRow", false);
        String itsABoolean = "itsaboolean";
        String itsADate = "itsadate";
        String itsADatetime = "itsadatetime";
        String itsADecimal = "itsadecimal";
        String itsADropdown = "itsadropdown";
        String itsANumber = "itsanumber";
        String itsAString = "itsastring";
        String itsAText = "itsatext";
        String itsAJson = "itsajson";
        String tst_tst_tst_cd_itsADropdown = tst_tst_tst + "_cd_itsadropdown";
        String dateFormat = "dateFormat";

        addDatatableColumn(datatableColumnsList, itsABoolean, "Boolean", false, null, null);
        addDatatableColumn(datatableColumnsList, itsADate, "Date", true, null, null);
        addDatatableColumn(datatableColumnsList, itsADatetime, "Datetime", true, null, null);
        addDatatableColumn(datatableColumnsList, itsADecimal, "Decimal", true, null, null);
        addDatatableColumn(datatableColumnsList, itsADropdown, "Dropdown", false, null, tst_tst_tst);
        addDatatableColumn(datatableColumnsList, itsANumber, "Number", true, null, null);
        addDatatableColumn(datatableColumnsList, itsAString, "String", true, 10, null);
        columnMap.put("columns", datatableColumnsList);

        // try to create datatable without apptable
        columnMap.put("apptableName", null);
        String errorRequestJsonString = new Gson().toJson(columnMap);
        ResponseSpecification responseSpecError400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        DatatableHelper error400Helper = new DatatableHelper(this.requestSpec, responseSpecError400);
        HashMap<String, Object> errorResponse = error400Helper.createDatatable(errorRequestJsonString, "");
        assertEquals("validation.msg.validation.errors.exist", ((Map) errorResponse).get("userMessageGlobalisationCode"));
        List errors = (List) ((Map) errorResponse).get("errors");
        assertEquals(2, errors.size());
        assertEquals("validation.msg.datatable.apptableName.cannot.be.blank", ((Map) errors.get(0)).get("userMessageGlobalisationCode"));
        assertEquals("validation.msg.datatable.apptableName.is.not.one.of.expected.enumerations",
                ((Map) errors.get(1)).get("userMessageGlobalisationCode"));

        // set valid apptable name
        columnMap.put("apptableName", CLIENT_APP_TABLE_NAME);

        // try to create datatable with invalid column type
        HashMap<String, Object> textColumn = addDatatableColumn(datatableColumnsList, itsAText, "Invalid", true, null, null);
        errorRequestJsonString = new Gson().toJson(columnMap);
        errorResponse = error400Helper.createDatatable(errorRequestJsonString, "");
        assertEquals("validation.msg.validation.errors.exist", ((Map) errorResponse).get("userMessageGlobalisationCode"));
        errors = (List) ((Map) errorResponse).get("errors");
        assertEquals(1, errors.size());
        Map error = (Map) errors.get(0);
        assertEquals("validation.msg.datatable.type.is.not.one.of.expected.enumerations", error.get("userMessageGlobalisationCode"));
        assertTrue(((String) error.get("defaultUserMessage"))
                .contains("string, number, boolean, decimal, date, datetime, text, json, dropdown"));

        // set valid type
        textColumn.put("type", "Text");
        // add json type
        addDatatableColumn(datatableColumnsList, itsAJson, "Json", false, null, null);

        String datatabelRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("map : {}", datatabelRequestJsonString);

        HashMap<String, Object> datatableResponse = this.datatableHelper.createDatatable(datatabelRequestJsonString, "");
        String datatableName = (String) datatableResponse.get("resourceIdentifier");
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, datatableName);

        // try to create with the same name
        errorResponse = error400Helper.createDatatable(datatabelRequestJsonString, "");
        assertEquals("validation.msg.validation.errors.exist", ((Map) errorResponse).get("userMessageGlobalisationCode"));

        // creating client with datatables
        final Integer clientID = ClientHelper.createClientAsPerson(requestSpec, responseSpec);

        // creating new client datatable entry
        final boolean genericResultSet = true;

        final HashMap<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put(itsABoolean, Utils.randomNumberGenerator(1) % 2 == 0);
        datatableEntryMap.put(itsADate, Utils.randomDateGenerator("yyyy-MM-dd"));
        datatableEntryMap.put(itsADatetime, Utils.randomDateTimeGenerator("yyyy-MM-dd"));
        datatableEntryMap.put(itsADecimal, Utils.randomDecimalGenerator(4, 3));
        datatableEntryMap.put(tst_tst_tst_cd_itsADropdown, createdCodeValueId);
        datatableEntryMap.put(itsANumber, Utils.randomNumberGenerator(5));
        datatableEntryMap.put(itsAString, Utils.randomStringGenerator("", 8));
        datatableEntryMap.put(itsAText, Utils.randomStringGenerator("", 1000));
        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put(dateFormat, "yyyy-MM-dd");

        String json = "{\"testparam\": \"testvalue\"}";
        // add invalid json
        datatableEntryMap.put(itsAJson, '{' + json);

        String datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        ResponseSpecification responseSpecError403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        DatatableHelper error403Helper = new DatatableHelper(this.requestSpec, responseSpecError403);
        errorResponse = error403Helper.createDatatableEntry(datatableName, clientID, genericResultSet, datatabelEntryRequestJsonString);

        // add valid json
        datatableEntryMap.put(itsAJson, json);

        datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("map : {}", datatabelEntryRequestJsonString);

        HashMap<String, Object> datatableEntryResponse = this.datatableHelper.createDatatableEntry(datatableName, clientID,
                genericResultSet, datatabelEntryRequestJsonString);
        assertNotNull(datatableEntryResponse.get("resourceId"), "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        final HashMap<String, Object> items = this.datatableHelper.readDatatableEntry(datatableName, clientID, genericResultSet,
                (Integer) datatableEntryResponse.get("resourceId"), "");
        assertNotNull(items);

        List columnHeaders = (List) items.get("columnHeaders");
        List columnData = (List) items.get("data");
        assertEquals(1, columnData.size());

        Map data = (Map) columnData.get(0);

        assertEquals("client_id", ((Map) columnHeaders.get(0)).get("columnName"));
        assertEquals(clientID, ((List) data.get("row")).get(0));

        assertEquals(itsABoolean, ((Map) columnHeaders.get(1)).get("columnName"));
        assertEquals(datatableEntryMap.get(itsABoolean), ((List) data.get("row")).get(1));

        assertEquals(itsADate, ((Map) columnHeaders.get(2)).get("columnName"));
        assertEquals(datatableEntryMap.get(itsADate), Utils.arrayDateToString((List) ((List) data.get("row")).get(2)));

        assertEquals(itsADatetime, ((Map) columnHeaders.get(3)).get("columnName"));
        assertEquals(datatableEntryMap.get(itsADatetime), Utils.arrayDateTimeToString((List) ((List) data.get("row")).get(3)));

        assertEquals(itsADecimal, ((Map) columnHeaders.get(4)).get("columnName"));
        assertEquals(datatableEntryMap.get(itsADecimal), ((List) data.get("row")).get(4));

        assertEquals(tst_tst_tst_cd_itsADropdown, ((Map) columnHeaders.get(5)).get("columnName"));
        assertEquals(datatableEntryMap.get(tst_tst_tst_cd_itsADropdown), ((List) data.get("row")).get(5));

        assertEquals(itsANumber, ((Map) columnHeaders.get(6)).get("columnName"));
        assertEquals(datatableEntryMap.get(itsANumber), ((List) data.get("row")).get(6));

        assertEquals(itsAString, ((Map) columnHeaders.get(7)).get("columnName"));
        assertEquals(datatableEntryMap.get(itsAString), ((List) data.get("row")).get(7));

        assertEquals(itsAText, ((Map) columnHeaders.get(8)).get("columnName"));
        assertEquals(datatableEntryMap.get(itsAText), ((List) data.get("row")).get(8));

        assertEquals(itsAJson, ((Map) columnHeaders.get(9)).get("columnName"));
        Object jsonResponse = ((List) data.get("row")).get(9);
        assertEquals(datatableEntryMap.get(itsAJson), jsonResponse instanceof Map ? ((Map) jsonResponse).get("value") : jsonResponse);

        // Read the Datatable entry generated with genericResultSet in false
        List<HashMap<String, Object>> datatableEntryResponseNoGenericResult = this.datatableHelper.readDatatableEntry(datatableName,
                clientID, !genericResultSet, (Integer) datatableEntryResponse.get("resourceId"), "");
        assertNotNull(datatableEntryResponseNoGenericResult, "ERROR IN GETTING THE DATE VALUE FROM DATATABLE RECORD");
        assertEquals(1, datatableEntryResponseNoGenericResult.size());

        HashMap<String, Object> responseMap = datatableEntryResponseNoGenericResult.get(0);
        assertEquals(clientID, responseMap.get("client_id"));
        assertEquals(datatableEntryMap.get(itsABoolean), Boolean.valueOf((String) responseMap.get(itsABoolean)));
        assertEquals(datatableEntryMap.get(itsADate), Utils.arrayDateToString((List) responseMap.get(itsADate)));
        assertEquals(datatableEntryMap.get(itsADecimal), responseMap.get(itsADecimal));
        assertEquals(datatableEntryMap.get(itsADatetime), Utils.arrayDateTimeToString((List<Integer>) responseMap.get(itsADatetime)));
        assertEquals(datatableEntryMap.get(tst_tst_tst_cd_itsADropdown), responseMap.get(tst_tst_tst_cd_itsADropdown));
        assertEquals(datatableEntryMap.get(itsANumber), responseMap.get(itsANumber));
        assertEquals(datatableEntryMap.get(itsAString), responseMap.get(itsAString));
        assertEquals(datatableEntryMap.get(itsAText), responseMap.get(itsAText));
        assertEquals(datatableEntryMap.get(itsAJson), responseMap.get(itsAJson));

        // Update datatable entry
        Boolean previousBoolean = (Boolean) datatableEntryMap.get(itsABoolean);
        datatableEntryMap.put(itsABoolean, !previousBoolean);
        datatableEntryMap.put(itsADate, Utils.randomDateGenerator("yyyy-MM-dd"));
        datatableEntryMap.put(itsADatetime, Utils.randomDateTimeGenerator("yyyy-MM-dd"));
        datatableEntryMap.put(itsADecimal, Utils.randomDecimalGenerator(4, 3));
        datatableEntryMap.put(tst_tst_tst_cd_itsADropdown, null);
        datatableEntryMap.put(itsANumber, Utils.randomNumberGenerator(5));
        datatableEntryMap.put(itsAString, Utils.randomStringGenerator("", 8));
        datatableEntryMap.put(itsAText, Utils.randomStringGenerator("", 1000));

        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put(dateFormat, "yyyy-MM-dd");

        datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("map : {}", datatabelEntryRequestJsonString);

        HashMap<String, Object> updatedDatatableEntryResponse = this.datatableHelper.updateDatatableEntry(datatableName, clientID, false,
                datatabelEntryRequestJsonString);

        assertEquals(clientID, updatedDatatableEntryResponse.get("clientId"));

        assertEquals(datatableEntryMap.get(itsABoolean), ((Map) updatedDatatableEntryResponse.get("changes")).get(itsABoolean));
        assertEquals(datatableEntryMap.get(itsADate),
                Utils.arrayDateToString((List) ((Map) updatedDatatableEntryResponse.get("changes")).get(itsADate)));
        assertEquals(datatableEntryMap.get(itsADecimal), ((Map) updatedDatatableEntryResponse.get("changes")).get(itsADecimal));
        assertEquals(datatableEntryMap.get(itsADatetime),
                Utils.arrayDateTimeToString((List<Integer>) ((Map) updatedDatatableEntryResponse.get("changes")).get(itsADatetime)));
        assertEquals(datatableEntryMap.get(tst_tst_tst_cd_itsADropdown),
                ((Map) updatedDatatableEntryResponse.get("changes")).get(tst_tst_tst_cd_itsADropdown));
        assertEquals(datatableEntryMap.get(itsANumber), ((Map) updatedDatatableEntryResponse.get("changes")).get(itsANumber));
        assertEquals(datatableEntryMap.get(itsAString), ((Map) updatedDatatableEntryResponse.get("changes")).get(itsAString));
        assertEquals(datatableEntryMap.get(itsAText), ((Map) updatedDatatableEntryResponse.get("changes")).get(itsAText));

        List<String> columnsToValidate = List.of(itsABoolean, itsADate, itsADatetime, itsAString, itsAText, itsADecimal,
                tst_tst_tst_cd_itsADropdown);
        for (String column : columnsToValidate) {
            String valueFilter = column.equals(tst_tst_tst_cd_itsADropdown) ? createdCodeValueId.toString()
                    : datatableEntryMap.get(column).toString();
            String rows = Calls.ok(fineract().dataTables.queryValues(datatableName, column, valueFilter, column));
            JsonArray jsonArray = JsonParser.parseString(rows).getAsJsonArray();
            if (itsADatetime.equals(column)) {
                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date parsedRequest = df1.parse(datatableEntryMap.get(column).toString());
                Date parsedResponse = df2.parse(jsonArray.get(0).getAsJsonObject().get(column).getAsString());
                assertFalse(parsedRequest.after(parsedResponse));
                assertFalse(parsedRequest.before(parsedResponse));
            } else if (itsADecimal.equals(column)) {
                assertEquals(0, new BigDecimal(datatableEntryMap.get(column).toString())
                        .compareTo(new BigDecimal(jsonArray.get(0).getAsJsonObject().get(column).getAsString())));
            } else if (tst_tst_tst_cd_itsADropdown.equals(column)) {
                assertEquals(createdCodeValueId.toString(), jsonArray.get(0).getAsJsonObject().get(column).getAsString());
            } else {
                assertEquals(datatableEntryMap.get(column).toString(), jsonArray.get(0).getAsJsonObject().get(column).getAsString());
            }
        }

        // deleting datatable entries
        Integer appTableId = (Integer) this.datatableHelper.deleteDatatableEntries(datatableName, clientID, "clientId");
        assertEquals(clientID, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName);
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");

        GetDataTablesResponse dataTable = datatableHelper.getDataTableDetails(datatableName);
        assertNull(dataTable);
    }

    @Test
    public void validateCreateReadDeleteDatatableWithCaseSensitive() throws ParseException {
        // creating datatable for client entity
        final HashMap<String, Object> columnMap = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();
        columnMap.put("datatableName", Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_", 5));
        columnMap.put("apptableName", CLIENT_APP_TABLE_NAME);
        columnMap.put("entitySubType", "PERSON");
        columnMap.put("multiRow", false);
        String itsADate = "itsADate";
        String itsADecimal = "itsADecimal";
        String itsAString = "itsAString";
        String dateFormat = "dateFormat";

        addDatatableColumn(datatableColumnsList, itsADate, "Date", true, null, null);
        addDatatableColumn(datatableColumnsList, itsADecimal, "Decimal", true, null, null);
        addDatatableColumn(datatableColumnsList, itsAString, "String", true, 10, null);
        columnMap.put("columns", datatableColumnsList);
        String datatabelRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("map : {}", datatabelRequestJsonString);

        HashMap<String, Object> datatableResponse = this.datatableHelper.createDatatable(datatabelRequestJsonString, "");
        String datatableName = (String) datatableResponse.get("resourceIdentifier");
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, datatableName);

        // creating client with datatables
        final Integer clientID = ClientHelper.createClientAsPerson(requestSpec, responseSpec);

        // creating new client datatable entry
        final boolean genericResultSet = true;

        final HashMap<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put(itsADate, Utils.randomDateGenerator("yyyy-MM-dd"));
        datatableEntryMap.put(itsADecimal, Utils.randomDecimalGenerator(4, 3));
        datatableEntryMap.put(itsAString, Utils.randomStringGenerator("", 8));
        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put(dateFormat, "yyyy-MM-dd");

        String datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("map : {}", datatabelEntryRequestJsonString);

        HashMap<String, Object> datatableEntryResponse = this.datatableHelper.createDatatableEntry(datatableName, clientID,
                genericResultSet, datatabelEntryRequestJsonString);
        assertNotNull(datatableEntryResponse.get("resourceId"), "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        final HashMap<String, Object> items = this.datatableHelper.readDatatableEntry(datatableName, clientID, genericResultSet,
                (Integer) datatableEntryResponse.get("resourceId"), "");
        assertNotNull(items);
        assertEquals(1, ((List) items.get("data")).size());

        assertEquals("client_id", ((Map) ((List) items.get("columnHeaders")).get(0)).get("columnName"));
        assertEquals(clientID, ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(0));

        assertEquals(itsADate, ((Map) ((List) items.get("columnHeaders")).get(1)).get("columnName"));
        assertEquals(datatableEntryMap.get(itsADate),
                Utils.arrayDateToString((List) ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(1)));

        assertEquals(itsADecimal, ((Map) ((List) items.get("columnHeaders")).get(2)).get("columnName"));
        assertEquals(datatableEntryMap.get(itsADecimal), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(2));

        assertEquals(itsAString, ((Map) ((List) items.get("columnHeaders")).get(3)).get("columnName"));
        assertEquals(datatableEntryMap.get(itsAString), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(3));

        // Update datatable entry
        final String randomValue = Utils.randomStringGenerator("", 8);
        datatableEntryMap.put(itsADate, Utils.randomDateGenerator("yyyy-MM-dd"));
        datatableEntryMap.put(itsADecimal, Utils.randomDecimalGenerator(4, 3));
        datatableEntryMap.put(itsAString, randomValue);

        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put(dateFormat, "yyyy-MM-dd");

        datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("map : {}", datatabelEntryRequestJsonString);

        HashMap<String, Object> updatedDatatableEntryResponse = this.datatableHelper.updateDatatableEntry(datatableName, clientID, false,
                datatabelEntryRequestJsonString);

        assertEquals(clientID, updatedDatatableEntryResponse.get("clientId"));

        assertEquals(datatableEntryMap.get(itsADate),
                Utils.arrayDateToString((List) ((Map) updatedDatatableEntryResponse.get("changes")).get(itsADate)));
        assertEquals(datatableEntryMap.get(itsADecimal), ((Map) updatedDatatableEntryResponse.get("changes")).get(itsADecimal));
        assertEquals(datatableEntryMap.get(itsAString), ((Map) updatedDatatableEntryResponse.get("changes")).get(itsAString));

        // Read the datatable with a query
        LOG.info("query in {} for value : {}", itsAString, randomValue);
        final String queryResult = this.datatableHelper.runDatatableQuery(datatableName, itsAString, randomValue, "client_id,itsADecimal");
        assertNotNull(queryResult);
        LOG.info("query result : {}", queryResult);

        // deleting datatable entries
        Integer appTableId = (Integer) this.datatableHelper.deleteDatatableEntries(datatableName, clientID, "clientId");
        assertEquals(clientID, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName);
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateInsertNullValues() {
        // Fetch / Create TST code
        HashMap<String, Object> codeResponse = CodeHelper.getCodeByName(this.requestSpec, this.responseSpec, "TST_TST_TST");

        // creating datatable for client entity
        final HashMap<String, Object> columnMap = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();
        columnMap.put("datatableName", Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5));
        columnMap.put("apptableName", LOAN_APP_TABLE_NAME);
        columnMap.put("entitySubType", "");
        columnMap.put("multiRow", true);
        addDatatableColumn(datatableColumnsList, "itsABoolean", "Boolean", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADate", "Date", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADatetime", "Datetime", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADecimal", "Decimal", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADropdown", "Dropdown", false, null, "TST_TST_TST");
        addDatatableColumn(datatableColumnsList, "itsANumber", "Number", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsAString", "String", false, 10, null);
        addDatatableColumn(datatableColumnsList, "itsAText", "Text", false, null, null);
        columnMap.put("columns", datatableColumnsList);
        String datatabelRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("map : {}", datatabelRequestJsonString);

        HashMap<String, Object> datatableResponse = this.datatableHelper.createDatatable(datatabelRequestJsonString, "");
        String datatableName = (String) datatableResponse.get("resourceIdentifier");
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, datatableName);

        // try to create with the same name
        ResponseSpecification responseSpecError400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        DatatableHelper error400Helper = new DatatableHelper(this.requestSpec, responseSpecError400);
        HashMap<String, Object> response = error400Helper.createDatatable(datatabelRequestJsonString, "");
        assertEquals("validation.msg.validation.errors.exist", ((Map) response).get("userMessageGlobalisationCode"));

        // creating client with datatables
        final Integer clientID = ClientHelper.createClientAsPerson(requestSpec, responseSpec);
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled();
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        // creating new client datatable entry
        final boolean genericResultSet = true;

        HashMap<String, Object> firstEntryMap = new HashMap<>();
        firstEntryMap.put("itsABoolean", null);
        firstEntryMap.put("itsADate", null);
        firstEntryMap.put("itsADatetime", null);
        firstEntryMap.put("itsADecimal", null);
        firstEntryMap.put("TST_TST_TST_cd_itsADropdown", null);
        firstEntryMap.put("itsANumber", null);
        firstEntryMap.put("itsAString", null);
        firstEntryMap.put("itsAText", null);

        firstEntryMap.put("locale", "en");
        firstEntryMap.put("dateFormat", "yyyy-MM-dd");

        String firstEntryRequestJsonString = new GsonBuilder().serializeNulls().create().toJson(firstEntryMap);
        LOG.info("map : {}", firstEntryRequestJsonString);

        HashMap<String, Object> firstEntryResponse = this.datatableHelper.createDatatableEntry(datatableName, loanID, genericResultSet,
                firstEntryRequestJsonString);
        assertNotNull(firstEntryResponse.get("resourceId"), "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        HashMap<String, Object> secondEntryMap = new HashMap<>();
        secondEntryMap.put("itsABoolean", "");
        secondEntryMap.put("itsADate", "");
        secondEntryMap.put("itsADatetime", "");
        secondEntryMap.put("itsADecimal", "");
        secondEntryMap.put("TST_TST_TST_cd_itsADropdown", "");
        secondEntryMap.put("itsANumber", "");
        secondEntryMap.put("itsAString", "");
        secondEntryMap.put("itsAText", "");

        secondEntryMap.put("locale", "en");
        secondEntryMap.put("dateFormat", "yyyy-MM-dd");

        String secondEntryRequestJsonString = new GsonBuilder().serializeNulls().create().toJson(secondEntryMap);
        HashMap<String, Object> secondEntryResponse = this.datatableHelper.createDatatableEntry(datatableName, loanID, genericResultSet,
                secondEntryRequestJsonString);
        assertNotNull(secondEntryResponse.get("resourceId"), "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        HashMap<String, Object> items = this.datatableHelper.readDatatableEntry(datatableName, loanID, genericResultSet, null, "");
        assertNotNull(items);
        assertEquals(2, ((List) items.get("data")).size());

        List headers = (List) items.get("columnHeaders");
        List firstEntryValues = (List) ((Map) ((List) items.get("data")).get(0)).get("row");
        assertEquals("id", ((Map) headers.get(0)).get("columnName"));
        assertEquals(1, firstEntryValues.get(0));
        assertEquals("loan_id", ((Map) headers.get(1)).get("columnName"));
        assertEquals(loanID, firstEntryValues.get(1));
        assertEquals("itsABoolean", ((Map) headers.get(2)).get("columnName"));
        assertNull(firstEntryValues.get(2));
        assertEquals("itsADate", ((Map) headers.get(3)).get("columnName"));
        assertNull(firstEntryValues.get(3));
        assertEquals("itsADatetime", ((Map) headers.get(4)).get("columnName"));
        assertNull(firstEntryValues.get(4));
        assertEquals("itsADecimal", ((Map) headers.get(5)).get("columnName"));
        assertNull(firstEntryValues.get(5));
        assertEquals("TST_TST_TST_cd_itsADropdown", ((Map) headers.get(6)).get("columnName"));
        assertNull(firstEntryValues.get(6));
        assertEquals("itsANumber", ((Map) headers.get(7)).get("columnName"));
        assertNull(firstEntryValues.get(7));
        assertEquals("itsAString", ((Map) headers.get(8)).get("columnName"));
        assertNull(firstEntryValues.get(8));
        assertEquals("itsAText", ((Map) headers.get(9)).get("columnName"));
        assertNull(firstEntryValues.get(9));

        List secondEntryValues = (List) ((Map) ((List) items.get("data")).get(1)).get("row");
        assertEquals(2, secondEntryValues.get(0));
        assertEquals(loanID, secondEntryValues.get(1));
        assertNull(secondEntryValues.get(2));
        assertNull(secondEntryValues.get(3));
        assertNull(secondEntryValues.get(4));
        assertNull(secondEntryValues.get(5));
        assertNull(secondEntryValues.get(6));
        assertNull(secondEntryValues.get(7));
        assertNull(secondEntryValues.get(8));
        assertNull(secondEntryValues.get(9));

        PutDataTablesAppTableIdDatatableIdResponse updatedDatatableEntryResponse = this.datatableHelper.updateDatatableEntry(datatableName,
                loanID, 1, secondEntryRequestJsonString);
        assertNotNull(updatedDatatableEntryResponse);
        assertEquals(0, updatedDatatableEntryResponse.getChanges().size());
    }

    @Test
    public void validateCreateAndEditDatatable() {
        // Creating client
        final Integer clientId = ClientHelper.createClientAsPerson(requestSpec, responseSpec);
        final Integer randomNumber = Utils.randomNumberGenerator(3);

        // Creating datatable for Client Person
        final String datatableName = Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_", 5);
        final boolean genericResultSet = true;

        HashMap<String, Object> columnMap = new HashMap<>();
        ArrayList<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();
        columnMap.put("datatableName", datatableName);
        columnMap.put("apptableName", CLIENT_APP_TABLE_NAME);
        columnMap.put("entitySubType", CLIENT_PERSON_SUBTYPE_NAME);
        columnMap.put("multiRow", false);
        addDatatableColumn(datatableColumnsList, "itsANumber", "Number", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsAString", "String", false, 10, null);
        columnMap.put("columns", datatableColumnsList);
        String datatabelRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("map : {}", datatabelRequestJsonString);

        PostDataTablesResponse datatableCreateResponse = this.datatableHelper.createDatatable(datatabelRequestJsonString);
        assertEquals(datatableName, datatableCreateResponse.getResourceIdentifier());
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, datatableName);

        // Insert first values
        final String randomString = Utils.randomStringGenerator("Q", 8);
        HashMap<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put("itsANumber", randomNumber);
        datatableEntryMap.put("itsAString", randomString);

        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        String datatableEntryRequestJsonString = new GsonBuilder().serializeNulls().create().toJson(datatableEntryMap);
        PostDataTablesAppTableIdResponse datatableEntryResponse = this.datatableHelper.addDatatableEntry(datatableName, clientId,
                genericResultSet, datatableEntryRequestJsonString);
        assertNotNull(datatableEntryResponse.getResourceId(), "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        HashMap<String, Object> items = this.datatableHelper.readDatatableEntry(datatableName, clientId, genericResultSet, null, "");
        assertNotNull(items);
        List data = (List) items.get("data");
        assertEquals(1, data.size());
        List records = (List) ((Map) data.get(0)).get("row");
        LOG.info("Record created at {}", records.get(3));
        LOG.info("Record updated at {}", records.get(4));

        assertEquals(clientId, records.get(0));
        assertEquals(randomString, records.get(2));

        // Update DataTable
        columnMap = new HashMap<>();
        columnMap.put("apptableName", CLIENT_APP_TABLE_NAME);
        columnMap.put("entitySubType", CLIENT_PERSON_SUBTYPE_NAME);
        datatableColumnsList = new ArrayList<>();
        addDatatableColumn(datatableColumnsList, "itsAText", "Text", false, null, null);
        columnMap.put("addColumns", datatableColumnsList);
        datatabelRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("map to update : {}", datatabelRequestJsonString);
        PutDataTablesResponse datatableUpdateResponse = this.datatableHelper.updateDatatable(datatableName, datatabelRequestJsonString);
        assertNotNull(datatableUpdateResponse);
        assertEquals(datatableName, datatableUpdateResponse.getResourceIdentifier());

        // Update DataTable Entry after Update DataTable schema
        datatableEntryMap = new HashMap<>();
        final String textValue = Utils.randomStringGenerator(randomString, 120);
        datatableEntryMap.put("itsAText", textValue);
        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        datatableEntryRequestJsonString = new GsonBuilder().serializeNulls().create().toJson(datatableEntryMap);
        LOG.info("map to update : {}", datatableEntryRequestJsonString);
        PutDataTablesAppTableIdDatatableIdResponse updatedDatatableEntryResponse = this.datatableHelper.updateDatatableEntry(datatableName,
                clientId, datatableEntryRequestJsonString);
        assertNotNull(updatedDatatableEntryResponse);
        assertEquals(1, updatedDatatableEntryResponse.getChanges().size());

        // Read the Datatable entry generated with genericResultSet in true (default)
        items = this.datatableHelper.readDatatableEntry(datatableName, clientId, genericResultSet, null, "");
        assertNotNull(items);
        data = (List) items.get("data");
        assertEquals(1, data.size());

        records = (List) ((Map) data.get(0)).get("row");
        LOG.info("Record created at {}", records.get(3));
        LOG.info("Record updated at {}", records.get(4));

        assertEquals(clientId, records.get(0));
        assertEquals(randomString, records.get(2));
        assertEquals(textValue, records.get(5));

        Integer resourceId = (Integer) this.datatableHelper.deleteDatatableEntries(datatableName, clientId, "resourceId");
        assertEquals(clientId, resourceId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // Update - update, delete DataTable columns
        columnMap = new HashMap<>();
        columnMap.put("apptableName", CLIENT_APP_TABLE_NAME);
        columnMap.put("entitySubType", CLIENT_PERSON_SUBTYPE_NAME);
        List<Map<String, Object>> dropColumnsList = Collections.singletonList(Collections.singletonMap("name", "itsANumber"));
        columnMap.put("dropColumns", dropColumnsList);
        ArrayList<HashMap<String, Object>> changeColumnsList = new ArrayList<>();
        addDatatableColumn(changeColumnsList, "itsAString", null, false, 100, null);
        columnMap.put("changeColumns", changeColumnsList);
        datatabelRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("map to update : {}", datatabelRequestJsonString);
        datatableUpdateResponse = this.datatableHelper.updateDatatable(datatableName, datatabelRequestJsonString);
        assertNotNull(datatableUpdateResponse);
        assertEquals(datatableName, datatableUpdateResponse.getResourceIdentifier());

        GetDataTablesResponse dataTable = datatableHelper.getDataTableDetails(datatableName);
        List<ResultsetColumnHeaderData> columnHeaders = dataTable.getColumnHeaderData();
        assertEquals(5, columnHeaders.size());
        ResultsetColumnHeaderData stringColumn = columnHeaders.get(1);
        assertEquals("itsAString", stringColumn.getColumnName());
        assertEquals(100, stringColumn.getColumnLength());
    }

    @Test
    public void validateReadDatatableMultirow() {
        // Fetch / Create TST code
        String tst_tst_tst = "tst_tst_tst";
        HashMap<String, Object> codeResponse = CodeHelper.getCodeByName(this.requestSpec, this.responseSpec, tst_tst_tst);

        Integer createdCodeId = (Integer) codeResponse.get("id");
        Integer createdCodeValueId;
        Integer createdCodeValueIdSecond;
        if (createdCodeId == null) {
            createdCodeId = (Integer) CodeHelper.createCode(this.requestSpec, this.responseSpec, tst_tst_tst, "resourceId");

            createdCodeValueId = CodeHelper.createCodeValue(this.requestSpec, this.responseSpec, createdCodeId,
                    Utils.randomStringGenerator("cv_", 8), 1);
            createdCodeValueIdSecond = CodeHelper.createCodeValue(this.requestSpec, this.responseSpec, createdCodeId,
                    Utils.randomStringGenerator("cv_", 8), 2);
        } else {
            List<HashMap<String, Object>> codeValuesForCode = CodeHelper.getCodeValuesForCode(this.requestSpec, this.responseSpec,
                    createdCodeId, "");
            createdCodeValueId = (Integer) codeValuesForCode.get(0).get("id");
            createdCodeValueIdSecond = (Integer) codeValuesForCode.get(1).get("id");
        }

        // creating datatable for client entity
        final HashMap<String, Object> columnMap = new HashMap<>();
        final List<HashMap<String, Object>> datatableColumnsList = new ArrayList<>();
        columnMap.put("datatableName", Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5));
        columnMap.put("apptableName", LOAN_APP_TABLE_NAME);
        columnMap.put("entitySubType", "");
        columnMap.put("multiRow", true);
        addDatatableColumn(datatableColumnsList, "itsABoolean", "Boolean", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADate", "Date", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADatetime", "Datetime", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADecimal", "Decimal", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADropdown", "Dropdown", false, null, tst_tst_tst);
        addDatatableColumn(datatableColumnsList, "itsANumber", "Number", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsAString", "String", false, 10, null);
        addDatatableColumn(datatableColumnsList, "itsAText", "Text", false, null, null);
        columnMap.put("columns", datatableColumnsList);
        String datatabelRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("map : {}", datatabelRequestJsonString);

        HashMap<String, Object> datatableResponse = this.datatableHelper.createDatatable(datatabelRequestJsonString, "");
        String datatableName = (String) datatableResponse.get("resourceIdentifier");
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, datatableName);

        // try to create with the same name
        ResponseSpecification responseSpecError400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        DatatableHelper error400Helper = new DatatableHelper(this.requestSpec, responseSpecError400);
        HashMap<String, Object> response = error400Helper.createDatatable(datatabelRequestJsonString, "");
        assertEquals("validation.msg.validation.errors.exist", ((Map) response).get("userMessageGlobalisationCode"));

        // creating client with datatables
        final Integer clientID = ClientHelper.createClientAsPerson(requestSpec, responseSpec);
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled();
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        // creating new client datatable entry
        final boolean genericResultSet = true;

        final HashMap<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put("itsABoolean", Utils.randomNumberGenerator(1) % 2 == 0);
        datatableEntryMap.put("itsADate", Utils.randomDateGenerator("yyyy-MM-dd"));
        datatableEntryMap.put("itsADatetime", Utils.randomDateTimeGenerator("yyyy-MM-dd"));
        datatableEntryMap.put("itsADecimal", Utils.randomDecimalGenerator(4, 3));
        datatableEntryMap.put(tst_tst_tst + "_cd_itsADropdown", createdCodeValueId);
        datatableEntryMap.put("itsANumber", Utils.randomNumberGenerator(5));
        datatableEntryMap.put("itsAString", Utils.randomStringGenerator("", 8));
        datatableEntryMap.put("itsAText", Utils.randomStringGenerator("", 1000));

        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        String datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("map : {}", datatabelEntryRequestJsonString);

        HashMap<String, Object> datatableEntryResponseFirst = this.datatableHelper.createDatatableEntry(datatableName, loanID,
                genericResultSet, datatabelEntryRequestJsonString);
        HashMap<String, Object> datatableEntryResponseSecond = this.datatableHelper.createDatatableEntry(datatableName, loanID,
                genericResultSet, datatabelEntryRequestJsonString);
        assertNotNull(datatableEntryResponseFirst.get("resourceId"), "ERROR IN CREATING THE ENTITY DATATABLE RECORD");
        assertNotNull(datatableEntryResponseSecond.get("resourceId"), "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        HashMap<String, Object> items = this.datatableHelper.readDatatableEntry(datatableName, loanID, genericResultSet, null, "");
        assertNotNull(items);
        assertEquals(2, ((List) items.get("data")).size());

        assertEquals("id", ((Map) ((List) items.get("columnHeaders")).get(0)).get("columnName"));
        assertEquals(1, ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(0));
        assertEquals("loan_id", ((Map) ((List) items.get("columnHeaders")).get(1)).get("columnName"));
        assertEquals(loanID, ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(1));
        assertEquals("itsABoolean", ((Map) ((List) items.get("columnHeaders")).get(2)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsABoolean"), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(2));
        assertEquals("itsADate", ((Map) ((List) items.get("columnHeaders")).get(3)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsADate"),
                Utils.arrayDateToString((List) ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(3)));
        assertEquals("itsADatetime", ((Map) ((List) items.get("columnHeaders")).get(4)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsADatetime"),
                Utils.arrayDateTimeToString((List) ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(4)));
        assertEquals("itsADecimal", ((Map) ((List) items.get("columnHeaders")).get(5)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsADecimal"), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(5));
        assertEquals(tst_tst_tst + "_cd_itsADropdown", ((Map) ((List) items.get("columnHeaders")).get(6)).get("columnName"));
        assertEquals(datatableEntryMap.get(tst_tst_tst + "_cd_itsADropdown"),
                ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(6));
        assertEquals("itsANumber", ((Map) ((List) items.get("columnHeaders")).get(7)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsANumber"), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(7));
        assertEquals("itsAString", ((Map) ((List) items.get("columnHeaders")).get(8)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsAString"), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(8));
        assertEquals("itsAText", ((Map) ((List) items.get("columnHeaders")).get(9)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsAText"), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(9));

        assertEquals(2, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(0));
        assertEquals(loanID, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(1));
        assertEquals(datatableEntryMap.get("itsABoolean"), ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(2));
        assertEquals(datatableEntryMap.get("itsADate"),
                Utils.arrayDateToString((List) ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(3)));
        assertEquals(datatableEntryMap.get("itsADatetime"),
                Utils.arrayDateTimeToString((List) ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(4)));
        assertEquals(datatableEntryMap.get("itsADecimal"), ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(5));
        assertEquals(datatableEntryMap.get(tst_tst_tst + "_cd_itsADropdown"),
                ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(6));
        assertEquals(datatableEntryMap.get("itsANumber"), ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(7));
        assertEquals(datatableEntryMap.get("itsAString"), ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(8));
        assertEquals(datatableEntryMap.get("itsAText"), ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(9));

        // Read the Datatable entry generated with genericResultSet in false
        List<HashMap<String, Object>> datatableEntryResponseNoGenericResult = this.datatableHelper.readDatatableEntry(datatableName, loanID,
                !genericResultSet, (Integer) datatableEntryResponseFirst.get("resourceId"), "");
        assertNotNull(datatableEntryResponseNoGenericResult, "ERROR IN GETTING THE DATE VALUE FROM DATATABLE RECORD");
        assertEquals(1, datatableEntryResponseNoGenericResult.size());

        assertEquals(loanID, datatableEntryResponseNoGenericResult.get(0).get("loan_id"));
        assertEquals(datatableEntryMap.get("itsABoolean"),
                Boolean.valueOf((String) datatableEntryResponseNoGenericResult.get(0).get("itsABoolean")));
        assertEquals(datatableEntryMap.get("itsADate"),
                Utils.arrayDateToString((List) datatableEntryResponseNoGenericResult.get(0).get("itsADate")));
        assertEquals(datatableEntryMap.get("itsADecimal"), datatableEntryResponseNoGenericResult.get(0).get("itsADecimal"));
        assertEquals(datatableEntryMap.get("itsADatetime"),
                Utils.arrayDateTimeToString((List<Integer>) datatableEntryResponseNoGenericResult.get(0).get("itsADatetime")));
        assertEquals(datatableEntryMap.get(tst_tst_tst + "_cd_itsADropdown"),
                datatableEntryResponseNoGenericResult.get(0).get(tst_tst_tst + "_cd_itsADropdown"));
        assertEquals(datatableEntryMap.get("itsANumber"), datatableEntryResponseNoGenericResult.get(0).get("itsANumber"));
        assertEquals(datatableEntryMap.get("itsAString"), datatableEntryResponseNoGenericResult.get(0).get("itsAString"));
        assertEquals(datatableEntryMap.get("itsAText"), datatableEntryResponseNoGenericResult.get(0).get("itsAText"));

        // Update datatable entry

        Boolean previousBoolean = (Boolean) datatableEntryMap.get("itsABoolean");

        datatableEntryMap.put("itsABoolean", null);
        datatableEntryMap.put("itsADate", null);
        datatableEntryMap.put("itsADatetime", null);
        datatableEntryMap.put("itsADecimal", null);
        datatableEntryMap.put(tst_tst_tst + "_cd_itsADropdown", null);
        datatableEntryMap.put("itsANumber", null);
        datatableEntryMap.put("itsAString", null);
        datatableEntryMap.put("itsAText", null);

        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        datatabelEntryRequestJsonString = new GsonBuilder().serializeNulls().create().toJson(datatableEntryMap);
        LOG.info("map : {}", datatabelEntryRequestJsonString);

        PutDataTablesAppTableIdDatatableIdResponse updatedDatatableEntryResponse = this.datatableHelper.updateDatatableEntry(datatableName,
                loanID, 1, datatabelEntryRequestJsonString);
        assertNotNull(updatedDatatableEntryResponse);
        assertEquals(1L, updatedDatatableEntryResponse.getResourceId());
        updatedDatatableEntryResponse = this.datatableHelper.updateDatatableEntry(datatableName, loanID, 2,
                datatabelEntryRequestJsonString);
        assertNotNull(updatedDatatableEntryResponse);
        assertEquals(2L, updatedDatatableEntryResponse.getResourceId());

        assertEquals(Long.valueOf(loanID), updatedDatatableEntryResponse.getLoanId());

        assertEquals(null, updatedDatatableEntryResponse.getChanges().get("itsABoolean"));
        assertEquals(null, updatedDatatableEntryResponse.getChanges().get("itsADate"));
        assertEquals(null, updatedDatatableEntryResponse.getChanges().get("itsADecimal"));
        assertEquals(null, updatedDatatableEntryResponse.getChanges().get("itsADatetime"));
        assertEquals(null, updatedDatatableEntryResponse.getChanges().get(tst_tst_tst + "_cd_itsADropdown"));
        assertEquals(null, updatedDatatableEntryResponse.getChanges().get("itsANumber"));
        assertEquals(null, updatedDatatableEntryResponse.getChanges().get("itsAString"));
        assertEquals(null, updatedDatatableEntryResponse.getChanges().get("itsAText"));

        items = this.datatableHelper.readDatatableEntry(datatableName, loanID, genericResultSet, null, "");
        assertNotNull(items);
        assertEquals(2, ((List) items.get("data")).size());

        assertEquals("loan_id", ((Map) ((List) items.get("columnHeaders")).get(1)).get("columnName"));
        assertEquals(loanID, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(1));
        assertEquals("itsABoolean", ((Map) ((List) items.get("columnHeaders")).get(2)).get("columnName"));
        assertEquals(null, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(2));
        assertEquals("itsADate", ((Map) ((List) items.get("columnHeaders")).get(3)).get("columnName"));
        assertEquals(null, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(3));
        assertEquals("itsADatetime", ((Map) ((List) items.get("columnHeaders")).get(4)).get("columnName"));
        assertEquals(null, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(4));
        assertEquals("itsADecimal", ((Map) ((List) items.get("columnHeaders")).get(5)).get("columnName"));
        assertEquals(null, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(5));
        assertEquals(tst_tst_tst + "_cd_itsADropdown", ((Map) ((List) items.get("columnHeaders")).get(6)).get("columnName"));
        assertEquals(null, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(6));
        assertEquals("itsANumber", ((Map) ((List) items.get("columnHeaders")).get(7)).get("columnName"));
        assertEquals(null, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(7));
        assertEquals("itsAString", ((Map) ((List) items.get("columnHeaders")).get(8)).get("columnName"));
        assertEquals(null, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(8));
        assertEquals("itsAText", ((Map) ((List) items.get("columnHeaders")).get(9)).get("columnName"));
        assertEquals(null, ((List) ((Map) ((List) items.get("data")).get(1)).get("row")).get(9));

        // Read the Datatable entry generated with genericResultSet in false
        datatableEntryResponseNoGenericResult = this.datatableHelper.readDatatableEntry(datatableName, loanID, !genericResultSet,
                (Integer) datatableEntryResponseFirst.get("resourceId"), "");
        assertNotNull(datatableEntryResponseNoGenericResult, "ERROR IN GETTING THE DATE VALUE FROM DATATABLE RECORD");
        assertEquals(1, datatableEntryResponseNoGenericResult.size());

        assertEquals(loanID, datatableEntryResponseNoGenericResult.get(0).get("loan_id"));
        assertEquals(datatableEntryMap.get("itsABoolean"), datatableEntryResponseNoGenericResult.get(0).get("itsABoolean"));
        assertEquals(datatableEntryMap.get("itsADate"), datatableEntryResponseNoGenericResult.get(0).get("itsADate"));
        assertEquals(datatableEntryMap.get("itsADecimal"), datatableEntryResponseNoGenericResult.get(0).get("itsADecimal"));
        assertEquals(datatableEntryMap.get("itsADatetime"), datatableEntryResponseNoGenericResult.get(0).get("itsADatetime"));
        assertEquals(datatableEntryMap.get(tst_tst_tst + "_cd_itsADropdown"),
                datatableEntryResponseNoGenericResult.get(0).get(tst_tst_tst + "_cd_itsADropdown"));
        assertEquals(datatableEntryMap.get("itsANumber"), datatableEntryResponseNoGenericResult.get(0).get("itsANumber"));
        assertEquals(datatableEntryMap.get("itsAString"), datatableEntryResponseNoGenericResult.get(0).get("itsAString"));
        assertEquals(datatableEntryMap.get("itsAText"), datatableEntryResponseNoGenericResult.get(0).get("itsAText"));

        // deleting datatable entries
        Integer appTableId = (Integer) this.datatableHelper.deleteDatatableEntries(datatableName, loanID, "loanId");
        assertEquals(loanID, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName);
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(LP_PRINCIPAL.toString())
                .withLoanTermFrequency(LOAN_TERM_FREQUENCY).withLoanTermFrequencyAsMonths().withNumberOfRepayments(LP_REPAYMENTS)
                .withRepaymentEveryAfter(LP_REPAYMENT_PERIOD).withRepaymentFrequencyTypeAsMonths()
                .withInterestRatePerPeriod(LP_INTEREST_RATE).withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate(EXPECTED_DISBURSAL_DATE)
                .withSubmittedOnDate(LOAN_APPLICATION_SUBMISSION_DATE).withLoanType(INDIVIDUAL_LOAN)
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer createLoanProductWithPeriodicAccrualAccountingEnabled() {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRuleAsNone().withDaysInMonth("30")
                .withDaysInYear("365").build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

}
