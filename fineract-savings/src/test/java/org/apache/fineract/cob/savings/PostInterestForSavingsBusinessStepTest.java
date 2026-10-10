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
package org.apache.fineract.cob.savings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostInterestForSavingsBusinessStepTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2024, 6, 1);

    @Mock
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private SavingsAccount savingsAccount;
    @InjectMocks
    private PostInterestForSavingsBusinessStep underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldPostInterestOnBusinessDateWithPivotConfigEnabled() {
        when(configurationDomainService.retrievePivotDateConfig()).thenReturn(true);

        final SavingsAccount result = underTest.execute(savingsAccount);

        // postInterestAs=false, transaction date = business date 2024-06-01, pivot flag forwarded as is
        verify(savingsAccountWritePlatformService).postInterest(savingsAccount, false, BUSINESS_DATE, true);
        verifyNoMoreInteractions(savingsAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldPostInterestOnBusinessDateWithPivotConfigDisabled() {
        when(configurationDomainService.retrievePivotDateConfig()).thenReturn(false);

        final SavingsAccount result = underTest.execute(savingsAccount);

        verify(savingsAccountWritePlatformService).postInterest(savingsAccount, false, BUSINESS_DATE, false);
        verifyNoMoreInteractions(savingsAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldExposeStepNames() {
        assertThat(underTest.getEnumStyledName()).isEqualTo("POST_INTEREST_FOR_SAVINGS");
        assertThat(underTest.getHumanReadableName()).isEqualTo("Post interest for savings");
    }
}
