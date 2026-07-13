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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.junit.jupiter.api.Test;

/**
 * Regression guard for the withholding-tax NPE on deposit-account closure.
 *
 * <p>
 * When a withholding-tax account (taxGroup set) posts/closes with <em>zero</em> total interest posted,
 * {@code SavingsAccountTransactionSummaryWrapper.calculateTotalInterestPosted} returns {@code null} (via
 * {@code getAmountDefaultedToNullIfZero()}). That null flows into {@link SavingsAccount#createWithHoldTransaction} /
 * {@link SavingsAccount#updateWithHoldTransaction}, which previously did an unguarded {@code amount.compareTo(ZERO)}
 * and threw {@code NullPointerException: Cannot invoke "java.math.BigDecimal.compareTo(...)" because "amount" is null}.
 *
 * <p>
 * Real-world trigger: premature-closing a short-term fixed deposit whose pre-closure penalty exceeds the chart rate, so
 * the effective interest is clamped to zero and no interest-posting transaction is created.
 */
class SavingsAccountWithHoldTaxNullAmountTest {

    // Fixed literal date — the transaction date is never dereferenced on the null/zero-amount path under test,
    // and a constant keeps the test deterministic and clear of the JavaTimeDefaultTimeZone ban on LocalDate.now().
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 1);

    private SavingsAccount accountWithTaxGroup() {
        SavingsAccount account = new SavingsAccount() {};
        setPrivateField(account, "taxGroup", mock(TaxGroup.class));
        return account;
    }

    @Test
    void createWithHoldTransaction_withNullAmount_doesNotThrowAndAddsNoTax() {
        SavingsAccount account = accountWithTaxGroup();

        assertThatCode(() -> {
            boolean isTaxAdded = account.createWithHoldTransaction(null, FIXED_DATE, false);
            assertThat(isTaxAdded).isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    void updateWithHoldTransaction_withNullAmount_doesNotThrowAndAddsNoTax() {
        SavingsAccount account = accountWithTaxGroup();

        assertThatCode(() -> {
            boolean isTaxAdded = account.updateWithHoldTransaction(null, mock(SavingsAccountTransaction.class));
            assertThat(isTaxAdded).isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    void createWithHoldTransaction_withZeroAmount_addsNoTax() {
        SavingsAccount account = accountWithTaxGroup();

        boolean isTaxAdded = account.createWithHoldTransaction(BigDecimal.ZERO, FIXED_DATE, false);

        assertThat(isTaxAdded).isFalse();
    }

    private static void setPrivateField(final Object target, final String fieldName, final Object value) {
        try {
            final Field field = SavingsAccount.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set field " + fieldName, e);
        }
    }
}
