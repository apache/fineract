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
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.shares.ShareProductHelper;
import org.apache.fineract.integrationtests.common.shares.ShareProductTransactionHelper;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShareProductDatatableIntegrationTest {

    private static final String SHARES_APP_TABLE_NAME = "m_share_product";
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private DatatableHelper datatableHelper;
    private ShareProductHelper shareProductHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testDatatableCreationForShareProduct() {
        // create dataTable
        String datatableName = Utils.uniqueRandomStringGenerator("dt_" + SHARES_APP_TABLE_NAME + "_", 5).toLowerCase().toLowerCase();
        String column1Name = "aNumber";
        String column2Name = "aString";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(SHARES_APP_TABLE_NAME);
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

        // verify Datatable got created
        GetDataTablesResponse dataTable = datatableHelper.getDataTableDetails(datatableName);

        // verfify columns
        List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertNotNull(columnHeaderData);

        // two columns with 1 primary key and 2 audit columns created
        assertEquals(columnHeaderData.size(), 5);
    }

    @Test
    public void testDatatableEntryForShareProduct() {
        // create Shares Product
        shareProductHelper = new ShareProductHelper();
        final Integer shareProductId = createShareProduct();

        assertNotNull(shareProductId);

        // create dataTable
        String datatableName = Utils.uniqueRandomStringGenerator("dt_" + SHARES_APP_TABLE_NAME + "_", 5).toLowerCase().toLowerCase();
        String column1Name = "aNumber";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(SHARES_APP_TABLE_NAME);
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

        String datatableId = response.getResourceIdentifier();

        // add entries
        final HashMap<String, Object> datatableEntryMap = new HashMap<>();
        datatableEntryMap.put(column1Name, Utils.randomNumberGenerator(5));
        datatableEntryMap.put("locale", "en");
        datatableEntryMap.put("dateFormat", "yyyy-MM-dd");

        String datatabelEntryRequestJsonString = new Gson().toJson(datatableEntryMap);

        final boolean genericResultSet = true;

        HashMap<String, Object> datatableEntryResponseFirst = this.datatableHelper.createDatatableEntry(datatableId, shareProductId,
                genericResultSet, datatabelEntryRequestJsonString);
        HashMap<String, Object> datatableEntryResponseSecond = this.datatableHelper.createDatatableEntry(datatableId, shareProductId,
                genericResultSet, datatabelEntryRequestJsonString);

        assertNotNull(datatableEntryResponseFirst.get("resourceId"));
        assertNotNull(datatableEntryResponseSecond.get("resourceId"));

        // Read the Datatable entry generated with genericResultSet
        HashMap<String, Object> items = this.datatableHelper.readDatatableEntry(datatableId, shareProductId, genericResultSet, null, "");
        assertNotNull(items);
        assertEquals(2, ((List) items.get("data")).size());
    }

    private Integer createShareProduct() {
        String shareProductJson = shareProductHelper.build();
        return ShareProductTransactionHelper.createShareProduct(shareProductJson, requestSpec, responseSpec);
    }
}
