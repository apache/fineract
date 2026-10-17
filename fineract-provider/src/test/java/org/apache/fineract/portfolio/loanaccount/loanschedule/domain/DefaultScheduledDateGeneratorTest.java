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
import static org.apache.fineract.portfolio.calendar.service.CalendarUtils.FLOATING_TIMEZONE_PROPERTY_KEY;
import static org.apache.fineract.portfolio.common.domain.DayOfWeekType.INVALID;
import static org.apache.fineract.portfolio.common.domain.MonthEndDueDateStrategy.FIRST_DAY_OF_NEXT_MONTH;
import static org.apache.fineract.portfolio.common.domain.MonthEndDueDateStrategy.LAST_DAY_OF_MONTH;
import static org.apache.fineract.portfolio.common.domain.PeriodFrequencyType.MONTHS;
import static org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType.CUMULATIVE;
import static org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod.EQUAL_PRINCIPAL;
import static org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
import static org.apache.fineract.portfolio.loanproduct.domain.InterestMethod.FLAT;
import static org.apache.fineract.portfolio.loanproduct.domain.LoanPreCloseInterestCalculationStrategy.NONE;
import static org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType.DISBURSEMENT_DATE;
import static org.apache.fineract.util.TimeZoneConstants.ASIA_MANILA_ID;
import static org.apache.fineract.util.TimeZoneConstants.EUROPE_BERLIN_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.junit.context.WithTenantContext;
import org.apache.fineract.junit.context.WithTenantContextExtension;
import org.apache.fineract.junit.system.WithSystemProperty;
import org.apache.fineract.junit.system.WithSystemPropertyExtension;
import org.apache.fineract.junit.timezone.WithSystemTimeZone;
import org.apache.fineract.junit.timezone.WithSystemTimeZoneExtension;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.workingdays.data.AdjustedDateDetailsDTO;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.MonthEndDueDateStrategy;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

@ExtendWith({ WithSystemTimeZoneExtension.class, WithTenantContextExtension.class, WithSystemPropertyExtension.class })
public class DefaultScheduledDateGeneratorTest {

    private DefaultScheduledDateGenerator underTest = new DefaultScheduledDateGenerator();

    @BeforeEach
    public void setUp() {
        // Initialize MoneyHelper with default rounding mode (HALF_EVEN = 6)
        MoneyHelper.initializeTenantRoundingMode("default", 6);
    }

