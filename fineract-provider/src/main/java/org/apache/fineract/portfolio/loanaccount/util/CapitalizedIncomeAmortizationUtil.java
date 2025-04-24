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
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeStrategy;

public final class CapitalizedIncomeAmortizationUtil {

    private CapitalizedIncomeAmortizationUtil() {}

    public static Money calculateDailyAmortization(final LoanCapitalizedIncomeStrategy capitalizedIncomeStrategy,
            final long daysTillMaturity, final BigDecimal remainingAmount, final CurrencyData currencyData) {
        BigDecimal amortization = switch (capitalizedIncomeStrategy) {
            case EQUAL_AMORTIZATION -> remainingAmount.divide(BigDecimal.valueOf(daysTillMaturity), MoneyHelper.getMathContext());
        };

        return Money.of(currencyData, amortization);
    }
}
