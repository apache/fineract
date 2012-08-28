package org.mifosng.platform.noncore;

import java.sql.Connection;
import java.sql.Statement;

import org.mifosng.platform.api.data.GenericResultsetData;

import com.sun.rowset.CachedRowSetImpl;

public interface GenericDataService {

	public CachedRowSetImpl getCachedResultSet(String sql, String errorMsg);

	GenericResultsetData fillGenericResultSet(final String sql);

	String replace(String str, String pattern, String replace);

	void dbClose(Statement db_statement, Connection db_connection);

}