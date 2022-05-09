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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Date;
import java.util.List;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatatableIntegrationTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private DatatableHelper datatableHelper;

    private static final String CLIENT_APP_TABLE_NAME = "m_client";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void validateCreateReadDeleteDatatable() {
        // creating datatable for client entity
        String datatableName = this.datatableHelper.createDatatable(CLIENT_APP_TABLE_NAME, false);
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, datatableName);

        // creating client with datatables
        final Integer clientID = ClientHelper.createClientAsPerson(requestSpec, responseSpec);

        // creating new client datatable entry
        final boolean genericResultSet = true;
        Integer datatableResourceID = this.datatableHelper.createDatatableEntry(CLIENT_APP_TABLE_NAME, datatableName, clientID,
                genericResultSet, "yyyy-MM-dd");
        assertNotNull(datatableResourceID, "ERROR IN CREATING THE ENTITY DATATABLE RECORD");

        // Read the Datatable entry generated with genericResultSet in true (default)
        final List<String> items = this.datatableHelper.readDatatableEntry(datatableName, clientID, genericResultSet, "data");
        assertEquals(1, items.size());

        // Read the Datatable entry generated with genericResultSet in false
        final Date valueDate = this.datatableHelper.readDatatableEntry(datatableName, clientID, !genericResultSet, 0, "Date of Approval");
        assertNotNull(valueDate, "ERROR IN GETTING THE DATE VALUE FROM DATATABLE RECORD");
        assertInstanceOf(Date.class, valueDate);

        // deleting datatable entries
        Integer appTableId = this.datatableHelper.deleteDatatableEntries(datatableName, clientID, "clientId");
        assertEquals(clientID, appTableId, "ERROR IN DELETING THE DATATABLE ENTRIES");

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName);
        assertEquals(datatableName, deletedDataTableName, "ERROR IN DELETING THE DATATABLE");
    }

}
