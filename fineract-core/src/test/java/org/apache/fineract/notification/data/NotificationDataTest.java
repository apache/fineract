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
package org.apache.fineract.notification.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationDataTest {

    private static final Instant CREATED = Instant.parse("2026-01-15T10:06:19.900905Z");

    private TimeZone systemTimeZone;

    @BeforeEach
    void setUp() {
        systemTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
        TimeZone.setDefault(systemTimeZone);
    }

    @Test
    void createdAtKeepsServerTimeAndCreatedAtTenantHasTenantOffset() {
        LocalDateTime storedSystemTime = LocalDateTime.ofInstant(CREATED, ZoneId.systemDefault());

        NotificationData data = new NotificationData(1L, "client", 100L, 1L, "created", "New client created", false, false,
                storedSystemTime);

        assertThat(data.getCreatedAt()).isEqualTo("2026-01-15T10:06:19.900905");
        assertThat(data.getCreatedAtTenant()).isEqualTo(OffsetDateTime.parse("2026-01-15T15:36:19.900905+05:30"));
        assertThat(data.getCreatedAtTenant().toInstant()).isEqualTo(CREATED);
    }

    @Test
    void nullCreatedAtStaysNull() {
        NotificationData data = new NotificationData(1L, "client", 100L, 1L, "created", "New client created", false, false, null);

        assertThat(data.getCreatedAt()).isNull();
        assertThat(data.getCreatedAtTenant()).isNull();
    }
}
