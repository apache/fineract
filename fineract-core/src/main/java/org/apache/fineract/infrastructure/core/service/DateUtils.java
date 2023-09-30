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

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.jetbrains.annotations.NotNull;

public final class DateUtils {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = DEFAULT_DATE_FORMAT + " HH:mm:ss";
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);

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

    public static OffsetDateTime getOffsetDateTimeOfTenantWithMostPrecision() {
        final ZoneId zone = getDateTimeZoneOfTenant();
        return OffsetDateTime.now(zone);
    }

    public static LocalDateTime getLocalDateTimeOfSystem() {
        return LocalDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
    }

    public static LocalDateTime getAuditLocalDateTime() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    public static OffsetDateTime getAuditOffsetDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    public static boolean isDateInTheFuture(final LocalDate localDate) {
        return localDate.isAfter(getBusinessLocalDate());
    }

    public static LocalDate getBusinessLocalDate() {
        return ThreadLocalContextUtil.getBusinessDate();
    }

    public static long getDifferenceInDays(final LocalDate localDateBefore, final LocalDate localDateAfter) {
        return DAYS.between(localDateBefore, localDateAfter);
    }

    public static int compareToBusinessDate(LocalDate date) {
        return compare(date, getBusinessLocalDate());
    }

    public static boolean isEqualBusinessDate(LocalDate date) {
        return isEqual(date, getBusinessLocalDate());
    }

    public static boolean isBeforeBusinessDate(LocalDate date) {
        return isBefore(date, getBusinessLocalDate());
    }

    public static boolean isAfterBusinessDate(LocalDate date) {
        return isAfter(date, getBusinessLocalDate());
    }

    public static int compare(LocalDate first, LocalDate second) {
        return first == null ? (second == null ? 0 : -1) : first.compareTo(second);
    }

    public static boolean isEqual(LocalDate first, LocalDate second) {
        return first == null ? second == null : (second != null && first.isEqual(second));
    }

    public static boolean isBefore(LocalDate first, LocalDate second) {
        return second != null && (first == null || first.isBefore(second));
    }

    public static boolean isAfter(LocalDate first, LocalDate second) {
        return first != null && (second == null || first.isAfter(second));
    }

    public static LocalDate parseLocalDate(String stringDate) {
        return parseLocalDate(stringDate, null);
    }

    public static LocalDate parseLocalDate(String stringDate, String format) {
        return parseLocalDate(stringDate, format, null);
    }

    public static LocalDate parseLocalDate(String stringDate, String format, Locale locale) {
        if (stringDate == null) {
            return null;
        }
        DateTimeFormatter formatter = getDateFormatter(format, locale);
        try {
            return LocalDate.parse(stringDate, formatter);
        } catch (final DateTimeParseException e) {
            final List<ApiParameterError> errors = List.of(ApiParameterError.parameterError("validation.msg.invalid.date.pattern",
                    "The parameter date (" + stringDate + ") format is invalid", "date", stringDate));
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", errors, e);
        }
    }

    public static String format(LocalDate date) {
        return format(date, null);
    }

    public static String format(LocalDate date, String format) {
        return format(date, format, null);
    }

    public static String format(LocalDate date, String format, Locale locale) {
        return date == null ? null : date.format(getDateFormatter(format, locale));
    }

    public static String format(LocalDateTime dateTime) {
        return format(dateTime, null);
    }

    public static String format(LocalDateTime dateTime, String format) {
        return format(dateTime, format, null);
    }

    public static String format(LocalDateTime dateTime, String format, Locale locale) {
        return dateTime == null ? null : dateTime.format(getDateTimeFormatter(format, locale));
    }

    @NotNull
    private static DateTimeFormatter getDateFormatter(String format, Locale locale) {
        DateTimeFormatter formatter = DEFAULT_DATE_FORMATTER;
        if (format != null || locale != null) {
            if (format == null) {
                format = DEFAULT_DATE_FORMAT;
            }
            formatter = locale == null ? DateTimeFormatter.ofPattern(format) : DateTimeFormatter.ofPattern(format, locale);
        }
        return formatter;
    }

    @NotNull
    private static DateTimeFormatter getDateTimeFormatter(String format, Locale locale) {
        DateTimeFormatter formatter = DEFAULT_DATETIME_FORMATTER;
        if (format != null || locale != null) {
            if (format == null) {
                format = DEFAULT_DATETIME_FORMAT;
            }
            formatter = locale == null ? DateTimeFormatter.ofPattern(format) : DateTimeFormatter.ofPattern(format, locale);
        }
        return formatter;
    }
}
