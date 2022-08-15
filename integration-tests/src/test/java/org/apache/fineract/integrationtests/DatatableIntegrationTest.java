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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatatableIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(DatatableIntegrationTest.class);

    private static final String CLIENT_APP_TABLE_NAME = "m_client";
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
    public void validateCreateReadDeleteDatatable() {
        // Fetch / Create tst code
        HashMap<String, Object> codeResponse = CodeHelper.getCodeByName(this.requestSpec, this.responseSpec, "TST_TST_TST");

        Integer createdCodeId = (Integer) codeResponse.get("id");
        Integer createdCodeValueId;
        Integer createdCodeValueIdSecond;
        if (createdCodeId == null) {
            createdCodeId = (Integer) CodeHelper.createCode(this.requestSpec, this.responseSpec, "TST_TST_TST", "resourceId");

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
        columnMap.put("datatableName", Utils.randomNameGenerator(CLIENT_APP_TABLE_NAME + "_", 5));
        columnMap.put("apptableName", CLIENT_APP_TABLE_NAME);
        columnMap.put("entitySubType", "PERSON");
        columnMap.put("multiRow", false);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsABoolean", "Boolean", false, null, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsADate", "Date", true, null, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsADatetime", "Datetime", true, null, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsADecimal", "Decimal", true, null, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsADropdown", "Dropdown", false, null, "TST_TST_TST");
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsANumber", "Number", true, null, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsAString", "String", true, 10, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsAText", "Text", true, null, null);
        columnMap.put("columns", datatableColumnsList);
        String datatabelRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("map : {}", datatabelRequestJsonString);

        HashMap<String, Object> datatableResponse = this.datatableHelper.createDatatable(datatabelRequestJsonString, "");
        String datatableName = (String) datatableResponse.get("resourceIdentifier");
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, datatableName);

        // try to create with the same name
        ResponseSpecification responseSpecError400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.datatableHelper = new DatatableHelper(this.requestSpec, responseSpecError400);
        HashMap<String, Object> response = this.datatableHelper.createDatatable(datatabelRequestJsonString, "");
        assertEquals("validation.msg.validation.errors.exist", ((Map) response).get("userMessageGlobalisationCode"));
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);

        // creating client with datatables
        final Integer clientID = ClientHelper.createClientAsPerson(requestSpec, responseSpec);

        // creating new client datatable entry
        final boolean genericResultSet = true;

        final HashMap<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put("itsABoolean", Utils.randomNumberGenerator(1) % 2 == 0);
        datatableEntryMap.put("itsADate", Utils.randomDateGenerator("yyyy-MM-dd"));
        datatableEntryMap.put("itsADatetime", Utils.randomDateTimeGenerator("yyyy-MM-dd"));
        datatableEntryMap.put("itsADecimal", Utils.randomDecimalGenerator(4, 3));
        datatableEntryMap.put("TST_TST_TST_cd_itsADropdown", createdCodeValueId);
        datatableEntryMap.put("itsANumber", Utils.randomNumberGenerator(5));
        datatableEntryMap.put("itsAString", Utils.randomStringGenerator("", 8));
        datatableEntryMap.put("itsAText", Utils.randomStringGenerator("", 1000));

        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

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

        assertEquals("itsABoolean", ((Map) ((List) items.get("columnHeaders")).get(1)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsABoolean"), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(1));

        assertEquals("itsADate", ((Map) ((List) items.get("columnHeaders")).get(2)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsADate"),
                Utils.arrayDateToString((List) ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(2)));

        assertEquals("itsADatetime", ((Map) ((List) items.get("columnHeaders")).get(3)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsADatetime"),
                Utils.arrayDateTimeToString((List) ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(3)));

        assertEquals("itsADecimal", ((Map) ((List) items.get("columnHeaders")).get(4)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsADecimal"), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(4));

        assertEquals("TST_TST_TST_cd_itsADropdown", ((Map) ((List) items.get("columnHeaders")).get(5)).get("columnName"));
        assertEquals(datatableEntryMap.get("TST_TST_TST_cd_itsADropdown"),
                ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(5));

        assertEquals("itsANumber", ((Map) ((List) items.get("columnHeaders")).get(6)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsANumber"), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(6));

        assertEquals("itsAString", ((Map) ((List) items.get("columnHeaders")).get(7)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsAString"), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(7));

        assertEquals("itsAText", ((Map) ((List) items.get("columnHeaders")).get(8)).get("columnName"));
        assertEquals(datatableEntryMap.get("itsAText"), ((List) ((Map) ((List) items.get("data")).get(0)).get("row")).get(8));

        // Read the Datatable entry generated with genericResultSet in false
        List<HashMap<String, Object>> datatableEntryResponseNoGenericResult = this.datatableHelper.readDatatableEntry(datatableName,
                clientID, !genericResultSet, (Integer) datatableEntryResponse.get("resourceId"), "");
        assertNotNull(datatableEntryResponseNoGenericResult, "ERROR IN GETTING THE DATE VALUE FROM DATATABLE RECORD");
        assertEquals(1, datatableEntryResponseNoGenericResult.size());

        assertEquals(clientID, datatableEntryResponseNoGenericResult.get(0).get("client_id"));
        assertEquals(datatableEntryMap.get("itsABoolean"),
                Boolean.valueOf((String) datatableEntryResponseNoGenericResult.get(0).get("itsABoolean")));
        assertEquals(datatableEntryMap.get("itsADate"),
                Utils.arrayDateToString((List) datatableEntryResponseNoGenericResult.get(0).get("itsADate")));
        assertEquals(datatableEntryMap.get("itsADecimal"), datatableEntryResponseNoGenericResult.get(0).get("itsADecimal"));
        assertEquals(datatableEntryMap.get("itsADatetime"),
                Utils.arrayDateTimeToString((List<Integer>) datatableEntryResponseNoGenericResult.get(0).get("itsADatetime")));
        assertEquals(datatableEntryMap.get("TST_TST_TST_cd_itsADropdown"),
                datatableEntryResponseNoGenericResult.get(0).get("TST_TST_TST_cd_itsADropdown"));
        assertEquals(datatableEntryMap.get("itsANumber"), datatableEntryResponseNoGenericResult.get(0).get("itsANumber"));
        assertEquals(datatableEntryMap.get("itsAString"), datatableEntryResponseNoGenericResult.get(0).get("itsAString"));
        assertEquals(datatableEntryMap.get("itsAText"), datatableEntryResponseNoGenericResult.get(0).get("itsAText"));

        // Update datatable entry
        Boolean previousBoolean = (Boolean) datatableEntryMap.get("itsABoolean");
        datatableEntryMap.put("itsABoolean", !previousBoolean);
        datatableEntryMap.put("itsADate", Utils.randomDateGenerator("yyyy-MM-dd"));
        datatableEntryMap.put("itsADatetime", Utils.randomDateTimeGenerator("yyyy-MM-dd"));
        datatableEntryMap.put("itsADecimal", Utils.randomDecimalGenerator(4, 3));
        datatableEntryMap.put("TST_TST_TST_cd_itsADropdown", null);
        datatableEntryMap.put("itsANumber", Utils.randomNumberGenerator(5));
        datatableEntryMap.put("itsAString", Utils.randomStringGenerator("", 8));
        datatableEntryMap.put("itsAText", Utils.randomStringGenerator("", 1000));

        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("map : {}", datatabelEntryRequestJsonString);

        HashMap<String, Object> updatedDatatableEntryResponse = this.datatableHelper.updateDatatableEntry(datatableName, clientID, false,
                datatabelEntryRequestJsonString);

        assertEquals(clientID, updatedDatatableEntryResponse.get("clientId"));

        assertEquals(datatableEntryMap.get("itsABoolean"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsABoolean"));
        assertEquals(datatableEntryMap.get("itsADate"),
                Utils.arrayDateToString((List) ((Map) updatedDatatableEntryResponse.get("changes")).get("itsADate")));
        assertEquals(datatableEntryMap.get("itsADecimal"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsADecimal"));
        assertEquals(datatableEntryMap.get("itsADatetime"),
                Utils.arrayDateTimeToString((List<Integer>) ((Map) updatedDatatableEntryResponse.get("changes")).get("itsADatetime")));
        assertEquals(datatableEntryMap.get("TST_TST_TST_cd_itsADropdown"),
                ((Map) updatedDatatableEntryResponse.get("changes")).get("TST_TST_TST_cd_itsADropdown"));
        assertEquals(datatableEntryMap.get("itsANumber"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsANumber"));
        assertEquals(datatableEntryMap.get("itsAString"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsAString"));
        assertEquals(datatableEntryMap.get("itsAText"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsAText"));

        // deleting datatable entries
        Integer appTableId = this.datatableHelper.deleteDatatableEntries(datatableName, clientID, "clientId");
        assertEquals(clientID, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName);
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void validateReadDatatableMultirow() {
        // Fetch / Create tst code
        HashMap<String, Object> codeResponse = CodeHelper.getCodeByName(this.requestSpec, this.responseSpec, "TST_TST_TST");

        Integer createdCodeId = (Integer) codeResponse.get("id");
        Integer createdCodeValueId;
        Integer createdCodeValueIdSecond;
        if (createdCodeId == null) {
            createdCodeId = (Integer) CodeHelper.createCode(this.requestSpec, this.responseSpec, "TST_TST_TST", "resourceId");

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
        columnMap.put("datatableName", Utils.randomNameGenerator(LOAN_APP_TABLE_NAME + "_", 5));
        columnMap.put("apptableName", LOAN_APP_TABLE_NAME);
        columnMap.put("entitySubType", "");
        columnMap.put("multiRow", true);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsABoolean", "Boolean", false, null, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsADate", "Date", true, null, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsADatetime", "Datetime", true, null, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsADecimal", "Decimal", true, null, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsADropdown", "Dropdown", false, null, "TST_TST_TST");
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsANumber", "Number", true, null, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsAString", "String", true, 10, null);
        DatatableHelper.addDatatableColumns(datatableColumnsList, "itsAText", "Text", true, null, null);
        columnMap.put("columns", datatableColumnsList);
        String datatabelRequestJsonString = new Gson().toJson(columnMap);
        LOG.info("map : {}", datatabelRequestJsonString);

        HashMap<String, Object> datatableResponse = this.datatableHelper.createDatatable(datatabelRequestJsonString, "");
        String datatableName = (String) datatableResponse.get("resourceIdentifier");
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, datatableName);

        // try to create with the same name
        ResponseSpecification responseSpecError400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.datatableHelper = new DatatableHelper(this.requestSpec, responseSpecError400);
        HashMap<String, Object> response = this.datatableHelper.createDatatable(datatabelRequestJsonString, "");
        assertEquals("validation.msg.validation.errors.exist", ((Map) response).get("userMessageGlobalisationCode"));
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);

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
        datatableEntryMap.put("TST_TST_TST_cd_itsADropdown", createdCodeValueId);
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
        final HashMap<String, Object> items = this.datatableHelper.readDatatableEntry(datatableName, loanID, genericResultSet, null, "");
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
        assertEquals("TST_TST_TST_cd_itsADropdown", ((Map) ((List) items.get("columnHeaders")).get(6)).get("columnName"));
        assertEquals(datatableEntryMap.get("TST_TST_TST_cd_itsADropdown"),
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
        assertEquals(datatableEntryMap.get("TST_TST_TST_cd_itsADropdown"),
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
        assertEquals(datatableEntryMap.get("TST_TST_TST_cd_itsADropdown"),
                datatableEntryResponseNoGenericResult.get(0).get("TST_TST_TST_cd_itsADropdown"));
        assertEquals(datatableEntryMap.get("itsANumber"), datatableEntryResponseNoGenericResult.get(0).get("itsANumber"));
        assertEquals(datatableEntryMap.get("itsAString"), datatableEntryResponseNoGenericResult.get(0).get("itsAString"));
        assertEquals(datatableEntryMap.get("itsAText"), datatableEntryResponseNoGenericResult.get(0).get("itsAText"));

        // Update datatable entry

        Boolean previousBoolean = (Boolean) datatableEntryMap.get("itsABoolean");

        datatableEntryMap.put("itsABoolean", !previousBoolean);
        datatableEntryMap.put("itsADate", Utils.randomDateGenerator("yyyy-MM-dd"));
        datatableEntryMap.put("itsADatetime", Utils.randomDateTimeGenerator("yyyy-MM-dd"));
        datatableEntryMap.put("itsADecimal", Utils.randomDecimalGenerator(4, 3));
        datatableEntryMap.put("TST_TST_TST_cd_itsADropdown", createdCodeValueIdSecond);
        datatableEntryMap.put("itsANumber", Utils.randomNumberGenerator(5));
        datatableEntryMap.put("itsAString", Utils.randomStringGenerator("", 8));
        datatableEntryMap.put("itsAText", Utils.randomStringGenerator("", 1000));

        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        LOG.info("map : {}", datatabelEntryRequestJsonString);

        HashMap<String, Object> updatedDatatableEntryResponse = this.datatableHelper.updateDatatableEntry(datatableName, loanID, 1, false,
                datatabelEntryRequestJsonString);

        assertEquals(loanID, updatedDatatableEntryResponse.get("loanId"));

        assertEquals(datatableEntryMap.get("itsABoolean"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsABoolean"));
        assertEquals(datatableEntryMap.get("itsADate"),
                Utils.arrayDateToString((List) ((Map) updatedDatatableEntryResponse.get("changes")).get("itsADate")));
        assertEquals(datatableEntryMap.get("itsADecimal"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsADecimal"));
        assertEquals(datatableEntryMap.get("itsADatetime"),
                Utils.arrayDateTimeToString((List<Integer>) ((Map) updatedDatatableEntryResponse.get("changes")).get("itsADatetime")));
        assertEquals(datatableEntryMap.get("TST_TST_TST_cd_itsADropdown"),
                ((Map) updatedDatatableEntryResponse.get("changes")).get("TST_TST_TST_cd_itsADropdown"));
        assertEquals(datatableEntryMap.get("itsANumber"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsANumber"));
        assertEquals(datatableEntryMap.get("itsAString"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsAString"));
        assertEquals(datatableEntryMap.get("itsAText"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsAText"));

        updatedDatatableEntryResponse = this.datatableHelper.updateDatatableEntry(datatableName, loanID, 2, false,
                datatabelEntryRequestJsonString);

        assertEquals(loanID, updatedDatatableEntryResponse.get("loanId"));

        assertEquals(datatableEntryMap.get("itsABoolean"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsABoolean"));
        assertEquals(datatableEntryMap.get("itsADate"),
                Utils.arrayDateToString((List) ((Map) updatedDatatableEntryResponse.get("changes")).get("itsADate")));
        assertEquals(datatableEntryMap.get("itsADecimal"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsADecimal"));
        assertEquals(datatableEntryMap.get("itsADatetime"),
                Utils.arrayDateTimeToString((List<Integer>) ((Map) updatedDatatableEntryResponse.get("changes")).get("itsADatetime")));
        assertEquals(datatableEntryMap.get("TST_TST_TST_cd_itsADropdown"),
                ((Map) updatedDatatableEntryResponse.get("changes")).get("TST_TST_TST_cd_itsADropdown"));
        assertEquals(datatableEntryMap.get("itsANumber"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsANumber"));
        assertEquals(datatableEntryMap.get("itsAString"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsAString"));
        assertEquals(datatableEntryMap.get("itsAText"), ((Map) updatedDatatableEntryResponse.get("changes")).get("itsAText"));

        // deleting datatable entries
        Integer appTableId = this.datatableHelper.deleteDatatableEntries(datatableName, loanID, "loanId");
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
