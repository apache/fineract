package org.mifosng.platform;

import java.sql.Connection;
import java.sql.ResultSet;
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
public class ReadExtraDataAndReportingServiceImpl implements ReadExtraDataAndReportingService {

	private final static Logger logger = LoggerFactory
			.getLogger(ReadExtraDataAndReportingServiceImpl.class);

	private final DataSource dataSource;

	@Autowired
	public ReadExtraDataAndReportingServiceImpl(final DataSource dataSource) {
		this.dataSource = dataSource;
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
				extraDatasetRows.add(new ExtraDatasetRow(rs.getInt("id"), rs.getString("datasetName"), rs.getString("datasetType")));
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

		//logger.info("specific: " + sql);
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
			selectFieldList += selectFieldSeparator + "`" + rsch.getColumnName() + "`";

			rsch.setColumnType(rsmd.getString("data_type"));
			rsch.setColumnLength(rsmd.getInt("data_length"));
			rsch.setColumnDisplayType(rsmd.getString("display_type"));
			allowedListId = rsmd.getInt("allowed_list_id");
			if (allowedListId != null) {
				sql = "select v.`name` from stretchydata_allowed_value v where allowed_list_id = " + allowedListId + " order by id";
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

	static String replace(String str, String pattern, String replace) {
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
		//logger.info("Save SQL: " + saveSql);
		return saveSql;
	}
}