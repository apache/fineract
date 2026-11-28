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
package org.apache.fineract.accounting.rule.service;

import static org.apache.fineract.query.QAccAccountingRule.accAccountingRule;
import static org.apache.fineract.query.QMOffice.mOffice;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Param;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.rule.data.AccountingRuleData;
import org.apache.fineract.accounting.rule.data.AccountingTagRuleData;
import org.apache.fineract.accounting.rule.domain.AccountingRuleProjection;
import org.apache.fineract.accounting.rule.exception.AccountingRuleNotFoundException;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.query.QAccGlAccount;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AccountingRuleReadPlatformServiceImpl implements AccountingRuleReadPlatformService {

    private static final QAccGlAccount CREDIT_ACCOUNT = new QAccGlAccount("creditAccount");
    private static final QAccGlAccount DEBIT_ACCOUNT = new QAccGlAccount("debitAccount");

    protected final JdbcTemplate jdbcTemplate;
    protected final GLAccountReadPlatformService glAccountReadPlatformService;
    private final SQLQueryFactory queryFactory;

    private final class AccountingRuleDataExtractor implements ResultSetExtractor<Map<Long, AccountingRuleData>> {

        private final String schemaSql;

        AccountingRuleDataExtractor() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(" rule.id as id,rule.name as name, rule.office_id as officeId,office.name as officeName,").append(
                    " rule.description as description, rule.system_defined as systemDefined, rule.allow_multiple_debits as allowMultipleDebitEntries, rule.allow_multiple_credits as allowMultipleCreditEntries, ")
                    .append("debitAccount.id AS debitAccountId, debitAccount.name as debitAccountName, debitAccount.gl_code as debitAccountGLCode, ")
                    .append("creditAccount.id AS creditAccountId, creditAccount.name as creditAccountName, creditAccount.gl_code as creditAccountGLCode")
                    .append(" from m_office AS office, acc_accounting_rule AS rule ")
                    .append(" LEFT JOIN acc_gl_account AS creditAccount ON rule.credit_account_id = creditAccount.id ")
                    .append(" LEFT JOIN acc_gl_account AS debitAccount ON rule.debit_account_id = debitAccount.id ")
                    .append("WHERE office.id=rule.office_id ");
            this.schemaSql = sqlBuilder.toString();
        }

        private String schema() {
            return this.schemaSql;
        }

        @Override
        public Map<Long, AccountingRuleData> extractData(final ResultSet rs) throws SQLException, DataAccessException {
            final Map<Long, AccountingRuleData> extractedData = new HashMap<>();

            while (rs.next()) {
                final Long id = rs.getLong("id");
                AccountingRuleData accountingRuleData = extractedData.get(id);
                if (accountingRuleData == null) {
                    final Long officeId = JdbcSupport.getLong(rs, "officeId");
                    final String officeName = rs.getString("officeName");
                    final String name = rs.getString("name");
                    final String description = rs.getString("description");
                    final Long accountToDebitId = JdbcSupport.getLong(rs, "debitAccountId");
                    final Long accountToCreditId = JdbcSupport.getLong(rs, "creditAccountId");
                    final boolean systemDefined = rs.getBoolean("systemDefined");
                    final boolean allowMultipleDebitEntries = rs.getBoolean("allowMultipleDebitEntries");
                    final boolean allowMultipleCreditEntries = rs.getBoolean("allowMultipleCreditEntries");
                    final String debitAccountName = rs.getString("debitAccountName");
                    final String creditAccountName = rs.getString("creditAccountName");
                    final String debitAccountGLCode = rs.getString("debitAccountGLCode");
                    final String creditAccountGLCode = rs.getString("creditAccountGLCode");

                    accountingRuleData = new AccountingRuleData().setId(id).setOfficeId(officeId).setOfficeName(officeName).setName(name)
                            .setDescription(description).setSystemDefined(systemDefined)
                            .setAllowMultipleDebitEntries(allowMultipleDebitEntries)
                            .setAllowMultipleCreditEntries(allowMultipleCreditEntries);
                    if (accountToCreditId != null) {
                        final GLAccountDataForLookup creditAccount = new GLAccountDataForLookup().setId(accountToCreditId)
                                .setName(creditAccountName).setGlCode(creditAccountGLCode);
                        accountingRuleData.setCreditAccounts(new ArrayList<>(Arrays.asList(creditAccount)));
                    }
                    if (accountToDebitId != null) {
                        final GLAccountDataForLookup debitAccount = new GLAccountDataForLookup().setId(accountToDebitId)
                                .setName(debitAccountName).setGlCode(debitAccountGLCode);
                        accountingRuleData.setDebitAccounts(new ArrayList<>(Arrays.asList(debitAccount)));
                    }
                    populateAssociations(accountingRuleData, false);
                }

                extractedData.put(id, accountingRuleData);
            }
            return extractedData;
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountingRuleData> retrieveAllAccountingRules(final String hierarchySearchString,
            final boolean isAssociationParametersExists) {
        final SQLQuery<AccountingRuleData> query = this.queryFactory
                .select(new AccountingRuleProjection(accAccountingRule, mOffice, DEBIT_ACCOUNT, CREDIT_ACCOUNT)).from(accAccountingRule)
                .innerJoin(mOffice).on(mOffice.id.eq(accAccountingRule.officeId)).leftJoin(CREDIT_ACCOUNT)
                .on(accAccountingRule.creditAccountId.eq(CREDIT_ACCOUNT.id)).leftJoin(DEBIT_ACCOUNT)
                .on(accAccountingRule.debitAccountId.eq(DEBIT_ACCOUNT.id)).where(accAccountingRule.systemDefined.eq(Expressions.FALSE));
        if (hierarchySearchString != null) {
            final Param<String> hierarchyPattern = new Param<>(String.class, "hierarchyPattern");
            query.where(mOffice.hierarchy.like(hierarchyPattern));
            query.set(hierarchyPattern, hierarchySearchString);
        }
        final List<AccountingRuleData> rules = query.orderBy(accAccountingRule.id.asc()).fetch();
        final Map<Long, AccountingRuleData> extractedData = new HashMap<>();
        for (final AccountingRuleData rule : rules) {
            populateAssociations(rule, isAssociationParametersExists);
            extractedData.put(rule.getId(), rule);
        }
        return new ArrayList<>(extractedData.values());
    }

    private void populateAssociations(final AccountingRuleData rule, final boolean includeAccounts) {
        if (rule.getCreditAccounts() == null) {
            if (includeAccounts) {
                rule.setCreditAccounts(
                        this.glAccountReadPlatformService.retrieveAccountsByTagId(rule.getId(), JournalEntryType.CREDIT.getValue()));
            } else {
                rule.setCreditTags(getCreditOrDebitTags(rule.getId(), JournalEntryType.CREDIT.getValue()));
            }
        }
        if (rule.getDebitAccounts() == null) {
            if (includeAccounts) {
                rule.setDebitAccounts(
                        this.glAccountReadPlatformService.retrieveAccountsByTagId(rule.getId(), JournalEntryType.DEBIT.getValue()));
            } else {
                rule.setDebitTags(getCreditOrDebitTags(rule.getId(), JournalEntryType.DEBIT.getValue()));
            }
        }
    }

    private List<AccountingTagRuleData> getCreditOrDebitTags(final Long ruleId, final Integer transactionType) {
        final AccountingTagRuleDataMapper mapper = new AccountingTagRuleDataMapper();
        final String taggedAccountsSchema = "Select " + mapper.taggedAccountSchema() + " where rule.id = ? and tag.acc_type_enum=?";
        return this.jdbcTemplate.query(taggedAccountsSchema, mapper, new Object[] { ruleId, transactionType }); // NOSONAR
    }

    @Override
    public AccountingRuleData retrieveAccountingRuleById(final Long accountingRuleId) {
        try {
            final AccountingRuleDataExtractor resultSetExtractor = new AccountingRuleDataExtractor();
            final String sql = "select " + resultSetExtractor.schema() + " and rule.id = ?";

            final Map<Long, AccountingRuleData> extractedData = this.jdbcTemplate.query(sql, resultSetExtractor, // NOSONAR
                    new Object[] { accountingRuleId });
            final AccountingRuleData accountingRuleData = extractedData.get(accountingRuleId);
            if (accountingRuleData == null) {
                throw new AccountingRuleNotFoundException(accountingRuleId);
            }
            return accountingRuleData;
        } catch (final EmptyResultDataAccessException e) {
            throw new AccountingRuleNotFoundException(accountingRuleId, e);
        }
    }

    private static final class AccountingTagRuleDataMapper implements RowMapper<AccountingTagRuleData> {

        private String taggedAccountSchema() {
            return " tag.id as id,tag.tag_id as tagId, tag.acc_type_enum as transactionType, cv.code_value as tagName from m_code_value cv join acc_rule_tags tag on tag.tag_id=cv.id "
                    + "join acc_accounting_rule rule on tag.acc_rule_id=rule.id ";
        }

        @Override
        public AccountingTagRuleData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long tagId = rs.getLong("tagId");
            final Integer transactionType = JdbcSupport.getInteger(rs, "transactionType");
            final String tagName = rs.getString("tagName");
            final CodeValueData tag = CodeValueData.instance(tagId, tagName);
            final EnumOptionData transactionTypeEnum = AccountingEnumerations.journalEntryType(transactionType);
            return new AccountingTagRuleData().setId(id).setTag(tag).setTransactionType(transactionTypeEnum);
        }

    }
}
