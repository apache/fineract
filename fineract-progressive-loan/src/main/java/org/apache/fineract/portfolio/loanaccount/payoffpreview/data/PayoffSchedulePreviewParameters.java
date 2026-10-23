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
package org.apache.fineract.portfolio.loanaccount.payoffpreview.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;

/**
 * Validated and resolved inputs of a payoff schedule preview. Exactly one of {@code term} and
 * {@code fixedPaymentAmount} is set.
 */
public record PayoffSchedulePreviewParameters(BigDecimal amount, PayoffSchedulePreviewPeriod term, BigDecimal fixedPaymentAmount,
        PayoffSchedulePreviewPeriod frequency, BigDecimal annualNominalInterestRate, CurrencyData currency, LocalDate startDate,
        DaysInYearType daysInYearType, DaysInMonthType daysInMonthType) {

    public boolean isTermMode() {
        return term != null;
    }
}
