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
package org.apache.fineract.portfolio.account.domain;

import static java.util.stream.Collectors.toList;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.account.data.AccountAssociationsData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.QLoan;
import org.apache.fineract.portfolio.savings.domain.QSavingsAccount;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountAssociationsCustomRepositoryImpl implements AccountAssociationsCustomRepository {

    private final EntityManager entityManager;

    @Override
    public AccountAssociationsData retrieveLoanLinkedAssociation(final Long loanId) {
        final QAccountAssociations qAccountAssociations = QAccountAssociations.accountAssociations;
        final JPAQuery<Tuple> query = getAccountAssociationsSelectQuery();
        final Tuple queryResult = query
                .where(eq(qAccountAssociations.loanAccount.id, loanId)
                        .and(qAccountAssociations.associationType.eq(AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue())))
                .fetchOne();

        return queryResult != null ? mapQueryResultToAccountAssociationsData(queryResult) : null;
    }

    @Override
    public AccountAssociationsData retrieveSavingsLinkedAssociation(final Long savingsId) {
        final QAccountAssociations qAccountAssociations = QAccountAssociations.accountAssociations;
        final JPAQuery<Tuple> query = getAccountAssociationsSelectQuery();
        final Tuple queryResult = query
                .where(eq(qAccountAssociations.savingsAccount.id, savingsId)
                        .and(qAccountAssociations.associationType.eq(AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue())))
                .fetchOne();

        return queryResult != null ? mapQueryResultToAccountAssociationsData(queryResult) : null;
    }

    @Override
    public Collection<AccountAssociationsData> retrieveLoanAssociations(final Long loanId, final Integer associationType) {
        final QAccountAssociations qAccountAssociations = QAccountAssociations.accountAssociations;
        final JPAQuery<Tuple> query = getAccountAssociationsSelectQuery();
        final List<Tuple> queryResult = query
                .where(eq(qAccountAssociations.loanAccount.id, loanId).and(eq(qAccountAssociations.associationType, associationType)))
                .fetch();
        return queryResult.isEmpty() ? Collections.emptyList() : mapQueryResultToAccountAssociationsDataList(queryResult);
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
