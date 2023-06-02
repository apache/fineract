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
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link ModifyLoanApplicationCommandStrategy}
 */
public class ModifyLoanApplicationCommandStrategyTest {

    /**
     * Test {@link ModifyLoanApplicationCommandStrategy#execute(BatchRequest, UriInfo)} happy path scenario.
     */
    @ParameterizedTest
    @MethodSource("commandParamDataProvider")
    public void testExecuteSuccessScenario(final String command, final String responseBody) {
        final TestContext testContext = new TestContext();
        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId, command);

        given(testContext.loansApiResource.modifyLoanApplication(eq(loanId), eq(command), eq(request.getBody()))).willReturn(responseBody);

        final BatchResponse response = testContext.testSubject.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(responseBody, response.getBody());
        verify(testContext.loansApiResource).modifyLoanApplication(eq(loanId), eq(command), eq(request.getBody()));
    }

    /**
     * Command Param data provider
     *
     * @return test data stream
     */
    private static Stream<Arguments> commandParamDataProvider() {
        return Stream.of(
                Arguments.of("markAsFraud", "{\"officeId\":1,\"clientId\":2,\"loanId\":2,\"resourceId\":2,\"changes\":{\"fraud\":true}}"),
                Arguments.of(null, "body"));
    }

    /**
     * Creates and returns a request with the given loan id.
     *
     * @param loanId
     *            the loan id
     * @param queryParameter
     *            the command query param
     * @return {@link BatchRequest}
     */
    private BatchRequest getBatchRequest(final Long loanId, final String queryParameter) {
        final BatchRequest batchRequest = new BatchRequest();

        String relativeUrl = String.format("loans/%s", loanId);

        if (StringUtils.isNotBlank(queryParameter)) {
            relativeUrl = relativeUrl + "?command=" + queryParameter;
        }

        batchRequest.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        batchRequest.setRelativeUrl(relativeUrl);
        batchRequest.setMethod(HttpMethod.PUT);
        batchRequest.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        batchRequest.setBody("{\"fraud\": \"true\"}");
        return batchRequest;
    }

    /**
     * Private test context class used since testng runs in parallel to avoid state between tests
     */
    private static class TestContext {

        /**
         * Mock URI info
         */
        @Mock
        private UriInfo uriInfo;

        /**
         * Mock loans api resource
         */
        @Mock
        private LoansApiResource loansApiResource;

        /**
         * {@link ModifyLoanApplicationCommandStrategy} under test
         */
        private final ModifyLoanApplicationCommandStrategy testSubject;

        /**
         * Constructor
         */
        TestContext() {
            MockitoAnnotations.openMocks(this);
            testSubject = new ModifyLoanApplicationCommandStrategy(loansApiResource);
        }
    }
}
