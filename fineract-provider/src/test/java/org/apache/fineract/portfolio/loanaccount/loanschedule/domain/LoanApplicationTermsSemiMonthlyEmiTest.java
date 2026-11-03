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

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.EMPTY_LIST;
import static org.apache.fineract.organisation.monetary.domain.MonetaryCurrency.fromApplicationCurrency;
import static org.apache.fineract.organisation.workingdays.domain.RepaymentRescheduleType.MOVE_TO_NEXT_WORKING_DAY;
import static org.apache.fineract.portfolio.common.domain.DayOfWeekType.INVALID;
import static org.apache.fineract.portfolio.common.domain.PeriodFrequencyType.SEMI_MONTHLY;
import static org.apache.fineract.portfolio.common.domain.PeriodFrequencyType.YEARS;
import static org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType.CUMULATIVE;
import static org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod.EQUAL_INSTALLMENTS;
import static org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod.DAILY;
import static org.apache.fineract.portfolio.loanproduct.domain.InterestMethod.DECLINING_BALANCE;
import static org.apache.fineract.portfolio.loanproduct.domain.LoanPreCloseInterestCalculationStrategy.NONE;
import static org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType.DISBURSEMENT_DATE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.fineract.junit.context.WithTenantContext;
import org.apache.fineract.junit.context.WithTenantContextExtension;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The EMI of a cumulative semi-monthly loan with daily interest is derived from a nominal period of a 24th of the year,
 * whatever the configured due days. The days from the 1st of the month to the first due day (1 day for 2/17, 9 days for
 * 10/25) must not leak into it, or the loan under-amortises and the shortfall piles up in the last installment.
 */
@ExtendWith(WithTenantContextExtension.class)
public class LoanApplicationTermsSemiMonthlyEmiTest {

    private static final ApplicationCurrency USD = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_EVEN);

    @BeforeEach
    public void setUp() {
        MoneyHelper.initializeTenantRoundingMode("default", 6);
    }

    /**
     * 1000 at 24% a year over 24 semi-monthly repayments: the nominal rate is 24% / 24 = 1% per period, so the EMI is
     * 1000 * 0.01 / (1 - 1.01^-24) = 47.07.
     */
    @Test
    @WithTenantContext
    public void theEmiUsesAHalfMonthNominalPeriodForTheTenthAndTheTwentyFifth() {
        assertThat(firstEmi(10, 25).getAmount()).isEqualByComparingTo("47.07");
    }

    @Test
    @WithTenantContext
    public void theEmiUsesAHalfMonthNominalPeriodForTheSecondAndTheSeventeenth() {
        assertThat(firstEmi(2, 17).getAmount()).isEqualByComparingTo("47.07");
    }

    @Test
    @WithTenantContext
    public void theEmiUsesAHalfMonthNominalPeriodForTheFifteenthAndTheLastDay() {
        assertThat(firstEmi(15, 31).getAmount()).isEqualByComparingTo("47.07");
    }

    private Money firstEmi(final int firstRepaymentDayOfMonth, final int secondRepaymentDayOfMonth) {
        final LoanApplicationTerms terms = semiMonthlyTerms(firstRepaymentDayOfMonth, secondRepaymentDayOfMonth);
        return terms.pmtForInstallment(new DefaultPaymentPeriodsInOneYearCalculator(), terms.getPrincipal(), 1, MC);
    }

    private LoanApplicationTerms semiMonthlyTerms(final int firstRepaymentDayOfMonth, final int secondRepaymentDayOfMonth) {
        final Money principal = Money.of(fromApplicationCurrency(USD), BigDecimal.valueOf(1000L));
        final LocalDate disbursementDate = LocalDate.of(2023, 1, 1);
        final BigDecimal annualRate = BigDecimal.valueOf(24L);
        final HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(false, EMPTY_LIST,
                new WorkingDays("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU", MOVE_TO_NEXT_WORKING_DAY.getValue(), false, false),
                false, false);
        return LoanApplicationTerms.assembleFrom(USD.toData(), 24, SEMI_MONTHLY, 24, 1, SEMI_MONTHLY, firstRepaymentDayOfMonth,
                secondRepaymentDayOfMonth, null, INVALID, EQUAL_INSTALLMENTS, DECLINING_BALANCE, annualRate, YEARS, annualRate, DAILY,
                false, principal, disbursementDate, null, null, null, null, null, null, null, Money.of(fromApplicationCurrency(USD), ZERO),
                false, null, EMPTY_LIST, null, null, DaysInMonthType.ACTUAL, DaysInYearType.DAYS_365, false, null, null, null, null, null,
                ZERO, null, NONE, null, ZERO, EMPTY_LIST, true, 0, false, holidayDetailDTO, false, false, false, null, false, false, null,
                false, DISBURSEMENT_DATE, disbursementDate, CUMULATIVE, LoanScheduleProcessingType.HORIZONTAL, null, false, null, null,
                false, null, false, null, null, null, false, null, null, null, false, false, null);
    }
}
