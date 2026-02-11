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
package org.apache.fineract.batch.command.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.accounting.journalentry.api.JournalEntriesApiResource;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link CreateJournalEntryCommandStrategy}.
 */
public class CreateJournalEntryCommandStrategyTest {

    /**
     * Test {@link CreateJournalEntryCommandStrategy#execute} happy path scenario for standard journal entry creation.
     */
    @Test
    public void testExecuteSuccessScenarioStandardCreate() {
        final TestContext testContext = new TestContext();
        final BatchRequest batchRequest = getBatchRequest(null);
        final String responseBody = "myResponseBody";

        when(testContext.journalEntriesApiResource.createGLJournalEntry(batchRequest.getBody(), null)).thenReturn(responseBody);

        BatchResponse batchResponse = testContext.subjectToTest.execute(batchRequest, testContext.uriInfo);

        assertEquals(HttpStatus.SC_OK, batchResponse.getStatusCode());
        assertSame(responseBody, batchResponse.getBody());
        assertEquals(batchRequest.getRequestId(), batchResponse.getRequestId());
        assertEquals(batchRequest.getHeaders(), batchResponse.getHeaders());

        verify(testContext.journalEntriesApiResource).createGLJournalEntry(batchRequest.getBody(), null);
    }

    /**
     * Test {@link CreateJournalEntryCommandStrategy#execute} happy path scenario with command parameter.
     */
    @Test
    public void testExecuteSuccessScenarioWithCommand() {
        final TestContext testContext = new TestContext();
        final String command = "updateRunningBalance";
        final BatchRequest batchRequest = getBatchRequest(command);
        final String responseBody = "myResponseBody";

        when(testContext.journalEntriesApiResource.createGLJournalEntry(batchRequest.getBody(), command)).thenReturn(responseBody);

        BatchResponse batchResponse = testContext.subjectToTest.execute(batchRequest, testContext.uriInfo);

        assertEquals(HttpStatus.SC_OK, batchResponse.getStatusCode());
        assertSame(responseBody, batchResponse.getBody());
        assertEquals(batchRequest.getRequestId(), batchResponse.getRequestId());
        assertEquals(batchRequest.getHeaders(), batchResponse.getHeaders());

        verify(testContext.journalEntriesApiResource).createGLJournalEntry(batchRequest.getBody(), command);
    }

    /**
     * Test {@link CreateJournalEntryCommandStrategy#execute} happy path scenario with defineOpeningBalance command.
     */
    @Test
    public void testExecuteSuccessScenarioDefineOpeningBalance() {
        final TestContext testContext = new TestContext();
        final String command = "defineOpeningBalance";
        final BatchRequest batchRequest = getBatchRequest(command);
        final String responseBody = "myResponseBody";

        when(testContext.journalEntriesApiResource.createGLJournalEntry(batchRequest.getBody(), command)).thenReturn(responseBody);

        BatchResponse batchResponse = testContext.subjectToTest.execute(batchRequest, testContext.uriInfo);

        assertEquals(HttpStatus.SC_OK, batchResponse.getStatusCode());
        assertSame(responseBody, batchResponse.getBody());
        assertEquals(batchRequest.getRequestId(), batchResponse.getRequestId());
        assertEquals(batchRequest.getHeaders(), batchResponse.getHeaders());

        verify(testContext.journalEntriesApiResource).createGLJournalEntry(batchRequest.getBody(), command);
    }

    /**
     * Creates and returns a request with the optional command parameter.
     *
     * @param command
     *            the command parameter (can be null)
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final String command) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = "v1/journalentries";
        if (command != null) {
            relativeUrl += "?command=" + command;
        }

        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.POST);
        br.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setBody("{}");

        return br;
    }

    /**
     * Private test context class used since testng runs in parallel to avoid state between tests
     */
    private static class TestContext {

        /**
         * Mock URI info.
         */
        @Mock
        private UriInfo uriInfo;

        /**
         * Mock journal entries API resource.
         */
        @Mock
        private JournalEntriesApiResource journalEntriesApiResource;

        /**
         * The {@link CreateJournalEntryCommandStrategy} under test.
         */
        private final CreateJournalEntryCommandStrategy subjectToTest;

        /**
         * Constructor.
         */
        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new CreateJournalEntryCommandStrategy(journalEntriesApiResource);
        }
    }
}
