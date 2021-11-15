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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.AuditHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
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
    private AuditHelper auditHelper;
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
        this.auditHelper = new AuditHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    /**
     * Here we Create/Update different Entities and verify an audit is generated for each action. This can be further
     * extened with more entities and actions in similiar way.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void auditShouldbeCreated() {
        // Audits recieved after all actions are performed.
        List<HashMap<String, Object>> auditsRecieved;

        // Audits recieved before any action is performed, needed in special
        // cases eg: reactivate client, close client
        List<HashMap<String, Object>> auditsRecievedInitial;

        // When Client is created: Count should be "1"
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);

        auditsRecieved = auditHelper.getAuditDetails(clientId, "CREATE", "CLIENT");
        auditHelper.verifyOneAuditOnly(auditsRecieved, clientId, "CREATE", "CLIENT");

        // Performs multiple close and reactivate on client

        for (int i = 0; i < 4; i++) {
            // Close
            auditsRecievedInitial = auditHelper.getAuditDetails(clientId, "CLOSE", "CLIENT");
            this.clientHelper.closeClient(clientId);
            auditsRecieved = auditHelper.getAuditDetails(clientId, "CLOSE", "CLIENT");
            auditHelper.verifyMultipleAuditsOnserver(auditsRecievedInitial, auditsRecieved, clientId, "CLOSE", "CLIENT");

            // Activate
            auditsRecievedInitial = auditHelper.getAuditDetails(clientId, "REACTIVATE", "CLIENT");
            this.clientHelper.reactivateClient(clientId);
            auditsRecieved = auditHelper.getAuditDetails(clientId, "REACTIVATE", "CLIENT");
            auditHelper.verifyMultipleAuditsOnserver(auditsRecievedInitial, auditsRecieved, clientId, "REACTIVATE", "CLIENT");
        }

        // When Office is created
        OfficeHelper officeHelper = new OfficeHelper(requestSpec, responseSpec);
        int officeId = officeHelper.createOffice("22 June 2020");
        auditsRecieved = auditHelper.getAuditDetails(officeId, "CREATE", "OFFICE");
        auditHelper.verifyOneAuditOnly(auditsRecieved, officeId, "CREATE", "OFFICE");
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
            auditHelper.verifyLimitParameterfor(limit);
        }
    }

    @Test
    public void checkIfOrderBySupported() {
        final List<String> shouldBeSupportedFor = Arrays.asList("checkedOnDate", "officeName", "resourceId", "clientId", "processingResult",
                "clientName", "maker", "subresourceId", "checker", "savingsAccountNo", "loanAccountNo", "groupName", "entityName",
                "madeOnDate", "id", "loanId", "actionName");

        for (int i = 0; i < shouldBeSupportedFor.size(); i++) {
            auditHelper.verifyOrderBysupported(shouldBeSupportedFor.get(i));
        }

    }

}
