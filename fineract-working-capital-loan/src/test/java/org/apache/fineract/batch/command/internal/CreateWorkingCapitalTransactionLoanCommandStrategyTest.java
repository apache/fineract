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
import org.apache.fineract.portfolio.workingcapitalloan.api.WorkingCapitalLoanTransactionsApiResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link CreateWorkingCapitalTransactionLoanCommandStrategy}.
 */
public class CreateWorkingCapitalTransactionLoanCommandStrategyTest {

    /**
     * Test {@link CreateWorkingCapitalTransactionLoanCommandStrategy#execute} happy path scenario.
     */
    @Test
    public void testExecuteSuccessScenario() {
        // given
        final TestContext testContext = new TestContext();

        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final String command = "repayment";
        final BatchRequest request = getBatchRequest(loanId, command);
        final CommandProcessingResult processingResult = CommandProcessingResult.resourceResult(loanId, null);
        final String serializedResponse = "{\"resourceId\":" + loanId + "}";

        given(testContext.workingCapitalLoanTransactionsApiResource.executeLoanTransactionById(eq(loanId), eq(command),
                eq(request.getBody()))).willReturn(processingResult);
        given(testContext.toApiJsonSerializer.serialize(processingResult)).willReturn(serializedResponse);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(serializedResponse, response.getBody());

        verify(testContext.workingCapitalLoanTransactionsApiResource).executeLoanTransactionById(eq(loanId), eq(command),
                eq(request.getBody()));
        verify(testContext.toApiJsonSerializer).serialize(processingResult);
    }

    /**
     * Creates and returns a batch request for {@code POST
     * working-capital-loans/{loanId}/transactions?command={command}}.
     *
     * @param loanId
     *            the loan id
     * @param command
     *            the mandatory command query param
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final Long loanId, final String command) {
        final BatchRequest br = new BatchRequest();
        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl("v1/working-capital-loans/" + loanId + "/transactions?command=" + command);
        br.setMethod(HttpMethod.POST);
        br.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setBody("{\"transactionDate\":\"01 January 2026\",\"transactionAmount\":500,\"locale\":\"en\",\"dateFormat\":\"dd MMMM yyyy\"}");
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

        private final CreateWorkingCapitalTransactionLoanCommandStrategy subjectToTest;

        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new CreateWorkingCapitalTransactionLoanCommandStrategy(workingCapitalLoanTransactionsApiResource,
                    toApiJsonSerializer);
        }
    }
}
