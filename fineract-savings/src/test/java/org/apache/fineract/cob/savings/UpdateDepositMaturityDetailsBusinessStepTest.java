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

import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.service.DepositAccountWritePlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateDepositMaturityDetailsBusinessStepTest {

    private static final Long ACCOUNT_ID = 42L;

    @Mock
    private DepositAccountWritePlatformService depositAccountWritePlatformService;
    @Mock
    private SavingsAccount savingsAccount;
    @InjectMocks
    private UpdateDepositMaturityDetailsBusinessStep underTest;

    @BeforeEach
    void setUp() {
        lenient().when(savingsAccount.getId()).thenReturn(ACCOUNT_ID);
    }

    @ParameterizedTest
    @EnumSource(value = DepositAccountType.class, names = { "FIXED_DEPOSIT", "RECURRING_DEPOSIT" })
    void shouldUpdateMaturityDetailsForActiveTermDeposit(DepositAccountType depositAccountType) {
        when(savingsAccount.depositAccountType()).thenReturn(depositAccountType);
        when(savingsAccount.isActive()).thenReturn(true);

        final SavingsAccount result = underTest.execute(savingsAccount);

        verify(depositAccountWritePlatformService).updateMaturityDetails(ACCOUNT_ID, depositAccountType);
        verifyNoMoreInteractions(depositAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldNotUpdateMaturityDetailsForPlainSavingsAccount() {
        when(savingsAccount.depositAccountType()).thenReturn(DepositAccountType.SAVINGS_DEPOSIT);
        when(savingsAccount.isActive()).thenReturn(true);

        final SavingsAccount result = underTest.execute(savingsAccount);

        verifyNoInteractions(depositAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldNotUpdateMaturityDetailsWhenDepositTypeIsNull() {
        when(savingsAccount.depositAccountType()).thenReturn(null);
        when(savingsAccount.isActive()).thenReturn(true);

        underTest.execute(savingsAccount);

        verifyNoInteractions(depositAccountWritePlatformService);
    }

    @ParameterizedTest
    @EnumSource(value = DepositAccountType.class, names = { "FIXED_DEPOSIT", "RECURRING_DEPOSIT" })
    void shouldNotUpdateMaturityDetailsForNonActiveTermDeposit(DepositAccountType depositAccountType) {
        // e.g. a submitted or approved fixed deposit that the COB still feeds to the step
        lenient().when(savingsAccount.depositAccountType()).thenReturn(depositAccountType);
        when(savingsAccount.isActive()).thenReturn(false);

        final SavingsAccount result = underTest.execute(savingsAccount);

        verifyNoInteractions(depositAccountWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldExposeStepNames() {
        assertThat(underTest.getEnumStyledName()).isEqualTo("UPDATE_DEPOSITS_ACCOUNT_MATURITY_DETAILS");
        assertThat(underTest.getHumanReadableName()).isEqualTo("Update deposit accounts maturity details");
    }
}
