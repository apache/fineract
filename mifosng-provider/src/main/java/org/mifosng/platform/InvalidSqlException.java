package org.mifosng.platform;

import java.sql.SQLException;

public class InvalidSqlException extends RuntimeException {

	private final String sql;

	public InvalidSqlException(SQLException e, String sql) {
		super(e);
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}
}
