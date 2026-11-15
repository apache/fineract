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
import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;
import org.apache.fineract.accounting.glaccount.domain.GLAccountProjection;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.accounting.glaccount.exception.GLAccountInvalidClassificationException;
import org.apache.fineract.accounting.glaccount.exception.GLAccountNotFoundException;
import org.apache.fineract.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.query.QAccGlJournalEntry;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GLAccountReadPlatformServiceImpl implements GLAccountReadPlatformService {

    private static final String NAME_DECORATED_BASE_ON_HIERARCHY = "concat(substring('........................................', 1, ((LENGTH(hierarchy) - LENGTH(REPLACE(hierarchy, '.', '')) - 1) * 4)), name)";

    private static final QAccGlJournalEntry BALANCE_ENTRY = new QAccGlJournalEntry("balanceEntry");
    private static final QAccGlJournalEntry CANDIDATE_ENTRY = new QAccGlJournalEntry("candidateEntry");
    private static final QAccGlJournalEntry CALCULATED_ENTRY = new QAccGlJournalEntry("calculatedEntry");
    private static final NumberExpression<Long> ORGANIZATION_RUNNING_BALANCE = Expressions.numberTemplate(Long.class, "{0}",
            BALANCE_ENTRY.organizationRunningBalance);

    private final JdbcTemplate jdbcTemplate;
    private final SQLQueryFactory queryFactory;

    private static final class GLAccountMapper implements RowMapper<GLAccountData> {

        private final JournalEntryAssociationParametersData associationParametersData;

        GLAccountMapper(final JournalEntryAssociationParametersData associationParametersData) {
            if (associationParametersData == null) {
                this.associationParametersData = new JournalEntryAssociationParametersData();
            } else {
                this.associationParametersData = associationParametersData;
            }
        }

        private String schema() {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    " gl.id as id, name as name, parent_id as parentId, gl_code as glCode, disabled as disabled, manual_journal_entries_allowed as manualEntriesAllowed, ")
                    .append("classification_enum as classification, account_usage as accountUsage, gl.description as description, ")
                    .append(NAME_DECORATED_BASE_ON_HIERARCHY).append(" as nameDecorated, ")
                    .append("cv.id as codeId, cv.code_value as codeValue ");
            if (this.associationParametersData.isRunningBalanceRequired()) {
                sb.append(",gl_j.organization_running_balance as organizationRunningBalance ");
            }
            sb.append("from acc_gl_account gl left join m_code_value cv on tag_id=cv.id ");
            if (this.associationParametersData.isRunningBalanceRequired()) {
                sb.append("left outer Join acc_gl_journal_entry gl_j on gl_j.account_id = gl.id");
            }
            return sb.toString();
        }

        @Override
        public GLAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final Long parentId = JdbcSupport.getLong(rs, "parentId");
            final String glCode = rs.getString("glCode");
            final boolean disabled = rs.getBoolean("disabled");
            final boolean manualEntriesAllowed = rs.getBoolean("manualEntriesAllowed");
            final int accountTypeId = JdbcSupport.getInteger(rs, "classification");
            final EnumOptionData accountType = AccountingEnumerations.gLAccountType(accountTypeId);
            final int usageId = JdbcSupport.getInteger(rs, "accountUsage");
            final EnumOptionData usage = AccountingEnumerations.gLAccountUsage(usageId);
            final String description = rs.getString("description");
            final String nameDecorated = rs.getString("nameDecorated");
            final Long codeId = rs.wasNull() ? null : rs.getLong("codeId");
            final String codeValue = rs.getString("codeValue");
            final CodeValueData tagId = CodeValueData.instance(codeId, codeValue);
            Long organizationRunningBalance = null;
            if (associationParametersData.isRunningBalanceRequired()) {
                organizationRunningBalance = rs.getLong("organizationRunningBalance");
            }
            return new GLAccountData().setId(id).setName(name).setParentId(parentId).setGlCode(glCode).setDisabled(disabled)
                    .setManualEntriesAllowed(manualEntriesAllowed).setType(accountType).setUsage(usage).setDescription(description)
                    .setNameDecorated(nameDecorated).setTagId(tagId).setOrganizationRunningBalance(organizationRunningBalance);
        }
    }

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
    public GLAccountData retrieveGLAccountById(final long glAccountId, JournalEntryAssociationParametersData associationParametersData) {
        try {
            final GLAccountMapper rm = new GLAccountMapper(associationParametersData);
            final StringBuilder sql = new StringBuilder();
            sql.append("select ").append(rm.schema());
            sql.append(" where gl.id = ?");
            if (associationParametersData.isRunningBalanceRequired()) {
                sql.append(" and gl_j.is_running_balance_calculated = true ")
                        .append("  ORDER BY gl_j.entry_date DESC,gl_j.id DESC LIMIT 1");
            }

            return this.jdbcTemplate.queryForObject(sql.toString(), rm, new Object[] { glAccountId });
        } catch (final EmptyResultDataAccessException e) {
            throw new GLAccountNotFoundException(glAccountId, e);
        }
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
