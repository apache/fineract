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
package org.apache.fineract.infrastructure.core.auditing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomDateTimeProviderTest {

    @BeforeEach
    public void init() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void instanceDateProvider() {
        Optional<TemporalAccessor> dateTimeProvider = CustomDateTimeProvider.INSTANCE.getNow();
        LocalDateTime now = DateUtils.getLocalDateTimeOfSystem();
        assertTrue(dateTimeProvider.isPresent());
        assertTrue(dateTimeProvider.get() instanceof LocalDateTime);

        assertEquals(now.getYear(), ((LocalDateTime) dateTimeProvider.get()).getYear());
        assertEquals(now.getMonth(), ((LocalDateTime) dateTimeProvider.get()).getMonth());
        assertEquals(now.getDayOfMonth(), ((LocalDateTime) dateTimeProvider.get()).getDayOfMonth());
        assertEquals(now.getHour(), ((LocalDateTime) dateTimeProvider.get()).getHour());
        assertEquals(now.getMinute(), ((LocalDateTime) dateTimeProvider.get()).getMinute());
    }

    @Test
    public void tenantDateProvider() {
        Optional<TemporalAccessor> dateTimeProvider = CustomDateTimeProvider.UTC.getNow();
        OffsetDateTime now = DateUtils.getAuditOffsetDateTime();
        assertTrue(dateTimeProvider.isPresent());
        assertTrue(dateTimeProvider.get() instanceof OffsetDateTime);

        assertEquals(now.getYear(), ((OffsetDateTime) dateTimeProvider.get()).getYear());
        assertEquals(now.getMonth(), ((OffsetDateTime) dateTimeProvider.get()).getMonth());
        assertEquals(now.getDayOfMonth(), ((OffsetDateTime) dateTimeProvider.get()).getDayOfMonth());
        assertEquals(now.getHour(), ((OffsetDateTime) dateTimeProvider.get()).getHour());
        assertEquals(now.getMinute(), ((OffsetDateTime) dateTimeProvider.get()).getMinute());
    }
}
