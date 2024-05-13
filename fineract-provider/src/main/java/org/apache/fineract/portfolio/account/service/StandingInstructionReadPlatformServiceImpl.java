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
import static org.apache.fineract.portfolio.account.service.AccountTransferEnumerations.recurrenceType;
import static org.apache.fineract.portfolio.account.service.AccountTransferEnumerations.standingInstructionPriority;
import static org.apache.fineract.portfolio.account.service.AccountTransferEnumerations.standingInstructionStatus;
import static org.apache.fineract.portfolio.account.service.AccountTransferEnumerations.standingInstructionType;
import static org.apache.fineract.portfolio.account.service.AccountTransferEnumerations.transferType;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.domain.QOffice;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDuesData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.QAccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.QAccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.StandingInstructionPriority;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.exception.AccountTransferNotFoundException;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.domain.QClient;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.common.service.CommonEnumerations;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.QLoan;
import org.apache.fineract.portfolio.loanaccount.domain.QLoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanproduct.domain.QLoanProduct;
import org.apache.fineract.portfolio.savings.domain.QSavingsAccount;
import org.apache.fineract.portfolio.savings.domain.QSavingsProduct;
import org.springframework.util.CollectionUtils;

@AllArgsConstructor
public class StandingInstructionReadPlatformServiceImpl implements StandingInstructionReadPlatformService {

    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    private final DropdownReadPlatformService dropdownReadPlatformService;

    // pagination
    private final PaginationHelper paginationHelper;
    private final EntityManager entityManager;

    @Override
    public StandingInstructionData retrieveTemplate(final Long fromOfficeId, final Long fromClientId, final Long fromAccountId,
            final Integer fromAccountType, final Long toOfficeId, final Long toClientId, final Long toAccountId,
            final Integer toAccountType, Integer transferType) {

        AccountTransferType accountTransferType = AccountTransferType.INVALID;
        if (transferType != null) {
            accountTransferType = AccountTransferType.fromInt(transferType);
        }

        final EnumOptionData loanAccountType = accountType(PortfolioAccountType.LOAN);
        final EnumOptionData savingsAccountType = accountType(PortfolioAccountType.SAVINGS);

        Collection<EnumOptionData> fromAccountTypeOptions;
        Collection<EnumOptionData> toAccountTypeOptions;

        if (accountTransferType.isAccountTransfer()) {
            fromAccountTypeOptions = Collections.singletonList(savingsAccountType);
            toAccountTypeOptions = Collections.singletonList(savingsAccountType);
        } else if (accountTransferType.isLoanRepayment()) {
            fromAccountTypeOptions = Collections.singletonList(savingsAccountType);
            toAccountTypeOptions = Collections.singletonList(loanAccountType);
        } else {
            fromAccountTypeOptions = Arrays.asList(savingsAccountType, loanAccountType);
            toAccountTypeOptions = Arrays.asList(loanAccountType, savingsAccountType);
        }

        final EnumOptionData fromAccountTypeData = accountType(fromAccountType);
        final EnumOptionData toAccountTypeData = accountType(toAccountType);

        // from settings
        OfficeData fromOffice = null;
        ClientData fromClient = null;
        PortfolioAccountData fromAccount = null;

        OfficeData toOffice = null;
        ClientData toClient = null;
        PortfolioAccountData toAccount = null;

        // template
        Collection<PortfolioAccountData> fromAccountOptions = null;
        Collection<PortfolioAccountData> toAccountOptions = null;

        Long mostRelevantFromOfficeId = fromOfficeId;
        Long mostRelevantFromClientId = fromClientId;

        Long mostRelevantToOfficeId = toOfficeId;
        Long mostRelevantToClientId = toClientId;

        if (fromAccountId != null) {
            Integer accountType;
            if (fromAccountType == 1) {
                accountType = PortfolioAccountType.LOAN.getValue();
            } else {
                accountType = PortfolioAccountType.SAVINGS.getValue();
            }
            fromAccount = this.portfolioAccountReadPlatformService.retrieveOne(fromAccountId, accountType);

            // override provided fromClient with client of account
            mostRelevantFromClientId = fromAccount.getClientId();
        }

        if (mostRelevantFromClientId != null) {
            fromClient = this.clientReadPlatformService.retrieveOne(mostRelevantFromClientId);
            mostRelevantFromOfficeId = fromClient.getOfficeId();
            long[] loanStatus = null;
            if (fromAccountType == 1) {
                loanStatus = new long[] { 300, 700 };
            }
            PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(fromAccountType, mostRelevantFromClientId, loanStatus);
            fromAccountOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);
        }

