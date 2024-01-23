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
import static org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy.NONE;
import static org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType.DISBURSEMENT_DATE;
import static org.apache.fineract.util.TimeZoneConstants.ASIA_MANILA_ID;
import static org.apache.fineract.util.TimeZoneConstants.EUROPE_BERLIN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
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
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith({ WithSystemTimeZoneExtension.class, WithTenantContextExtension.class, WithSystemPropertyExtension.class })
public class DefaultScheduledDateGeneratorTest {

    private DefaultScheduledDateGenerator underTest = new DefaultScheduledDateGenerator();

    @BeforeEach
    public void setUp() {
        ConfigurationDomainService cds = Mockito.mock(ConfigurationDomainService.class);
        given(cds.getRoundingMode()).willReturn(6); // default

        MoneyHelper moneyHelper = new MoneyHelper();
        ReflectionTestUtils.setField(moneyHelper, "configurationDomainService", cds);
        moneyHelper.initialize();
    }

    @Test
    @WithSystemTimeZone(EUROPE_BERLIN_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_AdjustRepaymentDate_Works_WithSameTenant_And_SystemTimeZone() {
        // given
        HolidayDetailDTO holidayDetailDTO = createHolidayDTO();

        LocalDate dueRepaymentPeriodDate = LocalDate.of(2023, 11, 26);

        LoanApplicationTerms loanApplicationTerms = createLoanApplicationTerms(dueRepaymentPeriodDate, holidayDetailDTO);
        // when
        AdjustedDateDetailsDTO result = underTest.adjustRepaymentDate(dueRepaymentPeriodDate, loanApplicationTerms, holidayDetailDTO);
        // then
        assertThat(result.getChangedScheduleDate()).isEqualTo(LocalDate.of(2023, 11, 26));
        assertThat(result.getChangedActualRepaymentDate()).isEqualTo(LocalDate.of(2023, 11, 26));
        assertThat(result.getNextRepaymentPeriodDueDate()).isEqualTo(LocalDate.of(2023, 12, 26));
    }

    @Test
    @WithSystemTimeZone(ASIA_MANILA_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_AdjustRepaymentDate_Works_WithDifferentTenant_And_SystemTimeZone() {
        // given
        HolidayDetailDTO holidayDetailDTO = createHolidayDTO();

        LocalDate dueRepaymentPeriodDate = LocalDate.of(2023, 11, 26);

        LoanApplicationTerms loanApplicationTerms = createLoanApplicationTerms(dueRepaymentPeriodDate, holidayDetailDTO);
        // when
        AdjustedDateDetailsDTO result = underTest.adjustRepaymentDate(dueRepaymentPeriodDate, loanApplicationTerms, holidayDetailDTO);
        // then
        assertThat(result.getChangedScheduleDate()).isEqualTo(LocalDate.of(2023, 11, 26));
        assertThat(result.getChangedActualRepaymentDate()).isEqualTo(LocalDate.of(2023, 11, 26));
        assertThat(result.getNextRepaymentPeriodDueDate()).isEqualTo(LocalDate.of(2023, 12, 26));
    }

    private LoanApplicationTerms createLoanApplicationTerms(LocalDate dueRepaymentPeriodDate, HolidayDetailDTO holidayDetailDTO) {
        ApplicationCurrency dollarCurrency = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
        Money principalAmount = Money.of(fromApplicationCurrency(dollarCurrency), BigDecimal.valueOf(1000L));
        LocalDate expectedDisbursementDate = LocalDate.of(2023, 10, 26);

        LocalDate submittedOnDate = LocalDate.of(2023, 10, 24);
        return LoanApplicationTerms.assembleFrom(dollarCurrency, 1, MONTHS, 1, 1, MONTHS, null, INVALID, EQUAL_PRINCIPAL, FLAT, ZERO,
                MONTHS, ZERO, SAME_AS_REPAYMENT_PERIOD, false, principalAmount, expectedDisbursementDate, null, dueRepaymentPeriodDate,
                null, null, null, null, null, Money.of(fromApplicationCurrency(dollarCurrency), ZERO), false, null, EMPTY_LIST,
                BigDecimal.valueOf(36_000L), null, DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL, false, null, null, null, null, null, ZERO,
                null, NONE, null, ZERO, EMPTY_LIST, true, 0, false, holidayDetailDTO, false, false, false, null, false, false, null, false,
                DISBURSEMENT_DATE, submittedOnDate, CUMULATIVE, LoanScheduleProcessingType.HORIZONTAL);
    }

    private HolidayDetailDTO createHolidayDTO() {
        WorkingDays workingDays = new WorkingDays("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU", MOVE_TO_NEXT_WORKING_DAY.getValue(),
                false, false);
        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(false, EMPTY_LIST, workingDays, false, false);
        return holidayDetailDTO;
    }

}
