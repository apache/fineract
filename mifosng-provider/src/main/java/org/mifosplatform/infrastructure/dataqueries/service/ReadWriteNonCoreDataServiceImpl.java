/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetRowData;
import org.mifosplatform.infrastructure.dataqueries.exception.DatatableNotFoundException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;

@Service
public class ReadWriteNonCoreDataServiceImpl implements ReadWriteNonCoreDataService {

    private final static Logger logger = LoggerFactory.getLogger(ReadWriteNonCoreDataServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final JsonParserHelper helper;
    private final GenericDataService genericDataService;

    @Autowired
    public ReadWriteNonCoreDataServiceImpl(final TenantAwareRoutingDataSource dataSource, final PlatformSecurityContext context,
            final FromJsonHelper fromJsonHelper, final GenericDataService genericDataService) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.helper = new JsonParserHelper();
        this.genericDataService = genericDataService;
    }

    @Override
    public List<DatatableData> retrieveDatatableNames(final String appTable) {

        String andClause;
        if (appTable == null) {
            andClause = "";
        } else {
            andClause = " and application_table_name = '" + appTable + "'";
        }

        // PERMITTED datatables
        final String sql = "select application_table_name, registered_table_name" + " from x_registered_table " + " where exists"
                + " (select 'f'" + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id"
                + " left join m_role_permission rp on rp.role_id = r.id" + " left join m_permission p on p.id = rp.permission_id"
                + " where ur.appuser_id = " + context.authenticatedUser().getId()
                + " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', registered_table_name))) "
                + andClause + " order by application_table_name, registered_table_name";

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        final List<DatatableData> datatables = new ArrayList<DatatableData>();
        while (rs.next()) {
            final String appTableName = rs.getString("application_table_name");
            final String registeredDatatableName = rs.getString("registered_table_name");

            datatables.add(DatatableData.create(appTableName, registeredDatatableName));
        }

        return datatables;
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        logger.error(dve.getMessage(), dve);
    }

