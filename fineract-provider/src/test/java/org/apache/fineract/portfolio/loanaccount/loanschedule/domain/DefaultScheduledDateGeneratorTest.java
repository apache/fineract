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
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
        LoanApplicationTerms loanApplicationTerms = LoanApplicationTerms.assembleFrom(dollarCurrency.toData(), 1, MONTHS, 4, 1, MONTHS,
                null, INVALID, EQUAL_PRINCIPAL, FLAT, ZERO, MONTHS, ZERO, SAME_AS_REPAYMENT_PERIOD, false, principalAmount,
                expectedDisbursementDate, null, dueRepaymentPeriodDate, null, null, null, null, null,
                Money.of(fromApplicationCurrency(dollarCurrency), ZERO), false, null, EMPTY_LIST, BigDecimal.valueOf(36_000L), null,
                DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL, false, null, null, null, null, null, ZERO, null, NONE, null, ZERO,
                EMPTY_LIST, true, 0, false, holidayDetailDTO, false, false, false, null, false, false, null, false, DISBURSEMENT_DATE,
                submittedOnDate, CUMULATIVE, LoanScheduleProcessingType.HORIZONTAL, null, false, null, null, false, null, false, null, null,
                null, false, null, null, null, false, false);

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

    /**
     * FINERACT-2968: when the disbursement falls on the 29th-31st the first due date is clamped into a shorter month
     * (31 Jan -> 28 Feb). The ideal disbursement date (first due date minus one period) must snap back onto the anchor
     * day, otherwise first-period interest starts before the money was disbursed.
     */
    @ParameterizedTest(name = "disbursed {0}, every {1} month(s): first due {2}, ideal disbursement {3}")
    @WithSystemTimeZone(EUROPE_BERLIN_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    @CsvSource({ //
            "2025-01-31, 1, 2025-02-28, 2025-01-31", // clamped by 3 days
            "2025-01-30, 1, 2025-02-28, 2025-01-30", // clamped by 2 days
            "2024-12-31, 2, 2025-02-28, 2024-12-31", // multi-month period
            "2025-01-15, 1, 2025-02-15, 2025-01-15" }) // not clamped: unchanged
    public void test_idealDisbursementDate_snapsOntoAnchorDay_whenFirstDueDateIsClamped(LocalDate expectedDisbursementDate, int repaidEvery,
            LocalDate expectedFirstRepaymentDate, LocalDate expectedIdealDisbursementDate) {
        // given
        HolidayDetailDTO holidayDetailDTO = createHolidayDTO();
        LoanApplicationTerms loanApplicationTerms = createLoanApplicationTermsWithoutFirstRepaymentDate(expectedDisbursementDate,
                repaidEvery, holidayDetailDTO);

        // when
        LocalDate firstRepaymentDate = underTest.generateNextRepaymentDate(expectedDisbursementDate, loanApplicationTerms, true);
        LocalDate idealDisbursementDate = underTest.idealDisbursementDateBasedOnFirstRepaymentDate(MONTHS, repaidEvery, firstRepaymentDate,
                null, holidayDetailDTO, loanApplicationTerms);

        // then
        assertThat(firstRepaymentDate).isEqualTo(expectedFirstRepaymentDate);
        assertThat(idealDisbursementDate).isEqualTo(expectedIdealDisbursementDate);
    }

    private LoanApplicationTerms createLoanApplicationTermsWithoutFirstRepaymentDate(LocalDate expectedDisbursementDate, int repaidEvery,
            HolidayDetailDTO holidayDetailDTO) {
        ApplicationCurrency dollarCurrency = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
        Money principalAmount = Money.of(fromApplicationCurrency(dollarCurrency), BigDecimal.valueOf(10_000L));

        return LoanApplicationTerms.assembleFrom(dollarCurrency.toData(), repaidEvery, MONTHS, 4, repaidEvery, MONTHS, null, INVALID,
                EQUAL_PRINCIPAL, FLAT, ZERO, MONTHS, ZERO, SAME_AS_REPAYMENT_PERIOD, false, principalAmount, expectedDisbursementDate, null,
                null, null, null, null, null, null, Money.of(fromApplicationCurrency(dollarCurrency), ZERO), false, null, EMPTY_LIST,
                BigDecimal.valueOf(36_000L), null, DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL, false, null, null, null, null, null, ZERO,
                null, NONE, null, ZERO, EMPTY_LIST, false, 0, false, holidayDetailDTO, false, false, false, null, false, false, null, false,
                DISBURSEMENT_DATE, expectedDisbursementDate, CUMULATIVE, LoanScheduleProcessingType.HORIZONTAL, null, false, null, null,
                false, null, false, null, null, null, false, null, null, null, false, false);
    }

    private LoanApplicationTerms createLoanApplicationTerms(LocalDate dueRepaymentPeriodDate, HolidayDetailDTO holidayDetailDTO) {
        ApplicationCurrency dollarCurrency = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
        Money principalAmount = Money.of(fromApplicationCurrency(dollarCurrency), BigDecimal.valueOf(1000L));
        LocalDate expectedDisbursementDate = LocalDate.of(2023, 10, 26);

        LocalDate submittedOnDate = LocalDate.of(2023, 10, 24);
        return LoanApplicationTerms.assembleFrom(dollarCurrency.toData(), 1, MONTHS, 1, 1, MONTHS, null, INVALID, EQUAL_PRINCIPAL, FLAT,
                ZERO, MONTHS, ZERO, SAME_AS_REPAYMENT_PERIOD, false, principalAmount, expectedDisbursementDate, null,
                dueRepaymentPeriodDate, null, null, null, null, null, Money.of(fromApplicationCurrency(dollarCurrency), ZERO), false, null,
                EMPTY_LIST, BigDecimal.valueOf(36_000L), null, DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL, false, null, null, null, null,
                null, ZERO, null, NONE, null, ZERO, EMPTY_LIST, true, 0, false, holidayDetailDTO, false, false, false, null, false, false,
                null, false, DISBURSEMENT_DATE, submittedOnDate, CUMULATIVE, LoanScheduleProcessingType.HORIZONTAL, null, false, null, null,
                false, null, false, null, null, null, false, null, null, null, false, false);
    }

    private HolidayDetailDTO createHolidayDTO() {
        return new HolidayDetailDTO(false, EMPTY_LIST,
                new WorkingDays("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU", MOVE_TO_NEXT_WORKING_DAY.getValue(), false, false),
                false, false);
    }
}
