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
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.client.models.AuditSearchData;
import org.apache.fineract.integrationtests.common.AuditHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Manthan Surkar
 *
 */
public class AuditIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private SchedulerJobHelper schedulerJobHelper;
    private static final SecureRandom rand = new SecureRandom();

    /**
     * Sets up the essential settings for the TEST like contentType, expectedStatusCode. It uses the '@BeforeEach'
     * annotation provided by jUnit.
     */
    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec);
    }

    @Test
    public void testAuditSearchTemplate() {
        // given
        // when
        AuditSearchData auditSearchTemplate = AuditHelper.getAuditSearchTemplate();

        // then
        assertNotNull(auditSearchTemplate);
        assertTrue(auditSearchTemplate.getActionNames().size() > 0);

        // verify all command processing status enum values are present and use enum_value (not enum_message_property)
        assertNotNull(auditSearchTemplate.getStatuses());
        assertEquals(6, auditSearchTemplate.getStatuses().size());

        List<String> statusValues = auditSearchTemplate.getStatuses().stream().map(r -> r.getProcessingResult())
                .collect(java.util.stream.Collectors.toList());

        assertTrue(statusValues.contains("Invalid"));
        assertTrue(statusValues.contains("Processed"));
        assertTrue(statusValues.contains("Awaiting Approval"));
        assertTrue(statusValues.contains("Rejected"));
        assertTrue(statusValues.contains("Under Processing"));
        assertTrue(statusValues.contains("Error"));
    }

    /**
     * Here we Create/Update different Entities and verify an audit is generated for each action. This can be further
     * extened with more entities and actions in similiar way.
     */
    @Test
    public void auditShouldbeCreated() {
        // Audits recieved after all actions are performed.
        List<AuditData> auditsRecieved;

        // Audits recieved before any action is performed, needed in special
        // cases eg: reactivate client, close client
        List<AuditData> auditsRecievedInitial;

        // When Client is created: Count should be "1"
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        auditsRecieved = AuditHelper.getAuditDetails(clientId, "CREATE", "CLIENT");
        AuditHelper.verifyOneAuditOnly(auditsRecieved, clientId, "CREATE", "CLIENT");

        // Performs multiple close and reactivate on client

        for (int i = 0; i < 4; i++) {
            // Close
            auditsRecievedInitial = AuditHelper.getAuditDetails(clientId, "CLOSE", "CLIENT");
            this.clientHelper.closeClient(clientId);
            auditsRecieved = AuditHelper.getAuditDetails(clientId, "CLOSE", "CLIENT");
            AuditHelper.verifyMultipleAuditsOnserver(auditsRecievedInitial, auditsRecieved, clientId, "CLOSE", "CLIENT");

            // Activate
            auditsRecievedInitial = AuditHelper.getAuditDetails(clientId, "REACTIVATE", "CLIENT");
            this.clientHelper.reactivateClient(clientId);
            auditsRecieved = AuditHelper.getAuditDetails(clientId, "REACTIVATE", "CLIENT");
            AuditHelper.verifyMultipleAuditsOnserver(auditsRecievedInitial, auditsRecieved, clientId, "REACTIVATE", "CLIENT");
        }

        // When Office is created
        OfficeHelper officeHelper = new OfficeHelper();
        int officeId = officeHelper.createOffice(java.time.LocalDate.of(2020, 6, 22)).getResourceId().intValue();
        auditsRecieved = AuditHelper.getAuditDetails(officeId, "CREATE", "OFFICE");
        AuditHelper.verifyOneAuditOnly(auditsRecieved, officeId, "CREATE", "OFFICE");
    }

    @Test
    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public void checkAuditsWithLimitParam() {
        // Create client
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);

        // The following loop would ensure database have atleast 8 audits.
        for (int i = 0; i < 4; i++) {
            // Close client
            this.clientHelper.closeClient(clientId);
            // Activate client
            this.clientHelper.reactivateClient(clientId);
        }

        for (int i = 0; i < 3; i++) {
            // limit contains a number between 1-8
            int limit = rand.nextInt(7) + 1;
            AuditHelper.verifyLimitParameterfor(limit);
        }
    }

    @Test
    public void checkIfOrderBySupported() {
        final List<String> shouldBeSupportedFor = Arrays.asList("checkedOnDate", "officeName", "resourceId", "clientId", "processingResult",
                "clientName", "maker", "subresourceId", "checker", "savingsAccountNo", "loanAccountNo", "groupName", "entityName",
                "madeOnDate", "id", "loanId", "actionName");

        for (int i = 0; i < shouldBeSupportedFor.size(); i++) {
            AuditHelper.verifyOrderBysupported(shouldBeSupportedFor.get(i));
        }

    }

    @Test
    public void executeSchedulerJobShouldCreateAuditEntry() {
        // given
        int jobId = schedulerJobHelper.getSchedulerJobIdByShortName("SA_AANF").intValue();
        List<AuditData> auditsRecievedInitial = AuditHelper.getAuditDetails(jobId, "EXECUTEJOB", "SCHEDULER");

        // when
        schedulerJobHelper.runSchedulerJob(jobId);

        // then
        List<AuditData> auditsRecieved = AuditHelper.getAuditDetails(jobId, "EXECUTEJOB", "SCHEDULER");
        AuditHelper.verifyMultipleAuditsOnserver(auditsRecievedInitial, auditsRecieved, jobId, "EXECUTEJOB", "SCHEDULER");
    }

}
