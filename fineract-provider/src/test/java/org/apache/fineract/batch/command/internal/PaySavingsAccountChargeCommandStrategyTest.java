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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.savings.api.SavingsAccountChargesApiResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link PaySavingsAccountChargeCommandStrategy}.
 */
public class PaySavingsAccountChargeCommandStrategyTest {

    /**
     * Test {@link PaySavingsAccountChargeCommandStrategy#execute} happy path scenario with paycharge command.
     */
    @Test
    public void testExecuteWithPayChargeCommandSuccessScenario() {
        // given
        final TestContext testContext = new TestContext();

        final Long savingsAccountId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long savingsAccountChargeId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final String command = "paycharge";
        final BatchRequest request = getBatchRequest(savingsAccountId, savingsAccountChargeId, command);
        final String responseBody = "{\"savingsId\":51,\"resourceId\":47,\"changes\":{}}";

        given(testContext.savingsAccountChargesApiResource.payOrWaiveSavingsAccountCharge(eq(savingsAccountId), eq(savingsAccountChargeId),
                eq(command), eq(request.getBody()))).willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(responseBody, response.getBody());
        verify(testContext.savingsAccountChargesApiResource).payOrWaiveSavingsAccountCharge(eq(savingsAccountId),
                eq(savingsAccountChargeId), eq(command), eq(request.getBody()));
    }

    /**
     * Test {@link PaySavingsAccountChargeCommandStrategy#execute} with waive command.
     */
    @Test
    public void testExecuteWithWaiveCommandSuccessScenario() {
        // given
        final TestContext testContext = new TestContext();

        final Long savingsAccountId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long savingsAccountChargeId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final String command = "waive";
        final BatchRequest request = getBatchRequest(savingsAccountId, savingsAccountChargeId, command);
        final String responseBody = "{\"savingsId\":51,\"resourceId\":47,\"changes\":{}}";

        given(testContext.savingsAccountChargesApiResource.payOrWaiveSavingsAccountCharge(eq(savingsAccountId), eq(savingsAccountChargeId),
                eq(command), eq(request.getBody()))).willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        verify(testContext.savingsAccountChargesApiResource).payOrWaiveSavingsAccountCharge(eq(savingsAccountId),
                eq(savingsAccountChargeId), eq(command), eq(request.getBody()));
    }

    /**
     * Test {@link PaySavingsAccountChargeCommandStrategy#execute} error scenario when no command is present.
     */
    @Test
    public void testExecuteWithoutCommandErrorScenario() {
        // given
        final TestContext testContext = new TestContext();

        final Long savingsAccountId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final Long savingsAccountChargeId = Long.valueOf(RandomStringUtils.randomNumeric(4));

        // URL without ?command=... — should return 501
        final BatchRequest request = new BatchRequest();
        request.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        request.setRelativeUrl(String.format("savingsaccounts/%s/charges/%s", savingsAccountId, savingsAccountChargeId));
        request.setMethod(HttpMethod.POST);
        request.setBody("{\"transactionDate\":\"2026-03-16\",\"amount\":100}");

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        verifyNoInteractions(testContext.savingsAccountChargesApiResource);
    }

    /**
     * Creates and returns a request for the given savings account, charge, and command.
     */
    private BatchRequest getBatchRequest(final Long savingsAccountId, final Long savingsAccountChargeId, final String command) {
        final BatchRequest br = new BatchRequest();
        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl(String.format("savingsaccounts/%s/charges/%s?command=%s", savingsAccountId, savingsAccountChargeId, command));
        br.setMethod(HttpMethod.POST);
        br.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setBody("{\"transactionDate\":\"2026-03-16\",\"amount\":100,\"locale\":\"en\",\"dateFormat\":\"yyyy-MM-dd\"}");
        return br;
    }

    /**
     * Private test context class used since testng runs in parallel to avoid state between tests.
     */
    private static class TestContext {

        @Mock
        private UriInfo uriInfo;

        @Mock
        private SavingsAccountChargesApiResource savingsAccountChargesApiResource;

        private final PaySavingsAccountChargeCommandStrategy subjectToTest;

        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new PaySavingsAccountChargeCommandStrategy(savingsAccountChargesApiResource);
        }
    }
}
