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
package org.apache.fineract.portfolio.loanaccount.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CapitalizedIncomeAmortizationUtilTest {

    private static final MockedStatic<MoneyHelper> MONEY_HELPER = Mockito.mockStatic(MoneyHelper.class);

    @BeforeAll
    public static void init() {
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(19, RoundingMode.HALF_EVEN));
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterAll
    public static void deregister() {
        MONEY_HELPER.close();
    }

    @Test
    void calculateDailyAmortizationEqualAmortization() {
        CurrencyData currency = new CurrencyData("USD", 2, null);

        BigDecimal remaining = BigDecimal.valueOf(50.0);
        Money dailyAmortization = CapitalizedIncomeAmortizationUtil
                .calculateDailyAmortization(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION, 91L, remaining, currency);

        Assertions.assertEquals(BigDecimal.valueOf(0.55), dailyAmortization.getAmount());

        BigDecimal remaining2 = BigDecimal.valueOf(8.2);
        Money dailyAmortization2 = CapitalizedIncomeAmortizationUtil
                .calculateDailyAmortization(LoanCapitalizedIncomeStrategy.EQUAL_AMORTIZATION, 15L, remaining2, currency);

        Assertions.assertEquals(BigDecimal.valueOf(0.55), dailyAmortization2.getAmount());
    }
}
