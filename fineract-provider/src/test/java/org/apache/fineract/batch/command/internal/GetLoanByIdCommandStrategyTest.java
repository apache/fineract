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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.api.MutableUriInfo;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GetLoanByIdCommandStrategyTest {

    private static Stream<Arguments> provideQueryParameters() {
        return Stream.of(Arguments.of(null, 0), Arguments.of("associations=all", 1),
                Arguments.of("fields=id,principal,annualInterestRate&associations=repaymentSchedule,transactions", 2));
    }

    /**
     * Test {@link GetLoanByIdCommandStrategy#execute} happy path scenario.
     *
     */
    @ParameterizedTest
    @MethodSource("provideQueryParameters")
    public void testExecuteSuccessScenario(final String queryParameter, final int noOfQueryParams) {
        // given
        final TestContext testContext = new TestContext();

        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId, queryParameter);
        final String responseBody = "{\\\"id\\\":2,\\\"accountNo\\\":\\\"000000002\\\"}";

        given(testContext.loansApiResource.retrieveLoan(eq(loanId), eq(false), any(UriInfo.class))).willReturn(responseBody);

        // when
        final BatchResponse response = testContext.underTest.execute(request, testContext.uriInfo);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
        assertThat(response.getHeaders()).isEqualTo(request.getHeaders());
        assertThat(response.getBody()).isEqualTo(responseBody);

        verify(testContext.loansApiResource).retrieveLoan(eq(loanId), eq(false), testContext.uriInfoCaptor.capture());
        MutableUriInfo mutableUriInfo = testContext.uriInfoCaptor.getValue();
        assertThat(mutableUriInfo.getAdditionalQueryParameters()).hasSize(noOfQueryParams);
    }

    /**
     * Test {@link GetLoanByIdCommandStrategy#execute} for internal server error.
     */
    @Test
    public void testExecuteForInternalServerError() {
        // given
        final TestContext testContext = new TestContext();
        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId, null);

        given(testContext.loansApiResource.retrieveLoan(eq(loanId), eq(false), any(UriInfo.class)))
                .willThrow(new RuntimeException("Some error"));

        // when
        final BatchResponse response = testContext.underTest.execute(request, testContext.uriInfo);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("{\"Exception\": java.lang.RuntimeException: Some error}");
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
        assertThat(response.getHeaders()).isEqualTo(request.getHeaders());
    }

    /**
     * Test {@link GetLoanByIdCommandStrategy#execute} for loan not found exception.
     */
    @Test
    public void testExecuteForLoanNotFoundException() {
        // given
        final TestContext testContext = new TestContext();
        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final BatchRequest request = getBatchRequest(loanId, null);

        given(testContext.loansApiResource.retrieveLoan(eq(loanId), eq(false), any(UriInfo.class)))
                .willThrow(new LoanNotFoundException(loanId));

        // when
        final BatchResponse response = testContext.underTest.execute(request, testContext.uriInfo);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(buildLoanNotFoundError(loanId));
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
        assertThat(response.getHeaders()).isEqualTo(request.getHeaders());
    }

    private String buildLoanNotFoundError(final Long loanId) {
        return String.format(
                "{\n  \"developerMessage\": \"The requested resource is not available.\",\n"
                        + "  \"httpStatusCode\": \"404\",\n  \"defaultUserMessage\": \"The requested resource is not available.\",\n"
                        + "  \"userMessageGlobalisationCode\": \"error.msg.resource.not.found\",\n  \"errors\": [\n    {\n"
                        + "      \"developerMessage\": \"Loan with identifier %s does not exist\",\n"
                        + "      \"defaultUserMessage\": \"Loan with identifier %s does not exist\",\n"
                        + "      \"userMessageGlobalisationCode\": \"error.msg.loan.id.invalid\",\n      \"parameterName\": \"id\",\n"
                        + "      \"args\": [\n        {\n          \"value\": %s\n        }\n      ]\n    }\n  ]\n" + "}",
                loanId, loanId, loanId);
    }

    /**
     * Creates and returns a request with the given loan id.
     *
     * @param loanId
     *            the loan id
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final Long loanId, final String queryParamStr) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = "loans/" + loanId;
        if (queryParamStr != null) {
            relativeUrl = relativeUrl + "?" + queryParamStr;
        }

        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setBody("{}");

        return br;
    }

    /**
     * Private test context class used since testng runs in parallel to avoid state between tests
     */
    private static class TestContext {

        @Mock
        private UriInfo uriInfo;

        @Mock
        private LoansApiResource loansApiResource;

        @Captor
        private ArgumentCaptor<MutableUriInfo> uriInfoCaptor;

        private final GetLoanByIdCommandStrategy underTest;

        TestContext() {
            MockitoAnnotations.openMocks(this);
            underTest = new GetLoanByIdCommandStrategy(loansApiResource);
        }
    }
}
