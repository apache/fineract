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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriInfo;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link AdjustLoanTransactionByExternalIdCommandStrategy}.
 */
public class AdjustLoanTransactionByExternalIdCommandStrategyTest {

    /**
     * Test {@link AdjustLoanTransactionByExternalIdCommandStrategy#execute} happy path scenario.
     */
    @Test
    public void testExecuteWithoutCommandSuccessScenario() {
        // given
        final TestContext testContext = new TestContext();

        final String loanExternalId = UUID.randomUUID().toString();
        final String transactionExternalId = UUID.randomUUID().toString();
        final BatchRequest request = getBatchRequest(loanExternalId, transactionExternalId, null);
        final String responseBody = "{\"officeId\":1,\"clientId\":107,\"loanId\":71,\"resourceId\":193,\"changes\""
                + ":{\"transactionDate\":\"03 October 2022\",\"transactionAmount\":\"500\",\"locale\":\"en\",\"dateFormat\":"
                + "\"dd MMMM yyyy\",\"paymentTypeId\":\"\"}}";

        given(testContext.loanTransactionsApiResource.adjustLoanTransaction(eq(loanExternalId), eq(transactionExternalId),
                eq(request.getBody()), eq(null))).willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(responseBody, response.getBody());
        verify(testContext.loanTransactionsApiResource).adjustLoanTransaction(eq(loanExternalId), eq(transactionExternalId),
                eq(request.getBody()), isNull());
    }

    /**
     * Test {@link AdjustLoanTransactionByExternalIdCommandStrategy#execute} happy path scenario.
     */
    @Test
    public void testExecuteWithCommandSuccessScenario() {
        // given
        final TestContext testContext = new TestContext();

        final String loanExternalId = UUID.randomUUID().toString();
        final String transactionExternalId = UUID.randomUUID().toString();
        final BatchRequest request = getBatchRequest(loanExternalId, transactionExternalId, "chargeback");
        final String responseBody = "{\"officeId\":1,\"clientId\":107,\"loanId\":71,\"resourceId\":193,\"changes\""
                + ":{\"transactionDate\":\"03 October 2022\",\"transactionAmount\":\"500\",\"locale\":\"en\",\"dateFormat\":"
                + "\"dd MMMM yyyy\",\"paymentTypeId\":\"\"}}";

        given(testContext.loanTransactionsApiResource.adjustLoanTransaction(eq(loanExternalId), eq(transactionExternalId),
                eq(request.getBody()), eq("chargeback"))).willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(responseBody, response.getBody());
        verify(testContext.loanTransactionsApiResource).adjustLoanTransaction(eq(loanExternalId), eq(transactionExternalId),
                eq(request.getBody()), eq("chargeback"));
    }

    /**
     * Creates and returns a request with the given loan id and transaction id.
     *
     * @param loanExternalId
     *            the loan id
     * @param transactionExternalId
     *            the transaction id
     * @param transactionCommand
     *            the optional transaction command
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final String loanExternalId, final String transactionExternalId, final String transactionCommand) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = String.format("loans/external-id/%s/transactions/external-id/%s", loanExternalId, transactionExternalId);

        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl(relativeUrl);
        if (StringUtils.isNotBlank(transactionCommand)) {
            br.setRelativeUrl(br.getRelativeUrl() + String.format("?command=%s", transactionCommand));
        }
        br.setMethod(HttpMethod.POST);
        br.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setBody("{\"locale\":\"en\",\"dateFormat\":\"dd MMMM yyyy\",\"transactionDate\":\"03 October 2022\",\"transactionAmount\":500}");

        return br;
    }

    /**
     * Private test context class used since testng runs in parallel to avoid state between tests
     */
    private static class TestContext {

        /**
         * The Mock UriInfo
         */
        @Mock
        private UriInfo uriInfo;

        /**
         * The Mock {@link LoanTransactionsApiResource}
         */
        @Mock
        private LoanTransactionsApiResource loanTransactionsApiResource;

        /**
         * The class under test.
         */
        private final AdjustLoanTransactionByExternalIdCommandStrategy subjectToTest;

        /**
         * Constructor.
         */
        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new AdjustLoanTransactionByExternalIdCommandStrategy(loanTransactionsApiResource);
        }
    }
}
