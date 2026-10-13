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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class SavingsAccountChargeWithoutSavingsAccountTest {

    private static final MathContext MATH_CONTEXT = new MathContext(19, RoundingMode.HALF_EVEN);

    private MockedStatic<MoneyHelper> moneyHelper;

    @BeforeEach
    void setUp() {
        moneyHelper = mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(MATH_CONTEXT);
    }

    @AfterEach
    void tearDown() {
        moneyHelper.close();
    }

    @Test
    void createNewWithoutSavingsAccount_withFlatCharge_keepsTheRequestedAmount() {
        final SavingsAccountCharge charge = newFlatChargeWithoutAccount(new BigDecimal("10.126"));

        assertThat(charge.savingsAccount()).isNull();
        assertThat(charge.amount()).isEqualByComparingTo("10.126");
        assertThat(charge.amoutOutstanding()).isEqualByComparingTo("10.126");
    }

    @Test
    void update_attachingFlatChargeToItsAccount_roundsToTheAccountCurrency() {
        final SavingsAccountCharge charge = newFlatChargeWithoutAccount(new BigDecimal("10.126"));
        final SavingsAccount account = mock(SavingsAccount.class);
        when(account.getCurrency()).thenReturn(new MonetaryCurrency("USD", 2, 0));

        charge.update(account);

        assertThat(charge.savingsAccount()).isSameAs(account);
        assertThat(charge.amount()).isEqualByComparingTo("10.13");
        assertThat(charge.amoutOutstanding()).isEqualByComparingTo("10.13");
    }

    private static SavingsAccountCharge newFlatChargeWithoutAccount(final BigDecimal amount) {
        final Charge chargeDefinition = mock(Charge.class);
        when(chargeDefinition.getChargeTimeType()).thenReturn(ChargeTimeType.SAVINGS_ACTIVATION.getValue());
        when(chargeDefinition.getChargeCalculation()).thenReturn(ChargeCalculationType.FLAT.getValue());
        when(chargeDefinition.getAmount()).thenReturn(amount);
        return SavingsAccountCharge.createNewWithoutSavingsAccount(chargeDefinition, amount, ChargeTimeType.SAVINGS_ACTIVATION,
                ChargeCalculationType.FLAT, null, true, null, null);
    }
}
