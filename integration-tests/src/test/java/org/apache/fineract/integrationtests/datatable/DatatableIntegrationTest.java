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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesAppTableIdResponse;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutDataTablesAppTableIdDatatableIdResponse;
import org.apache.fineract.client.models.PutDataTablesAppTableIdResponse;
import org.apache.fineract.client.models.PutDataTablesRequest;
import org.apache.fineract.client.models.PutDataTablesRequestAddColumns;
import org.apache.fineract.client.models.PutDataTablesRequestChangeColumns;
import org.apache.fineract.client.models.PutDataTablesRequestDropColumns;
import org.apache.fineract.client.models.PutDataTablesResponse;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatatableIntegrationTest extends FeignLoanTestBase {

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
    private FeignDatatableHelper datatableHelper;

    @BeforeAll
    public void setup() {
        this.datatableHelper = new FeignDatatableHelper(fineractClient());
    }

    @Test
    public void validateCreateReadDeleteDatatable() throws ParseException {
        // Fetch / Create tst code
        String tst_tst_tst = "TST_TST_TST".toLowerCase();
        Long createdCodeId = retrieveCodeId(tst_tst_tst);
        Integer createdCodeValueId;
        Integer createdCodeValueIdSecond;
        if (createdCodeId == null) {
            createdCodeId = codeHelper.createCode(tst_tst_tst);

            createdCodeValueId = codeHelper.createCodeValue(createdCodeId, Utils.randomStringGenerator("cv_", 8), 1).intValue();
            createdCodeValueIdSecond = codeHelper.createCodeValue(createdCodeId, Utils.randomStringGenerator("cv_", 8), 2).intValue();
        } else {
            List<GetCodeValuesDataResponse> codeValuesForCode = codeHelper.retrieveAllCodeValues(createdCodeId);
            createdCodeValueId = codeValuesForCode.get(0).getId().intValue();
            createdCodeValueIdSecond = codeValuesForCode.get(1).getId().intValue();
        }

        // creating datatable for client entity
        final PostDataTablesRequest columnMap = new PostDataTablesRequest();
        final List<PostColumnHeaderData> datatableColumnsList = new ArrayList<>();
        columnMap.datatableName(Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_", 5).toLowerCase().toLowerCase());
        columnMap.entitySubType("PERSON");
        columnMap.multiRow(false);
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
        columnMap.columns(datatableColumnsList);

        // try to create datatable without apptable
        columnMap.apptableName(null);
        Map<String, Object> errorResponse = createDatatableExpectingError(columnMap);
        assertEquals("validation.msg.validation.errors.exist", ((Map) errorResponse).get("userMessageGlobalisationCode"));
        List errors = (List) ((Map) errorResponse).get("errors");
        assertEquals(2, errors.size());
        assertEquals("validation.msg.datatable.apptableName.cannot.be.blank", ((Map) errors.get(0)).get("userMessageGlobalisationCode"));
        assertEquals("validation.msg.datatable.apptableName.is.not.one.of.expected.enumerations",
                ((Map) errors.get(1)).get("userMessageGlobalisationCode"));

        // set valid apptable name
        columnMap.apptableName(CLIENT_APP_TABLE_NAME);

        // try to create datatable with invalid column type
        PostColumnHeaderData textColumn = addDatatableColumn(datatableColumnsList, itsAText, "Invalid", true, null, null);
        errorResponse = createDatatableExpectingError(columnMap);
        assertEquals("validation.msg.validation.errors.exist", ((Map) errorResponse).get("userMessageGlobalisationCode"));
        errors = (List) ((Map) errorResponse).get("errors");
        assertEquals(1, errors.size());
        Map error = (Map) errors.get(0);
        assertEquals("validation.msg.datatable.type.is.not.one.of.expected.enumerations", error.get("userMessageGlobalisationCode"));
        assertTrue(((String) error.get("defaultUserMessage"))
                .contains("string, number, boolean, decimal, date, datetime, text, json, dropdown"));

        // set valid type
        textColumn.type("Text");
        // add json type
        addDatatableColumn(datatableColumnsList, itsAJson, "Json", false, null, null);

        LOG.info("map : {}", columnMap);

        String datatableName = createDatatable(columnMap);
        verifyDatatableCreatedOnServer(datatableName);

        // try to create with the same name
        errorResponse = createDatatableExpectingError(columnMap);
        assertEquals("validation.msg.validation.errors.exist", ((Map) errorResponse).get("userMessageGlobalisationCode"));

        // creating client with datatables
        final Integer clientID = createClient().intValue();

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
        errorResponse = createDatatableEntryExpectingError(datatableName, clientID, datatabelEntryRequestJsonString);

        // add valid json
        datatableEntryMap.put(itsAJson, json);

        datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("map : {}", datatabelEntryRequestJsonString);

        Long datatableEntryResourceId = createDatatableEntry(datatableName, clientID, datatabelEntryRequestJsonString);
        assertNotNull(datatableEntryResourceId, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        final Map<String, Object> items = readDatatableEntry(datatableName, clientID, genericResultSet, datatableEntryResourceId);
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
        List<Map<String, Object>> datatableEntryResponseNoGenericResult = readDatatableEntry(datatableName, clientID, !genericResultSet,
                datatableEntryResourceId);
        assertNotNull(datatableEntryResponseNoGenericResult, "ERROR IN GETTING THE DATE VALUE FROM DATATABLE RECORD");
        assertEquals(1, datatableEntryResponseNoGenericResult.size());

        Map<String, Object> responseMap = datatableEntryResponseNoGenericResult.get(0);
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

        Map<String, Object> updatedDatatableEntryResponse = updateDatatableEntry(datatableName, clientID, datatabelEntryRequestJsonString);

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
            String rows = ok(() -> fineractClient().dataTables().queryValues(datatableName, column, valueFilter, column));
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
        Integer appTableId = deleteDatatableEntries(datatableName, clientID);
        assertEquals(clientID, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName).getResourceIdentifier();
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");

        GetDataTablesResponse dataTable = datatableHelper.getDatatable(datatableName);
        assertNull(dataTable);
    }

    @Test
    public void validateCreateReadDeleteDatatableWithCaseSensitive() throws ParseException {
        // creating datatable for client entity
        final PostDataTablesRequest columnMap = new PostDataTablesRequest();
        final List<PostColumnHeaderData> datatableColumnsList = new ArrayList<>();
        columnMap.datatableName(Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_", 5));
        columnMap.apptableName(CLIENT_APP_TABLE_NAME);
        columnMap.entitySubType("PERSON");
        columnMap.multiRow(false);
        String itsADate = "itsADate";
        String itsADecimal = "itsADecimal";
        String itsAString = "itsAString";
        String dateFormat = "dateFormat";

        addDatatableColumn(datatableColumnsList, itsADate, "Date", true, null, null);
        addDatatableColumn(datatableColumnsList, itsADecimal, "Decimal", true, null, null);
        addDatatableColumn(datatableColumnsList, itsAString, "String", true, 10, null);
        columnMap.columns(datatableColumnsList);
        LOG.info("map : {}", columnMap);

        String datatableName = createDatatable(columnMap);
        verifyDatatableCreatedOnServer(datatableName);

        // creating client with datatables
        final Integer clientID = createClient().intValue();

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

        Long datatableEntryResourceId = createDatatableEntry(datatableName, clientID, datatabelEntryRequestJsonString);
        assertNotNull(datatableEntryResourceId, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        final Map<String, Object> items = readDatatableEntry(datatableName, clientID, genericResultSet, datatableEntryResourceId);
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

        Map<String, Object> updatedDatatableEntryResponse = updateDatatableEntry(datatableName, clientID, datatabelEntryRequestJsonString);

        assertEquals(clientID, updatedDatatableEntryResponse.get("clientId"));

        assertEquals(datatableEntryMap.get(itsADate),
                Utils.arrayDateToString((List) ((Map) updatedDatatableEntryResponse.get("changes")).get(itsADate)));
        assertEquals(datatableEntryMap.get(itsADecimal), ((Map) updatedDatatableEntryResponse.get("changes")).get(itsADecimal));
        assertEquals(datatableEntryMap.get(itsAString), ((Map) updatedDatatableEntryResponse.get("changes")).get(itsAString));

        // Read the datatable with a query
        LOG.info("query in {} for value : {}", itsAString, randomValue);
        final String queryResult = ok(
                () -> fineractClient().dataTables().queryValues(datatableName, itsAString, randomValue, "client_id,itsADecimal"));
        assertNotNull(queryResult);
        LOG.info("query result : {}", queryResult);

        // deleting datatable entries
        Integer appTableId = deleteDatatableEntries(datatableName, clientID);
        assertEquals(clientID, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName).getResourceIdentifier();
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateInsertNullValues() {
        // Fetch / Create TST code
        retrieveCodeId("TST_TST_TST");

        // creating datatable for client entity
        final PostDataTablesRequest columnMap = new PostDataTablesRequest();
        final List<PostColumnHeaderData> datatableColumnsList = new ArrayList<>();
        columnMap.datatableName(Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5));
        columnMap.apptableName(LOAN_APP_TABLE_NAME);
        columnMap.entitySubType("");
        columnMap.multiRow(true);
        addDatatableColumn(datatableColumnsList, "itsABoolean", "Boolean", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADate", "Date", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADatetime", "Datetime", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADecimal", "Decimal", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADropdown", "Dropdown", false, null, "TST_TST_TST");
        addDatatableColumn(datatableColumnsList, "itsANumber", "Number", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsAString", "String", false, 10, null);
        addDatatableColumn(datatableColumnsList, "itsAText", "Text", false, null, null);
        columnMap.columns(datatableColumnsList);
        LOG.info("map : {}", columnMap);

        String datatableName = createDatatable(columnMap);
        verifyDatatableCreatedOnServer(datatableName);

        // try to create with the same name
        Map<String, Object> response = createDatatableExpectingError(columnMap);
        assertEquals("validation.msg.validation.errors.exist", ((Map) response).get("userMessageGlobalisationCode"));

        // creating client with datatables
        final Integer clientID = createClient().intValue();
        final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled();
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

        Long firstEntryResponse = createDatatableEntry(datatableName, loanID, firstEntryRequestJsonString);
        assertNotNull(firstEntryResponse, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

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
        Long secondEntryResponse = createDatatableEntry(datatableName, loanID, secondEntryRequestJsonString);
        assertNotNull(secondEntryResponse, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        Map<String, Object> items = readDatatableEntry(datatableName, loanID, genericResultSet, null);
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

        PutDataTablesAppTableIdDatatableIdResponse updatedDatatableEntryResponse = updateDatatableEntry(datatableName, loanID, 1L,
                secondEntryRequestJsonString);
        assertNotNull(updatedDatatableEntryResponse);
        assertEquals(0, updatedDatatableEntryResponse.getChanges().size());
    }

    @Test
    public void validateCreateAndEditDatatable() {
        // Creating client
        final Integer clientId = createClient().intValue();
        final Integer randomNumber = Utils.randomNumberGenerator(3);

        // Creating datatable for Client Person
        final String datatableName = Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_", 5);
        final boolean genericResultSet = true;

        PostDataTablesRequest columnMap = new PostDataTablesRequest();
        List<PostColumnHeaderData> datatableColumnsList = new ArrayList<>();
        columnMap.datatableName(datatableName);
        columnMap.apptableName(CLIENT_APP_TABLE_NAME);
        columnMap.entitySubType(CLIENT_PERSON_SUBTYPE_NAME);
        columnMap.multiRow(false);
        addDatatableColumn(datatableColumnsList, "itsANumber", "Number", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsAString", "String", false, 10, null);
        columnMap.columns(datatableColumnsList);
        LOG.info("map : {}", columnMap);

        PostDataTablesResponse datatableCreateResponse = this.datatableHelper.createDatatable(columnMap);
        assertEquals(datatableName, datatableCreateResponse.getResourceIdentifier());
        verifyDatatableCreatedOnServer(datatableName);

        // Insert first values
        final String randomString = Utils.randomStringGenerator("Q", 8);
        HashMap<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put("itsANumber", randomNumber);
        datatableEntryMap.put("itsAString", randomString);

        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        String datatableEntryRequestJsonString = new GsonBuilder().serializeNulls().create().toJson(datatableEntryMap);
        PostDataTablesAppTableIdResponse datatableEntryResponse = this.datatableHelper.createDatatableEntry(datatableName,
                clientId.longValue(), datatableEntryRequestJsonString);
        assertNotNull(datatableEntryResponse.getResourceId(), "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        Map<String, Object> items = readDatatableEntry(datatableName, clientId, genericResultSet, null);
        assertNotNull(items);
        List data = (List) items.get("data");
        assertEquals(1, data.size());
        List records = (List) ((Map) data.get(0)).get("row");
        LOG.info("Record created at {}", records.get(3));
        LOG.info("Record updated at {}", records.get(4));

        assertEquals(clientId, records.get(0));
        assertEquals(randomString, records.get(2));

        // Update DataTable
        PutDataTablesRequest updateRequest = new PutDataTablesRequest().apptableName(CLIENT_APP_TABLE_NAME)
                .entitySubType(CLIENT_PERSON_SUBTYPE_NAME)
                .addColumns(List.of(new PutDataTablesRequestAddColumns().name("itsAText").type("Text").mandatory(false)));
        LOG.info("map to update : {}", updateRequest);
        PutDataTablesResponse datatableUpdateResponse = this.datatableHelper.updateDatatable(datatableName, updateRequest);
        assertNotNull(datatableUpdateResponse);
        assertEquals(datatableName, datatableUpdateResponse.getResourceIdentifier());

        // Update DataTable Entry after Update DataTable schema
        datatableEntryMap = new HashMap<>();
        final String textValue = Utils.randomStringGenerator(randomString, 120);
        datatableEntryMap.put("itsAText", textValue);
        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        final String datatableEntryUpdateJsonString = new GsonBuilder().serializeNulls().create().toJson(datatableEntryMap);
        LOG.info("map to update : {}", datatableEntryUpdateJsonString);
        PutDataTablesAppTableIdResponse updatedDatatableEntryResponse = ok(() -> fineractClient().dataTables()
                .updateDatatableEntryOnetoOne(datatableName, clientId.longValue(), datatableEntryUpdateJsonString));
        assertNotNull(updatedDatatableEntryResponse);
        assertEquals(1, updatedDatatableEntryResponse.getChanges().size());

        // Read the Datatable entry generated with genericResultSet in true (default)
        items = readDatatableEntry(datatableName, clientId, genericResultSet, null);
        assertNotNull(items);
        data = (List) items.get("data");
        assertEquals(1, data.size());

        records = (List) ((Map) data.get(0)).get("row");
        LOG.info("Record created at {}", records.get(3));
        LOG.info("Record updated at {}", records.get(4));

        assertEquals(clientId, records.get(0));
        assertEquals(randomString, records.get(2));
        assertEquals(textValue, records.get(5));

        Integer resourceId = deleteDatatableEntries(datatableName, clientId);
        assertEquals(clientId, resourceId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // Update - update, delete DataTable columns
        updateRequest = new PutDataTablesRequest().apptableName(CLIENT_APP_TABLE_NAME).entitySubType(CLIENT_PERSON_SUBTYPE_NAME)
                .dropColumns(List.of(new PutDataTablesRequestDropColumns().name("itsANumber")))
                .changeColumns(List.of(new PutDataTablesRequestChangeColumns().name("itsAString").mandatory(false).length(100L)));
        LOG.info("map to update : {}", updateRequest);
        datatableUpdateResponse = this.datatableHelper.updateDatatable(datatableName, updateRequest);
        assertNotNull(datatableUpdateResponse);
        assertEquals(datatableName, datatableUpdateResponse.getResourceIdentifier());

        GetDataTablesResponse dataTable = datatableHelper.getDatatable(datatableName);
        assertEquals(CLIENT_PERSON_SUBTYPE_NAME, dataTable.getEntitySubType());
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
        Long createdCodeId = retrieveCodeId(tst_tst_tst);
        Integer createdCodeValueId;
        Integer createdCodeValueIdSecond;
        if (createdCodeId == null) {
            createdCodeId = codeHelper.createCode(tst_tst_tst);

            createdCodeValueId = codeHelper.createCodeValue(createdCodeId, Utils.randomStringGenerator("cv_", 8), 1).intValue();
            createdCodeValueIdSecond = codeHelper.createCodeValue(createdCodeId, Utils.randomStringGenerator("cv_", 8), 2).intValue();
        } else {
            List<GetCodeValuesDataResponse> codeValuesForCode = codeHelper.retrieveAllCodeValues(createdCodeId);
            createdCodeValueId = codeValuesForCode.get(0).getId().intValue();
            createdCodeValueIdSecond = codeValuesForCode.get(1).getId().intValue();
        }

        // creating datatable for client entity
        final PostDataTablesRequest columnMap = new PostDataTablesRequest();
        final List<PostColumnHeaderData> datatableColumnsList = new ArrayList<>();
        columnMap.datatableName(Utils.uniqueRandomStringGenerator(LOAN_APP_TABLE_NAME + "_", 5));
        columnMap.apptableName(LOAN_APP_TABLE_NAME);
        columnMap.entitySubType("");
        columnMap.multiRow(true);
        addDatatableColumn(datatableColumnsList, "itsABoolean", "Boolean", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADate", "Date", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADatetime", "Datetime", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADecimal", "Decimal", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsADropdown", "Dropdown", false, null, tst_tst_tst);
        addDatatableColumn(datatableColumnsList, "itsANumber", "Number", false, null, null);
        addDatatableColumn(datatableColumnsList, "itsAString", "String", false, 10, null);
        addDatatableColumn(datatableColumnsList, "itsAText", "Text", false, null, null);
        columnMap.columns(datatableColumnsList);
        LOG.info("map : {}", columnMap);

        String datatableName = createDatatable(columnMap);
        verifyDatatableCreatedOnServer(datatableName);

        // try to create with the same name
        Map<String, Object> response = createDatatableExpectingError(columnMap);
        assertEquals("validation.msg.validation.errors.exist", ((Map) response).get("userMessageGlobalisationCode"));

        // creating client with datatables
        final Integer clientID = createClient().intValue();
        final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled();
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

        Long datatableEntryResponseFirst = createDatatableEntry(datatableName, loanID, datatabelEntryRequestJsonString);
        Long datatableEntryResponseSecond = createDatatableEntry(datatableName, loanID, datatabelEntryRequestJsonString);
        assertNotNull(datatableEntryResponseFirst, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");
        assertNotNull(datatableEntryResponseSecond, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        Map<String, Object> items = readDatatableEntry(datatableName, loanID, genericResultSet, null);
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
        List<Map<String, Object>> datatableEntryResponseNoGenericResult = readDatatableEntry(datatableName, loanID, !genericResultSet,
                datatableEntryResponseFirst);
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

        PutDataTablesAppTableIdDatatableIdResponse updatedDatatableEntryResponse = updateDatatableEntry(datatableName, loanID, 1L,
                datatabelEntryRequestJsonString);
        assertNotNull(updatedDatatableEntryResponse);
        assertEquals(1L, updatedDatatableEntryResponse.getResourceId());
        updatedDatatableEntryResponse = updateDatatableEntry(datatableName, loanID, 2L, datatabelEntryRequestJsonString);
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

        items = readDatatableEntry(datatableName, loanID, genericResultSet, null);
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
        datatableEntryResponseNoGenericResult = readDatatableEntry(datatableName, loanID, !genericResultSet, datatableEntryResponseFirst);
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
        Integer appTableId = deleteDatatableEntries(datatableName, loanID);
        assertEquals(loanID, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName).getResourceIdentifier();
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void testDropNullColumnWithData() {
        // Create datatable for client entity
        final PostDataTablesRequest columnMap = new PostDataTablesRequest();
        final List<PostColumnHeaderData> datatableColumnsList = new ArrayList<>();
        columnMap.datatableName(Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_", 5));
        columnMap.apptableName(CLIENT_APP_TABLE_NAME);
        columnMap.entitySubType(CLIENT_PERSON_SUBTYPE_NAME);
        columnMap.multiRow(false);

        // Add columns: one that will have data and one that will be NULL
        addDatatableColumn(datatableColumnsList, "columnWithData", "String", false, 50, null);
        addDatatableColumn(datatableColumnsList, "columnWithNull", "String", false, 50, null);
        columnMap.columns(datatableColumnsList);

        LOG.info("Creating datatable: {}", columnMap);

        String datatableName = createDatatable(columnMap);
        assertNotNull(datatableName);
        verifyDatatableCreatedOnServer(datatableName);

        // Create a client
        final Integer clientId = createClient().intValue();

        // Create a datatable entry with data in one column and NULL in the other
        final HashMap<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put("columnWithData", "TestValue");
        // columnWithNull is intentionally not set, so it will be NULL
        datatableEntryMap.put("locale", "en");

        String datatableEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("Creating datatable entry: {}", datatableEntryRequestJsonString);

        final boolean genericResultSet = true;
        Long datatableEntryResponse = createDatatableEntry(datatableName, clientId, datatableEntryRequestJsonString);
        assertNotNull(datatableEntryResponse, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");
        assertEquals(clientId.longValue(), datatableEntryResponse);

        // Verify column count before drop
        GetDataTablesResponse dataTableBeforeDrop = datatableHelper.getDatatable(datatableName);
        List<ResultsetColumnHeaderData> columnHeadersBeforeDrop = dataTableBeforeDrop.getColumnHeaderData();
        // Should have 5 columns before drop: client_id, columnWithData, columnWithNull, created_at, updated_at
        // Note: Datatables automatically add audit columns (created_at, updated_at)
        assertEquals(5, columnHeadersBeforeDrop.size(), "Should have 5 columns before dropping columnWithNull");

        // Now try to drop the NULL column - this should succeed with the fix
        PutDataTablesRequest updateRequest = new PutDataTablesRequest().apptableName(CLIENT_APP_TABLE_NAME)
                .entitySubType(CLIENT_PERSON_SUBTYPE_NAME)
                .dropColumns(List.of(new PutDataTablesRequestDropColumns().name("columnWithNull")));
        LOG.info("Dropping NULL column: {}", updateRequest);

        PutDataTablesResponse updateResponse = this.datatableHelper.updateDatatable(datatableName, updateRequest);
        assertNotNull(updateResponse);
        assertEquals(datatableName, updateResponse.getResourceIdentifier());

        // Verify the column was dropped
        GetDataTablesResponse dataTable = datatableHelper.getDatatable(datatableName);
        List<ResultsetColumnHeaderData> columnHeaders = dataTable.getColumnHeaderData();
        // Should have 4 columns after drop: client_id, columnWithData, created_at, updated_at (columnWithNull should be
        // dropped)
        assertEquals(4, columnHeaders.size(), "Should have 4 columns after dropping columnWithNull");
        boolean hasColumnWithData = false;
        boolean hasColumnWithNull = false;
        for (ResultsetColumnHeaderData header : columnHeaders) {
            if ("columnWithData".equals(header.getColumnName())) {
                hasColumnWithData = true;
            }
            if ("columnWithNull".equals(header.getColumnName())) {
                hasColumnWithNull = true;
            }
        }
        assertTrue(hasColumnWithData, "columnWithData should still exist");
        assertFalse(hasColumnWithNull, "columnWithNull should have been dropped");

        // Clean up
        deleteDatatableEntries(datatableName, clientId);
        this.datatableHelper.deleteDatatable(datatableName);
    }

    @Test
    public void validateOrderParameterOnDatatableEntryRead() {
        // given: a single-row client datatable with a column name that requires quoting
        final List<PostColumnHeaderData> datatableColumnsList = new ArrayList<>();
        String plainColumn = "plaincolumn";
        String spacedColumn = "Spaced Column";
        addDatatableColumn(datatableColumnsList, plainColumn, "Number", false, null, null);
        addDatatableColumn(datatableColumnsList, spacedColumn, "String", false, 20, null);

        final PostDataTablesRequest columnMap = new PostDataTablesRequest();
        String datatableName = Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_", 5).toLowerCase();
        columnMap.datatableName(datatableName);
        columnMap.apptableName(CLIENT_APP_TABLE_NAME);
        columnMap.entitySubType(CLIENT_PERSON_SUBTYPE_NAME);
        columnMap.multiRow(false);
        columnMap.columns(datatableColumnsList);

        String assignedDatatableName = createDatatable(columnMap);
        assertEquals(datatableName, assignedDatatableName);

        final Integer clientID = createClient().intValue();

        final HashMap<String, Object> entryMap = new HashMap<>();
        entryMap.put(plainColumn, Utils.randomNumberGenerator(3));
        entryMap.put(spacedColumn, Utils.randomStringGenerator("", 8));
        entryMap.put("locale", "en");
        createDatatableEntry(datatableName, clientID, new Gson().toJson(entryMap));

        // valid: bare column, no direction
        Map<String, Object> result = readDatatableEntryWithOrder(datatableName, clientID, plainColumn);
        assertNotNull(result);

        // valid: bare column with direction
        result = readDatatableEntryWithOrder(datatableName, clientID, plainColumn + " DESC");
        assertNotNull(result);

        // valid: double-quoted column with a space
        result = readDatatableEntryWithOrder(datatableName, clientID, "\"" + spacedColumn + "\"");
        assertNotNull(result);

        // valid: backtick-quoted column with a space, plus direction
        result = readDatatableEntryWithOrder(datatableName, clientID, "`" + spacedColumn + "` ASC");
        assertNotNull(result);

        // invalid: unknown column -- expect 403
        assertEquals(403, fail(() -> readDatatableEntryWithOrder(datatableName, clientID, "not_a_real_column")).getStatus());

        // invalid: classic SQL injection payload -- expect 403
        assertEquals(403,
                fail(() -> readDatatableEntryWithOrder(datatableName, clientID, plainColumn + "; DROP TABLE m_client;--")).getStatus());

        // invalid: unquoted column name containing a space -- expect 403 (quoting is required)
        assertEquals(403, fail(() -> readDatatableEntryWithOrder(datatableName, clientID, spacedColumn)).getStatus());

        // cleanup
        deleteDatatableEntries(datatableName, clientID);
        this.datatableHelper.deleteDatatable(datatableName);
    }

    @Test
    public void validateOrderParameterOnDatatableManyEntryRead() {
        // given: a multi-row client datatable so we can obtain a datatableId to query against
        final List<PostColumnHeaderData> datatableColumnsList = new ArrayList<>();
        String plainColumn = "plaincolumn";
        String spacedColumn = "Spaced Column";
        addDatatableColumn(datatableColumnsList, plainColumn, "Number", false, null, null);
        addDatatableColumn(datatableColumnsList, spacedColumn, "String", false, 20, null);

        final PostDataTablesRequest columnMap = new PostDataTablesRequest();
        String datatableName = Utils.uniqueRandomStringGenerator(CLIENT_APP_TABLE_NAME + "_", 5).toLowerCase();
        columnMap.datatableName(datatableName);
        columnMap.apptableName(CLIENT_APP_TABLE_NAME);
        columnMap.entitySubType(CLIENT_PERSON_SUBTYPE_NAME);
        columnMap.multiRow(true);
        columnMap.columns(datatableColumnsList);

        assertEquals(datatableName, createDatatable(columnMap));

        final Integer clientID = createClient().intValue();

        final HashMap<String, Object> entryMap = new HashMap<>();
        entryMap.put(plainColumn, Utils.randomNumberGenerator(3));
        entryMap.put(spacedColumn, Utils.randomStringGenerator("", 8));
        entryMap.put("locale", "en");
        PostDataTablesAppTableIdResponse entryResponse = this.datatableHelper.createDatatableEntry(datatableName, clientID.longValue(),
                new Gson().toJson(entryMap));
        Long datatableId = entryResponse.getResourceId();
        assertNotNull(datatableId);

        // valid: bare column, no direction
        Map<String, Object> result = readDatatableManyEntryWithOrder(datatableName, clientID, datatableId, plainColumn);
        assertNotNull(result);

        // valid: bare column with direction
        result = readDatatableManyEntryWithOrder(datatableName, clientID, datatableId, plainColumn + " DESC");
        assertNotNull(result);

        // valid: double-quoted column with a space
        result = readDatatableManyEntryWithOrder(datatableName, clientID, datatableId, "\"" + spacedColumn + "\"");
        assertNotNull(result);

        // invalid: unknown column -- expect 403
        assertEquals(403,
                fail(() -> readDatatableManyEntryWithOrder(datatableName, clientID, datatableId, "not_a_real_column")).getStatus());

        // invalid: classic SQL injection payload -- expect 403
        assertEquals(403,
                fail(() -> readDatatableManyEntryWithOrder(datatableName, clientID, datatableId, plainColumn + "; DROP TABLE m_client;--"))
                        .getStatus());

        // invalid: subquery-based injection (the exact class of payload from the security report) -- expect 403
        assertEquals(403, fail(() -> readDatatableManyEntryWithOrder(datatableName, clientID, datatableId, "(SELECT 1 FROM pg_sleep(3))"))
                .getStatus());

        // cleanup
        deleteDatatableEntries(datatableName, clientID);
        this.datatableHelper.deleteDatatable(datatableName);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Long loanProductID) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return loanHelper
                .applyForLoan(new PostLoansRequest().clientId(clientID.longValue()).productId(loanProductID)
                        .principal(new BigDecimal(LP_PRINCIPAL.toString())).loanTermFrequency(Integer.valueOf(LOAN_TERM_FREQUENCY))
                        .loanTermFrequencyType(2).numberOfRepayments(Integer.valueOf(LP_REPAYMENTS))
                        .repaymentEvery(Integer.valueOf(LP_REPAYMENT_PERIOD)).repaymentFrequencyType(2)
                        .interestRatePerPeriod(new BigDecimal(LP_INTEREST_RATE)).interestType(1).amortizationType(0)
                        .interestCalculationPeriodType(1).expectedDisbursementDate(EXPECTED_DISBURSAL_DATE)
                        .submittedOnDate(LOAN_APPLICATION_SUBMISSION_DATE).loanType(INDIVIDUAL_LOAN)
                        .transactionProcessingStrategyCode(LoanApplicationTestBuilder.DEFAULT_STRATEGY)
                        .maxOutstandingLoanBalance(new BigDecimal("36000")).dateFormat("dd MMMM yyyy").locale("en_GB"))
                .getLoanId().intValue();
    }

    private Long createLoanProductWithPeriodicAccrualAccountingEnabled() {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRuleAsNone().withDaysInMonth("30")
                .withDaysInYear("365").buildRequest());
    }

    private static PostColumnHeaderData addDatatableColumn(List<PostColumnHeaderData> columns, String name, String type, boolean mandatory,
            Integer length, String code) {
        PostColumnHeaderData column = new PostColumnHeaderData().name(name).type(type).mandatory(mandatory)
                .length(length == null ? null : length.longValue()).code(code);
        columns.add(column);
        return column;
    }

    private Long retrieveCodeId(String codeName) {
        return ok(() -> fineractClient().codes().retrieveAllCodes()).stream().filter(code -> codeName.equals(code.getName())).findFirst()
                .map(code -> code.getId()).orElse(null);
    }

    private String createDatatable(PostDataTablesRequest request) {
        return this.datatableHelper.createDatatable(request).getResourceIdentifier();
    }

    private Map<String, Object> createDatatableExpectingError(PostDataTablesRequest request) {
        CallFailedRuntimeException exception = fail(() -> fineractClient().dataTables().createDatatable(request));
        assertEquals(400, exception.getStatus());
        return parse(exception.getResponseBody());
    }

    private void verifyDatatableCreatedOnServer(String datatableName) {
        assertEquals(datatableName, this.datatableHelper.getDatatable(datatableName).getRegisteredTableName(),
                "ERROR IN CREATING THE DATATABLE");
    }

    private Long createDatatableEntry(String datatableName, Integer apptableId, String json) {
        return this.datatableHelper.createDatatableEntry(datatableName, apptableId.longValue(), json).getResourceId();
    }

    private Map<String, Object> createDatatableEntryExpectingError(String datatableName, Integer apptableId, String json) {
        CallFailedRuntimeException exception = fail(
                () -> fineractClient().dataTables().createDatatableEntry(datatableName, apptableId.longValue(), json));
        assertEquals(403, exception.getStatus());
        return parse(exception.getResponseBody());
    }

    /** Without a datatable id this reads every entry of the application-table row, with one it reads that entry. */
    private <T> T readDatatableEntry(String datatableName, Integer apptableId, boolean genericResultSet, Long datatableId) {
        String json = datatableId == null
                ? ok(() -> fineractClient().dataTables().getDatatableEntries(datatableName, apptableId.longValue(),
                        Map.<String, Object>of("genericResultSet", genericResultSet)))
                : ok(() -> fineractClient().dataTables().getDatatableManyEntry(datatableName, apptableId.longValue(), datatableId, null,
                        genericResultSet));
        return parse(json);
    }

    private Map<String, Object> readDatatableEntryWithOrder(String datatableName, Integer apptableId, String order) {
        return parse(fineractClient().dataTables().getDatatableEntries(datatableName, apptableId.longValue(),
                Map.<String, Object>of("genericResultSet", true, "order", order)));
    }

    private Map<String, Object> readDatatableManyEntryWithOrder(String datatableName, Integer apptableId, Long datatableId, String order) {
        return parse(fineractClient().dataTables().getDatatableManyEntry(datatableName, apptableId.longValue(), datatableId, order, true));
    }

    private Map<String, Object> updateDatatableEntry(String datatableName, Integer apptableId, String json) {
        PutDataTablesAppTableIdResponse response = ok(
                () -> fineractClient().dataTables().updateDatatableEntryOnetoOne(datatableName, apptableId.longValue(), json));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("clientId", response.getClientId() == null ? null : response.getClientId().intValue());
        result.put("changes", normalize(response.getChanges()));
        return result;
    }

    private PutDataTablesAppTableIdDatatableIdResponse updateDatatableEntry(String datatableName, Integer apptableId, Long datatableId,
            String json) {
        return ok(() -> fineractClient().dataTables().updateDatatableEntryOneToMany(datatableName, apptableId.longValue(), datatableId,
                json));
    }

    private Integer deleteDatatableEntries(String datatableName, Integer apptableId) {
        return this.datatableHelper.deleteDatatableEntries(datatableName, apptableId.longValue()).getResourceId().intValue();
    }

    /**
     * Reads a datatable response the way the assertions below compare it: whole numbers as {@code Integer} and decimals
     * as {@code Float}, the types the entry values are generated with.
     */
    @SuppressWarnings("unchecked")
    private static <T> T parse(String json) {
        try {
            return (T) normalize(ObjectMapperFactory.getShared().readValue(json, Object.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Response is not JSON: " + json, e);
        }
    }

    private static Object normalize(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> normalized = new LinkedHashMap<>();
            map.forEach((key, entry) -> normalized.put(key, normalize(entry)));
            return normalized;
        }
        if (value instanceof List<?> list) {
            List<Object> normalized = new ArrayList<>();
            list.forEach(entry -> normalized.add(normalize(entry)));
            return normalized;
        }
        if (value instanceof Long || value instanceof BigInteger) {
            long number = ((Number) value).longValue();
            if (number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE) {
                return (int) number;
            }
            return number;
        }
        if (value instanceof Double || value instanceof BigDecimal) {
            return ((Number) value).floatValue();
        }
        return value;
    }
}
