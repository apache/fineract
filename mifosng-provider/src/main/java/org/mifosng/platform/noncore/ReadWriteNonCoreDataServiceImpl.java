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
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.api.data.DatatableData;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.data.ResultsetColumnHeader;
import org.mifosng.platform.api.data.ResultsetColumnValue;
import org.mifosng.platform.api.data.ResultsetDataRow;
import org.mifosng.platform.exceptions.AdditionalFieldsNotFoundException;
import org.mifosng.platform.exceptions.DataTableNotFoundException;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
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

	private String getFullDatasetName(final String type, final String set) {
		return type + "_x" + set;
	}

	// only exclusively datatable functions below here
	private void checkMainResourceExistsWithinScope(String appTable,
			Long appTableId) {

		String unscopedSql = "select t.id from " + appTable
				+ " t ${dataScopeCriteria} where t.id = " + appTableId;

		String sql = dataScopedSQL(unscopedSql, appTable);

		CachedRowSet rs = genericDataService.getCachedResultSet(sql, "SQL : "
				+ sql);

		if (rs.size() == 0)
			throw new DataTableNotFoundException(appTable, appTableId);
	}

	private String dataScopedSQL(String unscopedSQL, String appTable) {
		String dataScopeCriteria = null;
		/*
		 * unfortunately have to, one way or another, be able to restrict data
		 * to the users office hierarchy. Here it's hardcoded for client and
		 * loan. They are the main application tables. But if additional fields
		 * are needed on other tables like group, loan_transaction or others the
		 * same applies (hardcoding of some sort)
		 */

		AppUser currentUser = context.authenticatedUser();
		if (appTable.equalsIgnoreCase("m_client")) {
			dataScopeCriteria = " join m_office o on o.id = t.office_id and o.hierarchy like '"
					+ currentUser.getOffice().getHierarchy() + "%'";
		}
		if (appTable.equalsIgnoreCase("m_loan")) {
			dataScopeCriteria = " join m_client c on c.id = t.client_id "
					+ " join m_office o on o.id = c.office_id and o.hierarchy like '"
					+ currentUser.getOffice().getHierarchy() + "%'";
		}

		if (dataScopeCriteria == null) {
			throw new PlatformDataIntegrityException(
					"error.msg.invalid.dataScopeCriteria",
					"Application Table: " + appTable
							+ " not catered for in data Scoping");
		}

		return genericDataService.replace(unscopedSQL, "${dataScopeCriteria}",
				dataScopeCriteria);

	}

	@Override
	public void newDatatableEntry(String datatable, Long appTableId,
			Map<String, String> queryParams) {
		long startTime = System.currentTimeMillis();

		String appTable = getWithinScopeApplicationTableName(datatable,
				appTableId);

		List<ResultsetColumnHeader> columnHeaders = getDatatableResultsetColumnHeaders(datatable);

		String sql = getAddSql(columnHeaders, datatable, getFKField(appTable),
				appTableId, queryParams);

		genericDataService.updateSQL(sql, "SQL: " + sql);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING newDatatableEntry:      Elapsed Time: "
				+ elapsed + "       - datatable: " + datatable + "  id: "
				+ appTableId);

	}

	@Override
	public void updateDatatableEntryOnetoOne(String datatable, Long appTableId,
			Map<String, String> queryParams) {
		long startTime = System.currentTimeMillis();

		GenericResultsetData grs = retrieveDataTableGenericResultSet(datatable,
				appTableId, null, null);

		if (grs.getData().size() == 0)
			throw new DataTableNotFoundException(datatable, appTableId);

		String sql = getUpdateSql(grs, datatable,
				getFKField(getApplicationTableName(datatable)), appTableId,
				queryParams);

		if (sql != null)
			genericDataService.updateSQL(sql, "SQL: " + sql);
		else
			logger.info("No Changes");

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING updateDatatableEntryOnetoOne:      Elapsed Time: "
				+ elapsed
				+ "       - datatable: "
				+ datatable
				+ "  id: "
				+ appTableId);

	}

	@Override
	public void deleteDatatableEntries(String datatable, Long appTableId) {
		long startTime = System.currentTimeMillis();

		String appTable = getWithinScopeApplicationTableName(datatable,
				appTableId);

		String sql = getDeleteEntriesSql(datatable, getFKField(appTable),
				appTableId);

		genericDataService.updateSQL(sql, "SQL: " + sql);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING deleteDatatableEntries:      Elapsed Time: "
				+ elapsed + "       - datatable: " + datatable
				+ "  App Table Id: " + appTableId);

	}

	@Override
	public void deleteDatatableEntry(String datatable, Long appTableId,
			Long datatableId) {
		long startTime = System.currentTimeMillis();

		getWithinScopeApplicationTableName(datatable, appTableId);

		String sql = getDeleteEntrySql(datatable, datatableId);

		genericDataService.updateSQL(sql, "SQL: " + sql);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING deleteDatatableEntry:      Elapsed Time: "
				+ elapsed + "       - datatable: " + datatable
				+ "  App Table Id: " + appTableId + "   Data Table Id: "
				+ datatableId);

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
	public String retrieveDataTableJSONObject(String datatable,
			Long appTableId, String sqlFields, String sqlOrder) {
		long startTime = System.currentTimeMillis();

		GenericResultsetData result = retrieveDataTableGenericResultSet(
				datatable, appTableId, sqlFields, sqlOrder);

		String jsonString = generateJsonFromGenericResultsetData(result);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING DATATABLE JSON OBJECT: " + datatable
				+ "     Elapsed Time: " + elapsed);
		return jsonString;
	}

	@Override
	public GenericResultsetData retrieveDataTableGenericResultSet(
			String datatable, Long appTableId, String sqlFields, String sqlOrder) {

		long startTime = System.currentTimeMillis();

		String appTable = getWithinScopeApplicationTableName(datatable,
				appTableId);

		List<ResultsetColumnHeader> columnHeaders = getDatatableResultsetColumnHeaders(datatable);

		String sql = "select ";
		if (sqlFields != null)
			sql = sql + sqlFields;
		else
			sql = sql + " * ";

		sql = sql + " from " + datatable + " where " + getFKField(appTable)
				+ " = " + appTableId;
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

	private String getWithinScopeApplicationTableName(String datatable,
			Long appTableId) {
		String sql = "SELECT application_table_name FROM x_registered_table where registered_table_name = '"
				+ datatable + "'";

		CachedRowSet rs = genericDataService.getCachedResultSet(sql, "SQL : "
				+ sql);

		if (rs.size() == 0)
			throw new DataTableNotFoundException(datatable);

		try {
			rs.next();
			String appTable = rs.getString("application_table_name");

			checkMainResourceExistsWithinScope(appTable, appTableId);

			return appTable;
		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage());
		}
	}

	private String getApplicationTableName(String datatable) {
		// TODO - only used for update... can probably remove this after as its
		// a reread
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
			String datatable, String FKField, Long appTableId,
			Map<String, String> queryParams) {

		Map<String, String> affectedColumns = getAffectedColumns(columnHeaders,
				queryParams);

		String pValueWrite = "";
		String addSql = "";
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

		addSql = "insert into `" + datatable + "` (`" + FKField + "` "
				+ insertColumns + ")" + " select " + appTableId + " as id"
				+ selectColumns;

		return addSql;
	}

	private String getUpdateSql(GenericResultsetData grs, String datatable,
			String fkField, Long appTableId, Map<String, String> queryParams) {

		Map<String, String> affectedAndChangedColumns = getAffectedAndChangedColumns(
				grs, queryParams);

		// just updating fields that have changed since pre-update read - though
		// its possible these values are different from the page the user was
		// looking at and even different from the current db values (if some
		// other update got in quick) - would need a version field for
		// completeness but its okay to take this risk with additional fields
		// data

		if (affectedAndChangedColumns.size() == 0)
			return null;

		String pValue = null;
		String pValueWrite = "";
		String singleQuote = "'";
		boolean firstColumn = true;
		String sql = "update `" + datatable + "` ";

		for (String key : affectedAndChangedColumns.keySet()) {
			if (firstColumn) {
				sql += " set ";
				firstColumn = false;
			} else {
				sql += ", ";
			}

			pValue = affectedAndChangedColumns.get(key);
			if (StringUtils.isEmpty(pValue)) {
				pValueWrite = "null";
			} else {
				pValueWrite = singleQuote
						+ genericDataService.replace(pValue, singleQuote,
								singleQuote + singleQuote) + singleQuote;
			}
			sql += "`" + key + "` = " + pValueWrite;
		}

		sql += " where " + fkField + " = " + appTableId;

		return sql;
	}

	private Map<String, String> getAffectedAndChangedColumns(
			GenericResultsetData grs, Map<String, String> queryParams) {

		Map<String, String> affectedColumns = getAffectedColumns(
				grs.getColumnHeaders(), queryParams);
		Map<String, String> affectedAndChangedColumns = new HashMap<String, String>();
		String columnValue;

		for (String key : affectedColumns.keySet()) {
			columnValue = affectedColumns.get(key);
			if (columnChanged(key, columnValue, grs)) {
				affectedAndChangedColumns.put(key, columnValue);
			}
		}

		return affectedAndChangedColumns;
	}

	private boolean columnChanged(String key, String keyValue,
			GenericResultsetData grs) {

		List<String> columnValues = grs.getData().get(0).getRow();

		String columnValue = null;
		for (int i = 0; i < grs.getColumnHeaders().size(); i++) {

			if (key.equals(grs.getColumnHeaders().get(i).getColumnName())) {
				columnValue = columnValues.get(i);

				if (notTheSame(columnValue, keyValue)) {
					logger.info("Difference - Column: " + key
							+ "- Current Value: " + columnValue
							+ "    New Value: " + keyValue);
					return true;

				}
				return false;
			}
		}

		throw new PlatformDataIntegrityException(
				"error.msg.invalid.columnName", "Parameter Column Name: " + key
						+ " not found");
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
			// ignores any id field and matches incoming fields with and without
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
							validateColumn(columnHeader, pValue);
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

	private void validateColumn(ResultsetColumnHeader columnHeader,
			String pValue) {

		if ((StringUtils.isEmpty(pValue))
				&& (!(columnHeader.isColumnNullable()))) {

			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			ApiParameterError error = ApiParameterError.parameterError(
					"error.msg.column.mandatory", columnHeader.getColumnName(),
					"Mandatory");
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}

		// check allowed values
		if ((!StringUtils.isEmpty(pValue))
				&& columnHeader.getColumnValuesNew().size() > 0) {

			List<ResultsetColumnValue> allowedValues = columnHeader
					.getColumnValuesNew();
			if (columnHeader.getColumnType().equalsIgnoreCase("varchar")) {
				for (ResultsetColumnValue allowedValue : allowedValues) {
					if (pValue.equalsIgnoreCase(allowedValue.getValue()))
						return;
				}
				List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
				ApiParameterError error = ApiParameterError.parameterError(
						"error.msg.invalid.columnValue",
						columnHeader.getColumnName(), "Value :" + pValue
								+ "' not found in Allowed Value list");
				dataValidationErrors.add(error);
				throw new PlatformApiDataValidationException(
						"validation.msg.validation.errors.exist",
						"Validation errors exist.", dataValidationErrors);
			}

			if (columnHeader.getColumnType().equalsIgnoreCase("int")) {
				for (ResultsetColumnValue allowedValue : allowedValues) {
					if (pValue.equals(Integer.toString(allowedValue.getId())))
						return;
				}
				List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
				ApiParameterError error = ApiParameterError.parameterError(
						"error.msg.invalid.columnValue",
						columnHeader.getColumnName(), "Value :" + pValue
								+ "' not found in Allowed Value list");
				dataValidationErrors.add(error);
				throw new PlatformApiDataValidationException(
						"validation.msg.validation.errors.exist",
						"Validation errors exist.", dataValidationErrors);
			}

			throw new PlatformDataIntegrityException(
					"error.msg.invalid.columnType.", "Code: "
							+ columnHeader.getColumnName() + " - Invalid Type "
							+ columnHeader.getColumnType()
							+ " (neither varchar nor int)");
		}

	}

	private String getDeleteEntriesSql(String datatable, String FKField,
			Long appTableId) {

		return "delete from `" + datatable + "` where `" + FKField + "` = "
				+ appTableId;

	}

	private String getDeleteEntrySql(String datatable, Long datatableId) {

		return "delete from `" + datatable + "` where `id` = " + datatableId;

	}

}