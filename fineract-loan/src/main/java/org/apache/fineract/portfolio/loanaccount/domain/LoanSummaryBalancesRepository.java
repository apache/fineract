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
package org.apache.fineract.portfolio.loanaccount.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.jpa.CriteriaQueryFactory;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionBalance;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LoanSummaryBalancesRepository {

    private final EntityManager entityManager;
    private final CriteriaQueryFactory criteriaQueryFactory;

    public Collection<LoanTransactionBalance> retrieveLoanSummaryBalancesByTransactionType(final Long loanId,
            final List<Integer> transactionTypes) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoanTransactionBalance> query = cb.createQuery(LoanTransactionBalance.class);

        Root<LoanTransaction> root = query.from(LoanTransaction.class);

        Specification<LoanTransaction> spec = (r, q, builder) -> {
            Path<Loan> la = r.get("loan");

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(la.get("id"), loanId)); // Loan
            predicates.add(r.get("typeOf").in(transactionTypes)); // Transaction Type

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        criteriaQueryFactory.applySpecificationToCriteria(root, spec, query);

        query.groupBy(root.get("typeOf"), root.get("reversed"), root.get("manuallyAdjustedOrReversed"));
        query.select(cb.construct(LoanTransactionBalance.class, root.get("typeOf"), root.get("reversed"),
                root.get("manuallyAdjustedOrReversed"), cb.sum(root.get("amount"))));

        TypedQuery<LoanTransactionBalance> queryToExecute = entityManager.createQuery(query);
        return queryToExecute.getResultList();
    }

}
