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
package org.apache.fineract.portfolio.savings.service.search;

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_DATE_DB_FIELD;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;

import com.google.gson.JsonObject;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.PagedLocalRequest;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.SqlOperator;
import org.apache.fineract.infrastructure.dataqueries.data.DataTableValidator;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformServiceImpl;
import org.apache.fineract.portfolio.search.data.AdvancedQueryData;
import org.apache.fineract.portfolio.search.data.AdvancedQueryRequest;
import org.apache.fineract.portfolio.search.data.ColumnFilterData;
import org.apache.fineract.portfolio.search.data.TableQueryData;
import org.apache.fineract.portfolio.search.data.TransactionSearchRequest;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SavingsAccountTransactionsSearchServiceImpl implements SavingsAccountTransactionSearchService {

    private final PlatformSecurityContext context;
    private final GenericDataService genericDataService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final ReadWriteNonCoreDataService datatableService;
    private final DataTableValidator dataTableValidator;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<SavingsAccountTransactionData> searchTransactions(@NotNull Long savingsId,
            @NotNull TransactionSearchRequest searchParameters) {
        context.authenticatedUser().validateHasReadPermission(SAVINGS_ACCOUNT_RESOURCE_NAME);

        String apptable = EntityTables.SAVINGS_TRANSACTION.getApptableName();
        Map<String, ResultsetColumnHeaderData> headersByName = SearchUtil
                .mapHeadersToName(genericDataService.fillResultsetColumnHeaders(apptable));

        PageRequest pageable = searchParameters.getPageable();
        PageRequest sortPageable;
        if (pageable.getSort().isSorted()) {
            List<Sort.Order> orders = pageable.getSort().toList();
            sortPageable = pageable.withSort(Sort.by(orders.stream()
                    .map(e -> e.withProperty(SearchUtil.validateToJdbcColumnName(e.getProperty(), headersByName, false))).toList()));
        } else {
            pageable = pageable.withSort(Sort.Direction.DESC, "transaction_date", CREATED_DATE_DB_FIELD, "id");
            sortPageable = pageable;
        }

        List<ColumnFilterData> columnFilters = new ArrayList<>();
        columnFilters.add(ColumnFilterData.eq("savings_account_id", savingsId.toString()));
        columnFilters.add(ColumnFilterData.eq("is_reversal", Boolean.FALSE.toString()));
        addFromToFilter("transaction_date", DateUtils.format(searchParameters.getFromDate()),
                DateUtils.format(searchParameters.getToDate()), columnFilters);
        addFromToFilter("submitted_on_date", DateUtils.format(searchParameters.getFromSubmittedDate()),
                DateUtils.format(searchParameters.getToSubmittedDate()), columnFilters);
        addFromToFilter("amount", MathUtil.formatToSql(searchParameters.getFromAmount()),
                MathUtil.formatToSql(searchParameters.getToAmount()), columnFilters);

        Page<SavingsAccountTransactionData> emptyResult = PageableExecutionUtils.getPage(new ArrayList<>(0), pageable, () -> 0);
        if (addTransactionTypesFilter(searchParameters, columnFilters) == null) {
            return emptyResult;
        }

        String alias = "tr";
        StringBuilder where = new StringBuilder();
        ArrayList<Object> params = new ArrayList<>();
        SearchUtil.buildQueryCondition(columnFilters, where, params, alias, headersByName, null, null, null, false, sqlGenerator);

        SavingsAccountReadPlatformServiceImpl.SavingsAccountTransactionsMapper tm = new SavingsAccountReadPlatformServiceImpl.SavingsAccountTransactionsMapper();
        Object[] args = params.toArray();

        String countQuery = "SELECT COUNT(*) " + tm.from() + where;
        Integer totalElements = jdbcTemplate.queryForObject(countQuery, Integer.class, args); // NOSONAR
        if (totalElements == null || totalElements == 0) {
            return emptyResult;
        }

        StringBuilder query = new StringBuilder().append("SELECT ").append(tm.schema()).append(where);
        query.append(" ").append(sqlGenerator.buildOrderBy(sortPageable.getSort().toList(), alias, false));
        if (pageable.isPaged()) {
            query.append(" ").append(sqlGenerator.limit(pageable.getPageSize(), (int) pageable.getOffset()));
        }

        List<SavingsAccountTransactionData> results = this.jdbcTemplate.query(query.toString(), tm, args);
        return PageableExecutionUtils.getPage(results, pageable, () -> totalElements);
    }

    private static void addFromToFilter(@NotNull String column, String fromValue, String toValue,
            @NotNull List<ColumnFilterData> columnFilters) {
        if (fromValue != null) {
            columnFilters.add(toValue == null ? ColumnFilterData.create(column, SqlOperator.GTE, fromValue)
                    : ColumnFilterData.btw(column, fromValue, toValue));
        } else if (toValue != null) {
            columnFilters.add(ColumnFilterData.create(column, SqlOperator.LTE, toValue));
        }
    }

    @Nullable
    private static Boolean addTransactionTypesFilter(@NotNull TransactionSearchRequest searchParameters,
            List<ColumnFilterData> columnFilters) {
        Predicate<SavingsAccountTransactionType> filter = null;
        Boolean credit = searchParameters.getCredit();
        Boolean debit = searchParameters.getDebit();

        if (credit != null) {
            Predicate<SavingsAccountTransactionType> cf = SavingsAccountTransactionType::isCreditEntryType;
            filter = credit ? cf : Predicate.not(cf);
        }
        if (debit != null) {
            Predicate<SavingsAccountTransactionType> df = SavingsAccountTransactionType::isDebitEntryType;
            if (!debit) {
                df = Predicate.not(df);
            }
            filter = credit == null ? df : (credit && debit ? filter.or(df) : filter.and(df));
        }
        if (searchParameters.getTypes() != null) {
            List<String> types = Arrays.asList(searchParameters.getTypes());
            Predicate<SavingsAccountTransactionType> tf = t -> types.contains(String.valueOf(t.getId()));
            filter = filter == null ? tf : filter.and(tf);
        }
        if (filter != null) {
            filter = filter.and(SavingsAccountTransactionType::isValid);
            List<SavingsAccountTransactionType> filteredTypes = SavingsAccountTransactionType.getFiltered(filter);
            if (filteredTypes.isEmpty()) {
                return null;
            } else {
                String[] values = filteredTypes.stream().map(t -> String.valueOf(t.getId())).toArray(String[]::new);
                columnFilters.add(ColumnFilterData.create("transaction_type_enum", SqlOperator.IN, values));
                return true;
            }
        }
        return false;
    }

    @Override
    public Page<JsonObject> queryAdvanced(@NotNull Long savingsId, @NotNull PagedLocalRequest<AdvancedQueryRequest> pagedRequest) {
        context.authenticatedUser().validateHasReadPermission(SAVINGS_ACCOUNT_RESOURCE_NAME);
        String apptable = EntityTables.SAVINGS_TRANSACTION.getApptableName();

        AdvancedQueryRequest queryRequest = pagedRequest.getRequest().orElseThrow();
        dataTableValidator.validateTableSearch(queryRequest);

        List<ResultsetColumnHeaderData> columnHeaders = genericDataService.fillResultsetColumnHeaders(apptable);
        Map<String, ResultsetColumnHeaderData> headersByName = SearchUtil.mapHeadersToName(columnHeaders);
        String pkColumn = SearchUtil.getFiltered(columnHeaders, ResultsetColumnHeaderData::getIsColumnPrimaryKey).getColumnName();

        AdvancedQueryData baseQuery = queryRequest.getBaseQuery();
        List<TableQueryData> datatableQueries = queryRequest.getDatatableQueries();

        List<ColumnFilterData> columnFilters;
        List<String> resultColumns;
        List<String> selectColumns;
        if (baseQuery == null) {
            columnFilters = new ArrayList<>();
            resultColumns = new ArrayList<>();
            selectColumns = new ArrayList<>();
        } else {
            columnFilters = baseQuery.getNonNullFilters();
            columnFilters.forEach(e -> e.setColumn(SearchUtil.validateToJdbcColumnName(e.getColumn(), headersByName, false)));
            resultColumns = baseQuery.getNonNullResultColumns();
            selectColumns = new ArrayList<>(SearchUtil.validateToJdbcColumnNames(resultColumns, headersByName, true));
        }
        columnFilters.add(0, ColumnFilterData.eq("savings_account_id", savingsId.toString()));
        if (resultColumns.isEmpty() && !queryRequest.hasResultColumn()) {
            resultColumns.add(pkColumn);
            selectColumns.add(pkColumn);
        }
        PageRequest pageable = pagedRequest.toPageable();
        PageRequest sortPageable;
        if (pageable.getSort().isSorted()) {
            List<Sort.Order> orders = pageable.getSort().toList();
            sortPageable = pageable.withSort(Sort.by(orders.stream()
                    .map(e -> e.withProperty(SearchUtil.validateToJdbcColumnName(e.getProperty(), headersByName, false))).toList()));
        } else {
            pageable = pageable.withSort(Sort.Direction.DESC, pkColumn);
            sortPageable = pageable;
        }

        String alias = "main";
        String dateFormat = pagedRequest.getDateFormat();
        String dateTimeFormat = pagedRequest.getDateTimeFormat();
        Locale locale = pagedRequest.getLocaleObject();
        StringBuilder select = new StringBuilder(sqlGenerator.buildSelect(selectColumns, alias, false));
        StringBuilder from = new StringBuilder(" ").append(sqlGenerator.buildFrom(apptable, alias, false));
        StringBuilder where = new StringBuilder();
        ArrayList<Object> params = new ArrayList<>();
        SearchUtil.buildQueryCondition(columnFilters, where, params, alias, headersByName, dateFormat, dateTimeFormat, locale, false,
                sqlGenerator);

        if (datatableQueries != null) {
            StringBuilder dataSelect = new StringBuilder();
            StringBuilder dataFrom = new StringBuilder();
            StringBuilder dataWhere = new StringBuilder();
            ArrayList<Object> dataParams = new ArrayList<>();
            for (int i = 0; i < datatableQueries.size(); i++) {
                TableQueryData tableQuery = datatableQueries.get(i);
                boolean added = datatableService.buildDataQueryEmbedded(EntityTables.SAVINGS_TRANSACTION, tableQuery.getTable(),
                        tableQuery.getQuery(), selectColumns, dataSelect, dataFrom, dataWhere, dataParams, alias, ("d" + i), dateFormat,
                        dateTimeFormat, locale);
                if (added) {
                    if (!dataSelect.isEmpty()) {
                        select.append(select.isEmpty() ? "SELECT " : ", ").append(dataSelect);
                    }
                    if (!dataFrom.isEmpty()) {
                        from.append(" ").append(dataFrom);
                    }
                    if (!dataWhere.isEmpty()) {
                        where.append(where.isEmpty() ? " WHERE " : " AND ").append(dataWhere);
                    }
                    params.addAll(dataParams);
                    dataSelect.setLength(0);
                    dataFrom.setLength(0);
                    dataWhere.setLength(0);
                    dataParams.clear();
                }
                resultColumns.addAll(tableQuery.getQuery().getNonNullResultColumns());
            }
        }

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
            SearchUtil.extractJsonResult(rowSet, selectColumns, resultColumns, results);
        }
        return PageableExecutionUtils.getPage(results, pageable, () -> totalElements);
    }
}
