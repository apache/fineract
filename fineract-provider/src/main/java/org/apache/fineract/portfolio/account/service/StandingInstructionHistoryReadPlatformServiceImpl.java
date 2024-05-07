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
package org.apache.fineract.portfolio.account.service;

import static com.querydsl.core.types.dsl.Expressions.ONE;
import static java.util.stream.Collectors.toList;
import static org.apache.fineract.portfolio.account.service.AccountTransferEnumerations.accountType;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.domain.QOffice;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionHistoryData;
import org.apache.fineract.portfolio.account.domain.QAccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.QAccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.QAccountTransferStandingInstructionsHistory;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.domain.QClient;
import org.apache.fineract.portfolio.loanaccount.domain.QLoan;
import org.apache.fineract.portfolio.loanproduct.domain.QLoanProduct;
import org.apache.fineract.portfolio.savings.domain.QSavingsAccount;
import org.apache.fineract.portfolio.savings.domain.QSavingsProduct;

@AllArgsConstructor
public class StandingInstructionHistoryReadPlatformServiceImpl implements StandingInstructionHistoryReadPlatformService {

    // pagination
    private final PaginationHelper paginationHelper;
    private final EntityManager entityManager;

    @Override
    public Page<StandingInstructionHistoryData> retrieveAll(StandingInstructionDTO standingInstructionDTO) {
        final QAccountTransferStandingInstructionsHistory qHistory = QAccountTransferStandingInstructionsHistory.accountTransferStandingInstructionsHistory;
        final QAccountTransferDetails qAccountTransferDetails = QAccountTransferDetails.accountTransferDetails;
        final QClient qFromClient = new QClient("fromClient");
        final QSavingsAccount qFromSavingsAccount = new QSavingsAccount("fromSavingsAccount");
        final QLoan qFromLoanAccount = new QLoan("fromLoanAccount");

        final JPAQuery<Tuple> query = getStandingInstructionHistorySelectQuery();
        final JPAQuery<Long> totalCountQuery = getStandingInstructionHistoryCountQuery();
        BooleanExpression whereClause = null;

        if (standingInstructionDTO.transferType() != null) {
            whereClause = addCondition(null, qAccountTransferDetails.transferType.eq(standingInstructionDTO.transferType()));
        }
        if (standingInstructionDTO.clientId() != null) {
            whereClause = addCondition(whereClause, qFromClient.id.eq(standingInstructionDTO.clientId()));
        } else if (standingInstructionDTO.clientName() != null) {
            whereClause = addCondition(whereClause, qFromClient.displayName.eq(standingInstructionDTO.clientName()));
        }

        if (standingInstructionDTO.fromAccountType() != null && standingInstructionDTO.fromAccount() != null) {
            PortfolioAccountType accountType = PortfolioAccountType.fromInt(standingInstructionDTO.fromAccountType());
            if (accountType.isSavingsAccount()) {
                whereClause = addCondition(whereClause, qFromSavingsAccount.id.eq(standingInstructionDTO.fromAccount()));
            } else if (accountType.isLoanAccount()) {
                whereClause = addCondition(whereClause, qFromLoanAccount.id.eq(standingInstructionDTO.fromAccount()));
            }
        }

        final ZoneId tenantZone = DateUtils.getDateTimeZoneOfTenant();

        if (standingInstructionDTO.startDateRange() != null) {
            LocalDateTime startDateTime = standingInstructionDTO.startDateRange().atStartOfDay(tenantZone).toLocalDateTime();
            whereClause = addCondition(whereClause, qHistory.executionTime.goe(startDateTime));
        }

        if (standingInstructionDTO.endDateRange() != null) {
            LocalDateTime endDateTime = standingInstructionDTO.endDateRange().atTime(LocalTime.MAX).atZone(tenantZone).toLocalDateTime();
            whereClause = addCondition(whereClause, qHistory.executionTime.loe(endDateTime));
        }

        query.where(whereClause);
        totalCountQuery.where(whereClause);

        final SearchParameters searchParameters = standingInstructionDTO.searchParameters();
        if (searchParameters != null) {
            if (searchParameters.hasOrderBy()) {
                final Order order = searchParameters.getSortOrder().equalsIgnoreCase("desc") ? Order.DESC
                        : searchParameters.getSortOrder().equalsIgnoreCase("asc") || searchParameters.getSortOrder().isEmpty() ? Order.ASC
                                : null;
                if (order == null) {
                    throw new IllegalArgumentException("Unknown sort order: " + searchParameters.getSortOrder());
                }

                final OrderSpecifier<?> specifier = OrderSpecifierFactory.getSpecifier(searchParameters.getOrderBy(), order);
                query.orderBy(specifier);
            }

            if (searchParameters.hasLimit()) {
                query.limit(searchParameters.getLimit());
                if (searchParameters.hasOffset()) {
                    query.offset(searchParameters.getOffset());
                }
            }
        }

        final List<Tuple> queryResult = query.fetch();
        return this.paginationHelper.createPageFromItems(
                queryResult.isEmpty() ? Collections.emptyList() : mapQueryResultToStandingInstructionHistoryDataList(queryResult),
                Objects.requireNonNull(totalCountQuery.fetchOne()));
    }