        Collection<OfficeData> fromOfficeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
        Collection<ClientData> fromClientOptions = null;

        if (mostRelevantFromOfficeId != null) {
            fromOffice = this.officeReadPlatformService.retrieveOffice(mostRelevantFromOfficeId);
            fromClientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(mostRelevantFromOfficeId);
        }

        // defaults
        final LocalDate transferDate = DateUtils.getBusinessLocalDate();
        Collection<OfficeData> toOfficeOptions = fromOfficeOptions;
        Collection<ClientData> toClientOptions = null;

        if (toAccountId != null && fromAccount != null) {
            toAccount = this.portfolioAccountReadPlatformService.retrieveOne(toAccountId, toAccountType, fromAccount.getCurrencyCode());
            mostRelevantToClientId = toAccount.getClientId();
        }

        if (mostRelevantToClientId != null) {
            toClient = this.clientReadPlatformService.retrieveOne(mostRelevantToClientId);
            mostRelevantToOfficeId = toClient.getOfficeId();

            toClientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(mostRelevantToOfficeId);

            toAccountOptions = retrieveToAccounts(fromAccount, toAccountType, mostRelevantToClientId);
        }

        if (mostRelevantToOfficeId != null) {
            toOffice = this.officeReadPlatformService.retrieveOffice(mostRelevantToOfficeId);
            toOfficeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

            toClientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(mostRelevantToOfficeId);
            if (toClientOptions != null && toClientOptions.size() == 1) {
                toClient = new ArrayList<>(toClientOptions).get(0);

                toAccountOptions = retrieveToAccounts(fromAccount, toAccountType, mostRelevantToClientId);
            }
        }

        final Collection<EnumOptionData> transferTypeOptions = Arrays.asList(transferType(AccountTransferType.ACCOUNT_TRANSFER),
                transferType(
                        AccountTransferType.LOAN_REPAYMENT)/*
                                                            * , transferType( AccountTransferType . CHARGE_PAYMENT )
                                                            */);
        final Collection<EnumOptionData> statusOptions = Arrays.asList(standingInstructionStatus(StandingInstructionStatus.ACTIVE),
                standingInstructionStatus(StandingInstructionStatus.DISABLED));
        final Collection<EnumOptionData> instructionTypeOptions = Arrays.asList(standingInstructionType(StandingInstructionType.FIXED),
                standingInstructionType(StandingInstructionType.DUES));
        final Collection<EnumOptionData> priorityOptions = Arrays.asList(standingInstructionPriority(StandingInstructionPriority.URGENT),
                standingInstructionPriority(StandingInstructionPriority.HIGH),
                standingInstructionPriority(StandingInstructionPriority.MEDIUM),
                standingInstructionPriority(StandingInstructionPriority.LOW));
        final Collection<EnumOptionData> recurrenceTypeOptions = Arrays.asList(recurrenceType(AccountTransferRecurrenceType.PERIODIC),
                recurrenceType(AccountTransferRecurrenceType.AS_PER_DUES));
        final Collection<EnumOptionData> recurrenceFrequencyOptions = this.dropdownReadPlatformService.retrievePeriodFrequencyTypeOptions();

