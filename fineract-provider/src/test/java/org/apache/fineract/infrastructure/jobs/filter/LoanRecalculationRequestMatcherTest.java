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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.fineract.cob.service.InlineLoanCOBExecutorServiceImpl;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.cob.service.RetrieveLoanIdService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.http.BodyCachingHttpServletRequestWrapper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LoanRecalculationRequestMatcherTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = { //
            "POST|/v1/loans/17|command=recalculate", //
            "POST|/v1/loans/17/|command=recalculate", //
            "POST|/v1/loans/external-id/abc-1|command=recalculate", //
            "POST|/v2/loans/17|command=recalculate", //
            "POST|/v1/loans/17|command=Recalculate", //
            "POST|/v1/loans/17|command=%20recalculate", //
            "POST|/v1/loans/17|tenantIdentifier=default&command=recalculate", //
            "POST|/v1/loans/17|command=recalculate&command=disburse" //
    })
    void matchesRecalculationCommand(String method, String path, String query) {
        assertTrue(LoanRecalculationRequestMatcher.isRecalculation(method, path, query));
        assertTrue(LoanRecalculationRequestMatcher.isRecalculation(method, path + "?" + query));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = { //
            // another command, or the recalculation only as a later repetition that JAX-RS ignores
            "POST|/v1/loans/17|command=disburse", //
            "POST|/v1/loans/17|command=disburse&command=recalculate", //
            "POST|/v1/loans/17|commands=recalculate", //
            "POST|/v1/loans/17|command=recalculateLoan", //
            // the same query parameter on other methods or on sub-resources of the loan
            "PUT|/v1/loans/17|command=recalculate", //
            "DELETE|/v1/loans/17|command=recalculate", //
            "POST|/v1/loans/17/transactions|command=recalculate", //
            "POST|/v1/loans/17/charges|command=recalculate", //
            "POST|/v1/loans/external-id/abc-1/transactions|command=recalculate", //
            "POST|/v1/loans/17/../17|command=recalculate", //
            "POST|/v1/rescheduleloans/17|command=recalculate", //
            "POST|/v1/loans|command=recalculate" //
    })
    void doesNotMatchAnythingElse(String method, String path, String query) {
        assertFalse(LoanRecalculationRequestMatcher.isRecalculation(method, path, query));
        assertFalse(LoanRecalculationRequestMatcher.isRecalculation(method, path + "?" + query));
    }

    @Test
    void doesNotMatchWithoutQuery() {
        assertFalse(LoanRecalculationRequestMatcher.isRecalculation("POST", "/v1/loans/17", null));
        assertFalse(LoanRecalculationRequestMatcher.isRecalculation("POST", "/v1/loans/17"));
        assertFalse(LoanRecalculationRequestMatcher.isRecalculation("POST", null));
    }

    @Test
    void loanFiltersLetDirectRecalculationRequestThrough() throws IOException {
        BodyCachingHttpServletRequestWrapper recalculation = request("POST", "/v1/loans/17", "command=recalculate");
        assertFalse(cobHelper().isOnApiList(recalculation));
        assertFalse(modelCheckerHelper().isOnApiList(recalculation));

        BodyCachingHttpServletRequestWrapper disbursement = request("POST", "/v1/loans/17", "command=disburse");
        assertTrue(cobHelper().isOnApiList(disbursement));
        assertTrue(modelCheckerHelper().isOnApiList(disbursement));

        BodyCachingHttpServletRequestWrapper update = request("PUT", "/v1/loans/17", "command=recalculate");
        assertTrue(cobHelper().isOnApiList(update));
        assertTrue(modelCheckerHelper().isOnApiList(update));
    }

    @Test
    void loanFiltersSkipOnlyTheRecalculationItemsOfABatch() throws IOException {
        String onlyRecalculation = """
                [{"requestId": 1, "relativeUrl": "loans/17?command=recalculate", "method": "POST", "body": "{}"}]
                """;
        assertFalse(cobHelper().isOnApiList(batch(onlyRecalculation)));
        assertFalse(modelCheckerHelper().isOnApiList(batch(onlyRecalculation)));

        String mixed = """
                [{"requestId": 1, "relativeUrl": "loans/17?command=recalculate", "method": "POST", "body": "{}"},
                 {"requestId": 2, "relativeUrl": "loans/18/transactions?command=repayment", "method": "POST", "body": "{}"}]
                """;
        assertTrue(cobHelper().isOnApiList(batch(mixed)));
        assertTrue(modelCheckerHelper().isOnApiList(batch(mixed)));
        assertEquals(List.of(18L), cobHelper().calculateRelevantLoanIds(batch(mixed)));
        assertEquals(List.of(18L), modelCheckerHelper().calculateRelevantLoanIds(batch(mixed)));
    }

    private static LoanCOBFilterHelperImpl cobHelper() {
        return new LoanCOBFilterHelperImpl(mock(GLIMAccountInfoRepository.class), mock(LoanAccountLockService.class),
                mock(PlatformSecurityContext.class), mock(InlineLoanCOBExecutorServiceImpl.class), mock(LoanRepository.class),
                mock(FineractProperties.class), mock(RetrieveLoanIdService.class), mock(LoanRescheduleRequestRepository.class));
    }

    private static ProgressiveLoanModelCheckerHelper modelCheckerHelper() {
        return new ProgressiveLoanModelCheckerHelper(mock(GLIMAccountInfoRepository.class), mock(LoanRepository.class),
                mock(LoanRescheduleRequestRepository.class));
    }

    private static BodyCachingHttpServletRequestWrapper request(String method, String path, String query) {
        BodyCachingHttpServletRequestWrapper request = mock(BodyCachingHttpServletRequestWrapper.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getPathInfo()).thenReturn(path);
        when(request.getQueryString()).thenReturn(query);
        return request;
    }

    private static BodyCachingHttpServletRequestWrapper batch(String json) throws IOException {
        BodyCachingHttpServletRequestWrapper request = mock(BodyCachingHttpServletRequestWrapper.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn("/v1/batches");
        when(request.getInputStream()).thenAnswer(invocation -> new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))));
        return request;
    }
}
