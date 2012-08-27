package org.mifosng.platform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.data.ResultsetColumnHeader;
import org.mifosng.platform.api.data.ResultsetDataRow;
import org.mifosng.platform.exceptions.AdditionalFieldsNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.exceptions.ReportNotFoundException;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.CSVReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelReportUtil;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.pentaho.reporting.engine.classic.core.parameters.ReportParameterDefinition;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReadExtraDataAndReportingServiceImpl implements
		ReadExtraDataAndReportingService {

	private final PlatformSecurityContext context;

	private final static Logger logger = LoggerFactory
			.getLogger(ReadExtraDataAndReportingServiceImpl.class);

	private final DataSource dataSource;
	private Boolean noPentaho = false;

	@Autowired
	public ReadExtraDataAndReportingServiceImpl(
			final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		// kick off pentaho reports server
		ClassicEngineBoot.getInstance().start();
		noPentaho = false;

		this.context = context;
		this.dataSource = dataSource;
	}

	@Override
	public StreamingOutput retrieveReportCSV(final String name,
			final String type, final Map<String, String> queryParams) {

		return new StreamingOutput() {

			@Override
			public void write(OutputStream out) {
				try {

					GenericResultsetData result = retrieveGenericResultset(
							name, type, queryParams);
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
					throw new PlatformDataIntegrityException(
							"error.msg.exception.error", e.getMessage());
				}
			}
		};

	}

	private static StringBuffer generateCsvFileBuffer(
			GenericResultsetData result) {
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
	public GenericResultsetData retrieveGenericResultset(final String name,
			final String type, final Map<String, String> queryParams) {

		long startTime = System.currentTimeMillis();
		logger.info("STARTING REPORT: " + name + "   Type: " + type);

		String sql;
		if (name.equals(".")) {
			// this is to support api /reports - which isn't an important
			// call. It isn't used in the default reporting UI. But there is a
			// need to bring back 'permitted' reports via this api call.
			sql = "select r.report_id, r.report_name, r.report_type, r.report_subtype, r.report_category,"
					+ " rp.parameter_id, rp.report_parameter_name, p.parameter_name"
					+ " from stretchy_report r"
					+ " left join stretchy_report_parameter rp on rp.report_id = r.report_id"
					+ " left join stretchy_parameter p on p.parameter_id = rp.parameter_id"
					+ " where exists"
					+ " (select 'f'"
					+ " from m_appuser_role ur "
					+ " join m_role r on r.id = ur.role_id"
					+ " left join m_role_permission rp on rp.role_id = r.id"
					+ " left join m_permission p on p.id = rp.permission_id"
					+ " where ur.appuser_id = "
					+ context.authenticatedUser().getId()
					+ " and (r.name = 'Super User' or r.name = 'Read Only') or p.code = concat('CAN_RUN_', r.report_name))"
					+ " order by r.report_name, rp.parameter_id";
		} else {
			sql = getSQLtoRun(name, type, queryParams);
		}

		GenericResultsetData result = fillReportingGenericResultSet(sql);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING Report/Request Name: " + name + " - " + type
				+ "     Elapsed Time: " + elapsed);
		return result;
	}

	private GenericResultsetData fillReportingGenericResultSet(final String sql) {

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

	private String getSQLtoRun(final String name, final String type,
			final Map<String, String> queryParams) {
		String sql = null;

		if (type.equals("report")) {
			sql = getReportSql(name);
		} else {
			sql = getParameterSql(name);
		}

		Set<String> keys = queryParams.keySet();
		for (String key : keys) {
			String pValue = queryParams.get(key);
			// logger.info("(" + key + " : " + pValue + ")");
			sql = replace(sql, key, pValue);
		}

		AppUser currentUser = context.authenticatedUser();
		// Allows sql query to restrict data by office hierarchy if required
		sql = replace(sql, "${currentUserHierarchy}", currentUser.getOffice()
				.getHierarchy());
		// Allows sql query to restrict data by current user Id if required
		// (typically used to return report lists containing only reports
		// permitted to be run by the user
		sql = replace(sql, "${currentUserId}", currentUser.getId().toString());

		// wrap sql to prevent JDBC sql errors and also prevent malicious sql
		sql = "select x.* from (" + sql + ") x";

		return sql;

	}

	private String getReportSql(String reportName) {
		String sql = "select report_sql as the_sql from stretchy_report where report_name = '"
				+ reportName + "'";
		return getSql(sql);
	}

	private String getParameterSql(String parameterName) {
		String sql = "select parameter_sql as the_sql from stretchy_parameter where parameter_name = '"
				+ parameterName + "'";
		return getSql(sql);
	}

	private String getSql(String inputSql) {

		String sql = null;
		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			ResultSet rs = db_statement.executeQuery(inputSql);

			if (rs.next()) {
				sql = rs.getString("the_sql");
			} else {
				throw new ReportNotFoundException(inputSql);
			}

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Input Sql: " + inputSql);
		} finally {
			dbClose(db_statement, db_connection);
		}

		return sql;
	}

	@Override
	public List<AdditionalFieldsSetData> retrieveExtraDatasetNames(String type) {

		List<AdditionalFieldsSetData> additionalFieldsSets = new ArrayList<AdditionalFieldsSetData>();

		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();

			String andClause;
			if (type == null) {
				andClause = "";
			} else {
				andClause = " and t.`name` = '" + type + "'";
			}
			String sql = "select d.id, d.`name` as 'set', t.`name` as 'type' "
					+ " from stretchydata_dataset d join stretchydata_datasettype t on t.id = d.datasettype_id "
					+ " where exists"
					+ " (select 'f'"
					+ " from m_appuser_role ur "
					+ " join m_role r on r.id = ur.role_id"
					+ " left join m_role_permission rp on rp.role_id = r.id"
					+ " left join m_permission p on p.id = rp.permission_id"
					+ " where ur.appuser_id = "
					+ context.authenticatedUser().getId()
					+ " and (r.name = 'Super User' or r.name = 'Read Only') or p.code = concat('CAN_READ_', t.`name`, '_x', d.`name`)) "
					+ andClause + " order by d.`name`";

			ResultSet rs = db_statement.executeQuery(sql);

			while (rs.next()) {
				additionalFieldsSets.add(new AdditionalFieldsSetData(rs
						.getInt("id"), rs.getString("set"), rs
						.getString("type")));
			}

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Additional Fields Type: " + type);
		} finally {
			dbClose(db_statement, db_connection);
		}

		return additionalFieldsSets;
	}

	@Override
	public String retrieveDataTable(String datatable, String sqlFields,
			String sqlSearch, String sqlOrder) {
		long startTime = System.currentTimeMillis();

		String sql = "select ";
		if (sqlFields != null)
			sql = sql + sqlFields;
		else
			sql = sql + " * ";

		sql = sql + " from " + datatable;

		if (sqlSearch != null)
			sql = sql + " where " + sqlSearch;
		if (sqlOrder != null)
			sql = sql + " order by " + sqlOrder;

		GenericResultsetData result = fillReportingGenericResultSet(sql);

		String jsonString = generateJsonFromGenericResultsetData(result);

		logger.info("JSON is: " + jsonString);
		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING DATATABLE: " + datatable + "     Elapsed Time: "
				+ elapsed);
		return jsonString;
	}

	private static String generateJsonFromGenericResultsetData(
			GenericResultsetData result) {

		StringBuffer writer = new StringBuffer();

		writer.append("[");

		List<ResultsetColumnHeader> columnHeaders = result.getColumnHeaders();
		logger.info("NO. of Columns: " + columnHeaders.size());

		List<ResultsetDataRow> data = result.getData();
		List<String> row;
		Integer rSize;
		String currColType;
		String currVal;
		logger.info("NO. of Rows: " + data.size());
		for (int i = 0; i < data.size(); i++) {
			writer.append("\n{");

			row = data.get(i).getRow();
			rSize = row.size();
			for (int j = 0; j < rSize; j++) {

				writer.append('\"' + columnHeaders.get(j).getColumnName()
						+ '\"' + ": ");
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
						writer.append('\"' + currVal + '\"');
				} else
					writer.append("null");

				if (j < (rSize - 1))
					writer.append(",\n");
			}

			if (i < (data.size() - 1))
				writer.append("},");
			else
				writer.append("}");
		}

		writer.append("\n]");
		return writer.toString();

		/*
		 * JSONObject js = null; try { js = new JSONObject(writer.toString()); }
		 * catch (JSONException e) { throw new WebApplicationException(Response
		 * .status(Status.BAD_REQUEST).entity("JSON body is wrong") .build()); }
		 * 
		 * return js.toString();
		 */

	}

	@Override
	public GenericResultsetData retrieveExtraData(String type, String set,
			Long id) {

		long startTime = System.currentTimeMillis();
		GenericResultsetData result = fillExtraDataGenericResultSet(type, set,
				id);
		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING SET: " + set + "     Elapsed Time: " + elapsed);
		return result;
	}

	private GenericResultsetData fillExtraDataGenericResultSet(
			final String type, final String set, final Long id) {

		Connection db_connection = null;
		Statement db_statement1 = null;
		Statement db_statement2 = null;
		Statement db_statement3 = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement1 = db_connection.createStatement();
			String sql = "select f.`name`, f.data_type, f.data_length, f.display_type, f.allowed_list_id from stretchydata_datasettype t join stretchydata_dataset d on d.datasettype_id = t.id join stretchydata_dataset_fields f on f.dataset_id = d.id where d.`name` = '"
					+ set + "' and t.`name` = '" + type + "' order by f.id";

			ResultSet rsmd = db_statement1.executeQuery(sql);

			List<ResultsetColumnHeader> columnHeaders = null;
			List<ResultsetDataRow> resultsetDataRows = null;

			if (rsmd.next()) {

				String fullDatasetName = getFullDatasetName(type, set);
				db_statement2 = db_connection.createStatement();
				columnHeaders = new ArrayList<ResultsetColumnHeader>();
				Boolean firstColumn = true;
				Integer allowedListId;
				String selectFieldList = "";
				String selectFieldSeparator = "";
				do {
					ResultsetColumnHeader rsch = new ResultsetColumnHeader();
					rsch.setColumnName(rsmd.getString("name"));

					if (firstColumn) {
						selectFieldSeparator = " ";
						firstColumn = false;
					} else {
						selectFieldSeparator = ", ";
					}
					selectFieldList += selectFieldSeparator + "s.`"
							+ rsch.getColumnName() + "`";

					rsch.setColumnType(rsmd.getString("data_type"));
					if (rsmd.getInt("data_length") > 0)
						rsch.setColumnLength(rsmd.getInt("data_length"));
					rsch.setColumnDisplayType(rsmd.getString("display_type"));
					allowedListId = rsmd.getInt("allowed_list_id");
					if (allowedListId > 0) {

						// logger.info("allowedListId != null: Column: " +
						// rsch.getColumnName());
						sql = "select v.`name` from stretchydata_allowed_value v where allowed_list_id = "
								+ allowedListId + " order by id";
						ResultSet rsValues = db_statement2.executeQuery(sql);
						while (rsValues.next()) {
							rsch.getColumnValues().add(
									rsValues.getString("name"));
						}
					}
					columnHeaders.add(rsch);
				} while (rsmd.next());

				String unScopedSQL = "select " + selectFieldList + " from `"
						+ type + "` t ${dataScopeCriteria} left join `"
						+ fullDatasetName + "` s on s.id = t.id "
						+ " where t.id = " + id;

				sql = dataScopedSQL(unScopedSQL, type);

				logger.info("addition fields sql: " + sql);
				db_statement3 = db_connection.createStatement();
				ResultSet rs = db_statement3.executeQuery(sql);

				if (rs.next()) {
					String columnName = null;
					String columnValue = null;
					resultsetDataRows = new ArrayList<ResultsetDataRow>();
					ResultsetDataRow resultsetDataRow;
					do {
						resultsetDataRow = new ResultsetDataRow();
						List<String> columnValues = new ArrayList<String>();

						for (int i = 0; i < columnHeaders.size(); i++) {
							columnName = columnHeaders.get(i).getColumnName();
							columnValue = rs.getString(columnName);
							columnValues.add(columnValue);
						}
						resultsetDataRow.setRow(columnValues);
						resultsetDataRows.add(resultsetDataRow);
					} while (rs.next());
				} else {
					// could also be not found because of data scope
					throw new AdditionalFieldsNotFoundException(type, id);
				}
			} else {
				throw new AdditionalFieldsNotFoundException(type, set);
			}

			return new GenericResultsetData(columnHeaders, resultsetDataRows);

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Additional Fields Type: " + type
							+ "   Set: " + set + "   Id: " + id);
		} finally {
			dbClose(db_statement2, null);
			dbClose(db_statement3, null);
			dbClose(db_statement1, db_connection);
		}
	}

	private String dataScopedSQL(String unScopedSQL, String type) {
		String dataScopeCriteria = null;
		/*
		 * unfortunately have to, one way or another, be able to restrict data
		 * to the users office hierarchy. Here it's hardcoded for client and
		 * loan. They are the main application tables. But if additional fields
		 * are needed on other tables like group, loan_transaction or others the
		 * same applies (hardcoding of some sort)
		 */

		AppUser currentUser = context.authenticatedUser();
		if (type.equalsIgnoreCase("m_client")) {
			dataScopeCriteria = " join m_office o on o.id = t.office_id and o.hierarchy like '"
					+ currentUser.getOffice().getHierarchy() + "%'";
		}
		if (type.equalsIgnoreCase("m_loan")) {
			dataScopeCriteria = " join m_client c on c.id = t.client_id "
					+ " join m_office o on o.id = c.office_id and o.hierarchy like '"
					+ currentUser.getOffice().getHierarchy() + "%'";
		}

		if (dataScopeCriteria == null) {
			throw new PlatformDataIntegrityException(
					"error.msg.invalid.dataScopeCriteria", "Type: " + type
							+ " not catered for in data Scoping");
		}

		return replace(unScopedSQL, "${dataScopeCriteria}", dataScopeCriteria);

	}

	private String getFullDatasetName(final String type, final String set) {
		return type + "_x" + set;
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
	public void updateExtraData(String type, String set, Long id,
			Map<String, String> queryParams) {
		// TODO should be in a Write class
		logger.info("updateExtraData - type: " + type + "    set: " + set
				+ "  id: " + id);

		checkResourceTypeThere(type, set);
		String fullDatasetName = getFullDatasetName(type, set);
		String transType = getTransType(type, fullDatasetName, id);

		String saveSql = getSaveSql(fullDatasetName, id, transType, queryParams);

		logger.info("saveSQL: " + saveSql);
		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			db_statement.executeUpdate(saveSql);

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Additional Fields Type: " + type
							+ "   Set: " + set + "   Id: " + id);
		} finally {
			dbClose(db_statement, db_connection);
		}

	}

	private void checkResourceTypeThere(String type, String set) {
				
		String sql = "select 'f' from stretchydata_datasettype t join stretchydata_dataset d on d.datasettype_id = t.id where d.`name` = '"
				+ set + "' and t.`name` = '" + type + "'";

		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			ResultSet rs = db_statement.executeQuery(sql);

			if (!(rs.next())) {
				throw new AdditionalFieldsNotFoundException(type, set);
			}

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Additional Fields Type: " + type
							+ "   Set: " + set + "   Sql: " + sql);
		} finally {
			dbClose(db_statement, db_connection);
		}

	}

	private String getTransType(String type, String fullDatasetName, Long id) {
		String transType = null;

		String sql = "select s.id from `" + type + "` t left join `"
				+ fullDatasetName + "` s on s.id = t.id where t.id = " + id;

		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			ResultSet rs = db_statement.executeQuery(sql);

			if (rs.next()) {
				String idValue = rs.getString("id");
				if (idValue != null) {
					transType = "E";
				} else {
					transType = "A";
				}
			} else {
				throw new AdditionalFieldsNotFoundException(type, id);
			}
		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Additional Fields Type: " + type
							+ "   Full Set Name: " + fullDatasetName
							+ "   Sql: " + sql);
		} finally {
			dbClose(db_statement, db_connection);
		}

		return transType;
	}

	private String getSaveSql(String fullSetName, Long id, String transType,
			Map<String, String> queryParams) {

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
				if (!(key.equalsIgnoreCase("id"))) {
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
				if (!(key.equalsIgnoreCase("id"))) {

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
		return saveSql;
	}

	@Override
	public String getReportType(String reportName) {
		String sql = "SELECT ifnull(report_type,'') as report_type FROM `stretchy_report` where report_name = '"
				+ reportName + "'";
		String reportType = "";
		logger.info("get reportType: " + sql);

		Connection db_connection = null;
		Statement db_statement = null;
		ResultSet rs = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			rs = db_statement.executeQuery(sql);

			if (rs.next()) {
				reportType = rs.getString("report_type");
			} else {
				throw new ReportNotFoundException(sql);
			}
		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Report Name: " + reportName + "   Sql: "
							+ sql);
		} finally {
			dbClose(db_statement, db_connection);
		}

		return reportType;
	}

	private void dbClose(Statement db_statement, Connection db_connection) {
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

	@Override
	public Response processPentahoRequest(String reportName,
			String outputTypeParam, Map<String, String> queryParams) {

		String outputType = "HTML";
		if (StringUtils.isNotBlank(outputTypeParam))
			outputType = outputTypeParam;

		if (!(outputType.equalsIgnoreCase("HTML")
				|| outputType.equalsIgnoreCase("PDF")
				|| outputType.equalsIgnoreCase("XLS") || outputType
					.equalsIgnoreCase("CSV")))
			throw new PlatformDataIntegrityException(
					"error.msg.invalid.outputType", "No matching Output Type: "
							+ outputType);

		if (noPentaho)
			throw new PlatformDataIntegrityException("error.msg.no.pentaho",
					"Pentaho is not enabled", "Pentaho is not enabled");

		// TODO - use pentaho location finder like Pawel does in Mifos
		// String reportPath =
		// "C:\\dev\\apache-tomcat-7.0.25\\webapps\\ROOT\\PentahoReports\\"
		// + reportName + ".prpt";
		String reportPath = "/var/lib/tomcat7/webapps/ROOT/PentahoReports/"
				+ reportName + ".prpt";
		logger.info("Report path: " + reportPath);

		// load report definition
		ResourceManager manager = new ResourceManager();
		manager.registerDefaults();
		Resource res;

		logger.info("outputType: " + outputType);
		try {
			res = manager.createDirectly(reportPath, MasterReport.class);
			MasterReport masterReport = (MasterReport) res.getResource();

			addParametersToReport(masterReport, queryParams);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			if ("PDF".equalsIgnoreCase(outputType)) {
				PdfReportUtil.createPDF(masterReport, baos);
				return Response.ok().entity(baos.toByteArray())
						.type("application/pdf").build();
			}

			if ("XLS".equalsIgnoreCase(outputType)) {
				ExcelReportUtil.createXLS(masterReport, baos);
				return Response
						.ok()
						.entity(baos.toByteArray())
						.type("application/vnd.ms-excel")
						.header("Content-Disposition",
								"attachment;filename="
										+ reportName.replaceAll(" ", "")
										+ ".xls").build();
			}

			if ("CSV".equalsIgnoreCase(outputType)) {
				CSVReportUtil.createCSV(masterReport, baos, "UTF-8");
				return Response
						.ok()
						.entity(baos.toByteArray())
						.type("application/x-msdownload")
						.header("Content-Disposition",
								"attachment;filename="
										+ reportName.replaceAll(" ", "")
										+ ".csv").build();
			}

			if ("HTML".equalsIgnoreCase(outputType)) {
				HtmlReportUtil.createStreamHTML(masterReport, baos);
				return Response.ok().entity(baos.toByteArray())
						.type("text/html").build();
			}
		} catch (ResourceException e) {
			throw new PlatformDataIntegrityException(
					"error.msg.reporting.error", e.getMessage());
		} catch (ReportProcessingException e) {
			throw new PlatformDataIntegrityException(
					"error.msg.reporting.error", e.getMessage());
		} catch (IOException e) {
			throw new PlatformDataIntegrityException(
					"error.msg.reporting.error", e.getMessage());
		}

		throw new PlatformDataIntegrityException(
				"error.msg.invalid.outputType", "No matching Output Type: "
						+ outputType);

	}

	private void addParametersToReport(MasterReport report,
			Map<String, String> queryParams) {

		try {
			ReportParameterValues rptParamValues = report.getParameterValues();
			ReportParameterDefinition paramsDefinition = report
					.getParameterDefinition();

			/*
			 * only allow integer and string parameter types and assume all
			 * mandatory - could go more detailed like Pawel did in Mifos later
			 * and could match incoming and pentaho parameters better...
			 * currently assuming they come in ok... and if not an error
			 */
			for (ParameterDefinitionEntry paramDefEntry : paramsDefinition
					.getParameterDefinitions()) {
				String paramName = paramDefEntry.getName();
				String pValue = queryParams.get(paramName);
				if (StringUtils.isBlank(pValue))
					throw new PlatformDataIntegrityException(
							"error.msg.reporting.error", "Pentaho Parameter: "
									+ paramName + " - not Provided");

				Class<?> clazz = paramDefEntry.getValueType();
				logger.info("addParametersToReport(" + paramName + " : "
						+ pValue + " : " + clazz.getCanonicalName() + ")");

				if (clazz.getCanonicalName().equalsIgnoreCase(
						"java.lang.Integer"))
					rptParamValues.put(paramName, Integer.parseInt(pValue));
				else if (clazz.getCanonicalName().equalsIgnoreCase(
						"java.sql.Date"))
					rptParamValues.put(paramName, Date.valueOf(pValue));
				else
					rptParamValues.put(paramName, pValue);
			}

		} catch (Exception e) {
			throw new PlatformDataIntegrityException(
					"error.msg.reporting.error", e.getMessage());
		}
	}

}