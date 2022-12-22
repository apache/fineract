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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.PutDataTablesRequest;
import org.apache.fineract.client.models.PutDataTablesRequestAddColumns;
import org.apache.fineract.client.models.PutDataTablesRequestChangeColumns;
import org.apache.fineract.client.models.PutDataTablesResponse;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatatableUniqueAndIndexColumnTest {

    private static final String LOAN_APP_TABLE_NAME = "m_loan";
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private DatatableHelper datatableHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testDatableCreationWithUniqueAndIndexedColumns() {
        // create dataTable
        String datatableName = Utils.randomNameGenerator("dt_" + LOAN_APP_TABLE_NAME + "_", 5).toLowerCase().toLowerCase();
        String column1Name = "itsanumber";
        String column2Name = "itsastring";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(LOAN_APP_TABLE_NAME);
        request.setMultiRow(false);

        PostColumnHeaderData column1HeaderRequestData = new PostColumnHeaderData();
        column1HeaderRequestData.setName(column1Name);
        column1HeaderRequestData.setType("Number");
        column1HeaderRequestData.setMandatory(false);
        column1HeaderRequestData.setLength(10L);
        column1HeaderRequestData.setCode("");
        column1HeaderRequestData.setUnique(true);
        column1HeaderRequestData.setIndexed(true);

        request.addColumnsItem(column1HeaderRequestData);

        PostColumnHeaderData column2HeaderRequestData = new PostColumnHeaderData();
        column2HeaderRequestData.setName(column2Name);
        column2HeaderRequestData.setType("String");
        column2HeaderRequestData.setMandatory(false);
        column2HeaderRequestData.setLength(10L);
        column2HeaderRequestData.setCode("");
        column2HeaderRequestData.setUnique(false);
        column2HeaderRequestData.setIndexed(true);

        request.addColumnsItem(column2HeaderRequestData);

        PostDataTablesResponse response = datatableHelper.createDatatable(request);
        // Get Details of created datatable and verify unique and index
        GetDataTablesResponse dataTable = datatableHelper.getDataTableDetails(datatableName);

        // verfify columns
        List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertNotNull(columnHeaderData);

        // two columns with 1 primary key and 2 audit columns created
        assertEquals(columnHeaderData.size(), 5);

        // verify Only Unique is set for column with both unique and index set to true
        for (ResultsetColumnHeaderData column : columnHeaderData) {
            if (column.getColumnName().equalsIgnoreCase(column1Name)) {
                assertTrue(column.getIsColumnUnique());
                assertFalse(column.getIsColumnIndexed());
            }
            if (column.getColumnName().equalsIgnoreCase(column2Name)) {
                assertTrue(column.getIsColumnIndexed());
            }
        }

    }

    @Test
    public void testDatableModificationWithUniqueAndIndexedColumns() {
        // create dataTable
        String datatableName = Utils.randomNameGenerator("dt_" + LOAN_APP_TABLE_NAME + "_", 5).toLowerCase().toLowerCase();
        String column1Name = "itsanumber";
        String column2Name = "itsastring";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(LOAN_APP_TABLE_NAME);
        request.setMultiRow(false);

        PostColumnHeaderData column1HeaderRequestData = new PostColumnHeaderData();
        column1HeaderRequestData.setName(column1Name);
        column1HeaderRequestData.setType("Number");
        column1HeaderRequestData.setMandatory(false);
        column1HeaderRequestData.setLength(10L);
        column1HeaderRequestData.setCode("");
        column1HeaderRequestData.setUnique(true);
        column1HeaderRequestData.setIndexed(true);

        request.addColumnsItem(column1HeaderRequestData);

        PostColumnHeaderData column2HeaderRequestData = new PostColumnHeaderData();
        column2HeaderRequestData.setName(column2Name);
        column2HeaderRequestData.setType("String");
        column2HeaderRequestData.setMandatory(false);
        column2HeaderRequestData.setLength(10L);
        column2HeaderRequestData.setCode("");
        column2HeaderRequestData.setUnique(false);
        column2HeaderRequestData.setIndexed(true);

        request.addColumnsItem(column2HeaderRequestData);

        PostDataTablesResponse response = datatableHelper.createDatatable(request);

        assertEquals(datatableName, response.getResourceIdentifier());

        // Modify datatable add columns and change columns
        PutDataTablesRequest updateRequest = new PutDataTablesRequest();
        updateRequest.setApptableName(LOAN_APP_TABLE_NAME);

        String column3Name = "number1";
        String column4Name = "number2";

        PutDataTablesRequestAddColumns addColumn1 = new PutDataTablesRequestAddColumns();
        addColumn1.setName(column3Name);
        addColumn1.setType("Number");
        addColumn1.setMandatory(false);
        addColumn1.setCode("");
        addColumn1.setUnique(true);
        addColumn1.setIndexed(false);

        updateRequest.addAddColumnsItem(addColumn1);

        PutDataTablesRequestAddColumns addColumn2 = new PutDataTablesRequestAddColumns();
        addColumn2.setName(column4Name);
        addColumn2.setType("Number");
        addColumn2.setMandatory(false);
        addColumn2.setCode("");
        addColumn2.setUnique(false);
        addColumn2.setIndexed(true);

        updateRequest.addAddColumnsItem(addColumn2);

        PutDataTablesRequestChangeColumns changeColumns = new PutDataTablesRequestChangeColumns();
        changeColumns.setName(column1Name);
        String newColumnName = column1Name + "new";
        changeColumns.setNewName(newColumnName);
        changeColumns.setIndexed(true);

        updateRequest.addChangeColumnsItem(changeColumns);

        // update dataTable
        PutDataTablesResponse updateResponse = datatableHelper.updateDatatable(datatableName, updateRequest);

        // Get Details of created datatable and verify unique and index
        GetDataTablesResponse dataTable = datatableHelper.getDataTableDetails(datatableName);

        // verify columns
        List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertNotNull(columnHeaderData);

        // 2 columns with 1 primary key ,2 audit columns and 2 new columns created
        assertEquals(columnHeaderData.size(), 7);

        // verify unique and index is set for new columns and renamed column has index set
        for (ResultsetColumnHeaderData column : columnHeaderData) {
            if (column.getColumnName().equalsIgnoreCase(column3Name)) {
                assertTrue(column.getIsColumnUnique());
            }
            if (column.getColumnName().equalsIgnoreCase(column4Name)) {
                assertTrue(column.getIsColumnIndexed());
            }
            if (column.getColumnName().equalsIgnoreCase(newColumnName)) {
                assertTrue(column.getIsColumnIndexed());
            }
        }

    }
}
