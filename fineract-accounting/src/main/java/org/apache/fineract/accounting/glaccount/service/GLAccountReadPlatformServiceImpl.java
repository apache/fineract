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
package org.apache.fineract.accounting.glaccount.service;

import static org.apache.fineract.query.QAccGlAccount.accGlAccount;
import static org.apache.fineract.query.QMCodeValue.mCodeValue;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;
import org.apache.fineract.accounting.glaccount.domain.GLAccountProjection;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.accounting.glaccount.exception.GLAccountInvalidClassificationException;
import org.apache.fineract.accounting.glaccount.exception.GLAccountNotFoundException;
import org.apache.fineract.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.query.QAccGlJournalEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GLAccountReadPlatformServiceImpl implements GLAccountReadPlatformService {

    private static final QAccGlJournalEntry BALANCE_ENTRY = new QAccGlJournalEntry("balanceEntry");
    private static final QAccGlJournalEntry CANDIDATE_ENTRY = new QAccGlJournalEntry("candidateEntry");
    private static final QAccGlJournalEntry CALCULATED_ENTRY = new QAccGlJournalEntry("calculatedEntry");
    private static final NumberExpression<Long> ORGANIZATION_RUNNING_BALANCE = Expressions.numberTemplate(Long.class, "{0}",
            BALANCE_ENTRY.organizationRunningBalance);

    private final JdbcTemplate jdbcTemplate;
    private final SQLQueryFactory queryFactory;

    @Override
    public List<GLAccountData> retrieveAllGLAccounts(final Integer accountClassification, final String searchParam, final Integer usage,
            final Boolean manualTransactionsAllowed, final Boolean disabled,
            final JournalEntryAssociationParametersData associationParametersData) {
        if (accountClassification != null && !checkValidGLAccountType(accountClassification)) {
            throw new GLAccountInvalidClassificationException(accountClassification);
        }

        if (usage != null && !checkValidGLAccountUsage(usage)) {
            throw new GLAccountInvalidClassificationException(accountClassification);
        }

        final boolean runningBalanceRequired = associationParametersData != null && associationParametersData.isRunningBalanceRequired();
        final SQLQuery<GLAccountData> query = baseQuery(runningBalanceRequired);
        if (accountClassification != null) {
            query.where(accGlAccount.classificationEnum.eq(accountClassification));
        }
        if (StringUtils.isNotBlank(searchParam)) {
            final String pattern = "%" + searchParam + "%";
            query.where(accGlAccount.name.like(pattern).or(accGlAccount.glCode.like(pattern)));
        }
        if (usage != null) {
            query.where(accGlAccount.accountUsage.eq(usage));
        }
        if (manualTransactionsAllowed != null) {
            query.where(accGlAccount.manualJournalEntriesAllowed.eq(manualTransactionsAllowed));
        }
        if (disabled != null) {
            query.where(accGlAccount.disabled.eq(disabled));
        }
        return query.orderBy(accGlAccount.glCode.asc()).fetch();
    }

    private SQLQuery<GLAccountData> baseQuery(final boolean runningBalanceRequired) {
        final SQLQuery<GLAccountData> query = queryFactory
                .select(new GLAccountProjection(runningBalanceRequired ? ORGANIZATION_RUNNING_BALANCE : null)).from(accGlAccount)
                .leftJoin(mCodeValue).on(accGlAccount.tagId.eq(mCodeValue.id));
        if (runningBalanceRequired) {
            final SQLQuery<Long> latestEligibleEntryId = SQLExpressions.select(CANDIDATE_ENTRY.id.max()).from(CANDIDATE_ENTRY).where(
                    CANDIDATE_ENTRY.accountId.eq(accGlAccount.id),
                    SQLExpressions.selectOne().from(CALCULATED_ENTRY).where(CALCULATED_ENTRY.accountId.eq(CANDIDATE_ENTRY.accountId),
                            CALCULATED_ENTRY.entryDate.eq(CANDIDATE_ENTRY.entryDate), CALCULATED_ENTRY.isRunningBalanceCalculated.isTrue())
                            .exists());
            query.leftJoin(BALANCE_ENTRY).on(BALANCE_ENTRY.accountId.eq(accGlAccount.id), BALANCE_ENTRY.id.eq(latestEligibleEntryId));
        }
        return query;
    }

    @Override
    public GLAccountData retrieveGLAccountById(final long glAccountId,
            final JournalEntryAssociationParametersData associationParametersData) {
        final boolean runningBalanceRequired = associationParametersData.isRunningBalanceRequired();
        final SQLQuery<GLAccountData> query = glAccountQuery(runningBalanceRequired).where(accGlAccount.id.eq(glAccountId));
        if (runningBalanceRequired) {
            query.leftJoin(BALANCE_ENTRY).on(BALANCE_ENTRY.accountId.eq(accGlAccount.id))
                    .where(BALANCE_ENTRY.isRunningBalanceCalculated.isTrue())
                    .orderBy(BALANCE_ENTRY.entryDate.desc(), BALANCE_ENTRY.id.desc()).limit(1);
        }
        final GLAccountData account = query.fetchOne();
        if (account == null) {
            throw new GLAccountNotFoundException(glAccountId);
        }
        return account;
    }

    private SQLQuery<GLAccountData> glAccountQuery(final boolean runningBalanceRequired) {
        return queryFactory.select(new GLAccountProjection(runningBalanceRequired ? ORGANIZATION_RUNNING_BALANCE : null)).from(accGlAccount)
                .leftJoin(mCodeValue).on(accGlAccount.tagId.eq(mCodeValue.id));
    }

    @Override
    public List<GLAccountData> retrieveAllEnabledDetailGLAccounts(final GLAccountType accountType) {
        return retrieveAllGLAccounts(accountType.getValue(), null, GLAccountUsage.DETAIL.getValue(), null, false,
                new JournalEntryAssociationParametersData());
    }

    @Override
    public List<GLAccountData> retrieveAllEnabledDetailGLAccounts() {
        return retrieveAllGLAccounts(null, null, GLAccountUsage.DETAIL.getValue(), null, false,
                new JournalEntryAssociationParametersData());
    }

    private static boolean checkValidGLAccountType(final int type) {
        for (final GLAccountType accountType : GLAccountType.values()) {
            if (accountType.getValue().equals(type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkValidGLAccountUsage(final int type) {
        for (final GLAccountUsage accountUsage : GLAccountUsage.values()) {
            if (accountUsage.getValue().equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GLAccountData retrieveNewGLAccountDetails(final Integer type) {
        return GLAccountData.sensibleDefaultsForNewGLAccountCreation(type);
    }

    @Override
    public List<GLAccountData> retrieveAllEnabledHeaderGLAccounts(final GLAccountType accountType) {
        return retrieveAllGLAccounts(accountType.getValue(), null, GLAccountUsage.HEADER.getValue(), null, false,
                new JournalEntryAssociationParametersData());
    }

    @Override
    public List<GLAccountDataForLookup> retrieveAccountsByTagId(final Long ruleId, final Integer transactionType) {
        final GLAccountDataLookUpMapper mapper = new GLAccountDataLookUpMapper();
        final String sql = "Select " + GLAccountDataLookUpMapper.LOOKUP_SCHEMA + " where rule.id=? and tags.acc_type_enum=?";
        return this.jdbcTemplate.query(sql, mapper, ruleId, transactionType);
    }

    private static final class GLAccountDataLookUpMapper implements RowMapper<GLAccountDataForLookup> {

        private static final String LOOKUP_SCHEMA = " gl.id as id, gl.name as name, gl.gl_code as glCode from acc_accounting_rule rule join acc_rule_tags tags on tags.acc_rule_id = rule.id join acc_gl_account gl on gl.tag_id=tags.tag_id";

        @Override
        public GLAccountDataForLookup mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String name = rs.getString("name");
            final String glCode = rs.getString("glCode");

            return new GLAccountDataForLookup().setId(id).setName(name).setGlCode(glCode);
        }
    }
}
