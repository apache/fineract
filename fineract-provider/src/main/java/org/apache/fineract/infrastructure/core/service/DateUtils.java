/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class DateUtils {

    public static DateTimeZone getDateTimeZoneOfTenant() {
        final MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        DateTimeZone zone = null;
        if (tenant != null) {
            zone = DateTimeZone.forID(tenant.getTimezoneId());
            TimeZone.getTimeZone(tenant.getTimezoneId());
        }
        return zone;
    }

    public static TimeZone getTimeZoneOfTenant() {
        final MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        TimeZone zone = null;
        if (tenant != null) {
            zone = TimeZone.getTimeZone(tenant.getTimezoneId());
        }
        return zone;
    }

    public static Date getDateOfTenant() {
        return getLocalDateOfTenant().toDateTimeAtStartOfDay().toDate();
    }

    public static LocalDate getLocalDateOfTenant() {

        LocalDate today = new LocalDate();

        final DateTimeZone zone = getDateTimeZoneOfTenant();
        if (zone != null) {
            today = new LocalDate(zone);
        }

        return today;
    }

    public static LocalDateTime getLocalDateTimeOfTenant() {

        LocalDateTime today = new LocalDateTime();

        final DateTimeZone zone = getDateTimeZoneOfTenant();
        if (zone != null) {
            today = new LocalDateTime(zone);
        }

        return today;
    }

    public static LocalDate parseLocalDate(final String stringDate, final String pattern) {

        try {
            final DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern(pattern);
            dateStringFormat.withZone(getDateTimeZoneOfTenant());
            final DateTime dateTime = dateStringFormat.parseDateTime(stringDate);
            return dateTime.toLocalDate();
        } catch (final IllegalArgumentException e) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.date.pattern", "The parameter date ("
                    + stringDate + ") is invalid w.r.t. pattern " + pattern, "date", stringDate, pattern);
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public static String formatToSqlDate(final Date date) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(getTimeZoneOfTenant());
        final String formattedSqlDate = df.format(date);
        return formattedSqlDate;
    }

    public static boolean isDateInTheFuture(final LocalDate localDate) {
        return localDate.isAfter(getLocalDateOfTenant());
    }
}