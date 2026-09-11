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
package org.apache.fineract.portfolio.savings.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SavingsAccountTransactionTimeTest {

    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, 0);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 7, 27))));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
        MoneyHelper.clearCache();
    }

    @Test
    void withdrawalWithDateOnlyKeepsTransactionTimeNull() {
        final LocalDate transactionDate = LocalDate.of(2026, 7, 27);

        final SavingsAccountTransaction transaction = SavingsAccountTransaction.withdrawal(mock(SavingsAccount.class), mock(Office.class),
                null, transactionDate, amount(), null);

        assertThat(transaction.getTransactionDate()).isEqualTo(transactionDate);
        assertThat(transaction.getTransactionTime()).isNull();
        assertThat(transaction.isWithdrawal()).isTrue();
    }

    @Test
    void withdrawalWithPositiveOffsetNormalizesTimeToUtc() {
        final OffsetTime transactionTime = OffsetTime.of(14, 30, 0, 0, ZoneOffset.ofHoursMinutes(5, 30));

        final SavingsAccountTransaction transaction = SavingsAccountTransaction.withdrawal(mock(SavingsAccount.class), mock(Office.class),
                null, LocalDate.of(2026, 7, 27), transactionTime, amount(), null);

        assertThat(transaction.getTransactionTime()).isEqualTo(OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC));
        assertThat(transaction.isWithdrawal()).isTrue();
    }

    @Test
    void withdrawalWithNegativeOffsetNormalizesTimeToUtc() {
        final OffsetTime transactionTime = OffsetTime.of(4, 15, 30, 0, ZoneOffset.ofHours(-4));

        final SavingsAccountTransaction transaction = SavingsAccountTransaction.withdrawal(mock(SavingsAccount.class), mock(Office.class),
                null, LocalDate.of(2026, 7, 27), transactionTime, amount(), null);

        assertThat(transaction.getTransactionTime()).isEqualTo(OffsetTime.of(8, 15, 30, 0, ZoneOffset.UTC));
        assertThat(transaction.isWithdrawal()).isTrue();
    }

    @Test
    void withdrawalWithUtcTimeKeepsUtcTime() {
        final OffsetTime transactionTime = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC);

        final SavingsAccountTransaction transaction = SavingsAccountTransaction.withdrawal(mock(SavingsAccount.class), mock(Office.class),
                null, LocalDate.of(2026, 7, 27), transactionTime, amount(), null);

        assertThat(transaction.getTransactionTime()).isEqualTo(transactionTime);
        assertThat(transaction.isWithdrawal()).isTrue();
    }

    private Money amount() {
        return Money.of(CURRENCY, BigDecimal.TEN, MoneyHelper.createMathContext(RoundingMode.HALF_EVEN));
    }
}
