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

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.QApplicationCurrency;
import org.apache.fineract.organisation.staff.domain.QStaff;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.exception.AccountTransferNotFoundException;
import org.apache.fineract.portfolio.client.domain.QClient;
import org.apache.fineract.portfolio.group.domain.QGroup;
import org.apache.fineract.portfolio.loanaccount.domain.QLoan;
import org.apache.fineract.portfolio.loanaccount.domain.QLoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanproduct.domain.QLoanProduct;
import org.apache.fineract.portfolio.savings.domain.QSavingsAccount;
import org.apache.fineract.portfolio.savings.domain.QSavingsProduct;

@AllArgsConstructor
public class PortfolioAccountReadPlatformServiceImpl implements PortfolioAccountReadPlatformService {

    private final EntityManager entityManager;

    @Override
    public PortfolioAccountData retrieveOne(final Long accountId, final Integer accountTypeId) {
        return retrieveOne(accountId, accountTypeId, null);
    }

    @Override
    public PortfolioAccountData retrieveOne(final Long accountId, final Integer accountTypeId, final String currencyCode) {

        final PortfolioAccountData accountData;
        final PortfolioAccountType accountType = PortfolioAccountType.fromInt(accountTypeId);
        accountData = switch (accountType) {
            case INVALID -> null;
            case LOAN -> {
                final QLoan qLoan = QLoan.loan;
                final JPAQuery<PortfolioAccountData> loanQuery = getLoanPortfolioAccountSelectQuery();
                BooleanExpression loanPredicate = eq(qLoan.id, accountId);
                if (currencyCode != null) {
                    loanPredicate = loanPredicate.and(qLoan.loanRepaymentScheduleDetail.currency.code.eq(currencyCode));
                }
                yield loanQuery.where(loanPredicate).fetchOne();
            }
            case SAVINGS -> {
                final QSavingsAccount qSavingsAccount = QSavingsAccount.savingsAccount;
                final JPAQuery<PortfolioAccountData> savingsQuery = getSavingsPortfolioAccountSelectQuery();
                BooleanExpression savingsPredicate = eq(qSavingsAccount.id, accountId);
                if (currencyCode != null) {
                    savingsPredicate = savingsPredicate.and(qSavingsAccount.currency.code.eq(currencyCode));
                }
                yield savingsQuery.where(savingsPredicate).fetchOne();
            }
        };

        return Optional.ofNullable(accountData).orElseThrow(() -> new AccountTransferNotFoundException(accountId));
    }

    @Override
    public Collection<PortfolioAccountData> retrieveAllForLookup(final PortfolioAccountDTO portfolioAccountDTO) {

        Collection<PortfolioAccountData> accounts = null;
        long defaultAccountStatus = 300; // Active Status
        if (portfolioAccountDTO.getAccountStatus() != null) {
            defaultAccountStatus = portfolioAccountDTO.getFirstAccountStatus();
        }
        final PortfolioAccountType accountType = PortfolioAccountType.fromInt(portfolioAccountDTO.getAccountTypeId());
        accounts = switch (accountType) {
            case INVALID -> Collections.emptyList();
            case LOAN -> {
                final QLoan qLoan = QLoan.loan;
                JPAQuery<PortfolioAccountData> loanQuery = getLoanPortfolioAccountSelectQuery();
                BooleanExpression loanPredicate = qLoan.loanStatus.in(defaultAccountStatus);
                if (portfolioAccountDTO.getClientId() != null) {
                    loanPredicate = loanPredicate.and(qLoan.client.id.eq(portfolioAccountDTO.getClientId()));
                }
                if (portfolioAccountDTO.getCurrencyCode() != null) {
                    loanPredicate = loanPredicate
                            .and(qLoan.loanRepaymentScheduleDetail.currency.code.eq(portfolioAccountDTO.getCurrencyCode()));
                }
                yield loanQuery.where(loanPredicate).fetch();
            }
            case SAVINGS -> {
                final QSavingsAccount qSavingsAccount = QSavingsAccount.savingsAccount;
                JPAQuery<PortfolioAccountData> savingsQuery = getSavingsPortfolioAccountSelectQuery();
                BooleanExpression savingsPredicate = qSavingsAccount.status.in(defaultAccountStatus);
                if (portfolioAccountDTO.getClientId() != null) {
                    savingsPredicate = savingsPredicate.and(qSavingsAccount.client.id.eq(portfolioAccountDTO.getClientId()));
                } else if (portfolioAccountDTO.getGroupId() != null) {
                    savingsPredicate = savingsPredicate.and(qSavingsAccount.group.id.eq(portfolioAccountDTO.getGroupId()));
                }
                if (portfolioAccountDTO.getCurrencyCode() != null) {
                    savingsPredicate = savingsPredicate.and(qSavingsAccount.currency.code.eq(portfolioAccountDTO.getCurrencyCode()));
                }
                if (portfolioAccountDTO.getDepositType() != null) {
                    savingsPredicate = savingsPredicate.and(qSavingsAccount.depositType.eq(portfolioAccountDTO.getDepositType()));
                }
                if (portfolioAccountDTO.isExcludeOverDraftAccounts()) {
                    savingsPredicate = savingsPredicate.and(qSavingsAccount.allowOverdraft.isFalse());
                }
                yield savingsQuery.where(savingsPredicate).fetch();
            }
        };

        return accounts;
    }

