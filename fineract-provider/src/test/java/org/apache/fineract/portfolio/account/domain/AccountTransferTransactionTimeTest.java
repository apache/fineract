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
package org.apache.fineract.portfolio.account.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.junit.jupiter.api.Test;

class AccountTransferTransactionTimeTest {

    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, 0);

    @Test
    void savingsToSavingsTransferWithDateOnlyKeepsTransactionTimeNull() {
        final LocalDate transactionDate = LocalDate.of(2026, 7, 27);

        final AccountTransferTransaction transaction = AccountTransferTransaction.savingsToSavingsTransfer(
                mock(AccountTransferDetails.class), mock(SavingsAccountTransaction.class), mock(SavingsAccountTransaction.class),
                transactionDate, amount(), "transfer");

        assertThat(transaction.getDate()).isEqualTo(transactionDate);
        assertThat(transaction.getTransactionTime()).isNull();
    }

    @Test
    void savingsToSavingsTransferWithPositiveOffsetNormalizesTimeToUtc() {
        final OffsetTime transactionTime = OffsetTime.of(14, 30, 0, 0, ZoneOffset.ofHoursMinutes(5, 30));

        final AccountTransferTransaction transaction = AccountTransferTransaction.savingsToSavingsTransfer(
                mock(AccountTransferDetails.class), mock(SavingsAccountTransaction.class), mock(SavingsAccountTransaction.class),
                LocalDate.of(2026, 7, 27), transactionTime, amount(), "transfer");

        assertThat(transaction.getTransactionTime()).isEqualTo(OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC));
    }

    @Test
    void savingsToLoanTransferWithNegativeOffsetNormalizesTimeToUtc() {
        final OffsetTime transactionTime = OffsetTime.of(4, 15, 30, 0, ZoneOffset.ofHours(-4));

        final AccountTransferTransaction transaction = AccountTransferTransaction.savingsToLoanTransfer(mock(AccountTransferDetails.class),
                mock(SavingsAccountTransaction.class), null, LocalDate.of(2026, 7, 27), transactionTime, amount(), "transfer");

        assertThat(transaction.getTransactionTime()).isEqualTo(OffsetTime.of(8, 15, 30, 0, ZoneOffset.UTC));
    }

    @Test
    void loanToSavingsTransferWithUtcTimeKeepsUtcTime() {
        final OffsetTime transactionTime = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC);

        final AccountTransferTransaction transaction = AccountTransferTransaction.loanTosavingsTransfer(mock(AccountTransferDetails.class),
                mock(SavingsAccountTransaction.class), null, LocalDate.of(2026, 7, 27), transactionTime, amount(), "transfer");

        assertThat(transaction.getTransactionTime()).isEqualTo(transactionTime);
    }

    private Money amount() {
        return Money.of(CURRENCY, BigDecimal.TEN, MoneyHelper.createMathContext(RoundingMode.HALF_EVEN));
    }
}
