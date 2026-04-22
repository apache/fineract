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
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link DisburseToSavingsCommandStrategy}.
 */
public class DisburseToSavingsCommandStrategyTest {

    /**
     * Test {@link DisburseToSavingsCommandStrategy#execute} happy path scenario.
     */
    @Test
    public void testExecuteSuccessScenario() {
        // given
        final TestContext testContext = new TestContext();

        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId);
        final String responseBody = "{\"loanId\":" + loanId + ",\"resourceId\":16,\"changes\":{}}";

        given(testContext.loansApiResource.stateTransitions(eq(loanId), eq("disburseToSavings"), eq(request.getBody())))
                .willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(responseBody, response.getBody());
        verify(testContext.loansApiResource).stateTransitions(eq(loanId), eq("disburseToSavings"), eq(request.getBody()));
    }

    /**
     * Test {@link DisburseToSavingsCommandStrategy#execute} with a versioned relative URL (v1/ prefix).
     */
    @Test
    public void testExecuteWithVersionedUrlSuccessScenario() {
        // given
        final TestContext testContext = new TestContext();

        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId);
        request.setRelativeUrl("v1/loans/" + loanId + "?command=disburseToSavings");
        final String responseBody = "{\"loanId\":" + loanId + ",\"resourceId\":17,\"changes\":{}}";

        given(testContext.loansApiResource.stateTransitions(eq(loanId), eq("disburseToSavings"), eq(request.getBody())))
                .willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(responseBody, response.getBody());
        verify(testContext.loansApiResource).stateTransitions(eq(loanId), eq("disburseToSavings"), eq(request.getBody()));
    }

    /**
     * Creates and returns a batch request for the given loan id.
     */
    private BatchRequest getBatchRequest(final Long loanId) {
        final BatchRequest br = new BatchRequest();
        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl(String.format("loans/%s?command=disburseToSavings", loanId));
        br.setMethod(HttpMethod.POST);
        br.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setBody("{\"actualDisbursementDate\":\"01 March 2026\",\"locale\":\"en\",\"dateFormat\":\"dd MMMM yyyy\"}");
        return br;
    }

    /**
     * Private test context class used since testng runs in parallel to avoid state between tests.
     */
    private static class TestContext {

        @Mock
        private UriInfo uriInfo;

        @Mock
        private LoansApiResource loansApiResource;

        private final DisburseToSavingsCommandStrategy subjectToTest;

        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new DisburseToSavingsCommandStrategy(loansApiResource);
        }
    }
}
