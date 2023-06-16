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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.Test;

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
        Temporal adjustedDateWeekly = CalendarUtils.adjustDate(date, seedDate, PeriodFrequencyType.WEEKS);
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
    public void testGetNextRecurringDate(){
        // given
        String recurringRule = "FREQ=WEEKLY;INTERVAL=1";
        LocalDate seedDate = LocalDate.of(2023, Month.MAY, 1);
        LocalDate startDate = LocalDate.of(2023, Month.MAY, 22);

        // when
        LocalDate nextRecurringDate = CalendarUtils.getNextRecurringDate(recurringRule, seedDate, startDate);

        // then
        assertNotNull(nextRecurringDate);
        assertEquals(LocalDate.of(2023, Month.MAY, 29), nextRecurringDate);
    }

    @Test
    void testGetNextRecurringDateMonthly() {
        // given
        String recurringRule = "FREQ=MONTHLY;INTERVAL=2";
        LocalDate seedDate = LocalDate.of(2023, Month.MAY, 1);
        LocalDate startDate = LocalDate.of(2023, Month.MAY, 15);

        // when
        LocalDate nextRecurringDate = CalendarUtils.getNextRecurringDate(recurringRule, seedDate, startDate);

        // then
        assertNotNull(nextRecurringDate);
        assertEquals(LocalDate.of(2023, Month.JULY, 1), nextRecurringDate);
    }

}