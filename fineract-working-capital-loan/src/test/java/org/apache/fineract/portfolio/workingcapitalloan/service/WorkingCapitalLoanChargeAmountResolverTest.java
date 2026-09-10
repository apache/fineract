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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class WorkingCapitalLoanChargeAmountResolverTest {

    private final WorkingCapitalLoanChargeAmountResolver resolver = new WorkingCapitalLoanChargeAmountResolver();
    private MockedStatic<MoneyHelper> moneyHelper;

    @BeforeEach
    void mockPlatformRounding() {
        // MoneyHelper reads the tenant's rounding configuration; there is no tenant in a unit test.
        moneyHelper = mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(MathContext.DECIMAL64);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterEach
    void releaseStaticMock() {
        moneyHelper.close();
    }

    @Test
    @DisplayName("A flat charge keeps its amount whatever the principal is")
    void flatChargeKeepsItsAmount() {
        assertEquals(0, new BigDecimal("100")
                .compareTo(resolver.resolve(ChargeCalculationType.FLAT, new BigDecimal("100"), new BigDecimal("9000"))));
        assertEquals(0, new BigDecimal("100").compareTo(resolver.resolve(ChargeCalculationType.FLAT, new BigDecimal("100"), null)));
    }

    @Test
    @DisplayName("A percentage charge applies its rate to the principal")
    void percentageChargeAppliesRateToPrincipal() {
        final BigDecimal amount = resolver.resolve(ChargeCalculationType.PERCENT_OF_AMOUNT, new BigDecimal("5"), new BigDecimal("9000"));
        assertEquals(0, new BigDecimal("450").compareTo(amount), "5% of 9000");
    }

    @Test
    @DisplayName("A percentage charge with a fractional result is kept at six decimals")
    void percentageChargeKeepsSixDecimals() {
        final BigDecimal amount = resolver.resolve(ChargeCalculationType.PERCENT_OF_AMOUNT, new BigDecimal("1.5"),
                new BigDecimal("1234.5678"));
        assertEquals(6, amount.scale());
        assertEquals(0, new BigDecimal("18.518517").compareTo(amount), "1.5% of 1234.5678");
    }

    @Test
    @DisplayName("A percentage charge against no principal resolves to zero")
    void percentageChargeAgainstNoPrincipalIsZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(resolver.resolve(ChargeCalculationType.PERCENT_OF_AMOUNT, new BigDecimal("5"), null)));
        assertEquals(0, BigDecimal.ZERO.compareTo(resolver.resolve(ChargeCalculationType.PERCENT_OF_AMOUNT, null, new BigDecimal("9000"))));
    }

    @Test
    @DisplayName("A null calculation type is treated as flat")
    void nullCalculationTypeIsFlat() {
        assertEquals(0, new BigDecimal("7").compareTo(resolver.resolve(null, new BigDecimal("7"), new BigDecimal("9000"))));
    }
}