    @Override
    public PortfolioAccountData retrieveOneByPaidInAdvance(Long accountId, Integer accountTypeId) {
        // TODO Auto-generated method stub
        // final PortfolioAccountType accountType =
        // PortfolioAccountType.fromInt(accountTypeId);

        final QLoan qLoan = QLoan.loan;
        final QLoan qLoanSubQuery = new QLoan("loanSubQuery");
        final QLoanRepaymentScheduleInstallment qLoanRepaymentSchedule = QLoanRepaymentScheduleInstallment.loanRepaymentScheduleInstallment;
        final QLoanProduct qLoanProduct = QLoanProduct.loanProduct;
        final QApplicationCurrency qCurrency = QApplicationCurrency.applicationCurrency;
        final QClient qClient = QClient.client;
        final QGroup qGroup = QGroup.group;
        final QStaff qStaff = QStaff.staff;

        final JPAQuery<PortfolioAccountData> mainQuery = new JPAQuery<>();
        mainQuery
                .select(qLoan.id.as("id"), qLoan.accountNumber.as("accountNo"), qLoan.externalId.as("externalId"),
                        qClient.id.as("clientId"), qClient.displayName.as("clientName"), qGroup.id.as("groupId"),
                        qGroup.name.as("groupName"), qLoanProduct.id.as("productId"), qLoanProduct.name.as("productName"),
                        qStaff.id.as("fieldOfficerId"), qStaff.displayName.as("fieldOfficerName"),
                        qLoan.loanRepaymentScheduleDetail.currency.code.as("currencyCode"),
                        qLoan.loanRepaymentScheduleDetail.currency.digitsAfterDecimal.as("currencyDigits"),
                        qLoan.loanRepaymentScheduleDetail.currency.inMultiplesOf.as("inMultiplesOf"),
                        JPAExpressions
                                .select(qLoanRepaymentSchedule.principalCompleted.sumBigDecimal()
                                        .add(qLoanRepaymentSchedule.interestPaid.sumBigDecimal())
                                        .add(qLoanRepaymentSchedule.feeChargesPaid.sumBigDecimal())
                                        .add(qLoanRepaymentSchedule.penaltyChargesPaid.sumBigDecimal()).as("totalOverpaid"))
                                .from(qLoanSubQuery).join(qLoanRepaymentSchedule).on(qLoanSubQuery.id.eq(qLoanRepaymentSchedule.loan.id))
                                .where(qLoanRepaymentSchedule.loan.id.eq(qLoan.id).and(qLoan.loanStatus.eq(300))
                                        .and(qLoanRepaymentSchedule.dueDate.goe(LocalDate
                                                .parse(DateUtils.getBusinessLocalDate().format(DateUtils.DEFAULT_DATE_FORMATTER)))))
                                .groupBy(qLoanSubQuery.id)
                                .having(qLoanRepaymentSchedule.principalCompleted.sumBigDecimal()
                                        .add(qLoanRepaymentSchedule.interestPaid.sumBigDecimal())
                                        .add(qLoanRepaymentSchedule.feeChargesPaid.sumBigDecimal())
                                        .add(qLoanRepaymentSchedule.penaltyChargesPaid.sumBigDecimal()).gt(0.0)),
                        qCurrency.name.as("currencyName"), qCurrency.nameCode.as("currencyNameCode"),
                        qCurrency.displaySymbol.as("currencyDisplaySymbol"))
                .from(qLoan).join(qLoan.loanProduct, qLoanProduct).on(qLoanProduct.id.eq(qLoan.loanProduct.id)).join(qCurrency)
                .on(qCurrency.code.eq(qLoan.loanRepaymentScheduleDetail.currency.code)).leftJoin(qLoan.client, qClient)
                .on(qClient.id.eq(qLoan.client.id)).leftJoin(qLoan.group, qGroup).on(qGroup.id.eq(qLoan.group.id))
                .leftJoin(qLoan.loanOfficer, qStaff).on(qStaff.id.eq(qLoan.loanOfficer.id)).where(qLoan.id.eq(accountId));

        /*
         * if (currencyCode != null) { sql += " and la.currency_code = ?"; sqlParams = new Object[] {accountId ,
         * accountId,currencyCode }; }
         */

        return Optional.ofNullable(mainQuery.fetchOne()).orElseThrow(() -> new AccountTransferNotFoundException(accountId));
    }

