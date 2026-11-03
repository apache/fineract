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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PutDataTablesRequest;
import org.apache.fineract.client.models.PutDataTablesRequestChangeColumns;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Renaming a datatable column to the name of its foreign-key column fails in the database, and the failure must reach
 * the client instead of being swallowed: a swallowed failed ALTER answered 200 on MariaDB/MySQL and, on PostgreSQL,
 * left the transaction aborted so the next statement failed with an unrelated 500.
 */
public class FeignDatatableRenameToReservedColumnTest extends FeignIntegrationTest {

    private static final String CLIENT_APP_TABLE_NAME = "m_client";
    private static final String CLIENT_FK_COLUMN = "client_id";
    private static final String NOTE_COLUMN = "note";

    private static FeignDatatableHelper datatableHelper;

    @BeforeAll
    public static void setup() {
        FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        datatableHelper = new FeignDatatableHelper(fineractClient);
    }

    @Test
    void renameToForeignKeyColumnIsRejectedAndLeavesTheTableUnchanged() {
        String datatableName = createClientDatatable();
        try {
            PutDataTablesRequest rename = new PutDataTablesRequest()
                    .changeColumns(List.of(new PutDataTablesRequestChangeColumns().name(NOTE_COLUMN).newName(CLIENT_FK_COLUMN)));

            CallFailedRuntimeException failure = datatableHelper.updateDatatableExpectingError(datatableName, rename);

            assertEquals(403, failure.getStatus(), failure.getResponseBody());
            assertTrue(failure.getResponseBody().contains("data.integrity"), failure.getResponseBody());
            List<String> columns = columnNames(datatableName);
            assertTrue(columns.contains(NOTE_COLUMN), columns.toString());
            assertTrue(columns.contains(CLIENT_FK_COLUMN), columns.toString());
        } finally {
            datatableHelper.deleteDatatable(datatableName);
        }
    }

    private String createClientDatatable() {
        PostDataTablesRequest request = new PostDataTablesRequest()//
                .datatableName(Utils.uniqueRandomStringGenerator("dt_client_rename_", 5).toLowerCase())//
                .apptableName(CLIENT_APP_TABLE_NAME)//
                .entitySubType("PERSON")//
                .multiRow(false)//
                .columns(List.of(new PostColumnHeaderData().name(NOTE_COLUMN).type("String").length(20L).mandatory(false)));
        return datatableHelper.createDatatable(request).getResourceIdentifier();
    }

    private List<String> columnNames(String datatableName) {
        GetDataTablesResponse datatable = datatableHelper.getDatatable(datatableName);
        return datatable.getColumnHeaderData().stream().map(ResultsetColumnHeaderData::getColumnName).toList();
    }
}
