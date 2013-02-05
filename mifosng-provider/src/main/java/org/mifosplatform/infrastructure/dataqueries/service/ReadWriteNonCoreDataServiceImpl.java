package org.mifosplatform.infrastructure.dataqueries.service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
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
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;
import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeader;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnValue;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetDataRow;
import org.mifosplatform.infrastructure.dataqueries.exception.DataTableNotFoundException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;

@Service
public class ReadWriteNonCoreDataServiceImpl implements ReadWriteNonCoreDataService {

    private final static Logger logger = LoggerFactory.getLogger(ReadWriteNonCoreDataServiceImpl.class);

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public ReadWriteNonCoreDataServiceImpl(final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper) {
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
    }

    @Autowired
    private GenericDataService genericDataService;

    @Override
    public List<DatatableData> retrieveDatatableNames(final String appTable) {

        String andClause;
        if (appTable == null) {
            andClause = "";
        } else {
            andClause = " and application_table_name = '" + appTable + "'";
        }
        // PERMITTED datatables
        String sql = "select application_table_name, registered_table_name" + " from x_registered_table " + " where exists"
                + " (select 'f'" + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id"
                + " left join m_role_permission rp on rp.role_id = r.id" + " left join m_permission p on p.id = rp.permission_id"
                + " where ur.appuser_id = " + context.authenticatedUser().getId()
                + " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', registered_table_name))) "
                + andClause + " order by application_table_name, registered_table_name";

        String sqlErrorMsg = "Application Table Name: " + appTable + "   sql: " + sql;
        CachedRowSet rs = genericDataService.getCachedResultSet(sql, sqlErrorMsg);

        List<DatatableData> datatables = new ArrayList<DatatableData>();
        try {
            while (rs.next()) {
                datatables.add(new DatatableData(rs.getString("application_table_name"), rs.getString("registered_table_name")));
            }

        } catch (SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage(), sqlErrorMsg);
        }

