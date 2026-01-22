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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanOriginatorHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(1)
public class LoanOriginatorApiTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpec400;
    private ResponseSpecification responseSpec403;
    private ResponseSpecification responseSpec404;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());

        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpec400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.responseSpec403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.responseSpec404 = new ResponseSpecBuilder().expectStatusCode(404).build();
    }

    // ==================== CRUD LIFECYCLE TESTS ====================

    @Test
    public void testCreateOriginatorWithMinimalData() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();

        // Create with only externalId
        final Integer originatorId = LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId);

        assertNotNull(originatorId, "Originator ID should not be null");

        // Verify created data
        final HashMap<String, Object> originator = LoanOriginatorHelper.getOriginatorById(requestSpec, responseSpec, originatorId);

        assertEquals(externalId, originator.get("externalId"));
        assertEquals("ACTIVE", originator.get("status"), "Default status should be ACTIVE");

        // Cleanup
        LoanOriginatorHelper.deleteOriginator(requestSpec, responseSpec, originatorId);
    }

    @Test
    public void testCreateOriginatorWithAllFields() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();
        final String name = "Test Originator";
        final String status = "PENDING";

        final Integer originatorId = LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId, name, status, null, null);

        assertNotNull(originatorId);

        final HashMap<String, Object> originator = LoanOriginatorHelper.getOriginatorById(requestSpec, responseSpec, originatorId);

        assertEquals(externalId, originator.get("externalId"));
        assertEquals(name, originator.get("name"));
        assertEquals(status, originator.get("status"));

        // Cleanup
        LoanOriginatorHelper.deleteOriginator(requestSpec, responseSpec, originatorId);
    }

    @Test
    public void testRetrieveOriginatorById() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();
        final Integer originatorId = LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId);

        final HashMap<String, Object> originator = LoanOriginatorHelper.getOriginatorById(requestSpec, responseSpec, originatorId);

        assertNotNull(originator);
        assertEquals(originatorId, originator.get("id"));
        assertEquals(externalId, originator.get("externalId"));

        // Cleanup
        LoanOriginatorHelper.deleteOriginator(requestSpec, responseSpec, originatorId);
    }

    @Test
    public void testRetrieveOriginatorByExternalId() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();
        final Integer originatorId = LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId);

        final HashMap<String, Object> originator = LoanOriginatorHelper.getOriginatorByExternalId(requestSpec, responseSpec, externalId);

        assertNotNull(originator);
        assertEquals(originatorId, originator.get("id"));
        assertEquals(externalId, originator.get("externalId"));

        // Cleanup
        LoanOriginatorHelper.deleteOriginator(requestSpec, responseSpec, originatorId);
    }

    @Test
    public void testRetrieveAllOriginators() {
        final String externalId1 = LoanOriginatorHelper.generateUniqueExternalId();
        final String externalId2 = LoanOriginatorHelper.generateUniqueExternalId();

        final Integer originatorId1 = LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId1);
        final Integer originatorId2 = LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId2);

        final List<HashMap<String, Object>> originators = LoanOriginatorHelper.getAllOriginators(requestSpec, responseSpec);

        assertNotNull(originators);
        assertTrue(originators.size() >= 2, "Should have at least 2 originators");

        // Cleanup
        LoanOriginatorHelper.deleteOriginator(requestSpec, responseSpec, originatorId1);
        LoanOriginatorHelper.deleteOriginator(requestSpec, responseSpec, originatorId2);
    }

    @Test
    public void testUpdateOriginatorPartially() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();
        final Integer originatorId = LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId, "Original Name", "ACTIVE",
                null, null);

        // Update only the name
        final HashMap<String, Object> updateResult = LoanOriginatorHelper.updateOriginator(requestSpec, responseSpec, originatorId,
                "Updated Name", null, null, null);

        assertNotNull(updateResult);
        assertTrue(updateResult.containsKey("changes"), "Response should contain changes");

        // Verify update
        final HashMap<String, Object> originator = LoanOriginatorHelper.getOriginatorById(requestSpec, responseSpec, originatorId);
        assertEquals("Updated Name", originator.get("name"));
        assertEquals("ACTIVE", originator.get("status"), "Status should remain unchanged");

        // Cleanup
        LoanOriginatorHelper.deleteOriginator(requestSpec, responseSpec, originatorId);
    }

    @Test
    public void testUpdateOriginatorByExternalId() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();
        LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId, "Original Name", "ACTIVE", null, null);

        // Update by external ID
        final HashMap<String, Object> updateResult = LoanOriginatorHelper.updateOriginatorByExternalId(requestSpec, responseSpec,
                externalId, "Updated via ExternalId", null, null, null);

        assertNotNull(updateResult);

        // Verify update
        final HashMap<String, Object> originator = LoanOriginatorHelper.getOriginatorByExternalId(requestSpec, responseSpec, externalId);
        assertEquals("Updated via ExternalId", originator.get("name"));

        // Cleanup
        LoanOriginatorHelper.deleteOriginatorByExternalId(requestSpec, responseSpec, externalId);
    }

    @Test
    public void testDeleteOriginator() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();
        final Integer originatorId = LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId);

        // Delete
        final Integer deletedId = LoanOriginatorHelper.deleteOriginator(requestSpec, responseSpec, originatorId);
        assertEquals(originatorId, deletedId);

        // Verify deleted - should return 404
        LoanOriginatorHelper.getOriginatorById(requestSpec, responseSpec404, originatorId);
    }

    @Test
    public void testDeleteOriginatorByExternalId() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();
        final Integer originatorId = LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId);

        // Delete by external ID
        final Integer deletedId = LoanOriginatorHelper.deleteOriginatorByExternalId(requestSpec, responseSpec, externalId);
        assertEquals(originatorId, deletedId);

        // Verify deleted - should return 404
        LoanOriginatorHelper.getOriginatorByExternalId(requestSpec, responseSpec404, externalId);
    }

    // ==================== VALIDATION ERROR TESTS ====================

    @Test
    public void testCreateOriginatorWithMissingExternalId() {
        final String invalidJson = "{ \"name\": \"Test\" }";

        LoanOriginatorHelper.createOriginatorExpectingError(requestSpec, responseSpec400, invalidJson);
    }

    @Test
    public void testCreateOriginatorWithDuplicateExternalId() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();

        // Create first originator
        final Integer originatorId = LoanOriginatorHelper.createOriginator(requestSpec, responseSpec, externalId);

        // Attempt to create duplicate - should return 403
        final String duplicateJson = "{ \"externalId\": \"" + externalId + "\" }";
        LoanOriginatorHelper.createOriginatorExpectingError(requestSpec, responseSpec403, duplicateJson);

        // Cleanup
        LoanOriginatorHelper.deleteOriginator(requestSpec, responseSpec, originatorId);
    }

    @Test
    public void testCreateOriginatorWithInvalidStatus() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();
        final String invalidJson = "{ \"externalId\": \"" + externalId + "\", \"status\": \"INVALID\" }";

        LoanOriginatorHelper.createOriginatorExpectingError(requestSpec, responseSpec403, invalidJson);
    }

    @Test
    public void testGetOriginatorByNonExistentId() {
        LoanOriginatorHelper.getOriginatorById(requestSpec, responseSpec404, 999999);
    }

    @Test
    public void testGetOriginatorByNonExistentExternalId() {
        LoanOriginatorHelper.getOriginatorByExternalId(requestSpec, responseSpec404, "NON-EXISTENT-EXTERNAL-ID");
    }

    @Test
    public void testUpdateNonExistentOriginator() {
        final String json = "{ \"name\": \"Updated\" }";
        Utils.performServerPut(requestSpec, responseSpec404, "/fineract-provider/api/v1/loan-originators/999999?" + Utils.TENANT_IDENTIFIER,
                json, "");
    }

    @Test
    public void testDeleteNonExistentOriginator() {
        Utils.performServerDelete(requestSpec, responseSpec404,
                "/fineract-provider/api/v1/loan-originators/999999?" + Utils.TENANT_IDENTIFIER, "");
    }

    // ==================== CODE VALUE VALIDATION TESTS ====================

    @Test
    public void testCreateOriginatorWithInvalidCodeValueId() {
        final String externalId = LoanOriginatorHelper.generateUniqueExternalId();
        // Use an ID that doesn't exist
        final String json = "{ \"externalId\": \"" + externalId + "\", \"originatorTypeId\": 999999 }";

        LoanOriginatorHelper.createOriginatorExpectingError(requestSpec, responseSpec404, json);
    }
}
