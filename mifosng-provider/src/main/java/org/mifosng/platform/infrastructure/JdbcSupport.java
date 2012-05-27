package org.mifosng.platform.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * Support for retrieving possibly null values from jdbc recordset delegating to springs {@link JdbcUtils} where possible.
 */
public class JdbcSupport {

	public static DateTime getDateTime(ResultSet rs, String columnName) throws SQLException {
		DateTime dateTime = null;
		Timestamp dateValue = rs.getTimestamp(columnName);
		if (dateValue != null) {
			dateTime = new DateTime(dateValue.getTime());
		}
		return dateTime;
	}
	
	public static LocalDate getLocalDate(final ResultSet rs, final String columnName) throws SQLException {
		LocalDate localDate = null;
		Date dateValue = rs.getDate(columnName);
		if (dateValue != null) {
			localDate = new LocalDate(dateValue);
		}
		return localDate;
	}

	public static Long getLong(final ResultSet rs, final String columnName) throws SQLException {
		return (Long) JdbcUtils.getResultSetValue(rs, rs.findColumn(columnName), Long.class);
	}

	public static Integer getInteger(ResultSet rs, String columnName) throws SQLException {
		return (Integer) JdbcUtils.getResultSetValue(rs, rs.findColumn(columnName), Integer.class);
	}
}
