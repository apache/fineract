package org.mifosng.platform.noncore;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.rowset.CachedRowSet;

import org.mifosng.platform.api.data.GenericResultsetData;

public interface GenericDataService {

	CachedRowSet getCachedResultSet(String sql, String errorMsg);

	void updateSQL(String sql, String sqlErrorMsg);

	GenericResultsetData fillGenericResultSet(final String sql);

	String replace(String str, String pattern, String replace);

	String wrapSQL(String sql);

	void dbClose(Statement db_statement, Connection db_connection);

}