    @Override
    public void registerDatatable(final String dataTableName, final String applicationTableName) {

        // FIXME - KW - hardcoded supported app tables are m_loan or m_client?
        validateAppTable(applicationTableName);

        try {
            // TODO - JW - put in batch command later
            final String registerDatatableSql = "insert into x_registered_table (registered_table_name, application_table_name) values ('"
                    + dataTableName + "', '" + applicationTableName + "')";

            this.jdbcTemplate.update(registerDatatableSql);
        } catch (DataIntegrityViolationException dve) {
            Throwable realCause = dve.getMostSpecificCause();
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.datatable.registered", "Datatable `" + dataTableName
                            + "` is already registered against an application table.", "dataTableName", dataTableName); }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }

        try {
            final String createPermission = "'CREATE_" + dataTableName + "'";
            final String createPermissionChecker = "'CREATE_" + dataTableName + "_CHECKER'";
            final String readPermission = "'READ_" + dataTableName + "'";
            final String updatePermission = "'UPDATE_" + dataTableName + "'";
            final String updatePermissionChecker = "'UPDATE_" + dataTableName + "_CHECKER'";
            final String deletePermission = "'DELETE_" + dataTableName + "'";
            final String deletePermissionChecker = "'DELETE_" + dataTableName + "_CHECKER'";

            final String permissionsSql = "insert into m_permission (grouping, code, action_name, entity_name, can_maker_checker) values "
                    + "('datatable', "
                    + createPermission
                    + ", 'CREATE', '"
                    + dataTableName
                    + "', true),"
                    + "('datatable', "
                    + createPermissionChecker
                    + ", 'CREATE', '"
                    + dataTableName
                    + "', false),"
                    + "('datatable', "
                    + readPermission
                    + ", 'READ', '"
                    + dataTableName
                    + "', false),"
                    + "('datatable', "
                    + updatePermission
                    + ", 'UPDATE', '"
                    + dataTableName
                    + "', true),"
                    + "('datatable', "
                    + updatePermissionChecker
                    + ", 'UPDATE', '"
                    + dataTableName
                    + "', false),"
                    + "('datatable', "
                    + deletePermission
                    + ", 'DELETE', '"
                    + dataTableName
                    + "', true),"
                    + "('datatable', "
                    + deletePermissionChecker
                    + ", 'DELETE', '"
                    + dataTableName + "', false)";

            this.jdbcTemplate.update(permissionsSql);
        } catch (DataIntegrityViolationException dve) {
            Throwable realCause = dve.getMostSpecificCause();
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.permissions.datatable.duplicate", "Permissions for datatable `" + dataTableName + "` already exist.",
                    "dataTableName", dataTableName); }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    @Override
    public void deregisterDatatable(final String datatable) {
        // TODO - JW - put in batch command later
        final String permissionList = "('CREATE_" + datatable + "', 'CREATE_" + datatable + "_CHECKER', 'READ_" + datatable + "', 'UPDATE_"
                + datatable + "', 'UPDATE_" + datatable + "_CHECKER', 'DELETE_" + datatable + "', 'DELETE_" + datatable + "_CHECKER')";

        final String deleteRolePermissionsSql = "delete from m_role_permission where m_role_permission.permission_id in (select id from m_permission where code in "
                + permissionList + ")";
        this.jdbcTemplate.update(deleteRolePermissionsSql);

        final String deletePermissionsSql = "delete from m_permission where code in " + permissionList;
        this.jdbcTemplate.update(deletePermissionsSql);

        final String deleteRegisteredDatatableSql = "delete from x_registered_table where registered_table_name = '" + datatable + "'";
        this.jdbcTemplate.update(deleteRegisteredDatatableSql);
    }

    @Override
    public void createNewDatatableEntry(final String dataTableName, final Long appTableId, final JsonCommand command) {

        try {
            final String appTable = getWithinScopeApplicationTableName(dataTableName, appTableId);

            final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);

            final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
            final Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

            final String sql = getAddSql(columnHeaders, dataTableName, getFKField(appTable), appTableId, dataParams);

            this.jdbcTemplate.update(sql);
        } catch (ConstraintViolationException dve) {
            // NOTE: jdbctemplate throws a
            // org.hibernate.exception.ConstraintViolationException even though
            // it should be a DataAccessException?
            Throwable realCause = dve.getCause();
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                            + "` and application table with identifier `" + appTableId + "`.", "dataTableName", dataTableName, appTableId); }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        } catch (DataAccessException dve) {
            Throwable realCause = dve.getMostSpecificCause();
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                            + "` and application table with identifier `" + appTableId + "`.", "dataTableName", dataTableName, appTableId); }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    @Override
    public Map<String, Object> updateDatatableEntryOneToOne(final String datatable, final Long appTableId, final JsonCommand command) {

        GenericResultsetData grs = retrieveDataTableGenericResultSet(datatable, appTableId, null, null);

        if (grs.getData().size() == 0) throw new DatatableNotFoundException(datatable, appTableId);

        if (grs.getData().size() > 1)
            throw new PlatformDataIntegrityException("error.msg.attempting.multiple.update", "Application Table: " + datatable
                    + "   Foreign Key Id: " + appTableId);

        String fkName = getFKField(queryForApplicationTableName(datatable));

        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

        final Map<String, Object> changes = getAffectedAndChangedColumns(grs, dataParams, fkName);

        if (changes.size() == 0) return changes;

        final String sql = getUpdateSql(datatable, fkName, appTableId, changes);

        if (StringUtils.isNotBlank(sql)) {
            this.jdbcTemplate.update(sql);
        } else {
            logger.info("No Changes");
        }

        return changes;
    }

    @Override
    public Map<String, Object> updateDatatableEntryOneToMany(final String datatable, final Long appTableId, final Long datatableId,
            final JsonCommand command) {

        final GenericResultsetData grs = retrieveDataTableGenericResultSet(datatable, appTableId, null, datatableId);

        if (grs.getData().size() == 0) throw new DatatableNotFoundException(datatable, appTableId);

        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        final Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

        final Map<String, Object> changes = getAffectedAndChangedColumns(grs, dataParams, "id");

        if (changes.size() == 0) return changes;

        final String sql = getUpdateSql(datatable, "id", datatableId, changes);

        if (StringUtils.isNotBlank(sql)) {
            this.jdbcTemplate.update(sql);
        } else {
            logger.info("No Changes");
        }

        return changes;
    }

    @Override
    public void deleteDatatableEntries(final String datatable, final Long appTableId) {

        final String appTable = getWithinScopeApplicationTableName(datatable, appTableId);

        final String sql = getDeleteEntriesSql(datatable, getFKField(appTable), appTableId);

        this.jdbcTemplate.update(sql);
    }

    @Override
    public void deleteDatatableEntry(final String datatable, final Long appTableId, final Long datatableId) {

        getWithinScopeApplicationTableName(datatable, appTableId);

        final String sql = getDeleteEntrySql(datatable, datatableId);

        this.jdbcTemplate.update(sql);
    }

    @Override
    public GenericResultsetData retrieveDataTableGenericResultSet(final String datatable, final Long appTableId, final String order,
            final Long id) {

        final String appTable = getWithinScopeApplicationTableName(datatable, appTableId);

        final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(datatable);

        String sql = "";

        // id only used for reading a specific entry in a one to many datatable
        // (when updating)
        if (id == null) {
            sql = sql + "select * from `" + datatable + "` where " + getFKField(appTable) + " = " + appTableId;
        } else {
            sql = sql + "select * from `" + datatable + "` where id = " + id;
        }

        if (order != null) sql = sql + " order by " + order;

        List<ResultsetRowData> result = fillDatatableResultSetDataRows(sql);

        return new GenericResultsetData(columnHeaders, result);
    }

    private void checkMainResourceExistsWithinScope(final String appTable, final Long appTableId) {

        final String unscopedSql = "select t.id from `" + appTable + "` t ${dataScopeCriteria} where t.id = " + appTableId;

        final String sql = dataScopedSQL(unscopedSql, appTable);

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        if (!rs.next()) { throw new DatatableNotFoundException(appTable, appTableId); }
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

    private List<ResultsetRowData> fillDatatableResultSetDataRows(final String sql) {

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        final List<ResultsetRowData> resultsetDataRows = new ArrayList<ResultsetRowData>();

        final SqlRowSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {
            final List<String> columnValues = new ArrayList<String>();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                final String columnName = rsmd.getColumnName(i + 1);
                final String columnValue = rs.getString(columnName);
                columnValues.add(columnValue);
            }

            final ResultsetRowData resultsetDataRow = ResultsetRowData.create(columnValues);
            resultsetDataRows.add(resultsetDataRow);
        }

        return resultsetDataRows;
    }

    private String getWithinScopeApplicationTableName(final String datatable, final Long appTableId) {

        final String applicationTableName = queryForApplicationTableName(datatable);
        checkMainResourceExistsWithinScope(applicationTableName, appTableId);

        return applicationTableName;
    }

    private String queryForApplicationTableName(final String datatable) {
        final String sql = "SELECT application_table_name FROM x_registered_table where registered_table_name = '" + datatable + "'";

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        String applicationTableName = null;
        if (rs.next()) {
            applicationTableName = rs.getString("application_table_name");
        } else {
            throw new DatatableNotFoundException(datatable);
        }

        return applicationTableName;
    }

    private String getFKField(final String applicationTableName) {

        return applicationTableName.substring(2) + "_id";
    }

    private String getAddSql(final List<ResultsetColumnHeaderData> columnHeaders, final String datatable, final String fkName,
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

    private Map<String, String> getAffectedColumns(final List<ResultsetColumnHeaderData> columnHeaders,
            final Map<String, String> queryParams, final String keyFieldName) {

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
                for (ResultsetColumnHeaderData columnHeader : columnHeaders) {
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

    private String validateColumn(final ResultsetColumnHeaderData columnHeader, final String pValue, final String dateFormat,
            final Locale clientApplicationLocale) {

        String paramValue = pValue;
        if (columnHeader.isDateDisplayType() || columnHeader.isIntegerDisplayType() || columnHeader.isDecimalDisplayType()) {
            paramValue = paramValue.trim();
        }

        if (StringUtils.isEmpty(paramValue) && columnHeader.isMandatory()) {

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.column.mandatory", "Mandatory",
                    columnHeader.getColumnName());
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        if (StringUtils.isNotEmpty(paramValue)) {

            if (columnHeader.hasColumnValues()) {
                if (columnHeader.isCodeValueDisplayType()) {

                    if (columnHeader.isColumnValueNotAllowed(paramValue)) {
                        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
                        final ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.columnValue",
                                "Value not found in Allowed Value list", columnHeader.getColumnName(), paramValue);
                        dataValidationErrors.add(error);
                        throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                                dataValidationErrors);
                    }

                    return paramValue;
                } else if (columnHeader.isCodeLookupDisplayType()) {

                    final Integer codeLookup = Integer.valueOf(paramValue);
                    if (columnHeader.isColumnCodeNotAllowed(codeLookup)) {
                        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
                        final ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.columnValue",
                                "Value not found in Allowed Value list", columnHeader.getColumnName(), paramValue);
                        dataValidationErrors.add(error);
                        throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                                dataValidationErrors);
                    }

                    return paramValue;
                } else {
                    throw new PlatformDataIntegrityException("error.msg.invalid.columnType.", "Code: " + columnHeader.getColumnName()
                            + " - Invalid Type " + columnHeader.getColumnType() + " (neither varchar nor int)");
                }
            }

            if (columnHeader.isDateDisplayType()) {
                final LocalDate tmpDate = helper.convertFrom(paramValue, columnHeader.getColumnName(), dateFormat, clientApplicationLocale);
                if (tmpDate == null) {
                    paramValue = null;
                } else {
                    paramValue = tmpDate.toString();
                }
            }

            if (columnHeader.isIntegerDisplayType()) {
                Integer tmpInt = helper.convertToInteger(paramValue, columnHeader.getColumnName(), clientApplicationLocale);
                if (tmpInt == null) {
                    paramValue = null;
                } else {
                    paramValue = tmpInt.toString();
                }
            }

            if (columnHeader.isDecimalDisplayType()) {
                BigDecimal tmpDecimal = helper.convertFrom(paramValue, columnHeader.getColumnName(), clientApplicationLocale);
                if (tmpDecimal == null) {
                    paramValue = null;
                } else {
                    paramValue = tmpDecimal.toString();
                }
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