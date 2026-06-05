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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountSubStatusEnum;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateSavingsDormantStatusBusinessStepTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2024, 6, 1);
    private static final Long ACCOUNT_ID = 42L;

    @Mock
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    @Mock
    private SavingsDormancyService savingsDormancyService;
    @Mock
    private SavingsAccount savingsAccount;
    @InjectMocks
    private UpdateSavingsDormantStatusBusinessStep underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
        lenient().when(savingsAccount.getId()).thenReturn(ACCOUNT_ID);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldSetSubStatusInactiveOnInactiveTransition() {
        givenTransition(SavingsAccountSubStatusEnum.INACTIVE);

        final SavingsAccount result = underTest.execute(savingsAccount);

        verify(savingsAccountWritePlatformService).setSubStatusInactive(ACCOUNT_ID);
        verifyNoMoreInteractions(savingsAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldSetSubStatusDormantOnDormantTransition() {
        givenTransition(SavingsAccountSubStatusEnum.DORMANT);

        final SavingsAccount result = underTest.execute(savingsAccount);

        verify(savingsAccountWritePlatformService).setSubStatusDormant(ACCOUNT_ID);
        verifyNoMoreInteractions(savingsAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldEscheatOnEscheatTransition() {
        givenTransition(SavingsAccountSubStatusEnum.ESCHEAT);

        final SavingsAccount result = underTest.execute(savingsAccount);

        verify(savingsAccountWritePlatformService).escheat(ACCOUNT_ID);
        verifyNoMoreInteractions(savingsAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldNotTouchAccountWhenNoTransitionIsDue() {
        givenTransition(null);

        final SavingsAccount result = underTest.execute(savingsAccount);

        verifyNoInteractions(savingsAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldIgnoreUnexpectedTransition() {
        // NONE is not a dormancy transition; the step only logs it
        givenTransition(SavingsAccountSubStatusEnum.NONE);

        underTest.execute(savingsAccount);

        verifyNoInteractions(savingsAccountWritePlatformService);
    }

    @Test
    void shouldExposeStepNames() {
        assertThat(underTest.getEnumStyledName()).isEqualTo("UPDATE_SAVINGS_DORMANT_STATUS");
        assertThat(underTest.getHumanReadableName()).isEqualTo("Update savings dormant status");
    }

    private void givenTransition(SavingsAccountSubStatusEnum transition) {
        // the transition must be derived against the business date 2024-06-01
        when(savingsDormancyService.deriveDormancyTransition(savingsAccount, BUSINESS_DATE)).thenReturn(transition);
    }
}
