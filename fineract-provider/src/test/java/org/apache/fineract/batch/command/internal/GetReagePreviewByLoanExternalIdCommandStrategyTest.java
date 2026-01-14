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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.fineract.portfolio.loanaccount.api.request.ReAgePreviewRequest;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link GetReagePreviewByLoanExternalIdCommandStrategy}.
 */
public class GetReagePreviewByLoanExternalIdCommandStrategyTest {

    /**
     * The query parameter provider.
     *
     * @return Arguments.
     */
    private static Stream<Arguments> provideQueryParameters() {
        return Stream.of(Arguments.of(1, "MONTHS", "2024-01-15", 12, "dd MMMM yyyy", "en"),
                Arguments.of(2, "WEEKS", "2024-02-01", 24, "yyyy-MM-dd", "en_US"),
                Arguments.of(3, "DAYS", "2024-03-10", 6, "dd/MM/yyyy", "en_GB"));
    }

    /**
     * Test {@link GetReagePreviewByLoanExternalIdCommandStrategy#execute} with wrong parameter names.
     */
    @Test
    public void testExecuteWithWrongParameterNames() {
        // given
        final TestContext testContext = new TestContext();

        final String loanExternalId = UUID.randomUUID().toString();
        // Build request with WRONG parameter names
        final BatchRequest request = getBatchRequestWithWrongParameterNames(loanExternalId);

        // Mock LoanScheduleData since it doesn't have a default constructor
        final LoanScheduleData loanScheduleData = mock(LoanScheduleData.class);
        final String responseBody = "{\"periods\":[]}";

        given(testContext.loanTransactionsApiResource.previewReAgeSchedule(eq(loanExternalId), any(ReAgePreviewRequest.class)))
                .willReturn(loanScheduleData);
        given(testContext.toApiJsonSerializer.serialize(eq(loanScheduleData))).willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);

        // Verify the API resource was called
        verify(testContext.loanTransactionsApiResource).previewReAgeSchedule(eq(loanExternalId),
                testContext.reAgePreviewRequestCaptor.capture());

        // Verify the serializer was invoked
        verify(testContext.toApiJsonSerializer).serialize(eq(loanScheduleData));

