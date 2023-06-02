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
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link CreateTransactionByLoanExternalIdCommandStrategy}.
 */
public class CreateTransactionByLoanExternalIdCommandStrategyTest {

    /**
     * Test {@link CreateTransactionByLoanExternalIdCommandStrategy#execute} happy path scenario.
     */
    @Test
    public void testExecuteSuccessScenario() {
        final TestContext testContext = new TestContext();
        final String loanExternalId = UUID.randomUUID().toString();
        final String command = "myCommand";
        final BatchRequest batchRequest = getBatchRequest(loanExternalId, command);
        final String responseBody = "myResponseBody";

        when(testContext.loanTransactionsApiResource.executeLoanTransaction(loanExternalId, command, batchRequest.getBody()))
                .thenReturn(responseBody);

        BatchResponse batchResponse = testContext.subjectToTest.execute(batchRequest, testContext.uriInfo);

        assertEquals(HttpStatus.SC_OK, batchResponse.getStatusCode());
        assertSame(responseBody, batchResponse.getBody());
        assertEquals(batchRequest.getRequestId(), batchResponse.getRequestId());
        assertEquals(batchRequest.getHeaders(), batchResponse.getHeaders());

        verify(testContext.loanTransactionsApiResource).executeLoanTransaction(loanExternalId, command, batchRequest.getBody());
    }

    /**
     * Creates and returns a request with the given loan id and command value.
     *
     * @param loanExternalId
     *            the loan external id
     * @param command
     *            the transaction id
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final String loanExternalId, final String command) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = "loans/external-id/" + loanExternalId + "/transactions?command=" + command;

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
         * Mock loan transactions API resource.
         */
        @Mock
        private LoanTransactionsApiResource loanTransactionsApiResource;

        /**
         * The {@link CreateTransactionByLoanExternalIdCommandStrategy} under test.
         */
        private final CreateTransactionByLoanExternalIdCommandStrategy subjectToTest;

        /**
         * Constructor.
         */
        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new CreateTransactionByLoanExternalIdCommandStrategy(loanTransactionsApiResource);
        }
    }
}