    private static final class OrderSpecifierFactory {

        @FunctionalInterface
        interface OrderSpecifierGenerator {

            OrderSpecifier<?> generate(Order order);
        }

        private static final Map<String, OrderSpecifierGenerator> fieldToSpecifier = new HashMap<>();

        static {
            fieldToSpecifier.put("id",
                    order -> new OrderSpecifier<>(order, QAccountTransferStandingInstruction.accountTransferStandingInstruction.id));
            fieldToSpecifier.put("name",
                    order -> new OrderSpecifier<>(order, QAccountTransferStandingInstruction.accountTransferStandingInstruction.name));
            fieldToSpecifier.put("status", order -> new OrderSpecifier<>(order,
                    QAccountTransferStandingInstructionsHistory.accountTransferStandingInstructionsHistory.status));
            fieldToSpecifier.put("executionTime", order -> new OrderSpecifier<>(order,
                    QAccountTransferStandingInstructionsHistory.accountTransferStandingInstructionsHistory.executionTime));
            fieldToSpecifier.put("amount", order -> new OrderSpecifier<>(order,
                    QAccountTransferStandingInstructionsHistory.accountTransferStandingInstructionsHistory.amount));
            fieldToSpecifier.put("errorLog", order -> new OrderSpecifier<>(order,
                    QAccountTransferStandingInstructionsHistory.accountTransferStandingInstructionsHistory.errorLog));
            fieldToSpecifier.put("fromOfficeId", order -> new OrderSpecifier<>(order, new QOffice("fromOffice").id));
            fieldToSpecifier.put("fromOfficeName", order -> new OrderSpecifier<>(order, new QOffice("fromOffice").name));
            fieldToSpecifier.put("toOfficeId", order -> new OrderSpecifier<>(order, new QOffice("toOffice").id));
            fieldToSpecifier.put("toOfficeName", order -> new OrderSpecifier<>(order, new QOffice("toOffice").name));
            fieldToSpecifier.put("fromClientId", order -> new OrderSpecifier<>(order, new QClient("fromClient").id));
            fieldToSpecifier.put("fromClientName", order -> new OrderSpecifier<>(order, new QClient("fromClient").displayName));
            fieldToSpecifier.put("toClientId", order -> new OrderSpecifier<>(order, new QClient("toClient").id));
            fieldToSpecifier.put("toClientName", order -> new OrderSpecifier<>(order, new QClient("toClient").displayName));
            fieldToSpecifier.put("fromSavingsAccountId", order -> new OrderSpecifier<>(order, new QSavingsAccount("fromSavings").id));
            fieldToSpecifier.put("fromSavingsAccountNo",
                    order -> new OrderSpecifier<>(order, new QSavingsAccount("fromSavings").accountNumber));
            fieldToSpecifier.put("toSavingsAccountId", order -> new OrderSpecifier<>(order, new QSavingsAccount("toSavings").id));
            fieldToSpecifier.put("toSavingsAccountNo",
                    order -> new OrderSpecifier<>(order, new QSavingsAccount("toSavings").accountNumber));
            fieldToSpecifier.put("fromLoanAccountId", order -> new OrderSpecifier<>(order, new QLoan("fromLoan").id));
            fieldToSpecifier.put("fromLoanAccountNo", order -> new OrderSpecifier<>(order, new QLoan("fromLoan").accountNumber));
            fieldToSpecifier.put("toLoanAccountId", order -> new OrderSpecifier<>(order, new QLoan("toLoan").id));
            fieldToSpecifier.put("toLoanAccountNo", order -> new OrderSpecifier<>(order, new QLoan("toLoan").accountNumber));
            fieldToSpecifier.put("fromProductId", order -> new OrderSpecifier<>(order, new QSavingsProduct("fromSavingsProduct").id));
            fieldToSpecifier.put("fromProductName", order -> new OrderSpecifier<>(order, new QSavingsProduct("fromSavingsProduct").name));
            fieldToSpecifier.put("toProductId", order -> new OrderSpecifier<>(order, new QSavingsProduct("toSavingsProduct").id));
            fieldToSpecifier.put("toProductName", order -> new OrderSpecifier<>(order, new QSavingsProduct("toSavingsProduct").name));
            fieldToSpecifier.put("fromLoanProductId", order -> new OrderSpecifier<>(order, new QSavingsProduct("fromLoanProduct").id));
            fieldToSpecifier.put("fromLoanProductName", order -> new OrderSpecifier<>(order, new QSavingsProduct("fromLoanProduct").name));
            fieldToSpecifier.put("toLoanProductId", order -> new OrderSpecifier<>(order, new QSavingsProduct("toLoanProduct").id));
            fieldToSpecifier.put("toLoanProductName", order -> new OrderSpecifier<>(order, new QSavingsProduct("toLoanProduct").name));
        }

