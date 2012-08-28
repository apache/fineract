package org.mifosng.platform.noncore;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.data.ResultsetColumnHeader;
import org.mifosng.platform.api.data.ResultsetDataRow;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.rowset.CachedRowSetImpl;

@Service
public class GenericDataServiceImpl implements GenericDataService {

	private final static Logger logger = LoggerFactory
			.getLogger(GenericDataServiceImpl.class);

	private final DataSource dataSource;

	@Autowired
	public GenericDataServiceImpl(final TenantAwareRoutingDataSource dataSource) {

		this.dataSource = dataSource;
	}

	@Override
	public CachedRowSetImpl getCachedResultSet(String sql, String errorMsg) {

		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			ResultSet rs = db_statement.executeQuery(sql);

			CachedRowSetImpl crs = new CachedRowSetImpl();
			crs.populate(rs);
			return crs;
		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), errorMsg);
		} finally {
			dbClose(db_statement, db_connection);

		}

	}

	@Override
	public GenericResultsetData fillGenericResultSet(final String sql) {

		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			ResultSet rs = db_statement.executeQuery(sql);

			List<ResultsetColumnHeader> columnHeaders = new ArrayList<ResultsetColumnHeader>();
			List<ResultsetDataRow> resultsetDataRows = new ArrayList<ResultsetDataRow>();

			ResultSetMetaData rsmd = rs.getMetaData();
			String columnName = null;
			String columnValue = null;
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				ResultsetColumnHeader rsch = new ResultsetColumnHeader();
				rsch.setColumnName(rsmd.getColumnName(i + 1));
				rsch.setColumnType(rsmd.getColumnTypeName(i + 1));
				columnHeaders.add(rsch);
			}

			ResultsetDataRow resultsetDataRow;
			while (rs.next()) {
				resultsetDataRow = new ResultsetDataRow();
				List<String> columnValues = new ArrayList<String>();
				for (int i = 0; i < rsmd.getColumnCount(); i++) {
					columnName = rsmd.getColumnName(i + 1);
					columnValue = rs.getString(columnName);
					columnValues.add(columnValue);
				}
				resultsetDataRow.setRow(columnValues);
				resultsetDataRows.add(resultsetDataRow);
			}

			return new GenericResultsetData(columnHeaders, resultsetDataRows);
		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Sql: " + sql);
		} finally {
			dbClose(db_statement, db_connection);
		}
	}

	@Override
	public String replace(String str, String pattern, String replace) {
		// JPW - this replace may / may not be any better or quicker than the
		// apache stringutils equivalent. It works, but if someone shows the
		// apache one to be about the same then this can be removed.
		int s = 0;
		int e = 0;
		StringBuffer result = new StringBuffer();

		while ((e = str.indexOf(pattern, s)) >= 0) {
			result.append(str.substring(s, e));
			result.append(replace);
			s = e + pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();
	}

	@Override
	public void dbClose(Statement db_statement, Connection db_connection) {
		logger.debug("dbClose");
		try {
			if (db_statement != null) {
				db_statement.close();
				db_statement = null;
			}
			if (db_connection != null) {
				db_connection.close();
				db_connection = null;
			}
		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Error closing database connection");
		}
	}
}