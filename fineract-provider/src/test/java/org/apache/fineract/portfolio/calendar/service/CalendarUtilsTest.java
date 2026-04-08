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
package org.apache.fineract.portfolio.calendar.service;

import static org.apache.fineract.portfolio.calendar.service.CalendarUtils.FLOATING_TIMEZONE_PROPERTY_KEY;
import static org.apache.fineract.portfolio.common.domain.PeriodFrequencyType.WEEKS;
import static org.apache.fineract.util.TimeZoneConstants.ASIA_MANILA_ID;
import static org.apache.fineract.util.TimeZoneConstants.EUROPE_BERLIN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.parameter.Value;
import org.apache.fineract.junit.context.WithTenantContext;
import org.apache.fineract.junit.context.WithTenantContextExtension;
import org.apache.fineract.junit.system.WithSystemProperty;
import org.apache.fineract.junit.system.WithSystemPropertyExtension;
import org.apache.fineract.junit.timezone.WithSystemTimeZone;
import org.apache.fineract.junit.timezone.WithSystemTimeZoneExtension;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({ WithSystemTimeZoneExtension.class, WithTenantContextExtension.class, WithSystemPropertyExtension.class })
public class CalendarUtilsTest {

    @Test
    public void testAdjustDateWithMonthlyFrequencyAndSeedDateBefore28() {
        // given
        Temporal date = LocalDate.of(2023, Month.APRIL, 27);
        Temporal seedDate = LocalDate.of(2023, Month.JANUARY, 27);
        PeriodFrequencyType frequencyType = PeriodFrequencyType.MONTHS;

        // when
        Temporal adjustedDate = CalendarUtils.adjustDate(date, seedDate, frequencyType);

        // then
        assertEquals(27, adjustedDate.get(ChronoField.DAY_OF_MONTH));
    }

    @Test
    public void testAdjustDateWithMonthlyFrequencyAndSeedDateAfter28AndCurrentMonthHasLessDays() {
        // given
        Temporal date = LocalDate.of(2023, Month.FEBRUARY, 28);
        Temporal seedDate = LocalDate.of(2023, Month.JANUARY, 31);
        PeriodFrequencyType frequencyType = PeriodFrequencyType.MONTHS;

        // when
        Temporal adjustedDate = CalendarUtils.adjustDate(date, seedDate, frequencyType);

        // then
        assertEquals(28, adjustedDate.get(ChronoField.DAY_OF_MONTH));
    }

    @Test
    public void testAdjustDateWithMonthlyFrequencyAndSeedDateAfter28AndCurrentMonthHasEqualDays() {
        // given
        Temporal date = LocalDate.of(2023, Month.MARCH, 31);
        Temporal seedDate = LocalDate.of(2023, Month.JANUARY, 31);
        PeriodFrequencyType frequencyType = PeriodFrequencyType.MONTHS;

        // when
        Temporal adjustedDate = CalendarUtils.adjustDate(date, seedDate, frequencyType);

        // then
        assertEquals(31, adjustedDate.get(ChronoField.DAY_OF_MONTH));
    }

    @Test
    public void testAdjustDateWithMonthlyFrequencyAndSeedDateAfter28AndCurrentMonthHasMoreDays() {
        // given
        Temporal date = LocalDate.of(2023, Month.MAY, 31);
        Temporal seedDate = LocalDate.of(2023, Month.APRIL, 30);
        PeriodFrequencyType frequencyType = PeriodFrequencyType.MONTHS;

        // when
        Temporal adjustedDate = CalendarUtils.adjustDate(date, seedDate, frequencyType);

        // then
        assertEquals(30, adjustedDate.get(ChronoField.DAY_OF_MONTH));
    }

    @Test
    public void testAdjustDateWithMonthlyFrequencyAndSeedDateAfter28AndCurrentMonthIsMarch() {
        // given
        Temporal date = LocalDate.of(2023, Month.MARCH, 28);
        Temporal seedDate = LocalDate.of(2023, Month.JANUARY, 31);
        PeriodFrequencyType frequencyType = PeriodFrequencyType.MONTHS;

        // when
        Temporal adjustedDate = CalendarUtils.adjustDate(date, seedDate, frequencyType);

        // then
        assertEquals(31, adjustedDate.get(ChronoField.DAY_OF_MONTH));
    }

    @Test
    public void testAdjustDateWithMonthlyFrequencyAndSeedDateAfter28AndCurrentMonthIsFebLeapYear() {
        // given
        Temporal date = LocalDate.of(2024, Month.FEBRUARY, 29);
        Temporal seedDate = LocalDate.of(2024, Month.JANUARY, 30);
        PeriodFrequencyType frequencyType = PeriodFrequencyType.MONTHS;

        // when
        Temporal adjustedDate = CalendarUtils.adjustDate(date, seedDate, frequencyType);

        // then
        assertEquals(29, adjustedDate.get(ChronoField.DAY_OF_MONTH));
    }

    @Test
    public void testAdjustDateNonMonthlyPeriod() {
        // given
        Temporal date = LocalDate.of(2023, Month.APRIL, 30);
        Temporal seedDate = LocalDate.of(2023, Month.JANUARY, 27);

        // when
        Temporal adjustedDateWeekly = CalendarUtils.adjustDate(date, seedDate, WEEKS);
        // then
        assertEquals(30, adjustedDateWeekly.get(ChronoField.DAY_OF_MONTH));

        // when
        Temporal adjustedDateYearly = CalendarUtils.adjustDate(date, seedDate, PeriodFrequencyType.YEARS);
        // then
        assertEquals(30, adjustedDateYearly.get(ChronoField.DAY_OF_MONTH));

        // when
        Temporal adjustedDateDaily = CalendarUtils.adjustDate(date, seedDate, PeriodFrequencyType.DAYS);
        // then
        assertEquals(30, adjustedDateDaily.get(ChronoField.DAY_OF_MONTH));

    }

    @Test
    @WithSystemTimeZone(ASIA_MANILA_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void testConvertToLocalDateListWorksForDifferentTimezones() throws ParseException {
        // given
        TimeZone timeZone = TimeZoneRegistryFactory.getInstance().createRegistry().getTimeZone("Asia/Manila");
        DateList dates = new DateList(Value.DATE, timeZone);
        dates.add(new Date("20231104", "yyyyMMdd"));

        LocalDate seedDate = LocalDate.of(2023, 11, 4);

        // when
        Collection<LocalDate> result = CalendarUtils.convertToLocalDateList(dates, seedDate, WEEKS, false, 0);
        // then
        Collection<LocalDate> expected = List.of(LocalDate.of(2023, 11, 4));

        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @WithSystemTimeZone(EUROPE_BERLIN_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void testConvertToLocalDateListWorksForSameTimezones() throws ParseException {
        // given
        TimeZone timeZone = TimeZoneRegistryFactory.getInstance().createRegistry().getTimeZone("Europe/Berlin");
        DateList dates = new DateList(Value.DATE, timeZone);
        dates.add(new Date("20231104", "yyyyMMdd"));

        LocalDate seedDate = LocalDate.of(2023, 11, 4);

        // when
        Collection<LocalDate> result = CalendarUtils.convertToLocalDateList(dates, seedDate, WEEKS, false, 0);
        // then
        Collection<LocalDate> expected = List.of(LocalDate.of(2023, 11, 4));

        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(expected);
    }

}
