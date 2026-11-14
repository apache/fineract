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
package org.apache.fineract.portfolio.shareproducts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.products.service.ShareProductReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountTransactionData;
import org.apache.fineract.portfolio.shareaccounts.domain.PurchasedSharesStatusType;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountReadPlatformService;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductDividendPayOutDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShareProductDividendAssemblerTest {

    private static final Long PRODUCT_ID = 1L;
    private static final LocalDate START = LocalDate.of(2026, 7, 1);
    private static final LocalDate END = LocalDate.of(2026, 8, 1);
    private static final CurrencyData CURRENCY = new CurrencyData("USD", 2, 0);

    @Mock
    private ShareProductReadPlatformService productReadService;
    @Mock
    private ShareAccountReadPlatformService accountReadService;
    @InjectMocks
    private ShareProductDividendAssembler assembler;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "dividend-test", "Dividend Test", "UTC", null));
        MoneyHelper.initializeTenantRoundingMode("dividend-test", RoundingMode.HALF_UP.ordinal());
    }

    @AfterEach
    void tearDown() {
        MoneyHelper.clearCacheForTenant("dividend-test");
        ThreadLocalContextUtil.reset();
    }

    @Test
    void longstandingHoldingQualifiesForWindowShorterThanMinimumPeriod() {
        givenAccounts(90, account(1L, purchase(LocalDate.of(2026, 1, 1), 100)));

        assertAllocations(calculate(), "100.00");
    }

    @Test
    void holdingExactlyMinimumAgeQualifies() {
        givenAccounts(90, account(1L, purchase(END.minusDays(90), 100)));

        assertAllocations(calculate(), "100.00");
    }

    @Test
    void holdingOneDayBelowMinimumAgeDoesNotQualify() {
        givenAccounts(90, account(1L, purchase(END.minusDays(89), 100)));

        assertThat(calculate()).isNull();
    }

    @Test
    void olderPurchaseDatesDoNotIncreaseInWindowWeights() {
        givenAccounts(90, account(1L, purchase(START.minusYears(1), 100)), account(2L, purchase(LocalDate.of(2026, 1, 1), 100)));

        assertAllocations(calculate(), "50.00", "50.00");
    }

    @Test
    void recentPurchaseDoesNotDiluteEligibleHolding() {
        givenAccounts(90, account(1L, purchase(START.minusDays(100), 100), purchase(END.minusDays(10), 900)),
                account(2L, purchase(START.minusDays(100), 100)));

        assertAllocations(calculate(), "50.00", "50.00");
    }

    @Test
    void eligibleInWindowPurchaseContributesFromPurchaseDate() {
        givenAccounts(10, account(1L, purchase(START.minusDays(100), 100)), account(2L, purchase(START.plusDays(11), 100)));

        assertAllocations(calculate(), "60.78", "39.22");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 0 })
    void absentOrZeroMinimumPreservesWeighting(Integer minimumActivePeriod) {
        givenAccounts(minimumActivePeriod, account(1L, purchase(START.minusDays(100), 100)),
                account(2L, purchase(START.plusDays(11), 100)));

        assertAllocations(calculate(), "60.78", "39.22");
    }

    private void givenAccounts(Integer minimumActivePeriod, ShareAccountData... accounts) {
        ShareProductData product = ShareProductData.data(PRODUCT_ID, "Shares", "SHR", null, null, CURRENCY, 10000L, 10000L, BigDecimal.ONE,
                BigDecimal.valueOf(10000), 1L, 100L, 10000L, List.of(), List.of(), false, null, null, minimumActivePeriod, null, null);
        when(productReadService.retrieveOne(PRODUCT_ID, false)).thenReturn(product);
        when(accountReadService.retrieveAllShareAccountDataForDividends(PRODUCT_ID, false, START)).thenReturn(List.of(accounts));
    }

    private ShareProductDividendPayOutDetails calculate() {
        return assembler.calculateDividends(PRODUCT_ID, new BigDecimal("100.00"), START, END);
    }

    private static void assertAllocations(ShareProductDividendPayOutDetails payout, String... amounts) {
        assertThat(payout).isNotNull();
        assertThat(payout.getAccountDividendDetails()).hasSize(amounts.length);
        for (int i = 0; i < amounts.length; i++) {
            assertThat(payout.getAccountDividendDetails().get(i).getAmount()).isEqualByComparingTo(amounts[i]);
        }
    }

    private static ShareAccountData account(Long id, ShareAccountTransactionData... transactions) {
        return new ShareAccountData(id, "SHARE-" + id, null, null, null, null, null, PRODUCT_ID, "Shares", null, null, CURRENCY, null,
                List.of(), List.of(transactions), null, null, null, null, false);
    }

    private static ShareAccountTransactionData purchase(LocalDate date, long shares) {
        PurchasedSharesStatusType status = PurchasedSharesStatusType.APPROVED;
        PurchasedSharesStatusType type = PurchasedSharesStatusType.PURCHASED;
        return new ShareAccountTransactionData(1L, 1L, date, shares, BigDecimal.ONE,
                new EnumOptionData(status.getValue().longValue(), status.getCode(), status.name()),
                new EnumOptionData(type.getValue().longValue(), type.getCode(), type.name()), BigDecimal.valueOf(shares), BigDecimal.ZERO,
                BigDecimal.valueOf(shares), null);
    }
}
