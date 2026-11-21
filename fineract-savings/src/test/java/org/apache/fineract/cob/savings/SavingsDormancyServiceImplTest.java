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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountSubStatusEnum;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.junit.jupiter.api.Test;

public class SavingsDormancyServiceImplTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2024, 6, 1);
    private static final Long ACCOUNT_ID = 7L;

    private final SavingsAccountTransactionRepository transactionRepository = mock(SavingsAccountTransactionRepository.class);
    private final SavingsDormancyService underTest = new SavingsDormancyServiceImpl(transactionRepository);

    @Test
    public void shouldTransitionFromNoneToInactiveWhenDaysToInactiveElapsed() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, BUSINESS_DATE.minusDays(40), null);
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToInactive()).thenReturn(30L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isEqualTo(SavingsAccountSubStatusEnum.INACTIVE);
    }

    @Test
    public void shouldTransitionFromInactiveToDormantWhenDaysToDormancyElapsed() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.INACTIVE, BUSINESS_DATE.minusDays(70), null);
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToDormancy()).thenReturn(60L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isEqualTo(SavingsAccountSubStatusEnum.DORMANT);
    }

    @Test
    public void shouldTransitionFromDormantToEscheatWhenDaysToEscheatElapsed() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.DORMANT, BUSINESS_DATE.minusDays(100), null);
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToEscheat()).thenReturn(90L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isEqualTo(SavingsAccountSubStatusEnum.ESCHEAT);
    }

    @Test
    public void shouldReturnNullWhenDormancyTrackingIsDisabled() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, BUSINESS_DATE.minusDays(400), null);
        final SavingsProduct product = mock(SavingsProduct.class);
        when(account.savingsProduct()).thenReturn(product);
        when(product.isDormancyTrackingActive()).thenReturn(false);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isNull();
    }

    @Test
    public void shouldReturnNullWhenThresholdNotReached() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, BUSINESS_DATE.minusDays(10), null);
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToInactive()).thenReturn(30L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isNull();
    }

    @Test
    public void shouldReturnNullWhenAccountIsNotActive() {
        final SavingsAccount account = mock(SavingsAccount.class);
        when(account.isActive()).thenReturn(false);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isNull();
    }

    @Test
    public void shouldUseLatestActiveTransactionInsteadOfActivationDate() {
        // Activated long ago, but a recent deposit keeps the account active so no transition should happen.
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, BUSINESS_DATE.minusDays(100),
                BUSINESS_DATE.minusDays(10));
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToInactive()).thenReturn(30L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isNull();
    }

    @Test
    public void shouldCountFromActivationWhenLastActivityPrecedesActivation() {
        // 2024-01-01 activation, last deposit 2023-12-20 (before activation): 152 days since activation on 2024-06-01
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, LocalDate.of(2024, 1, 1),
                LocalDate.of(2023, 12, 20));
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToInactive()).thenReturn(152L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isEqualTo(SavingsAccountSubStatusEnum.INACTIVE);
    }

    @Test
    public void shouldQueryOnlyCustomerActivityTransactionTypes() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, BUSINESS_DATE.minusDays(40), null);
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToInactive()).thenReturn(30L);

        underTest.deriveDormancyTransition(account, BUSINESS_DATE);

        verify(transactionRepository).findLastTransactionDateBySavingsIdAndTypes(ACCOUNT_ID,
                List.of(SavingsAccountTransactionType.DEPOSIT.getValue(), SavingsAccountTransactionType.WITHDRAWAL.getValue()));
    }

    private SavingsAccount activeAccount(final SavingsAccountSubStatusEnum subStatus, final LocalDate activationDate,
            final LocalDate lastActivityDate) {
        final SavingsAccount account = mock(SavingsAccount.class);
        when(account.isActive()).thenReturn(true);
        lenient().when(account.getId()).thenReturn(ACCOUNT_ID);
        when(account.getSubStatus()).thenReturn(subStatus.getValue());
        lenient().when(account.getActivationDate()).thenReturn(activationDate);
        lenient().when(transactionRepository.findLastTransactionDateBySavingsIdAndTypes(eq(ACCOUNT_ID), anyList()))
                .thenReturn(lastActivityDate);
        return account;
    }

    private SavingsProduct dormancyProduct(final SavingsAccount account) {
        final SavingsProduct product = dormancyProductMock();
        when(account.savingsProduct()).thenReturn(product);
        return product;
    }

    private SavingsProduct dormancyProductMock() {
        final SavingsProduct product = mock(SavingsProduct.class);
        lenient().when(product.isDormancyTrackingActive()).thenReturn(true);
        return product;
    }
}
