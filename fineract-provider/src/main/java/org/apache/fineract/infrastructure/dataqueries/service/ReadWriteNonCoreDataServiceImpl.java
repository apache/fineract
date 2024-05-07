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
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.BIGINT;
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.DATETIME;
import static org.apache.fineract.infrastructure.core.service.database.SqlOperator.EQ;
import static org.apache.fineract.infrastructure.core.service.database.SqlOperator.IN;
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
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.CREATEDAT_FIELD_NAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.TABLE_FIELD_ID;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.TABLE_REGISTERED_TABLE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.UPDATEDAT_FIELD_NAME;
import static org.apache.fineract.portfolio.search.SearchConstants.API_PARAM_DATETIME_FORMAT;
import static org.apache.fineract.portfolio.search.SearchConstants.API_PARAM_DATE_FORMAT;
import static org.apache.fineract.portfolio.search.SearchConstants.API_PARAM_LOCALE;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.PersistenceException;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.PagedLocalRequest;
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
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.search.data.AdvancedQueryData;
import org.apache.fineract.portfolio.search.data.ColumnFilterData;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
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
    private final SqlValidator sqlValidator;
    private final SearchUtil searchUtil;

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
        sqlValidator.validate(datatable);
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
        Map<String, ResultsetColumnHeaderData> headersByName = searchUtil
                .mapHeadersToName(genericDataService.fillResultsetColumnHeaders(datatable));

        List<String> resultColumns = asList(resultColumnsString.split(","));
        List<String> selectColumns = searchUtil.validateToJdbcColumnNames(resultColumns, headersByName, false);
        ResultsetColumnHeaderData column = searchUtil.validateToJdbcColumn(columnName, headersByName, false);

        Object columnValue = searchUtil.parseJdbcColumnValue(column, columnValueString, null, null, null, false, sqlGenerator);
        String sql = sqlGenerator.buildSelect(selectColumns, null, false) + " " + sqlGenerator.buildFrom(datatable, null, false) + " WHERE "
                + EQ.formatPlaceholder(sqlGenerator, column.getColumnName(), 1, null);
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, columnValue); // NOSONAR

        List<JsonObject> results = new ArrayList<>();
        while (rowSet.next()) {
            searchUtil.extractJsonResult(rowSet, selectColumns, resultColumns, results);
        }
        return results;
    }

    @Override
    public Page<JsonObject> queryDataTableAdvanced(@NotNull String datatable, @NotNull PagedLocalRequest<AdvancedQueryData> pagedRequest) {
        datatable = validateDatatableRegistered(datatable);
        context.authenticatedUser().validateHasDatatableReadPermission(datatable);

        AdvancedQueryData request = pagedRequest.getRequest().orElseThrow();
        dataTableValidator.validateTableSearch(request);

        Map<String, ResultsetColumnHeaderData> headersByName = searchUtil
                .mapHeadersToName(genericDataService.fillResultsetColumnHeaders(datatable));
        String pkColumn = searchUtil.getFiltered(headersByName.values(), ResultsetColumnHeaderData::getIsColumnPrimaryKey).getColumnName();

        List<ColumnFilterData> columnFilters = request.getNonNullFilters();
        columnFilters.forEach(e -> e.setColumn(searchUtil.validateToJdbcColumnName(e.getColumn(), headersByName, false)));

        List<String> resultColumns = request.getNonNullResultColumns();
        List<String> selectColumns;
        if (resultColumns.isEmpty()) {
            resultColumns.add(pkColumn);
            selectColumns = new ArrayList<>();
            selectColumns.add(pkColumn);
        } else {
            selectColumns = searchUtil.validateToJdbcColumnNames(resultColumns, headersByName, false);
        }
        PageRequest pageable = pagedRequest.toPageable();
        PageRequest sortPageable;
        if (pageable.getSort().isSorted()) {
            List<Sort.Order> orders = pageable.getSort().toList();
            sortPageable = pageable.withSort(Sort.by(orders.stream()
                    .map(e -> e.withProperty(searchUtil.validateToJdbcColumnName(e.getProperty(), headersByName, false))).toList()));
        } else {
            pageable = pageable.withSort(Sort.Direction.DESC, pkColumn);
            sortPageable = pageable;
        }

        String dateFormat = pagedRequest.getDateFormat();
        String dateTimeFormat = pagedRequest.getDateTimeFormat();
        Locale locale = pagedRequest.getLocaleObject();

        String select = sqlGenerator.buildSelect(selectColumns, null, false);
        String from = " " + sqlGenerator.buildFrom(datatable, null, false);
        StringBuilder where = new StringBuilder();
        ArrayList<Object> params = new ArrayList<>();
        searchUtil.buildQueryCondition(columnFilters, where, params, null, headersByName, dateFormat, dateTimeFormat, locale, false,
                sqlGenerator);

        List<JsonObject> results = new ArrayList<>();
        Object[] args = params.toArray();

        // Execute the count Query
        String countQuery = "SELECT COUNT(*)" + from + where;
        Integer totalElements = jdbcTemplate.queryForObject(countQuery, Integer.class, args); // NOSONAR
        if (totalElements == null || totalElements == 0) {
            return PageableExecutionUtils.getPage(results, pageable, () -> 0);
        }

        StringBuilder query = new StringBuilder().append(select).append(from).append(where);
        query.append(" ").append(sqlGenerator.buildOrderBy(sortPageable.getSort().toList(), null, false));
        if (pageable.isPaged()) {
            query.append(" ").append(sqlGenerator.limit(pageable.getPageSize(), (int) pageable.getOffset()));
        }

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(query.toString(), args);

        while (rowSet.next()) {
            searchUtil.extractJsonResult(rowSet, selectColumns, resultColumns, results);
        }
        return PageableExecutionUtils.getPage(results, pageable, () -> totalElements);
    }

    @Override
    public boolean buildDataQueryEmbedded(@NotNull EntityTables entityTable, @NotNull String datatable, @NotNull AdvancedQueryData request,
            @NotNull List<String> selectColumns, @NotNull StringBuilder select, @NotNull StringBuilder from, @NotNull StringBuilder where,
            @NotNull List<Object> params, String mainAlias, String alias, String dateFormat, String dateTimeFormat, Locale locale) {
        List<String> resultColumns = request.getResultColumns();
        List<ColumnFilterData> columnFilters = request.getColumnFilters();
        if ((resultColumns == null || resultColumns.isEmpty()) && (columnFilters == null || columnFilters.isEmpty())) {
            return false;
        }

        datatable = validateDatatableRegistered(datatable);
        context.authenticatedUser().validateHasDatatableReadPermission(datatable);

        Map<String, ResultsetColumnHeaderData> headersByName = searchUtil
                .mapHeadersToName(genericDataService.fillResultsetColumnHeaders(datatable));

        List<String> thisSelectColumns = searchUtil.validateToJdbcColumnNames(resultColumns, headersByName, true);
        if (columnFilters != null) {
            columnFilters.forEach(e -> e.setColumn(searchUtil.validateToJdbcColumnName(e.getColumn(), headersByName, false)));
        }

        select.append(sqlGenerator.buildSelect(thisSelectColumns, alias, true));
        selectColumns.addAll(thisSelectColumns);

        String joinType = "LEFT";
        if (searchUtil.buildQueryCondition(columnFilters, where, params, alias, headersByName, dateFormat, dateTimeFormat, locale, true,
                sqlGenerator)) {
            joinType = null; // INNER
        }
        from.append(sqlGenerator.buildJoin(datatable, alias, entityTable.getForeignKeyColumnNameOnDatatable(), mainAlias,
                entityTable.getRefColumn(), joinType));
        return true;
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
        resolveEntity(entityName);
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
            if (category.equals(DataTableApiConstant.CATEGORY_PPI)) {
                this.namedParameterJdbcTemplate
                        .update("insert into c_configuration (name, value, enabled ) values( :dataTableName, '0', false)", paramMap);
            }

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dataTableName, null, dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            handleDataIntegrityIssues(dataTableName, null, ExceptionUtils.getRootCause(dve.getCause()), dve);

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
        return category == null ? DataTableApiConstant.CATEGORY_DEFAULT : category;
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
        validateDatatableRegistered(datatable);
        final String permissionList = "('CREATE_" + datatable + "', 'CREATE_" + datatable + "_CHECKER', 'READ_" + datatable + "', 'UPDATE_"
                + datatable + "', 'UPDATE_" + datatable + "_CHECKER', 'DELETE_" + datatable + "', 'DELETE_" + datatable + "_CHECKER')";

        final String deleteRolePermissionsSql = "delete from m_role_permission where m_role_permission.permission_id in (select id from m_permission where code in "
                + permissionList + ")";

        final String deletePermissionsSql = "delete from m_permission where code in " + permissionList;
        final String deleteRegisteredDatatableSql = "delete from x_registered_table where registered_table_name = '" + datatable + "'";
        final String deleteFromConfigurationSql = "delete from c_configuration where name ='" + datatable + "'";

        String[] sqlArray = new String[4];
        sqlArray[0] = deleteRolePermissionsSql;
        sqlArray[1] = deletePermissionsSql;
        sqlArray[2] = deleteRegisteredDatatableSql;
        sqlArray[3] = deleteFromConfigurationSql;

        this.jdbcTemplate.batchUpdate(sqlArray); // NOSONAR
    }

    private void parseDatatableColumnObjectForCreate(final JsonObject column, StringBuilder sqlBuilder,
            final StringBuilder constrainBuilder, final String dataTableNameAlias, final Map<String, Long> codeMappings,
            final boolean isConstraintApproach) {
        String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        final String type = column.has(API_FIELD_TYPE) ? column.get(API_FIELD_TYPE).getAsString().toLowerCase() : null;
        final Integer length = column.has(API_FIELD_LENGTH) ? column.get(API_FIELD_LENGTH).getAsInt() : null;
        final boolean mandatory = column.has(API_FIELD_MANDATORY) && column.get(API_FIELD_MANDATORY).getAsBoolean();
        final boolean unique = column.has(API_FIELD_UNIQUE) && column.get(API_FIELD_UNIQUE).getAsBoolean();
        final String code = column.has(API_FIELD_CODE) ? column.get(API_FIELD_CODE).getAsString() : null;

        if (StringUtils.isNotBlank(code)) {
            if (isConstraintApproach) {
                codeMappings.put(dataTableNameAlias + "_" + name, this.codeReadPlatformService.retriveCode(code).getId());
                String fkName = "fk_" + dataTableNameAlias + "_" + name;
                constrainBuilder.append(", CONSTRAINT ").append(sqlGenerator.escape(fkName)).append(" ").append("FOREIGN KEY (")
                        .append(sqlGenerator.escape(name)).append(") ").append("REFERENCES ").append(sqlGenerator.escape(CODE_VALUES_TABLE))
                        .append(" (id)");
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
            sqlBuilder.append("CREATE TABLE ").append(sqlGenerator.escape(datatableName)).append(" (");

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
            sqlBuilder.append(sqlGenerator.escape(fkColumnName)).append(" BIGINT NOT NULL, ");

            // Add Created At and Updated At
            columns.add(addColumn(CREATEDAT_FIELD_NAME, DATETIME, false, null, false, false));
            columns.add(addColumn(UPDATEDAT_FIELD_NAME, DATETIME, false, null, false, false));

            final Map<String, Long> codeMappings = new HashMap<>();
            final StringBuilder constrainBuilder = new StringBuilder();
            for (final JsonElement column : columns) {
                parseDatatableColumnObjectForCreate(column.getAsJsonObject(), sqlBuilder, constrainBuilder, dataTableNameAlias,
                        codeMappings, isConstraintApproach);
            }

            // Remove trailing comma and space
            sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

            String fullFkName = "fk_" + fkName;
            if (multiRow) {
                sqlBuilder.append(", PRIMARY KEY (").append(TABLE_FIELD_ID).append(")");
                if (databaseTypeResolver.isMySQL()) {
                    sqlBuilder.append(", KEY ").append(sqlGenerator.escape("fk_" + fkColumnName)).append(" (")
                            .append(sqlGenerator.escape(fkColumnName)).append(")");
                }
                sqlBuilder.append(", CONSTRAINT ").append(sqlGenerator.escape(fullFkName)).append(" FOREIGN KEY (")
                        .append(sqlGenerator.escape(fkColumnName)).append(") ").append("REFERENCES ")
                        .append(sqlGenerator.escape(entityTable.getApptableName())).append(" (").append(TABLE_FIELD_ID).append(")");
            } else {
                sqlBuilder.append(", PRIMARY KEY (").append(sqlGenerator.escape(fkColumnName)).append(")").append(", CONSTRAINT ")
                        .append(sqlGenerator.escape(fullFkName)).append(" FOREIGN KEY (").append(sqlGenerator.escape(fkColumnName))
                        .append(") ").append("REFERENCES ").append(sqlGenerator.escape(entityTable.getApptableName())).append(" (")
                        .append(TABLE_FIELD_ID).append(")");
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
            }
            // in case of non-multirow, the primary key of the table is the FK and MySQL and PostgreSQL
            // automatically puts an index onto it so no need to create it explicitly

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
            baseDataValidator.throwValidationErrors();
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withResourceIdAsString(datatableName).build();
    }

    private JsonElement addColumn(final String name, final JdbcJavaType dataType, final boolean isMandatory, final Integer length,
            final boolean isUnique, final boolean isIndexed) {
        JsonObject column = new JsonObject();
        column.addProperty(API_FIELD_NAME, name);
        column.addProperty(API_FIELD_TYPE, dataType.formatSql(databaseTypeResolver.databaseType()));
        if (dataType.isStringType()) {
            column.addProperty(API_FIELD_LENGTH, length);
        }
        column.addProperty(API_FIELD_MANDATORY, Boolean.toString(isMandatory));
        column.addProperty(API_FIELD_UNIQUE, Boolean.toString(isUnique));
        column.addProperty(API_FIELD_INDEXED, Boolean.toString(isIndexed));
        return column;
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
        final boolean unique = column.has(API_FIELD_UNIQUE) && column.get(API_FIELD_UNIQUE).getAsBoolean();
        final boolean indexed = column.has(API_FIELD_INDEXED) && column.get(API_FIELD_INDEXED).getAsBoolean();
        if (!unique && indexed) {
            String indexName = datatableKeywordGenerator.generateIndexName(datatableName, name);
            createIndex(indexName, datatableName, name);
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
            int rowCount = getDatatableRowCount(datatableName);
            final List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService.fillResultsetColumnHeaders(datatableName);
            final Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition = searchUtil.mapHeadersToName(columnHeaderData);

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
                        sqlBuilder.append("ALTER TABLE ").append(sqlGenerator.escape(datatableName)).append(" DROP KEY ")
                                .append(sqlGenerator.escape(fullOldFk)).append(",").append("DROP FOREIGN KEY ")
                                .append(sqlGenerator.escape(fullOldConstraint)).append(",").append("CHANGE COLUMN ")
                                .append(sqlGenerator.escape(oldFKName)).append(" ").append(sqlGenerator.escape(newFKName))
                                .append(" BIGINT NOT NULL,").append("ADD KEY ").append(sqlGenerator.escape(fullNewFk)).append(" (")
                                .append(sqlGenerator.escape(newFKName)).append("),").append("ADD CONSTRAINT ")
                                .append(sqlGenerator.escape(fullNewConstraint)).append(" FOREIGN KEY (")
                                .append(sqlGenerator.escape(newFKName)).append(") ").append("REFERENCES ")
                                .append(sqlGenerator.escape(entityTable.getApptableName())).append(" (").append(TABLE_FIELD_ID).append(")");
                    } else {
                        sqlBuilder.append("ALTER TABLE ").append(sqlGenerator.escape(datatableName)).append(" DROP FOREIGN KEY ")
                                .append(sqlGenerator.escape(fullOldConstraint)).append(",").append("CHANGE COLUMN ")
                                .append(sqlGenerator.escape(oldFKName)).append(" ").append(sqlGenerator.escape(newFKName))
                                .append(" BIGINT NOT NULL,").append("ADD CONSTRAINT ").append(sqlGenerator.escape(fullNewConstraint))
                                .append(" FOREIGN KEY (").append(sqlGenerator.escape(newFKName)).append(") ").append("REFERENCES ")
                                .append(sqlGenerator.escape(entityTable.getApptableName())).append(" (").append(TABLE_FIELD_ID).append(")");
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
                    sqlBuilder.deleteCharAt(indexOfFirstComma);
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
                    sqlBuilder.deleteCharAt(indexOfFirstComma);
                }
                sqlBuilder.append(constrainBuilder);
                jdbcTemplate.execute(sqlBuilder.toString());
                createIndexesForTable(datatableName, addColumns);
                registerColumnCodeMapping(codeMappings);
            }
            if (changeColumns != null) {
                final StringBuilder renameBuilder = new StringBuilder();
                StringBuilder changeBuilder = new StringBuilder();
                final StringBuilder constrainBuilder = new StringBuilder();
                final Map<String, Long> codeMappings = new HashMap<>();
                final List<String> removeMappings = new ArrayList<>();
                for (final JsonElement column : changeColumns) {
                    // remove NULL values from column where mandatory is true
                    removeNullValuesFromStringColumn(datatableName, column.getAsJsonObject(), mapColumnNameDefinition);
                    parseDatatableColumnForUpdate(column.getAsJsonObject(), mapColumnNameDefinition, datatableName, renameBuilder,
                            changeBuilder, constrainBuilder, codeMappings, removeMappings, isConstraintApproach);
                }

                // Remove the first comma, right after ALTER TABLE datatable
                StringBuilder sqlBuilder = renameBuilder;
                if (!changeBuilder.isEmpty() || !constrainBuilder.isEmpty()) {
                    int idx = changeBuilder.indexOf(",");
                    if (idx > -1) {
                        changeBuilder.deleteCharAt(idx);
                    } else if ((idx = constrainBuilder.indexOf(",")) > -1) {
                        constrainBuilder.deleteCharAt(idx);
                    }
                    sqlBuilder.append("ALTER TABLE " + sqlGenerator.escape(datatableName)).append(changeBuilder).append(constrainBuilder);
                }

                try {
                    if (!sqlBuilder.isEmpty()) {
                        jdbcTemplate.execute(sqlBuilder.toString());
                    }
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
            baseDataValidator.throwValidationErrors();
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
            baseDataValidator.throwValidationErrors();
        }
    }

    private void parseDatatableColumnForAdd(final JsonObject column, StringBuilder sqlBuilder, final String dataTableNameAlias,
            final StringBuilder constrainBuilder, final Map<String, Long> codeMappings, final boolean isConstraintApproach) {

        String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        final String type = column.has(API_FIELD_TYPE) ? column.get(API_FIELD_TYPE).getAsString().toLowerCase() : null;
        final Integer length = column.has(API_FIELD_LENGTH) ? column.get(API_FIELD_LENGTH).getAsInt() : null;
        final boolean mandatory = column.has(API_FIELD_MANDATORY) && column.get(API_FIELD_MANDATORY).getAsBoolean();
        final boolean unique = column.has(API_FIELD_UNIQUE) && column.get(API_FIELD_UNIQUE).getAsBoolean();
        final String after = column.has(API_FIELD_AFTER) ? column.get(API_FIELD_AFTER).getAsString() : null;
        final String code = column.has(API_FIELD_CODE) ? column.get(API_FIELD_CODE).getAsString() : null;

        if (StringUtils.isNotBlank(code)) {
            if (isConstraintApproach) {
                String fkName = "fk_" + dataTableNameAlias + "_" + name;
                codeMappings.put(dataTableNameAlias + "_" + name, this.codeReadPlatformService.retriveCode(code).getId());
                constrainBuilder.append(",ADD CONSTRAINT  ").append(sqlGenerator.escape(fkName)).append(" ").append("FOREIGN KEY (")
                        .append(sqlGenerator.escape(name)).append(") ").append("REFERENCES ").append(sqlGenerator.escape(CODE_VALUES_TABLE))
                        .append(" (").append(TABLE_FIELD_ID).append(")");
            } else {
                name = datatableColumnNameToCodeValueName(name, code);
            }
        }
        sqlBuilder.append(", ADD ").append(sqlGenerator.escape(name)).append(" ").append(mapApiTypeToDbType(type, length));

        if (unique) {
            String uniqueKeyName = datatableKeywordGenerator.generateUniqueKeyName(dataTableNameAlias, name);
            constrainBuilder.append(",ADD CONSTRAINT  ").append(sqlGenerator.escape(uniqueKeyName)).append(" ").append("UNIQUE (")
                    .append(sqlGenerator.escape(name)).append(")");
        }

        if (mandatory) {
            sqlBuilder.append(" NOT NULL");
        } else {
            sqlBuilder.append(" DEFAULT NULL");
        }

        if (after != null) {
            sqlBuilder.append(" AFTER ").append(sqlGenerator.escape(after));
        }
    }

    private void parseDatatableColumnForUpdate(final JsonObject column,
            final Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition, final String datatableName, StringBuilder renameBuilder,
            StringBuilder changeBuilder, final StringBuilder constrainBuilder, final Map<String, Long> codeMappings,
            final List<String> removeMappings, final boolean isConstraintApproach) {
        String oldName = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        if (!mapColumnNameDefinition.containsKey(oldName)) {
            throw new PlatformDataIntegrityException("error.msg.datatable.column.missing.update.parse",
                    "Column " + oldName + " does not exist.", oldName);
        }
        final String lengthStr = column.has(API_FIELD_LENGTH) ? column.get(API_FIELD_LENGTH).getAsString() : null;
        Long length = StringUtils.isNotBlank(lengthStr) ? Long.parseLong(lengthStr) : null;
        String newName = column.has(API_FIELD_NEWNAME) ? column.get(API_FIELD_NEWNAME).getAsString() : null;
        final Boolean newMandatory = column.has(API_FIELD_MANDATORY) ? column.get(API_FIELD_MANDATORY).getAsBoolean() : null;
        final String after = column.has(API_FIELD_AFTER) ? column.get(API_FIELD_AFTER).getAsString() : null;
        final String code = column.has(API_FIELD_CODE) ? column.get(API_FIELD_CODE).getAsString() : null;
        final String newCode = column.has(API_FIELD_NEWCODE) ? column.get(API_FIELD_NEWCODE).getAsString() : null;
        final String dataTableNameAlias = datatableName.toLowerCase().replaceAll("\\s", "_");
        if (isConstraintApproach) {
            if (StringUtils.isBlank(newName)) {
                newName = oldName;
            }
            String fkName = "fk_" + dataTableNameAlias + "_" + oldName;
            String newFkName = "fk_" + dataTableNameAlias + "_" + newName;
            if (!StringUtils.equalsIgnoreCase(code, newCode) || !StringUtils.equalsIgnoreCase(oldName, newName)) {
                if (StringUtils.equalsIgnoreCase(code, newCode)) {
                    final int codeId = getCodeIdForColumn(dataTableNameAlias, oldName);
                    if (codeId > 0) {
                        removeMappings.add(dataTableNameAlias + "_" + oldName);
                        constrainBuilder.append(", DROP CONSTRAINT ").append(sqlGenerator.escape(fkName)).append(" ");
                        codeMappings.put(dataTableNameAlias + "_" + newName, (long) codeId);
                        constrainBuilder.append(", ADD CONSTRAINT ").append(sqlGenerator.escape(newFkName)).append(" ")
                                .append("FOREIGN KEY (").append(sqlGenerator.escape(newName)).append(") ").append("REFERENCES ")
                                .append(sqlGenerator.escape(CODE_VALUES_TABLE)).append(" (").append(TABLE_FIELD_ID).append(")");
                    }

                } else {
                    if (code != null) {
                        removeMappings.add(dataTableNameAlias + "_" + oldName);
                        if (newCode == null || !StringUtils.equalsIgnoreCase(oldName, newName)) {
                            constrainBuilder.append(", DROP CONSTRAINT ").append(sqlGenerator.escape(fkName)).append(" ");
                        }
                    }
                    if (newCode != null) {
                        codeMappings.put(dataTableNameAlias + "_" + newName, this.codeReadPlatformService.retriveCode(newCode).getId());
                        if (code == null || !StringUtils.equalsIgnoreCase(oldName, newName)) {
                            constrainBuilder.append(", ADD CONSTRAINT  ").append(sqlGenerator.escape(newFkName)).append(" ")
                                    .append("FOREIGN KEY (").append(sqlGenerator.escape(newName)).append(") ").append("REFERENCES ")
                                    .append(sqlGenerator.escape(CODE_VALUES_TABLE)).append(" (").append(TABLE_FIELD_ID).append(")");
                        }
                    }
                }
            }
        } else {
            if (StringUtils.isNotBlank(code)) {
                oldName = datatableColumnNameToCodeValueName(oldName, code);
                if (StringUtils.isNotBlank(newCode)) {
                    newName = datatableColumnNameToCodeValueName(newName, newCode);
                } else {
                    newName = datatableColumnNameToCodeValueName(newName, code);
                }
            }
        }
        DatabaseType dialect = databaseTypeResolver.databaseType();
        ResultsetColumnHeaderData columnHeader = mapColumnNameDefinition.get(oldName);
        final JdbcJavaType type = columnHeader.getColumnType();
        boolean nameChanged = !StringUtils.isBlank(newName) && !newName.equals(oldName);
        boolean lengthChanged = length != null && !length.equals(columnHeader.getColumnLength()) && type.hasPrecision(dialect);
        boolean nullityChanged = newMandatory != null && newMandatory != columnHeader.isMandatory();
        boolean afterChanged = after != null && databaseTypeResolver.isMySQL();
        if (nameChanged || lengthChanged || nullityChanged || afterChanged) {
            Integer precision = length == null ? null : length.intValue();
            Integer scale = null;
            if (type.isDecimalType()) {
                precision = 19;
                scale = 6;
            }
            String colName = StringUtils.isBlank(newName) ? oldName : newName;
            boolean mandatory = newMandatory == null ? columnHeader.isMandatory() : newMandatory;
            if (databaseTypeResolver.isMySQL()) {
                String modifySql = nameChanged ? ("CHANGE " + sqlGenerator.escape(oldName) + " " + sqlGenerator.escape(colName))
                        : (" MODIFY " + sqlGenerator.escape(colName));
                changeBuilder.append(", ").append(modifySql).append(" ").append(type.formatSql(dialect, precision, scale))
                        .append(mandatory ? " NOT NULL" : " DEFAULT NULL");
                if (after != null) {
                    changeBuilder.append(" AFTER ").append(sqlGenerator.escape(after));
                }
            } else {
                if (nameChanged) {
                    renameBuilder.append("ALTER TABLE ").append(sqlGenerator.escape(datatableName)).append(" RENAME COLUMN ")
                            .append(sqlGenerator.escape(oldName)).append(" TO ").append(sqlGenerator.escape(newName)).append("; ");
                }
                if (lengthChanged) {
                    changeBuilder.append(", ALTER ").append(sqlGenerator.escape(colName)).append(" type ")
                            .append(type.formatSql(dialect, precision, scale));
                }
                if (nullityChanged) {
                    changeBuilder.append(", ALTER ").append(sqlGenerator.escape(colName))
                            .append(mandatory ? " set not null" : " drop not null");
                }
            }
        }
    }

    private void parseDatatableColumnForDrop(final JsonObject column, StringBuilder sqlBuilder, final String datatableName,
            final StringBuilder constrainBuilder, final List<String> codeMappings) {
        final String datatableAlias = datatableName.toLowerCase().replaceAll("\\s", "_");
        final String name = column.has(API_FIELD_NAME) ? column.get(API_FIELD_NAME).getAsString() : null;
        if (name == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.missing.datatable.column.name",
                    "Datatable column name to drop is missing.");
        }
        sqlBuilder.append(", DROP COLUMN ").append(sqlGenerator.escape(name)).append(" ");

        String fkName = "fk_" + datatableAlias + "_" + name;
        String schemaSql = databaseTypeResolver.isMySQL() ? "i.TABLE_SCHEMA = SCHEMA()"
                : "i.table_catalog = current_catalog AND i.table_schema = current_schema";
        String findFKSql = "SELECT count(*) FROM information_schema.TABLE_CONSTRAINTS i" + " WHERE i.CONSTRAINT_TYPE = 'FOREIGN KEY' AND "
                + schemaSql + " AND i.TABLE_NAME = '" + datatableName + "' AND i.CONSTRAINT_NAME = '" + fkName + "' ";

        final Integer count = this.jdbcTemplate.queryForObject(findFKSql, Integer.class); // NOSONAR
        if (count != null && count > 0) {
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
            this.jdbcTemplate.update(sql); // NOSONAR
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

        final boolean isAlreadyUnique = genericDataService.isExplicitlyUnique(datatableName, name);
        boolean setUnique = column.has(API_FIELD_UNIQUE) ? column.get(API_FIELD_UNIQUE).getAsBoolean() : isAlreadyUnique;
        String uniqueKeyName = datatableKeywordGenerator.generateUniqueKeyName(datatableName, name);

        if (isAlreadyUnique) {
            if (!setUnique) {
                // drop existing constraint
                dropUniqueConstraint(datatableName, uniqueKeyName);
            } else {
                // if columnname changed
                checkColumnRenameAndModifyUniqueConstraint(datatableName, columnNewName, uniqueKeyName);
            }
        } else if (setUnique) {
            checkColumnRenameAndCreateUniqueConstraint(datatableName, name, columnNewName, uniqueKeyName);
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
        String sql = "ALTER TABLE " + sqlGenerator.escape(datatableName) + " ADD CONSTRAINT " + sqlGenerator.escape(uniqueKeyName)
                + " UNIQUE (" + sqlGenerator.escape(columnName) + ");";
        this.jdbcTemplate.execute(sql); // NOSONAR
    }

    private void dropUniqueConstraint(String datatableName, String uniqueKeyName) {
        String sql = "ALTER TABLE " + sqlGenerator.escape(datatableName) + " DROP CONSTRAINT " + sqlGenerator.escape(uniqueKeyName) + ";";
        this.jdbcTemplate.execute(sql); // NOSONAR
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
        final boolean isAlreadyUnique = genericDataService.isExplicitlyUnique(datatableName, name);
        final boolean setForUnique = column.has(API_FIELD_UNIQUE) ? column.get(API_FIELD_UNIQUE).getAsBoolean() : isAlreadyUnique;
        if (setForUnique) {
            return;
        }
        final boolean isAlreadyIndexed = genericDataService.isExplicitlyIndexed(datatableName, name);
        boolean setForIndexed = column.has(API_FIELD_INDEXED) ? column.get(API_FIELD_INDEXED).getAsBoolean() : isAlreadyIndexed;

        String indexName = datatableKeywordGenerator.generateIndexName(datatableName, name);
        if (isAlreadyIndexed) {
            if (!setForIndexed) {
                // drop index
                dropIndex(datatableName, indexName);
            } else { // if column name changed
                checkColumnRenameAndModifyIndex(datatableName, columnNewName, indexName);
            }
        } else if (setForIndexed) {
            checkColumnRenameAndCreateIndex(datatableName, name, columnNewName, indexName);
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
            validateDatatableName(datatableName);
            assertDataTableEmpty(datatableName);
            deregisterDatatable(datatableName);
            String[] sqlArray;
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
            baseDataValidator.throwValidationErrors();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult createNewDatatableEntry(final String dataTableName, final Long appTableId, final JsonCommand command) {
        return createNewDatatableEntry(dataTableName, appTableId, command.json(), false);
    }

    @Transactional
    @Override
    public CommandProcessingResult createNewDatatableEntry(final String dataTableName, final Long appTableId, final String json) {
        return createNewDatatableEntry(dataTableName, appTableId, json, false);
    }

    @Transactional
    @Override
    public CommandProcessingResult createPPIEntry(final String dataTableName, final Long appTableId, final JsonCommand command) {
        return createNewDatatableEntry(dataTableName, appTableId, command.json(), true);
    }

    private CommandProcessingResult createNewDatatableEntry(final String dataTableName, final Long appTableId, final String json,
            boolean addScore) {
        final EntityTables entityTable = queryForApplicationEntity(dataTableName);
        CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(entityTable, appTableId);

        List<ResultsetColumnHeaderData> columnHeaders = genericDataService.fillResultsetColumnHeaders(dataTableName);
        Map<String, ResultsetColumnHeaderData> headersByName = searchUtil.mapHeadersToName(columnHeaders);

        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        final Map<String, String> dataParams = fromJsonHelper.extractDataMap(typeOfMap, json);

        final String dateFormat = dataParams.get(API_PARAM_DATE_FORMAT);
        // fall back to dateFormat to keep backward compatibility
        final String dateTimeFormat = dataParams.getOrDefault(API_PARAM_DATETIME_FORMAT, dateFormat);
        final String localeString = dataParams.get(API_PARAM_LOCALE);
        Locale locale = localeString == null ? null : JsonParserHelper.localeFromString(localeString);

        ArrayList<String> insertColumns = new ArrayList<>(
                List.of(entityTable.getForeignKeyColumnNameOnDatatable(), CREATEDAT_FIELD_NAME, UPDATEDAT_FIELD_NAME));
        LocalDateTime auditDateTime = DateUtils.getAuditLocalDateTime();
        ArrayList<Object> params = new ArrayList<>(List.of(appTableId, auditDateTime, auditDateTime));
        for (Map.Entry<String, String> entry : dataParams.entrySet()) {
            if (isTechnicalParam(entry.getKey())) {
                continue;
            }
            ResultsetColumnHeaderData columnHeader = searchUtil.validateToJdbcColumn(entry.getKey(), headersByName, false);
            if (!isUserInsertable(entityTable, columnHeader)) {
                continue;
            }
            insertColumns.add(columnHeader.getColumnName());
            params.add(searchUtil.parseJdbcColumnValue(columnHeader, entry.getValue(), dateFormat, dateTimeFormat, locale, false,
                    sqlGenerator));
        }
        if (addScore) {
            List<Object> scoreIds = params.stream().filter(e -> e != null && !String.valueOf(e).isBlank()).toList();
            int scoreValue;
            if (scoreIds.isEmpty()) {
                scoreValue = 0;
            } else {
                StringBuilder scoreSql = new StringBuilder("SELECT SUM(code_score) FROM m_code_value WHERE m_code_value.");
                ArrayList<Object> scoreParams = new ArrayList<>();
                searchUtil.buildCondition("id", BIGINT, IN, scoreIds, scoreSql, scoreParams, null, sqlGenerator);
                Integer score = jdbcTemplate.queryForObject(scoreSql.toString(), Integer.class, scoreParams.toArray(Object[]::new));
                scoreValue = score == null ? 0 : score;
            }
            insertColumns.add("score");
            params.add(scoreValue);
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final String sql = sqlGenerator.buildInsert(dataTableName, insertColumns, headersByName);
        try {
            int updated = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                setParameters(params, ps);
                return ps;
            }, keyHolder);
            if (updated != 1) {
                throw new PlatformDataIntegrityException("error.msg.invalid.insert", "Expected one inserted row.");
            }

            Long resourceId = appTableId;
            if (isMultirowDatatable(columnHeaders)) {
                resourceId = sqlGenerator.fetchPK(keyHolder);
            }
            return CommandProcessingResult.fromCommandProcessingResult(commandProcessingResult, resourceId);
        } catch (final DataAccessException dve) {
            handleDataIntegrityIssues(dataTableName, appTableId, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException e) {
            handleDataIntegrityIssues(dataTableName, appTableId, ExceptionUtils.getRootCause(e.getCause()), e);
            return CommandProcessingResult.empty();
        }
    }

    private static void setParameters(ArrayList<Object> params, PreparedStatement ps) {
        AtomicInteger parameterIndex = new AtomicInteger(1);
        params.forEach(param -> {
            try {
                ps.setObject(parameterIndex.getAndIncrement(), param);
            } catch (SQLException e) {
                throw new IllegalArgumentException(e);
            }
        });
    }

    private static boolean isUserInsertable(@NotNull EntityTables entityTable, @NotNull ResultsetColumnHeaderData columnHeader) {
        String columnName = columnHeader.getColumnName();
        return !columnHeader.getIsColumnPrimaryKey() && !CREATEDAT_FIELD_NAME.equals(columnName) && !UPDATEDAT_FIELD_NAME.equals(columnName)
                && !entityTable.getForeignKeyColumnNameOnDatatable().equals(columnName);
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

    @SuppressWarnings({ "WhitespaceAround" })
    private CommandProcessingResult updateDatatableEntry(final String dataTableName, final Long appTableId, final Long datatableId,
            final JsonCommand command) {
        final EntityTables entityTable = queryForApplicationEntity(dataTableName);
        CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(entityTable, appTableId);

        final GenericResultsetData existingRows = retrieveDataTableGenericResultSet(entityTable, dataTableName, appTableId, null,
                datatableId);
        if (existingRows.hasNoEntries()) {
            throw new DatatableNotFoundException(dataTableName, appTableId);
        }
        if (existingRows.hasMoreThanOneEntry()) {
            throw new PlatformDataIntegrityException("error.msg.attempting.multiple.update",
                    "Application table: " + dataTableName + " Foreign key id: " + appTableId);
        }

        List<ResultsetColumnHeaderData> columnHeaders = existingRows.getColumnHeaders();
        if (isMultirowDatatable(columnHeaders) && datatableId == null) {
            throw new PlatformDataIntegrityException("error.msg.attempting.multiple.update",
                    "Application table: " + dataTableName + " Foreign key id: " + appTableId);
        }
        Map<String, ResultsetColumnHeaderData> headersByName = searchUtil.mapHeadersToName(columnHeaders);
        final List<Object> existingValues = existingRows.getData().get(0).getRow();
        HashMap<ResultsetColumnHeaderData, Object> valuesByHeader = columnHeaders.stream().collect(HashMap::new,
                (map, e) -> map.put(e, existingValues.get(map.size())), (map, map2) -> {});

        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        final Map<String, String> dataParams = fromJsonHelper.extractDataMap(typeOfMap, command.json());

        final String dateFormat = dataParams.get(API_PARAM_DATE_FORMAT);
        // fall back to dateFormat to keep backward compatibility
        final String dateTimeFormat = dataParams.getOrDefault(API_PARAM_DATETIME_FORMAT, dateFormat);
        final String localeString = dataParams.get(API_PARAM_LOCALE);
        Locale locale = localeString == null ? null : JsonParserHelper.localeFromString(localeString);

        DatabaseType dialect = sqlGenerator.getDialect();
        ArrayList<String> updateColumns = new ArrayList<>(List.of(UPDATEDAT_FIELD_NAME));
        ArrayList<Object> params = new ArrayList<>(List.of(DateUtils.getAuditLocalDateTime()));
        final HashMap<String, Object> changes = new HashMap<>();
        for (Map.Entry<String, String> entry : dataParams.entrySet()) {
            if (isTechnicalParam(entry.getKey())) {
                continue;
            }
            ResultsetColumnHeaderData columnHeader = searchUtil.validateToJdbcColumn(entry.getKey(), headersByName, false);
            if (!isUserUpdatable(entityTable, columnHeader)) {
                continue;
            }
            String columnName = columnHeader.getColumnName();
            Object existingValue = valuesByHeader.get(columnHeader);
            Object columnValue = searchUtil.parseColumnValue(columnHeader, entry.getValue(), dateFormat, dateTimeFormat, locale, false,
                    sqlGenerator);
            if ((columnHeader.getColumnType().isDecimalType() && MathUtil.isEqualTo((BigDecimal) existingValue, (BigDecimal) columnValue))
                    || (existingValue == null ? columnValue == null : existingValue.equals(columnValue))) {
                log.debug("Ignore change on update {}:{}", dataTableName, columnName);
                continue;
            }
            updateColumns.add(columnName);
            params.add(columnHeader.getColumnType().toJdbcValue(dialect, columnValue, false));
            changes.put(columnName, columnValue);
        }
        Long primaryKey = datatableId == null ? appTableId : datatableId;
        if (!updateColumns.isEmpty()) {
            ResultsetColumnHeaderData pkColumn = searchUtil.getFiltered(columnHeaders, ResultsetColumnHeaderData::getIsColumnPrimaryKey);
            params.add(primaryKey);
            final String sql = sqlGenerator.buildUpdate(dataTableName, updateColumns, headersByName) + " WHERE " + pkColumn.getColumnName()
                    + " = ?";
            int updated = jdbcTemplate.update(sql, params.toArray(Object[]::new)); // NOSONAR
            if (updated != 1) {
                throw new PlatformDataIntegrityException("error.msg.invalid.update", "Expected one updated row.");
            }
        } else {
            log.debug("No change on update {}", dataTableName);
        }
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()) //
                .withEntityId(primaryKey) //
                .withOfficeId(commandProcessingResult.getOfficeId()) //
                .withGroupId(commandProcessingResult.getGroupId()) //
                .withClientId(commandProcessingResult.getClientId()) //
                .withSavingsId(commandProcessingResult.getSavingsId()) //
                .withLoanId(commandProcessingResult.getLoanId()) //
                .withTransactionId(commandProcessingResult.getTransactionId()) //
                .with(changes).build();
    }

    private static boolean isUserUpdatable(@NotNull EntityTables entityTable, @NotNull ResultsetColumnHeaderData columnHeader) {
        return isUserInsertable(entityTable, columnHeader);
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteDatatableEntries(final String dataTableName, final Long appTableId, JsonCommand command) {
        return deleteDatatableEntries(dataTableName, appTableId, null, command);
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteDatatableEntry(final String dataTableName, final Long appTableId, final Long datatableId,
            JsonCommand command) {
        return deleteDatatableEntries(dataTableName, appTableId, datatableId, command);
    }

    private CommandProcessingResult deleteDatatableEntries(final String dataTableName, final Long appTableId, final Long datatableId,
            JsonCommand command) {
        validateDatatableName(dataTableName);
        if (isDatatableAttachedToEntityDatatableCheck(dataTableName)) {
            throw new DatatableEntryRequiredException(dataTableName, appTableId);
        }
        final EntityTables entityTable = queryForApplicationEntity(dataTableName);
        final CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(entityTable, appTableId);

        String whereColumn;
        Long whereValue;
        if (datatableId == null) {
            whereColumn = getFKField(entityTable);
            whereValue = appTableId;
        } else {
            whereColumn = TABLE_FIELD_ID;
            whereValue = datatableId;
        }
        String sql = "DELETE FROM " + sqlGenerator.escape(dataTableName) + " WHERE " + sqlGenerator.escape(whereColumn) + " = "
                + whereValue;

        this.jdbcTemplate.update(sql); // NOSONAR
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(whereValue) //
                .withOfficeId(commandProcessingResult.getOfficeId()) //
                .withGroupId(commandProcessingResult.getGroupId()) //
                .withClientId(commandProcessingResult.getClientId()) //
                .withSavingsId(commandProcessingResult.getSavingsId()) //
                .withLoanId(commandProcessingResult.getLoanId()) //
                .withTransactionId(commandProcessingResult.getTransactionId()) //
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GenericResultsetData retrieveDataTableGenericResultSet(final String dataTableName, final Long appTableId, final String order,
            final Long id) {
        final EntityTables entityTable = queryForApplicationEntity(dataTableName);
        checkMainResourceExistsWithinScope(entityTable, appTableId);
        return retrieveDataTableGenericResultSet(entityTable, dataTableName, appTableId, order, id);
    }

    private GenericResultsetData retrieveDataTableGenericResultSet(final EntityTables entityTable, final String dataTableName,
            final Long appTableId, final String order, final Long id) {
        final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);
        final boolean multiRow = isMultirowDatatable(columnHeaders);

        String whereClause = getFKField(entityTable) + " = " + appTableId;
        sqlValidator.validate(whereClause);
        String sql = "select * from " + sqlGenerator.escape(dataTableName) + " where " + whereClause;

        // id only used for reading a specific entry that belongs to appTableId (in a one to many datatable)
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

    private CommandProcessingResult checkMainResourceExistsWithinScope(@NotNull EntityTables entityTable, final Long appTableId) {
        final String sql = dataScopedSQL(entityTable, appTableId);
        log.debug("data scoped sql: {}", sql);
        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        if (!rs.next()) {
            throw new DatatableNotFoundException(entityTable, appTableId);
        }
        final Long officeId = (Long) rs.getObject("officeId");
        final Long groupId = (Long) rs.getObject("groupId");
        final Long clientId = (Long) rs.getObject("clientId");
        final Long savingsId = (Long) rs.getObject("savingsId");
        final Long loanId = (Long) rs.getObject("loanId");
        final Long transactionId = (Long) rs.getObject("transactionId");
        final Long entityId = (Long) rs.getObject("entityId");

        if (rs.next()) {
            throw new DatatableSystemErrorException("System Error: More than one row returned from data scoping query");
        }

        return new CommandProcessingResultBuilder() //
                .withOfficeId(officeId) //
                .withGroupId(groupId) //
                .withClientId(clientId) //
                .withSavingsId(savingsId) //
                .withLoanId(loanId).withTransactionId(transactionId == null ? null : String.valueOf(transactionId)).withEntityId(entityId)//
                .build();
    }

    private String dataScopedSQL(@NotNull EntityTables entityTable, final Long appTableId) {
        // unfortunately have to, one way or another, be able to restrict data to the users office hierarchy. Here, a
        // few key tables are done. But if additional fields are needed on other tables the same pattern applies
        final AppUser currentUser = this.context.authenticatedUser();
        String officeHierarchy = currentUser.getOffice().getHierarchy();
        // m_loan and m_savings_account are connected to an m_office through either an m_client or an m_group If both it
        // means it relates to an m_client that is in a group (still an m_client account)
        return switch (entityTable) {
            case LOAN -> "select distinct x.* from ( "
                    + "(select o.id as officeId, l.group_id as groupId, l.client_id as clientId, null as savingsId, l.id as loanId, null as transactionId, null as entityId from m_loan l "
                    + getClientOfficeJoinCondition(officeHierarchy, "l") + " where l.id = " + appTableId + ")" + " union all "
                    + "(select o.id as officeId, l.group_id as groupId, l.client_id as clientId, null as savingsId, l.id as loanId, null as transactionId, null as entityId from m_loan l "
                    + getGroupOfficeJoinCondition(officeHierarchy, "l") + " where l.id = " + appTableId + ")" + " ) as x";
            case SAVINGS -> "select distinct x.* from ( "
                    + "(select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, null as transactionId, null as entityId "
                    + "from m_savings_account s " + getClientOfficeJoinCondition(officeHierarchy, "s") + " where s.id = " + appTableId + ")"
                    + " union all "
                    + "(select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, null as transactionId, null as entityId "
                    + "from m_savings_account s " + getGroupOfficeJoinCondition(officeHierarchy, "s") + " where s.id = " + appTableId + ")"
                    + " ) as x";
            case SAVINGS_TRANSACTION -> "select distinct x.* from ( "
                    + "(select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, t.id as transactionId, null as entityId "
                    + "from m_savings_account_transaction t join m_savings_account s on t.savings_account_id = s.id "
                    + getClientOfficeJoinCondition(officeHierarchy, "s") + " where t.id = " + appTableId + ")" + " union all "
                    + "(select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, t.id as transactionId, null as entityId "
                    + "from m_savings_account_transaction t join m_savings_account s on t.savings_account_id = s.id "
                    + getGroupOfficeJoinCondition(officeHierarchy, "s") + " where t.id = " + appTableId + ")" + " ) as x";
            case CLIENT ->
                "select o.id as officeId, null as groupId, c.id as clientId, null as savingsId, null as loanId, null as transactionId, null as entityId from m_client c "
                        + getOfficeJoinCondition(officeHierarchy, "c") + " where c.id = " + appTableId;
            case GROUP, CENTER ->
                "select o.id as officeId, g.id as groupId, null as clientId, null as savingsId, null as loanId, null as transactionId, null as entityId from m_group g "
                        + getOfficeJoinCondition(officeHierarchy, "g") + " where g.id = " + appTableId;
            case OFFICE ->
                "select o.id as officeId, null as groupId, null as clientId, null as savingsId, null as loanId, null as transactionId, null as entityId from m_office o "
                        + "where o.hierarchy like '" + officeHierarchy + "%'" + " and o.id = " + appTableId;
            case LOAN_PRODUCT, SAVINGS_PRODUCT, SHARE_PRODUCT ->
                "select null as officeId, null as groupId, null as clientId, null as savingsId, null as loanId, null as transactionId, p.id as entityId from "
                        + entityTable.getName() + " as p WHERE p.id = " + appTableId;
            default -> throw new PlatformDataIntegrityException("error.msg.invalid.dataScopeCriteria",
                    "Application Table: " + entityTable.getName() + " not catered for in data Scoping");
        };
    }

    private String getClientOfficeJoinCondition(String officeHierarchy, String appTableAlias) {
        return " join m_client c on c.id = " + appTableAlias + ".client_id " + getOfficeJoinCondition(officeHierarchy, "c");
    }

    private String getGroupOfficeJoinCondition(String officeHierarchy, String appTableAlias) {
        return " join m_group g on g.id = " + appTableAlias + ".group_id " + getOfficeJoinCondition(officeHierarchy, "g");
    }

    private String getOfficeJoinCondition(String officeHierarchy, String joinTableAlias) {
        return " join m_office o on o.id = " + joinTableAlias + ".office_id and o.hierarchy like '" + officeHierarchy + "%' ";
    }

    @NotNull
    private EntityTables queryForApplicationEntity(final String datatable) {
        sqlValidator.validate(datatable);
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

    @Override
    public Long countDatatableEntries(final String datatableName, final Long appTableId, String foreignKeyColumn) {
        final String sqlString = "SELECT COUNT(" + sqlGenerator.escape(foreignKeyColumn) + ") FROM " + sqlGenerator.escape(datatableName)
                + " WHERE " + sqlGenerator.escape(foreignKeyColumn) + " = " + appTableId;
        return this.jdbcTemplate.queryForObject(sqlString, Long.class); // NOSONAR
    }

    // --- Validation ---

    public boolean isDatatableAttachedToEntityDatatableCheck(final String datatableName) {
        String sql = "SELECT COUNT(edc.x_registered_table_name) FROM x_registered_table xrt"
                + " JOIN m_entity_datatable_check edc ON edc.x_registered_table_name = xrt.registered_table_name"
                + " WHERE edc.x_registered_table_name = '" + datatableName + "'";
        final Long count = this.jdbcTemplate.queryForObject(sql, Long.class); // NOSONAR
        return count != null && count > 0;
    }

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
        sqlValidator.validate(name);
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
        final Integer count = jdbcTemplate.queryForObject(sql, Integer.class, datatable);
        return count != null && count > 0;
    }

    private void validateDataTableExists(final String datatableName) {
        final String sql = "select (CASE WHEN exists (select 1 from information_schema.tables where table_schema = "
                + sqlGenerator.currentSchema() + " and table_name = ?) THEN 'true' ELSE 'false' END)";
        final boolean dataTableExists = Boolean.parseBoolean(this.jdbcTemplate.queryForObject(sql, String.class, datatableName)); // NOSONAR
        if (!dataTableExists) {
            throw new PlatformDataIntegrityException("error.msg.invalid.datatable", "Invalid Data Table: " + datatableName, API_FIELD_NAME,
                    datatableName);
        }
    }

    private void assertDataTableEmpty(final String datatableName) {
        final int rowCount = getDatatableRowCount(datatableName);
        if (rowCount != 0) {
            throw new GeneralPlatformDomainRuleException("error.msg.non.empty.datatable.cannot.be.deleted",
                    "Non-empty datatable cannot be deleted.");
        }
    }

    // --- DbUtils ---

    @NotNull
    private String mapApiTypeToDbType(String apiType, Integer length) {
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

    private int getDatatableRowCount(final String datatableName) {
        final String sql = "select count(*) from " + sqlGenerator.escape(datatableName);
        Integer count = this.jdbcTemplate.queryForObject(sql, Integer.class); // NOSONAR
        return count == null ? 0 : count;
    }

    // --- Utils ---

    private String getFKField(EntityTables entityTable) {
        return entityTable.getForeignKeyColumnNameOnDatatable();
    }

    private static boolean isTechnicalParam(String param) {
        return API_PARAM_DATE_FORMAT.equals(param) || API_PARAM_DATETIME_FORMAT.equals(param) || API_PARAM_LOCALE.equals(param);
    }

    private boolean isMultirowDatatable(final List<ResultsetColumnHeaderData> columnHeaders) {
        return searchUtil.findFiltered(columnHeaders, e -> e.isNamed(TABLE_FIELD_ID)) != null;
    }

    private String datatableColumnNameToCodeValueName(final String columnName, final String code) {
        return code + "_cd_" + columnName;
    }

    private void handleDataIntegrityIssues(String dataTableName, Long appTableId, final Throwable realCause, final Exception e) {
        String msgCode = "error.msg.datatable";
        String msg = "Unknown data integrity issue with datatable `" + dataTableName + "`";
        String param = null;
        Object[] msgArgs;
        final Throwable cause = e.getCause();
        if ((realCause != null && realCause.getMessage().contains("Duplicate entry"))
                || (cause != null && cause.getMessage().contains("Duplicate entry"))) {
            msgCode += ".entry.duplicate";
            param = API_PARAM_DATATABLE_NAME;
            if (appTableId == null) {
                msg = "Datatable `" + dataTableName + "` is already registered against an application table.";
                msgArgs = new Object[] { dataTableName, e };
            } else {
                msg = "An entry already exists for datatable `" + dataTableName + "` and application table with identifier `" + appTableId
                        + "`.";
                msgArgs = new Object[] { dataTableName, appTableId, e };
            }
        } else if ((realCause != null && realCause.getMessage().contains("doesn't have a default value"))
                || (cause != null && cause.getMessage().contains("doesn't have a default value"))) {
            msgCode += ".no.value.provided.for.required.fields";
            msg = "No values provided for the datatable `" + dataTableName + "` and application table with identifier `" + appTableId
                    + "`.";
            param = API_PARAM_DATATABLE_NAME;
            msgArgs = new Object[] { dataTableName, appTableId, e };
        } else {
            msgCode += ".unknown.data.integrity.issue";
            msgArgs = new Object[] { dataTableName, e };
        }
        log.error("Error occured.", e);
        throw ErrorHandler.getMappable(e, msgCode, msg, param, msgArgs);
    }
}
