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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.workingcapitalloan.api.WorkingCapitalLoanApiResource;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanData;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link GetWorkingCapitalLoanByIdCommandStrategy}.
 */
public class GetWorkingCapitalLoanByIdCommandStrategyTest {

    /**
     * Test {@link GetWorkingCapitalLoanByIdCommandStrategy#execute} happy path scenario without query params.
     */
    @Test
    public void testExecuteSuccessScenarioWithoutQueryParams() {
        // given
        final TestContext testContext = new TestContext();

        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId, null);
        final WorkingCapitalLoanData loanData = WorkingCapitalLoanData.builder().id(loanId).build();
        final String serializedResponse = "{\"id\":" + loanId + "}";

        given(testContext.workingCapitalLoanApiResource.retrieveOne(eq(loanId))).willReturn(loanData);
        given(testContext.toApiJsonSerializer.serialize(loanData)).willReturn(serializedResponse);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(serializedResponse, response.getBody());

        verify(testContext.workingCapitalLoanApiResource).retrieveOne(eq(loanId));
        verify(testContext.toApiJsonSerializer).serialize(loanData);
    }

    /**
     * Test {@link GetWorkingCapitalLoanByIdCommandStrategy#execute} happy path scenario with query params.
     */
    @Test
    public void testExecuteSuccessScenarioWithQueryParams() {
        // given
        final TestContext testContext = new TestContext();

        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId, "associations=all");
        final WorkingCapitalLoanData loanData = WorkingCapitalLoanData.builder().id(loanId).build();
        final String serializedResponse = "{\"id\":" + loanId + "}";

        given(testContext.workingCapitalLoanApiResource.retrieveOne(eq(loanId))).willReturn(loanData);
        given(testContext.toApiJsonSerializer.serialize(loanData)).willReturn(serializedResponse);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(serializedResponse, response.getBody());

        verify(testContext.workingCapitalLoanApiResource).retrieveOne(eq(loanId));
    }

    /**
     * Creates and returns a batch request for {@code GET working-capital-loans/{loanId}}.
     *
     * @param loanId
     *            the loan id
     * @param queryParams
     *            optional query params string (without leading '?')
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final Long loanId, final String queryParams) {
        final BatchRequest br = new BatchRequest();
        String relativeUrl = "v1/working-capital-loans/" + loanId;
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
        private WorkingCapitalLoanApiResource workingCapitalLoanApiResource;

        @Mock
        private DefaultToApiJsonSerializer<CommandProcessingResult> toApiJsonSerializer;

        private final GetWorkingCapitalLoanByIdCommandStrategy subjectToTest;

        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new GetWorkingCapitalLoanByIdCommandStrategy(workingCapitalLoanApiResource, toApiJsonSerializer);
        }
    }
}
