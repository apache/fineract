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
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.jpa.CriteriaQueryFactory;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionBalanceWithLoanId;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LoanSummaryBalancesRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final CriteriaQueryFactory criteriaQueryFactory;

    public Collection<LoanTransactionBalanceWithLoanId> retrieveLoanSummaryBalancesByTransactionType(final Long loanId,
            final List<LoanTransactionType> transactionTypes) {
        return retrieveLoanSummaryBalancesByTransactionType(Arrays.asList(loanId), transactionTypes).getOrDefault(loanId,
                new ArrayList<>());
    }

    public Map<Long, List<LoanTransactionBalanceWithLoanId>> retrieveLoanSummaryBalancesByTransactionType(final List<Long> loanIds,
            final List<LoanTransactionType> transactionTypes) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoanTransactionBalanceWithLoanId> query = cb.createQuery(LoanTransactionBalanceWithLoanId.class);

        Root<LoanTransaction> root = query.from(LoanTransaction.class);

        Specification<LoanTransaction> spec = (r, q, builder) -> {
            Path<Loan> la = r.get("loan");

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(la.get("id").in(loanIds)); // Loans
            predicates.add(r.get("typeOf").in(transactionTypes)); // Transaction Type

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        criteriaQueryFactory.applySpecificationToCriteria(root, spec, query);

        query.groupBy(root.get("typeOf"), root.get("reversed"), root.get("manuallyAdjustedOrReversed"), root.get("loan").get("id"));
        query.select(cb.construct(LoanTransactionBalanceWithLoanId.class, root.get("typeOf"), root.get("reversed"),
                root.get("manuallyAdjustedOrReversed"), cb.sum(root.get("amount")), root.get("loan").get("id")));

        TypedQuery<LoanTransactionBalanceWithLoanId> queryToExecute = entityManager.createQuery(query);
        Map<Long, List<LoanTransactionBalanceWithLoanId>> collect = queryToExecute.getResultList().stream()
                .collect(Collectors.groupingBy(LoanTransactionBalanceWithLoanId::getLoanId));
        return collect;
    }

}
