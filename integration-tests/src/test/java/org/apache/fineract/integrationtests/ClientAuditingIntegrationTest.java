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

import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.CREATED_DATE_PLUS_ONE;
import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.DEFAULT_SUBMITTED_ON_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import org.apache.fineract.client.models.ClientAuditFieldsData;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAuditingIntegrationTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientAuditingIntegrationTest.class);
    private static final Long SUPER_USER_ROLE_ID = 1L;
    private static final Long ADMIN_USER_ID = 1L;
    private static final String PASSWORD = "A1b2c3d4e5f$";

    private FeignClientHelper clientHelper;
    private FeignStaffHelper staffHelper;

    @BeforeAll
    public void setup() {
        clientHelper = new FeignClientHelper(fineractClient());
        staffHelper = new FeignStaffHelper(fineractClient());
    }

    @Test
    public void checkAuditDates() throws InterruptedException {
        final Long staffId = staffHelper.createStaff().getResourceId();
        String username = Utils.uniqueRandomStringGenerator("user", 8);
        final Long userId = FeignUserHelper.createUser(SUPER_USER_ROLE_ID, staffId, username, PASSWORD).getResourceId();
        OffsetDateTime now = Utils.getAuditDateTimeToCompare();
        LOG.info("-------------------------Creating Client---------------------------");

        final Long clientID = clientHelper.createClientPending(DEFAULT_SUBMITTED_ON_DATE).getClientId();
        assertEquals(clientID, clientHelper.getClient(clientID).getId());
        ClientAuditFieldsData auditFieldsResponse = clientHelper.getClientAuditFields(clientID);

        OffsetDateTime createdDate = auditFieldsResponse.getCreatedDate();
        OffsetDateTime lastModifiedDate = auditFieldsResponse.getLastModifiedDate();

        LOG.info("-------------------------Check Audit dates---------------------------");
        assertEquals(ADMIN_USER_ID, auditFieldsResponse.getCreatedBy());
        assertEquals(ADMIN_USER_ID, auditFieldsResponse.getLastModifiedBy());
        assertTrue(DateUtils.isEqual(now, createdDate, ChronoUnit.MINUTES));
        assertTrue(DateUtils.isEqual(now, lastModifiedDate, ChronoUnit.MINUTES));

        LOG.info("-------------------------Modify Client with System user---------------------------");
        FeignClientHelper userClientHelper = new FeignClientHelper(
                FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD));

        OffsetDateTime now2 = Utils.getAuditDateTimeToCompare();
        userClientHelper.activateClient(clientID, ClientRequestBuilders.activateClient(CREATED_DATE_PLUS_ONE));
        auditFieldsResponse = clientHelper.getClientAuditFields(clientID);

        OffsetDateTime createdDate2 = auditFieldsResponse.getCreatedDate();
        lastModifiedDate = auditFieldsResponse.getLastModifiedDate();

        LOG.info("-------------------------Check Audit dates---------------------------");
        assertEquals(ADMIN_USER_ID, auditFieldsResponse.getCreatedBy());
        assertTrue(DateUtils.isEqual(now, createdDate2, ChronoUnit.MINUTES));
        assertTrue(DateUtils.isEqual(createdDate, createdDate2));

        assertEquals(userId, auditFieldsResponse.getLastModifiedBy());
        assertTrue(DateUtils.isEqual(now2, lastModifiedDate, ChronoUnit.MINUTES));
    }
}
