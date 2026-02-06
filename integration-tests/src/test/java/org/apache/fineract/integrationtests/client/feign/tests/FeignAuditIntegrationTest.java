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

import java.util.Arrays;
import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AuditSearchData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAuditHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAuditHelper.AuditEntryItem;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAuditHelper.PagedAuditResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FeignAuditIntegrationTest extends FeignIntegrationTest {

    private static FeignAuditHelper auditHelper;
    private static FeignClientHelper clientHelper;
    private static FeignOfficeHelper officeHelper;

    @BeforeAll
    public static void setup() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        auditHelper = new FeignAuditHelper(client);
        clientHelper = new FeignClientHelper(client);
        officeHelper = new FeignOfficeHelper(client);
    }

    @Test
    public void testAuditSearchTemplate() {
        AuditSearchData searchTemplate = auditHelper.getAuditSearchTemplate();

        assertThat(searchTemplate).isNotNull();
        assertThat(searchTemplate.getActionNames()).isNotEmpty();
        assertThat(searchTemplate.getEntityNames()).isNotEmpty();
        assertThat(searchTemplate.getStatuses()).isNotNull();
    }

    @Test
    public void testAuditCreatedForClientCreation() {
        Long clientId = clientHelper.createClient();

        List<AuditEntryItem> audits = auditHelper.getAuditsByResourceAndAction(clientId, "CREATE", "CLIENT");

        assertThat(audits).hasSize(1);
        AuditEntryItem audit = audits.get(0);
        assertThat(audit.getActionName()).isEqualTo("CREATE");
        assertThat(audit.getEntityName()).isEqualTo("CLIENT");
        assertThat(audit.getResourceId()).isEqualTo(clientId);
    }

    @Test
    public void testAuditCreatedForOfficeCreation() {
        Long officeId = officeHelper.createOffice();

        List<AuditEntryItem> audits = auditHelper.getAuditsByResourceAndAction(officeId, "CREATE", "OFFICE");
        assertThat(audits).hasSize(1);
        assertThat(audits.get(0).getEntityName()).isEqualTo("OFFICE");
    }

    @Test
    public void testAuditsWithLimitParameter() {
        clientHelper.createClient();

        for (int limit = 1; limit <= 5; limit++) {
            PagedAuditResponse response = auditHelper.getAudits(limit);
            assertThat(response.getPageItems()).hasSizeLessThanOrEqualTo(limit);
        }
    }

    @Test
    public void testOrderBySupported() {
        List<String> supportedOrderBy = Arrays.asList("checkedOnDate", "officeName", "resourceId", "clientId", "processingResult",
                "clientName", "maker", "subresourceId", "checker", "savingsAccountNo", "loanAccountNo", "groupName", "entityName",
                "madeOnDate", "id", "loanId", "actionName");

        for (String orderBy : supportedOrderBy) {
            PagedAuditResponse response = auditHelper.getAuditsWithOrderBy(orderBy);
            assertThat(response).isNotNull();
        }
    }

    @Test
    public void testAuditSearchWithDateTimeAndTimeZone() {
        clientHelper.createClient();
        String today = Utils.getLocalDateOfTenant().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fromDateTime = today + " 00:00:00";
        String toDateTime = today + " 23:59:59";

        List<AuditEntryItem> audits = auditHelper.getAuditsPageItems(fromDateTime, toDateTime, "yyyy-MM-dd HH:mm:ss", "Europe/Budapest");

        assertThat(audits).isNotEmpty();
    }

    @Test
    public void testAuditSearchWithInlineOffset() {
        clientHelper.createClient();
        String today = Utils.getLocalDateOfTenant().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fromDateTime = today + " 00:00:00+00:00";
        String toDateTime = today + " 23:59:59+00:00";

        List<AuditEntryItem> audits = auditHelper.getAuditsPageItems(fromDateTime, toDateTime, "yyyy-MM-dd HH:mm:ss", null);

        assertThat(audits).isNotEmpty();
    }

    @Test
    public void testAuditSearchWithDateOnly() {
        clientHelper.createClient();
        String today = Utils.getLocalDateOfTenant().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<AuditEntryItem> audits = auditHelper.getAuditsPageItems(today, today, "yyyy-MM-dd", null);

        assertThat(audits).isNotEmpty();
    }

    @Test
    public void testAuditSearchWithInvalidTimeZone() {
        String today = Utils.getLocalDateOfTenant().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fromDateTime = today + " 00:00:00";

        CallFailedRuntimeException exception = auditHelper.getAuditsExpectingError(fromDateTime, null, "yyyy-MM-dd HH:mm:ss",
                "INVALID_TIMEZONE");
        assertThat(exception.getStatus()).isEqualTo(400);
    }

    @Test
    public void testGetAuditsReturnsTypedResponse() {
        clientHelper.createClient();

        PagedAuditResponse response = auditHelper.getAudits(5);

        assertThat(response).isNotNull();
        assertThat(response.getTotalFilteredRecords()).isNotNull();
        assertThat(response.getPageItems()).isNotNull();

        if (!response.getPageItems().isEmpty()) {
            AuditEntryItem item = response.getPageItems().get(0);
            assertThat(item.getId()).isNotNull();
            assertThat(item.getActionName()).isNotNull();
            assertThat(item.getEntityName()).isNotNull();
        }
    }

    @Test
    public void testAuditCreatedForCloseAndReactivateClient() {
        Long clientId = clientHelper.createClient();

        for (int i = 0; i < 4; i++) {
            int closeAuditsBefore = auditHelper.getAuditsByResourceAndAction(clientId, "CLOSE", "CLIENT").size();
            clientHelper.closeClient(clientId);
            List<AuditEntryItem> closeAudits = auditHelper.getAuditsByResourceAndAction(clientId, "CLOSE", "CLIENT");
            assertThat(closeAudits).hasSize(closeAuditsBefore + 1);
            assertThat(closeAudits.get(0).getActionName()).isEqualTo("CLOSE");
            assertThat(closeAudits.get(0).getEntityName()).isEqualTo("CLIENT");

            int reactivateAuditsBefore = auditHelper.getAuditsByResourceAndAction(clientId, "REACTIVATE", "CLIENT").size();
            clientHelper.reactivateClient(clientId);
            List<AuditEntryItem> reactivateAudits = auditHelper.getAuditsByResourceAndAction(clientId, "REACTIVATE", "CLIENT");
            assertThat(reactivateAudits).hasSize(reactivateAuditsBefore + 1);
            assertThat(reactivateAudits.get(0).getActionName()).isEqualTo("REACTIVATE");
            assertThat(reactivateAudits.get(0).getEntityName()).isEqualTo("CLIENT");
        }
    }
}
