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

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.SqlOperator;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformServiceImpl;
import org.apache.fineract.portfolio.search.data.ColumnFilterData;
import org.apache.fineract.portfolio.search.data.TransactionSearchRequest;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SavingsAccountTransactionsSearchServiceImpl implements SavingsAccountTransactionSearchService {

    private final PlatformSecurityContext context;
    private final GenericDataService genericDataService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<SavingsAccountTransactionData> searchTransactions(@NotNull Long savingsId,
            @NotNull TransactionSearchRequest searchParameters) {
        context.authenticatedUser().validateHasReadPermission(SAVINGS_ACCOUNT_RESOURCE_NAME);

        String apptable = EntityTables.SAVINGS_TRANSACTION.getApptableName();
        Map<String, ResultsetColumnHeaderData> columnHeaders = SearchUtil
                .mapHeadersToName(genericDataService.fillResultsetColumnHeaders(apptable));

        PageRequest pageable = searchParameters.getPageable();
        PageRequest sortPageable;
        if (pageable.getSort().isSorted()) {
            List<ApiParameterError> errors = new ArrayList<>();
            List<Sort.Order> orders = pageable.getSort().toList();
            sortPageable = pageable.withSort(Sort.by(orders.stream()
                    .map(e -> e.withProperty(SearchUtil.validateToJdbcColumn(e.getProperty(), columnHeaders, errors, false))).toList()));
            if (!errors.isEmpty()) {
                throw new PlatformApiDataValidationException(errors);
            }
        } else {
            pageable = pageable.withSort(Sort.Direction.DESC, "transaction_date", "created_date", "id");
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
        StringBuilder where = new StringBuilder(" WHERE ");
        ArrayList<Object> params = new ArrayList<>();
        SearchUtil.buildQueryCondition(columnFilters, where, params, alias, columnHeaders, false, false, sqlGenerator);

        SavingsAccountReadPlatformServiceImpl.SavingsAccountTransactionsMapper tm = new SavingsAccountReadPlatformServiceImpl.SavingsAccountTransactionsMapper();
        Object[] args = params.toArray();

        String countQuery = "SELECT COUNT(*) " + tm.from() + where;
        Integer totalElements = jdbcTemplate.queryForObject(countQuery, Integer.class, args);
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
}
