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
package org.apache.fineract.infrastructure.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateUtilsTest {

    @BeforeEach
    public void init() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 12))));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void getSystemZoneIdTest() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+02:00"));
        assertEquals("GMT+02:00", DateUtils.getSystemZoneId().getId());
    }

    @Test
    public void getDateTimeZoneOfTenant() {
        assertEquals(ZoneId.of("Asia/Kolkata"), DateUtils.getDateTimeZoneOfTenant());
    }

    @Test
    public void getLocalDateOfTenant() {
        assertTrue(DateUtils.isEqualTenantDate(LocalDate.now(ZoneId.of("Asia/Kolkata"))));
    }

    @Test
    public void getLocalDateTimeOfTenant() {
        assertTrue(DateUtils.isEqualTenantDateTime(LocalDateTime.now(ZoneId.of("Asia/Kolkata")), ChronoUnit.SECONDS));
    }

    @Test
    public void getOffsetDateTimeOfTenant() {
        assertTrue(DateUtils.isEqualTenantDateTime(OffsetDateTime.now(ZoneId.of("Asia/Kolkata")), ChronoUnit.SECONDS));
    }

    @Test
    public void getLocalDateTimeOfSystem() {
        assertTrue(DateUtils.isEqualSystemDateTime(LocalDateTime.now(ZoneId.systemDefault()), ChronoUnit.SECONDS));
    }

    @Test
    public void isDateInTheFuture() {
        assertFalse(DateUtils.isDateInTheFuture(LocalDate.of(2022, 6, 11)));
        assertFalse(DateUtils.isDateInTheFuture(LocalDate.of(2022, 6, 12)));
        assertTrue(DateUtils.isDateInTheFuture(LocalDate.of(2022, 6, 13)));
    }

    @Test
    public void getBusinesLocalDate() {
        assertTrue(DateUtils.isEqualBusinessDate(LocalDate.of(2022, 6, 12)));
    }

    // --- safeMonthDay (clamps day to last valid day of month for current business year) ---

    @Test
    public void safeMonthDay_validDay_returnsSameMonthDay() {
        assertEquals(MonthDay.of(1, 15), DateUtils.safeMonthDay(1, 15));
        assertEquals(MonthDay.of(3, 31), DateUtils.safeMonthDay(3, 31));
        assertEquals(MonthDay.of(4, 30), DateUtils.safeMonthDay(4, 30));
        assertEquals(MonthDay.of(12, 31), DateUtils.safeMonthDay(12, 31));
    }

    @Test
    public void safeMonthDay_february29_inNonLeapYear_clampedTo28() {
        // business date initialized to 2022-06-12 (non-leap year) in @BeforeEach
        assertEquals(MonthDay.of(2, 28), DateUtils.safeMonthDay(2, 29));
    }

    @Test
    public void safeMonthDay_february30_inNonLeapYear_clampedTo28() {
        assertEquals(MonthDay.of(2, 28), DateUtils.safeMonthDay(2, 30));
    }

    @Test
    public void safeMonthDay_february31_inNonLeapYear_clampedTo28() {
        assertEquals(MonthDay.of(2, 28), DateUtils.safeMonthDay(2, 31));
    }

    @Test
    public void safeMonthDay_february29_inLeapYear_preserved() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 6, 12))));
        assertEquals(MonthDay.of(2, 29), DateUtils.safeMonthDay(2, 29));
    }

    @Test
    public void safeMonthDay_february30_inLeapYear_clampedTo29() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 6, 12))));
        assertEquals(MonthDay.of(2, 29), DateUtils.safeMonthDay(2, 30));
    }

    @Test
    public void safeMonthDay_february31_inLeapYear_clampedTo29() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 6, 12))));
        assertEquals(MonthDay.of(2, 29), DateUtils.safeMonthDay(2, 31));
    }

    @Test
    public void safeMonthDay_april31_clampedTo30() {
        assertEquals(MonthDay.of(4, 30), DateUtils.safeMonthDay(4, 31));
    }

    @Test
    public void safeMonthDay_june31_clampedTo30() {
        assertEquals(MonthDay.of(6, 30), DateUtils.safeMonthDay(6, 31));
    }

    @Test
    public void safeMonthDay_november31_clampedTo30() {
        assertEquals(MonthDay.of(11, 30), DateUtils.safeMonthDay(11, 31));
    }

    @Test
    public void safeMonthDay_firstDayOfMonth_preserved() {
        assertEquals(MonthDay.of(2, 1), DateUtils.safeMonthDay(2, 1));
        assertEquals(MonthDay.of(7, 1), DateUtils.safeMonthDay(7, 1));
    }
}
