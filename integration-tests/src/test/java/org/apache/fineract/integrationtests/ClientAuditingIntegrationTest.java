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

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_BY;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_DATE;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_BY;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAuditingIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientAuditingIntegrationTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());

        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void checkAuditDates() throws InterruptedException {
        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        String username = Utils.randomNameGenerator("user", 8);
        final Integer userId = (Integer) UserHelper.createUser(this.requestSpec, this.responseSpec, 1, staffId, username, "password",
                "resourceId");
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Kolkata"));
        // Testing in minutes precision, but still need to take care around the end of the actual minute
        if (now.getSecond() > 56) {
            Thread.sleep(5000);
            now = OffsetDateTime.now(ZoneId.of("Asia/Kolkata"));
        }
        LOG.info("-------------------------Creating Client---------------------------");

        final Integer clientID = ClientHelper.createClientPending(requestSpec, responseSpec);
        ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientID);
        Map<String, Object> auditFieldsResponse = ClientHelper.getClientAuditFields(requestSpec, responseSpec, clientID, "");

        OffsetDateTime createdDate = OffsetDateTime.parse((String) auditFieldsResponse.get(CREATED_DATE),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        OffsetDateTime lastModifiedDate = OffsetDateTime.parse((String) auditFieldsResponse.get(LAST_MODIFIED_DATE),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        LOG.info("-------------------------Check Audit dates---------------------------");
        assertEquals(1, auditFieldsResponse.get(CREATED_BY));
        assertEquals(1, auditFieldsResponse.get(LAST_MODIFIED_BY));
        assertEquals(now.getYear(), createdDate.getYear());
        assertEquals(now.getMonth(), createdDate.getMonth());
        assertEquals(now.getDayOfMonth(), createdDate.getDayOfMonth());
        assertEquals(now.getHour(), createdDate.getHour());
        assertEquals(now.getMinute(), createdDate.getMinute());

        assertEquals(now.getYear(), lastModifiedDate.getYear());
        assertEquals(now.getMonth(), lastModifiedDate.getMonth());
        assertEquals(now.getDayOfMonth(), lastModifiedDate.getDayOfMonth());
        assertEquals(now.getHour(), lastModifiedDate.getHour());
        assertEquals(now.getMinute(), lastModifiedDate.getMinute());

        LOG.info("-------------------------Modify Client with System user---------------------------");
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization",
                "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(username, "password"));

        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.clientHelper.activateClient(clientID);
        auditFieldsResponse = ClientHelper.getClientAuditFields(requestSpec, responseSpec, clientID, "");

        createdDate = OffsetDateTime.parse((String) auditFieldsResponse.get(CREATED_DATE), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        lastModifiedDate = OffsetDateTime.parse((String) auditFieldsResponse.get(LAST_MODIFIED_DATE),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        LOG.info("-------------------------Check Audit dates---------------------------");
        assertEquals(1, auditFieldsResponse.get(CREATED_BY));
        assertEquals(now.getYear(), createdDate.getYear());
        assertEquals(now.getMonth(), createdDate.getMonth());
        assertEquals(now.getDayOfMonth(), createdDate.getDayOfMonth());
        assertEquals(now.getHour(), createdDate.getHour());
        assertEquals(now.getMinute(), createdDate.getMinute());

        now = OffsetDateTime.now(ZoneId.of("Asia/Kolkata"));

        assertEquals(userId, auditFieldsResponse.get(LAST_MODIFIED_BY));
        assertEquals(now.getYear(), lastModifiedDate.getYear());
        assertEquals(now.getMonth(), lastModifiedDate.getMonth());
        assertEquals(now.getDayOfMonth(), lastModifiedDate.getDayOfMonth());
        assertEquals(now.getHour(), lastModifiedDate.getHour());
        assertEquals(now.getMinute(), lastModifiedDate.getMinute());
    }

}