    @Test
    @WithSystemTimeZone(EUROPE_BERLIN_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_generateRepaymentPeriods() {
        // given
        HolidayDetailDTO holidayDetailDTO = createHolidayDTO();
        MathContext mathContext = new MathContext(12, RoundingMode.HALF_EVEN);

        ApplicationCurrency dollarCurrency = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
        Money principalAmount = Money.of(fromApplicationCurrency(dollarCurrency), BigDecimal.valueOf(100));
        LocalDate expectedDisbursementDate = LocalDate.of(2024, 1, 1);
        LocalDate dueRepaymentPeriodDate = LocalDate.of(2024, 2, 1);

        LocalDate submittedOnDate = LocalDate.of(2024, 1, 1);
        LoanApplicationTerms loanApplicationTerms = createLoanApplicationTerms(dollarCurrency, principalAmount, expectedDisbursementDate,
                submittedOnDate, dueRepaymentPeriodDate, 4, MONTHS, null, null, holidayDetailDTO);

        // when
        List<? extends LoanScheduleModelPeriod> result = underTest.generateRepaymentPeriods(mathContext, expectedDisbursementDate,
                loanApplicationTerms, holidayDetailDTO);

        // then
        assertThat(result).hasSize(4);
        assertThat(result).satisfies(periods -> {
            LoanScheduleModelPeriod firstPeriod = periods.get(0);
            assertThat(firstPeriod.periodNumber()).isEqualTo(1);
            assertThat(firstPeriod.periodFromDate()).hasToString("2024-01-01");
            assertThat(firstPeriod.periodDueDate()).hasToString("2024-02-01");

            LoanScheduleModelPeriod secondPeriod = periods.get(1);
            assertThat(secondPeriod.periodNumber()).isEqualTo(2);
            assertThat(secondPeriod.periodFromDate()).hasToString("2024-02-01");
            assertThat(secondPeriod.periodDueDate()).hasToString("2024-03-01");

            LoanScheduleModelPeriod thirdPeriod = periods.get(2);
            assertThat(thirdPeriod.periodNumber()).isEqualTo(3);
            assertThat(thirdPeriod.periodFromDate()).hasToString("2024-03-01");
            assertThat(thirdPeriod.periodDueDate()).hasToString("2024-04-01");

            LoanScheduleModelPeriod fourthPeriod = periods.get(3);
            assertThat(fourthPeriod.periodNumber()).isEqualTo(4);
            assertThat(fourthPeriod.periodFromDate()).hasToString("2024-04-01");
            assertThat(fourthPeriod.periodDueDate()).hasToString("2024-05-01");
        });
    }

    @Test
    @WithSystemTimeZone(EUROPE_BERLIN_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_AdjustRepaymentDate_Works_WithSameTenant_And_SystemTimeZone() {
        // given
        LocalDate dueRepaymentPeriodDate = LocalDate.of(2023, 11, 26);

        LoanApplicationTerms loanApplicationTerms = createLoanApplicationTerms(dueRepaymentPeriodDate, createHolidayDTO());
        // when
        AdjustedDateDetailsDTO result = underTest.adjustRepaymentDate(dueRepaymentPeriodDate, loanApplicationTerms, createHolidayDTO());
        // then
        assertThat(result).satisfies(r -> {
            assertThat(r.getChangedScheduleDate()).isEqualTo(LocalDate.of(2023, 11, 26));
            assertThat(r.getChangedActualRepaymentDate()).isEqualTo(LocalDate.of(2023, 11, 26));
            assertThat(r.getNextRepaymentPeriodDueDate()).isEqualTo(LocalDate.of(2023, 12, 26));
        });
    }

    @Test
    @WithSystemTimeZone(ASIA_MANILA_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_AdjustRepaymentDate_Works_WithDifferentTenant_And_SystemTimeZone() {
        // given
        LocalDate dueRepaymentPeriodDate = LocalDate.of(2023, 11, 26);

        LoanApplicationTerms loanApplicationTerms = createLoanApplicationTerms(dueRepaymentPeriodDate, createHolidayDTO());
        // when
        AdjustedDateDetailsDTO result = underTest.adjustRepaymentDate(dueRepaymentPeriodDate, loanApplicationTerms, createHolidayDTO());
        // then
        assertThat(result).satisfies(r -> {
            assertThat(r.getChangedScheduleDate()).isEqualTo(LocalDate.of(2023, 11, 26));
            assertThat(r.getChangedActualRepaymentDate()).isEqualTo(LocalDate.of(2023, 11, 26));
            assertThat(r.getNextRepaymentPeriodDueDate()).isEqualTo(LocalDate.of(2023, 12, 26));
        });
    }

    private LoanApplicationTerms createLoanApplicationTerms(LocalDate dueRepaymentPeriodDate, HolidayDetailDTO holidayDetailDTO) {
        ApplicationCurrency dollarCurrency = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
        Money principalAmount = Money.of(fromApplicationCurrency(dollarCurrency), BigDecimal.valueOf(1000L));
        LocalDate expectedDisbursementDate = LocalDate.of(2023, 10, 26);

        LocalDate submittedOnDate = LocalDate.of(2023, 10, 24);
        return createLoanApplicationTerms(dollarCurrency, principalAmount, expectedDisbursementDate, submittedOnDate,
                dueRepaymentPeriodDate, 1, MONTHS, null, null, holidayDetailDTO);
    }

    private LoanApplicationTerms createLoanApplicationTerms(final LocalDate disbursementDate, final int numberOfRepayments,
            final PeriodFrequencyType repaymentFrequency, final Integer fixedLength,
            final MonthEndDueDateStrategy monthEndDueDateStrategy) {
        ApplicationCurrency dollarCurrency = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
        Money principalAmount = Money.of(fromApplicationCurrency(dollarCurrency), BigDecimal.valueOf(1000L));
        return createLoanApplicationTerms(dollarCurrency, principalAmount, disbursementDate, disbursementDate, null, numberOfRepayments,
                repaymentFrequency, fixedLength, monthEndDueDateStrategy, createHolidayDTO());
    }

    private LoanApplicationTerms createLoanApplicationTerms(final ApplicationCurrency currency, final Money principalAmount,
            final LocalDate expectedDisbursementDate, final LocalDate submittedOnDate, final LocalDate calculatedFirstRepaymentDate,
            final int numberOfRepayments, final PeriodFrequencyType repaymentFrequency, final Integer fixedLength,
            final MonthEndDueDateStrategy monthEndDueDateStrategy, final HolidayDetailDTO holidayDetailDTO) {
        return LoanApplicationTerms.assembleFrom(currency.toData(), 1, repaymentFrequency, numberOfRepayments, 1, repaymentFrequency, null,
                INVALID, EQUAL_PRINCIPAL, FLAT, ZERO, repaymentFrequency, ZERO, SAME_AS_REPAYMENT_PERIOD, false, principalAmount,
                expectedDisbursementDate, null, calculatedFirstRepaymentDate, null, null, null, null, null,
                Money.of(fromApplicationCurrency(currency), ZERO), false, null, EMPTY_LIST, BigDecimal.valueOf(36_000L), null,
                DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL, false, null, null, null, null, null, ZERO, null, NONE, null, ZERO,
                EMPTY_LIST, true, 0, false, holidayDetailDTO, false, false, false, null, false, false, null, false, DISBURSEMENT_DATE,
                submittedOnDate, CUMULATIVE, LoanScheduleProcessingType.HORIZONTAL, fixedLength, false, null, null, false, null, false,
                null, null, null, false, null, null, null, false, false, monthEndDueDateStrategy);
    }

    private List<LocalDate> dueDates(final LoanApplicationTerms loanApplicationTerms, final LocalDate scheduleStartDate) {
        return underTest.generateRepaymentPeriods(new MathContext(12, RoundingMode.HALF_EVEN), scheduleStartDate, loanApplicationTerms,
                createHolidayDTO()).stream().map(LoanScheduleModelPeriod::periodDueDate).toList();
    }

    @ParameterizedTest(name = "{0}")
    @NullSource
    @EnumSource(value = MonthEndDueDateStrategy.class, names = "LAST_DAY_OF_MONTH")
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_MissingDayIsClampedToLastDayOfMonthByDefault(final MonthEndDueDateStrategy strategy) {
        final LocalDate disbursementDate = LocalDate.of(2025, 1, 31);
        assertThat(dueDates(createLoanApplicationTerms(disbursementDate, 3, MONTHS, null, strategy), disbursementDate))
                .containsExactly(LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 4, 30));
    }

    @Test
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_MissingDayRollsToFirstDayOfNextMonthAndAnchorSurvives() {
        final LocalDate disbursementDate = LocalDate.of(2025, 1, 31);
        assertThat(dueDates(createLoanApplicationTerms(disbursementDate, 3, MONTHS, null, FIRST_DAY_OF_NEXT_MONTH), disbursementDate))
                .containsExactly(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1));
    }

    @Test
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_LeapDayAnchorRollsOnlyInCommonYear() {
        final LocalDate disbursementDate = LocalDate.of(2024, 2, 29);
        final List<LocalDate> dueDates = dueDates(createLoanApplicationTerms(disbursementDate, 12, MONTHS, null, FIRST_DAY_OF_NEXT_MONTH),
                disbursementDate);
        assertThat(dueDates).hasSize(12);
        assertThat(dueDates.subList(0, 11)).allSatisfy(dueDate -> assertThat(dueDate.getDayOfMonth()).isEqualTo(29));
        assertThat(dueDates.get(0)).isEqualTo(LocalDate.of(2024, 3, 29));
        assertThat(dueDates.get(10)).isEqualTo(LocalDate.of(2025, 1, 29));
        assertThat(dueDates.get(11)).isEqualTo(LocalDate.of(2025, 3, 1));
    }

    @Test
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_RollOnlyHappensWhenTheDayIsMissing() {
        final LocalDate thirtiethAnchor = LocalDate.of(2025, 1, 30);
        assertThat(dueDates(createLoanApplicationTerms(thirtiethAnchor, 4, MONTHS, null, FIRST_DAY_OF_NEXT_MONTH), thirtiethAnchor))
                .containsExactly(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 30), LocalDate.of(2025, 4, 30), LocalDate.of(2025, 5, 30));

        final LocalDate twentyEighthAnchor = LocalDate.of(2025, 1, 28);
        assertThat(dueDates(createLoanApplicationTerms(twentyEighthAnchor, 2, MONTHS, null, FIRST_DAY_OF_NEXT_MONTH), twentyEighthAnchor))
                .containsExactly(LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 28));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = PeriodFrequencyType.class, names = { "DAYS", "WEEKS", "YEARS" })
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_OtherFrequenciesAreUnaffected(final PeriodFrequencyType frequency) {
        final LocalDate disbursementDate = LocalDate.of(2024, 2, 29);
        assertThat(dueDates(createLoanApplicationTerms(disbursementDate, 3, frequency, null, FIRST_DAY_OF_NEXT_MONTH), disbursementDate))
                .isEqualTo(dueDates(createLoanApplicationTerms(disbursementDate, 3, frequency, null, LAST_DAY_OF_MONTH), disbursementDate));
    }

    @Test
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_FixedLengthMaturityAgreesWithGeneratedLastPeriod() {
        final LocalDate disbursementDate = LocalDate.of(2025, 1, 31);
        final LoanApplicationTerms loanApplicationTerms = createLoanApplicationTerms(disbursementDate, 3, MONTHS, 3,
                FIRST_DAY_OF_NEXT_MONTH);
        assertThat(loanApplicationTerms.calculateMaxDateForFixedLength()).isEqualTo(LocalDate.of(2025, 5, 1));
        assertThat(dueDates(loanApplicationTerms, disbursementDate)).last().isEqualTo(LocalDate.of(2025, 5, 1));
    }

    @Test
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_IdealDisbursementDateStepsBackFromRolledFirstRepayment() {
        final LocalDate disbursementDate = LocalDate.of(2025, 1, 31);
        final LoanApplicationTerms loanApplicationTerms = createLoanApplicationTerms(disbursementDate, 3, MONTHS, null,
                FIRST_DAY_OF_NEXT_MONTH);
        assertThat(underTest.idealDisbursementDateBasedOnFirstRepaymentDate(MONTHS, 1, LocalDate.of(2025, 3, 1), null, createHolidayDTO(),
                loanApplicationTerms)).isEqualTo(disbursementDate);
    }

    @Test
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_RolledPeriodCountsAsOneWholePeriod() {
        final LoanApplicationTerms loanApplicationTerms = createLoanApplicationTerms(LocalDate.of(2025, 1, 31), 3, MONTHS, null,
                FIRST_DAY_OF_NEXT_MONTH);
        assertThat(loanApplicationTerms.calculatePeriodsBetweenDates(LocalDate.of(2025, 1, 31), LocalDate.of(2025, 3, 1)))
                .isEqualByComparingTo(BigDecimal.ONE);
        assertThat(loanApplicationTerms.calculatePeriodsBetweenDates(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31)))
                .isEqualByComparingTo(BigDecimal.ONE);
        assertThat(loanApplicationTerms.calculatePeriodsBetweenDates(LocalDate.of(2025, 1, 31), LocalDate.of(2025, 5, 1)))
                .isEqualByComparingTo(BigDecimal.valueOf(3));
    }

    private HolidayDetailDTO createHolidayDTO() {
        return new HolidayDetailDTO(false, EMPTY_LIST,
                new WorkingDays("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU", MOVE_TO_NEXT_WORKING_DAY.getValue(), false, false),
                false, false);
    }
}
