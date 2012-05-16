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

import org.mifosng.data.AdditionalFieldsSet;
import org.mifosng.data.AdditionalFieldsSets;
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

		String sql;
		if (name.equals(".")) {
			sql = "select r.report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,"
					+ " rp.parameter_id, rp.report_parameter_name, p.parameter_name"
					+ " from stretchy_report r"
					+ " left join stretchy_report_parameter rp on rp.report_id = r.report_id"
					+ " left join stretchy_parameter p on p.parameter_id = rp.parameter_id"
					+ " order by r.report_name, rp.parameter_id";
		} else {
			try {
				sql = getSQLtoRun(name, type, queryParams);
			} catch (SQLException e) {
				logger.info(name + ": Failed in getSQLtoRun");
				throw new WebApplicationException(Response
						.status(Status.BAD_REQUEST).entity(e.getMessage())
						.build());
			}
		}

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
			final Map<String, String> queryParams) throws SQLException {
		String sql = null;
		String rptDB = queryParams.get("${rptDB}");
		if ((rptDB == null) || rptDB.equals("")) {
			rptDB = reportingMetaDataDB;
		}

		if (type.equals("report")) {
			sql = getReportSql(rptDB, name);
		} else {
			sql = getParameterSql(rptDB, name);
		}

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
	public AdditionalFieldsSets retrieveExtraDatasetNames(String type) {

		List<AdditionalFieldsSet> additionalFieldsSets = new ArrayList<AdditionalFieldsSet>();
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
			String sql = "select d.id, d.`name` as 'set', t.`name` as 'type' from stretchydata_dataset d join stretchydata_datasettype t on t.id = d.datasettype_id "
					+ whereClause + " order by d.`name`";

			ResultSet rs = db_statement.executeQuery(sql);

			while (rs.next()) {
				additionalFieldsSets.add(new AdditionalFieldsSet(rs
						.getInt("id"), rs.getString("set"), rs
						.getString("type")));
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

		return new AdditionalFieldsSets(additionalFieldsSets);
	}

	@Override
	public GenericResultset retrieveExtraData(String type, String set, String id) {

		if (type == null) {
			logger.info("Type not Found");
			return null;
		}
		if (set == null) {
			logger.info("Set not Found");
			return null;
		}
		if (id == null) {
			logger.info("Id not Found");
			return null;
		}

		long startTime = System.currentTimeMillis();
		logger.info("STARTING SET: " + set + "   ID: " + id);

		try {
			GenericResultset result = fillExtraDataGenericResultSet(type, set,
					id);

			long elapsed = System.currentTimeMillis() - startTime;
			logger.info("FINISHING SET: " + set + "     Elapsed Time: "
					+ elapsed);
			return result;
		} catch (SQLException e) {
			logger.info("Error - SQL: " + e.toString());
			throw new InvalidSqlException(e, e.toString());
		}
	}

	private GenericResultset fillExtraDataGenericResultSet(String type,
			String set, String id) throws SQLException {

		GenericResultset result = new GenericResultset();
		String fullDatasetName = getFullDatasetName(type, set);
		Connection db_connection = dataSource.getConnection();
		Statement db_statement1 = db_connection.createStatement();
		Statement db_statement2 = db_connection.createStatement();
		Statement db_statement3 = db_connection.createStatement();
		String sql = "select f.`name`, f.data_type, f.data_length, f.display_type, f.allowed_list_id from stretchydata_datasettype t join stretchydata_dataset d on d.datasettype_id = t.id join stretchydata_dataset_fields f on f.dataset_id = d.id where d.`name` = '"
				+ set + "' and t.`name` = '" + type + "' order by f.id";

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
				+ "` where id = " + id;

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

	private String getFullDatasetName(final String type, final String set) {
		return type + "_extra_" + set;
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
	public void updateExtraData(String type, String set, String id,
			Map<String, String> queryParams) {
		logger.info("updateExtraData - type: " + type + "    set: " + set
				+ "  id: " + id);

		logger.info("startjpw: ");
		Set<String> keys = queryParams.keySet();
		String pValue = "";
		for (String key : keys) {
			pValue = queryParams.get(key);
			logger.info("jpw: " + key + " - " + pValue);
		}

		logger.info("endjpw: ");

		String fullDatasetName = getFullDatasetName(type, set);
		String saveSql = getSaveSql(fullDatasetName, id, queryParams);
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

	private String getSaveSql(String fullSetName, String id,
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
			saveSql = "update `" + fullSetName + "` ";

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

			saveSql += " where id = " + id;
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

			saveSql = "insert into `" + fullSetName + "` (id" + insertColumns
					+ ")" + " select " + id + " as id" + selectColumns;
		}
		// logger.info("Save SQL: " + saveSql);
		return saveSql;
	}
}