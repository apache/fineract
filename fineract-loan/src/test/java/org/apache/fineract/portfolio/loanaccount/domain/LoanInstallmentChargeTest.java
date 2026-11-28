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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class LoanInstallmentChargeTest {

    private final LoanInstallmentCharge installmentCharge = new LoanInstallmentCharge(BigDecimal.valueOf(4), mock(LoanCharge.class),
            mock(LoanRepaymentScheduleInstallment.class));
    private MockedStatic<MoneyHelper> moneyHelper;

    @BeforeEach
    void setUp() {
        moneyHelper = mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(19, RoundingMode.HALF_EVEN));
    }

    @AfterEach
    void tearDown() {
        moneyHelper.close();
    }

    @Test
    void waivesPartOfTheOutstandingAmount() {
        assertThat(installmentCharge.waive(new BigDecimal("2.67"))).isEqualByComparingTo("2.67");

        assertThat(installmentCharge.getAmountWaived()).isEqualByComparingTo("2.67");
        assertThat(installmentCharge.getAmountOutstanding()).isEqualByComparingTo("1.33");
        assertThat(installmentCharge.isWaived()).isFalse();
    }

    @Test
    void waivesTheRestOfTheOutstandingAmount() {
        installmentCharge.waive(BigDecimal.ONE);

        assertThat(installmentCharge.waive(BigDecimal.valueOf(3))).isEqualByComparingTo("3");

        assertThat(installmentCharge.getAmountWaived()).isEqualByComparingTo("4");
        assertThat(installmentCharge.getAmountOutstanding()).isEqualByComparingTo("0");
        assertThat(installmentCharge.isWaived()).isTrue();
    }

    @Test
    void waivesNoMoreThanTheOutstandingAmount() {
        assertThat(installmentCharge.waive(BigDecimal.TEN)).isEqualByComparingTo("4");

        assertThat(installmentCharge.getAmountWaived()).isEqualByComparingTo("4");
        assertThat(installmentCharge.getAmountOutstanding()).isEqualByComparingTo("0");
        assertThat(installmentCharge.isWaived()).isTrue();
        assertThat(installmentCharge.waive(BigDecimal.ONE)).isEqualByComparingTo("0");
    }

    @Test
    void waivesNothingForNothing() {
        assertThat(installmentCharge.waive(null)).isEqualByComparingTo("0");
        assertThat(installmentCharge.waive(BigDecimal.ZERO)).isEqualByComparingTo("0");

        assertThat(installmentCharge.getAmountWaived()).isNull();
        assertThat(installmentCharge.getAmountOutstanding()).isEqualByComparingTo("4");
        assertThat(installmentCharge.isWaived()).isFalse();
    }
}
