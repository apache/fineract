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

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidByData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionRelationData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanTransactionMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanTransactionDataReadServiceImpl implements LoanTransactionDataReadService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanChargePaidByReadService loanChargePaidByReadService;
    private final LoanTransactionRelationReadService loanTransactionRelationReadService;
    private final LoanTransactionMapper loanTransactionMapper;

    @Override
    public Collection<LoanTransactionData> retrieveLoanTransactions(final Long loanId) {
        try {
            this.context.authenticatedUser();

            final LoanTransactionsMapper rm = new LoanTransactionsMapper(sqlGenerator);

            // retrieve all loan transactions that are not invalid and have not
            // been 'contra'ed by another transaction
            // repayments at time of disbursement (e.g. charges)

            /***
             * TODO Vishwas: Remove references to "Contra" from the codebase
             ***/
            final String sql = "select " + rm.loanPaymentsSchema() + " where tr.loan_id = ? and tr.transaction_type_enum not in (0, 3) "
                    + " and (tr.is_reversed=false or tr.manually_adjusted_or_reversed = true)  order by tr.transaction_date, tr.created_on_utc, tr.id ";
            Collection<LoanTransactionData> loanTransactionData = this.jdbcTemplate.query(sql, rm, loanId); // NOSONAR
            // TODO: would worth to rework in the future. It is not nice to fetch relations one by one... might worth to
            // give a try to get rid of native queries
            final List<Long> loanIds = loanTransactionData.stream().map(LoanTransactionData::getId).collect(Collectors.toList());
            final List<LoanTransactionRelationData> loanTransactionRelationDatas = loanTransactionRelationReadService
                    .fetchLoanTransactionRelationDataFrom(loanIds);
            final List<LoanChargePaidByData> loanChargePaidByDatas = loanChargePaidByReadService
                    .fetchLoanChargesPaidByDataTransactionId(loanIds);
            for (LoanTransactionData loanTransaction : loanTransactionData) {
                loanTransaction.setTransactionRelations(loanTransactionRelationDatas.stream().filter(
                        loanTransactionRelationData -> loanTransactionRelationData.getFromLoanTransaction().equals(loanTransaction.getId()))
                        .toList());
                loanTransaction.setLoanChargePaidByList(loanChargePaidByDatas.stream()
                        .filter(loanChargePaidByData -> loanChargePaidByData.getTransactionId().equals(loanTransaction.getId())).toList());
            }
            return loanTransactionData;
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public LoanTransactionData retrieveLoanTransaction(final Long loanId, final Long transactionId) {
        this.context.authenticatedUser();
        try {
            final LoanTransactionsMapper rm = new LoanTransactionsMapper(sqlGenerator);
            final String sql = "select " + rm.loanPaymentsSchema() + " where l.id = ? and tr.id = ? ";
            LoanTransactionData loanTransactionData = this.jdbcTemplate.queryForObject(sql, rm, loanId, transactionId); // NOSONAR
            loanTransactionData.setTransactionRelations(
                    loanTransactionRelationReadService.fetchLoanTransactionRelationDataFrom(loanTransactionData.getId()));
            return loanTransactionData;
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanTransactionNotFoundException(transactionId, e);
        }
    }

    @Override
    public Page<LoanTransactionData> retrieveLoanTransactions(@NonNull Long loanId, Set<LoanTransactionType> excludedTransactionTypes,
            Pageable pageable) {
        final org.springframework.data.domain.Page<LoanTransaction> transactionPage = loanTransactionRepository
                .findAll((root, query, builder) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    final Join<Object, Object> loanjoin = root.join("loan");
                    predicates.add(builder.equal(loanjoin.get("id"), loanId));

                    if (excludedTransactionTypes != null && !excludedTransactionTypes.isEmpty()) {
                        final List<LoanTransactionType> excludedTransactionTypeValues = excludedTransactionTypes.stream().toList();
                        predicates.add(builder.not(root.get("typeOf").in(excludedTransactionTypeValues)));
                    }
                    return builder.and(predicates.toArray(new Predicate[] {}));
                }, pageable);

        return transactionPage.map(loanTransactionMapper::mapLoanTransaction);
    }
}
