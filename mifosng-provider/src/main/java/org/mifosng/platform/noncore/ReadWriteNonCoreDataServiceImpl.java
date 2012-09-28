package org.mifosng.platform.noncore;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.api.data.DatatableData;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.data.ResultsetColumnHeader;
import org.mifosng.platform.api.data.ResultsetColumnValue;
import org.mifosng.platform.api.data.ResultsetDataRow;
import org.mifosng.platform.api.infrastructure.JsonParserHelper;
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
	public List<DatatableData> retrieveDatatableNames(String appTable) {

		long startTime = System.currentTimeMillis();

		String andClause;
		if (appTable == null) {
			andClause = "";
		} else {
			andClause = " and application_table_name = '" + appTable + "'";
		}
		// PERMITTED datatables
		String sql = "select application_table_name, registered_table_name"
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

		String sqlErrorMsg = "Application Table Name: " + appTable + "   sql: "
				+ sql;
		CachedRowSet rs = genericDataService.getCachedResultSet(sql,
				sqlErrorMsg);

		List<DatatableData> datatables = new ArrayList<DatatableData>();
		try {
			while (rs.next()) {
				datatables.add(new DatatableData(rs
						.getString("application_table_name"), rs
						.getString("registered_table_name")));
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
	public void registerDatatable(String datatable, String appTable) {

		long startTime = System.currentTimeMillis();

		validateAppTable(appTable);

		String createPermission = "'CAN_CREATE_" + datatable + "'";
		String readPermission = "'CAN_READ_" + datatable + "'";
		String updatePermission = "'CAN_UPDATE_" + datatable + "'";
		String deletePermission = "'CAN_DELETE_" + datatable + "'";
		// TODO - put in batch command later
		String sql = "insert into x_registered_table (registered_table_name, application_table_name) values ('"
				+ datatable + "', '" + appTable + "')";

		genericDataService.updateSQL(sql, "SQL: " + sql);

		sql = "insert into m_permission (group_enum, code, default_description, default_name) values "
				+ "(3, "
				+ createPermission
				+ ", "
				+ createPermission
				+ ", "
				+ createPermission
				+ "),"
				+ "(3, "
				+ readPermission
				+ ", "
				+ readPermission
				+ ", "
				+ readPermission
				+ "),"
				+ "(3, "
				+ updatePermission
				+ ", "
				+ updatePermission
				+ ", "
				+ updatePermission
				+ "),"
				+ "(3, "
				+ deletePermission
				+ ", "
				+ deletePermission + ", " + deletePermission + ")";

		genericDataService.updateSQL(sql, "SQL: " + sql);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING registerDatatable:      Elapsed Time: "
				+ elapsed + "       - datatable: " + datatable
				+ "  application table: " + appTable);
	}

	@Override
	public void deregisterDatatable(String datatable) {
		long startTime = System.currentTimeMillis();

		// TODO - put in batch command later

		String permissionList = "('CAN_CREATE_" + datatable + "', 'CAN_READ_"
				+ datatable + "', 'CAN_UPDATE_" + datatable + "', 'CAN_DELETE_"
				+ datatable + "')";

		String sql = "delete from m_role_permission where m_role_permission.permission_id in (select id from m_permission where code in "
				+ permissionList + ")";
		genericDataService.updateSQL(sql, "SQL: " + sql);

		sql = "delete from m_permission where code in " + permissionList;
		genericDataService.updateSQL(sql, "SQL: " + sql);

		sql = "delete from x_registered_table where registered_table_name = '"
				+ datatable + "'";
		genericDataService.updateSQL(sql, "SQL: " + sql);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING deregisterDatatable:      Elapsed Time: "
				+ elapsed + "       - datatable: " + datatable);
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
				appTableId, null, null, null);

		if (grs.getData().size() == 0)
			throw new DataTableNotFoundException(datatable, appTableId);

		if (grs.getData().size() > 1)
			throw new PlatformDataIntegrityException(
					"error.msg.attempting.multiple.update",
					"Application Table: " + datatable + "   Foreign Key Id: "
							+ appTableId);

		String fkName = getFKField(getApplicationTableName(datatable));
		String sql = getUpdateSql(grs, datatable, fkName, appTableId,
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
				+ fkName
				+ ": "
				+ appTableId);

	}

	@Override
	public void updateDatatableEntryOnetoMany(String datatable,
			Long appTableId, Long datatableId, Map<String, String> queryParams) {
		long startTime = System.currentTimeMillis();

		GenericResultsetData grs = retrieveDataTableGenericResultSet(datatable,
				appTableId, null, null, datatableId);

		if (grs.getData().size() == 0)
			throw new DataTableNotFoundException(datatable, appTableId);

		String sql = getUpdateSql(grs, datatable, "id", datatableId,
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
				+ datatableId);
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
	public GenericResultsetData retrieveDataTableGenericResultSet(
			String datatable, Long appTableId, String sqlFields,
			String sqlOrder, Long id) {

		long startTime = System.currentTimeMillis();

		String appTable = getWithinScopeApplicationTableName(datatable,
				appTableId);

		List<ResultsetColumnHeader> columnHeaders = getDatatableResultsetColumnHeaders(datatable);

		String sql = "select ";
		if (sqlFields != null)
			sql = sql + sqlFields;
		else
			sql = sql + " * ";

		// id only used for reading a specific entry in a one to many datatable
		// (when updating)
		if (id == null) {
			sql = sql + " from `" + datatable + "` where "
					+ getFKField(appTable) + " = " + appTableId;
		} else {
			sql = sql + " from `" + datatable + "` where id = " + id;
		}

		if (sqlOrder != null)
			sql = sql + " order by " + sqlOrder;

		List<ResultsetDataRow> result = fillDatatableResultSetDataRows(sql);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING DATATABLE: " + datatable + "     Elapsed Time: "
				+ elapsed + "    SQL: " + sql);

		return new GenericResultsetData(columnHeaders, result);

	}

	private void checkMainResourceExistsWithinScope(String appTable,
			Long appTableId) {

		String unscopedSql = "select t.id from `" + appTable
				+ "` t ${dataScopeCriteria} where t.id = " + appTableId;

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

	private void validateAppTable(String appTable) {

		if (appTable.equalsIgnoreCase("m_client"))
			return;
		if (appTable.equalsIgnoreCase("m_loan"))
			return;

		throw new PlatformDataIntegrityException(
				"error.msg.invalid.application.table",
				"Invalid Application Table: " + appTable);
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

				/* look for codes */
				if (rsch.getColumnType().equalsIgnoreCase("varchar"))
					addCodesValueIfNecessary(rsch, "_cv");

				if (rsch.getColumnType().equalsIgnoreCase("int"))
					addCodesValueIfNecessary(rsch, "_cd");

				rsch.setColumnDisplayTypeNew();

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

	private String getAddSql(List<ResultsetColumnHeader> columnHeaders,
			String datatable, String fkName, Long appTableId,
			Map<String, String> queryParams) {

		Map<String, String> affectedColumns = getAffectedColumns(columnHeaders,
				queryParams, fkName);

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

		addSql = "insert into `" + datatable + "` (`" + fkName + "` "
				+ insertColumns + ")" + " select " + appTableId + " as id"
				+ selectColumns;

		return addSql;
	}

	private String getUpdateSql(GenericResultsetData grs, String datatable,
			String keyFieldName, Long keyFieldValue,
			Map<String, String> queryParams) {

		Map<String, String> affectedAndChangedColumns = getAffectedAndChangedColumns(
				grs, queryParams, keyFieldName);

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

		sql += " where " + keyFieldName + " = " + keyFieldValue;

		return sql;
	}

	private Map<String, String> getAffectedAndChangedColumns(
			GenericResultsetData grs, Map<String, String> queryParams,
			String fkName) {

		Map<String, String> affectedColumns = getAffectedColumns(
				grs.getColumnHeaders(), queryParams, fkName);
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
			Map<String, String> queryParams, String keyFieldName) {

		String dateFormat = queryParams.get("dateFormat");
		Locale clientApplicationLocale = null;
		String localeQueryParam = queryParams.get("locale");
		if (!(StringUtils.isBlank(localeQueryParam)))
			clientApplicationLocale = new Locale(queryParams.get("locale"));

		String underscore = "_";
		String space = " ";
		String pValue = null;
		String queryParamColumnUnderscored;
		String columnHeaderUnderscored;
		boolean notFound;

		Map<String, String> affectedColumns = new HashMap<String, String>();
		Set<String> keys = queryParams.keySet();
		for (String key : keys) {
			// ignores id and foreign key fields
			// also ignores locale and dateformat fields that are used for
			// validating numeric and date data
			if (!((key.equalsIgnoreCase("id"))
					|| (key.equalsIgnoreCase(keyFieldName))
					|| (key.equals("locale")) || (key.equals("dateFormat")))) {
				notFound = true;
				// matches incoming fields with and without underscores (spaces
				// and underscores considered the same)
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
							pValue = validateColumn(columnHeader, pValue,
									dateFormat, clientApplicationLocale);
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

	private String validateColumn(ResultsetColumnHeader columnHeader,
			String pValue, String dateFormat, Locale clientApplicationLocale) {

		String paramValue = pValue;

		if ((StringUtils.isEmpty(paramValue))
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

		if (!StringUtils.isEmpty(paramValue)) {

			if (columnHeader.getColumnValuesNew().size() > 0) {
				// match code value or id
				List<ResultsetColumnValue> allowedValues = columnHeader
						.getColumnValuesNew();
				if (columnHeader.getColumnDisplayTypeNew().equals("CODEVALUE")) {
					for (ResultsetColumnValue allowedValue : allowedValues) {
						if (paramValue
								.equalsIgnoreCase(allowedValue.getValue()))
							return paramValue;
					}
					List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
					ApiParameterError error = ApiParameterError.parameterError(
							"error.msg.invalid.columnValue",
							columnHeader.getColumnName(), "Value :"
									+ paramValue
									+ "' not found in Allowed Value list");
					dataValidationErrors.add(error);
					throw new PlatformApiDataValidationException(
							"validation.msg.validation.errors.exist",
							"Validation errors exist.", dataValidationErrors);
				}

				if (columnHeader.getColumnDisplayTypeNew().equals("CODELOOKUP")) {
					for (ResultsetColumnValue allowedValue : allowedValues) {
						if (paramValue.equals(Integer.toString(allowedValue
								.getId())))
							return paramValue;
					}
					List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
					ApiParameterError error = ApiParameterError.parameterError(
							"error.msg.invalid.columnValue",
							columnHeader.getColumnName(), "Value :"
									+ paramValue
									+ "' not found in Allowed Value list");
					dataValidationErrors.add(error);
					throw new PlatformApiDataValidationException(
							"validation.msg.validation.errors.exist",
							"Validation errors exist.", dataValidationErrors);
				}

				throw new PlatformDataIntegrityException(
						"error.msg.invalid.columnType.", "Code: "
								+ columnHeader.getColumnName()
								+ " - Invalid Type "
								+ columnHeader.getColumnType()
								+ " (neither varchar nor int)");
			}

			JsonParserHelper helper = new JsonParserHelper();

			if (columnHeader.getColumnDisplayTypeNew().equals("DATE"))
				paramValue = helper.convertFrom(paramValue,
						columnHeader.getColumnName(), dateFormat,
						clientApplicationLocale).toString();

			if (columnHeader.getColumnDisplayTypeNew().equals("INTEGER"))
				paramValue = helper.convertToInteger(paramValue,
						columnHeader.getColumnName(), clientApplicationLocale)
						.toString();

			if (columnHeader.getColumnDisplayTypeNew().equals("DECIMAL"))
				paramValue = helper.convertFrom(paramValue,
						columnHeader.getColumnName(), clientApplicationLocale)
						.toString();
			// logger.info("Converted Value: " + paramValue + " - was: " +
			// pValue);

		}

		return paramValue;
	}

	private String getDeleteEntriesSql(String datatable, String FKField,
			Long appTableId) {

		return "delete from `" + datatable + "` where `" + FKField + "` = "
				+ appTableId;

	}

	private String getDeleteEntrySql(String datatable, Long datatableId) {

		return "delete from `" + datatable + "` where `id` = " + datatableId;

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

}