    private JPAQuery<PortfolioAccountData> getLoanPortfolioAccountSelectQuery() {
        final QLoan qLoan = QLoan.loan;
        final QLoanProduct qLoanProduct = QLoanProduct.loanProduct;
        final QApplicationCurrency qApplicationCurrency = QApplicationCurrency.applicationCurrency;
        final QClient qClient = QClient.client;
        final QGroup qGroup = QGroup.group;
        final QStaff qStaff = QStaff.staff;

        final JPAQuery<PortfolioAccountData> loanQuery = new JPAQuery<>(entityManager);
        loanQuery
                .select(qLoan.id, qLoan.accountNumber.as("accountNo"), qLoan.externalId, qClient.id.as("clientId"),
                        qClient.displayName.as("clientName"), qGroup.id.as("groupId"), qGroup.name.as("groupName"),
                        qLoanProduct.id.as("productId"), qLoanProduct.name.as("productName"), qStaff.id.as("fieldOfficerId"),
                        qStaff.displayName.as("fieldOfficerName"), qLoan.loanRepaymentScheduleDetail.currency.code.as("currencyCode"),
                        qLoan.loanRepaymentScheduleDetail.currency.digitsAfterDecimal.as("currencyDigits"),
                        qLoan.loanRepaymentScheduleDetail.currency.inMultiplesOf, qLoan.totalOverpaid,
                        qApplicationCurrency.name.as("currencyName"), qApplicationCurrency.code.as("currencyNameCode"),
                        qApplicationCurrency.displaySymbol.as("currencyDisplaySymbol"))
                .from(qLoan).join(qLoan.loanProduct, qLoanProduct).on(qLoanProduct.id.eq(qLoan.loanProduct.id)).join(qApplicationCurrency)
                .on(qApplicationCurrency.code.eq(qLoan.loanRepaymentScheduleDetail.currency.code)).leftJoin(qLoan.client, qClient)
                .on(qClient.id.eq(qLoan.client.id)).leftJoin(qLoan.group, qGroup).on(qGroup.id.eq(qLoan.group.id))
                .leftJoin(qLoan.loanOfficer, qStaff).on(qStaff.id.eq(qLoan.loanOfficer.id));

        return loanQuery;
    }

    private JPAQuery<PortfolioAccountData> getSavingsPortfolioAccountSelectQuery() {
        final QSavingsAccount qSavingsAccount = QSavingsAccount.savingsAccount;
        final QSavingsProduct qSavingsProduct = QSavingsProduct.savingsProduct;
        final QApplicationCurrency qApplicationCurrency = QApplicationCurrency.applicationCurrency;
        final QClient qClient = QClient.client;
        final QGroup qGroup = QGroup.group;
        final QStaff qStaff = QStaff.staff;

        final JPAQuery<PortfolioAccountData> savingsQuery = new JPAQuery<>(entityManager);
        savingsQuery
                .select(qSavingsAccount.id, qSavingsAccount.accountNumber.as("accountNo"), qSavingsAccount.externalId,
                        qClient.id.as("clientId"), qClient.displayName.as("clientName"), qGroup.id.as("groupId"),
                        qGroup.name.as("groupName"), qSavingsProduct.id.as("productId"), qSavingsProduct.name.as("productName"),
                        qStaff.id.as("fieldOfficerId"), qStaff.displayName.as("fieldOfficerName"),
                        qSavingsAccount.currency.code.as("currencyCode"), qSavingsAccount.currency.digitsAfterDecimal.as("currencyDigits"),
                        qSavingsAccount.currency.inMultiplesOf, qApplicationCurrency.name.as("currencyName"),
                        qApplicationCurrency.code.as("currencyNameCode"), qApplicationCurrency.displaySymbol.as("currencyDisplaySymbol"))
                .from(qSavingsAccount).join(qSavingsProduct).on(qSavingsAccount.product.id.eq(qSavingsProduct.id))
                .join(qApplicationCurrency).on(qApplicationCurrency.code.eq(qSavingsAccount.currency.code))
                .leftJoin(qSavingsAccount.client, qClient).on(qClient.id.eq(qSavingsAccount.client.id))
                .leftJoin(qSavingsAccount.group, qGroup).on(qGroup.id.eq(qSavingsAccount.group.id))
                .leftJoin(qSavingsAccount.savingsOfficer, qStaff).on(qStaff.id.eq(qSavingsAccount.savingsOfficer.id));

        return savingsQuery;
    }

    private <T> BooleanExpression eq(final SimpleExpression<T> expression, final T value) {
        return value == null ? expression.isNull() : expression.eq(value);
    }
}
