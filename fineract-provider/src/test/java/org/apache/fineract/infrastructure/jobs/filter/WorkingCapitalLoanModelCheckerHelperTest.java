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
package org.apache.fineract.infrastructure.jobs.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import org.apache.fineract.infrastructure.core.http.BodyCachingHttpServletRequestWrapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The filter only repairs the loans it can identify from the request, so which requests it recognises and which id it
 * pulls out of them is the whole of its behaviour.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanModelCheckerHelperTest {

    @Mock
    private WorkingCapitalLoanRepository loanRepository;

    @InjectMocks
    private WorkingCapitalLoanModelCheckerHelper helper;

    private BodyCachingHttpServletRequestWrapper request(final String method, final String pathInfo) {
        final BodyCachingHttpServletRequestWrapper request = mock(BodyCachingHttpServletRequestWrapper.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getPathInfo()).thenReturn(pathInfo);
        return request;
    }

    @Test
    void aWriteAgainstAWorkingCapitalLoanIsRecognised() throws IOException {
        assertTrue(helper.isOnApiList(request("POST", "/v1/working-capital-loans/17/transactions")));
        assertTrue(helper.isOnApiList(request("PUT", "/v1/working-capital-loans/17/rate")));
        assertTrue(helper.isOnApiList(request("DELETE", "/v1/working-capital-loans/17")));
    }

    @Test
    void aReadIsNotRecognisedBecauseItChangesNothingToPersistTheLossInto() throws IOException {
        assertFalse(helper.isOnApiList(request("GET", "/v1/working-capital-loans/17/amortization-schedule")));
    }

    @Test
    void otherLoanKindsAreLeftToTheirOwnChecker() throws IOException {
        assertFalse(helper.isOnApiList(request("POST", "/v1/loans/17/transactions")));
    }

    /**
     * Catch-up runs the batch itself, which does its own rebuilding; repairing ahead of it would duplicate the work.
     */
    @Test
    void theCatchUpEndpointIsSkipped() throws IOException {
        assertFalse(helper.isOnApiList(request("POST", "/v1/working-capital-loans/catch-up")));
    }

    @Test
    void theLoanIdIsTakenFromThePath() throws IOException {
        assertEquals(List.of(4711L), helper.calculateRelevantLoanIds(request("POST", "/v1/working-capital-loans/4711/transactions")));
    }

    @Test
    void anExternalIdIsResolvedToTheLoanItNames() throws IOException {
        when(loanRepository.findIdByExternalId(any())).thenReturn(4711L);

        assertEquals(List.of(4711L),
                helper.calculateRelevantLoanIds(request("POST", "/v1/working-capital-loans/external-id/abc-123/transactions")));
    }

    @Test
    void aPathCarryingNoUsableIdYieldsNothingRatherThanFailing() throws IOException {
        assertEquals(List.of(), helper.calculateRelevantLoanIds(request("POST", "/v1/working-capital-loans/template")));
    }
}
