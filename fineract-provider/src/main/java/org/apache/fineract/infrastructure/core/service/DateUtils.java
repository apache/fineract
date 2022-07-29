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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;

public final class DateUtils {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = DEFAULT_DATE_FORMAT + " HH:mm:ss";
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATER = new DateTimeFormatterBuilder().appendPattern(DEFAULT_DATETIME_FORMAT)
            .toFormatter();
    public static final DateTimeFormatter DEFAULT_DATE_FORMATER = new DateTimeFormatterBuilder().appendPattern(DEFAULT_DATE_FORMAT)
            .toFormatter();

    private DateUtils() {

    }

    public static ZoneId getSystemZoneId() {
        return ZoneId.systemDefault();
    }

    public static ZoneId getDateTimeZoneOfTenant() {
        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        return ZoneId.of(tenant.getTimezoneId());
    }

    public static LocalDate getLocalDateOfTenant() {
        final ZoneId zone = getDateTimeZoneOfTenant();
        return LocalDate.now(zone);
    }

    public static LocalDateTime getLocalDateTimeOfTenant() {
        final ZoneId zone = getDateTimeZoneOfTenant();
        return LocalDateTime.now(zone).truncatedTo(ChronoUnit.SECONDS);
    }

    public static OffsetDateTime getOffsetDateTimeOfTenant() {
        final ZoneId zone = getDateTimeZoneOfTenant();
        return OffsetDateTime.now(zone).truncatedTo(ChronoUnit.SECONDS);
    }

    public static LocalDateTime getLocalDateTimeOfSystem() {
        return LocalDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
    }

    public static boolean isDateInTheFuture(final LocalDate localDate) {
        return localDate.isAfter(getBusinessLocalDate());
    }

    public static LocalDate getBusinessLocalDate() {
        return ThreadLocalContextUtil.getBusinessDate();
    }

}
