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
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.EntityDatatableChecksHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Entity Data Table Checks on {@code m_client} must be enforced whatever case the datatable's entity subtype was
 * registered with ({@code "Person"} is the documented value).
 */
public class FeignClientEntityDatatableCheckSubtypeTest extends FeignIntegrationTest {

    private static final String CLIENT_APP_TABLE_NAME = "m_client";
    private static final long STATUS_CREATE = 100L;
    private static final long STATUS_ACTIVATE = 300L;

    private static FeignClientHelper clientHelper;
    private static FeignDatatableHelper datatableHelper;

    @BeforeAll
    public static void setup() {
        FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        clientHelper = new FeignClientHelper(fineractClient);
        datatableHelper = new FeignDatatableHelper(fineractClient);
    }

    @Test
    void createPersonClientIsRejectedWhenCheckDatatableHasMixedCaseSubtype() {
        String datatableName = createClientDatatable("Person");
        Long checkId = EntityDatatableChecksHelper.createEntityDatatableCheck(CLIENT_APP_TABLE_NAME, datatableName, STATUS_CREATE, null)
                .getResourceId();
        try {
            String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
            CallFailedRuntimeException failure = clientHelper.createClientExpectingError(ClientRequestBuilders.createPendingClient(today));

            assertEquals(403, failure.getStatus());
            assertTrue(failure.getResponseBody().contains("error.msg.entry.required.in.datatable"), failure.getResponseBody());
        } finally {
            EntityDatatableChecksHelper.deleteEntityDatatableCheck(checkId);
            datatableHelper.deleteDatatable(datatableName);
        }
    }

    @Test
    void activatePersonClientIsRejectedWhenCheckDatatableHasMixedCaseSubtype() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        PostClientsResponse pending = clientHelper.createClientPending(today);

        String datatableName = createClientDatatable("Person");
        Long checkId = EntityDatatableChecksHelper.createEntityDatatableCheck(CLIENT_APP_TABLE_NAME, datatableName, STATUS_ACTIVATE, null)
                .getResourceId();
        try {
            CallFailedRuntimeException failure = clientHelper.activateClientExpectingError(pending.getClientId(),
                    ClientRequestBuilders.activateClient(today));

            assertEquals(403, failure.getStatus());
            assertTrue(failure.getResponseBody().contains("error.msg.entry.required.in.datatable"), failure.getResponseBody());
        } finally {
            EntityDatatableChecksHelper.deleteEntityDatatableCheck(checkId);
            datatableHelper.deleteDatatable(datatableName);
        }
    }

    private String createClientDatatable(String entitySubType) {
        PostDataTablesRequest request = new PostDataTablesRequest()//
                .datatableName(Utils.uniqueRandomStringGenerator("dt_client_subtype_", 5).toLowerCase())//
                .apptableName(CLIENT_APP_TABLE_NAME)//
                .entitySubType(entitySubType)//
                .multiRow(false)//
                .columns(List.of(new PostColumnHeaderData().name("note").type("String").length(50L).mandatory(true)));
        return datatableHelper.createDatatable(request).getResourceIdentifier();
    }
}