        public static OrderSpecifier<?> getSpecifier(final String fieldName, final Order order) {
            final OrderSpecifierGenerator generator = fieldToSpecifier.get(fieldName);
            if (generator != null) {
                return generator.generate(order);
            } else {
                throw new IllegalArgumentException("Unknown order field name: " + fieldName);
            }
        }
    }

    private JPAQuery<Tuple> getStandingInstructionHistorySelectQuery() {
        final QAccountTransferStandingInstructionsHistory qHistory = QAccountTransferStandingInstructionsHistory.accountTransferStandingInstructionsHistory;
        final QAccountTransferStandingInstruction qAccountTransferStandingInstruction = QAccountTransferStandingInstruction.accountTransferStandingInstruction;
        final QOffice qFromOffice = new QOffice("fromOffice");
        final QOffice qToOffice = new QOffice("toOffice");
        final QClient qFromClient = new QClient("fromClient");
        final QClient qToClient = new QClient("toClient");
        final QSavingsAccount qFromSavingsAccount = new QSavingsAccount("fromSavingsAccount");
        final QSavingsAccount qToSavingsAccount = new QSavingsAccount("toSavingsAccount");
        final QLoan qFromLoanAccount = new QLoan("fromLoanAccount");
        final QLoan qToLoanAccount = new QLoan("toLoanAccount");
        final QSavingsProduct qFromSavingsProduct = new QSavingsProduct("fromSavingsProduct");
        final QSavingsProduct qToSavingsProduct = new QSavingsProduct("toSavingsProduct");
        final QLoanProduct qFromLoanProduct = new QLoanProduct("fromLoanProduct");
        final QLoanProduct qToLoanProduct = new QLoanProduct("toLoanProduct");

        final JPAQuery<Tuple> query = new JPAQuery<>(entityManager);

        query.select(qAccountTransferStandingInstruction.id, qAccountTransferStandingInstruction.name, qHistory.status,
                qHistory.executionTime, qHistory.amount, qHistory.errorLog, qFromOffice.id.as("fromOfficeId"),
                qFromOffice.name.as("fromOfficeName"), qToOffice.id.as("toOfficeId"), qToOffice.name.as("toOfficeName"),
                qFromClient.id.as("fromClientId"), qFromClient.displayName.as("fromClientName"), qToClient.id.as("toClientId"),
                qToClient.displayName.as("toClientName"), qFromSavingsAccount.id.as("fromSavingsAccountId"),
                qFromSavingsAccount.accountNumber.as("fromSavingsAccountNo"), qToSavingsAccount.id.as("toSavingsAccountId"),
                qToSavingsAccount.accountNumber.as("toSavingsAccountNo"), qFromLoanAccount.id.as("fromLoanAccountId"),
                qFromLoanAccount.accountNumber.as("fromLoanAccountNo"), qToLoanAccount.id.as("toLoanAccountId"),
                qToLoanAccount.accountNumber.as("toLoanAccountNo"), qFromSavingsProduct.id.as("fromProductId"),
                qFromSavingsProduct.name.as("fromProductName"), qToSavingsProduct.id.as("toProductId"),
                qToSavingsProduct.name.as("toProductName"), qFromLoanProduct.id.as("fromLoanProductId"),
                qFromLoanProduct.name.as("fromLoanProductName"), qToLoanProduct.id.as("toLoanProductId"),
                qToLoanProduct.name.as("toLoanProductName")).from(qHistory);

        addJoinsToStandingInstructionHistoryQuery(query);
        return query;
    }

