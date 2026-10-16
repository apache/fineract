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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class LoanApplicationTermsTest {

    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_EVEN);

    private MockedStatic<MoneyHelper> moneyHelper;

    @BeforeEach
    void setUp() {
        moneyHelper = Mockito.mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(MC);
    }

    @AfterEach
    void tearDown() {
        moneyHelper.close();
    }

    @Test
    void assembleFromModelDataKeepsDaysInYearCustomStrategy() {
        final LoanApplicationTerms terms = LoanApplicationTerms.assembleFrom(modelData(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY),
                MC);

        assertEquals(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY, terms.toLoanConfigurationDetails().getDaysInYearCustomStrategy());
    }

    @Test
    void assembleFromModelDataKeepsMissingDaysInYearCustomStrategy() {
        final LoanApplicationTerms terms = LoanApplicationTerms.assembleFrom(modelData(null), MC);

        assertNull(terms.toLoanConfigurationDetails().getDaysInYearCustomStrategy());
    }

    private static LoanRepaymentScheduleModelData modelData(final DaysInYearCustomStrategyType daysInYearCustomStrategy) {
        final CurrencyData currency = new CurrencyData("USD", "US Dollar", 2, null, "USD", "$");
        final LocalDate startDate = LocalDate.of(2024, 1, 1);
        return new LoanRepaymentScheduleModelData(startDate, currency, BigDecimal.valueOf(100), startDate, 6, 1, "MONTHS",
                BigDecimal.valueOf(7), false, DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL, BigDecimal.ZERO, null, null, false,
                daysInYearCustomStrategy, InterestMethod.DECLINING_BALANCE, true, false);
    }
}
