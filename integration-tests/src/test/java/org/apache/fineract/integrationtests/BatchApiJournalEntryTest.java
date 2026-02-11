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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.integrationtests.common.BatchHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for creating journal entries through the Batch API.
 *
 * This test validates that:
 * 1. Journal entries can be created via batch API
 * 2. Command parameters (updateRunningBalance, defineOpeningBalance) work correctly
 * 3. Batch requests with enclosingTransaction execute atomically
 */
public class BatchApiJournalEntryTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    /**
     * Test that a simple journal entry can be created through the batch API.
     */
    @Test
    public void testCreateJournalEntryViaBatchApi() {
        // Create a batch request with a single journal entry
        final List<BatchRequest> batchRequests = new ArrayList<>();

        // Create journal entry request: Debit from GL Account 1 (Asset), Credit to GL Account 2 (Liability)
        final BatchRequest journalEntryRequest = BatchHelper.createJournalEntryRequest(1L, // requestId
                1, // officeId
                "08 January 2026", // transactionDate
                "Test journal entry via batch API", // comments
                "USD", // currencyCode
                "[{\"glAccountId\":1,\"amount\":100}]", // debits
                "[{\"glAccountId\":2,\"amount\":100}]" // credits
        );

        batchRequests.add(journalEntryRequest);

        // Execute batch request
        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);
        final List<BatchResponse> responses = BatchHelper.fromJsonString(
                Utils.performServerPost(this.requestSpec, this.responseSpec, BatchHelper.BATCH_API_URL, jsonifiedRequest), BatchResponse.class);

        // Verify response
        assertEquals(1, responses.size(), "Expected exactly one response");
        final BatchResponse response = responses.get(0);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode(), "Expected HTTP 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
    }

    /**
     * Test creating journal entry with updateRunningBalance command parameter.
     */
    @Test
    public void testCreateJournalEntryWithUpdateRunningBalanceCommand() {
        final List<BatchRequest> batchRequests = new ArrayList<>();

        // Create journal entry request with command parameter
        final BatchRequest journalEntryRequest = BatchHelper.createJournalEntryRequest(1L, // requestId
                1, // officeId
                "08 January 2026", // transactionDate
                "Journal entry with updateRunningBalance", // comments
                "USD", // currencyCode
                "[{\"glAccountId\":1,\"amount\":200}]", // debits
                "[{\"glAccountId\":2,\"amount\":200}]", // credits
                "updateRunningBalance" // command
        );

        batchRequests.add(journalEntryRequest);

        // Execute batch request
        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);
        final List<BatchResponse> responses = BatchHelper.fromJsonString(
                Utils.performServerPost(this.requestSpec, this.responseSpec, BatchHelper.BATCH_API_URL, jsonifiedRequest), BatchResponse.class);

        // Verify response
        assertEquals(1, responses.size(), "Expected exactly one response");
        final BatchResponse response = responses.get(0);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode(), "Expected HTTP 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
    }

    /**
     * Test creating journal entry with defineOpeningBalance command parameter.
     */
    @Test
    public void testCreateJournalEntryWithDefineOpeningBalanceCommand() {
        final List<BatchRequest> batchRequests = new ArrayList<>();

        // Create journal entry request with defineOpeningBalance command
        final BatchRequest journalEntryRequest = BatchHelper.createJournalEntryRequest(1L, // requestId
                1, // officeId
                "01 January 2026", // transactionDate
                "Opening balance journal entry", // comments
                "USD", // currencyCode
                "[{\"glAccountId\":1,\"amount\":1000}]", // debits
                "[{\"glAccountId\":2,\"amount\":1000}]", // credits
                "defineOpeningBalance" // command
        );

        batchRequests.add(journalEntryRequest);

        // Execute batch request
        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);
        final List<BatchResponse> responses = BatchHelper.fromJsonString(
                Utils.performServerPost(this.requestSpec, this.responseSpec, BatchHelper.BATCH_API_URL, jsonifiedRequest), BatchResponse.class);

        // Verify response
        assertEquals(1, responses.size(), "Expected exactly one response");
        final BatchResponse response = responses.get(0);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode(), "Expected HTTP 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
    }

    /**
     * Test atomic transaction with enclosingTransaction=true. This simulates a scenario where multiple operations
     * (e.g., loan repayment and journal entry) must succeed or fail together.
     */
    @Test
    public void testJournalEntryWithEnclosingTransaction() {
        final List<BatchRequest> batchRequests = new ArrayList<>();

        // First request: Create journal entry for security deposit release
        final BatchRequest journalEntry1 = BatchHelper.createJournalEntryRequest(1L, // requestId
                1, // officeId
                "08 January 2026", // transactionDate
                "Release security deposit from liability to cash", // comments
                "USD", // currencyCode
                "[{\"glAccountId\":1,\"amount\":500}]", // debits - Cash account
                "[{\"glAccountId\":2,\"amount\":500}]" // credits - Liability account
        );

        // Second request: Another journal entry that should execute in same transaction
        final BatchRequest journalEntry2 = BatchHelper.createJournalEntryRequest(2L, // requestId
                1, // officeId
                "08 January 2026", // transactionDate
                "Related accounting entry", // comments
                "USD", // currencyCode
                "[{\"glAccountId\":3,\"amount\":500}]", // debits
                "[{\"glAccountId\":4,\"amount\":500}]" // credits
        );

        batchRequests.add(journalEntry1);
        batchRequests.add(journalEntry2);

        // Execute batch request with enclosingTransaction=true for atomicity
        final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);
        final List<BatchResponse> responses = BatchHelper.fromJsonString(Utils.performServerPost(this.requestSpec, this.responseSpec,
                BatchHelper.BATCH_API_URL_EXT, jsonifiedRequest), BatchResponse.class);

        // Verify both requests succeeded
        assertEquals(2, responses.size(), "Expected exactly two responses");
        for (BatchResponse response : responses) {
            assertEquals(HttpStatus.SC_OK, response.getStatusCode(), "Expected HTTP 200 OK for both requests");
            assertNotNull(response.getBody(), "Response body should not be null");
        }
    }
}
