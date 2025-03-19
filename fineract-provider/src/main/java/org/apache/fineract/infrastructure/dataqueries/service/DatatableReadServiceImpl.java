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
import static org.apache.fineract.infrastructure.core.service.database.SqlOperator.EQ;

import com.google.common.base.Splitter;
import com.google.gson.JsonObject;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.PagedLocalRequest;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.data.DataTableValidator;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.search.data.AdvancedQueryData;
import org.apache.fineract.portfolio.search.data.ColumnFilterData;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class DatatableReadServiceImpl implements DatatableReadService {

    private static final String APPLICATION_TABLE_NAME = "application_table_name";

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final PlatformSecurityContext context;
    private final GenericDataService genericDataService;
    private final DataTableValidator dataTableValidator;
    private final SqlValidator sqlValidator;
    private final SearchUtil searchUtil;
    private final DatatableUtil datatableUtil;

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
            final String appTableName = rowSet.getString(APPLICATION_TABLE_NAME);
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
            final String appTableName = rowSet.getString(APPLICATION_TABLE_NAME);
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
        datatable = datatableUtil.validateDatatableRegistered(datatable);
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
        datatable = datatableUtil.validateDatatableRegistered(datatable);
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
        datatable = datatableUtil.validateDatatableRegistered(datatable);
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

    @Override
    @Transactional(readOnly = true)
    public GenericResultsetData retrieveDataTableGenericResultSet(final String dataTableName, final Long appTableId, final String order,
            final Long id) {
        final EntityTables entityTable = datatableUtil.queryForApplicationEntity(dataTableName);
        datatableUtil.checkMainResourceExistsWithinScope(entityTable, appTableId);
        return datatableUtil.retrieveDataTableGenericResultSet(entityTable, dataTableName, appTableId, order, id);
    }

    @Override
    public Long countDatatableEntries(final String datatableName, final Long appTableId, String foreignKeyColumn) {
        final String sqlString = "SELECT COUNT(" + sqlGenerator.escape(foreignKeyColumn) + ") FROM " + sqlGenerator.escape(datatableName)
                + " WHERE " + sqlGenerator.escape(foreignKeyColumn) + " = " + appTableId;
        return this.jdbcTemplate.queryForObject(sqlString, Long.class); // NOSONAR
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

}
