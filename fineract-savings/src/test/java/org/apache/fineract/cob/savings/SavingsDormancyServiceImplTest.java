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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountSubStatusEnum;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.junit.jupiter.api.Test;

public class SavingsDormancyServiceImplTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2024, 6, 1);

    private final SavingsDormancyService underTest = new SavingsDormancyServiceImpl();

    @Test
    public void shouldTransitionFromNoneToInactiveWhenDaysToInactiveElapsed() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, BUSINESS_DATE.minusDays(40), List.of());
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToInactive()).thenReturn(30L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isEqualTo(SavingsAccountSubStatusEnum.INACTIVE);
    }

    @Test
    public void shouldTransitionFromInactiveToDormantWhenDaysToDormancyElapsed() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.INACTIVE, BUSINESS_DATE.minusDays(70), List.of());
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToDormancy()).thenReturn(60L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isEqualTo(SavingsAccountSubStatusEnum.DORMANT);
    }

    @Test
    public void shouldTransitionFromDormantToEscheatWhenDaysToEscheatElapsed() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.DORMANT, BUSINESS_DATE.minusDays(100), List.of());
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToEscheat()).thenReturn(90L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isEqualTo(SavingsAccountSubStatusEnum.ESCHEAT);
    }

    @Test
    public void shouldReturnNullWhenDormancyTrackingIsDisabled() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, BUSINESS_DATE.minusDays(400), List.of());
        final SavingsProduct product = mock(SavingsProduct.class);
        when(account.savingsProduct()).thenReturn(product);
        when(product.isDormancyTrackingActive()).thenReturn(false);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isNull();
    }

    @Test
    public void shouldReturnNullWhenThresholdNotReached() {
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, BUSINESS_DATE.minusDays(10), List.of());
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
        final SavingsAccountTransaction recentDeposit = activeTransaction(BUSINESS_DATE.minusDays(10));
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, BUSINESS_DATE.minusDays(100),
                List.of(recentDeposit));
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToInactive()).thenReturn(30L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isNull();
    }

    @Test
    public void shouldIgnoreReversedTransactionsWhenComputingLastActivity() {
        final SavingsAccountTransaction reversedDeposit = mock(SavingsAccountTransaction.class);
        lenient().when(reversedDeposit.isDeposit()).thenReturn(true);
        when(reversedDeposit.isNotReversed()).thenReturn(false);
        final SavingsAccount account = activeAccount(SavingsAccountSubStatusEnum.NONE, BUSINESS_DATE.minusDays(100),
                List.of(reversedDeposit));
        final SavingsProduct product = dormancyProduct(account);
        when(product.getDaysToInactive()).thenReturn(30L);

        assertThat(underTest.deriveDormancyTransition(account, BUSINESS_DATE)).isEqualTo(SavingsAccountSubStatusEnum.INACTIVE);
    }

    private SavingsAccount activeAccount(final SavingsAccountSubStatusEnum subStatus, final LocalDate activationDate,
            final List<SavingsAccountTransaction> transactions) {
        final SavingsAccount account = mock(SavingsAccount.class);
        when(account.isActive()).thenReturn(true);
        when(account.getSubStatus()).thenReturn(subStatus.getValue());
        lenient().when(account.getActivationDate()).thenReturn(activationDate);
        lenient().when(account.getTransactions()).thenReturn(transactions);
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

    private SavingsAccountTransaction activeTransaction(final LocalDate transactionDate) {
        final SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        lenient().when(transaction.isDeposit()).thenReturn(true);
        lenient().when(transaction.isNotReversed()).thenReturn(true);
        lenient().when(transaction.isReversalTransaction()).thenReturn(false);
        lenient().when(transaction.getTransactionDate()).thenReturn(transactionDate);
        return transaction;
    }
}
