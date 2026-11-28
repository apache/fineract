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
import static org.apache.fineract.query.QAccRuleTags.accRuleTags;
import static org.apache.fineract.query.QMCodeValue.mCodeValue;

import com.querydsl.sql.SQLQueryFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.rule.data.AccountingRuleData;
import org.apache.fineract.accounting.rule.data.AccountingTagRuleData;
import org.apache.fineract.accounting.rule.domain.AccountingTagRuleProjection;
import org.apache.fineract.accounting.rule.exception.AccountingRuleNotFoundException;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AccountingRuleReadPlatformServiceImpl implements AccountingRuleReadPlatformService {

    protected final JdbcTemplate jdbcTemplate;
    protected final GLAccountReadPlatformService glAccountReadPlatformService;
    private final SQLQueryFactory queryFactory;

    private static final class AccountingRuleDataExtractor implements ResultSetExtractor<Map<Long, AccountingRuleData>> {

        private final String schemaSql;
        private final SQLQueryFactory queryFactory;
        private final GLAccountReadPlatformService glAccountReadPlatformService;
        private final boolean isAssociationParametersExists;

        AccountingRuleDataExtractor(final SQLQueryFactory queryFactory, final GLAccountReadPlatformService glAccountReadPlatformService,
                final boolean isAssociationParametersExists) {
            this.queryFactory = queryFactory;
            this.glAccountReadPlatformService = glAccountReadPlatformService;
            this.isAssociationParametersExists = isAssociationParametersExists;
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

                    final List<AccountingTagRuleData> creditTags;
                    final List<AccountingTagRuleData> debitTags;
                    final List<GLAccountDataForLookup> creditAccounts;
                    final List<GLAccountDataForLookup> debitAccounts;

                    if (accountToCreditId == null) {
                        creditTags = !this.isAssociationParametersExists ? getCreditOrDebitTags(id, JournalEntryType.CREDIT.getValue())
                                : null;
                        creditAccounts = this.isAssociationParametersExists
                                ? this.glAccountReadPlatformService.retrieveAccountsByTagId(id, JournalEntryType.CREDIT.getValue())
                                : null;
                    } else {
                        creditTags = null;
                        final GLAccountDataForLookup creditAccount = new GLAccountDataForLookup().setId(accountToCreditId)
                                .setName(creditAccountName).setGlCode(creditAccountGLCode);
                        creditAccounts = new ArrayList<>(Arrays.asList(creditAccount));
                    }
                    if (accountToDebitId == null) {
                        debitTags = !this.isAssociationParametersExists ? getCreditOrDebitTags(id, JournalEntryType.DEBIT.getValue())
                                : null;
                        debitAccounts = this.isAssociationParametersExists
                                ? this.glAccountReadPlatformService.retrieveAccountsByTagId(id, JournalEntryType.DEBIT.getValue())
                                : null;
                    } else {
                        debitTags = null;
                        final GLAccountDataForLookup debitAccount = new GLAccountDataForLookup().setId(accountToDebitId)
                                .setName(debitAccountName).setGlCode(debitAccountGLCode);
                        debitAccounts = new ArrayList<>(Arrays.asList(debitAccount));
                    }
                    accountingRuleData = new AccountingRuleData().setId(id).setOfficeId(officeId).setOfficeName(officeName).setName(name)
                            .setDescription(description).setSystemDefined(systemDefined)
                            .setAllowMultipleDebitEntries(allowMultipleDebitEntries)
                            .setAllowMultipleCreditEntries(allowMultipleCreditEntries).setCreditTags(creditTags).setDebitTags(debitTags)
                            .setCreditAccounts(creditAccounts).setDebitAccounts(debitAccounts);
                }

                extractedData.put(id, accountingRuleData);
            }
            return extractedData;
        }

        private List<AccountingTagRuleData> getCreditOrDebitTags(final Long accountingRuleId, final Integer transactionType) {
            return queryFactory.select(new AccountingTagRuleProjection()).from(mCodeValue).innerJoin(accRuleTags)
                    .on(accRuleTags.tagId.eq(mCodeValue.id)).innerJoin(accAccountingRule).on(accRuleTags.accRuleId.eq(accAccountingRule.id))
                    .where(accAccountingRule.id.eq(accountingRuleId), accRuleTags.accTypeEnum.eq(transactionType)).fetch();
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountingRuleData> retrieveAllAccountingRules(final String hierarchySearchString,
            final boolean isAssociationParametersExists) {
        final AccountingRuleDataExtractor resultSetExtractor = new AccountingRuleDataExtractor(this.queryFactory,
                this.glAccountReadPlatformService, isAssociationParametersExists);
        Object[] arguments = new Object[] {};
        String sql = "select " + resultSetExtractor.schema() + " and system_defined=false ";
        if (hierarchySearchString != null) {
            sql = sql + " and office.hierarchy like ?";
            arguments = new Object[] { hierarchySearchString };
        }
        sql = sql + " order by rule.id asc";
        final Map<Long, AccountingRuleData> extractedData = this.jdbcTemplate.query(sql, resultSetExtractor, arguments); // NOSONAR
        return new ArrayList<>(extractedData.values());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountingRuleData retrieveAccountingRuleById(final Long accountingRuleId) {
        try {
            final AccountingRuleDataExtractor resultSetExtractor = new AccountingRuleDataExtractor(this.queryFactory,
                    this.glAccountReadPlatformService, false);
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
}
