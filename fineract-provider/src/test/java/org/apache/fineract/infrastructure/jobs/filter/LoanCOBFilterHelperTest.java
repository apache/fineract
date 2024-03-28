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

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.fineract.cob.loan.RetrieveLoanIdService;
import org.apache.fineract.cob.service.InlineLoanCOBExecutorServiceImpl;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.http.BodyCachingHttpServletRequestWrapper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequestRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanCOBFilterHelperTest {

    @Mock
    private GLIMAccountInfoRepository glimAccountInfoRepository;
    @Mock
    private LoanAccountLockService loanAccountLockService;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private InlineLoanCOBExecutorServiceImpl inlineLoanCOBExecutorService;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private RetrieveLoanIdService retrieveLoanIdService;

    @Mock
    private LoanRescheduleRequestRepository loanRescheduleRequestRepository;

    @InjectMocks
    private LoanCOBFilterHelper helper;

    @BeforeEach
    public void initLoanCOBFilterHelper() throws Exception {
        helper.afterPropertiesSet();
    }

    @Test
    public void testCOBFilterUnescapedChars() throws IOException {
        String json = """
                [
                    {
                        "requestId": 1,
                        "relativeUrl": "clients",
                        "method": "POST",
                        "headers": [
                            {
                                "name": "Idempotency-Key",
                                "value": "{{temp_idempotencyKey1}}"
                            },
                            {
                                "name": "Content-Type",
                                "value": "application/json"
                            },
                            {
                                "name": "Fineract-Platform-TenantId",
                                "value": "{{tenantId}}"
                            },
                            {
                                "name": "Authorization",
                                "value": "Basic bWlmb3M6cGFzc3dvcmQ="
                            }
                        ],
                        "body":"{
                    \\"officeId\\": 1,
                    \\"legalFormId\\": 2,
                    \\"isStaff\\": false,
                    \\"active\\": true,
                    \\"fullname\\": \\"Current Company 1\\",
                    \\"clientNonPersonDetails\\": {
                        \\"constitutionId\\": 1,
                        \\"incorpValidityTillDate\\": \\"\\",
                        \\"incorpNumber\\": \\"\\",
                        \\"mainBusinessLineId\\": \\"\\",
                        \\"remarks\\": \\"\\"
                    },
                    \\"activationDate\\": \\"01 January 2022\\",
                    \\"familyMembers\\": [],
                    \\"dateFormat\\": \\"dd MMMM yyyy\\",
                    \\"locale\\": \\"en\\"
                    }"}
                ]
                """;

        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getPathInfo()).thenReturn("/v1/batches/endpoint");
        BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream inputStream = new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(
                json.getBytes(Charset.forName("UTF-8")));
        Mockito.when(httpServletRequest.getInputStream()).thenReturn(inputStream);
        List<Long> loanIds = helper.calculateRelevantLoanIds(httpServletRequest);
        Assertions.assertEquals(0, loanIds.size());
    }

}