        return datatables;
    }

    @Override
    public void registerDatatable(final String datatable, final String appTable) {

        validateAppTable(appTable);

        String createPermission = "'CREATE_" + datatable + "'";
        String createPermissionChecker = "'CREATE_" + datatable + "_CHECKER'";
        String readPermission = "'READ_" + datatable + "'";
        String updatePermission = "'UPDATE_" + datatable + "'";
        String updatePermissionChecker = "'UPDATE_" + datatable + "_CHECKER'";
        String deletePermission = "'DELETE_" + datatable + "'";
        String deletePermissionChecker = "'DELETE_" + datatable + "_CHECKER'";
        // TODO - put in batch command later
        String sql = "insert into x_registered_table (registered_table_name, application_table_name) values ('" + datatable + "', '"
                + appTable + "')";

        genericDataService.updateSQL(sql, "SQL: " + sql);
        /*
         * add all permissions, including checker permissions for maintenance
         * tasks
         */
        sql = "insert into m_permission (grouping, code, action_name, entity_name, can_maker_checker) values " + "('datatable', "
                + createPermission + ", 'CREATE', '" + datatable + "', true)," + "('datatable', " + createPermissionChecker
                + ", 'CREATE', '" + datatable + "', false)," + "('datatable', " + readPermission + ", 'READ', '" + datatable + "', false),"
                + "('datatable', " + updatePermission + ", 'UPDATE', '" + datatable + "', true)," + "('datatable', "
                + updatePermissionChecker + ", 'UPDATE', '" + datatable + "', false)," + "('datatable', " + deletePermission
                + ", 'DELETE', '" + datatable + "', true)," + "('datatable', " + deletePermissionChecker + ", 'DELETE', '" + datatable
                + "', false)";

        genericDataService.updateSQL(sql, "SQL: " + sql);
    }

    @Override
    public void deregisterDatatable(final String datatable) {
        // TODO - put in batch command later

        String permissionList = "('CREATE_" + datatable + "', 'CREATE_" + datatable + "_CHECKER', 'READ_" + datatable + "', 'UPDATE_"
                + datatable + "', 'UPDATE_" + datatable + "_CHECKER', 'DELETE_" + datatable + "', 'DELETE_" + datatable + "_CHECKER')";

        String sql = "delete from m_role_permission where m_role_permission.permission_id in (select id from m_permission where code in "
                + permissionList + ")";
        genericDataService.updateSQL(sql, "SQL: " + sql);

        sql = "delete from m_permission where code in " + permissionList;
        genericDataService.updateSQL(sql, "SQL: " + sql);

        sql = "delete from x_registered_table where registered_table_name = '" + datatable + "'";
        genericDataService.updateSQL(sql, "SQL: " + sql);
    }

    @Override
    public void newDatatableEntry(final String datatable, final Long appTableId, final JsonCommand command) {
        String appTable = getWithinScopeApplicationTableName(datatable, appTableId);

        List<ResultsetColumnHeader> columnHeaders = getDatatableResultsetColumnHeaders(datatable);

        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

        String sql = getAddSql(columnHeaders, datatable, getFKField(appTable), appTableId, dataParams);

        genericDataService.updateSQL(sql, "SQL: " + sql);
    }

    @Override
    public Map<String, Object> updateDatatableEntryOneToOne(final String datatable, final Long appTableId, final JsonCommand command) {

        GenericResultsetData grs = retrieveDataTableGenericResultSet(datatable, appTableId, null, null);

        if (grs.getData().size() == 0) throw new DataTableNotFoundException(datatable, appTableId);

        if (grs.getData().size() > 1)
            throw new PlatformDataIntegrityException("error.msg.attempting.multiple.update", "Application Table: " + datatable
                    + "   Foreign Key Id: " + appTableId);

        String fkName = getFKField(getApplicationTableName(datatable));

        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

        final Map<String, Object> changes = getAffectedAndChangedColumns(grs, dataParams, fkName);

        if (changes.size() == 0) return changes;

        String sql = getUpdateSql(datatable, fkName, appTableId, changes);

        if (sql != null) {
            genericDataService.updateSQL(sql, "SQL: " + sql);
        } else {
            logger.info("No Changes");
        }

        return changes;
    }

    @Override
    public Map<String, Object> updateDatatableEntryOneToMany(final String datatable, final Long appTableId, final Long datatableId,
            final JsonCommand command) {

        GenericResultsetData grs = retrieveDataTableGenericResultSet(datatable, appTableId, null, datatableId);

        if (grs.getData().size() == 0) throw new DataTableNotFoundException(datatable, appTableId);

        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

        final Map<String, Object> changes = getAffectedAndChangedColumns(grs, dataParams, "id");

        if (changes.size() == 0) return changes;

        String sql = getUpdateSql(datatable, "id", datatableId, changes);

        if (sql != null) {
            genericDataService.updateSQL(sql, "SQL: " + sql);
        } else {
            logger.info("No Changes");
        }

        return changes;
    }

    @Override
    public void deleteDatatableEntries(final String datatable, final Long appTableId) {

        String appTable = getWithinScopeApplicationTableName(datatable, appTableId);

        String sql = getDeleteEntriesSql(datatable, getFKField(appTable), appTableId);

        genericDataService.updateSQL(sql, "SQL: " + sql);
    }

    @Override
    public void deleteDatatableEntry(final String datatable, final Long appTableId, final Long datatableId) {

        getWithinScopeApplicationTableName(datatable, appTableId);

        String sql = getDeleteEntrySql(datatable, datatableId);

        genericDataService.updateSQL(sql, "SQL: " + sql);
    }

    @Override
    public GenericResultsetData retrieveDataTableGenericResultSet(final String datatable, final Long appTableId, final String order,
            final Long id) {

        String appTable = getWithinScopeApplicationTableName(datatable, appTableId);

        List<ResultsetColumnHeader> columnHeaders = getDatatableResultsetColumnHeaders(datatable);

        String sql = "";

        // id only used for reading a specific entry in a one to many datatable
        // (when updating)
        if (id == null) {
            sql = sql + "select * from `" + datatable + "` where " + getFKField(appTable) + " = " + appTableId;
        } else {
            sql = sql + "select * from `" + datatable + "` where id = " + id;
        }

        if (order != null) sql = sql + " order by " + order;

        List<ResultsetDataRow> result = fillDatatableResultSetDataRows(sql);

        return new GenericResultsetData(columnHeaders, result);
    }

    private void checkMainResourceExistsWithinScope(final String appTable, final Long appTableId) {

        String unscopedSql = "select t.id from `" + appTable + "` t ${dataScopeCriteria} where t.id = " + appTableId;

        String sql = dataScopedSQL(unscopedSql, appTable);

        CachedRowSet rs = genericDataService.getCachedResultSet(sql, "SQL : " + sql);

        if (rs.size() == 0) throw new DataTableNotFoundException(appTable, appTableId);
    }

    private String dataScopedSQL(final String unscopedSQL, final String appTable) {
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
            dataScopeCriteria = " join m_office o on o.id = t.office_id and o.hierarchy like '" + currentUser.getOffice().getHierarchy()
                    + "%'";
        }
        if (appTable.equalsIgnoreCase("m_loan")) {
            dataScopeCriteria = " join m_client c on c.id = t.client_id " + " join m_office o on o.id = c.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy() + "%'";
        }

        if (dataScopeCriteria == null) { throw new PlatformDataIntegrityException("error.msg.invalid.dataScopeCriteria",
                "Application Table: " + appTable + " not catered for in data Scoping"); }

        return genericDataService.replace(unscopedSQL, "${dataScopeCriteria}", dataScopeCriteria);

    }

    private void validateAppTable(final String appTable) {

        if (appTable.equalsIgnoreCase("m_client")) return;
        if (appTable.equalsIgnoreCase("m_loan")) return;

        throw new PlatformDataIntegrityException("error.msg.invalid.application.table", "Invalid Application Table: " + appTable);
    }

    private List<ResultsetDataRow> fillDatatableResultSetDataRows(final String sql) {

        String sqlErrorMsg = "Sql: " + sql;
        CachedRowSet rs = genericDataService.getCachedResultSet(sql, sqlErrorMsg);

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
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage(), sqlErrorMsg);
        }
    }

    private String getWithinScopeApplicationTableName(final String datatable, final Long appTableId) {
        String sql = "SELECT application_table_name FROM x_registered_table where registered_table_name = '" + datatable + "'";

        CachedRowSet rs = genericDataService.getCachedResultSet(sql, "SQL : " + sql);

        if (rs.size() == 0) throw new DataTableNotFoundException(datatable);

        try {
            rs.next();
            String appTable = rs.getString("application_table_name");

            checkMainResourceExistsWithinScope(appTable, appTableId);

            return appTable;
        } catch (SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage());
        }
    }

    private String getApplicationTableName(final String datatable) {
        // TODO - only used for update... can probably remove this after as its
        // a reread
        String sql = "SELECT application_table_name FROM x_registered_table where registered_table_name = '" + datatable + "'";

        CachedRowSet rs = genericDataService.getCachedResultSet(sql, "SQL : " + sql);

        if (rs.size() == 0) throw new DataTableNotFoundException(datatable);

        try {
            rs.next();
            return rs.getString("application_table_name");
        } catch (SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage());
        }
    }

    private String getFKField(final String applicationTableName) {

        return applicationTableName.substring(2) + "_id";
    }

    private CachedRowSet getDatatableMetaData(final String datatable) {

        String sql = "select COLUMN_NAME, IS_NULLABLE, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_KEY"
                + " from INFORMATION_SCHEMA.COLUMNS " + " where TABLE_SCHEMA = schema() and TABLE_NAME = '" + datatable
                + "'order by ORDINAL_POSITION";

        CachedRowSet columnDefinitions = genericDataService.getCachedResultSet(sql, "SQL: " + sql);

        if (columnDefinitions.size() > 0) return columnDefinitions;

        throw new DataTableNotFoundException(datatable);
    }

    private List<ResultsetColumnHeader> getDatatableResultsetColumnHeaders(final String datatable) {

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

                Long columnLength = columnDefinitions.getLong("CHARACTER_MAXIMUM_LENGTH");
                if (columnLength > 0) rsch.setColumnLength(columnDefinitions.getLong("CHARACTER_MAXIMUM_LENGTH"));

                rsch.setColumnType(columnDefinitions.getString("DATA_TYPE"));

                /* look for codes */
                if (rsch.getColumnType().equalsIgnoreCase("varchar")) addCodesValueIfNecessary(rsch, "_cv");

                if (rsch.getColumnType().equalsIgnoreCase("int")) addCodesValueIfNecessary(rsch, "_cd");

                rsch.setColumnDisplayType();

                columnHeaders.add(rsch);
            }
            ;
            return columnHeaders;

        } catch (SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage());
        }

    }

    private void addCodesValueIfNecessary(final ResultsetColumnHeader rsch, final String code_suffix) {
        int codePosition = rsch.getColumnName().indexOf(code_suffix);
        if (codePosition > 0) {
            String codeName = rsch.getColumnName().substring(0, codePosition);

            String sql = "select v.id, v.code_value from m_code m " + " join m_code_value v on v.code_id = m.id "
                    + " where m.code_name = '" + codeName + "' order by v.order_position, v.id";

            CachedRowSet rsValues = genericDataService.getCachedResultSet(sql, "SQL: " + sql);

            try {
                while (rsValues.next()) {
                    rsch.getColumnValues().add(new ResultsetColumnValue(rsValues.getInt("id"), rsValues.getString("code_value")));
                }
            } catch (SQLException e) {
                throw new PlatformDataIntegrityException("error.msg.sql.error", e.getMessage());
            }
        }

    }

    private String getAddSql(final List<ResultsetColumnHeader> columnHeaders, final String datatable, final String fkName,
            final Long appTableId, final Map<String, String> queryParams) {

        final Map<String, String> affectedColumns = getAffectedColumns(columnHeaders, queryParams, fkName);

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
                pValueWrite = singleQuote + genericDataService.replace(pValue, singleQuote, singleQuote + singleQuote) + singleQuote;
            }
            columnName = "`" + key + "`";
            insertColumns += ", " + columnName;
            selectColumns += "," + pValueWrite + " as " + columnName;
        }

        addSql = "insert into `" + datatable + "` (`" + fkName + "` " + insertColumns + ")" + " select " + appTableId + " as id"
                + selectColumns;

        return addSql;
    }

    private String getUpdateSql(final String datatable, final String keyFieldName, final Long keyFieldValue,
            final Map<String, Object> changedColumns) {

        // just updating fields that have changed since pre-update read - though
        // its possible these values are different from the page the user was
        // looking at and even different from the current db values (if some
        // other update got in quick) - would need a version field for
        // completeness but its okay to take this risk with additional fields
        // data

        if (changedColumns.size() == 0) return null;

        String pValue = null;
        String pValueWrite = "";
        String singleQuote = "'";
        boolean firstColumn = true;
        String sql = "update `" + datatable + "` ";

        for (String key : changedColumns.keySet()) {
            if (firstColumn) {
                sql += " set ";
                firstColumn = false;
            } else {
                sql += ", ";
            }

            pValue = (String) changedColumns.get(key);
            if (StringUtils.isEmpty(pValue)) {
                pValueWrite = "null";
            } else {
                pValueWrite = singleQuote + genericDataService.replace(pValue, singleQuote, singleQuote + singleQuote) + singleQuote;
            }
            sql += "`" + key + "` = " + pValueWrite;
        }

        sql += " where " + keyFieldName + " = " + keyFieldValue;

        return sql;
    }

    private Map<String, Object> getAffectedAndChangedColumns(final GenericResultsetData grs, final Map<String, String> queryParams,
            final String fkName) {

        Map<String, String> affectedColumns = getAffectedColumns(grs.getColumnHeaders(), queryParams, fkName);
        Map<String, Object> affectedAndChangedColumns = new HashMap<String, Object>();
        String columnValue;

        for (String key : affectedColumns.keySet()) {
            columnValue = affectedColumns.get(key);
            if (columnChanged(key, columnValue, grs)) {
                affectedAndChangedColumns.put(key, columnValue);
            }
        }

        return affectedAndChangedColumns;
    }

    private boolean columnChanged(final String key, final String keyValue, final GenericResultsetData grs) {

        List<String> columnValues = grs.getData().get(0).getRow();

        String columnValue = null;
        for (int i = 0; i < grs.getColumnHeaders().size(); i++) {

            if (key.equals(grs.getColumnHeaders().get(i).getColumnName())) {
                columnValue = columnValues.get(i);

                if (notTheSame(columnValue, keyValue)) {
                    // logger.info("Difference - Column: " + key +
                    // "- Current Value: " + columnValue + "    New Value: " +
                    // keyValue);
                    return true;

                }
                return false;
            }
        }

        throw new PlatformDataIntegrityException("error.msg.invalid.columnName", "Parameter Column Name: " + key + " not found");
    }

    private Map<String, String> getAffectedColumns(final List<ResultsetColumnHeader> columnHeaders, final Map<String, String> queryParams,
            final String keyFieldName) {

        String dateFormat = queryParams.get("dateFormat");
        Locale clientApplicationLocale = null;
        String localeQueryParam = queryParams.get("locale");
        if (!(StringUtils.isBlank(localeQueryParam))) clientApplicationLocale = new Locale(queryParams.get("locale"));

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
            if (!((key.equalsIgnoreCase("id")) || (key.equalsIgnoreCase(keyFieldName)) || (key.equals("locale")) || (key
                    .equals("dateFormat")))) {
                notFound = true;
                // matches incoming fields with and without underscores (spaces
                // and underscores considered the same)
                queryParamColumnUnderscored = genericDataService.replace(key, space, underscore);
                for (ResultsetColumnHeader columnHeader : columnHeaders) {
                    if (notFound) {
                        columnHeaderUnderscored = genericDataService.replace(columnHeader.getColumnName(), space, underscore);
                        if (queryParamColumnUnderscored.equalsIgnoreCase(columnHeaderUnderscored)) {
                            pValue = queryParams.get(key);
                            pValue = validateColumn(columnHeader, pValue, dateFormat, clientApplicationLocale);
                            affectedColumns.put(columnHeader.getColumnName(), pValue);
                            notFound = false;
                        }
                    }

                }
                if (notFound) { throw new PlatformDataIntegrityException("error.msg.column.not.found", "Column: " + key + " Not Found"); }
            }
        }
        return affectedColumns;
    }

    private String validateColumn(final ResultsetColumnHeader columnHeader, final String pValue, final String dateFormat,
            final Locale clientApplicationLocale) {

        String paramValue = pValue;
        if ((columnHeader.getColumnDisplayType().equals("DATE")) || (columnHeader.getColumnDisplayType().equals("INTEGER"))
                || (columnHeader.getColumnDisplayType().equals("DECIMAL"))) paramValue = paramValue.trim();

        if ((StringUtils.isEmpty(paramValue)) && (!(columnHeader.isColumnNullable()))) {

            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            ApiParameterError error = ApiParameterError.parameterError("error.msg.column.mandatory", "Mandatory",
                    columnHeader.getColumnName());
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        if (!StringUtils.isEmpty(paramValue)) {

            if (columnHeader.getColumnValues().size() > 0) {
                // match code value or id
                List<ResultsetColumnValue> allowedValues = columnHeader.getColumnValues();
                if (columnHeader.getColumnDisplayType().equals("CODEVALUE")) {
                    for (ResultsetColumnValue allowedValue : allowedValues) {
                        if (paramValue.equalsIgnoreCase(allowedValue.getValue())) return paramValue;
                    }
                    List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
                    ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.columnValue",
                            "Value not found in Allowed Value list", columnHeader.getColumnName(), paramValue);
                    dataValidationErrors.add(error);
                    throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                            dataValidationErrors);
                }

                if (columnHeader.getColumnDisplayType().equals("CODELOOKUP")) {
                    for (ResultsetColumnValue allowedValue : allowedValues) {
                        if (paramValue.equals(Integer.toString(allowedValue.getId()))) return paramValue;
                    }
                    List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
                    ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.columnValue",
                            "Value not found in Allowed Value list", columnHeader.getColumnName(), paramValue);
                    dataValidationErrors.add(error);
                    throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                            dataValidationErrors);
                }

                throw new PlatformDataIntegrityException("error.msg.invalid.columnType.", "Code: " + columnHeader.getColumnName()
                        + " - Invalid Type " + columnHeader.getColumnType() + " (neither varchar nor int)");
            }

            JsonParserHelper helper = new JsonParserHelper();

            if (columnHeader.getColumnDisplayType().equals("DATE")) {
                LocalDate tmpDate = helper.convertFrom(paramValue, columnHeader.getColumnName(), dateFormat, clientApplicationLocale);
                if (tmpDate == null) return null;
                return tmpDate.toString();
            }
            if (columnHeader.getColumnDisplayType().equals("INTEGER")) {
                Integer tmpInt = helper.convertToInteger(paramValue, columnHeader.getColumnName(), clientApplicationLocale);
                if (tmpInt == null) return null;
                return tmpInt.toString();
            }
            if (columnHeader.getColumnDisplayType().equals("DECIMAL")) {
                BigDecimal tmpDecimal = helper.convertFrom(paramValue, columnHeader.getColumnName(), clientApplicationLocale);
                if (tmpDecimal == null) return null;
                return tmpDecimal.toString();
            }

        }

        return paramValue;
    }

    private String getDeleteEntriesSql(final String datatable, final String FKField, final Long appTableId) {

        return "delete from `" + datatable + "` where `" + FKField + "` = " + appTableId;

    }

    private String getDeleteEntrySql(final String datatable, final Long datatableId) {

        return "delete from `" + datatable + "` where `id` = " + datatableId;

    }

    private boolean notTheSame(final String currValue, final String pValue) {
        if (StringUtils.isEmpty(currValue) && StringUtils.isEmpty(pValue)) return false;

        if (StringUtils.isEmpty(currValue)) return true;

        if (StringUtils.isEmpty(pValue)) return true;

        if (currValue.equals(pValue)) return false;

        return true;
    }
}