    private List<StandingInstructionHistoryData> mapQueryResultToStandingInstructionHistoryDataList(final List<Tuple> queryResult) {
        return queryResult.stream().map(this::mapQueryResultToStandingInstructionHistoryData).collect(toList());
    }

    private StandingInstructionHistoryData mapQueryResultToStandingInstructionHistoryData(final Tuple queryResult) {
        final QAccountTransferStandingInstructionsHistory qHistory = QAccountTransferStandingInstructionsHistory.accountTransferStandingInstructionsHistory;
        final QAccountTransferStandingInstruction qAccountTransferStandingInstruction = QAccountTransferStandingInstruction.accountTransferStandingInstruction;
        final QOffice qFromOffice = new QOffice("fromOffice");
        final QOffice qToOffice = new QOffice("toOffice");
        final QClient qFromClient = new QClient("fromClient");
        final QClient qToClient = new QClient("toClient");
        final QSavingsAccount qFromSavingsAccount = new QSavingsAccount("fromSavingsAccount");
        final QSavingsAccount qToSavingsAccount = new QSavingsAccount("toSavingsAccount");
        final QLoan qFromLoanAccount = new QLoan("fromLoanAccount");
        final QLoan qToLoanAccount = new QLoan("toLoanAccount");
        final QSavingsProduct qFromSavingsProduct = new QSavingsProduct("fromSavingsProduct");
        final QSavingsProduct qToSavingsProduct = new QSavingsProduct("toSavingsProduct");
        final QLoanProduct qFromLoanProduct = new QLoanProduct("fromLoanProduct");
        final QLoanProduct qToLoanProduct = new QLoanProduct("toLoanProduct");

        final OfficeData fromOffice = OfficeData.dropdown(queryResult.get(qFromOffice.id.as("fromOfficeId")),
                queryResult.get(qFromOffice.name.as("fromOfficeName")), null);
        final OfficeData toOffice = OfficeData.dropdown(queryResult.get(qToOffice.id.as("toOfficeId")),
                queryResult.get(qToOffice.name.as("toOfficeName")), null);
        final ClientData fromClient = ClientData.lookup(queryResult.get(qFromClient.id.as("fromClientId")),
                queryResult.get(qFromClient.displayName.as("fromClientName")), queryResult.get(qFromOffice.id.as("fromOfficeId")),
                queryResult.get(qFromOffice.name.as("fromOfficeName")));
        final ClientData toClient = ClientData.lookup(queryResult.get(qToClient.id.as("toClientId")),
                queryResult.get(qToClient.displayName.as("toClientName")), queryResult.get(qToOffice.id.as("toOfficeId")),
                queryResult.get(qToOffice.name.as("toOfficeName")));

        final Long fromSavingsAccountId = queryResult.get(qFromSavingsAccount.id.as("fromSavingsAccountId"));
        final String fromSavingsAccountNo = queryResult.get(qFromSavingsAccount.accountNumber.as("fromSavingsAccountNo"));
        final Long fromProductId = queryResult.get(qFromSavingsProduct.id.as("fromProductId"));
        final String fromProductName = queryResult.get(qFromSavingsProduct.name.as("fromProductName"));
        final Long fromLoanAccountId = queryResult.get(qFromLoanAccount.id.as("fromLoanAccountId"));
        final String fromLoanAccountNo = queryResult.get(qFromLoanAccount.accountNumber.as("fromLoanAccountNo"));
        final Long fromLoanProductId = queryResult.get(qFromLoanProduct.id.as("fromLoanProductId"));
        final String fromLoanProductName = queryResult.get(qFromLoanProduct.name.as("fromLoanProductName"));
        PortfolioAccountData fromAccount = null;
        EnumOptionData fromAccountType = null;
        if (fromSavingsAccountId != null) {
            fromAccount = new PortfolioAccountData(fromSavingsAccountId, fromSavingsAccountNo, null, null, null, null, null, fromProductId,
                    fromProductName, null, null, null);
            fromAccountType = accountType(PortfolioAccountType.SAVINGS);
        } else if (fromLoanAccountId != null) {
            fromAccount = new PortfolioAccountData(fromLoanAccountId, fromLoanAccountNo, null, null, null, null, null, fromLoanProductId,
                    fromLoanProductName, null, null, null);
            fromAccountType = accountType(PortfolioAccountType.LOAN);
        }

        PortfolioAccountData toAccount = null;
        EnumOptionData toAccountType = null;
        final Long toSavingsAccountId = queryResult.get(qToSavingsAccount.id.as("toSavingsAccountId"));
        final String toSavingsAccountNo = queryResult.get(qToSavingsAccount.accountNumber.as("toSavingsAccountNo"));
        final Long toProductId = queryResult.get(qToSavingsProduct.id.as("toProductId"));
        final String toProductName = queryResult.get(qToSavingsProduct.name.as("toProductName"));
        final Long toLoanAccountId = queryResult.get(qToLoanAccount.id.as("toLoanAccountId"));
        final String toLoanAccountNo = queryResult.get(qToLoanAccount.accountNumber.as("toLoanAccountNo"));
        final Long toLoanProductId = queryResult.get(qToLoanProduct.id.as("toLoanProductId"));
        final String toLoanProductName = queryResult.get(qToLoanProduct.name.as("toLoanProductName"));

        if (toSavingsAccountId != null) {
            toAccount = new PortfolioAccountData(toSavingsAccountId, toSavingsAccountNo, null, null, null, null, null, toProductId,
                    toProductName, null, null, null);
            toAccountType = accountType(PortfolioAccountType.SAVINGS);
        } else if (toLoanAccountId != null) {
            toAccount = new PortfolioAccountData(toLoanAccountId, toLoanAccountNo, null, null, null, null, null, toLoanProductId,
                    toLoanProductName, null, null, null);
            toAccountType = accountType(PortfolioAccountType.LOAN);
        }

        return new StandingInstructionHistoryData(queryResult.get(qAccountTransferStandingInstruction.id),
                queryResult.get(qAccountTransferStandingInstruction.name), fromOffice, fromClient, fromAccountType, fromAccount,
                toAccountType, toAccount, toOffice, toClient, queryResult.get(qHistory.amount), queryResult.get(qHistory.status),
                Objects.requireNonNull(queryResult.get(qHistory.executionTime)).atZone(DateUtils.getDateTimeZoneOfTenant()).toInstant()
                        .atZone(DateUtils.getDateTimeZoneOfTenant()).toLocalDate(),
                queryResult.get(qHistory.errorLog));
    }

