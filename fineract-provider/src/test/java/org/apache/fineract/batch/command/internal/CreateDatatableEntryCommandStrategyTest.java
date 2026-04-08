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

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.dataqueries.api.DatatablesApiResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for {@link CreateDatatableEntryCommandStrategy}.
 */
public class CreateDatatableEntryCommandStrategyTest {

    /**
     * Test {@link CreateDatatableEntryCommandStrategy#execute} happy path scenario.
     */
    @Test
    public void testExecuteSuccessScenario() {
        // given
        final TestContext testContext = new TestContext();

        final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(4));
        final String datatableName = RandomStringUtils.randomAlphabetic(10);
        final BatchRequest request = getBatchRequest(datatableName, loanId);
        final String responseBody = "{\"resourceId\":193}";

        given(testContext.datatablesApiResource.createDatatableEntry(eq(datatableName), eq(loanId), eq(request.getBody())))
                .willReturn(responseBody);

        // when
        final BatchResponse response = testContext.subjectToTest.execute(request, testContext.uriInfo);

        // then
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(request.getRequestId(), response.getRequestId());
        assertEquals(request.getHeaders(), response.getHeaders());
        assertEquals(responseBody, response.getBody());
    }

    /**
     * Creates and returns a create datatable entry request with the given loan id.
     *
     * @param datatableName
     *            the name of the datatable
     * @param loanId
     *            the loan id
     * @return BatchRequest
     */
    private BatchRequest getBatchRequest(final String datatableName, final Long loanId) {

        final BatchRequest br = new BatchRequest();
        String relativeUrl = String.format("datatables/%s/%s", datatableName, loanId);

        br.setRequestId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        br.setRelativeUrl(relativeUrl);
        br.setMethod(HttpMethod.POST);
        br.setBody("{\"locale\":\"en\",\"dateFormat\":\"dd MMMM yyyy\",\"enabled\":true,\"amount\":500.10}");

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
         * The Mock {@link DatatablesApiResource}
         */
        @Mock
        private DatatablesApiResource datatablesApiResource;

        /**
         * The class under test.
         */
        private final CreateDatatableEntryCommandStrategy subjectToTest;

        /**
         * Constructor.
         */

        TestContext() {
            MockitoAnnotations.openMocks(this);
            subjectToTest = new CreateDatatableEntryCommandStrategy(datatablesApiResource);
        }
    }
}
