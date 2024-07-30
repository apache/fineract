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
package org.apache.fineract.portfolio.loanaccount.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanTransactionReadService {

    private final EntityManager entityManager;

    public List<LoanTransaction> fetchLoanTransactionsByType(final Long loanId, final String externalId, final Integer transactionType) {
        final List<Integer> transactionTypes = new ArrayList<>();
        transactionTypes.add(transactionType);
        return fetchLoanTransactionsByTypes(loanId, externalId, transactionTypes);
    }

    public List<LoanTransaction> fetchLoanTransactionsByTypes(final Long loanId, final String externalId,
            final List<Integer> transactionTypes) {

        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<LoanTransaction> query = cb.createQuery(LoanTransaction.class);

        final Root<LoanTransaction> root = query.from(LoanTransaction.class);
        root.fetch("loan", JoinType.INNER);
        final Path<Loan> loan = root.join("loan", JoinType.INNER);

        Predicate loanPredicate = cb.equal(loan.get("id"), loanId);
        if (externalId != null) {
            loanPredicate = cb.equal(loan.get("externalId"), externalId);
        }

        query.select(root)
                .where(cb.and(loanPredicate, root.get("typeOf").in(transactionTypes), cb.equal(root.get("reversed"), Boolean.FALSE)));

        final List<Order> orders = new ArrayList<>();
        orders.add(cb.desc(root.get("dateOf")));
        orders.add(cb.desc(root.get("createdDate")));
        orders.add(cb.desc(root.get("id")));
        query.orderBy(orders);

        final TypedQuery<LoanTransaction> queryToExecute = entityManager.createQuery(query);
        return queryToExecute.getResultList();
    }

}
