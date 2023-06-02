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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.api.MutableUriInfo;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link GetLoanByExternalIdCommandStrategy}.
 */
public class GetLoanByExternalIdCommandStrategyTest {

    /**
     * The query parameter provider.
     *
     * @return Arguments.
     */
    private static Stream<Arguments> provideQueryParameters() {
        return Stream.of(Arguments.of(null, null, null, null, 0), Arguments.of("all", null, null, null, 1),
                Arguments.of("repaymentSchedule,transactions", null, "guarantors,futureSchedule", "true", 3),
                Arguments.of("repaymentSchedule,transactions", "id,principal,annualInterestRate", null, "false", 3),
                Arguments.of("repaymentSchedule,transactions", "id,principal,annualInterestRate", "guarantors,futureSchedule", "false", 4));
    }

    /**
     * Test {@link GetLoanByExternalIdCommandStrategy#execute} happy path scenario.
     *
     */
    @ParameterizedTest
    @MethodSource("provideQueryParameters")
    public void testExecuteSuccessScenario(final String associations, final String fields, final String exclude,
            final String staffInSelectedOfficeOnlyFlag, final int noOfQueryParams) {
        // given
        final TestContext testContext = new TestContext();

        final String loanExternalId = UUID.randomUUID().toString();
        final BatchRequest request = getBatchRequest(loanExternalId, associations, exclude, fields, staffInSelectedOfficeOnlyFlag);
        final Boolean staffInSelectedOfficeOnlyBooleanFlag = BooleanUtils.toBoolean(staffInSelectedOfficeOnlyFlag);
        final String responseBody = "{\\\"id\\\":2,\\\"accountNo\\\":\\\"000000002\\\"}";

        given(testContext.loansApiResource.retrieveLoan(eq(loanExternalId), eq(staffInSelectedOfficeOnlyBooleanFlag), eq(associations),
                eq(exclude), eq(fields), any(UriInfo.class))).willReturn(responseBody);

        // when
        final BatchResponse response = testContext.underTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(responseBody, response.getBody());

        verify(testContext.loansApiResource).retrieveLoan(eq(loanExternalId), eq(staffInSelectedOfficeOnlyBooleanFlag), eq(associations),
                eq(exclude), eq(fields), testContext.uriInfoCaptor.capture());
        final MutableUriInfo mutableUriInfo = testContext.uriInfoCaptor.getValue();
        assertEquals(noOfQueryParams, mutableUriInfo.getAdditionalQueryParameters().size());
    }

    /**
     * Creates and returns a request with the given loan external id.
     *
     * @param loanExternalId
     *            the loan external id
     * @param associations
     *            the associations query param
     * @param exclude
     *            exclude query param
     * @param fields
     *            fields query param
     * @param staffInSelectedOfficeOnlyFlag
     *            staff in selected office only query param
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final String loanExternalId, final String associations, final String exclude, final String fields,
            final String staffInSelectedOfficeOnlyFlag) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = "loans/external-id/" + loanExternalId;

        Set<String> queryParams = new HashSet<>();
        if (associations != null) {
            queryParams.add("associations=" + associations);
        }
        if (exclude != null) {
            queryParams.add("exclude=" + exclude);
        }
        if (fields != null) {
            queryParams.add("fields=" + fields);
        }
        if (staffInSelectedOfficeOnlyFlag != null) {
            queryParams.add("staffInSelectedOfficeOnly=" + staffInSelectedOfficeOnlyFlag);
        }
        if (!queryParams.isEmpty()) {
            relativeUrl = relativeUrl + "?" + String.join("&", queryParams);
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

        /**
         * The Mock UriInfo
         */
        @Mock
        private UriInfo uriInfo;

        /**
         * The Mock {@link LoansApiResource}
         */
        @Mock
        private LoansApiResource loansApiResource;

        /**
         * The Captor for UriInfo
         */
        @Captor
        private ArgumentCaptor<MutableUriInfo> uriInfoCaptor;

        /**
         * The class under test.
         */
        private final GetLoanByExternalIdCommandStrategy underTest;

        TestContext() {
            MockitoAnnotations.openMocks(this);
            underTest = new GetLoanByExternalIdCommandStrategy(loansApiResource);
        }
    }
}
