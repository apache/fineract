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
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.PutDataTablesRequest;
import org.apache.fineract.client.models.PutDataTablesRequestAddColumns;
import org.apache.fineract.client.models.PutDataTablesResponse;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SavingsAccountTransactionDatatableIntegrationTest {

    private static final String SAVINGS_TRANSACTION_APP_TABLE_NAME = EntityTables.SAVINGS_TRANSACTION.getName();
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    final String startDate = "01 Jun 2023";
    final String firstDepositDate = "05 Jun 2023";
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private DatatableHelper datatableHelper;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
    }

    @Test
    public void testDatatableCreateReadUpdateDeleteForSavingsAccountTransaction() {
        // create dataTable
        String datatableName = Utils.uniqueRandomStringGenerator("dt_savings_transaction_", 5).toLowerCase().toLowerCase();
        String column1Name = "aNumber";
        String column2Name = "aString";
        String column3Name = "aBoolean";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(SAVINGS_TRANSACTION_APP_TABLE_NAME);
        request.setMultiRow(false);

        PostColumnHeaderData column1HeaderRequestData = new PostColumnHeaderData();
        column1HeaderRequestData.setName(column1Name);
        column1HeaderRequestData.setType("Number");
        column1HeaderRequestData.setMandatory(false);
        column1HeaderRequestData.setLength(10L);
        column1HeaderRequestData.setCode("");
        column1HeaderRequestData.setUnique(false);
        column1HeaderRequestData.setIndexed(false);

        request.addColumnsItem(column1HeaderRequestData);

        PostColumnHeaderData column2HeaderRequestData = new PostColumnHeaderData();
        column2HeaderRequestData.setName(column2Name);
        column2HeaderRequestData.setType("String");
        column2HeaderRequestData.setMandatory(false);
        column2HeaderRequestData.setLength(10L);
        column2HeaderRequestData.setCode("");
        column2HeaderRequestData.setUnique(false);
        column2HeaderRequestData.setIndexed(false);

        request.addColumnsItem(column2HeaderRequestData);

        PostDataTablesResponse response = datatableHelper.createDatatable(request);
        assertNotNull(response.getResourceIdentifier());

        // update datatable
        PutDataTablesRequest putRequest = new PutDataTablesRequest();
        putRequest.setApptableName(SAVINGS_TRANSACTION_APP_TABLE_NAME);
        PutDataTablesRequestAddColumns column3HeaderPutRequestData = new PutDataTablesRequestAddColumns();
        column3HeaderPutRequestData.setName(column3Name);
        column3HeaderPutRequestData.setType("Boolean");
        column3HeaderPutRequestData.setMandatory(false);

        putRequest.addAddColumnsItem(column3HeaderPutRequestData);

        PutDataTablesResponse updateResponse = datatableHelper.updateDatatable(datatableName, putRequest);
        assertNotNull(updateResponse.getResourceIdentifier());

        // verify Datatable got created
        GetDataTablesResponse dataTable = datatableHelper.getDataTableDetails(datatableName);

        // verfify columns
        List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertNotNull(columnHeaderData);

        // two columns with 1 primary key and 2 audit columns created
        assertEquals(6, columnHeaderData.size());

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName);
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void testDatatableCreateReadUpdateDeleteEntryForSavingsAccountTransaction() {
        // Create Client
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        // Create savings product and account
        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        final Integer transactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", firstDepositDate,
                CommonConstants.RESPONSE_RESOURCE_ID);

        assertNotNull(transactionId);

        // create dataTable
        String datatableName = Utils.uniqueRandomStringGenerator("dt_savings_transaction_", 5).toLowerCase().toLowerCase();
        String column1Name = "aNumber";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(SAVINGS_TRANSACTION_APP_TABLE_NAME);
        request.setMultiRow(true);

        PostColumnHeaderData column1HeaderRequestData = new PostColumnHeaderData();
        column1HeaderRequestData.setName(column1Name);
        column1HeaderRequestData.setType("Number");
        column1HeaderRequestData.setMandatory(false);
        column1HeaderRequestData.setLength(10L);
        column1HeaderRequestData.setCode("");
        column1HeaderRequestData.setUnique(false);
        column1HeaderRequestData.setIndexed(false);

        request.addColumnsItem(column1HeaderRequestData);

        PostDataTablesResponse response = datatableHelper.createDatatable(request);

        assertNotNull(response);

        String createdName = response.getResourceIdentifier();
        assertEquals(datatableName, createdName);

        // add entries
        final HashMap<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put(column1Name, Utils.randomNumberGenerator(5));
        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        String datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);

        final boolean genericResultSet = true;

        HashMap<String, Object> datatableEntryResponseFirst = this.datatableHelper.createDatatableEntry(datatableName, transactionId,
                genericResultSet, datatabelEntryRequestJsonString);

        Integer datatableId = (Integer) datatableEntryResponseFirst.get("resourceId");
        assertNotNull(datatableId);

        // Read the Datatable entry generated with genericResultSet
        HashMap<String, Object> items = this.datatableHelper.readDatatableEntry(datatableName, transactionId, genericResultSet, null, "");
        assertNotNull(items);
        assertEquals(1, ((List) items.get("data")).size());

        // update datatable entry
        datatableEntryMap.put(column1Name, 100);
        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");
        datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);
        HashMap<String, Object> updatedDatatableEntryResponse = this.datatableHelper.updateDatatableEntry(datatableName, transactionId,
                datatableId, false, datatabelEntryRequestJsonString);

        assertEquals(transactionId, Integer.valueOf((String) updatedDatatableEntryResponse.get("transactionId")));
        assertEquals(datatableId, updatedDatatableEntryResponse.get("resourceId"));

        // deleting datatable entries
        String deletedTransactionId = (String) this.datatableHelper.deleteDatatableEntries(datatableName, transactionId, "transactionId");
        assertEquals(transactionId, Integer.valueOf(deletedTransactionId), "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName);
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

    private Integer createSavingsAccountDailyPosting(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, startDate);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsProductDailyPosting() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    // Reset configuration fields
    @AfterEach
    public void tearDown() {
        GlobalConfigurationHelper.resetAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
        GlobalConfigurationHelper.verifyAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
    }
}
