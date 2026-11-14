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
import static java.util.Collections.emptyList;
import static org.apache.fineract.organisation.monetary.domain.MonetaryCurrency.fromApplicationCurrency;
import static org.apache.fineract.organisation.workingdays.domain.RepaymentRescheduleType.MOVE_TO_NEXT_WORKING_DAY;
import static org.apache.fineract.portfolio.common.domain.DayOfWeekType.INVALID;
import static org.apache.fineract.portfolio.common.domain.PeriodFrequencyType.MONTHS;
import static org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType.CUMULATIVE;
import static org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod.EQUAL_INSTALLMENTS;
import static org.apache.fineract.portfolio.loanproduct.domain.InterestMethod.DECLINING_BALANCE;
import static org.apache.fineract.portfolio.loanproduct.domain.LoanPreCloseInterestCalculationStrategy.NONE;
import static org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType.DISBURSEMENT_DATE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.monetary.mapper.CurrencyMapper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

/**
 * Cumulative declining balance loans with DAILY interest and ACTUAL days in year must use 366 days in a leap year and
 * split a period that crosses a year boundary by calendar year (same as the progressive engine). 12% a year on 10,000
 * unless stated otherwise.
 */
class LoanApplicationTermsLeapYearInterestTest {

    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_EVEN);
    private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);

    private ApplicationCurrency currency;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(new EnumMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 1)))));
        MoneyHelper.initializeTenantRoundingMode("default", 6);
        currency = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @ParameterizedTest(name = "{0} to {1} = {2}")
    @CsvSource({
            // non-leap year: 31/365
            "2023-01-01, 2023-02-01, 101.92",
            // leap year: 31/366
            "2024-01-01, 2024-02-01, 101.64",
            // 16/365 + 15/366 and 16/366 + 15/365
            "2023-12-15, 2024-01-15, 101.78", "2024-12-15, 2025-01-15, 101.77",
            // Feb 29 inside the period: 29/366
            "2024-02-15, 2024-03-15, 95.08",
            // more than two calendar years: 60/365 + 365/365 + 366/366 + 32/365
            "2022-11-01, 2025-02-01, 2702.47",
            // one day: Feb 29 and the first day of a new year take the year they end in
            "2024-02-29, 2024-03-01, 3.28", "2024-12-31, 2025-01-01, 3.29", "2023-12-31, 2024-01-01, 3.28",
            // 2100 is not a leap year, 2000 is
            "2099-12-15, 2100-01-15, 101.92", "2100-02-01, 2100-03-01, 92.05", "2000-02-01, 2000-03-01, 95.08",
            "1999-12-15, 2000-01-15, 101.78",
            // starting or ending on Jan 1 and Dec 31
            "2024-01-01, 2024-03-01, 196.72", "2023-12-01, 2023-12-31, 98.63", "2024-12-01, 2024-12-31, 98.36",
            "2023-12-31, 2024-02-01, 104.92", "2023-12-01, 2024-01-01, 101.91", "2024-12-01, 2025-01-01, 101.65" })
    void dailyActualInterestIsLeapYearAware(String periodStart, String periodEnd, String expectedInterest) {
        assertThat(interest(DaysInYearType.ACTUAL, InterestCalculationPeriodMethod.DAILY, TEN_THOUSAND, periodStart, periodEnd))
                .isEqualByComparingTo(expectedInterest);
    }

    @Test
    void periodAcrossTwoNonLeapYearsEqualsTheFormerSingleMultiply() {
        // 100 billion makes a difference of 1e-12 in the rate visible in the 2 decimals of the interest
        final BigDecimal principal = new BigDecimal("100000000000");
        final BigDecimal oneDayRate = BigDecimal.valueOf(12).divide(BigDecimal.valueOf(365), MC).divide(BigDecimal.valueOf(100), MC);
        for (LocalDate start = LocalDate.of(2025, 1, 1); start.getYear() == 2025; start = start.plusDays(1)) {
            for (int days = 1; days <= 360; days++) {
                final LocalDate end = start.plusDays(days);
                if (end.getYear() != 2026) {
                    continue;
                }
                final BigDecimal expected = Money.of(fromApplicationCurrency(currency), principal)
                        .multiplyRetainScale(oneDayRate.multiply(BigDecimal.valueOf(days), MC), MC).getAmount();
                assertThat(
                        interest(DaysInYearType.ACTUAL, InterestCalculationPeriodMethod.DAILY, principal, start.toString(), end.toString()))
                        .as("%s to %s", start, end).isEqualByComparingTo(expected);
            }
        }
    }

    @ParameterizedTest(name = "{0} to {1} = {2}")
    @CsvSource({ "2024-03-01, 2024-03-01, 0.00", "2024-03-05, 2024-03-01, -13.15", "2024-01-01, 2023-01-01, -1200.00" })
    void periodNotEndingAfterItsStartKeepsTheFormerFormula(String periodStart, String periodEnd, String expectedInterest) {
        // days / 365 as before, even where the dates are in different years
        assertThat(interest(DaysInYearType.ACTUAL, InterestCalculationPeriodMethod.DAILY, TEN_THOUSAND, periodStart, periodEnd))
                .isEqualByComparingTo(expectedInterest);
    }

    @Test
    void fixedDaysInYearTypesAreUnchangedInLeapYear() {
        assertThat(interest(DaysInYearType.DAYS_365, InterestCalculationPeriodMethod.DAILY, TEN_THOUSAND, "2024-01-01", "2024-02-01"))
                .isEqualByComparingTo("101.92");
        assertThat(interest(DaysInYearType.DAYS_365, InterestCalculationPeriodMethod.DAILY, TEN_THOUSAND, "2023-12-15", "2024-01-15"))
                .isEqualByComparingTo("101.92");
        assertThat(interest(DaysInYearType.DAYS_360, InterestCalculationPeriodMethod.DAILY, TEN_THOUSAND, "2024-01-01", "2024-02-01"))
                .isEqualByComparingTo("103.33");
    }

    @Test
    void sameAsRepaymentPeriodIsUnchangedInLeapYear() {
        // 10000 * 12% / 12 whatever the days in the period
        assertThat(interest(DaysInYearType.ACTUAL, InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD, TEN_THOUSAND, "2024-01-01",
                "2024-02-01")).isEqualByComparingTo("100.00");
    }

    @ParameterizedTest(name = "disbursed {0}: first interest {1}, total interest {2}")
    @CsvSource({ "2024-01-01, 101.64, 660.86", "2023-01-01, 101.92, 659.44" })
    void generatedScheduleOfTwelveMonthlyInstallments(String disbursement, String firstInterest, String totalInterest) {
        final LoanApplicationTerms terms = terms(DaysInYearType.ACTUAL, InterestCalculationPeriodMethod.DAILY, TEN_THOUSAND,
                LocalDate.parse(disbursement));
        final CumulativeDecliningBalanceInterestLoanScheduleGenerator generator = new CumulativeDecliningBalanceInterestLoanScheduleGenerator(
                new DefaultScheduledDateGenerator(), new DefaultPaymentPeriodsInOneYearCalculator(),
                Mockito.mock(LoanTransactionRepository.class), Mockito.mock(CurrencyMapper.class));
        final HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(false, emptyList(),
                new WorkingDays("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU", MOVE_TO_NEXT_WORKING_DAY.getValue(), false, false),
                false, false);

        final LoanScheduleModel schedule = generator.generate(MC, terms, new HashSet<>(), holidayDetailDTO);

        BigDecimal interest = ZERO;
        BigDecimal principal = ZERO;
        BigDecimal first = null;
        for (LoanScheduleModelPeriod period : schedule.getPeriods()) {
            if (period.isRepaymentPeriod()) {
                first = first == null ? period.interestDue() : first;
                interest = interest.add(period.interestDue());
                principal = principal.add(period.principalDue());
            }
        }
        assertThat(first).isEqualByComparingTo(firstInterest);
        assertThat(interest).isEqualByComparingTo(totalInterest);
        assertThat(principal).isEqualByComparingTo("10000.00");
    }

    private BigDecimal interest(DaysInYearType daysInYearType, InterestCalculationPeriodMethod interestCalculationPeriodMethod,
            BigDecimal principal, String periodStart, String periodEnd) {
        LoanApplicationTerms terms = terms(daysInYearType, interestCalculationPeriodMethod, principal, LocalDate.of(2024, 1, 1));
        return terms.calculateTotalInterestForPeriod(new DefaultPaymentPeriodsInOneYearCalculator(), ZERO, 1, MC,
                Money.zero(fromApplicationCurrency(currency)), terms.getPrincipal(), LocalDate.parse(periodStart),
                LocalDate.parse(periodEnd)).interest().getAmount();
    }

    private LoanApplicationTerms terms(DaysInYearType daysInYearType, InterestCalculationPeriodMethod interestCalculationPeriodMethod,
            BigDecimal principal, LocalDate disbursementDate) {
        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(false, emptyList(),
                new WorkingDays("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU", MOVE_TO_NEXT_WORKING_DAY.getValue(), false, false),
                false, false);
        Money principalAmount = Money.of(fromApplicationCurrency(currency), principal);
        BigDecimal annualRate = BigDecimal.valueOf(12);
        return LoanApplicationTerms.assembleFrom(currency.toData(), 12, MONTHS, 12, 1, MONTHS, null, null, null, INVALID,
                EQUAL_INSTALLMENTS, DECLINING_BALANCE, annualRate, MONTHS, annualRate, interestCalculationPeriodMethod, false,
                principalAmount, disbursementDate, null, disbursementDate.plusMonths(1), null, null, null, null, null,
                Money.of(fromApplicationCurrency(currency), ZERO), false, null, emptyList(), null, null, DaysInMonthType.ACTUAL,
                daysInYearType, false, null, null, null, null, null, ZERO, null, NONE, null, ZERO, emptyList(), true, 0, false,
                holidayDetailDTO, false, false, false, null, false, false, null, false, DISBURSEMENT_DATE, disbursementDate, CUMULATIVE,
                LoanScheduleProcessingType.HORIZONTAL, null, false, null, null, false, null, false, null, null, null, false, null, null,
                null, false, false, null);
    }
}
