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
package org.apache.fineract.portfolio.savings.domain.search;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsTransactionSearchResult;
import org.apache.fineract.portfolio.savings.domain.search.SavingsTransactionSearch.Filters;
import org.apache.fineract.portfolio.savings.domain.search.SavingsTransactionSearch.RangeFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SavingsTransactionsSearchRepositoryImpl implements SavingsTransactionsSearchRepository {

    private static final String AMOUNT_FIELD_NAME = "amount";
    private static final String ID_FIELD_NAME = "id";
    private static final String CREATED_DATE_FIELD_NAME = "createdDate";
    private static final String TRANSACTION_DATE_FIELD_NAME = "dateOf";
    private final EntityManager entityManager;

    @Override
    public Page<SavingsTransactionSearchResult> searchTransactions(SavingsTransactionSearchParameters searchParameters) {
        // Build base query with filters but without the selection
        BaseQueryParametersMapResult baseQueryParameterMapResult = buildBaseQueryWithFilters(searchParameters.getSavingsId(),
                searchParameters.getDepositAccountType(), searchParameters.getFilters());

        // Attach the selection
        String jpqlQuery = attachSelection(baseQueryParameterMapResult.getBaseQueryString());

        // Attach the ordering
        String queryWithOrdering = attachOrdering(jpqlQuery, searchParameters.getPageable().getSort());

        // Execute Query
        TypedQuery<SavingsTransactionSearchResult> queryToExecute = entityManager.createQuery(queryWithOrdering,
                SavingsTransactionSearchResult.class);
        setQueryParameters(queryToExecute, baseQueryParameterMapResult.getParametersMap());
        applyPagination(queryToExecute, searchParameters.getPageable());
        List<SavingsTransactionSearchResult> resultList = queryToExecute.getResultList();

        // Attach the count selection
        String countQuery = attachCountSelection(baseQueryParameterMapResult.getBaseQueryString());

        // Execute count query
        TypedQuery<Long> countQueryToExecute = entityManager.createQuery(countQuery, Long.class);
        setQueryParameters(countQueryToExecute, baseQueryParameterMapResult.getParametersMap());
        Long totalElements = countQueryToExecute.getSingleResult();

        return PageableExecutionUtils.getPage(resultList, searchParameters.getPageable(), () -> totalElements);
    }

    private <T> void setQueryParameters(TypedQuery<T> queryToExecute, Map<String, Object> parametersMap) {
        for (Map.Entry<String, Object> entry : parametersMap.entrySet()) {
            queryToExecute.setParameter(entry.getKey(), entry.getValue());
        }
    }

    private BaseQueryParametersMapResult buildBaseQueryWithFilters(Long savingsId, DepositAccountType depositAccountType, Filters filters) {
        String baseQuery = """
                        SELECT tr
                        FROM SavingsAccountTransaction tr
                        JOIN ApplicationCurrency currency ON (currency.code = tr.savingsAccount.currency.code)
                        LEFT JOIN AccountTransferTransaction fromtran ON (fromtran.fromSavingsTransaction = tr)
                        LEFT JOIN AccountTransferTransaction totran ON (totran.toSavingsTransaction = tr)
                        LEFT JOIN tr.notes nt ON (nt.savingsTransaction = tr)
                        WHERE tr.savingsAccount.id = :savingsId
                        AND tr.savingsAccount.depositType = :depositType
                """;
        StringBuilder baseQueryBuilder = new StringBuilder(baseQuery);

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("savingsId", savingsId);
        parameterMap.put("depositType", depositAccountType.getValue());

        setFilterConditions(baseQueryBuilder, parameterMap, filters);
        return new BaseQueryParametersMapResult(baseQueryBuilder.toString(), parameterMap);
    }

    private String attachSelection(String baseQuery) {
        return baseQuery.replace("SELECT tr",
                "SELECT NEW org.apache.fineract.portfolio.savings.data.SavingsTransactionSearchResult(tr.id,tr.typeOf, tr.dateOf, tr.amount, tr.releaseIdOfHoldAmountTransaction, tr.reasonForBlock,tr.createdDate, tr.appUser, nt.note, tr.runningBalance, tr.reversed,tr.reversalTransaction, tr.originalTxnId, tr.lienTransaction, tr.isManualTransaction,fromTran, toTran, tr.savingsAccount, tr.paymentDetail, currency) ");
    }

    private String attachOrdering(String jpqlQuery, Sort sort) {
        StringJoiner orderByClauseBuilder = new StringJoiner(", ", " ORDER BY ", "");

        if (Objects.nonNull(sort) && sort.isSorted()) {
            buildOrderByClause(sort.toList(), orderByClauseBuilder);
        } else {
            List<Order> defaultOrders = getDefaultOrders();
            buildOrderByClause(defaultOrders, orderByClauseBuilder);
        }
        return new StringBuilder(jpqlQuery).append(orderByClauseBuilder.toString()).toString();
    }

    private void buildOrderByClause(List<Order> orders, StringJoiner orderByClauseBuilder) {
        for (Order order : orders) {
            String property = "tr." + order.getProperty();
            String direction = order.getDirection().name();
            String orderByExpression = new StringBuilder(property).append(StringUtils.SPACE).append(direction).toString();
            orderByClauseBuilder.add(orderByExpression);
        }
    }

    private List<Order> getDefaultOrders() {
        return List.of(Order.desc(TRANSACTION_DATE_FIELD_NAME), Order.desc(CREATED_DATE_FIELD_NAME), Order.desc(ID_FIELD_NAME));
    }

    private void applyPagination(TypedQuery<?> query, Pageable pageable) {
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
    }

    private String attachCountSelection(String baseQuery) {
        return baseQuery.replace("SELECT tr", "SELECT COUNT(tr) ");
    }

    private void setFilterConditions(StringBuilder queryBuilder, Map<String, Object> parameterMap, Filters filters) {
        if (Objects.nonNull(filters)) {
            List<RangeFilter<LocalDate>> dateFilters = filters.getTransactionDate();
            List<RangeFilter<BigDecimal>> amountFilters = filters.getTransactionAmount();
            List<SavingsAccountTransactionType> transactionTypes = filters.getTransactionType();

            if (Objects.nonNull(dateFilters)) {
                processRangeFilters(queryBuilder, parameterMap, dateFilters, TRANSACTION_DATE_FIELD_NAME);
            }

            if (Objects.nonNull(amountFilters)) {
                processRangeFilters(queryBuilder, parameterMap, amountFilters, AMOUNT_FIELD_NAME);
            }

            if (CollectionUtils.isNotEmpty(transactionTypes)) {
                List<Integer> transactionTypeValues = transactionTypes.stream().map(SavingsAccountTransactionType::getValue)
                        .collect(Collectors.toList());
                queryBuilder.append(" AND tr.typeOf IN :transactionTypes ");
                parameterMap.put("transactionTypes", transactionTypeValues);
            }
        }
    }

    private <T> void processRangeFilters(StringBuilder queryBuilder, Map<String, Object> parameterMap, List<RangeFilter<T>> filters,
            String field) {
        filters.forEach(filter -> {
            String paramName = new StringBuilder(field).append(filter.getOperator()).toString();
            queryBuilder.append(" AND tr.").append(field).append(StringUtils.SPACE).append(filter.getOperator().getSymbol()).append(" :")
                    .append(paramName);
            parameterMap.put(paramName, filter.getValue());
        });
    }
}
