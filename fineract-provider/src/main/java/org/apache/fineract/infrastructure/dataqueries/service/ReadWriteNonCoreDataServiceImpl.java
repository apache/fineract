/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.dataqueries.service;

import static java.util.Arrays.asList;
import static org.apache.fineract.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer.DATATABLE_NAME_REGEX_PATTERN;
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.BIT;
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.DATETIME;
import static org.apache.fineract.infrastructure.core.service.database.SqlOperator.EQ;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_AFTER;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_CODE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_INDEXED;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_LENGTH;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_MANDATORY;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_NAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_NEWCODE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_NEWNAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_DROPDOWN;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_UNIQUE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_ADDCOLUMNS;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_APPTABLE_NAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_CHANGECOLUMNS;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_COLUMNS;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_DATATABLE_NAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_DROPCOLUMNS;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_MULTIROW;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_SUBTYPE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.TABLE_FIELD_ID;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.TABLE_REGISTERED_TABLE;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.PersistenceException;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.service.CodeReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.core.service.database.JdbcJavaType;
import org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant;
import org.apache.fineract.infrastructure.dataqueries.data.DataTableValidator;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetRowData;
import org.apache.fineract.infrastructure.dataqueries.exception.DatatableEntryRequiredException;
import org.apache.fineract.infrastructure.dataqueries.exception.DatatableNotFoundException;
import org.apache.fineract.infrastructure.dataqueries.exception.DatatableSystemErrorException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlInjectionPreventerService;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.infrastructure.security.utils.SQLInjectionValidator;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class ReadWriteNonCoreDataServiceImpl implements ReadWriteNonCoreDataService {

    private static final String CODE_VALUES_TABLE = "m_code_value";

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseTypeResolver databaseTypeResolver;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final GenericDataService genericDataService;
    private final DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final ConfigurationDomainService configurationDomainService;
    private final CodeReadPlatformService codeReadPlatformService;
    private final DataTableValidator dataTableValidator;
    private final ColumnValidator columnValidator;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SqlInjectionPreventerService preventSqlInjectionService;
    private final DatatableKeywordGenerator datatableKeywordGenerator;

    @Override
    public List<DatatableData> retrieveDatatableNames(final String appTable) {
        // PERMITTED datatables
        String sql = "select application_table_name, registered_table_name, entity_subtype from x_registered_table where exists"
                + " (select 'f' from m_appuser_role ur join m_role r on r.id = ur.role_id"
                + " left join m_role_permission rp on rp.role_id = r.id left join m_permission p on p.id = rp.permission_id"
                + " where ur.appuser_id = ? and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat"
                + "('READ_', registered_table_name))) ";

        Object[] params;
        if (appTable != null) {
            sql = sql + " and application_table_name like ? ";
            params = new Object[] { this.context.authenticatedUser().getId(), appTable };
        } else {
            params = new Object[] { this.context.authenticatedUser().getId() };
        }
        sql = sql + " order by application_table_name, registered_table_name";

        final List<DatatableData> datatables = new ArrayList<>();

        final SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, params); // NOSONAR
        while (rowSet.next()) {
            final String appTableName = rowSet.getString("application_table_name");
            final String registeredDatatableName = rowSet.getString("registered_table_name");
            final String entitySubType = rowSet.getString("entity_subtype");
            final List<ResultsetColumnHeaderData> columnHeaderData = genericDataService.fillResultsetColumnHeaders(registeredDatatableName);

            datatables.add(DatatableData.create(appTableName, registeredDatatableName, entitySubType, columnHeaderData));
        }

        return datatables;
    }

    @Override
    public DatatableData retrieveDatatable(final String datatable) {
        // PERMITTED datatables
        SQLInjectionValidator.validateSQLInput(datatable);
        final String sql = "select application_table_name, registered_table_name, entity_subtype from x_registered_table "
                + " where exists (select 'f' from m_appuser_role ur join m_role r on r.id = ur.role_id"
                + " left join m_role_permission rp on rp.role_id = r.id left join m_permission p on p.id = rp.permission_id"
                + " where ur.appuser_id = ? and registered_table_name=? and (p.code in ('ALL_FUNCTIONS', "
                + "'ALL_FUNCTIONS_READ') or p.code = concat('READ_', registered_table_name))) "
                + " order by application_table_name, registered_table_name";

        DatatableData datatableData = null;

        final SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, new Object[] { this.context.authenticatedUser().getId(), datatable }); // NOSONAR
        if (rowSet.next()) {
            final String appTableName = rowSet.getString("application_table_name");
            final String registeredDatatableName = rowSet.getString("registered_table_name");
            final String entitySubType = rowSet.getString("entity_subtype");
            final List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService
                    .fillResultsetColumnHeaders(registeredDatatableName);

            datatableData = DatatableData.create(appTableName, registeredDatatableName, entitySubType, columnHeaderData);
        }

        return datatableData;
    }

    @Override
    public List<JsonObject> queryDataTable(@NotNull String datatable, @NotNull String columnName, String columnValueString,
            @NotNull String resultColumnsString) {
        datatable = validateDatatableRegistered(datatable);
        Map<String, ResultsetColumnHeaderData> columnHeaders = SearchUtil
                .mapHeadersToName(genericDataService.fillResultsetColumnHeaders(datatable));

        List<ApiParameterError> errors = new ArrayList<>();
        columnName = SearchUtil.validateToJdbcColumn(columnName, columnHeaders, errors, false);
        List<String> resultColumns = asList(resultColumnsString.split(","));
        List<String> selectColumns = SearchUtil.validateToJdbcColumns(resultColumns, columnHeaders, errors, false);
        if (!errors.isEmpty()) {
            throw new PlatformApiDataValidationException(errors);
        }

        Object columnValue = SearchUtil.parseAndValidateJdbcColumnValue(columnName, columnValueString, columnHeaders, false, sqlGenerator);
        String sql = sqlGenerator.buildSelect(selectColumns, null, false) + " " + sqlGenerator.buildFrom(datatable, null, false) + " WHERE "
                + EQ.formatPlaceholder(sqlGenerator, columnName, null);
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, columnValue);

        List<JsonObject> results = new ArrayList<>();
        while (rowSet.next()) {
            SearchUtil.extractJsonResult(rowSet, selectColumns, resultColumns, results);
        }
        return results;
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("Error occurred.", dve);
    }

    @Transactional
    @Override
    public void registerDatatable(final String dataTableName, final String entityName, final String entitySubType) {
        Integer category = DataTableApiConstant.CATEGORY_DEFAULT;

        final String permissionSql = this.getPermissionSql(dataTableName);
        this.registerDataTable(entityName, dataTableName, entitySubType, category, permissionSql);
    }

    @Transactional
    @Override
    public void registerDatatable(final JsonCommand command) {
        final String applicationTableName = this.getTableName(command.getUrl());
        final String dataTableName = this.getDataTableName(command.getUrl());
        final String entitySubType = command.stringValueOfParameterNamed("entitySubType");

        Integer category = this.getCategory(command);

        this.dataTableValidator.validateDataTableRegistration(command.json());
        final String permissionSql = this.getPermissionSql(dataTableName);
        this.registerDataTable(applicationTableName, dataTableName, entitySubType, category, permissionSql);
    }

    @Transactional
    @Override
    public void registerDatatable(final JsonCommand command, final String permissionSql) {
        final String applicationTableName = this.getTableName(command.getUrl());
        final String dataTableName = this.getDataTableName(command.getUrl());
        final String entitySubType = command.stringValueOfParameterNamed("entitySubType");

        Integer category = this.getCategory(command);

        this.dataTableValidator.validateDataTableRegistration(command.json());

        this.registerDataTable(applicationTableName, dataTableName, entitySubType, category, permissionSql);
    }

    private void registerDataTable(final String entityName, final String dataTableName, final String entitySubType, final Integer category,
            final String permissionsSql) {
        EntityTables entityTable = resolveEntity(entityName);
        validateDatatableName(dataTableName);
        validateDataTableExists(dataTableName);

        Map<String, Object> paramMap = new HashMap<>(3);
        final String registerDatatableSql = "insert into x_registered_table "
                + "(registered_table_name, application_table_name, entity_subtype, category) "
                + "values (:dataTableName, :applicationTableName, :entitySubType, :category)";
        paramMap.put("dataTableName", dataTableName);
        paramMap.put("applicationTableName", entityName);
        paramMap.put("entitySubType", entitySubType);
        paramMap.put("category", category);

        try {
            this.namedParameterJdbcTemplate.update(registerDatatableSql, paramMap);
            this.jdbcTemplate.update(permissionsSql);

            // add the registered table to the config if it is a ppi
            if (this.isSurveyCategory(category)) {
                this.namedParameterJdbcTemplate
                        .update("insert into c_configuration (name, value, enabled ) values( :dataTableName , '0',false)", paramMap);
            }

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable cause = dve.getCause();
            final Throwable realCause = dve.getMostSpecificCause();
            // even if duplicate is only due to permission duplicate, okay to
            // show duplicate datatable error msg
            if (realCause.getMessage().contains("Duplicate entry") || cause.getMessage().contains("Duplicate entry")) {
                throw new PlatformDataIntegrityException("error.msg.datatable.registered",
                        "Datatable `" + dataTableName + "` is already registered against an application table.", API_PARAM_DATATABLE_NAME,
                        dataTableName, dve);
            }
            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.", dve);
        } catch (final PersistenceException dve) {
            final Throwable cause = dve.getCause();
            if (cause.getMessage().contains("Duplicate entry")) {
                throw new PlatformDataIntegrityException("error.msg.datatable.registered",
                        "Datatable `" + dataTableName + "` is already registered against an application table.", API_PARAM_DATATABLE_NAME,
                        dataTableName, dve);
            }
            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.", dve);
        }
    }

    private String getPermissionSql(final String dataTableName) {
        final String createPermission = "'CREATE_" + dataTableName + "'";
        final String createPermissionChecker = "'CREATE_" + dataTableName + "_CHECKER'";
        final String readPermission = "'READ_" + dataTableName + "'";
        final String updatePermission = "'UPDATE_" + dataTableName + "'";
        final String updatePermissionChecker = "'UPDATE_" + dataTableName + "_CHECKER'";
        final String deletePermission = "'DELETE_" + dataTableName + "'";
        final String deletePermissionChecker = "'DELETE_" + dataTableName + "_CHECKER'";
        final List<String> escapedColumns = Stream.of("grouping", "code", "action_name", "entity_name", "can_maker_checker")
                .map(sqlGenerator::escape).toList();
        final String columns = String.join(", ", escapedColumns);

        return "insert into m_permission (" + columns + ") values " + "('datatable', " + createPermission + ", 'CREATE', '" + dataTableName
                + "', true)," + "('datatable', " + createPermissionChecker + ", 'CREATE', '" + dataTableName + "', false),"
                + "('datatable', " + readPermission + ", 'READ', '" + dataTableName + "', false)," + "('datatable', " + updatePermission
                + ", 'UPDATE', '" + dataTableName + "', true)," + "('datatable', " + updatePermissionChecker + ", 'UPDATE', '"
                + dataTableName + "', false)," + "('datatable', " + deletePermission + ", 'DELETE', '" + dataTableName + "', true),"
                + "('datatable', " + deletePermissionChecker + ", 'DELETE', '" + dataTableName + "', false)";
    }

    private Integer getCategory(final JsonCommand command) {
        Integer category = command.integerValueOfParameterNamedDefaultToNullIfZero(DataTableApiConstant.categoryParamName);
        if (category == null) {
            category = DataTableApiConstant.CATEGORY_DEFAULT;
        }
        return category;
    }

    private boolean isSurveyCategory(final Integer category) {
        return category.equals(DataTableApiConstant.CATEGORY_PPI);
    }

    private JsonElement addColumn(final String name, final JdbcJavaType dataType, final boolean isMandatory, final Integer length,
            final boolean isUnique, final boolean isIndexed) {
        JsonObject column = new JsonObject();
        column.addProperty(API_FIELD_NAME, name);
        column.addProperty(API_FIELD_TYPE, dataType.formatSql(databaseTypeResolver.databaseType()));
        if (dataType.isStringType()) {
            column.addProperty(API_FIELD_LENGTH, length);
        }
        column.addProperty(API_FIELD_MANDATORY, (isMandatory ? "true" : "false"));
        column.addProperty(API_FIELD_UNIQUE, (isUnique ? "true" : "false"));
        column.addProperty(API_FIELD_INDEXED, (isIndexed ? "true" : "false"));
        return column;
    }

    @Override
    public String getDataTableName(String url) {
        List<String> urlParts = Splitter.on('/').splitToList(url);
        return urlParts.get(3);
    }

    @Override
    public String getTableName(String url) {
        List<String> urlParts = Splitter.on('/').splitToList(url);
        return urlParts.get(4);
    }

    @Transactional
    @Override
    public void deregisterDatatable(final String datatable) {
        String validatedDatatable = this.preventSqlInjectionService.encodeSql(datatable);
        final String permissionList = "('CREATE_" + validatedDatatable + "', 'CREATE_" + validatedDatatable + "_CHECKER', 'READ_"
                + validatedDatatable + "', 'UPDATE_" + validatedDatatable + "', 'UPDATE_" + validatedDatatable + "_CHECKER', 'DELETE_"
                + validatedDatatable + "', 'DELETE_" + validatedDatatable + "_CHECKER')";

        final String deleteRolePermissionsSql = "delete from m_role_permission where m_role_permission.permission_id in (select id from m_permission where code in "
                + permissionList + ")";

        final String deletePermissionsSql = "delete from m_permission where code in " + permissionList;

        final String deleteRegisteredDatatableSql = "delete from x_registered_table where registered_table_name = '" + validatedDatatable
                + "'";

        final String deleteFromConfigurationSql = "delete from c_configuration where name ='" + validatedDatatable + "'";

        String[] sqlArray = new String[4];
        sqlArray[0] = deleteRolePermissionsSql;
        sqlArray[1] = deletePermissionsSql;
        sqlArray[2] = deleteRegisteredDatatableSql;
        sqlArray[3] = deleteFromConfigurationSql;

        this.jdbcTemplate.batchUpdate(sqlArray); // NOSONAR
    }

    @Transactional
    @Override
    public CommandProcessingResult createNewDatatableEntry(final String dataTableName, final Long appTableId, final JsonCommand command) {
        return createNewDatatableEntry(dataTableName, appTableId, command.json());
    }

    @Transactional
    @Override
    public CommandProcessingResult createNewDatatableEntry(final String dataTableName, final Long appTableId, final String json) {
        try {
            final EntityTables entityTable = queryForApplicationEntity(dataTableName);
            CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(entityTable, appTableId);

            final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);

            final boolean multiRow = isMultirowDatatable(columnHeaders);

            final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
            final Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, json);

            final String sql = getAddSql(columnHeaders, dataTableName, getFKField(entityTable), appTableId, dataParams);

            if (!multiRow) {
                this.jdbcTemplate.update(sql);
                commandProcessingResult = CommandProcessingResult.fromCommandProcessingResult(commandProcessingResult, appTableId);
            } else {
                final Long resourceId = addMultirowRecord(sql);
                commandProcessingResult = CommandProcessingResult.fromCommandProcessingResult(commandProcessingResult, resourceId);
            }

            return commandProcessingResult; //

        } catch (final SQLException e) {
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.", e);
        } catch (final DataAccessException dve) {
            final Throwable cause = dve.getCause();
            final Throwable realCause = dve.getMostSpecificCause();
            if (realCause.getMessage().contains("Duplicate entry") || cause.getMessage().contains("Duplicate entry")) {
                throw new PlatformDataIntegrityException(
                        "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                                + "` and application table with identifier `" + appTableId + "`.",
                        API_PARAM_DATATABLE_NAME, dataTableName, appTableId, dve);
            } else if (realCause.getMessage().contains("doesn't have a default value")
                    || cause.getMessage().contains("doesn't have a default value")) {
                throw new PlatformDataIntegrityException(
                        "error.msg.datatable.no.value.provided.for.required.fields", "No values provided for the datatable `"
                                + dataTableName + "` and application table with identifier `" + appTableId + "`.",
                        API_PARAM_DATATABLE_NAME, dataTableName, appTableId, dve);
            }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.", dve);
        } catch (final PersistenceException e) {
            final Throwable cause = e.getCause();
            if (cause.getMessage().contains("Duplicate entry")) {
                throw new PlatformDataIntegrityException(
                        "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                                + "` and application table with identifier `" + appTableId + "`.",
                        API_PARAM_DATATABLE_NAME, dataTableName, appTableId, e);
            } else if (cause.getMessage().contains("doesn't have a default value")) {
                throw new PlatformDataIntegrityException(
                        "error.msg.datatable.no.value.provided.for.required.fields", "No values provided for the datatable `"
                                + dataTableName + "` and application table with identifier `" + appTableId + "`.",
                        API_PARAM_DATATABLE_NAME, dataTableName, appTableId, e);
            }

            logAsErrorUnexpectedDataIntegrityException(e);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.", e);
        }
    }

    @Override
    public CommandProcessingResult createPPIEntry(final String dataTableName, final Long appTableId, final JsonCommand command) {
        try {
            final EntityTables entityTable = queryForApplicationEntity(dataTableName);
            final CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(entityTable, appTableId);

            final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);

            final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
            final Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

            final String sql = getAddSqlWithScore(columnHeaders, dataTableName, getFKField(entityTable), appTableId, dataParams);

            this.jdbcTemplate.update(sql);

            return commandProcessingResult; //

        } catch (final DataAccessException dve) {
            final Throwable cause = dve.getCause();
            final Throwable realCause = dve.getMostSpecificCause();
            if (realCause.getMessage().contains("Duplicate entry") || cause.getMessage().contains("Duplicate entry")) {
                throw new PlatformDataIntegrityException(
                        "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                                + "` and application table with identifier `" + appTableId + "`.",
                        API_PARAM_DATATABLE_NAME, dataTableName, appTableId, dve);
            }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.", dve);
        } catch (final PersistenceException dve) {
            final Throwable cause = dve.getCause();
            if (cause.getMessage().contains("Duplicate entry")) {
                throw new PlatformDataIntegrityException(
                        "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                                + "` and application table with identifier `" + appTableId + "`.",
                        API_PARAM_DATATABLE_NAME, dataTableName, appTableId, dve);
            }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.", dve);
        }
    }

    private String datatableColumnNameToCodeValueName(final String columnName, final String code) {

        return code + "_cd_" + columnName;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private void parseDatatableColumnObjectForCreate(final JsonObject column, StringBuilder sqlBuilder,
            final StringBuilder constrainBuilder, final String dataTableNameAlias, final Map<String, Long> codeMappings,
            final boolean isConstraintApproach) {
        String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        final String type = column.has(API_FIELD_TYPE) ? column.get(API_FIELD_TYPE).getAsString().toLowerCase() : null;
        final Integer length = column.has(API_FIELD_LENGTH) ? column.get(API_FIELD_LENGTH).getAsInt() : null;
        final Boolean mandatory = column.has(API_FIELD_MANDATORY) ? column.get(API_FIELD_MANDATORY).getAsBoolean() : false;
        final Boolean unique = column.has(API_FIELD_UNIQUE) ? column.get(API_FIELD_UNIQUE).getAsBoolean() : false;
        final String code = column.has(API_FIELD_CODE) ? column.get(API_FIELD_CODE).getAsString() : null;

        if (StringUtils.isNotBlank(code)) {
            if (isConstraintApproach) {
                codeMappings.put(dataTableNameAlias + "_" + name, this.codeReadPlatformService.retriveCode(code).getId());
                String fkName = "fk_" + dataTableNameAlias + "_" + name;
                constrainBuilder.append(", CONSTRAINT ").append(sqlGenerator.escape(fkName)).append(" ")
                        .append("FOREIGN KEY (" + sqlGenerator.escape(name) + ") ").append("REFERENCES ")
                        .append(sqlGenerator.escape(CODE_VALUES_TABLE)).append(" (id)");
            } else {
                name = datatableColumnNameToCodeValueName(name, code);
            }
        }
        sqlBuilder.append(sqlGenerator.escape(name));
        if (type != null) {
            sqlBuilder.append(" ").append(mapApiTypeToDbType(type, length));
        }

        if (unique) {
            String uniqueKeyName = datatableKeywordGenerator.generateUniqueKeyName(dataTableNameAlias, name);
            constrainBuilder.append(", CONSTRAINT ").append(sqlGenerator.escape(uniqueKeyName)).append(" ").append("UNIQUE (")
                    .append(sqlGenerator.escape(name)).append(")");
        }

        if (mandatory) {
            sqlBuilder.append(" NOT NULL");
        } else {
            sqlBuilder.append(" DEFAULT NULL");
        }

        sqlBuilder.append(", ");
    }

    @Transactional
    @Override
    public CommandProcessingResult createDatatable(final JsonCommand command) {
        String datatableName = null;
        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final JsonElement element = this.fromJsonHelper.parse(command.json());
            final JsonArray columns = this.fromJsonHelper.extractJsonArrayNamed(API_PARAM_COLUMNS, element);
            datatableName = this.fromJsonHelper.extractStringNamed(API_PARAM_DATATABLE_NAME, element);
            String entitySubType = this.fromJsonHelper.extractStringNamed(API_PARAM_SUBTYPE, element);
            final String entityName = this.fromJsonHelper.extractStringNamed(API_PARAM_APPTABLE_NAME, element);
            Boolean multiRow = this.fromJsonHelper.extractBooleanNamed(API_PARAM_MULTIROW, element);

            /*
             * In cases of tables storing hierarchical entities (like m_group), different entities would end up being
             * stored in the same table. Ex: Centers are a specific type of group, add abstractions for the same
             */
            if (multiRow == null) {
                multiRow = false;
            }

            validateDatatableName(datatableName);
            EntityTables entityTable = resolveEntity(entityName);
            final boolean isConstraintApproach = this.configurationDomainService.isConstraintApproachEnabledForDatatables();
            final String fkColumnName = getFKField(entityTable);
            final String dataTableNameAlias = datatableName.toLowerCase().replaceAll("\\s", "_");
            final String fkName = dataTableNameAlias + "_" + fkColumnName;
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("CREATE TABLE " + sqlGenerator.escape(datatableName) + " (");

            if (multiRow) {
                if (databaseTypeResolver.isMySQL()) {
                    sqlBuilder.append(TABLE_FIELD_ID).append(" BIGINT NOT NULL AUTO_INCREMENT, ");
                } else if (databaseTypeResolver.isPostgreSQL()) {
                    sqlBuilder.append(TABLE_FIELD_ID).append(
                            " bigint NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ), ");
                } else {
                    throw new IllegalStateException("Current database is not supported");
                }
            }
            sqlBuilder.append(sqlGenerator.escape(fkColumnName) + " BIGINT NOT NULL, ");

            // Add Created At and Updated At
            columns.add(addColumn(DataTableApiConstant.CREATEDAT_FIELD_NAME, DATETIME, false, null, false, false));
            columns.add(addColumn(DataTableApiConstant.UPDATEDAT_FIELD_NAME, DATETIME, false, null, false, false));

            final Map<String, Long> codeMappings = new HashMap<>();
            final StringBuilder constrainBuilder = new StringBuilder();
            for (final JsonElement column : columns) {
                parseDatatableColumnObjectForCreate(column.getAsJsonObject(), sqlBuilder, constrainBuilder, dataTableNameAlias,
                        codeMappings, isConstraintApproach);
            }

            // Remove trailing comma and space
            sqlBuilder = sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

            String fullFkName = "fk_" + fkName;
            if (multiRow) {
                sqlBuilder.append(", PRIMARY KEY (").append(TABLE_FIELD_ID).append(")");
                if (databaseTypeResolver.isMySQL()) {
                    sqlBuilder
                            .append(", KEY " + sqlGenerator.escape("fk_" + fkColumnName) + " (" + sqlGenerator.escape(fkColumnName) + ")");
                }
                sqlBuilder.append(", CONSTRAINT " + sqlGenerator.escape(fullFkName) + " ")
                        .append("FOREIGN KEY (" + sqlGenerator.escape(fkColumnName) + ") ")
                        .append("REFERENCES " + sqlGenerator.escape(entityTable.getApptableName()) + " (").append(TABLE_FIELD_ID)
                        .append(")");
            } else {
                sqlBuilder.append(", PRIMARY KEY (" + sqlGenerator.escape(fkColumnName) + ")")
                        .append(", CONSTRAINT " + sqlGenerator.escape(fullFkName) + " ")
                        .append("FOREIGN KEY (" + sqlGenerator.escape(fkColumnName) + ") ")
                        .append("REFERENCES " + sqlGenerator.escape(entityTable.getApptableName()) + " (").append(TABLE_FIELD_ID)
                        .append(")");
            }

            sqlBuilder.append(constrainBuilder);
            sqlBuilder.append(")");
            if (databaseTypeResolver.isMySQL()) {
                sqlBuilder.append(" ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;");
            }
            log.debug("SQL:: {}", sqlBuilder);

            jdbcTemplate.execute(sqlBuilder.toString());

            // create indexes
            if (multiRow) {
                createFkIndex(datatableName, fkColumnName);
            } else {
                /*
                 * in case of non-multirow, the primary key of the table is the FK and MySQL and PostgreSQL
                 * automatically puts an index onto it so no need to create it explicitly
                 */
            }
            createIndexesForTable(datatableName, columns);
            registerDatatable(datatableName, entityName, entitySubType);
            registerColumnCodeMapping(codeMappings);
        } catch (final PersistenceException | DataAccessException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

            if (realCause.getMessage().toLowerCase().contains("duplicate column name")) {
                baseDataValidator.reset().parameter(API_FIELD_NAME).failWithCode("duplicate.column.name");
            } else if ((realCause.getMessage().contains("Table") || realCause.getMessage().contains("relation"))
                    && realCause.getMessage().contains("already exists")) {
                baseDataValidator.reset().parameter(API_PARAM_DATATABLE_NAME).value(datatableName).failWithCode("datatable.already.exists");
            } else if (realCause.getMessage().contains("Column") && realCause.getMessage().contains("big")) {
                baseDataValidator.reset().parameter("column").failWithCode("length.too.big");
            } else if (realCause.getMessage().contains("Row") && realCause.getMessage().contains("large")) {
                baseDataValidator.reset().parameter("row").failWithCode("size.too.large");
            }

            throwExceptionIfValidationWarningsExist(dataValidationErrors);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withResourceIdAsString(datatableName).build();
    }

    private void createFkIndex(String datatableName, String fkColumnName) {
        String indexName = datatableKeywordGenerator.generateIndexName(datatableName, fkColumnName);
        createIndex(indexName, datatableName, fkColumnName);
    }

    private void createIndexesForTable(String datatableName, JsonArray columns) {
        for (final JsonElement column : columns) {
            createIndexForColumn(datatableName, column.getAsJsonObject());
        }
    }

    private void createIndexForColumn(String datatableName, JsonObject column) {
        String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        final Boolean unique = column.has(API_FIELD_UNIQUE) ? column.get(API_FIELD_UNIQUE).getAsBoolean() : false;
        final Boolean indexed = column.has(API_FIELD_INDEXED) ? column.get(API_FIELD_INDEXED).getAsBoolean() : false;
        if (indexed) {
            if (!unique) {
                String indexName = datatableKeywordGenerator.generateIndexName(datatableName, name);
                createIndex(indexName, datatableName, name);
            }
        }
    }

    private long addMultirowRecord(String sql) throws SQLException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int insertsCount = this.jdbcTemplate.update(c -> c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS), keyHolder);
        if (insertsCount == 1) {
            Number assignedKey = null;
            if (keyHolder.getKeys().size() > 1) {
                assignedKey = (Long) keyHolder.getKeys().get(TABLE_FIELD_ID);
            } else {
                assignedKey = keyHolder.getKey();
            }
            if (assignedKey == null) {
                throw new SQLException("Row id getting error.");
            }
            return assignedKey.longValue();
        }
        throw new SQLException("Expected one inserted row.");
    }

    private void parseDatatableColumnForUpdate(final JsonObject column,
            final Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition, StringBuilder sqlBuilder, final String datatableName,
            final StringBuilder constrainBuilder, final Map<String, Long> codeMappings, final List<String> removeMappings,
            final boolean isConstraintApproach) {
        String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        final String lengthStr = column.has(API_FIELD_LENGTH) ? column.get(API_FIELD_LENGTH).getAsString() : null;
        Integer length = StringUtils.isNotBlank(lengthStr) ? Integer.parseInt(lengthStr) : null;
        String newName = column.has(API_FIELD_NEWNAME) ? column.get(API_FIELD_NEWNAME).getAsString() : name;
        final Boolean mandatory = column.has(API_FIELD_MANDATORY) ? column.get(API_FIELD_MANDATORY).getAsBoolean() : false;
        final String after = column.has(API_FIELD_AFTER) ? column.get(API_FIELD_AFTER).getAsString() : null;
        final String code = column.has(API_FIELD_CODE) ? column.get(API_FIELD_CODE).getAsString() : null;
        final String newCode = column.has(API_FIELD_NEWCODE) ? column.get(API_FIELD_NEWCODE).getAsString() : null;
        final String dataTableNameAlias = datatableName.toLowerCase().replaceAll("\\s", "_");
        if (isConstraintApproach) {
            if (StringUtils.isBlank(newName)) {
                newName = name;
            }
            String fkName = "fk_" + dataTableNameAlias + "_" + name;
            String newFkName = "fk_" + dataTableNameAlias + "_" + newName;
            if (!StringUtils.equalsIgnoreCase(code, newCode) || !StringUtils.equalsIgnoreCase(name, newName)) {
                if (StringUtils.equalsIgnoreCase(code, newCode)) {
                    final int codeId = getCodeIdForColumn(dataTableNameAlias, name);
                    if (codeId > 0) {
                        removeMappings.add(dataTableNameAlias + "_" + name);
                        constrainBuilder.append(", DROP FOREIGN KEY ").append(sqlGenerator.escape(fkName)).append(" ");
                        codeMappings.put(dataTableNameAlias + "_" + newName, (long) codeId);
                        constrainBuilder.append(",ADD CONSTRAINT ").append(sqlGenerator.escape(newFkName)).append(" ")
                                .append("FOREIGN KEY (" + sqlGenerator.escape(newName) + ") ").append("REFERENCES ")
                                .append(sqlGenerator.escape(CODE_VALUES_TABLE)).append(" (").append(TABLE_FIELD_ID).append(")");
                    }

                } else {
                    if (code != null) {
                        removeMappings.add(dataTableNameAlias + "_" + name);
                        if (newCode == null || !StringUtils.equalsIgnoreCase(name, newName)) {
                            constrainBuilder.append(", DROP FOREIGN KEY ").append(sqlGenerator.escape(fkName)).append(" ");
                        }
                    }
                    if (newCode != null) {
                        codeMappings.put(dataTableNameAlias + "_" + newName, this.codeReadPlatformService.retriveCode(newCode).getId());
                        if (code == null || !StringUtils.equalsIgnoreCase(name, newName)) {
                            constrainBuilder.append(",ADD CONSTRAINT  ").append(sqlGenerator.escape(newFkName)).append(" ")
                                    .append("FOREIGN KEY (" + sqlGenerator.escape(newName) + ") ").append("REFERENCES ")
                                    .append(sqlGenerator.escape(CODE_VALUES_TABLE)).append(" (").append(TABLE_FIELD_ID).append(")");
                        }
                    }
                }
            }
        } else {
            if (StringUtils.isNotBlank(code)) {
                name = datatableColumnNameToCodeValueName(name, code);
                if (StringUtils.isNotBlank(newCode)) {
                    newName = datatableColumnNameToCodeValueName(newName, newCode);
                } else {
                    newName = datatableColumnNameToCodeValueName(newName, code);
                }
            }
        }
        if (!mapColumnNameDefinition.containsKey(name)) {
            throw new PlatformDataIntegrityException("error.msg.datatable.column.missing.update.parse",
                    "Column " + name + " does not exist.", name);
        }
        final JdbcJavaType type = mapColumnNameDefinition.get(name).getColumnType();
        DatabaseType dialect = databaseTypeResolver.databaseType();
        if (length == null && type.hasPrecision(dialect)) {
            Long columnLength = mapColumnNameDefinition.get(name).getColumnLength();
            length = columnLength == null ? null : columnLength.intValue();
        }

        if (databaseTypeResolver.isMySQL()) {
            sqlBuilder.append(", CHANGE ").append(sqlGenerator.escape(name)).append(" ").append(sqlGenerator.escape(newName)).append(" ")
                    .append(type);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            sqlBuilder.append(", RENAME ").append(sqlGenerator.escape(name)).append(" TO ").append(sqlGenerator.escape(newName));
        }
        if (length != null && length > 0) {
            if (type.isDecimalType()) {
                sqlBuilder.append(" ").append(type.formatSql(dialect, 19, 6));
            } else {
                sqlBuilder.append(" ").append(type.formatSql(dialect, length));
            }
        }

        if (databaseTypeResolver.isMySQL()) {
            sqlBuilder.append(mandatory ? " NOT NULL" : " DEFAULT NULL");
        }

        if (after != null) {
            sqlBuilder.append(" AFTER " + sqlGenerator.escape(after));
        }
    }

    private int getCodeIdForColumn(final String dataTableNameAlias, final String name) {
        final StringBuilder checkColumnCodeMapping = new StringBuilder();
        checkColumnCodeMapping.append("select ccm.code_id from x_table_column_code_mappings ccm where ccm.column_alias_name='")
                .append(dataTableNameAlias).append("_").append(name).append("'");
        Integer codeId = 0;
        try {
            codeId = this.jdbcTemplate.queryForObject(checkColumnCodeMapping.toString(), Integer.class);
        } catch (final EmptyResultDataAccessException e) {
            log.warn("Error occurred.", e);
        }
        return ObjectUtils.defaultIfNull(codeId, 0);
    }

    private void parseDatatableColumnForAdd(final JsonObject column, StringBuilder sqlBuilder, final String dataTableNameAlias,
            final StringBuilder constrainBuilder, final Map<String, Long> codeMappings, final boolean isConstraintApproach) {

        String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        final String type = column.has(API_FIELD_TYPE) ? column.get(API_FIELD_TYPE).getAsString().toLowerCase() : null;
        final Integer length = column.has(API_FIELD_LENGTH) ? column.get(API_FIELD_LENGTH).getAsInt() : null;
        final Boolean mandatory = column.has(API_FIELD_MANDATORY) ? column.get(API_FIELD_MANDATORY).getAsBoolean() : false;
        final Boolean unique = column.has(API_FIELD_UNIQUE) ? column.get(API_FIELD_UNIQUE).getAsBoolean() : false;
        final String after = column.has(API_FIELD_AFTER) ? column.get(API_FIELD_AFTER).getAsString() : null;
        final String code = column.has(API_FIELD_CODE) ? column.get(API_FIELD_CODE).getAsString() : null;

        if (StringUtils.isNotBlank(code)) {
            if (isConstraintApproach) {
                String fkName = "fk_" + dataTableNameAlias + "_" + name;
                codeMappings.put(dataTableNameAlias + "_" + name, this.codeReadPlatformService.retriveCode(code).getId());
                constrainBuilder.append(",ADD CONSTRAINT  ").append(sqlGenerator.escape(fkName)).append(" ")
                        .append("FOREIGN KEY (" + sqlGenerator.escape(name) + ") ").append("REFERENCES ")
                        .append(sqlGenerator.escape(CODE_VALUES_TABLE)).append(" (").append(TABLE_FIELD_ID).append(")");
            } else {
                name = datatableColumnNameToCodeValueName(name, code);
            }
        }
        sqlBuilder.append(", ADD ").append(sqlGenerator.escape(name)).append(" ").append(mapApiTypeToDbType(type, length));

        if (unique) {
            String uniqueKeyName = datatableKeywordGenerator.generateUniqueKeyName(dataTableNameAlias, name);
            constrainBuilder.append(",ADD CONSTRAINT  ").append(sqlGenerator.escape(uniqueKeyName)).append(" ")
                    .append("UNIQUE (" + sqlGenerator.escape(name) + ")");
        }

        if (mandatory) {
            sqlBuilder.append(" NOT NULL");
        } else {
            sqlBuilder.append(" DEFAULT NULL");
        }

        if (after != null) {
            sqlBuilder.append(" AFTER " + sqlGenerator.escape(after));
        }
    }

    private void parseDatatableColumnForDrop(final JsonObject column, StringBuilder sqlBuilder, final String datatableName,
            final StringBuilder constrainBuilder, final List<String> codeMappings) {
        final String datatableAlias = datatableName.toLowerCase().replaceAll("\\s", "_");
        final String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        final StringBuilder findFKSql = new StringBuilder();
        findFKSql.append("SELECT count(*)").append("FROM information_schema.TABLE_CONSTRAINTS i")
                .append(" WHERE i.CONSTRAINT_TYPE = 'FOREIGN KEY'").append(" AND i.TABLE_SCHEMA = DATABASE()")
                .append(" AND i.TABLE_NAME = '").append(datatableName).append("' AND i.CONSTRAINT_NAME = 'fk_").append(datatableAlias)
                .append("_").append(name).append("' ");
        final int count = this.jdbcTemplate.queryForObject(findFKSql.toString(), Integer.class);
        if (count > 0) {
            String fkName = "fk_" + datatableAlias + "_" + name;
            codeMappings.add(datatableAlias + "_" + name);
            constrainBuilder.append(", DROP FOREIGN KEY ").append(sqlGenerator.escape(fkName)).append(" ");
        }
    }

    private void registerColumnCodeMapping(final Map<String, Long> codeMappings) {
        if (codeMappings != null && !codeMappings.isEmpty()) {
            final String[] addSqlList = new String[codeMappings.size()];
            int i = 0;
            for (final Map.Entry<String, Long> mapEntry : codeMappings.entrySet()) {
                addSqlList[i++] = "insert into x_table_column_code_mappings (column_alias_name, code_id) values ('" + mapEntry.getKey()
                        + "'," + mapEntry.getValue() + ");";
            }

            this.jdbcTemplate.batchUpdate(addSqlList);
        }
    }

    private void deleteColumnCodeMapping(final List<String> columnNames) {
        if (columnNames != null && !columnNames.isEmpty()) {
            final String[] deleteSqlList = new String[columnNames.size()];
            int i = 0;
            for (final String columnName : columnNames) {
                deleteSqlList[i++] = "DELETE FROM x_table_column_code_mappings WHERE  column_alias_name='" + columnName + "';";
            }

            this.jdbcTemplate.batchUpdate(deleteSqlList);
        }
    }

    /**
     * Update data table, set column value to empty string where current value is NULL. Run update SQL only if the
     * "mandatory" property is set to true
     *
     * @param datatableName
     *            Name of data table
     * @param column
     *            JSON encoded array of column properties
     * @see <a href="https://mifosforge.jira.com/browse/MIFOSX-1145">MIFOSX-1145</a>
     **/
    private void removeNullValuesFromStringColumn(final String datatableName, final JsonObject column,
            final Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition) {
        final boolean mandatory = column.has(API_FIELD_MANDATORY) && column.get(API_FIELD_MANDATORY).getAsBoolean();
        final String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : "";
        final JdbcJavaType type = mapColumnNameDefinition.containsKey(name) ? mapColumnNameDefinition.get(name).getColumnType() : null;

        if (type != null && mandatory && type.isStringType()) {
            String sql = "UPDATE " + sqlGenerator.escape(datatableName) + " SET " + sqlGenerator.escape(name) + " = '' WHERE "
                    + sqlGenerator.escape(name) + " IS NULL";
            this.jdbcTemplate.update(sql);
        }
    }

    @Transactional
    @Override
    public void updateDatatable(final String datatableName, final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final JsonElement element = this.fromJsonHelper.parse(command.json());
            final JsonArray changeColumns = this.fromJsonHelper.extractJsonArrayNamed(API_PARAM_CHANGECOLUMNS, element);
            final JsonArray addColumns = this.fromJsonHelper.extractJsonArrayNamed(API_PARAM_ADDCOLUMNS, element);
            final JsonArray dropColumns = this.fromJsonHelper.extractJsonArrayNamed(API_PARAM_DROPCOLUMNS, element);
            final String entityName = this.fromJsonHelper.extractStringNamed(API_PARAM_APPTABLE_NAME, element);
            final String entitySubType = this.fromJsonHelper.extractStringNamed(API_PARAM_SUBTYPE, element);

            validateDatatableName(datatableName);
            int rowCount = getRowCount(datatableName);
            final List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService.fillResultsetColumnHeaders(datatableName);
            final Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition = SearchUtil.mapHeadersToName(columnHeaderData);

            final boolean isConstraintApproach = this.configurationDomainService.isConstraintApproachEnabledForDatatables();

            if (!StringUtils.isBlank(entitySubType)) {
                jdbcTemplate.update("update x_registered_table SET entity_subtype=? WHERE registered_table_name = ?", // NOSONAR
                        new Object[] { entitySubType, datatableName });
            }

            if (!StringUtils.isBlank(entityName)) {
                EntityTables entityTable = resolveEntity(entityName);
                EntityTables oldEntityTable = queryForApplicationEntity(datatableName);
                if (entityTable != oldEntityTable) {
                    final String oldFKName = getFKField(oldEntityTable);
                    final String newFKName = getFKField(entityTable);
                    final String oldConstraintName = datatableName.toLowerCase().replaceAll("\\s", "_") + "_" + oldFKName;
                    final String newConstraintName = datatableName.toLowerCase().replaceAll("\\s", "_") + "_" + newFKName;
                    StringBuilder sqlBuilder = new StringBuilder();

                    String fullOldFk = "fk_" + oldFKName;
                    String fullOldConstraint = "fk_" + oldConstraintName;
                    String fullNewFk = "fk_" + newFKName;
                    String fullNewConstraint = "fk_" + newConstraintName;
                    if (mapColumnNameDefinition.containsKey(TABLE_FIELD_ID)) {
                        sqlBuilder.append("ALTER TABLE " + sqlGenerator.escape(datatableName) + " ")
                                .append("DROP KEY " + sqlGenerator.escape(fullOldFk) + ",")
                                .append("DROP FOREIGN KEY " + sqlGenerator.escape(fullOldConstraint) + ",")
                                .append("CHANGE COLUMN " + sqlGenerator.escape(oldFKName) + " " + sqlGenerator.escape(newFKName)
                                        + " BIGINT NOT NULL,")
                                .append("ADD KEY " + sqlGenerator.escape(fullNewFk) + " (" + sqlGenerator.escape(newFKName) + "),")
                                .append("ADD CONSTRAINT " + sqlGenerator.escape(fullNewConstraint) + " ")
                                .append("FOREIGN KEY (" + sqlGenerator.escape(newFKName) + ") ")
                                .append("REFERENCES " + sqlGenerator.escape(entityTable.getApptableName()) + " (").append(TABLE_FIELD_ID)
                                .append(")");
                    } else {
                        sqlBuilder.append("ALTER TABLE " + sqlGenerator.escape(datatableName) + " ")
                                .append("DROP FOREIGN KEY " + sqlGenerator.escape(fullOldConstraint) + ",")
                                .append("CHANGE COLUMN " + sqlGenerator.escape(oldFKName) + " " + sqlGenerator.escape(newFKName)
                                        + " BIGINT NOT NULL,")
                                .append("ADD CONSTRAINT " + sqlGenerator.escape(fullNewConstraint) + " ")
                                .append("FOREIGN KEY (" + sqlGenerator.escape(newFKName) + ") ")
                                .append("REFERENCES " + sqlGenerator.escape(entityTable.getApptableName()) + " (").append(TABLE_FIELD_ID)
                                .append(")");
                    }

                    this.jdbcTemplate.execute(sqlBuilder.toString());

                    deregisterDatatable(datatableName);
                    registerDatatable(datatableName, entityName, entitySubType);
                }
            }

            if (changeColumns == null && addColumns == null && dropColumns == null) {
                return;
            }

            if (dropColumns != null) {
                if (rowCount > 0) {
                    throw new GeneralPlatformDomainRuleException("error.msg.non.empty.datatable.column.cannot.be.deleted",
                            "Non-empty datatable columns can not be deleted.");
                }
                StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE " + sqlGenerator.escape(datatableName));
                final StringBuilder constrainBuilder = new StringBuilder();
                final List<String> codeMappings = new ArrayList<>();
                for (final JsonElement column : dropColumns) {
                    parseDatatableColumnForDrop(column.getAsJsonObject(), sqlBuilder, datatableName, constrainBuilder, codeMappings);
                }

                // Remove the first comma, right after ALTER TABLE datatable
                final int indexOfFirstComma = sqlBuilder.indexOf(",");
                if (indexOfFirstComma != -1) {
                    sqlBuilder = sqlBuilder.deleteCharAt(indexOfFirstComma);
                }
                sqlBuilder.append(constrainBuilder);
                this.jdbcTemplate.execute(sqlBuilder.toString());
                deleteColumnCodeMapping(codeMappings);
            }
            if (addColumns != null) {

                StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE " + sqlGenerator.escape(datatableName));
                final StringBuilder constrainBuilder = new StringBuilder();
                final Map<String, Long> codeMappings = new HashMap<>();
                for (final JsonElement column : addColumns) {
                    JsonObject columnAsJson = column.getAsJsonObject();
                    if (rowCount > 0 && columnAsJson.has(API_FIELD_MANDATORY) && columnAsJson.get(API_FIELD_MANDATORY).getAsBoolean()) {
                        throw new GeneralPlatformDomainRuleException("error.msg.non.empty.datatable.mandatory.column.cannot.be.added",
                                "Non empty datatable mandatory columns can not be added.");
                    }
                    parseDatatableColumnForAdd(columnAsJson, sqlBuilder, datatableName.toLowerCase().replaceAll("\\s", "_"),
                            constrainBuilder, codeMappings, isConstraintApproach);
                }

                // Remove the first comma, right after ALTER TABLE datatable
                final int indexOfFirstComma = sqlBuilder.indexOf(",");
                if (indexOfFirstComma != -1) {
                    sqlBuilder = sqlBuilder.deleteCharAt(indexOfFirstComma);
                }
                sqlBuilder.append(constrainBuilder);
                jdbcTemplate.execute(sqlBuilder.toString());
                createIndexesForTable(datatableName, addColumns);
                registerColumnCodeMapping(codeMappings);
            }
            if (changeColumns != null) {
                StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE " + sqlGenerator.escape(datatableName));
                final StringBuilder constrainBuilder = new StringBuilder();
                final Map<String, Long> codeMappings = new HashMap<>();
                final List<String> removeMappings = new ArrayList<>();
                for (final JsonElement column : changeColumns) {
                    // remove NULL values from column where mandatory is true
                    removeNullValuesFromStringColumn(datatableName, column.getAsJsonObject(), mapColumnNameDefinition);

                    parseDatatableColumnForUpdate(column.getAsJsonObject(), mapColumnNameDefinition, sqlBuilder, datatableName,
                            constrainBuilder, codeMappings, removeMappings, isConstraintApproach);
                }

                // Remove the first comma, right after ALTER TABLE datatable
                final int indexOfFirstComma = sqlBuilder.indexOf(",");
                if (indexOfFirstComma != -1) {
                    sqlBuilder = sqlBuilder.deleteCharAt(indexOfFirstComma);
                }
                sqlBuilder.append(constrainBuilder);
                try {
                    jdbcTemplate.execute(sqlBuilder.toString());
                    deleteColumnCodeMapping(removeMappings);
                    registerColumnCodeMapping(codeMappings);
                    // update unique constraint
                    updateUniqueConstraintsForTable(datatableName, changeColumns, mapColumnNameDefinition);
                    // update indexes
                    updateIndexesForTable(datatableName, changeColumns, mapColumnNameDefinition);
                } catch (final Exception e) {
                    log.error("Exception while modifying a datatable", e);
                    if (e.getMessage().contains("Error on rename")) {
                        throw new PlatformServiceUnavailableException("error.msg.datatable.column.update.not.allowed",
                                "One of the column name modification not allowed", e);
                    }
                    // handle all other exceptions in here

                    // check if exception message contains the
                    // "invalid use of null value" SQL exception message
                    // throw a 503 HTTP error -
                    // PlatformServiceUnavailableException
                    if (e.getMessage().toLowerCase().contains("invalid use of null value")) {
                        throw new PlatformServiceUnavailableException("error.msg.datatable.column.update.not.allowed",
                                "One of the data table columns contains null values", e);
                    }
                }
            }
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

            if (realCause.getMessage().toLowerCase().contains("unknown column")) {
                baseDataValidator.reset().parameter(API_FIELD_NAME).failWithCode("does.not.exist");
            } else if (realCause.getMessage().toLowerCase().contains("can't drop")) {
                baseDataValidator.reset().parameter(API_FIELD_NAME).failWithCode("does.not.exist");
            } else if (realCause.getMessage().toLowerCase().contains("duplicate column")) {
                baseDataValidator.reset().parameter(API_FIELD_NAME).failWithCode("column.already.exists");
            }

            throwExceptionIfValidationWarningsExist(dataValidationErrors);
        } catch (final PersistenceException ee) {
            Throwable realCause = ExceptionUtils.getRootCause(ee.getCause());
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");
            if (realCause.getMessage().toLowerCase().contains("duplicate column name")) {
                baseDataValidator.reset().parameter(API_FIELD_NAME).failWithCode("duplicate.column.name");
            } else if ((realCause.getMessage().contains("Table") || realCause.getMessage().contains("relation"))
                    && realCause.getMessage().contains("already exists")) {
                baseDataValidator.reset().parameter(API_PARAM_DATATABLE_NAME).value(datatableName).failWithCode("datatable.already.exists");
            } else if (realCause.getMessage().contains("Column") && realCause.getMessage().contains("big")) {
                baseDataValidator.reset().parameter("column").failWithCode("length.too.big");
            } else if (realCause.getMessage().contains("Row") && realCause.getMessage().contains("large")) {
                baseDataValidator.reset().parameter("row").failWithCode("size.too.large");
            }

            throwExceptionIfValidationWarningsExist(dataValidationErrors);
        }
    }

    private void updateUniqueConstraintsForTable(String datatableName, JsonArray changeColumns,
            Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition) {
        for (final JsonElement column : changeColumns) {
            String name = column.getAsJsonObject().has(API_FIELD_NAME) ? column.getAsJsonObject().get(API_FIELD_NAME).getAsString() : null;

            if (!mapColumnNameDefinition.containsKey(name)) {
                throw new PlatformDataIntegrityException("error.msg.datatable.column.missing.update.parse",
                        "Column " + name + " does not exist.", name);
            }

            updateColumnUniqueConstraints(datatableName, column.getAsJsonObject(),
                    mapColumnNameDefinition.get(column.getAsJsonObject().get(API_FIELD_NAME).getAsString()));
        }
    }

    private void updateColumnUniqueConstraints(String datatableName, JsonObject column, ResultsetColumnHeaderData columnMetaData) {
        // check for unique constraint update
        String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        String columnNewName = column.has(API_FIELD_NEWNAME) ? column.get(API_FIELD_NEWNAME).getAsString() : null;
        final Boolean setUnique = column.has(API_FIELD_UNIQUE) ? column.get(API_FIELD_UNIQUE).getAsBoolean() : false;
        final Boolean isAlreadyUnique = genericDataService.isExplicitlyUnique(datatableName, name);
        String uniqueKeyName = datatableKeywordGenerator.generateUniqueKeyName(datatableName, name);

        if (isAlreadyUnique) {
            if (!setUnique) {
                // drop existing constraint
                dropUniqueConstraint(datatableName, uniqueKeyName);
            } else {
                // if columnname changed
                checkColumnRenameAndModifyUniqueConstraint(datatableName, columnNewName, uniqueKeyName);
            }
        } else {
            if (setUnique) {
                checkColumnRenameAndCreateUniqueConstraint(datatableName, name, columnNewName, uniqueKeyName);
            }
        }
    }

    private void checkColumnRenameAndCreateUniqueConstraint(String datatableName, String name, String columnNewName, String constraintKey) {
        if (columnNewName != null) {
            // create constraint with new column name
            String uniqueKeyName = datatableKeywordGenerator.generateUniqueKeyName(datatableName, columnNewName);
            createUniqueConstraint(datatableName, columnNewName, uniqueKeyName);
        } else {
            // create constraint for column
            createUniqueConstraint(datatableName, name, constraintKey);
        }
    }

    private void checkColumnRenameAndModifyUniqueConstraint(String datatableName, String columnNewName, String existingConstraint) {
        if (columnNewName != null) {
            // drop existing constraint
            dropUniqueConstraint(datatableName, existingConstraint);
            // create constraint with new column name
            String uniqueKeyName = datatableKeywordGenerator.generateUniqueKeyName(datatableName, columnNewName);
            createUniqueConstraint(datatableName, columnNewName, uniqueKeyName);
        }
    }

    private void createUniqueConstraint(String datatableName, String columnName, String uniqueKeyName) {
        StringBuilder constrainBuilder = new StringBuilder();
        constrainBuilder.append("ALTER TABLE ").append(sqlGenerator.escape(datatableName)).append(" ADD CONSTRAINT ")
                .append(sqlGenerator.escape(uniqueKeyName)).append(" UNIQUE (" + sqlGenerator.escape(columnName) + ");");
        this.jdbcTemplate.execute(constrainBuilder.toString());
    }

    private void dropUniqueConstraint(String datatableName, String uniqueKeyName) {
        StringBuilder constrainBuilder = new StringBuilder();
        constrainBuilder.append("ALTER TABLE ").append(sqlGenerator.escape(datatableName)).append(" DROP CONSTRAINT ")
                .append(sqlGenerator.escape(uniqueKeyName)).append(";");
        this.jdbcTemplate.execute(constrainBuilder.toString());
    }

    private void updateIndexesForTable(String datatableName, JsonArray changeColumns,
            Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition) {
        for (final JsonElement column : changeColumns) {
            String name = column.getAsJsonObject().has(API_FIELD_NAME) ? column.getAsJsonObject().get(API_FIELD_NAME).getAsString() : null;
            if (!mapColumnNameDefinition.containsKey(name)) {
                throw new PlatformDataIntegrityException("error.msg.datatable.column.missing.update.parse",
                        "Column " + name + " does not exist.", name);
            }
            updateIndexForColumn(datatableName, column.getAsJsonObject(),
                    mapColumnNameDefinition.get(column.getAsJsonObject().get(API_FIELD_NAME).getAsString()));
        }
    }

    private void updateIndexForColumn(String datatableName, JsonObject column, ResultsetColumnHeaderData columnMetaData) {
        String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        String columnNewName = column.has(API_FIELD_NEWNAME) ? column.get(API_FIELD_NEWNAME).getAsString() : null;
        final Boolean setForUnique = column.has(API_FIELD_UNIQUE) ? column.get(API_FIELD_UNIQUE).getAsBoolean() : false;
        final Boolean setForIndexed = column.has(API_FIELD_INDEXED) ? column.get(API_FIELD_INDEXED).getAsBoolean() : false;
        if (!setForUnique) {
            final Boolean isAlreadyIndexed = genericDataService.isExplicitlyIndexed(datatableName, name);
            String indexName = datatableKeywordGenerator.generateIndexName(datatableName, name);
            if (isAlreadyIndexed) {
                if (!setForIndexed) {
                    // drop index
                    dropIndex(datatableName, indexName);
                } else { // if column name changed
                    checkColumnRenameAndModifyIndex(datatableName, columnNewName, indexName);
                }

            } else {
                if (setForIndexed) {
                    checkColumnRenameAndCreateIndex(datatableName, name, columnNewName, indexName);
                }
            }
        }
    }

    private void checkColumnRenameAndCreateIndex(String datatableName, String columnExistingName, String columnNewName, String indexName) {
        if (columnNewName != null) {
            String newIndexName = datatableKeywordGenerator.generateIndexName(datatableName, columnNewName);
            // create index with new column name
            createIndex(newIndexName, datatableName, columnNewName);
        } else {
            // create index with previous name
            createIndex(indexName, datatableName, columnExistingName);
        }
    }

    private void checkColumnRenameAndModifyIndex(String datatableName, String columnNewName, String existingIndex) {
        if (columnNewName != null) {
            // drop index with previous name
            dropIndex(datatableName, existingIndex);
            // create index with new name
            String newIndexName = datatableKeywordGenerator.generateIndexName(datatableName, columnNewName);
            createIndex(newIndexName, datatableName, columnNewName);
        }
    }

    private void createIndex(String indexName, String tableName, String columnName) {
        String safeIndexName = sqlGenerator.escape(indexName);
        String safeTableName = sqlGenerator.escape(tableName);
        String safeColumnName = sqlGenerator.escape(columnName);
        String sqlIndexUpdateBuilder = "CREATE INDEX %s ON %s (%s);".formatted(safeIndexName, safeTableName, safeColumnName);
        jdbcTemplate.execute(sqlIndexUpdateBuilder);
    }

    private void dropIndex(String datatableName, String uniqueIndexName) {
        StringBuilder sqlIndexUpdateBuilder = new StringBuilder();
        if (databaseTypeResolver.isMySQL()) {
            sqlIndexUpdateBuilder.append("ALTER TABLE ").append(sqlGenerator.escape(datatableName)).append(" ");
        }
        sqlIndexUpdateBuilder.append("DROP INDEX ").append(sqlGenerator.escape(uniqueIndexName)).append(";");
        jdbcTemplate.execute(sqlIndexUpdateBuilder.toString());
    }

    @Transactional
    @Override
    public void deleteDatatable(final String datatableName) {
        try {
            this.context.authenticatedUser();
            validateDatatableRegistered(datatableName);
            assertDataTableEmpty(datatableName);
            deregisterDatatable(datatableName);
            String[] sqlArray = null;
            if (this.configurationDomainService.isConstraintApproachEnabledForDatatables()) {
                final String deleteColumnCodeSql = "delete from x_table_column_code_mappings where column_alias_name like'"
                        + datatableName.toLowerCase().replaceAll("\\s", "_") + "_%'";
                sqlArray = new String[2];
                sqlArray[1] = deleteColumnCodeSql;
            } else {
                sqlArray = new String[1];
            }
            final String sql = "DROP TABLE " + sqlGenerator.escape(datatableName);
            sqlArray[0] = sql;
            this.jdbcTemplate.batchUpdate(sqlArray);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");
            if (realCause.getMessage().contains("Unknown table")) {
                baseDataValidator.reset().parameter(API_PARAM_DATATABLE_NAME).failWithCode("does.not.exist");
            }

            throwExceptionIfValidationWarningsExist(dataValidationErrors);
        }
    }

    private void assertDataTableEmpty(final String datatableName) {
        final int rowCount = getRowCount(datatableName);
        if (rowCount != 0) {
            throw new GeneralPlatformDomainRuleException("error.msg.non.empty.datatable.cannot.be.deleted",
                    "Non-empty datatable cannot be deleted.");
        }
    }

    private int getRowCount(final String datatableName) {
        final String sql = "select count(*) from " + sqlGenerator.escape(datatableName);
        return this.jdbcTemplate.queryForObject(sql, Integer.class); // NOSONAR
    }

    @Transactional
    @Override
    public CommandProcessingResult updateDatatableEntryOneToOne(final String dataTableName, final Long appTableId,
            final JsonCommand command) {
        return updateDatatableEntry(dataTableName, appTableId, null, command);
    }

    @Transactional
    @Override
    public CommandProcessingResult updateDatatableEntryOneToMany(final String dataTableName, final Long appTableId, final Long datatableId,
            final JsonCommand command) {
        return updateDatatableEntry(dataTableName, appTableId, datatableId, command);
    }

    private CommandProcessingResult updateDatatableEntry(final String dataTableName, final Long appTableId, final Long datatableId,
            final JsonCommand command) {
        final EntityTables entityTable = queryForApplicationEntity(dataTableName);
        final CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(entityTable, appTableId);

        final GenericResultsetData grs = retrieveDataTableGenericResultSetForUpdate(entityTable, dataTableName, appTableId, datatableId);

        if (grs.hasNoEntries()) {
            throw new DatatableNotFoundException(dataTableName, appTableId);
        }

        if (grs.hasMoreThanOneEntry()) {
            throw new PlatformDataIntegrityException("error.msg.attempting.multiple.update",
                    "Application table: " + dataTableName + " Foreign key id: " + appTableId);
        }

        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        final Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

        String pkName = TABLE_FIELD_ID; // 1:M datatable
        if (datatableId == null) {
            pkName = getFKField(entityTable);
        } // 1:1 datatable

        final Map<String, Object> changes = getAffectedAndChangedColumns(grs, dataParams, pkName);

        if (!changes.isEmpty()) {
            Long pkValue = appTableId;
            if (datatableId != null) {
                pkValue = datatableId;
            }
            final String sql = getUpdateSql(grs.getColumnHeaders(), dataTableName, pkName, pkValue, changes);
            log.debug("Update sql: {}", sql);
            if (StringUtils.isNotBlank(sql)) {
                this.jdbcTemplate.update(sql);
            } else {
                log.debug("No Changes");
            }
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()) //
                .withEntityId(datatableId != null ? command.subentityId() : command.entityId()) //
                .withOfficeId(commandProcessingResult.getOfficeId()) //
                .withGroupId(commandProcessingResult.getGroupId()) //
                .withClientId(commandProcessingResult.getClientId()) //
                .withSavingsId(commandProcessingResult.getSavingsId()) //
                .withLoanId(commandProcessingResult.getLoanId()) //
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteDatatableEntries(final String dataTableName, final Long appTableId) {
        validateDatatableName(dataTableName);
        if (isDatatableAttachedToEntityDatatableCheck(dataTableName)) {
            throw new DatatableEntryRequiredException(dataTableName, appTableId);
        }
        final EntityTables entityTable = queryForApplicationEntity(dataTableName);
        final CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(entityTable, appTableId);
        final String deleteOneToOneEntrySql = getDeleteEntriesSql(dataTableName, getFKField(entityTable), appTableId);

        final int rowsDeleted = this.jdbcTemplate.update(deleteOneToOneEntrySql);
        if (rowsDeleted < 1) {
            throw new DatatableNotFoundException(dataTableName, appTableId);
        }

        return commandProcessingResult;
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteDatatableEntry(final String dataTableName, final Long appTableId, final Long datatableId) {
        validateDatatableName(dataTableName);
        if (isDatatableAttachedToEntityDatatableCheck(dataTableName)) {
            throw new DatatableEntryRequiredException(dataTableName, appTableId);
        }
        final EntityTables entityTable = queryForApplicationEntity(dataTableName);
        final CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(entityTable, appTableId);

        final String sql = getDeleteEntrySql(dataTableName, datatableId);

        this.jdbcTemplate.update(sql);
        return commandProcessingResult;
    }

    @Override
    @Transactional(readOnly = true)
    public GenericResultsetData retrieveDataTableGenericResultSet(final String dataTableName, final Long appTableId, final String order,
            final Long id) {
        final EntityTables entityTable = queryForApplicationEntity(dataTableName);
        checkMainResourceExistsWithinScope(entityTable, appTableId);

        final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);
        final boolean multiRow = isMultirowDatatable(columnHeaders);

        String whereClause = getFKField(entityTable) + " = " + appTableId;
        SQLInjectionValidator.validateSQLInput(whereClause);
        String sql = "select * from " + sqlGenerator.escape(dataTableName) + " where " + whereClause;

        // id only used for reading a specific entry that belongs to appTableId (in a
        // one to many datatable)
        if (multiRow && id != null) {
            sql = sql + " and " + TABLE_FIELD_ID + " = " + id;
        }

        if (StringUtils.isNotBlank(order)) {
            this.columnValidator.validateSqlInjection(sql, order);
            sql = sql + " order by " + order;
        }

        final List<ResultsetRowData> result = genericDataService.fillResultsetRowData(sql, columnHeaders);

        return new GenericResultsetData(columnHeaders, result);
    }

    private GenericResultsetData retrieveDataTableGenericResultSetForUpdate(final EntityTables entityTable, final String dataTableName,
            final Long appTableId, final Long id) {
        final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);

        final boolean multiRow = isMultirowDatatable(columnHeaders);

        String whereClause = getFKField(entityTable) + " = " + appTableId;
        SQLInjectionValidator.validateSQLInput(whereClause);
        String sql = "select * from " + sqlGenerator.escape(dataTableName) + " where " + whereClause;

        // id only used for reading a specific entry that belongs to appTableId (in a
        // one to many datatable)
        if (multiRow && id != null) {
            sql = sql + " and " + TABLE_FIELD_ID + " = " + id;
        }

        final List<ResultsetRowData> result = genericDataService.fillResultsetRowData(sql, columnHeaders);
        return new GenericResultsetData(columnHeaders, result);
    }

    private CommandProcessingResult checkMainResourceExistsWithinScope(@NotNull EntityTables entityTable, final Long appTableId) {
        final String sql = dataScopedSQL(entityTable, appTableId);
        log.debug("data scoped sql: {}", sql);
        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        if (!rs.next()) {
            throw new DatatableNotFoundException(entityTable, appTableId);
        }

        final Long officeId = getLongSqlRowSet(rs, "officeId");
        final Long groupId = getLongSqlRowSet(rs, "groupId");
        final Long clientId = getLongSqlRowSet(rs, "clientId");
        final Long savingsId = getLongSqlRowSet(rs, "savingsId");
        final Long LoanId = getLongSqlRowSet(rs, "loanId");
        final Long entityId = getLongSqlRowSet(rs, "entityId");

        if (rs.next()) {
            throw new DatatableSystemErrorException("System Error: More than one row returned from data scoping query");
        }

        return new CommandProcessingResultBuilder() //
                .withOfficeId(officeId) //
                .withGroupId(groupId) //
                .withClientId(clientId) //
                .withSavingsId(savingsId) //
                .withLoanId(LoanId).withEntityId(entityId)//
                .build();
    }

    private Long getLongSqlRowSet(final SqlRowSet rs, final String column) {
        Long val = rs.getLong(column);
        if (val == 0) {
            val = null;
        }
        return val;
    }

    private String dataScopedSQL(@NotNull EntityTables entityTable, final Long appTableId) {
        /*
         * unfortunately have to, one way or another, be able to restrict data to the users office hierarchy. Here, a
         * few key tables are done. But if additional fields are needed on other tables the same pattern applies
         */

        final AppUser currentUser = this.context.authenticatedUser();
        String officeHierarchy = currentUser.getOffice().getHierarchy();
        /*
         * m_loan and m_savings_account are connected to an m_office thru either an m_client or an m_group If both it
         * means it relates to an m_client that is in a group (still an m_client account)
         */
        return switch (entityTable) {
            case LOAN -> "select distinct x.* from ("
                    + " (select o.id as officeId, l.group_id as groupId, l.client_id as clientId, null as savingsId, l.id as loanId, null as entityId from m_loan l "
                    + getClientOfficeJoinCondition(officeHierarchy, "l") + " where l.id = " + appTableId + ")" + " union all "
                    + " (select o.id as officeId, l.group_id as groupId, l.client_id as clientId, null as savingsId, l.id as loanId, null as entityId from m_loan l "
                    + getGroupOfficeJoinCondition(officeHierarchy, "l") + " where l.id = " + appTableId + ")" + " ) as x";
            case SAVINGS -> "select distinct x.* from ("
                    + " (select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, null as entityId from m_savings_account s "
                    + getClientOfficeJoinCondition(officeHierarchy, "s") + " where s.id = " + appTableId + ")" + " union all "
                    + " (select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, null as entityId from m_savings_account s "
                    + getGroupOfficeJoinCondition(officeHierarchy, "s") + " where s.id = " + appTableId + ")" + " ) as x";
            case SAVINGS_TRANSACTION -> "select distinct x.* from ("
                    + " (select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, t.id as entityId from m_savings_account_transaction t"
                    + " join m_savings_account s on t.savings_account_id = s.id " + getClientOfficeJoinCondition(officeHierarchy, "s")
                    + " where t.id = " + appTableId + ")" + " union all "
                    + " (select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, t.id as entityId from m_savings_account_transaction t "
                    + " join m_savings_account s on t.savings_account_id = s.id " + getGroupOfficeJoinCondition(officeHierarchy, "s")
                    + " where t.id = " + appTableId + ")" + " ) as x";
            case CLIENT ->
                "select o.id as officeId, null as groupId, c.id as clientId, null as savingsId, null as loanId, null as entityId from m_client c "
                        + getOfficeJoinCondition(officeHierarchy, "c") + " where c.id = " + appTableId;
            case GROUP, CENTER ->
                "select o.id as officeId, g.id as groupId, null as clientId, null as savingsId, null as loanId, null as entityId from m_group g "
                        + getOfficeJoinCondition(officeHierarchy, "g") + " where g.id = " + appTableId;
            case OFFICE ->
                "select o.id as officeId, null as groupId, null as clientId, null as savingsId, null as loanId, null as entityId from m_office o "
                        + " where o.hierarchy like '" + officeHierarchy + "%'" + " and o.id = " + appTableId;
            case LOAN_PRODUCT, SAVINGS_PRODUCT, SHARE_PRODUCT ->
                "select null as officeId, null as groupId, null as clientId, null as savingsId, null as loanId, p.id as entityId from "
                        + entityTable.getName() + " as p WHERE p.id = " + appTableId;
            default -> throw new PlatformDataIntegrityException("error.msg.invalid.dataScopeCriteria",
                    "Application Table: " + entityTable.getName() + " not catered for in data Scoping");
        };
    }

    private String getClientOfficeJoinCondition(String officeHierarchy, String appTableAlias) {
        return " join m_client c on c.id = " + appTableAlias + ".client_id " + getOfficeJoinCondition(officeHierarchy, "c");
    }

    private String getGroupOfficeJoinCondition(String officeHierarchy, String appTableAlias) {
        return " join m_group g on g.id = " + appTableAlias + ".client_id " + getOfficeJoinCondition(officeHierarchy, "g");
    }

    private String getOfficeJoinCondition(String officeHierarchy, String joinTableAlias) {
        return " join m_office o on o.id = " + joinTableAlias + ".office_id and o.hierarchy like '" + officeHierarchy + "%' ";
    }

    @NotNull
    private EntityTables queryForApplicationEntity(final String datatable) {
        SQLInjectionValidator.validateSQLInput(datatable);
        final String sql = "SELECT application_table_name FROM x_registered_table where registered_table_name = ?";
        final SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, datatable); // NOSONAR

        String applicationTableName;
        if (rowSet.next()) {
            applicationTableName = rowSet.getString("application_table_name");
        } else {
            throw new DatatableNotFoundException(datatable);
        }
        return resolveEntity(applicationTableName);
    }

    private String getFKField(EntityTables entityTable) {
        return entityTable.getForeignKeyColumnNameOnDatatable();
    }

    private String getAddSql(final List<ResultsetColumnHeaderData> columnHeaders, final String datatable, final String fkName,
            final Long appTableId, final Map<String, String> queryParams) {

        final Map<String, Object> affectedColumns = getAffectedColumns(columnHeaders, queryParams, fkName);

        String pValueWrite = "";
        String addSql = "";
        final String singleQuote = "'";

        String insertColumns = "";
        String selectColumns = "";
        String columnName = "";
        String pValue = null;
        for (final ResultsetColumnHeaderData pColumnHeader : columnHeaders) {
            final String key = pColumnHeader.getColumnName();
            if (affectedColumns.containsKey(key)) {
                pValue = String.valueOf(affectedColumns.get(key));
                if (StringUtils.isEmpty(pValue) || "null".equalsIgnoreCase(pValue)) {
                    pValueWrite = "null";
                } else {
                    if (pColumnHeader.getColumnType() == BIT) {
                        if (databaseTypeResolver.isMySQL()) {
                            pValueWrite = BooleanUtils.toString(BooleanUtils.toBooleanObject(pValue), "1", "0", "null");
                        } else if (databaseTypeResolver.isPostgreSQL()) {
                            pValueWrite = BooleanUtils.toString(BooleanUtils.toBooleanObject(pValue), "B'1'", "B'0'", "null");
                        } else {
                            throw new IllegalStateException("Current database is not supported");
                        }

                    } else {
                        pValueWrite = singleQuote + this.genericDataService.replace(pValue, singleQuote, singleQuote + singleQuote)
                                + singleQuote;
                    }

                }
                columnName = sqlGenerator.escape(key);
                insertColumns += ", " + columnName;
                selectColumns += "," + pValueWrite + " as " + columnName;
            } else {
                if (key.equalsIgnoreCase(DataTableApiConstant.CREATEDAT_FIELD_NAME)
                        || key.equalsIgnoreCase(DataTableApiConstant.UPDATEDAT_FIELD_NAME)) {
                    columnName = sqlGenerator.escape(key);
                    insertColumns += ", " + columnName;
                    selectColumns += "," + sqlGenerator.currentTenantDateTime() + " as " + columnName;
                }
            }
        }

        addSql = "insert into " + sqlGenerator.escape(datatable) + " (" + sqlGenerator.escape(fkName) + " " + insertColumns + ")"
                + " select " + appTableId + " as id" + selectColumns;

        log.debug("{}", addSql);

        return addSql;
    }

    /**
     * This method is used special for ppi cases Where the score need to be computed
     *
     * @param columnHeaders
     * @param datatable
     * @param fkName
     * @param appTableId
     * @param queryParams
     * @return
     */
    public String getAddSqlWithScore(final List<ResultsetColumnHeaderData> columnHeaders, final String datatable, final String fkName,
            final Long appTableId, final Map<String, String> queryParams) {

        final Map<String, Object> affectedColumns = getAffectedColumns(columnHeaders, queryParams, fkName);

        String pValueWrite = "";
        String scoresId = " ";
        final String singleQuote = "'";

        String insertColumns = "";
        String selectColumns = "";
        String columnName = "";
        String pValue = null;
        for (final String key : affectedColumns.keySet()) {
            pValue = String.valueOf(affectedColumns.get(key));

            if (StringUtils.isEmpty(pValue) || "null".equalsIgnoreCase(pValue)) {
                pValueWrite = "null";
            } else {
                pValueWrite = singleQuote + this.genericDataService.replace(pValue, singleQuote, singleQuote + singleQuote) + singleQuote;

                scoresId += pValueWrite + " ,";

            }
            columnName = sqlGenerator.escape(key);
            insertColumns += ", " + columnName;
            selectColumns += "," + pValueWrite + " as " + columnName;
        }

        scoresId = scoresId.replaceAll(" ,$", "");

        String vaddSql = "insert into " + sqlGenerator.escape(datatable) + " (" + sqlGenerator.escape(fkName) + " " + insertColumns
                + ", score )" + " select " + appTableId + " as id" + selectColumns
                + " , ( SELECT SUM( code_score ) FROM m_code_value WHERE m_code_value.id IN (" + scoresId + " ) ) as score";

        log.debug("{}", vaddSql);

        return vaddSql;
    }

    private String getUpdateSql(List<ResultsetColumnHeaderData> columnHeaders, final String datatable, final String keyFieldName,
            final Long keyFieldValue, final Map<String, Object> changedColumns) {

        // just updating fields that have changed since pre-update read - though
        // its possible these values are different from the page the user was
        // looking at and even different from the current db values (if some
        // other update got in quick) - would need a version field for
        // completeness but its okay to take this risk with additional fields
        // data

        if (changedColumns.size() == 0) {
            return null;
        }

        String pValue = null;
        String pValueWrite = "";
        final String singleQuote = "'";
        boolean firstColumn = true;
        String sql = "update " + sqlGenerator.escape(datatable) + " ";
        for (final ResultsetColumnHeaderData pColumnHeader : columnHeaders) {
            final String key = pColumnHeader.getColumnName();
            if (changedColumns.containsKey(key)) {
                if (firstColumn) {
                    sql += " set ";
                    firstColumn = false;
                } else {
                    sql += ", ";
                }

                pValue = String.valueOf(changedColumns.get(key));
                if (StringUtils.isEmpty(pValue) || "null".equalsIgnoreCase(pValue)) {
                    pValueWrite = "null";
                } else {
                    if (pColumnHeader.getColumnType() == BIT) {
                        if (databaseTypeResolver.isMySQL()) {
                            pValueWrite = BooleanUtils.toString(BooleanUtils.toBooleanObject(pValue), "1", "0", "null");
                        } else if (databaseTypeResolver.isPostgreSQL()) {
                            pValueWrite = BooleanUtils.toString(BooleanUtils.toBooleanObject(pValue), "B'1'", "B'0'", "null");
                        } else {
                            throw new IllegalStateException("Current database is not supported");
                        }
                    } else {
                        pValueWrite = singleQuote + this.genericDataService.replace(pValue, singleQuote, singleQuote + singleQuote)
                                + singleQuote;
                    }
                }
                sql += sqlGenerator.escape(key) + " = " + pValueWrite;
            } else {
                if (key.equalsIgnoreCase(DataTableApiConstant.UPDATEDAT_FIELD_NAME)) {
                    if (firstColumn) {
                        sql += " set ";
                        firstColumn = false;
                    } else {
                        sql += ", ";
                    }
                    sql += sqlGenerator.escape(key) + " = " + sqlGenerator.currentTenantDateTime();
                }
            }
        }

        sql += " where " + keyFieldName + " = " + keyFieldValue;

        return sql;
    }

    private Map<String, Object> getAffectedAndChangedColumns(final GenericResultsetData grs, final Map<String, String> queryParams,
            final String fkName) {
        final Map<String, Object> affectedColumns = getAffectedColumns(grs.getColumnHeaders(), queryParams, fkName);
        final Map<String, Object> affectedAndChangedColumns = new HashMap<>();

        for (final String key : affectedColumns.keySet()) {
            final Object columnValue = affectedColumns.get(key);
            if (columnChanged(key, columnValue, grs)) {
                affectedAndChangedColumns.put(key, columnValue);
            }
        }

        return affectedAndChangedColumns;
    }

    private boolean columnChanged(final String key, final Object value, final GenericResultsetData grs) {
        final List<Object> columnValues = grs.getData().get(0).getRow();

        Object columnValue = null;
        for (int i = 0; i < grs.getColumnHeaders().size(); i++) {
            if (key.equals(grs.getColumnHeaders().get(i).getColumnName())) {
                columnValue = columnValues.get(i);
                return notTheSame(columnValue, value);
            }
        }

        throw new PlatformDataIntegrityException("error.msg.invalid.columnName", "Parameter Column Name: " + key + " not found");
    }

    public Map<String, Object> getAffectedColumns(final List<ResultsetColumnHeaderData> columnHeaders,
            final Map<String, String> queryParams, final String keyFieldName) {

        final String dateFormat = queryParams.get("dateFormat");
        Locale clientApplicationLocale = null;
        final String localeQueryParam = queryParams.get("locale");
        if (!StringUtils.isBlank(localeQueryParam)) {
            clientApplicationLocale = new Locale(queryParams.get("locale"));
        }

        final String underscore = "_";
        final String space = " ";
        String pValue = null;
        Object validatedValue = null;
        String queryParamColumnUnderscored;
        String columnHeaderUnderscored;
        boolean notFound;

        final Map<String, Object> affectedColumns = new HashMap<>();
        final Set<String> keys = queryParams.keySet();
        for (final String key : keys) {
            // ignores id and foreign key fields
            // also ignores locale and dateformat fields that are used for
            // validating numeric and date data
            if (!(key.equalsIgnoreCase(TABLE_FIELD_ID) || key.equalsIgnoreCase(keyFieldName) || key.equals("locale")
                    || key.equals("dateFormat") || key.equals(DataTableApiConstant.CREATEDAT_FIELD_NAME)
                    || key.equals(DataTableApiConstant.UPDATEDAT_FIELD_NAME))) {
                notFound = true;
                // matches incoming fields with and without underscores (spaces
                // and underscores considered the same)
                queryParamColumnUnderscored = this.genericDataService.replace(key, space, underscore);
                for (final ResultsetColumnHeaderData columnHeader : columnHeaders) {
                    if (notFound) {
                        columnHeaderUnderscored = this.genericDataService.replace(columnHeader.getColumnName(), space, underscore);
                        if (queryParamColumnUnderscored.equalsIgnoreCase(columnHeaderUnderscored)) {
                            pValue = queryParams.get(key);
                            validatedValue = SearchUtil.parseAndValidateColumnValue(columnHeader, pValue, dateFormat,
                                    clientApplicationLocale, true, sqlGenerator);
                            affectedColumns.put(columnHeader.getColumnName(), validatedValue);
                            notFound = false;
                        }
                    }

                }
                if (notFound) {
                    throw new PlatformDataIntegrityException("error.msg.column.not.found", "Column: " + key + " Not Found");
                }
            }
        }
        return affectedColumns;
    }

    private String getDeleteEntriesSql(final String datatable, final String FKField, final Long appTableId) {
        return "delete from " + sqlGenerator.escape(datatable) + " where " + sqlGenerator.escape(FKField) + " = " + appTableId;
    }

    private String getDeleteEntrySql(final String datatable, final Long datatableId) {
        return "delete from " + sqlGenerator.escape(datatable) + " where " + TABLE_FIELD_ID + " = " + datatableId;
    }

    private boolean isMultirowDatatable(final List<ResultsetColumnHeaderData> columnHeaders) {
        boolean multiRow = false;
        for (ResultsetColumnHeaderData column : columnHeaders) {
            if (column.isNamed(TABLE_FIELD_ID)) {
                multiRow = true;
                break;
            }
        }
        return multiRow;
    }

    private boolean notTheSame(final Object currValue, final Object pValue) {
        if (currValue == null && pValue == null) {
            return false;
        } else if (currValue == null || pValue == null) {
            return true;
        }
        // Equals would fail if the scale is not the same
        if (currValue instanceof BigDecimal && pValue instanceof BigDecimal) {
            return !MathUtil.isEqualTo((BigDecimal) currValue, (BigDecimal) pValue);
        }
        return !currValue.equals(pValue);
    }

    @Override
    public Long countDatatableEntries(final String datatableName, final Long appTableId, String foreignKeyColumn) {

        final String sqlString = "SELECT COUNT(" + sqlGenerator.escape(foreignKeyColumn) + ") FROM " + sqlGenerator.escape(datatableName)
                + " WHERE " + sqlGenerator.escape(foreignKeyColumn) + "=" + appTableId;
        final Long count = this.jdbcTemplate.queryForObject(sqlString, Long.class); // NOSONAR
        return count;
    }

    public boolean isDatatableAttachedToEntityDatatableCheck(final String datatableName) {
        StringBuilder builder = new StringBuilder();
        builder.append(" SELECT COUNT(edc.x_registered_table_name) FROM x_registered_table xrt");
        builder.append(" JOIN m_entity_datatable_check edc ON edc.x_registered_table_name = xrt.registered_table_name");
        builder.append(" WHERE edc.x_registered_table_name = '" + datatableName + "'");
        final Long count = this.jdbcTemplate.queryForObject(builder.toString(), Long.class);
        return count > 0;
    }

    // --- DbUtils ---

    @NotNull
    private String mapApiTypeToDbType(@NotNull String apiType, Integer length) {
        if (StringUtils.isEmpty(apiType)) {
            return "";
        }
        JdbcJavaType jdbcType = DatatableCommandFromApiJsonDeserializer.mapApiTypeToJdbcType(apiType);
        DatabaseType dialect = databaseTypeResolver.databaseType();
        if (jdbcType.isDecimalType()) {
            return jdbcType.formatSql(dialect, 19, 6); // TODO: parameter length is not used
        } else if (apiType.equalsIgnoreCase(API_FIELD_TYPE_DROPDOWN)) {
            return jdbcType.formatSql(dialect, 11); // TODO: parameter length is not used
        }
        return jdbcType.formatSql(dialect, length);
    }

    // --- Validation ---

    private EntityTables resolveEntity(final String entityName) {
        EntityTables entityTable = EntityTables.fromEntityName(entityName);
        if (entityTable == null) {
            throw new PlatformDataIntegrityException("error.msg.invalid.application.table", "Invalid Datatable entity: " + entityName,
                    API_FIELD_NAME, entityName);
        }
        return entityTable;
    }

    private void validateDatatableName(final String name) {
        if (name == null || name.isEmpty()) {
            throw new PlatformDataIntegrityException("error.msg.datatables.datatable.null.name", "Data table name must not be blank.");
        } else if (!name.matches(DATATABLE_NAME_REGEX_PATTERN)) {
            throw new PlatformDataIntegrityException("error.msg.datatables.datatable.invalid.name.regex", "Invalid data table name.", name);
        }
        SQLInjectionValidator.validateSQLInput(name);
    }

    private String validateDatatableRegistered(String datatable) {
        validateDatatableName(datatable);
        if (!isRegisteredDataTable(datatable)) {
            throw new DatatableNotFoundException(datatable);
        }
        return datatable;
    }

    private boolean isRegisteredDataTable(final String datatable) {
        final String sql = "SELECT COUNT(application_table_name) FROM " + TABLE_REGISTERED_TABLE + " WHERE registered_table_name = ?";
        final int count = jdbcTemplate.queryForObject(sql, Integer.class, datatable);
        return count > 0;
    }

    private void validateDataTableExists(final String datatableName) {
        final String sql = "select (CASE WHEN exists (select 1 from information_schema.tables where table_schema = "
                + sqlGenerator.currentSchema() + " and table_name = ?) THEN 'true' ELSE 'false' END)";
        final boolean dataTableExists = Boolean.parseBoolean(this.jdbcTemplate.queryForObject(sql, String.class, datatableName));
        if (!dataTableExists) {
            throw new PlatformDataIntegrityException("error.msg.invalid.datatable", "Invalid Data Table: " + datatableName, API_FIELD_NAME,
                    datatableName);
        }
    }
}
