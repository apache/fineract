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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriInfo;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.workingcapitalloan.api.WorkingCapitalLoanTransactionsApiResource;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link GetWorkingCapitalLoanTransactionByExternalIdCommandStrategy}.
 */
public class GetWorkingCapitalLoanTransactionByExternalIdCommandStrategyTest {

    /**
     * Test {@link GetWorkingCapitalLoanTransactionByExternalIdCommandStrategy#execute} happy path scenario without
     * query params.
     */
    @Test
    public void testExecuteSuccessScenarioWithoutQueryParams() {
        // given
        final TestContext testContext = new TestContext();

        final String loanExternalId = UUID.randomUUID().toString();
        final String transactionExternalId = UUID.randomUUID().toString();
        final BatchRequest request = getBatchRequest(loanExternalId, transactionExternalId, null);
        final WorkingCapitalLoanTransactionData transactionData = WorkingCapitalLoanTransactionData.builder().id(1L).build();
        final String serializedResponse = "{\"id\":1}";

        given(testContext.workingCapitalLoanTransactionsApiResource
                .retrieveTransactionByExternalLoanIdAndTransactionExternalId(eq(loanExternalId), eq(transactionExternalId)))
                .willReturn(transactionData);
        given(testContext.toApiJsonSerializer.serialize(transactionData)).willReturn(serializedResponse);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(serializedResponse, response.getBody());

        verify(testContext.workingCapitalLoanTransactionsApiResource)
                .retrieveTransactionByExternalLoanIdAndTransactionExternalId(eq(loanExternalId), eq(transactionExternalId));
        verify(testContext.toApiJsonSerializer).serialize(transactionData);
    }

    /**
     * Test {@link GetWorkingCapitalLoanTransactionByExternalIdCommandStrategy#execute} happy path scenario with query
     * params.
     */
    @Test
    public void testExecuteSuccessScenarioWithQueryParams() {
        // given
        final TestContext testContext = new TestContext();

        final String loanExternalId = UUID.randomUUID().toString();
        final String transactionExternalId = UUID.randomUUID().toString();
        final BatchRequest request = getBatchRequest(loanExternalId, transactionExternalId, "fields=id");
        final WorkingCapitalLoanTransactionData transactionData = WorkingCapitalLoanTransactionData.builder().id(1L).build();
        final String serializedResponse = "{\"id\":1}";

        given(testContext.workingCapitalLoanTransactionsApiResource
                .retrieveTransactionByExternalLoanIdAndTransactionExternalId(eq(loanExternalId), eq(transactionExternalId)))
                .willReturn(transactionData);
        given(testContext.toApiJsonSerializer.serialize(transactionData)).willReturn(serializedResponse);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(serializedResponse, response.getBody());

        verify(testContext.workingCapitalLoanTransactionsApiResource)
                .retrieveTransactionByExternalLoanIdAndTransactionExternalId(eq(loanExternalId), eq(transactionExternalId));
    }

    /**
     * Creates and returns a batch request for {@code GET
     * working-capital-loans/external-id/{loanExternalId}/transactions/external-id/{transactionExternalId}}.
     *
     * @param loanExternalId
     *            the loan external id
     * @param transactionExternalId
     *            the transaction external id
     * @param queryParams
     *            optional query params string (without leading '?')
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final String loanExternalId, final String transactionExternalId, final String queryParams) {
        final BatchRequest br = new BatchRequest();
        String relativeUrl = "v1/working-capital-loans/external-id/" + loanExternalId + "/transactions/external-id/"
                + transactionExternalId;
        if (queryParams != null) {
            relativeUrl = relativeUrl + "?" + queryParams;
        }
        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setBody("{}");
        return br;
    }

    /**
     * Private test context class used since testng runs in parallel to avoid state between tests.
     */
    private static class TestContext {

        @Mock
        private UriInfo uriInfo;

        @Mock
        private WorkingCapitalLoanTransactionsApiResource workingCapitalLoanTransactionsApiResource;

        @Mock
        private DefaultToApiJsonSerializer<CommandProcessingResult> toApiJsonSerializer;

        private final GetWorkingCapitalLoanTransactionByExternalIdCommandStrategy subjectToTest;

        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new GetWorkingCapitalLoanTransactionByExternalIdCommandStrategy(workingCapitalLoanTransactionsApiResource,
                    toApiJsonSerializer);
        }
    }
}
