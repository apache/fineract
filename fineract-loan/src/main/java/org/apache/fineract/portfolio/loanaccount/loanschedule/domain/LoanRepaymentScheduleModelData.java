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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;

public record LoanRepaymentScheduleModelData(@NotNull LocalDate scheduleGenerationStartDate, @NotNull CurrencyData currency,
        @NotNull BigDecimal disbursementAmount, @NotNull LocalDate disbursementDate, @NotNull int numberOfRepayments,
        @NotNull int repaymentFrequency, @NotBlank String repaymentFrequencyType, @NotNull BigDecimal annualNominalInterestRate,
        @NotNull boolean downPaymentEnabled, @NotNull DaysInMonthType daysInMonth, @NotNull DaysInYearType daysInYear,
        BigDecimal downPaymentPercentage, Integer installmentAmountInMultiplesOf, Integer fixedLength) {
}
