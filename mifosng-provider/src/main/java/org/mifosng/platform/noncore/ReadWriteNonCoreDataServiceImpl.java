package org.mifosng.platform.noncore;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.DatatableData;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.data.ResultsetColumnHeader;
import org.mifosng.platform.api.data.ResultsetColumnValue;
import org.mifosng.platform.api.data.ResultsetDataRow;
import org.mifosng.platform.exceptions.AdditionalFieldsNotFoundException;
import org.mifosng.platform.exceptions.DataTableNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReadWriteNonCoreDataServiceImpl implements
		ReadWriteNonCoreDataService {

	private final PlatformSecurityContext context;

	private final static Logger logger = LoggerFactory
			.getLogger(ReadWriteNonCoreDataServiceImpl.class);

	@Autowired
	public ReadWriteNonCoreDataServiceImpl(final PlatformSecurityContext context) {
		this.context = context;
	}

	@Autowired
	private GenericDataService genericDataService;

	@Override
	public List<AdditionalFieldsSetData> retrieveExtraDatasetNames(String type) {

		long startTime = System.currentTimeMillis();
		List<AdditionalFieldsSetData> additionalFieldsSets = new ArrayList<AdditionalFieldsSetData>();

		String andClause;
		if (type == null) {
			andClause = "";
		} else {
			andClause = " and t.`name` = '" + type + "'";
		}
		// PERMITTED ADDITIONAL FIELDS datasets
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
				+ " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('CAN_READ_', t.`name`, '_x', d.`name`))) "
				+ andClause + " order by d.`name`";

		sql = genericDataService.wrapSQL(sql);
		String sqlErrorMsg = "Additional Fields Type: " + type + "   sql: "
				+ sql;
		CachedRowSet rs = genericDataService.getCachedResultSet(sql,
				sqlErrorMsg);

		try {
			while (rs.next()) {
				additionalFieldsSets.add(new AdditionalFieldsSetData(rs
						.getInt("id"), rs.getString("set"), rs
						.getString("type")));
			}

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), sqlErrorMsg);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING retrieveExtraDatasetNames:      Elapsed Time: "
				+ elapsed);

		return additionalFieldsSets;
	}

	@Override
	public GenericResultsetData retrieveExtraData(String type, String set,
			Long id) {

		long startTime = System.currentTimeMillis();

		checkMainResourceExistsWithinScope(type, id);

		CachedRowSet columnDefinitions = getAdditionalFieldsMetaData(type, set);

		String sqlErrorMsg = "Additional Fields Type: " + type + "   Set: "
				+ set + "   Id: " + id;
		List<ResultsetColumnHeader> columnHeaders = getResultsetColumnHeaders(
				columnDefinitions, sqlErrorMsg);

		String selectFieldList = getSelectFieldListFromColumnHeaders(columnHeaders);

		String sql = "select " + selectFieldList + " from `" + type
				+ "` t left join `" + getFullDatasetName(type, set)
				+ "` s on s.id = t.id where t.id = " + id;
		logger.info("addition fields sql: " + sql);

		List<ResultsetDataRow> resultsetDataRows = getResultsetDataRows(
				columnHeaders, sql, sqlErrorMsg);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING SET: " + set + "     Elapsed Time: " + elapsed);

		return new GenericResultsetData(columnHeaders, resultsetDataRows);

	}

	private String getSelectFieldListFromColumnHeaders(
			List<ResultsetColumnHeader> columnHeaders) {

		Boolean firstColumn = true;
		String selectFieldList = "";
		String selectFieldSeparator = "";

		for (ResultsetColumnHeader columnHeader : columnHeaders) {
			if (firstColumn) {
				selectFieldSeparator = " ";
				firstColumn = false;
			} else {
				selectFieldSeparator = ", ";
			}
			selectFieldList += selectFieldSeparator + "s.`"
					+ columnHeader.getColumnName() + "`";
		}
		return selectFieldList;
	}

	private CachedRowSet getAdditionalFieldsMetaData(String type, String set) {
		String sql = "select f.`name`, f.data_type, f.data_length, f.display_type, f.code_id "
				+ " from stretchydata_datasettype t "
				+ " join stretchydata_dataset d on d.datasettype_id = t.id "
				+ " join stretchydata_dataset_fields f on f.dataset_id = d.id "
				+ " where d.`name` = '"
				+ set
				+ "' and t.`name` = '"
				+ type
				+ "' order by f.id";

		CachedRowSet columnDefinitions = genericDataService.getCachedResultSet(
				sql, "SQL: " + sql);

		if (columnDefinitions.size() > 0)
			return columnDefinitions;

		throw new AdditionalFieldsNotFoundException(type, set);
	}

	private List<ResultsetColumnHeader> getResultsetColumnHeaders(
			CachedRowSet columnDefinitions, String sqlErrorMsg) {

		List<ResultsetColumnHeader> columnHeaders = new ArrayList<ResultsetColumnHeader>();
		ResultsetColumnHeader rschId = new ResultsetColumnHeader();
		rschId.setColumnName("id");
		rschId.setColumnType("Integer");
		columnHeaders.add(rschId);

		try {

			Integer codeId;
			while (columnDefinitions.next()) {
				ResultsetColumnHeader rsch = new ResultsetColumnHeader();
				rsch.setColumnName(columnDefinitions.getString("name"));

				rsch.setColumnType(columnDefinitions.getString("data_type"));
				if (columnDefinitions.getInt("data_length") > 0)
					rsch.setColumnLength(columnDefinitions
							.getLong("data_length"));

				rsch.setColumnDisplayType(columnDefinitions
						.getString("display_type"));
				codeId = columnDefinitions.getInt("code_id");

				if (codeId > 0) {
					String sql = "select code_value from m_code_value where code_id = "
							+ codeId + " order by order_position, id";
					CachedRowSet rsValues = genericDataService
							.getCachedResultSet(sql, "SQL: " + sql);
					while (rsValues.next()) {
						rsch.getColumnValues().add(
								rsValues.getString("code_value"));
					}
				}
				columnHeaders.add(rsch);
			}
			;
			return columnHeaders;

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), sqlErrorMsg);
		}

	}

	private List<ResultsetDataRow> getResultsetDataRows(
			List<ResultsetColumnHeader> columnHeaders, String sql,
			String sqlErrorMsg) {

		List<ResultsetDataRow> resultsetDataRows = null;
		CachedRowSet rs = genericDataService.getCachedResultSet(sql,
				sqlErrorMsg);

		if (rs.size() != 1)
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					"Expected One Entry to be Returned But " + rs.size()
							+ " were Found - " + sqlErrorMsg);

		String columnName = null;
		String columnValue = null;
		resultsetDataRows = new ArrayList<ResultsetDataRow>();
		ResultsetDataRow resultsetDataRow;

		try {
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
			return resultsetDataRows;

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), sqlErrorMsg);
		}

	}

	@Override
	public void updateExtraData(String type, String set, Long id,
			Map<String, String> queryParams) {

		long startTime = System.currentTimeMillis();

		GenericResultsetData readResultset = retrieveExtraData(type, set, id);

		String idValue = readResultset.getData().get(0).getRow().get(0);
		String transType = "E";
		if (idValue == null)
			transType = "A";

		String fullDatasetName = getFullDatasetName(type, set);

		String saveSql = getSaveSql(readResultset, fullDatasetName, id,
				transType, queryParams);

		if (saveSql != null) {
			String sqlErrorMsg = "Additional Fields Type: " + type + "   Set: "
					+ set + "   Id: " + id;
			genericDataService.updateSQL(saveSql, sqlErrorMsg);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING updateExtraData:      Elapsed Time: " + elapsed
				+ "       - type: " + type + "    set: " + set + "  id: " + id);
	}

	private String getSaveSql(GenericResultsetData readResultset,
			String fullSetName, Long id, String transType,
			Map<String, String> queryParams) {

		Set<String> keys = queryParams.keySet();

		String underscore = "_";
		String space = " ";
		String pValue = null;
		String keyUpdated = null;
		List<ResultsetColumnHeader> columnHeaders = readResultset
				.getColumnHeaders();
		List<String> columnValues = readResultset.getData().get(0).getRow();

		Map<String, String> updatedColumns = new HashMap<String, String>();

		for (String key : keys) {
			if (!(key.equalsIgnoreCase("id"))) {
				keyUpdated = genericDataService.replace(key, underscore, space);
				pValue = queryParams.get(key);

				if (newValueValidAndChanged(keyUpdated, columnHeaders,
						columnValues, pValue))
					updatedColumns.put(keyUpdated, pValue);

			}
		}

		// just updating fields that have changed since pre-update read - though
		// its possible these values are different from the page the user was
		// looking at and even different from the current db values (if some
		// other update got in quick) - would need a version field for
		// completeness but its okay to take this risk with additional fields
		// data

		if (updatedColumns.size() == 0)
			return null;

		String pValueWrite = "";
		String saveSql = "";
		String singleQuote = "'";

		if (transType.equals("E")) {
			boolean firstColumn = true;
			saveSql = "update `" + fullSetName + "` ";

			for (String key : updatedColumns.keySet()) {
				if (firstColumn) {
					saveSql += " set ";
					firstColumn = false;
				} else {
					saveSql += ", ";
				}

				pValue = updatedColumns.get(key);
				if (StringUtils.isEmpty(pValue)) {
					pValueWrite = "null";
				} else {
					pValueWrite = singleQuote
							+ genericDataService.replace(pValue, singleQuote,
									singleQuote + singleQuote) + singleQuote;
				}
				saveSql += "`" + key + "` = " + pValueWrite;
			}

			saveSql += " where id = " + id;
		} else {
			String insertColumns = "";
			String selectColumns = "";
			String columnName = "";
			for (String key : updatedColumns.keySet()) {
				pValue = updatedColumns.get(key);

				if (StringUtils.isEmpty(pValue)) {
					pValueWrite = "null";
				} else {
					pValueWrite = singleQuote
							+ genericDataService.replace(pValue, singleQuote,
									singleQuote + singleQuote) + singleQuote;
				}
				columnName = "`" + key + "`";
				insertColumns += ", " + columnName;
				selectColumns += "," + pValueWrite + " as " + columnName;
			}

			saveSql = "insert into `" + fullSetName + "` (id" + insertColumns
					+ ")" + " select " + id + " as id" + selectColumns;
		}
		return saveSql;
	}

	private boolean notTheSame(String currValue, String pValue) {
		if (StringUtils.isEmpty(currValue) && StringUtils.isEmpty(pValue))
			return false;

		if (StringUtils.isEmpty(currValue))
			return true;

		if (StringUtils.isEmpty(pValue))
			return true;

		if (currValue.equals(pValue))
			return false;

		return true;
	}

	private boolean newValueValidAndChanged(String key,
			List<ResultsetColumnHeader> columnHeaders,
			List<String> columnValues, String newValue) {

		String columnValue = null;
		for (int i = 0; i < columnHeaders.size(); i++) {
			if (columnHeaders.get(i).getColumnName().equalsIgnoreCase(key)) {
				columnValue = columnValues.get(i);

				if ((!StringUtils.isEmpty(newValue))
						&& columnHeaders.get(i).getColumnValues().size() > 0) {
					if (!(columnHeaders.get(i).getColumnValues()
							.contains(newValue))) {
						throw new PlatformDataIntegrityException(
								"error.msg.invalid.columnValue",
								"Parameter Column Name: " + key + " - Value '"
										+ newValue
										+ "' not found in Allowed Value list");
					}
				}

				if (notTheSame(columnValue, newValue)) {
					logger.info("Difference - Column: " + key
							+ "- Current Value: " + columnValue
							+ "    New Value: " + newValue);
					return true;

				}
				return false;
			}
		}

		throw new PlatformDataIntegrityException(
				"error.msg.invalid.columnName", "Parameter Column Name: " + key
						+ " not found");
	}

	private void checkMainResourceExistsWithinScope(String tableName, Long id) {

		String unscopedSql = "select t.id from " + tableName
				+ " t ${dataScopeCriteria} where t.id = " + id;

		String sql = dataScopedSQL(unscopedSql, tableName);

		CachedRowSet rs = genericDataService.getCachedResultSet(sql, "SQL : "
				+ sql);

		if (rs.size() == 0)
			throw new DataTableNotFoundException(tableName, id);
	}

	private String dataScopedSQL(String unscopedSQL, String type) {
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

		return genericDataService.replace(unscopedSQL, "${dataScopeCriteria}",
				dataScopeCriteria);

	}

	private String getFullDatasetName(final String type, final String set) {
		return type + "_x" + set;
	}

	// only exclusively datatable functions below here
	@Override
	public void newDatatableEntry(String datatable, Long id,
			Map<String, String> queryParams) {
		long startTime = System.currentTimeMillis();

		String applicationTableName = getApplicationTableName(datatable);

		checkMainResourceExistsWithinScope(applicationTableName, id);

		List<ResultsetColumnHeader> columnHeaders = getDatatableResultsetColumnHeaders(datatable);

		String sql = getAddSql(columnHeaders, datatable,
				getFKField(applicationTableName), id, queryParams);

		String sqlErrorMsg = "SQL: " + sql;
		genericDataService.updateSQL(sql, sqlErrorMsg);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING newDatatableEntry:      Elapsed Time: "
				+ elapsed + "       - datatable: " + datatable + "  id: " + id);

	}

	@Override
	public List<DatatableData> retrieveDatatableNames(String appTable) {

		long startTime = System.currentTimeMillis();

		String andClause;
		if (appTable == null) {
			andClause = "";
		} else {
			andClause = " and application_table_name = '" + appTable + "'";
		}
		// PERMITTED datatables
		String sql = "select application_table_name, registered_table_name, registered_table_label"
				+ " from x_registered_table "
				+ " where exists"
				+ " (select 'f'"
				+ " from m_appuser_role ur "
				+ " join m_role r on r.id = ur.role_id"
				+ " left join m_role_permission rp on rp.role_id = r.id"
				+ " left join m_permission p on p.id = rp.permission_id"
				+ " where ur.appuser_id = "
				+ context.authenticatedUser().getId()
				+ " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('CAN_READ_', registered_table_name))) "
				+ andClause
				+ " order by application_table_name, registered_table_name";

		// sql = genericDataService.wrapSQL(sql);
		String sqlErrorMsg = "Application Table Name: " + appTable + "   sql: "
				+ sql;
		CachedRowSet rs = genericDataService.getCachedResultSet(sql,
				sqlErrorMsg);

		List<DatatableData> datatables = new ArrayList<DatatableData>();
		try {
			while (rs.next()) {
				datatables.add(new DatatableData(rs
						.getString("application_table_name"), rs
						.getString("registered_table_name"), rs
						.getString("registered_table_label")));
			}

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), sqlErrorMsg);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING retrieveDatatableNames:      Elapsed Time: "
				+ elapsed);

		return datatables;
	}

	@Override
	public String retrieveDataTableJSONObject(String datatable, Long id,
			String sqlFields, String sqlOrder) {
		long startTime = System.currentTimeMillis();

		GenericResultsetData result = retrieveDataTableGenericResultSet(
				datatable, id, sqlFields, sqlOrder);

		String jsonString = generateJsonFromGenericResultsetData(result);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING DATATABLE JSON OBJECT: " + datatable
				+ "     Elapsed Time: " + elapsed);
		return jsonString;
	}

	@Override
	public GenericResultsetData retrieveDataTableGenericResultSet(
			String datatable, Long id, String sqlFields, String sqlOrder) {

		long startTime = System.currentTimeMillis();

		String applicationTableName = getApplicationTableName(datatable);

		checkMainResourceExistsWithinScope(applicationTableName, id);

		List<ResultsetColumnHeader> columnHeaders = getDatatableResultsetColumnHeaders(datatable);

		String sql = "select ";
		if (sqlFields != null)
			sql = sql + sqlFields;
		else
			sql = sql + " * ";

		sql = sql + " from " + datatable + " where "
				+ getFKField(applicationTableName) + " = " + id;
		if (sqlOrder != null)
			sql = sql + " order by " + sqlOrder;
		logger.info(sql);

		List<ResultsetDataRow> result = fillDatatableResultSetDataRows(sql);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING DATATABLE: " + datatable + "     Elapsed Time: "
				+ elapsed);

		return new GenericResultsetData(columnHeaders, result);

	}

	private List<ResultsetDataRow> fillDatatableResultSetDataRows(
			final String sql) {

		String sqlErrorMsg = "Sql: " + sql;
		CachedRowSet rs = genericDataService.getCachedResultSet(sql,
				sqlErrorMsg);

		List<ResultsetDataRow> resultsetDataRows = new ArrayList<ResultsetDataRow>();

		try {

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			ResultsetDataRow resultsetDataRow;
			String columnName = null;
			String columnValue = null;
			while (rs.next()) {
				resultsetDataRow = new ResultsetDataRow();
				List<String> columnValues = new ArrayList<String>();
				for (int i = 0; i < columnCount; i++) {
					columnName = rsmd.getColumnName(i + 1);
					columnValue = rs.getString(columnName);
					columnValues.add(columnValue);
				}
				resultsetDataRow.setRow(columnValues);
				resultsetDataRows.add(resultsetDataRow);
			}

			return resultsetDataRows;
		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), sqlErrorMsg);
		}
	}

	private String getApplicationTableName(String datatable) {
		String sql = "SELECT application_table_name FROM x_registered_table where registered_table_name = '"
				+ datatable + "'";

		CachedRowSet rs = genericDataService.getCachedResultSet(sql, "SQL : "
				+ sql);

		if (rs.size() == 0)
			throw new DataTableNotFoundException(datatable);

		try {
			rs.next();
			return rs.getString("application_table_name");
		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage());
		}
	}

	private String getFKField(String applicationTableName) {

		return applicationTableName.substring(2) + "_id";
	}

	private CachedRowSet getDatatableMetaData(String datatable) {

		String sql = "select COLUMN_NAME, IS_NULLABLE, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_KEY"
				+ " from INFORMATION_SCHEMA.COLUMNS "
				+ " where TABLE_SCHEMA = schema() and TABLE_NAME = '"
				+ datatable + "'order by ORDINAL_POSITION";

		CachedRowSet columnDefinitions = genericDataService.getCachedResultSet(
				sql, "SQL: " + sql);

		if (columnDefinitions.size() > 0)
			return columnDefinitions;

		throw new DataTableNotFoundException(datatable);
	}

	private List<ResultsetColumnHeader> getDatatableResultsetColumnHeaders(
			String datatable) {

		CachedRowSet columnDefinitions = getDatatableMetaData(datatable);

		List<ResultsetColumnHeader> columnHeaders = new ArrayList<ResultsetColumnHeader>();

		try {

			while (columnDefinitions.next()) {
				ResultsetColumnHeader rsch = new ResultsetColumnHeader();

				rsch.setColumnName(columnDefinitions.getString("COLUMN_NAME"));

				String isNullable = columnDefinitions.getString("IS_NULLABLE");
				if (isNullable.equalsIgnoreCase("YES"))
					rsch.setColumnNullable(true);
				else
					rsch.setColumnNullable(false);

				String isPrimaryKey = columnDefinitions.getString("COLUMN_KEY");
				if (isPrimaryKey.equalsIgnoreCase("PRI"))
					rsch.setColumnPrimaryKey(true);
				else
					rsch.setColumnPrimaryKey(false);

				Long columnLength = columnDefinitions
						.getLong("CHARACTER_MAXIMUM_LENGTH");
				if (columnLength > 0)
					rsch.setColumnLength(columnDefinitions
							.getLong("CHARACTER_MAXIMUM_LENGTH"));

				rsch.setColumnType(columnDefinitions.getString("DATA_TYPE"));

				rsch.setColumnDisplayType(null);

				/* look for codes */
				if (rsch.getColumnType().equalsIgnoreCase("varchar"))
					addCodesValueIfNecessary(rsch, "_cv");

				if (rsch.getColumnType().equalsIgnoreCase("int"))
					addCodesValueIfNecessary(rsch, "_cd");

				columnHeaders.add(rsch);
			}
			;
			return columnHeaders;

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage());
		}

	}

	private void addCodesValueIfNecessary(ResultsetColumnHeader rsch,
			String code_suffix) {
		int codePosition = rsch.getColumnName().indexOf(code_suffix);
		if (codePosition > 0) {
			String codeName = rsch.getColumnName().substring(0, codePosition);

			String sql = "select v.id, v.code_value from m_code m "
					+ " join m_code_value v on v.code_id = m.id "
					+ " where m.code_name = '" + codeName
					+ "' order by v.order_position, v.id";

			CachedRowSet rsValues = genericDataService.getCachedResultSet(sql,
					"SQL: " + sql);

			try {
				while (rsValues.next()) {
					rsch.getColumnValuesNew().add(
							new ResultsetColumnValue(rsValues.getInt("id"),
									rsValues.getString("code_value")));
				}
			} catch (SQLException e) {
				throw new PlatformDataIntegrityException("error.msg.sql.error",
						e.getMessage());
			}
		}

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

	}

	private String getAddSql(List<ResultsetColumnHeader> columnHeaders,
			String datatable, String FKField, Long id,
			Map<String, String> queryParams) {

		Map<String, String> affectedColumns = getAffectedColumns(columnHeaders,
				queryParams);

		String pValueWrite = "";
		String saveSql = "";
		String singleQuote = "'";

		String insertColumns = "";
		String selectColumns = "";
		String columnName = "";
		String pValue = null;
		for (String key : affectedColumns.keySet()) {
			pValue = affectedColumns.get(key);

			if (StringUtils.isEmpty(pValue)) {
				pValueWrite = "null";
			} else {
				pValueWrite = singleQuote
						+ genericDataService.replace(pValue, singleQuote,
								singleQuote + singleQuote) + singleQuote;
			}
			columnName = "`" + key + "`";
			insertColumns += ", " + columnName;
			selectColumns += "," + pValueWrite + " as " + columnName;
		}

		saveSql = "insert into `" + datatable + "` (`" + FKField + "` "
				+ insertColumns + ")" + " select " + id + " as id"
				+ selectColumns;

		return saveSql;
	}

	private Map<String, String> getAffectedColumns(
			List<ResultsetColumnHeader> columnHeaders,
			Map<String, String> queryParams) {

		String underscore = "_";
		String space = " ";
		String pValue = null;
		String queryParamColumnUnderscored;
		String columnHeaderUnderscored;
		boolean notFound;

		Map<String, String> affectedColumns = new HashMap<String, String>();
		Set<String> keys = queryParams.keySet();
		for (String key : keys) {
			// ignore any id field and matches incoming fields with and without
			// underscores (spaces and underscores considered the same)
			if (!(key.equalsIgnoreCase("id"))) {
				notFound = true;
				queryParamColumnUnderscored = genericDataService.replace(key,
						space, underscore);
				for (ResultsetColumnHeader columnHeader : columnHeaders) {
					if (notFound) {
						columnHeaderUnderscored = genericDataService
								.replace(columnHeader.getColumnName(), space,
										underscore);
						if (queryParamColumnUnderscored
								.equalsIgnoreCase(columnHeaderUnderscored)) {
							pValue = queryParams.get(key);
							affectedColumns.put(columnHeader.getColumnName(),
									pValue);
							notFound = false;
						}
					}

				}
				if (notFound) {
					throw new PlatformDataIntegrityException(
							"error.msg.column.not.found", "Column: " + key
									+ " Not Found");
				}
			}
		}
		return affectedColumns;
	}

}