        return StandingInstructionData.template(fromOffice, fromClient, fromAccountTypeData, fromAccount, transferDate, toOffice, toClient,
                toAccountTypeData, toAccount, fromOfficeOptions, fromClientOptions, fromAccountTypeOptions, fromAccountOptions,
                toOfficeOptions, toClientOptions, toAccountTypeOptions, toAccountOptions, transferTypeOptions, statusOptions,
                instructionTypeOptions, priorityOptions, recurrenceTypeOptions, recurrenceFrequencyOptions);
    }

    private Collection<PortfolioAccountData> retrieveToAccounts(final PortfolioAccountData excludeThisAccountFromOptions,
            final Integer toAccountType, final Long toClientId) {

        final String currencyCode = excludeThisAccountFromOptions != null ? excludeThisAccountFromOptions.getCurrencyCode() : null;

        PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(toAccountType, toClientId, currencyCode, null, null);
        Collection<PortfolioAccountData> accountOptions = this.portfolioAccountReadPlatformService
                .retrieveAllForLookup(portfolioAccountDTO);
        if (!CollectionUtils.isEmpty(accountOptions)) {
            accountOptions.remove(excludeThisAccountFromOptions);
        } else {
            accountOptions = null;
        }

        return accountOptions;
    }

    @Override
    public Page<StandingInstructionData> retrieveAll(final StandingInstructionDTO standingInstructionDTO) {

        final QAccountTransferDetails qAccountTransferDetails = QAccountTransferDetails.accountTransferDetails;
        final QClient qFromClient = new QClient("qFromClient");
        final QSavingsAccount qFromSavingsAccount = new QSavingsAccount("qFromSavingsAccount");
        final QLoan qFromLoan = new QLoan("qFromLoan");
        final JPAQuery<Tuple> query = getStandingInstructionSelectQuery();
        final JPAQuery<Long> totalCountQuery = getStandingInstructionCountQuery();

        BooleanExpression whereClause = null;

        if (standingInstructionDTO.transferType() != null || standingInstructionDTO.clientId() != null
                || standingInstructionDTO.clientName() != null) {
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
                    whereClause = addCondition(whereClause, qFromLoan.id.eq(standingInstructionDTO.fromAccount()));
                }
            }
        }

        query.where(whereClause);
        totalCountQuery.where(whereClause);

        final SearchParameters searchParameters = standingInstructionDTO.searchParameters();
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

        final List<Tuple> queryResult = query.fetch();
        return this.paginationHelper.createPageFromItems(
                queryResult.isEmpty() ? Collections.emptyList() : mapQueryResultToStandingInstructionDuesDataList(queryResult),
                Objects.requireNonNull(totalCountQuery.fetchOne()));
    }

    @Override
    public Collection<StandingInstructionData> retrieveAll(final Integer status) {
        final String businessDate = DateUtils.getBusinessLocalDate().format(DateUtils.DEFAULT_DATE_FORMATTER);

        final QAccountTransferStandingInstruction qAccountTransferStandingInstruction = QAccountTransferStandingInstruction.accountTransferStandingInstruction;
        final List<Tuple> queryResult = getStandingInstructionSelectQuery()
                .where(eq(qAccountTransferStandingInstruction.status, status)
                        .and(qAccountTransferStandingInstruction.validFrom.loe(LocalDate.parse(businessDate)))
                        .and(qAccountTransferStandingInstruction.validTill.isNull()
                                .or(qAccountTransferStandingInstruction.validTill.gt(LocalDate.parse(businessDate))))
                        .and(qAccountTransferStandingInstruction.latsRunDate.ne(LocalDate.parse(businessDate))
                                .or(qAccountTransferStandingInstruction.latsRunDate.isNull())))
                .orderBy(new OrderSpecifier<>(Order.DESC, qAccountTransferStandingInstruction.priority)).fetch();
        return queryResult.isEmpty() ? Collections.emptyList() : mapQueryResultToStandingInstructionDuesDataList(queryResult);
    }

    @Override
    public StandingInstructionData retrieveOne(final Long instructionId) {
        final QAccountTransferStandingInstruction qAccountTransferStandingInstruction = QAccountTransferStandingInstruction.accountTransferStandingInstruction;
        final Tuple queryResult = getStandingInstructionSelectQuery().where(eq(qAccountTransferStandingInstruction.id, instructionId))
                .fetchOne();

        return Optional.ofNullable(queryResult).map(this::mapQueryResultToStandingInstructionDuesData)
                .orElseThrow(() -> new AccountTransferNotFoundException(instructionId));
    }

    @Override
    public StandingInstructionDuesData retriveLoanDuesData(final Long loanId) {
        final QLoanRepaymentScheduleInstallment qLoanRepaymentSchedule = QLoanRepaymentScheduleInstallment.loanRepaymentScheduleInstallment;
        final QLoan qLoan = QLoan.loan;
        final JPAQuery<StandingInstructionDuesData> query = getStandingInstructionDuesSelectQuery();
        query.where(eq(qLoan.id, loanId),
                qLoanRepaymentSchedule.dueDate
                        .loe(LocalDate.parse(DateUtils.getBusinessLocalDate().format(DateUtils.DEFAULT_DATE_FORMATTER))),
                qLoanRepaymentSchedule.obligationsMet.ne(true));
        return query.fetchOne();
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
            fieldToSpecifier.put("priority",
                    order -> new OrderSpecifier<>(order, QAccountTransferStandingInstruction.accountTransferStandingInstruction.priority));
            fieldToSpecifier.put("status",
                    order -> new OrderSpecifier<>(order, QAccountTransferStandingInstruction.accountTransferStandingInstruction.status));
            fieldToSpecifier.put("instructionType", order -> new OrderSpecifier<>(order,
                    QAccountTransferStandingInstruction.accountTransferStandingInstruction.instructionType));
            fieldToSpecifier.put("amount",
                    order -> new OrderSpecifier<>(order, QAccountTransferStandingInstruction.accountTransferStandingInstruction.amount));
            fieldToSpecifier.put("validFrom",
                    order -> new OrderSpecifier<>(order, QAccountTransferStandingInstruction.accountTransferStandingInstruction.validFrom));
            fieldToSpecifier.put("validTill",
                    order -> new OrderSpecifier<>(order, QAccountTransferStandingInstruction.accountTransferStandingInstruction.validTill));
            fieldToSpecifier.put("recurrenceType", order -> new OrderSpecifier<>(order,
                    QAccountTransferStandingInstruction.accountTransferStandingInstruction.recurrenceType));
            fieldToSpecifier.put("recurrenceFrequency", order -> new OrderSpecifier<>(order,
                    QAccountTransferStandingInstruction.accountTransferStandingInstruction.recurrenceFrequency));
            fieldToSpecifier.put("recurrenceInterval", order -> new OrderSpecifier<>(order,
                    QAccountTransferStandingInstruction.accountTransferStandingInstruction.recurrenceInterval));
            fieldToSpecifier.put("recurrenceOnDay", order -> new OrderSpecifier<>(order,
                    QAccountTransferStandingInstruction.accountTransferStandingInstruction.recurrenceOnDay));
            fieldToSpecifier.put("recurrenceOnMonth", order -> new OrderSpecifier<>(order,
                    QAccountTransferStandingInstruction.accountTransferStandingInstruction.recurrenceOnMonth));
            fieldToSpecifier.put("accountDetailId",
                    order -> new OrderSpecifier<>(order, QAccountTransferDetails.accountTransferDetails.id));
            fieldToSpecifier.put("transferType",
                    order -> new OrderSpecifier<>(order, QAccountTransferDetails.accountTransferDetails.transferType));
            fieldToSpecifier.put("fromOfficeId", order -> new OrderSpecifier<>(order, new QOffice("fromOffice").id));
            fieldToSpecifier.put("fromOfficeName", order -> new OrderSpecifier<>(order, new QOffice("fromOffice").name));
            fieldToSpecifier.put("toOfficeId", order -> new OrderSpecifier<>(order, new QOffice("toOffice").id));
            fieldToSpecifier.put("toOfficeName", order -> new OrderSpecifier<>(order, new QOffice("toOffice").name));
            fieldToSpecifier.put("fromClientId", order -> new OrderSpecifier<>(order, new QClient("fromClient").id));
            fieldToSpecifier.put("fromClientName", order -> new OrderSpecifier<>(order, new QClient("fromClient").displayName));
            fieldToSpecifier.put("toClientId", order -> new OrderSpecifier<>(order, new QClient("toClient").id));
            fieldToSpecifier.put("toClientName", order -> new OrderSpecifier<>(order, new QClient("toClient").displayName));
            fieldToSpecifier.put("fromSavingsAccountId",
                    order -> new OrderSpecifier<>(order, new QSavingsAccount("fromSavingsAccount").id));
            fieldToSpecifier.put("fromSavingsAccountNo",
                    order -> new OrderSpecifier<>(order, new QSavingsAccount("fromSavingsAccount").accountNumber));
            fieldToSpecifier.put("toSavingsAccountId", order -> new OrderSpecifier<>(order, new QSavingsAccount("toSavingsAccount").id));
            fieldToSpecifier.put("toSavingsAccountNo",
                    order -> new OrderSpecifier<>(order, new QSavingsAccount("toSavingsAccount").accountNumber));
            fieldToSpecifier.put("fromLoanAccountId", order -> new OrderSpecifier<>(order, new QLoan("fromLoan").id));
            fieldToSpecifier.put("fromLoanAccountNo", order -> new OrderSpecifier<>(order, new QLoan("fromLoan").accountNumber));
            fieldToSpecifier.put("toLoanAccountId", order -> new OrderSpecifier<>(order, new QLoan("toLoan").id));
            fieldToSpecifier.put("toLoanAccountNo", order -> new OrderSpecifier<>(order, new QLoan("toLoan").accountNumber));
            fieldToSpecifier.put("fromProductId", order -> new OrderSpecifier<>(order, new QSavingsProduct("fromSavingsProduct").id));
            fieldToSpecifier.put("fromProductName", order -> new OrderSpecifier<>(order, new QSavingsProduct("fromSavingsProduct").name));
            fieldToSpecifier.put("toProductId", order -> new OrderSpecifier<>(order, new QSavingsProduct("toSavingsProduct").id));
            fieldToSpecifier.put("fromLoanProductId", order -> new OrderSpecifier<>(order, new QLoanProduct("fromLoanProduct").id));
            fieldToSpecifier.put("fromLoanProductName", order -> new OrderSpecifier<>(order, new QLoanProduct("fromLoanProduct").name));
            fieldToSpecifier.put("toLoanProductId", order -> new OrderSpecifier<>(order, new QLoanProduct("toLoanProduct").id));
            fieldToSpecifier.put("toLoanProductName", order -> new OrderSpecifier<>(order, new QLoanProduct("toLoanProduct").name));
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

    private JPAQuery<Tuple> getStandingInstructionSelectQuery() {
        final QAccountTransferStandingInstruction qAccountTransferStandingInstruction = QAccountTransferStandingInstruction.accountTransferStandingInstruction;
        final QAccountTransferDetails qAccountTransferDetails = QAccountTransferDetails.accountTransferDetails;
        final QOffice qFromOffice = new QOffice("qFromOffice");
        final QOffice qToOffice = new QOffice("qToOffice");
        final QClient qFromClient = new QClient("qFromClient");
        final QClient qToClient = new QClient("qToClient");
        final QSavingsAccount qFromSavingsAccount = new QSavingsAccount("qFromSavingsAccount");
        final QSavingsAccount qToSavingsAccount = new QSavingsAccount("qToSavingsAccount");
        final QSavingsProduct qFromSavingsProduct = new QSavingsProduct("qFromSavingsProduct");
        final QSavingsProduct qToSavingsProduct = new QSavingsProduct("qToSavingsProduct");
        final QLoan qFromLoan = new QLoan("qFromLoan");
        final QLoan qToLoan = new QLoan("qToLoan");
        final QLoanProduct qFromLoanProduct = new QLoanProduct("qFromLoanProduct");
        final QLoanProduct qToLoanProduct = new QLoanProduct("qToLoanProduct");

        final JPAQuery<Tuple> query = new JPAQuery<>(entityManager);
        query.select(qAccountTransferStandingInstruction.id, qAccountTransferStandingInstruction.name,
                qAccountTransferStandingInstruction.priority, qAccountTransferStandingInstruction.status,
                qAccountTransferStandingInstruction.instructionType, qAccountTransferStandingInstruction.amount,
                qAccountTransferStandingInstruction.validFrom, qAccountTransferStandingInstruction.validTill,
                qAccountTransferStandingInstruction.recurrenceType, qAccountTransferStandingInstruction.recurrenceFrequency,
                qAccountTransferStandingInstruction.recurrenceInterval, qAccountTransferStandingInstruction.recurrenceOnDay,
                qAccountTransferStandingInstruction.recurrenceOnMonth, qAccountTransferDetails.id.as("accountDetailId"),
                qAccountTransferDetails.transferType, qFromOffice.id.as("fromOfficeId"), qFromOffice.name.as("fromOfficeName"),
                qToOffice.id.as("toOfficeId"), qToOffice.name.as("toOfficeName"), qFromClient.id.as("fromClientId"),
                qFromClient.displayName.as("fromClientName"), qToClient.id.as("toClientId"), qToClient.displayName.as("toClientName"),
                qFromSavingsAccount.id.as("fromSavingsAccountId"), qFromSavingsAccount.accountNumber.as("fromSavingsAccountNo"),
                qFromSavingsProduct.id.as("fromProductId"), qFromSavingsProduct.name.as("fromProductName"),
                qFromLoan.id.as("fromLoanAccountId"), qFromLoan.accountNumber.as("fromLoanAccountNo"),
                qFromLoanProduct.id.as("fromLoanProductId"), qFromLoanProduct.name.as("fromLoanProductName"),
                qToSavingsAccount.id.as("toSavingsAccountId"), qToSavingsAccount.accountNumber.as("toSavingsAccountNo"),
                qToSavingsProduct.id.as("toProductId"), qToSavingsProduct.name.as("toProductName"), qToLoan.id.as("toLoanAccountId"),
                qToLoan.accountNumber.as("toLoanAccountNo"), qToLoanProduct.id.as("toLoanProductId"),
                qToLoanProduct.name.as("toLoanProductName")).from(qAccountTransferStandingInstruction);

        addJoinsToStandingInstructionQuery(query);
        return query;
    }

    private StandingInstructionData mapQueryResultToStandingInstructionDuesData(final Tuple queryResult) {
        final QAccountTransferStandingInstruction qAccountTransferStandingInstruction = QAccountTransferStandingInstruction.accountTransferStandingInstruction;
        final QAccountTransferDetails qAccountTransferDetails = QAccountTransferDetails.accountTransferDetails;
        final QOffice qFromOffice = new QOffice("qFromOffice");
        final QOffice qToOffice = new QOffice("qToOffice");
        final QClient qFromClient = new QClient("qFromClient");
        final QClient qToClient = new QClient("qToClient");
        final QSavingsAccount qFromSavingsAccount = new QSavingsAccount("qFromSavingsAccount");
        final QSavingsAccount qToSavingsAccount = new QSavingsAccount("qToSavingsAccount");
        final QSavingsProduct qFromSavingsProduct = new QSavingsProduct("qFromSavingsProduct");
        final QSavingsProduct qToSavingsProduct = new QSavingsProduct("qToSavingsProduct");
        final QLoan qFromLoan = new QLoan("qFromLoan");
        final QLoan qToLoan = new QLoan("qToLoan");
        final QLoanProduct qFromLoanProduct = new QLoanProduct("qFromLoanProduct");
        final QLoanProduct qToLoanProduct = new QLoanProduct("qToLoanProduct");

        EnumOptionData priorityEnum = AccountTransferEnumerations
                .standingInstructionPriority(queryResult.get(qAccountTransferStandingInstruction.priority));
        EnumOptionData statusEnum = AccountTransferEnumerations
                .standingInstructionStatus(queryResult.get(qAccountTransferStandingInstruction.status));
        EnumOptionData instructionTypeEnum = AccountTransferEnumerations
                .standingInstructionType(queryResult.get(qAccountTransferStandingInstruction.instructionType));

        final LocalDate validFrom = queryResult.get(qAccountTransferStandingInstruction.validFrom);
        final LocalDate validTill = queryResult.get(qAccountTransferStandingInstruction.validTill);
        final BigDecimal transferAmount = JdbcSupport.defaultToNullIfZero(queryResult.get(qAccountTransferStandingInstruction.amount));
        EnumOptionData recurrenceTypeEnum = AccountTransferEnumerations
                .recurrenceType(queryResult.get(qAccountTransferStandingInstruction.recurrenceType));
        final Integer recurrenceFrequency = queryResult.get(qAccountTransferStandingInstruction.recurrenceFrequency);

        EnumOptionData recurrenceFrequencyEnum = null;
        if (recurrenceFrequency != null) {
            recurrenceFrequencyEnum = CommonEnumerations.termFrequencyType(recurrenceFrequency, "recurrence");
        }

        MonthDay recurrenceOnMonthDay = null;
        final Integer recurrenceOnDay = queryResult.get(qAccountTransferStandingInstruction.recurrenceOnDay);
        final Integer recurrenceOnMonth = queryResult.get(qAccountTransferStandingInstruction.recurrenceOnMonth);
        if (recurrenceOnDay != null && recurrenceOnMonth != null) {
            recurrenceOnMonthDay = MonthDay.now(DateUtils.getDateTimeZoneOfTenant()).withMonth(recurrenceOnMonth)
                    .withDayOfMonth(recurrenceOnDay);
        }

        EnumOptionData transferTypeEnum = AccountTransferEnumerations.transferType(queryResult.get(qAccountTransferDetails.transferType));

        final Long fromOfficeId = queryResult.get(qFromOffice.id.as("fromOfficeId"));
        final String fromOfficeName = queryResult.get(qFromOffice.name.as("fromOfficeName"));
        final OfficeData fromOffice = OfficeData.dropdown(fromOfficeId, fromOfficeName, null);

        final Long toOfficeId = queryResult.get(qToOffice.id.as("toOfficeId"));
        final String toOfficeName = queryResult.get(qToOffice.name.as("toOfficeName"));
        final OfficeData toOffice = OfficeData.dropdown(toOfficeId, toOfficeName, null);

        final Long fromClientId = queryResult.get(qFromClient.id.as("fromClientId"));
        final String fromClientName = queryResult.get(qFromClient.displayName.as("fromClientName"));
        final ClientData fromClient = ClientData.lookup(fromClientId, fromClientName, fromOfficeId, fromOfficeName);

        final Long toClientId = queryResult.get(qToClient.id.as("toClientId"));
        final String toClientName = queryResult.get(qToClient.displayName.as("toClientName"));
        final ClientData toClient = ClientData.lookup(toClientId, toClientName, toOfficeId, toOfficeName);

        final Long fromSavingsAccountId = queryResult.get(qFromSavingsAccount.id.as("fromSavingsAccountId"));
        final String fromSavingsAccountNo = queryResult.get(qFromSavingsAccount.accountNumber.as("fromSavingsAccountNo"));
        final Long fromProductId = queryResult.get(qFromSavingsProduct.id.as("fromProductId"));
        final String fromProductName = queryResult.get(qFromSavingsProduct.name.as("fromProductName"));
        final Long fromLoanAccountId = queryResult.get(qFromLoan.id.as("fromLoanAccountId"));
        final String fromLoanAccountNo = queryResult.get(qFromLoan.accountNumber.as("fromLoanAccountNo"));
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
        final Long toLoanAccountId = queryResult.get(qToLoan.id.as("toLoanAccountId"));
        final String toLoanAccountNo = queryResult.get(qToLoan.accountNumber.as("toLoanAccountNo"));
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

        return StandingInstructionData.instance(queryResult.get(qAccountTransferStandingInstruction.id),
                queryResult.get(qAccountTransferDetails.id.as("accountDetailId")),
                queryResult.get(qAccountTransferStandingInstruction.name), fromOffice, toOffice, fromClient, toClient, fromAccountType,
                fromAccount, toAccountType, toAccount, transferTypeEnum, priorityEnum, instructionTypeEnum, statusEnum, transferAmount,
                validFrom, validTill, recurrenceTypeEnum, recurrenceFrequencyEnum,
                queryResult.get(qAccountTransferStandingInstruction.recurrenceInterval), recurrenceOnMonthDay);
    }

    private List<StandingInstructionData> mapQueryResultToStandingInstructionDuesDataList(final List<Tuple> queryResult) {
        return queryResult.stream().map(this::mapQueryResultToStandingInstructionDuesData).collect(toList());
    }

    private JPAQuery<StandingInstructionDuesData> getStandingInstructionDuesSelectQuery() {
        final QLoanRepaymentScheduleInstallment qLoanRepaymentSchedule = QLoanRepaymentScheduleInstallment.loanRepaymentScheduleInstallment;
        final QLoan qLoan = QLoan.loan;

        final JPAQuery<StandingInstructionDuesData> query = new JPAQuery<>(entityManager);

        query.select(qLoanRepaymentSchedule.dueDate.max().as("dueDate"),
                qLoanRepaymentSchedule.principal.sumBigDecimal().as("principalAmount"),
                qLoanRepaymentSchedule.principalCompleted.sumBigDecimal().as("principalCompleted"),
                qLoanRepaymentSchedule.principalWrittenOff.sumBigDecimal().as("principalWrittenOff"),
                qLoanRepaymentSchedule.interestCharged.sumBigDecimal().as("interestAmount"),
                qLoanRepaymentSchedule.interestPaid.sumBigDecimal().as("interestCompleted"),
                qLoanRepaymentSchedule.interestWrittenOff.sumBigDecimal().as("interestWrittenOff"),
                qLoanRepaymentSchedule.interestWaived.sumBigDecimal().as("interestWaived"),
                qLoanRepaymentSchedule.penaltyCharges.sumBigDecimal().as("penalityAmount"),
                qLoanRepaymentSchedule.penaltyChargesPaid.sumBigDecimal().as("penalityCompleted"),
                qLoanRepaymentSchedule.penaltyChargesWrittenOff.sumBigDecimal().as("penaltyWrittenOff"),
                qLoanRepaymentSchedule.penaltyChargesWaived.sumBigDecimal().as("penaltyWaived"),
                qLoanRepaymentSchedule.feeChargesCharged.sumBigDecimal().as("feeAmount"),
                qLoanRepaymentSchedule.feeChargesPaid.sumBigDecimal().as("feecompleted"),
                qLoanRepaymentSchedule.feeChargesWrittenOff.sumBigDecimal().as("feeWrittenOff"),
                qLoanRepaymentSchedule.feeChargesWaived.sumBigDecimal().as("feeWaived")).from(qLoanRepaymentSchedule).join(qLoan)
                .on(qLoan.id.eq(qLoanRepaymentSchedule.loan.id));

        return query;
    }

    private void addJoinsToStandingInstructionQuery(final JPAQuery<?> selectFromQuery) {
        final QAccountTransferStandingInstruction qAccountTransferStandingInstruction = QAccountTransferStandingInstruction.accountTransferStandingInstruction;
        final QAccountTransferDetails qAccountTransferDetails = QAccountTransferDetails.accountTransferDetails;
        final QOffice qFromOffice = new QOffice("qFromOffice");
        final QOffice qToOffice = new QOffice("qToOffice");
        final QClient qFromClient = new QClient("qFromClient");
        final QClient qToClient = new QClient("qToClient");
        final QSavingsAccount qFromSavingsAccount = new QSavingsAccount("qFromSavingsAccount");
        final QSavingsAccount qToSavingsAccount = new QSavingsAccount("qToSavingsAccount");
        final QSavingsProduct qFromSavingsProduct = new QSavingsProduct("qFromSavingsProduct");
        final QSavingsProduct qToSavingsProduct = new QSavingsProduct("qToSavingsProduct");
        final QLoan qFromLoan = new QLoan("qFromLoan");
        final QLoan qToLoan = new QLoan("qToLoan");
        final QLoanProduct qFromLoanProduct = new QLoanProduct("qFromLoanProduct");
        final QLoanProduct qToLoanProduct = new QLoanProduct("qToLoanProduct");

        selectFromQuery.join(qAccountTransferStandingInstruction.accountTransferDetails, qAccountTransferDetails)
                .on(qAccountTransferDetails.id.eq(qAccountTransferStandingInstruction.accountTransferDetails.id))
                .join(qAccountTransferDetails.fromOffice, qFromOffice).on(qFromOffice.id.eq(qAccountTransferDetails.fromOffice.id))
                .join(qAccountTransferDetails.toOffice, qToOffice).on(qToOffice.id.eq(qAccountTransferDetails.toOffice.id))
                .join(qAccountTransferDetails.fromClient, qFromClient).on(qFromClient.id.eq(qAccountTransferDetails.fromClient.id))
                .join(qAccountTransferDetails.toClient, qToClient).on(qToClient.id.eq(qAccountTransferDetails.toClient.id))
                .leftJoin(qAccountTransferDetails.fromSavingsAccount, qFromSavingsAccount)
                .on(qFromSavingsAccount.id.eq(qAccountTransferDetails.fromSavingsAccount.id))
                .leftJoin(qFromSavingsAccount.product, qFromSavingsProduct).on(qFromSavingsProduct.id.eq(qFromSavingsAccount.product.id))
                .leftJoin(qAccountTransferDetails.fromLoanAccount, qFromLoan)
                .on(qFromLoan.id.eq(qAccountTransferDetails.fromLoanAccount.id)).leftJoin(qFromLoan.loanProduct, qFromLoanProduct)
                .on(qFromLoanProduct.id.eq(qFromLoan.loanProduct.id)).leftJoin(qAccountTransferDetails.toSavingsAccount, qToSavingsAccount)
                .on(qToSavingsAccount.id.eq(qAccountTransferDetails.toSavingsAccount.id))
                .leftJoin(qToSavingsAccount.product, qToSavingsProduct).on(qToSavingsProduct.id.eq(qToSavingsAccount.product.id))
                .leftJoin(qAccountTransferDetails.toLoanAccount, qToLoan).on(qToLoan.id.eq(qAccountTransferDetails.toLoanAccount.id))
                .leftJoin(qToLoan.loanProduct, qToLoanProduct).on(qToLoanProduct.id.eq(qToLoan.loanProduct.id));
    }

    private JPAQuery<Long> getStandingInstructionCountQuery() {
        final QAccountTransferStandingInstruction qAccountTransferStandingInstruction = QAccountTransferStandingInstruction.accountTransferStandingInstruction;
        final JPAQuery<Long> query = new JPAQuery<>(entityManager);

        query.select(ONE.count()).from(qAccountTransferStandingInstruction);
        addJoinsToStandingInstructionQuery(query);
        return query;
    }

    private <T> BooleanExpression eq(final SimpleExpression<T> expression, final T value) {
        return value == null ? expression.isNull() : expression.eq(value);
    }

    private BooleanExpression addCondition(final BooleanExpression whereClause, final BooleanExpression condition) {
        if (whereClause == null) {
            return condition;
        } else {
            return whereClause.and(condition);
        }
    }
}
