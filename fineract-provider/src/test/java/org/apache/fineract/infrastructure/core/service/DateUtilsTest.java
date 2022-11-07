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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateUtilsTest {

    @BeforeEach
    public void init() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 12))));
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
        assertEquals(LocalDate.now(ZoneId.of("Asia/Kolkata")), DateUtils.getLocalDateOfTenant());
    }

    @Test
    public void getLocalDateTimeOfTenant() {
        assertEquals(LocalDateTime.now(ZoneId.of("Asia/Kolkata")).truncatedTo(ChronoUnit.SECONDS), DateUtils.getLocalDateTimeOfTenant());
    }

    @Test
    public void getOffsetDateTimeOfTenant() {
        assertEquals(OffsetDateTime.now(ZoneId.of("Asia/Kolkata")).truncatedTo(ChronoUnit.SECONDS), DateUtils.getOffsetDateTimeOfTenant());
    }

    @Test
    public void getLocalDateTimeOfSystem() {
        assertEquals(LocalDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS), DateUtils.getLocalDateTimeOfSystem());
    }

    @Test
    public void isDateInTheFuture() {
        assertFalse(DateUtils.isDateInTheFuture(LocalDate.of(2022, 6, 11)));
        assertFalse(DateUtils.isDateInTheFuture(LocalDate.of(2022, 6, 12)));
        assertTrue(DateUtils.isDateInTheFuture(LocalDate.of(2022, 6, 13)));
    }

    @Test
    public void getBusinesLocalDate() {
        assertEquals(LocalDate.of(2022, 6, 12), DateUtils.getBusinessLocalDate());
    }
}
