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
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriInfo;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.loanaccount.api.LoanChargesApiResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link AdjustChargeByChargeExternalIdCommandStrategy}.
 */
public class AdjustChargeByChargeExternalIdCommandStrategyTest {

    /**
     * Test {@link AdjustChargeByChargeExternalIdCommandStrategy#execute} happy path scenario.
     *
     */
    @Test
    public void testExecuteAdjustChargeByExternalIdWithCommandSuccessScenario() {
        // given
        final TestContext testContext = new TestContext();

        final String loanExternalId = UUID.randomUUID().toString();
        final String loanChargeExternalId = UUID.randomUUID().toString();
        final String command = "adjustment";
        final BatchRequest request = getBatchRequest(loanExternalId, loanChargeExternalId, command);
        final String responseBody = "{\"loanId\":13,\"resourceId\":16,\"subResourceId\":26,\"changes\":{\"amount\":10.0,"
                + "\"transactionDate\":[2022,12,7],\"locale\":\"de_DE\"}}";

        given(testContext.loanChargesApiResource.executeLoanCharge(eq(loanExternalId), eq(loanChargeExternalId), eq(command),
                eq(request.getBody()))).willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(responseBody, response.getBody());
        verify(testContext.loanChargesApiResource).executeLoanCharge(eq(loanExternalId), eq(loanChargeExternalId), eq(command),
                eq(request.getBody()));
    }

    /**
     * Test {@link AdjustChargeByChargeExternalIdCommandStrategy#execute} error scenario.
     *
     */
    @Test
    public void testExecuteAdjustChargeByExternalIdWithoutCommandErrorScenario() {
        // given
        final TestContext testContext = new TestContext();

        final String loanExternalId = UUID.randomUUID().toString();
        final String loanChargeExternalId = UUID.randomUUID().toString();
        final BatchRequest request = getBatchRequest(loanExternalId, loanChargeExternalId, null);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals("Resource with method " + request.getMethod() + " and relativeUrl " + request.getRelativeUrl() + " doesn't exist",
                response.getBody());
        verifyNoInteractions(testContext.loanChargesApiResource);
    }

    /**
     * Creates and returns a request with the given loan external id and charge external id.
     *
     * @param loanExternalId
     *            the loan external id
     * @param chargeExternalId
     *            the charge external id
     * @param chargeCommand
     *            the charge command
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final String loanExternalId, final String chargeExternalId, final String chargeCommand) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = String.format("loans/external-id/%s/charges/external-id/%s", loanExternalId, chargeExternalId);

        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl(relativeUrl);
        if (StringUtils.isNotBlank(chargeCommand)) {
            br.setRelativeUrl(br.getRelativeUrl() + String.format("?command=%s", chargeCommand));
        }
        br.setMethod(HttpMethod.POST);
        br.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setBody("{\"amount\":7.00,\"locale\":\"en\"}");

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
         * The Mock {@link LoanChargesApiResource}
         */
        @Mock
        private LoanChargesApiResource loanChargesApiResource;

        /**
         * The class under test.
         */
        private final AdjustChargeByChargeExternalIdCommandStrategy subjectToTest;

        /**
         * Constructor.
         */
        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new AdjustChargeByChargeExternalIdCommandStrategy(loanChargesApiResource);
        }
    }
}
