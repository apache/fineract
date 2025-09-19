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
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionRelationData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanTransactionRelationMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanTransactionRelationReadService {

    @PersistenceContext
    private EntityManager entityManager;

    private final LoanTransactionRelationMapper loanTransactionRelationMapper;

    public List<LoanTransactionRelationData> fetchLoanTransactionRelationDataFrom(final Long transactionId) {
        final List<Long> transactionIds = Arrays.asList(transactionId);
        return fetchLoanTransactionRelationFrom(transactionIds).stream().filter(this::shouldIncludeRelation)
                .map(loanTransactionRelationMapper::map).toList();
    }

    public List<LoanTransactionRelationData> fetchLoanTransactionRelationDataFrom(final List<Long> transactionIds) {
        return fetchLoanTransactionRelationFrom(transactionIds).stream().filter(this::shouldIncludeRelation)
                .map(loanTransactionRelationMapper::map).toList();
    }

    public List<LoanTransactionRelation> fetchLoanTransactionRelationFrom(final List<Long> transactionIds) {

        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<LoanTransactionRelation> query = cb.createQuery(LoanTransactionRelation.class);

        final Root<LoanTransactionRelation> root = query.from(LoanTransactionRelation.class);
        root.fetch("fromTransaction", JoinType.INNER);
        final Path<LoanTransaction> fromTransaction = root.join("fromTransaction", JoinType.INNER);

        query.select(root).where(fromTransaction.get("id").in(transactionIds));

        final List<Order> orders = new ArrayList<>();
        orders.add(cb.desc(root.get("id")));
        query.orderBy(orders);

        final TypedQuery<LoanTransactionRelation> queryToExecute = entityManager.createQuery(query);
        return queryToExecute.getResultList();
    }

    /**
     * Determines if a transaction relation should be included in the API response.
     *
     * Only includes relations that represent legitimate business operations visible to API consumers. Filters out
     * internal processing relations that are not relevant to external users.
     *
     * @param relation
     *            the transaction relation to evaluate
     * @return true if the relation should be included, false otherwise
     */
    private boolean shouldIncludeRelation(LoanTransactionRelation relation) {
        LoanTransactionRelationTypeEnum relationType = relation.getRelationType();
        LoanTransaction fromTransaction = relation.getFromTransaction();
        LoanTransaction toTransaction = relation.getToTransaction();

        // Only include relations that represent legitimate business operations visible to API consumers
        switch (relationType) {
            case CHARGEBACK:
                // Always include chargeback relations as they represent user-visible business operations
                return true;
            case CHARGE_ADJUSTMENT:
                // Include charge adjustment relations as they represent user-visible business operations
                return true;
            case ADJUSTMENT:
                // Include adjustment relations as they represent user-visible business operations
                return true;
            case REPLAYED:
                // Include REPLAYED relations for charged-off loans as they are important for tracking
                // backdated transaction processing on charged-off loans
                // Check if either transaction is on a charged-off loan
                if (fromTransaction != null && fromTransaction.getLoan() != null && fromTransaction.getLoan().isChargedOff()) {
                    return true;
                }
                if (toTransaction != null && toTransaction.getLoan() != null && toTransaction.getLoan().isChargedOff()) {
                    return true;
                }
                // Filter out other REPLAYED relations as they are created during internal processing
                return false;
            case RELATED:
                // Filter out RELATED relations as they are typically created during internal processing
                return false;
            default:
                // Filter out unknown relation types by default to be safe
                return false;
        }
    }

}
