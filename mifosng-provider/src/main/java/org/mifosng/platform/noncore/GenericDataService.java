package org.mifosng.platform.noncore;

import java.sql.Connection;
import java.sql.Statement;

import org.mifosng.platform.api.data.GenericResultsetData;

public interface GenericDataService {

	GenericResultsetData fillGenericResultSet(final String sql);

	String replace(String str, String pattern, String replace);

	void dbClose(Statement db_statement, Connection db_connection);

}