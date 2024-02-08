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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.api.MutableUriInfo;
import org.apache.fineract.infrastructure.dataqueries.api.DatatablesApiResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests {GetDatatableEntryByAppTableIdCommandStrategy}.
 */
public class GetDatatableEntryByAppTableIdCommandStrategyTest {

    /**
     * Query parameter provider.
     *
     * @return the test data stream
     */
    private static Stream<Arguments> provideQueryParameters() {
        return Stream.of(Arguments.of(null, 0), Arguments.of("genericResultSet=true", 1));
    }

    /**
     * Test {@link GetDatatableEntryByAppTableIdCommandStrategy#execute} happy path scenario.
     *
     * @param queryParameter
     *            the query parameter
     * @param numberOfQueryParams
     *            number of query params are provided
     */
    @ParameterizedTest
    @MethodSource("provideQueryParameters")
    public void testExecuteSuccessScenario(final String queryParameter, final int numberOfQueryParams) {
        final TestContext testContext = new TestContext();

        final Long loanId = RandomUtils.nextLong();
        final String datatableName = "dt_loan_xyz";
        final BatchRequest request = getBatchRequest(loanId, queryParameter, datatableName);
        final String responseBody = "{\\\"columnHeaders\\\":[{}],\\\"data\\\":\\\"{}\\\"}";

        given(testContext.dataTableApiResource.getDatatable(eq(datatableName), eq(loanId), eq(null), any(UriInfo.class)))
                .willReturn(responseBody);

        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(responseBody, response.getBody());

        verify(testContext.dataTableApiResource).getDatatable(eq(datatableName), eq(loanId), eq(null), testContext.uriInfoCaptor.capture());
        MutableUriInfo mutableUriInfo = testContext.uriInfoCaptor.getValue();
        assertThat(mutableUriInfo.getAdditionalQueryParameters()).hasSize(numberOfQueryParams);
        if (numberOfQueryParams > 0) {
            List<String> param = mutableUriInfo.getAdditionalQueryParameters().get("genericResultSet");
            assertEquals(param.get(0), "true");
        }
    }

    /**
     * Creates and returns a request with the given loan id.
     *
     * @param loanId
     *            the loan id
     * @param queryParameter
     *            the query parameter
     * @param datatableName
     *            the datatable name
     * @return the {@link BatchRequest}
     */
    private BatchRequest getBatchRequest(final Long loanId, final String queryParameter, final String datatableName) {
        final BatchRequest br = new BatchRequest();
        String relativeUrl = String.format("datatables/%s/%s", datatableName, loanId);
        if (queryParameter != null) {
            relativeUrl = relativeUrl + "?" + queryParameter;
        }

        br.setRequestId(RandomUtils.nextLong());
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.GET);
        br.setReference(RandomUtils.nextLong());
        br.setBody("{}");

        return br;
    }

    /**
     * Private test context class used since testng runs in parallel to avoid state between tests
     */
    private static final class TestContext {

        /**
         * The subject under test.
         */
        private final GetDatatableEntryByAppTableIdCommandStrategy subjectToTest;

        /**
         * Mock of {@link UriInfo}
         */
        @Mock
        private UriInfo uriInfo;

        /**
         * Captor of {@link MutableUriInfo}.
         */
        @Captor
        private ArgumentCaptor<MutableUriInfo> uriInfoCaptor;

        /**
         * {@link DatatablesApiResource} mock.
         */
        @Mock
        private DatatablesApiResource dataTableApiResource;

        /**
         * Constructor.
         */
        private TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new GetDatatableEntryByAppTableIdCommandStrategy(dataTableApiResource);
        }
    }
}