    private void addJoinsToStandingInstructionHistoryQuery(final JPAQuery<?> selectFromQuery) {
        final QAccountTransferStandingInstructionsHistory qHistory = QAccountTransferStandingInstructionsHistory.accountTransferStandingInstructionsHistory;
        final QAccountTransferStandingInstruction qAccountTransferStandingInstruction = QAccountTransferStandingInstruction.accountTransferStandingInstruction;
        final QAccountTransferDetails qAccountTransferDetails = QAccountTransferDetails.accountTransferDetails;
        final QOffice qFromOffice = new QOffice("fromOffice");
        final QOffice qToOffice = new QOffice("toOffice");
        final QClient qFromClient = new QClient("fromClient");
        final QClient qToClient = new QClient("toClient");
        final QSavingsAccount qFromSavingsAccount = new QSavingsAccount("fromSavingsAccount");
        final QSavingsAccount qToSavingsAccount = new QSavingsAccount("toSavingsAccount");
        final QLoan qFromLoanAccount = new QLoan("fromLoanAccount");
        final QLoan qToLoanAccount = new QLoan("toLoanAccount");
        final QSavingsProduct qFromSavingsProduct = new QSavingsProduct("fromSavingsProduct");
        final QSavingsProduct qToSavingsProduct = new QSavingsProduct("toSavingsProduct");
        final QLoanProduct qFromLoanProduct = new QLoanProduct("fromLoanProduct");
        final QLoanProduct qToLoanProduct = new QLoanProduct("toLoanProduct");

        selectFromQuery.join(qHistory.accountTransferStandingInstruction, qAccountTransferStandingInstruction)
                .on(qAccountTransferStandingInstruction.id.eq(qHistory.accountTransferStandingInstruction.id))
                .join(qAccountTransferStandingInstruction.accountTransferDetails, qAccountTransferDetails)
                .on(qAccountTransferDetails.id.eq(qAccountTransferStandingInstruction.accountTransferDetails.id))
                .join(qAccountTransferDetails.fromOffice, qFromOffice).on(qFromOffice.id.eq(qAccountTransferDetails.fromOffice.id))
                .join(qAccountTransferDetails.toOffice, qToOffice).on(qToOffice.id.eq(qAccountTransferDetails.toOffice.id))
                .join(qAccountTransferDetails.fromClient, qFromClient).on(qFromClient.id.eq(qAccountTransferDetails.fromClient.id))
                .join(qAccountTransferDetails.toClient, qToClient).on(qToClient.id.eq(qAccountTransferDetails.toClient.id))
                .leftJoin(qAccountTransferDetails.fromSavingsAccount, qFromSavingsAccount)
                .on(qFromSavingsAccount.id.eq(qAccountTransferDetails.fromSavingsAccount.id))
                .leftJoin(qAccountTransferDetails.toSavingsAccount, qToSavingsAccount)
                .on(qToSavingsAccount.id.eq(qAccountTransferDetails.toSavingsAccount.id))
                .leftJoin(qAccountTransferDetails.fromLoanAccount, qFromLoanAccount)
                .on(qFromLoanAccount.id.eq(qAccountTransferDetails.fromLoanAccount.id))
                .leftJoin(qAccountTransferDetails.toLoanAccount, qToLoanAccount)
                .on(qToLoanAccount.id.eq(qAccountTransferDetails.toLoanAccount.id))
                .leftJoin(qFromSavingsAccount.product, qFromSavingsProduct).on(qFromSavingsProduct.id.eq(qFromSavingsAccount.product.id))
                .leftJoin(qToSavingsAccount.product, qToSavingsProduct).on(qToSavingsProduct.id.eq(qToSavingsAccount.product.id))
                .leftJoin(qFromLoanAccount.loanProduct, qFromLoanProduct).on(qFromLoanProduct.id.eq(qFromLoanAccount.loanProduct.id))
                .leftJoin(qToLoanAccount.loanProduct, qToLoanProduct).on(qToLoanProduct.id.eq(qToLoanAccount.loanProduct.id));
    }

    private JPAQuery<Long> getStandingInstructionHistoryCountQuery() {
        final QAccountTransferStandingInstructionsHistory qHistory = QAccountTransferStandingInstructionsHistory.accountTransferStandingInstructionsHistory;
        final JPAQuery<Long> query = new JPAQuery<>(entityManager);

        query.select(ONE.count()).from(qHistory);
        addJoinsToStandingInstructionHistoryQuery(query);
        return query;
    }

    private BooleanExpression addCondition(final BooleanExpression whereClause, final BooleanExpression condition) {
        if (whereClause == null) {
            return condition;
        } else {
            return whereClause.and(condition);
        }
    }
}
