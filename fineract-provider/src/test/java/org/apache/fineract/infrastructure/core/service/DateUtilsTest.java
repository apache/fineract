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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
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

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithShortOffset() {
        OffsetDateTime result = DateUtils.convertDateTimeStringToOffsetDateTime("2025-05-23 01:00:00+6", "yyyy-MM-dd HH:mm:ss", null,
                LocalTime.MIN, ZoneOffset.UTC);
        assertEquals(OffsetDateTime.of(2025, 5, 23, 1, 0, 0, 0, ZoneOffset.ofHours(6)), result);
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithFullOffset() {
        OffsetDateTime result = DateUtils.convertDateTimeStringToOffsetDateTime("2025-05-23 01:00:00+06:00", "yyyy-MM-dd HH:mm:ss", null,
                LocalTime.MIN, ZoneOffset.UTC);
        assertEquals(OffsetDateTime.of(2025, 5, 23, 1, 0, 0, 0, ZoneOffset.ofHours(6)), result);
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithNegativeOffset() {
        OffsetDateTime result = DateUtils.convertDateTimeStringToOffsetDateTime("2025-05-23 01:00:00-05:30", "yyyy-MM-dd HH:mm:ss", null,
                LocalTime.MIN, ZoneOffset.UTC);
        assertEquals(OffsetDateTime.of(2025, 5, 23, 1, 0, 0, 0, ZoneOffset.ofHoursMinutes(-5, -30)), result);
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithZulu() {
        OffsetDateTime result = DateUtils.convertDateTimeStringToOffsetDateTime("2025-05-23 01:00:00Z", "yyyy-MM-dd HH:mm:ss", null,
                LocalTime.MIN, ZoneOffset.ofHours(5));
        assertEquals(OffsetDateTime.of(2025, 5, 23, 1, 0, 0, 0, ZoneOffset.UTC), result);
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithoutOffset() {
        OffsetDateTime result = DateUtils.convertDateTimeStringToOffsetDateTime("2025-05-23 01:00:00", "yyyy-MM-dd HH:mm:ss", null,
                LocalTime.MIN, ZoneOffset.ofHours(2));
        assertEquals(OffsetDateTime.of(2025, 5, 23, 1, 0, 0, 0, ZoneOffset.ofHours(2)), result);
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithDateOnly() {
        OffsetDateTime result = DateUtils.convertDateTimeStringToOffsetDateTime("2025-05-23", "yyyy-MM-dd", null, LocalTime.MIN,
                ZoneOffset.UTC);
        assertEquals(OffsetDateTime.of(2025, 5, 23, 0, 0, 0, 0, ZoneOffset.UTC), result);
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithDotFormat() {
        OffsetDateTime result = DateUtils.convertDateTimeStringToOffsetDateTime("2025.05.23 01:00:00+6", "yyyy.MM.dd HH:mm:ss", null,
                LocalTime.MIN, ZoneOffset.UTC);
        assertEquals(OffsetDateTime.of(2025, 5, 23, 1, 0, 0, 0, ZoneOffset.ofHours(6)), result);
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithNull() {
        assertNull(DateUtils.convertDateTimeStringToOffsetDateTime(null, "yyyy-MM-dd", null, LocalTime.MIN, ZoneOffset.UTC));
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithBlank() {
        assertNull(DateUtils.convertDateTimeStringToOffsetDateTime("   ", "yyyy-MM-dd", null, LocalTime.MIN, ZoneOffset.UTC));
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithTimeZoneString() {
        OffsetDateTime result = DateUtils.convertDateTimeStringToOffsetDateTime("2025-05-23 01:00:00", "yyyy-MM-dd HH:mm:ss", null,
                LocalTime.MIN, "+06:00");
        assertEquals(OffsetDateTime.of(2025, 5, 23, 1, 0, 0, 0, ZoneOffset.ofHours(6)), result);
    }

    @Test
    public void testResolveOffsetWithZoneOffset() {
        assertEquals(ZoneOffset.ofHours(6), DateUtils.resolveOffset("+06:00"));
        assertEquals(ZoneOffset.ofHoursMinutes(-5, -30), DateUtils.resolveOffset("-05:30"));
        assertEquals(ZoneOffset.UTC, DateUtils.resolveOffset("Z"));
    }

    @Test
    public void testResolveOffsetWithZoneId() {
        ZoneOffset result = DateUtils.resolveOffset("Europe/Budapest");
        assertTrue(result.getTotalSeconds() == 3600 || result.getTotalSeconds() == 7200);
    }

    @Test
    public void testResolveOffsetWithNull() {
        assertEquals(ZoneOffset.UTC, DateUtils.resolveOffset(null));
    }

    @Test
    public void testResolveOffsetWithBlank() {
        assertEquals(ZoneOffset.UTC, DateUtils.resolveOffset("   "));
    }

    @Test
    public void testResolveOffsetWithInvalidTimezone() {
        assertThrows(PlatformApiDataValidationException.class, () -> DateUtils.resolveOffset("INVALID_TZ"));
    }

    @Test
    public void testConvertDateTimeStringToLocalDateTimeBackwardCompatibility() {
        LocalDateTime result = DateUtils.convertDateTimeStringToLocalDateTime("2025-05-23 01:00:00", "yyyy-MM-dd HH:mm:ss", null,
                LocalTime.MIN);
        assertEquals(LocalDateTime.of(2025, 5, 23, 1, 0, 0), result);
    }

    @Test
    public void testConvertDateTimeStringToLocalDateTimeWithOffsetStillWorks() {
        LocalDateTime result = DateUtils.convertDateTimeStringToLocalDateTime("2025-05-23 01:00:00+06:00", "yyyy-MM-dd HH:mm:ss", null,
                LocalTime.MIN);
        assertEquals(LocalDateTime.of(2025, 5, 23, 1, 0, 0), result);
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithInvalidInlineOffset() {
        assertThrows(PlatformApiDataValidationException.class,
                () -> DateUtils.convertDateTimeStringToOffsetDateTime("2025-05-23 01:00:00+6A", "yyyy-MM-dd HH:mm:ss", null, LocalTime.MIN,
                        ZoneOffset.UTC));
    }

    @Test
    public void testConvertDateTimeStringToOffsetDateTimeWithInvalidInlineOffsetViaTimeZone() {
        assertThrows(PlatformApiDataValidationException.class,
                () -> DateUtils.convertDateTimeStringToOffsetDateTime("2025-05-23 01:00:00+ABC", "yyyy-MM-dd HH:mm:ss", null, LocalTime.MIN,
                        "Europe/Budapest"));
    }

    @Test
    public void testResolveOffsetWithDstWinterTime() {
        LocalDateTime winterDate = LocalDateTime.of(2025, 1, 15, 12, 0, 0);
        ZoneOffset result = DateUtils.resolveOffset("Europe/Budapest", winterDate);
        assertEquals(ZoneOffset.ofHours(1), result);
    }

    @Test
    public void testResolveOffsetWithDstSummerTime() {
        LocalDateTime summerDate = LocalDateTime.of(2025, 7, 15, 12, 0, 0);
        ZoneOffset result = DateUtils.resolveOffset("Europe/Budapest", summerDate);
        assertEquals(ZoneOffset.ofHours(2), result);
    }

    @Test
    public void testConvertDateTimeStringWithTimeZoneUsesDstCorrectOffset() {
        OffsetDateTime winterResult = DateUtils.convertDateTimeStringToOffsetDateTime("2025-01-15 12:00:00", "yyyy-MM-dd HH:mm:ss", null,
                LocalTime.MIN, "Europe/Budapest");
        assertEquals(ZoneOffset.ofHours(1), winterResult.getOffset());

        OffsetDateTime summerResult = DateUtils.convertDateTimeStringToOffsetDateTime("2025-07-15 12:00:00", "yyyy-MM-dd HH:mm:ss", null,
                LocalTime.MIN, "Europe/Budapest");
        assertEquals(ZoneOffset.ofHours(2), summerResult.getOffset());
    }
}
