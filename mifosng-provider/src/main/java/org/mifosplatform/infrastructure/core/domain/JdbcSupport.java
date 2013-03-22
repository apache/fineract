/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.domain;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * Support for retrieving possibly null values from jdbc recordset delegating to
 * springs {@link JdbcUtils} where possible.
 */
public class JdbcSupport {

    public static DateTime getDateTime(final ResultSet rs, final String columnName) throws SQLException {
        DateTime dateTime = null;
        Timestamp dateValue = rs.getTimestamp(columnName);
        if (dateValue != null) {
            dateTime = new DateTime(dateValue.getTime());
        }
        return dateTime;
    }

    public static LocalDate getLocalDate(final ResultSet rs, final String columnName) throws SQLException {
        LocalDate localDate = null;
        final Date dateValue = rs.getDate(columnName);
        if (dateValue != null) {
            localDate = new LocalDate(dateValue);
        }
        return localDate;
    }

    public static Long getLong(final ResultSet rs, final String columnName) throws SQLException {
        return (Long) JdbcUtils.getResultSetValue(rs, rs.findColumn(columnName), Long.class);
    }

    public static Integer getInteger(final ResultSet rs, final String columnName) throws SQLException {
        return (Integer) JdbcUtils.getResultSetValue(rs, rs.findColumn(columnName), Integer.class);
    }

    public static BigDecimal getBigDecimalDefaultToZeroIfNull(final ResultSet rs, final String columnName) throws SQLException {
        final BigDecimal value = rs.getBigDecimal(columnName);
        return defaultToZeroIfNull(value);
    }

    private static BigDecimal defaultToZeroIfNull(final BigDecimal value) {
        BigDecimal result = BigDecimal.ZERO;
        if (value != null) {
            result = value;
        }
        return result;
    }

    public static BigDecimal getBigDecimalDefaultToNullIfZero(final ResultSet rs, final String columnName) throws SQLException {
        final BigDecimal value = rs.getBigDecimal(columnName);
        return defaultToNullIfZero(value);
    }

    private static BigDecimal defaultToNullIfZero(final BigDecimal value) {
        BigDecimal result = value;
        if (value != null && BigDecimal.ZERO.compareTo(value) == 0) {
            result = null;
        }
        return result;
    }
}