        // Verify that wrong parameter names result in null values
        final ReAgePreviewRequest capturedRequest = testContext.reAgePreviewRequestCaptor.getValue();
        assertThat(capturedRequest.getFrequencyNumber()).isNull(); // frequencyNo was sent, not frequencyNumber
        assertThat(capturedRequest.getFrequencyType()).isNull(); // freqType was sent, not frequencyType
        assertThat(capturedRequest.getStartDate()).isNull(); // start was sent, not startDate
        assertThat(capturedRequest.getNumberOfInstallments()).isNull(); // installments was sent, not
                                                                        // numberOfInstallments
        assertThat(capturedRequest.getDateFormat()).isNull(); // format was sent, not dateFormat
        assertThat(capturedRequest.getLocale()).isNull(); // lang was sent, not locale
    }

    /**
     * Test {@link GetReagePreviewByLoanExternalIdCommandStrategy#execute} happy path scenario.
     */
    @ParameterizedTest
    @MethodSource("provideQueryParameters")
    public void testExecuteSuccessScenario(final Integer frequencyNumber, final String frequencyType, final String startDate,
            final Integer numberOfInstallments, final String dateFormat, final String locale) {
        // given
        final TestContext testContext = new TestContext();

        final String loanExternalId = UUID.randomUUID().toString();
        final BatchRequest request = getBatchRequest(loanExternalId, frequencyNumber, frequencyType, startDate, numberOfInstallments,
                dateFormat, locale);

        // Mock LoanScheduleData since it doesn't have a default constructor
        final LoanScheduleData loanScheduleData = mock(LoanScheduleData.class);
        final String responseBody = "{\"periods\":[]}";

        given(testContext.loanTransactionsApiResource.previewReAgeSchedule(eq(loanExternalId), any(ReAgePreviewRequest.class)))
                .willReturn(loanScheduleData);
        given(testContext.toApiJsonSerializer.serialize(eq(loanScheduleData))).willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
        assertThat(response.getHeaders()).isEqualTo(request.getHeaders());
        assertThat(response.getBody()).isEqualTo(responseBody);

        // Verify the API resource was called with correct parameters
        verify(testContext.loanTransactionsApiResource).previewReAgeSchedule(eq(loanExternalId),
                testContext.reAgePreviewRequestCaptor.capture());

        // Verify the serializer was invoked with the returned LoanScheduleData
        verify(testContext.toApiJsonSerializer).serialize(eq(loanScheduleData));

        // Verify the ReAgePreviewRequest was built correctly from query parameters
        final ReAgePreviewRequest capturedRequest = testContext.reAgePreviewRequestCaptor.getValue();
        assertThat(capturedRequest.getFrequencyNumber()).isEqualTo(frequencyNumber);
        assertThat(capturedRequest.getFrequencyType()).isEqualTo(frequencyType);
        assertThat(capturedRequest.getStartDate()).isEqualTo(startDate);
        assertThat(capturedRequest.getNumberOfInstallments()).isEqualTo(numberOfInstallments);
        assertThat(capturedRequest.getDateFormat()).isEqualTo(dateFormat);
        assertThat(capturedRequest.getLocale()).isEqualTo(locale);
    }

    /**
     * Creates and returns a batch request with the given parameters.
     *
     * @param loanExternalId
     *            the loan external id
     * @param frequencyNumber
     *            the frequency number
     * @param frequencyType
     *            the frequency type
     * @param startDate
     *            the start date
     * @param numberOfInstallments
     *            the number of installments
     * @param dateFormat
     *            the date format
     * @param locale
     *            the locale
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final String loanExternalId, final Integer frequencyNumber, final String frequencyType,
            final String startDate, final Integer numberOfInstallments, final String dateFormat, final String locale) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = "loans/external-id/" + loanExternalId + "/transactions/reage-preview";

        Set<String> queryParams = new HashSet<>();
        queryParams.add("frequencyNumber=" + frequencyNumber);
        queryParams.add("frequencyType=" + frequencyType);
        queryParams.add("startDate=" + startDate);
        queryParams.add("numberOfInstallments=" + numberOfInstallments);
        queryParams.add("dateFormat=" + dateFormat);
        queryParams.add("locale=" + locale);

        relativeUrl = relativeUrl + "?" + String.join("&", queryParams);

        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setReference(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setBody("{}");

        return br;
    }

    /**
     * Creates and returns a batch request with WRONG parameter names to test validation.
     *
     * @param loanExternalId
     *            the loan external id
     * @return BatchRequest
     */
    private BatchRequest getBatchRequestWithWrongParameterNames(final String loanExternalId) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = "loans/external-id/" + loanExternalId + "/transactions/reage-preview";

        Set<String> queryParams = new HashSet<>();
        // Using wrong parameter names
        queryParams.add("frequencyNo=1"); // should be frequencyNumber
        queryParams.add("freqType=MONTHS"); // should be frequencyType
        queryParams.add("start=2024-01-15"); // should be startDate
        queryParams.add("installments=12"); // should be numberOfInstallments
        queryParams.add("format=dd MMMM yyyy"); // should be dateFormat
        queryParams.add("lang=en"); // should be locale

        relativeUrl = relativeUrl + "?" + String.join("&", queryParams);

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
         * The Mock {@link LoanTransactionsApiResource}
         */
        @Mock
        private LoanTransactionsApiResource loanTransactionsApiResource;

        /**
         * The Mock {@link DefaultToApiJsonSerializer}
         */
        @Mock
        private DefaultToApiJsonSerializer<LoanScheduleData> toApiJsonSerializer;

        /**
         * The Captor for ReAgePreviewRequest
         */
        @Captor
        private ArgumentCaptor<ReAgePreviewRequest> reAgePreviewRequestCaptor;

        /**
         * The class under test.
         */
        private final GetReagePreviewByLoanExternalIdCommandStrategy subjectToTest;

        /**
         * Constructor.
         */
        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new GetReagePreviewByLoanExternalIdCommandStrategy(loanTransactionsApiResource, toApiJsonSerializer);
        }
    }
}
