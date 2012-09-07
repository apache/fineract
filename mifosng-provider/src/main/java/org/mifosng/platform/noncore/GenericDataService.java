package org.mifosng.platform.noncore;

import javax.sql.rowset.CachedRowSet;

import org.mifosng.platform.api.data.GenericResultsetData;

public interface GenericDataService {

	CachedRowSet getCachedResultSet(String sql, String errorMsg);

	void updateSQL(String sql, String sqlErrorMsg);

	GenericResultsetData fillGenericResultSet(final String sql);

	String replace(String str, String pattern, String replace);

	String wrapSQL(String sql);

	String getDatabaseName();

}