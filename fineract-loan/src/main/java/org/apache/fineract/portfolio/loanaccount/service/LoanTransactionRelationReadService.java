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
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionRelationData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanTransactionRelationMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoanTransactionRelationReadService {

    private final EntityManager entityManager;
    private final LoanTransactionRelationMapper loanTransactionRelationMapper;

    public List<LoanTransactionRelationData> fetchLoanTransactionRelationDataFrom(final Long transactionId) {
        final List<Long> transactionIds = Arrays.asList(transactionId);
        return fetchLoanTransactionRelationFrom(transactionIds).stream().map(loanTransactionRelationMapper::map).toList();
    }

    public List<LoanTransactionRelationData> fetchLoanTransactionRelationDataFrom(final List<Long> transactionIds) {
        return fetchLoanTransactionRelationFrom(transactionIds).stream().map(loanTransactionRelationMapper::map).toList();
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

}
