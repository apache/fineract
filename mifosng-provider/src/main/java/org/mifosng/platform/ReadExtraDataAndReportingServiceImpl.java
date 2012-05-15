package org.mifosng.platform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.mifosng.data.ExtraDatasetRow;
import org.mifosng.data.ExtraDatasets;
import org.mifosng.data.reports.GenericResultset;
import org.mifosng.data.reports.ResultsetColumnHeader;
import org.mifosng.data.reports.ResultsetDataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReadExtraDataAndReportingServiceImpl implements
		ReadExtraDataAndReportingService {

	private final static Logger logger = LoggerFactory
			.getLogger(ReadExtraDataAndReportingServiceImpl.class);

	private final DataSource dataSource;
	private final String reportingMetaDataDB;

	@Autowired
	public ReadExtraDataAndReportingServiceImpl(final DataSource dataSource)
			throws SQLException {
		this.dataSource = dataSource;
		Connection db_connection = dataSource.getConnection();
		this.reportingMetaDataDB = db_connection.getCatalog();
		db_connection.close();
		db_connection = null;
	}

	@Override
	public StreamingOutput retrieveReportCSV(final String name,
			final String type, final Map<String, String> queryParams) {

		return new StreamingOutput() {
			
			@Override
			public void write(OutputStream out) throws IOException,
					WebApplicationException {
				try {

					GenericResultset result = retrieveGenericResultset(name,
							type, queryParams);
					StringBuffer sb = generateCsvFileBuffer(result);

					InputStream in = new ByteArrayInputStream(sb.toString()
							.getBytes("UTF-8"));

					byte[] outputByte = new byte[4096];
					Integer readLen = in.read(outputByte, 0, 4096);

					while (readLen != -1) {
						out.write(outputByte, 0, readLen);
						readLen = in.read(outputByte, 0, 4096);
					}
					// in.close();
					// out.flush();
					// out.close();
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};

	}

	private static StringBuffer generateCsvFileBuffer(GenericResultset result) {
		StringBuffer writer = new StringBuffer();

		List<ResultsetColumnHeader> columnHeaders = result.getColumnHeaders();
		logger.info("NO. of Columns: " + columnHeaders.size());
		Integer chSize = columnHeaders.size();
		for (int i = 0; i < chSize; i++) {
			writer.append('"' + columnHeaders.get(i).getColumnName() + '"');
			if (i < (chSize - 1))
				writer.append(",");
		}
		writer.append('\n');

		List<ResultsetDataRow> data = result.getData();
		List<String> row;
		Integer rSize;
		// String currCol;
		String currColType;
		String currVal;
		logger.info("NO. of Rows: " + data.size());
		for (int i = 0; i < data.size(); i++) {
			row = data.get(i).getRow();
			rSize = row.size();
			for (int j = 0; j < rSize; j++) {
				// currCol = columnHeaders.get(j).getColumnName();
				currColType = columnHeaders.get(j).getColumnType();
				currVal = row.get(j);
				if (currVal != null) {
					if (currColType.equals("DECIMAL")
							|| currColType.equals("DOUBLE")
							|| currColType.equals("BIGINT")
							|| currColType.equals("SMALLINT")
							|| currColType.equals("INT"))
						writer.append(currVal);
					else
						writer.append('"' + currVal + '"');
				}
				if (j < (rSize - 1))
					writer.append(",");
			}
			writer.append('\n');
		}

		return writer;
	}

	@Override
	public GenericResultset retrieveGenericResultset(final String name,
			final String type, final Map<String, String> queryParams) {

		if (name == null) {
			logger.info("Report Name not Found");
			return null;
		}

		long startTime = System.currentTimeMillis();
		logger.info("STARTING REPORT: " + name + "   Type: " + type);

		// AppUser currentUser = extractAuthenticatedUser();
		// Collection<GrantedAuthority> permissions =
		// currentUser.getAuthorities();
		/*
		 * AppUser currentUser = extractAuthenticatedUser(); Boolean validUser =
		 * verifyUserDetails(currentUser);
		 * 
		 * if (!validUser) { return null; }
		 * 
		 * String orgId = currentUser.getOrganisation().getId().toString(); put
		 * back in later
		 */
		String orgId = "1";

		String sql;
		try {
			sql = getSQLtoRun(name, type, orgId, queryParams);
		} catch (SQLException e) {
			logger.info(name + ": Failed in getSQLtoRun");
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
		}
		// logger.info(name + ": RUNNING SQL");

		GenericResultset result = null;
		try {
			result = fillReportingGenericResultSet(sql);
		} catch (SQLException e) {
			logger.info("Error - SQL: " + sql);
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
		}

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING Report/Request Name: " + name + " - " + type
				+ "     Elapsed Time: " + elapsed);
		return result;
	}

	private GenericResultset fillReportingGenericResultSet(final String sql)
			throws SQLException {

		GenericResultset result = new GenericResultset();

		Connection db_connection = dataSource.getConnection();
		Statement db_statement = db_connection.createStatement();
		ResultSet rs = db_statement.executeQuery(sql);

		ResultSetMetaData rsmd = rs.getMetaData();
		String columnName = null;
		String columnValue = null;
		List<ResultsetColumnHeader> columnHeaders = new ArrayList<ResultsetColumnHeader>();
		for (int i = 0; i < rsmd.getColumnCount(); i++) {
			ResultsetColumnHeader rsch = new ResultsetColumnHeader();
			rsch.setColumnName(rsmd.getColumnName(i + 1));
			rsch.setColumnType(rsmd.getColumnTypeName(i + 1));
			columnHeaders.add(rsch);
		}
		result.setColumnHeaders(columnHeaders);

		List<ResultsetDataRow> resultsetDataRows = new ArrayList<ResultsetDataRow>();
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
		result.setData(resultsetDataRows);

		db_statement.close();
		db_statement = null;
		db_connection.close();
		db_connection = null;

		return result;

	}

	private String getSQLtoRun(final String name, final String type,
			final String orgId, final Map<String, String> queryParams)
			throws SQLException {
		String sql = null;
		String rptDB = queryParams.get("${rptDB}");
		if ((rptDB == null) || rptDB.equals("")) {
			rptDB = reportingMetaDataDB;
		}

		if (type.equals("report")) {
			sql = getReportSql(rptDB, name);
		} else {
			// todo - dont need to check for orgID if special parameter sql (but
			// prob need to check restrictions
			sql = getParameterSql(rptDB, name);
		}

		sql = replace(sql, "${orgId}", orgId);

		Set<String> keys = queryParams.keySet();
		for (String key : keys) {
			String pValue = queryParams.get(key);
			// logger.info("(" + key + " : " + pValue + ")");
			sql = replace(sql, key, pValue);
		}

		// wrap sql to prevent JDBC sql errors and also prevent malicious sql
		sql = "select x.* from (" + sql + ") x";

		return sql;

	}

	// private Boolean verifyUserDetails(AppUser usr) {
	//
	// // some logs to be taken out after testing
	// String idDetails = usr.getId() + ", " + usr.getLastname() + ", "
	// + usr.getFirstname();
	// logger.info("Id: " + idDetails + "   Organisation: "
	// + usr.getOrganisation().getId() + "   Office: "
	// + usr.getOffice().getId() + "   Role Names: "
	// + usr.getRoleNames());
	// String otherDetails = "Head Officer User? " + usr.isHeadOfficeUser()
	// + "  Enabled: " + usr.isEnabled();
	// logger.info(otherDetails);
	// if (usr.getAuthorities() != null) {
	// // for (GrantedAuthority grantedAuthority : usr.getAuthorities()) {
	// // logger.info("Granted Authority: " +
	// // grantedAuthority.getAuthority());
	// // }
	// }
	// logger.info("");
	//
	// // some checks
	// if (usr.getOrganisation().getId() == null) {
	// logger.info("Organisation ID not Found");
	// return false;
	// }
	//
	// return true;
	// }

	private String getReportSql(String rptDB, String reportName)
			throws SQLException {
		String sql = "select report_sql as the_sql from " + rptDB
				+ ".stretchy_report where report_name = '" + reportName + "'";
		logger.info("Report SQL: " + sql);

		return getSql(sql);
	}

	private String getParameterSql(String rptDB, String parameterName)
			throws SQLException {
		String sql = "select parameter_sql as the_sql from " + rptDB
				+ ".stretchy_parameter where parameter_name = '"
				+ parameterName + "'";
		logger.info("Parameter SQL: " + sql);

		return getSql(sql);
	}

	private String getSql(String inputSql) throws SQLException {

		Connection db_connection = dataSource.getConnection();
		Statement db_statement = db_connection.createStatement();
		ResultSet rs = db_statement.executeQuery(inputSql);

		String sql = null;

		while (rs.next()) {
			sql = rs.getString("the_sql");
		}

		db_statement.close();
		db_statement = null;
		db_connection.close();
		db_connection = null;

		return sql;
	}

	@Override
	public ExtraDatasets retrieveExtraDatasetNames(String type) {

		List<ExtraDatasetRow> extraDatasetRows = new ArrayList<ExtraDatasetRow>();
		Connection db_connection;
		try {
			db_connection = dataSource.getConnection();
			Statement db_statement = db_connection.createStatement();

			String whereClause;
			if (type == null) {
				whereClause = "";
			} else {
				whereClause = "where t.`name` = '" + type + "'";
			}
			String sql = "select d.id, d.`name` as datasetName, t.`name` as datasetType from stretchydata_dataset d join stretchydata_datasettype t on t.id = d.datasettype_id "
					+ whereClause + " order by d.`name`";

			ResultSet rs = db_statement.executeQuery(sql);

			while (rs.next()) {
				extraDatasetRows
						.add(new ExtraDatasetRow(rs.getInt("id"), rs
								.getString("datasetName"), rs
								.getString("datasetType")));
			}

			db_statement.close();
			db_statement = null;
			db_connection.close();
			db_connection = null;
		} catch (SQLException e) {
			logger.info(": Failed in retrieveExtraDatasetNames");
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
		}

		return new ExtraDatasets(extraDatasetRows);
	}

	@Override
	public GenericResultset retrieveExtraData(String datasetType,
			String datasetName, String datasetPKValue) {

		if (datasetType == null) {
			logger.info("Extra Data Table Type not Found");
			return null;
		}
		if (datasetName == null) {
			logger.info("Extra Data Table Name not Found");
			return null;
		}
		if (datasetPKValue == null) {
			logger.info("Extra Data Table ID not Found");
			return null;
		}

		long startTime = System.currentTimeMillis();
		logger.info("STARTING EXTRA DATA TABLE: " + datasetName + "   ID: "
				+ datasetPKValue);

		try {
			GenericResultset result = fillExtraDataGenericResultSet(
					datasetType, datasetName, datasetPKValue);

			long elapsed = System.currentTimeMillis() - startTime;
			logger.info("FINISHING EXTRA DATA TABLE: " + datasetName
					+ "     Elapsed Time: " + elapsed);
			return result;
		} catch (SQLException e) {
			logger.info("Error - SQL: " + e.toString());
			throw new InvalidSqlException(e, e.toString());
		}
	}

	private GenericResultset fillExtraDataGenericResultSet(String datasetType,
			String datasetName, String datasetPKValue) throws SQLException {

		GenericResultset result = new GenericResultset();
		String fullDatasetName = getFullDatasetName(datasetType, datasetName);
		Connection db_connection = dataSource.getConnection();
		Statement db_statement1 = db_connection.createStatement();
		Statement db_statement2 = db_connection.createStatement();
		Statement db_statement3 = db_connection.createStatement();
		String sql = "select f.`name`, f.data_type, f.data_length, f.display_type, f.allowed_list_id from stretchydata_datasettype t join stretchydata_dataset d on d.datasettype_id = t.id join stretchydata_dataset_fields f on f.dataset_id = d.id where d.`name` = '"
				+ datasetName
				+ "' and t.`name` = '"
				+ datasetType
				+ "' order by f.id";

		// logger.info("specific: " + sql);
		ResultSet rsmd = db_statement1.executeQuery(sql);

		List<ResultsetColumnHeader> columnHeaders = new ArrayList<ResultsetColumnHeader>();
		Boolean firstColumn = true;
		Integer allowedListId;
		String selectFieldList = "";
		String selectFieldSeparator = "";
		while (rsmd.next()) {
			ResultsetColumnHeader rsch = new ResultsetColumnHeader();
			rsch.setColumnName(rsmd.getString("name"));

			if (firstColumn) {
				selectFieldSeparator = " ";
				firstColumn = false;
			} else {
				selectFieldSeparator = ", ";
			}
			selectFieldList += selectFieldSeparator + "`"
					+ rsch.getColumnName() + "`";

			rsch.setColumnType(rsmd.getString("data_type"));
			rsch.setColumnLength(rsmd.getInt("data_length"));
			rsch.setColumnDisplayType(rsmd.getString("display_type"));
			allowedListId = rsmd.getInt("allowed_list_id");
			if (allowedListId != null) {
				sql = "select v.`name` from stretchydata_allowed_value v where allowed_list_id = "
						+ allowedListId + " order by id";
				ResultSet rsValues = db_statement2.executeQuery(sql);
				while (rsValues.next()) {
					rsch.getColumnValues().add(rsValues.getString("name"));
				}
			}
			columnHeaders.add(rsch);
		}
		result.setColumnHeaders(columnHeaders);

		sql = "select " + selectFieldList + " from `" + fullDatasetName
				+ "` where id = " + datasetPKValue;

		ResultSet rs = db_statement3.executeQuery(sql);
		String columnName = null;
		String columnValue = null;
		List<ResultsetDataRow> resultsetDataRows = new ArrayList<ResultsetDataRow>();
		ResultsetDataRow resultsetDataRow;
		while (rs.next()) {
			resultsetDataRow = new ResultsetDataRow();
			List<String> columnValues = new ArrayList<String>();

			for (int i = 0; i < columnHeaders.size(); i++) {
				columnName = columnHeaders.get(i).getColumnName();
				columnValue = rs.getString(columnName);
				columnValues.add(columnValue);
			}
			resultsetDataRow.setRow(columnValues);
			resultsetDataRows.add(resultsetDataRow);
		}
		result.setData(resultsetDataRows);

		db_statement1.close();
		db_statement1 = null;
		db_statement2.close();
		db_statement2 = null;
		db_statement3.close();
		db_statement3 = null;
		db_connection.close();
		db_connection = null;

		return result;

	}

	private String getFullDatasetName(final String datasetType,
			final String datasetName) {
		return datasetType + "_extra_" + datasetName;
	}

	private static String replace(String str, String pattern, String replace) {
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
	public void tempSaveExtraData(String datasetType, String datasetName,
			String datasetPKValue, Map<String, String> queryParams) {
		logger.info("SaveExtraData - DatasetType: " + datasetType
				+ "    DatasetName: " + datasetName + "  datasetPKValue: "
				+ datasetPKValue);

		logger.info("startjpw: ");
		Set<String> keys = queryParams.keySet();
		String pValue = "";
		for (String key : keys) {
			pValue = queryParams.get(key);
			logger.info("jpw: " + key + " - " + pValue);
		}

		logger.info("endjpw: ");

		String fullDatasetName = getFullDatasetName(datasetType, datasetName);
		String saveSql = getSaveSql(fullDatasetName, datasetPKValue,
				queryParams);
		try {
			Connection db_connection = dataSource.getConnection();
			Statement db_statement = db_connection.createStatement();
			db_statement.executeUpdate(saveSql);

			db_statement.close();
			db_statement = null;
			db_connection.close();
			db_connection = null;
		} catch (SQLException e) {
			logger.info("SQL: " + saveSql + "  ERROR: " + e.toString());
			throw new InvalidSqlException(e, e.toString());
		}

	}

	private String getSaveSql(String fullDatasetName, String datasetPKValue,
			Map<String, String> queryParams) {

		String errMsg = "";
		String transType = queryParams.get("ed_transType");
		if (!(transType.equals("E") || transType.equals("A"))) {
			errMsg = "transType not E or A - Value is: " + transType;
			logger.info(errMsg);
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST).entity(errMsg).build());
		}

		String pValue = "";
		String pValueWrite = "";
		String saveSql = "";
		String singleQuote = "'";
		String underscore = "_";
		String space = " ";
		Set<String> keys = queryParams.keySet();

		if (transType.equals("E")) {
			boolean firstColumn = true;
			saveSql = "update `" + fullDatasetName + "` ";

			for (String key : keys) {
				if (!(key.equals("ed_transType") || key.equals("id"))) {
					if (firstColumn) {
						saveSql += " set ";
						firstColumn = false;
					} else {
						saveSql += ", ";
					}

					pValue = queryParams.get(key);
					if (pValue == null || pValue.equals("")) {
						pValueWrite = "null";
					} else {
						pValueWrite = singleQuote
								+ replace(pValue, singleQuote, singleQuote
										+ singleQuote) + singleQuote;
					}
					saveSql += "`" + replace(key, underscore, space) + "` = "
							+ pValueWrite;
				}
			}

			saveSql += " where id = " + datasetPKValue;
		} else {
			String insertColumns = "";
			String selectColumns = "";
			String columnName = "";
			for (String key : keys) {
				pValue = queryParams.get(key);
				if (!(key.equals("ed_transType") || key.equals("id"))) {

					pValue = queryParams.get(key);
					if (pValue == null || pValue.equals("")) {
						pValueWrite = "null";
					} else {
						pValueWrite = singleQuote
								+ replace(pValue, singleQuote, singleQuote
										+ singleQuote) + singleQuote;
					}
					columnName = "`" + replace(key, underscore, space) + "`";
					insertColumns += ", " + columnName;
					selectColumns += "," + pValueWrite + " as " + columnName;
				}
			}

			saveSql = "insert into `" + fullDatasetName + "` (id"
					+ insertColumns + ")" + " select " + datasetPKValue
					+ " as id" + selectColumns;
		}
		// logger.info("Save SQL: " + saveSql);
		return saveSql;
	}
}