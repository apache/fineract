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
package org.apache.fineract.integrationtests.campaigns;

import static org.apache.fineract.integrationtests.client.IntegrationTest.assertThat;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EmailTest {

    private static final Gson GSON = new Gson();
    private static final String EMAIL_URL = "/fineract-provider/api/v1/email";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testEmailCreateRetrieveUpdateDeleteLifecycle() {
        // Arrange: client must have an emailAddress, since EmailMessageAssembler
        // derives the recipient address from it (no address => data integrity exception).
        PostClientsRequest clientRequest = ClientHelper.defaultClientCreationRequest();
        clientRequest.emailAddress(Utils.randomStringGenerator("email_", 6) + "@example.com");
        PostClientsResponse client = ClientHelper.createClient(clientRequest);

        String initialSubject = Utils.randomStringGenerator("Subject_", 10);
        String initialMessage = Utils.randomStringGenerator("Message_", 20);

        Map<String, Object> createRequest = new LinkedHashMap<>();
        createRequest.put("clientId", client.getClientId());
        createRequest.put("emailSubject", initialSubject);
        createRequest.put("emailMessage", initialMessage);
        createRequest.put("locale", "en");

        // Act: CREATE
        Long emailId = ((Number) Utils.performServerPost(requestSpec, responseSpec, emailUrl(), GSON.toJson(createRequest), "resourceId"))
                .longValue();

        // Assert: RETRIEVE after create
        JsonPath created = retrieveEmail(emailId);
        assertThat(created.getLong("id")).isEqualTo(emailId);
        assertThat(created.getLong("clientId")).isEqualTo(client.getClientId().longValue());
        assertThat(created.getString("emailSubject")).isEqualTo(initialSubject);
        assertThat(created.getString("emailMessage")).isEqualTo(initialMessage);

        // Act: UPDATE (only emailMessage is a supported update param)
        String updatedMessage = Utils.randomStringGenerator("UpdatedMessage_", 20);
        Map<String, Object> updateRequest = new LinkedHashMap<>();
        updateRequest.put("emailMessage", updatedMessage);

        JsonPath updateResponse = JsonPath
                .from(Utils.performServerPut(requestSpec, responseSpec, emailUrl(emailId), GSON.toJson(updateRequest)));
        assertThat(updateResponse.getLong("resourceId")).isEqualTo(emailId);
        assertThat(updateResponse.getString("changes.emailMessage")).isEqualTo(updatedMessage);

        // Assert: RETRIEVE after update
        JsonPath updated = retrieveEmail(emailId);
        assertThat(updated.getString("emailMessage")).isEqualTo(updatedMessage);
        // subject is untouched by update, since UPDATE_REQUEST_DATA_PARAMETERS only allows emailMessage
        assertThat(updated.getString("emailSubject")).isEqualTo(initialSubject);

        // Act: DELETE
        Long deletedResourceId = ((Number) Utils.performServerDelete(requestSpec, responseSpec, emailUrl(emailId), "resourceId"))
                .longValue();
        assertThat(deletedResourceId).isEqualTo(emailId);

        // Assert: RETRIEVE after delete should 404 -- use a 404-expecting response spec
        ResponseSpecification notFoundSpec = new ResponseSpecBuilder().expectStatusCode(404).build();
        Utils.performServerGet(requestSpec, notFoundSpec, emailUrl(emailId), null);
    }

    @Test
    public void testEmailCreateWithStaffIdOnlyDoesNotThrow() {
        // Arrange: staff must have an emailAddress, since EmailMessageAssembler
        // derives the recipient address from it (no address => data integrity exception,
        // which the platform maps to a 403 -- Postgres enforces the NOT NULL constraint
        // on email_address strictly, unlike MySQL/MariaDB in non-strict mode).
        Map<String, Object> staffRequest = StaffHelper.getMapWithJoiningDate();
        staffRequest.put("officeId", 1);
        staffRequest.put("firstname", Utils.uniqueRandomStringGenerator("staff_", 5));
        staffRequest.put("lastname", Utils.uniqueRandomStringGenerator("Doe_", 4));
        staffRequest.put("isLoanOfficer", true);
        staffRequest.put("emailAddress", Utils.randomStringGenerator("staff_email_", 6) + "@example.com");

        Integer staffId = (Integer) StaffHelper.createStaffWithJson(requestSpec, responseSpec, GSON.toJson(staffRequest)).get("resourceId");

        Map<String, Object> createRequest = new LinkedHashMap<>();
        createRequest.put("staffId", staffId);
        createRequest.put("emailSubject", Utils.randomStringGenerator("Subject_", 10));
        createRequest.put("emailMessage", Utils.randomStringGenerator("Message_", 20));
        createRequest.put("locale", "en");

        Long emailId = ((Number) Utils.performServerPost(requestSpec, responseSpec, emailUrl(), GSON.toJson(createRequest), "resourceId"))
                .longValue();

        assertThat(retrieveEmail(emailId).getLong("staffId")).isEqualTo(staffId.longValue());
    }

    @Test
    public void testEmailCreateWithoutClientOrStaffIdFails() {
        Map<String, Object> createRequest = new LinkedHashMap<>();
        createRequest.put("emailSubject", Utils.randomStringGenerator("Subject_", 10));
        createRequest.put("emailMessage", Utils.randomStringGenerator("Message_", 20));
        createRequest.put("locale", "en");

        ResponseSpecification badRequestSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        Utils.performServerPost(requestSpec, badRequestSpec, emailUrl(), GSON.toJson(createRequest), "");
    }

    private JsonPath retrieveEmail(final Long emailId) {
        String response = Utils.performServerGet(requestSpec, responseSpec, emailUrl(emailId), null);
        return JsonPath.from(response);
    }

    private String emailUrl() {
        return EMAIL_URL + "?" + Utils.TENANT_IDENTIFIER;
    }

    private String emailUrl(final Long emailId) {
        return EMAIL_URL + "/" + emailId + "?" + Utils.TENANT_IDENTIFIER;
    }
}
