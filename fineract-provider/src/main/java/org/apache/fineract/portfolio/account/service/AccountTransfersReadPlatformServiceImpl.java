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

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.monetary.domain.QApplicationCurrency;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.domain.QOffice;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.QAccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.QAccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.QAccountTransferTransaction;
import org.apache.fineract.portfolio.account.exception.AccountTransferNotFoundException;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.domain.QClient;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.QLoan;
import org.apache.fineract.portfolio.loanaccount.domain.QLoanTransaction;
import org.apache.fineract.portfolio.savings.domain.QSavingsAccount;
import org.apache.fineract.portfolio.savings.domain.QSavingsAccountTransaction;
import org.springframework.util.CollectionUtils;

@AllArgsConstructor
public class AccountTransfersReadPlatformServiceImpl implements AccountTransfersReadPlatformService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    // pagination
    private final PaginationHelper paginationHelper;
    private final EntityManager entityManager;

    @Override
    public AccountTransferData retrieveTemplate(final Long fromOfficeId, final Long fromClientId, final Long fromAccountId,
            final Integer fromAccountType, final Long toOfficeId, final Long toClientId, final Long toAccountId,
            final Integer toAccountType) {

        final EnumOptionData loanAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.LOAN);
        final EnumOptionData savingsAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.SAVINGS);

        final Collection<EnumOptionData> fromAccountTypeOptions = Arrays.asList(savingsAccountType, loanAccountType);
        final Collection<EnumOptionData> toAccountTypeOptions;
        if (fromAccountType != null && fromAccountType == 1) {
            // overpaid loan amt transfer to savings account
            toAccountTypeOptions = Collections.singletonList(savingsAccountType);
        } else {
            toAccountTypeOptions = Arrays.asList(loanAccountType, savingsAccountType);
        }

        final EnumOptionData fromAccountTypeData = AccountTransferEnumerations.accountType(fromAccountType);
        final EnumOptionData toAccountTypeData = AccountTransferEnumerations.accountType(toAccountType);

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

        Collection<OfficeData> fromOfficeOptions = null;
        Collection<ClientData> fromClientOptions = null;
        if (mostRelevantFromOfficeId != null) {
            fromOffice = this.officeReadPlatformService.retrieveOffice(mostRelevantFromOfficeId);
            fromOfficeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
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

        return AccountTransferData.template(fromOffice, fromClient, fromAccountTypeData, fromAccount, transferDate, toOffice, toClient,
                toAccountTypeData, toAccount, fromOfficeOptions, fromClientOptions, fromAccountTypeOptions, fromAccountOptions,
                toOfficeOptions, toClientOptions, toAccountTypeOptions, toAccountOptions);
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
    public Page<AccountTransferData> retrieveAll(final SearchParameters searchParameters, final Long accountDetailId) {
        final JPAQuery<AccountTransferData> query = getAccountTransferSelectQuery();
        final JPAQuery<Long> totalCountQuery = getAccountTransferCountQuery();

        if (accountDetailId != null) {
            query.where(QAccountTransferTransaction.accountTransferTransaction.accountTransferDetails.id.eq(accountDetailId));
            totalCountQuery.where(QAccountTransferTransaction.accountTransferTransaction.accountTransferDetails.id.eq(accountDetailId));
        }

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

        return this.paginationHelper.createPageFromItems(query.fetch(), Objects.requireNonNull(totalCountQuery.fetchOne()));
    }

    @Override
    public AccountTransferData retrieveOne(final Long transferId) {
        final QAccountTransferTransaction qAccountTransferTransaction = QAccountTransferTransaction.accountTransferTransaction;
        final JPAQuery<AccountTransferData> query = getAccountTransferSelectQuery().where(eq(qAccountTransferTransaction.id, transferId));
        return Optional.ofNullable(query.fetchOne()).orElseThrow(() -> new AccountTransferNotFoundException(transferId));
    }

    @Override
    public Collection<Long> fetchPostInterestTransactionIds(final Long accountId) {
        final QAccountTransferTransaction qAccountTransferTransaction = QAccountTransferTransaction.accountTransferTransaction;
        final QAccountTransferDetails qAccountTransferDetails = QAccountTransferDetails.accountTransferDetails;

        final JPAQuery<Long> query = new JPAQuery<>(entityManager);
        query.select(qAccountTransferTransaction.fromSavingsTransaction.id).from(qAccountTransferTransaction)
                .innerJoin(qAccountTransferTransaction.accountTransferDetails, qAccountTransferDetails)
                .on(qAccountTransferDetails.id.eq(qAccountTransferTransaction.accountTransferDetails.id))
                .where(qAccountTransferTransaction.reversed.eq(false)
                        .and(qAccountTransferDetails.transferType.eq(AccountTransferType.INTEREST_TRANSFER.getValue()))
                        .and(eq(qAccountTransferDetails.fromSavingsAccount.id, accountId)));

        return query.fetch();
    }

    @Override
    public Collection<Long> fetchPostInterestTransactionIdsWithPivotDate(final Long accountId, final LocalDate pivotDate) {
        final QAccountTransferTransaction qAccountTransferTransaction = QAccountTransferTransaction.accountTransferTransaction;
        final QAccountTransferDetails qAccountTransferDetails = QAccountTransferDetails.accountTransferDetails;

        final JPAQuery<Long> query = new JPAQuery<>(entityManager);

        return query.select(qAccountTransferTransaction.fromSavingsTransaction.id).from(qAccountTransferTransaction)
                .innerJoin(qAccountTransferTransaction.accountTransferDetails, qAccountTransferDetails)
                .on(qAccountTransferDetails.id.eq(qAccountTransferTransaction.accountTransferDetails.id))
                .where(eq(qAccountTransferDetails.fromSavingsAccount.id, accountId).and(qAccountTransferTransaction.reversed.eq(false))
                        .and(qAccountTransferDetails.transferType.eq(AccountTransferType.INTEREST_TRANSFER.getValue()))
                        .and(qAccountTransferTransaction.date.after(pivotDate.minusDays(1))))
                .fetch();
    }

    @Override
    public boolean isAccountTransfer(final Long transactionId, final PortfolioAccountType accountType) {
        final QAccountTransferTransaction qAccountTransferTransaction = QAccountTransferTransaction.accountTransferTransaction;
        final JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        final BooleanExpression predicate;
        if (accountType.isLoanAccount()) {
            predicate = eq(qAccountTransferTransaction.fromLoanTransaction.id, transactionId)
                    .or(eq(qAccountTransferTransaction.toLoanTransaction.id, transactionId));
        } else {
            predicate = eq(qAccountTransferTransaction.fromSavingsTransaction.id, transactionId)
                    .or(eq(qAccountTransferTransaction.toSavingsTransaction.id, transactionId));
        }

        final Long count = queryFactory.select(qAccountTransferTransaction.count()).from(qAccountTransferTransaction).where(predicate)
                .fetchOne();

        return count != null && count > 0;
    }

    @Override
    public Page<AccountTransferData> retrieveByStandingInstruction(final Long id, final SearchParameters searchParameters) {
        final QAccountTransferStandingInstruction qAccountTransferStandingInstruction = QAccountTransferStandingInstruction.accountTransferStandingInstruction;
        final JPAQuery<AccountTransferData> query = getAccountTransferSelectQuery().join(qAccountTransferStandingInstruction)
                .on(qAccountTransferStandingInstruction.accountTransferDetails.id
                        .eq(QAccountTransferTransaction.accountTransferTransaction.accountTransferDetails.id))
                .where(eq(qAccountTransferStandingInstruction.id, id));
        final JPAQuery<Long> totalCountQuery = getAccountTransferCountQuery().join(qAccountTransferStandingInstruction)
                .on(qAccountTransferStandingInstruction.accountTransferDetails.id
                        .eq(QAccountTransferTransaction.accountTransferTransaction.accountTransferDetails.id))
                .where(eq(qAccountTransferStandingInstruction.id, id));

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

        return this.paginationHelper.createPageFromItems(query.fetch(), Objects.requireNonNull(totalCountQuery.fetchOne()));
    }

    @Override
    public AccountTransferData retrieveRefundByTransferTemplate(final Long fromOfficeId, final Long fromClientId, final Long fromAccountId,
            final Integer fromAccountType, final Long toOfficeId, final Long toClientId, final Long toAccountId,
            final Integer toAccountType) {
        // TODO Auto-generated method stub
        final EnumOptionData loanAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.LOAN);
        final EnumOptionData savingsAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.SAVINGS);

        final Collection<EnumOptionData> fromAccountTypeOptions = Arrays.asList(savingsAccountType, loanAccountType);
        final Collection<EnumOptionData> toAccountTypeOptions;
        if (fromAccountType == 1) {
            // overpaid loan amt transfer to savings account
            toAccountTypeOptions = Collections.singletonList(savingsAccountType);
        } else {
            toAccountTypeOptions = Arrays.asList(loanAccountType, savingsAccountType);
        }

        final EnumOptionData fromAccountTypeData = AccountTransferEnumerations.accountType(fromAccountType);
        final EnumOptionData toAccountTypeData = AccountTransferEnumerations.accountType(toAccountType);

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
            fromAccount = this.portfolioAccountReadPlatformService.retrieveOneByPaidInAdvance(fromAccountId, accountType);

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

        Collection<OfficeData> fromOfficeOptions = null;
        Collection<ClientData> fromClientOptions = null;
        if (mostRelevantFromOfficeId != null) {
            fromOffice = this.officeReadPlatformService.retrieveOffice(mostRelevantFromOfficeId);
            fromOfficeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
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

        return AccountTransferData.template(fromOffice, fromClient, fromAccountTypeData, fromAccount, transferDate, toOffice, toClient,
                toAccountTypeData, toAccount, fromOfficeOptions, fromClientOptions, fromAccountTypeOptions, fromAccountOptions,
                toOfficeOptions, toClientOptions, toAccountTypeOptions, toAccountOptions);
    }

    @Override
    public BigDecimal getTotalTransactionAmount(Long accountId, Integer accountType, LocalDate transactionDate) {
        final QAccountTransferDetails qAccountTransferDetails = QAccountTransferDetails.accountTransferDetails;
        final QAccountTransferTransaction qAccountTransferTransaction = QAccountTransferTransaction.accountTransferTransaction;

        final JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        BooleanExpression condition = qAccountTransferTransaction.reversed.eq(false)
                .and(qAccountTransferTransaction.date.eq(LocalDate.parse(DATE_TIME_FORMATTER.format(transactionDate))));

        if (PortfolioAccountType.LOAN.getValue().equals(accountType)) {
            condition = condition.and(eq(qAccountTransferDetails.fromLoanAccount.id, accountId));
        } else {
            condition = condition.and(eq(qAccountTransferDetails.fromSavingsAccount.id, accountId));
        }

        final BigDecimal totalTransactionAmount = queryFactory.select(qAccountTransferTransaction.amount.sumBigDecimal())
                .from(qAccountTransferDetails).innerJoin(qAccountTransferTransaction)
                .on(qAccountTransferDetails.id.eq(qAccountTransferTransaction.accountTransferDetails.id)).where(condition).fetchOne();

        return totalTransactionAmount != null ? totalTransactionAmount : BigDecimal.ZERO;
    }

    private static final class OrderSpecifierFactory {

        @FunctionalInterface
        interface OrderSpecifierGenerator {

            OrderSpecifier<?> generate(Order order);
        }

        private static final Map<String, OrderSpecifierGenerator> fieldToSpecifier = new HashMap<>();

        static {
            fieldToSpecifier.put("id", order -> new OrderSpecifier<>(order, QAccountTransferTransaction.accountTransferTransaction.id));
            fieldToSpecifier.put("isReversed",
                    order -> new OrderSpecifier<>(order, QAccountTransferTransaction.accountTransferTransaction.reversed));
            fieldToSpecifier.put("transferDate",
                    order -> new OrderSpecifier<>(order, QAccountTransferTransaction.accountTransferTransaction.date));
            fieldToSpecifier.put("transferAmount",
                    order -> new OrderSpecifier<>(order, QAccountTransferTransaction.accountTransferTransaction.amount));
            fieldToSpecifier.put("transferDescription",
                    order -> new OrderSpecifier<>(order, QAccountTransferTransaction.accountTransferTransaction.description));
            fieldToSpecifier.put("currencyCode",
                    order -> new OrderSpecifier<>(order, QAccountTransferTransaction.accountTransferTransaction.currency.code));
            fieldToSpecifier.put("currencyDigits", order -> new OrderSpecifier<>(order,
                    QAccountTransferTransaction.accountTransferTransaction.currency.digitsAfterDecimal));
            fieldToSpecifier.put("inMultiplesOf",
                    order -> new OrderSpecifier<>(order, QAccountTransferTransaction.accountTransferTransaction.currency.inMultiplesOf));
            fieldToSpecifier.put("currencyName", order -> new OrderSpecifier<>(order, QApplicationCurrency.applicationCurrency.name));
            fieldToSpecifier.put("currencyNameCode",
                    order -> new OrderSpecifier<>(order, QApplicationCurrency.applicationCurrency.nameCode));
            fieldToSpecifier.put("currencyDisplaySymbol",
                    order -> new OrderSpecifier<>(order, QApplicationCurrency.applicationCurrency.displaySymbol));
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
            fieldToSpecifier.put("fromSavingsAccountTransactionId",
                    order -> new OrderSpecifier<>(order, new QSavingsAccountTransaction("fromSavingsTrans").id));
            fieldToSpecifier.put("fromSavingsAccountTransactionType",
                    order -> new OrderSpecifier<>(order, new QSavingsAccountTransaction("fromSavingsTrans").typeOf));
            fieldToSpecifier.put("toSavingsAccountTransactionId",
                    order -> new OrderSpecifier<>(order, new QSavingsAccountTransaction("toSavingsTrans").id));
            fieldToSpecifier.put("toSavingsAccountTransactionType",
                    order -> new OrderSpecifier<>(order, new QSavingsAccountTransaction("toSavingsTrans").typeOf));
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

    private JPAQuery<AccountTransferData> getAccountTransferSelectQuery() {
        final QAccountTransferTransaction qAccountTransferTransaction = QAccountTransferTransaction.accountTransferTransaction;
        final QApplicationCurrency qApplicationCurrency = QApplicationCurrency.applicationCurrency;
        final QOffice qFromOffice = new QOffice("fromOffice");
        final QOffice qToOffice = new QOffice("toOffice");
        final QClient qFromClient = new QClient("fromClient");
        final QClient qToClient = new QClient("toClient");
        final QSavingsAccount qFromSavingsAccount = new QSavingsAccount("fromSavings");
        final QSavingsAccount qToSavingsAccount = new QSavingsAccount("toSavings");
        final QLoan qFromLoan = new QLoan("fromLoan");
        final QLoan qToLoan = new QLoan("toLoan");
        final QSavingsAccountTransaction qFromSavingsAccountTransaction = new QSavingsAccountTransaction("fromSavingsTrans");
        final QSavingsAccountTransaction qToSavingsAccountTransaction = new QSavingsAccountTransaction("toSavingsTrans");

        JPAQuery<AccountTransferData> query = new JPAQuery<>(entityManager);

        query.select(qAccountTransferTransaction.id, qAccountTransferTransaction.reversed.as("isReversed"),
                qAccountTransferTransaction.date.as("transferDate"), qAccountTransferTransaction.amount.as("transferAmount"),
                qAccountTransferTransaction.description.as("transferDescription"),
                qAccountTransferTransaction.currency.code.as("currencyCode"),
                qAccountTransferTransaction.currency.digitsAfterDecimal.as("currencyDigits"),
                qAccountTransferTransaction.currency.inMultiplesOf, qApplicationCurrency.name.as("currencyName"),
                qApplicationCurrency.nameCode.as("currencyNameCode"), qApplicationCurrency.displaySymbol.as("currencyDisplaySymbol"),
                qFromOffice.id.as("fromOfficeId"), qFromOffice.name.as("fromOfficeName"), qToOffice.id.as("toOfficeId"),
                qToOffice.name.as("toOfficeName"), qFromClient.id.as("fromClientId"), qFromClient.displayName.as("fromClientName"),
                qToClient.id.as("toClientId"), qToClient.displayName.as("toClientName"), qFromSavingsAccount.id.as("fromSavingsAccountId"),
                qFromSavingsAccount.accountNumber.as("fromSavingsAccountNo"), qFromLoan.id.as("fromLoanAccountId"),
                qFromLoan.accountNumber.as("fromLoanAccountNo"), qToSavingsAccount.id.as("toSavingsAccountId"),
                qToSavingsAccount.accountNumber.as("toSavingsAccountNo"), qToLoan.id.as("toLoanAccountId"),
                qToLoan.accountNumber.as("toLoanAccountNo"), qFromSavingsAccountTransaction.id.as("fromSavingsAccountTransactionId"),
                qFromSavingsAccountTransaction.typeOf.as("fromSavingsAccountTransactionType"),
                qToSavingsAccountTransaction.id.as("toSavingsAccountTransactionId"),
                qToSavingsAccountTransaction.typeOf.as("toSavingsAccountTransactionType")).from(qAccountTransferTransaction);

        addJoinsToAccountTransferQuery(query);
        return query;
    }

    private void addJoinsToAccountTransferQuery(final JPAQuery<?> selectFromQuery) {
        final QAccountTransferTransaction qAccountTransferTransaction = QAccountTransferTransaction.accountTransferTransaction;
        final QAccountTransferDetails qAccountTransferDetails = QAccountTransferDetails.accountTransferDetails;
        final QApplicationCurrency qApplicationCurrency = QApplicationCurrency.applicationCurrency;
        final QOffice qFromOffice = new QOffice("fromOffice");
        final QOffice qToOffice = new QOffice("toOffice");
        final QClient qFromClient = new QClient("fromClient");
        final QClient qToClient = new QClient("toClient");
        final QSavingsAccount qFromSavingsAccount = new QSavingsAccount("fromSavings");
        final QSavingsAccount qToSavingsAccount = new QSavingsAccount("toSavings");
        final QLoan qFromLoan = new QLoan("fromLoan");
        final QLoan qToLoan = new QLoan("toLoan");
        final QSavingsAccountTransaction qFromSavingsAccountTransaction = new QSavingsAccountTransaction("fromSavingsTrans");
        final QSavingsAccountTransaction qToSavingsAccountTransaction = new QSavingsAccountTransaction("toSavingsTrans");
        final QLoanTransaction qFromLoanTransaction = new QLoanTransaction("fromLoanTrans");
        final QLoanTransaction qToLoanTransaction = new QLoanTransaction("toLoanTrans");

        selectFromQuery.leftJoin(qAccountTransferTransaction.accountTransferDetails, qAccountTransferDetails)
                .on(qAccountTransferDetails.id.eq(qAccountTransferTransaction.accountTransferDetails.id)).join(qApplicationCurrency)
                .on(qApplicationCurrency.code.eq(qAccountTransferTransaction.currency.code))
                .join(qAccountTransferDetails.fromOffice, qFromOffice).on(qFromOffice.id.eq(qAccountTransferDetails.fromOffice.id))
                .join(qAccountTransferDetails.toOffice, qToOffice).on(qToOffice.id.eq(qAccountTransferDetails.toOffice.id))
                .join(qAccountTransferDetails.fromClient, qFromClient).on(qFromClient.id.eq(qAccountTransferDetails.fromClient.id))
                .join(qAccountTransferDetails.toClient, qToClient).on(qToClient.id.eq(qAccountTransferDetails.toClient.id))
                .join(qAccountTransferDetails.fromSavingsAccount, qFromSavingsAccount)
                .on(qFromSavingsAccount.id.eq(qAccountTransferDetails.fromSavingsAccount.id))
                .join(qAccountTransferDetails.fromLoanAccount, qFromLoan).on(qFromLoan.id.eq(qAccountTransferDetails.fromLoanAccount.id))
                .join(qAccountTransferDetails.toSavingsAccount, qToSavingsAccount)
                .on(qToSavingsAccount.id.eq(qAccountTransferDetails.toSavingsAccount.id))
                .join(qAccountTransferDetails.toLoanAccount, qToLoan).on(qToLoan.id.eq(qAccountTransferDetails.toLoanAccount.id))
                .join(qAccountTransferTransaction.fromSavingsTransaction, qFromSavingsAccountTransaction)
                .on(qFromSavingsAccountTransaction.id.eq(qAccountTransferTransaction.fromSavingsTransaction.id))
                .join(qAccountTransferTransaction.toSavingsTransaction, qToSavingsAccountTransaction)
                .on(qToSavingsAccountTransaction.id.eq(qAccountTransferTransaction.toSavingsTransaction.id))
                .join(qAccountTransferTransaction.fromLoanTransaction, qFromLoanTransaction)
                .on(qFromLoanTransaction.id.eq(qAccountTransferTransaction.fromLoanTransaction.id))
                .join(qAccountTransferTransaction.toLoanTransaction, qToLoanTransaction)
                .on(qToLoanTransaction.id.eq(qAccountTransferTransaction.toLoanTransaction.id));
    }

    private JPAQuery<Long> getAccountTransferCountQuery() {
        final QAccountTransferTransaction qAccountTransferTransaction = QAccountTransferTransaction.accountTransferTransaction;
        final JPAQuery<Long> query = new JPAQuery<>(entityManager);

        query.select(ONE.count()).from(qAccountTransferTransaction);
        addJoinsToAccountTransferQuery(query);
        return query;
    }

    private <T> BooleanExpression eq(final SimpleExpression<T> expression, final T value) {
        return value == null ? expression.isNull() : expression.eq(value);
    }
}
