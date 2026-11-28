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
package org.apache.fineract.portfolio.savings.service;

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_DATE_DB_FIELD;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class SavingsAccountTransactionReadServiceImpl implements SavingsAccountTransactionReadService {

    private final JdbcTemplate jdbcTemplate;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final SavingsAccountTransactionTemplateMapper transactionTemplateMapper = new SavingsAccountTransactionTemplateMapper();
    private final SavingsAccountTransactionsMapper transactionsMapper = new SavingsAccountTransactionsMapper();
    private final SavingsAccountTransactionsForBatchMapper savingsAccountTransactionsForBatchMapper = new SavingsAccountTransactionsForBatchMapper();

    @Override
    public List<SavingsAccountTransactionData> retrieveAllTransactionData(final List<String> refNo) throws DataAccessException {
        String inSql = String.join(",", Collections.nCopies(refNo.size(), "?"));
        String sql = "select " + this.savingsAccountTransactionsForBatchMapper.schema() + " where tr.ref_no in (%s)";
        Object[] params = new Object[refNo.size()];
        int i = 0;
        for (String element : refNo) {
            params[i] = element;
            i++;
        }
        return this.jdbcTemplate.query(String.format(sql, inSql), this.savingsAccountTransactionsForBatchMapper, params);
    }

    @Override
    public SavingsAccountTransactionData retrieveDepositTransactionTemplate(final Long savingsId,
            final DepositAccountType depositAccountType) {

        try {
            final String sql = "select " + this.transactionTemplateMapper.schema() + " where sa.id = ? and sa.deposit_type_enum = ?";

            return this.jdbcTemplate.queryForObject(sql, this.transactionTemplateMapper, // NOSONAR
                    new Object[] { savingsId, depositAccountType.getValue() });
        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsAccountNotFoundException(savingsId, e);
        }
    }

    @Override
    public Collection<SavingsAccountTransactionData> retrieveAllTransactions(final Long savingsId, DepositAccountType depositAccountType) {
        // #audit backward compatibility
        final String sql = "select " + this.transactionsMapper.schema()
                + " where sa.id = ? and sa.deposit_type_enum = ? order by tr.transaction_date DESC," + " tr." + CREATED_DATE_DB_FIELD
                + " DESC, tr.created_date DESC, tr.id DESC";
        return this.jdbcTemplate.query(sql, this.transactionsMapper, new Object[] { savingsId, depositAccountType.getValue() }); // NOSONAR
    }

    @Override
    public SavingsAccountTransactionData retrieveSavingsTransaction(final Long savingsId, final Long transactionId,
            DepositAccountType depositAccountType) {

        final String sql = "select " + this.transactionsMapper.schema() + " where sa.id = ? and sa.deposit_type_enum = ? and tr.id= ?";

        try {
            return this.jdbcTemplate.queryForObject(sql, this.transactionsMapper, // NOSONAR
                    new Object[] { savingsId, depositAccountType.getValue(), transactionId });
        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsAccountTransactionNotFoundException(savingsId, transactionId, e);
        }
    }

    @Override
    public Long retrieveSavingsTransactionIdByExternalId(final ExternalId externalId) {
        return savingsAccountTransactionRepository.findIdByExternalId(externalId);
    }
}
