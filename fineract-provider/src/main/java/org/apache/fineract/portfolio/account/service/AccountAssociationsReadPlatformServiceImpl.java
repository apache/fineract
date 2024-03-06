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

import static java.util.stream.Collectors.toList;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.account.data.AccountAssociationsData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.account.domain.QAccountAssociations;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.QLoan;
import org.apache.fineract.portfolio.savings.domain.QSavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class AccountAssociationsReadPlatformServiceImpl implements AccountAssociationsReadPlatformService {

    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public PortfolioAccountData retriveLoanLinkedAssociation(final Long loanId) {
        final QAccountAssociations qAccountAssociations = QAccountAssociations.accountAssociations;
        final JPAQuery<Tuple> query = getAccountAssociationsSelectQuery();
        final Tuple queryResult = query
                .where(eq(qAccountAssociations.loanAccount.id, loanId)
                        .and(qAccountAssociations.associationType.eq(AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue())))
                .fetchOne();
        if (queryResult != null) {
            final AccountAssociationsData accountAssociationsData = mapQueryResultToAccountAssociationsData(queryResult);
            return accountAssociationsData.linkedAccount();
        }
        log.debug("Linking account is not configured");
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Collection<AccountAssociationsData> retriveLoanAssociations(final Long loanId, final Integer associationType) {
        final QAccountAssociations qAccountAssociations = QAccountAssociations.accountAssociations;
        final JPAQuery<Tuple> query = getAccountAssociationsSelectQuery();

        final List<Tuple> queryResult = query
                .where(eq(qAccountAssociations.loanAccount.id, loanId).and(eq(qAccountAssociations.associationType, associationType)))
                .fetch();
        return queryResult.isEmpty() ? Collections.emptyList() : mapQueryResultToAccountAssociationsDataList(queryResult);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public PortfolioAccountData retriveSavingsLinkedAssociation(final Long savingsId) {
        final QAccountAssociations qAccountAssociations = QAccountAssociations.accountAssociations;
        final JPAQuery<Tuple> query = getAccountAssociationsSelectQuery();

        final Tuple queryResult = query
                .where(eq(qAccountAssociations.savingsAccount.id, savingsId)
                        .and(qAccountAssociations.associationType.eq(AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue())))
                .fetchOne();
        if (queryResult != null) {
            final AccountAssociationsData accountAssociationsData = mapQueryResultToAccountAssociationsData(queryResult);
            return accountAssociationsData.linkedAccount();
        }
        log.debug("Linking account is not configured");
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean isLinkedWithAnyActiveAccount(final Long savingsId) {
        boolean hasActiveAccount = false;

        final QAccountAssociations qAccountAssociations = QAccountAssociations.accountAssociations;
        final QLoan qLoan = QLoan.loan;
        final QSavingsAccount qSavingsAccount = QSavingsAccount.savingsAccount;

        final JPAQuery<Map<String, Object>> query = new JPAQuery<>(entityManager);
        query.select(qAccountAssociations.active, qAccountAssociations.associationType, qLoan.loanStatus,
                qSavingsAccount.status.as("savingsStatus")).from(qAccountAssociations).leftJoin(qAccountAssociations.loanAccount, qLoan)
                .on(qLoan.id.eq(qAccountAssociations.loanAccount.id)).leftJoin(qAccountAssociations.savingsAccount, qSavingsAccount)
                .on(qSavingsAccount.id.eq(qAccountAssociations.savingsAccount.id))
                .where(eq(qAccountAssociations.linkedSavingsAccount.id, savingsId));

        List<Map<String, Object>> statusList = query.fetch();

        for (final Map<String, Object> statusMap : statusList) {
            AccountAssociationType associationType = AccountAssociationType.fromInt((Integer) statusMap.get("type"));
            if (!associationType.isLinkedAccountAssociation() && (Boolean) statusMap.get("active")) {
                hasActiveAccount = true;
                break;
            }

            if (statusMap.get("loanStatus") != null) {
                final LoanStatus loanStatus = LoanStatus.fromInt((Integer) statusMap.get("loanStatus"));
                if (loanStatus.isActiveOrAwaitingApprovalOrDisbursal() || loanStatus.isUnderTransfer()) {
                    hasActiveAccount = true;
                    break;
                }
            }

            if (statusMap.get("savingsStatus") != null) {
                final SavingsAccountStatusType saveStatus = SavingsAccountStatusType.fromInt((Integer) statusMap.get("savingsStatus"));
                if (saveStatus.isActiveOrAwaitingApprovalOrDisbursal() || saveStatus.isUnderTransfer()) {
                    hasActiveAccount = true;
                    break;
                }
            }
        }

        return hasActiveAccount;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public PortfolioAccountData retriveSavingsAccount(final Long savingsId) {
        final QSavingsAccount qSavingsAccount = QSavingsAccount.savingsAccount;
        final JPAQuery<String> query = new JPAQuery<>(entityManager);
        final String accountNo = query.select(qSavingsAccount.accountNumber).from(qSavingsAccount).where(eq(qSavingsAccount.id, savingsId))
                .fetchOne();
        return PortfolioAccountData.lookup(savingsId, accountNo);
    }

    private JPAQuery<Tuple> getAccountAssociationsSelectQuery() {
        final QAccountAssociations qAccountAssociations = QAccountAssociations.accountAssociations;
        final QLoan qLoan = QLoan.loan;
        final QSavingsAccount qSavingsAccount = QSavingsAccount.savingsAccount;

        final JPAQuery<Tuple> query = new JPAQuery<>(entityManager);
        query.select(qAccountAssociations.id, qLoan.id.as("loanAccountId"), qLoan.accountNumber.as("loanAccountNo"),
                qSavingsAccount.id.as("linkSavingsAccountId"), qSavingsAccount.accountNumber.as("linkSavingsAccountNo"))
                .from(qAccountAssociations).leftJoin(qAccountAssociations.loanAccount, qLoan)
                .on(qLoan.id.eq(qAccountAssociations.loanAccount.id)).leftJoin(qAccountAssociations.linkedSavingsAccount, qSavingsAccount)
                .on(qSavingsAccount.id.eq(qAccountAssociations.linkedSavingsAccount.id));

        return query;
    }

    private AccountAssociationsData mapQueryResultToAccountAssociationsData(final Tuple queryResult) {
        final QAccountAssociations qAccountAssociations = QAccountAssociations.accountAssociations;
        final QLoan qLoan = QLoan.loan;
        final QSavingsAccount qSavingsAccount = QSavingsAccount.savingsAccount;

        final PortfolioAccountData account = PortfolioAccountData.lookup(queryResult.get(qLoan.id.as("loanAccountId")),
                queryResult.get(qLoan.accountNumber.as("loanAccountNo")));
        final PortfolioAccountData linkedAccount = PortfolioAccountData.lookup(
                queryResult.get(qSavingsAccount.id.as("linkSavingsAccountId")),
                queryResult.get(qSavingsAccount.accountNumber.as("linkSavingsAccountNo")));

        return new AccountAssociationsData(queryResult.get(qAccountAssociations.id), account, linkedAccount);
    }

    private List<AccountAssociationsData> mapQueryResultToAccountAssociationsDataList(final List<Tuple> queryResult) {
        return queryResult.stream().map(this::mapQueryResultToAccountAssociationsData).collect(toList());
    }

    private <T> BooleanExpression eq(final SimpleExpression<T> expression, final T value) {
        return value == null ? expression.isNull() : expression.eq(value);
    }
}
