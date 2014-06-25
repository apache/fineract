/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.accounting.glaccount.data.GLAccountDataForLookup;
import org.mifosplatform.accounting.glaccount.service.GLAccountReadPlatformService;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryType;
import org.mifosplatform.accounting.rule.data.AccountingRuleData;
import org.mifosplatform.accounting.rule.data.AccountingTagRuleData;
import org.mifosplatform.accounting.rule.exception.AccountingRuleNotFoundException;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AccountingRuleReadPlatformServiceImpl implements AccountingRuleReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final GLAccountReadPlatformService glAccountReadPlatformService;

    @Autowired
    public AccountingRuleReadPlatformServiceImpl(final RoutingDataSource dataSource,
            final GLAccountReadPlatformService glAccountReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.glAccountReadPlatformService = glAccountReadPlatformService;
    }

    private static final class AccountingRuleDataExtractor implements ResultSetExtractor<Map<Long, AccountingRuleData>> {

        private final String schemaSql;
        private final JdbcTemplate jdbcTemplate;
        private final GLAccountReadPlatformService glAccountReadPlatformService;
        private final boolean isAssociationParametersExists;

        public AccountingRuleDataExtractor(final JdbcTemplate jdbcTemplate,
                final GLAccountReadPlatformService glAccountReadPlatformService, final boolean isAssociationParametersExists) {
            this.jdbcTemplate = jdbcTemplate;
            this.glAccountReadPlatformService = glAccountReadPlatformService;
            this.isAssociationParametersExists = isAssociationParametersExists;
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder
                    .append(" rule.id as id,rule.name as name, rule.office_id as officeId,office.name as officeName,")
                    .append(" rule.description as description, rule.system_defined as systemDefined, rule.allow_multiple_debits as allowMultipleDebitEntries, rule.allow_multiple_credits as allowMultipleCreditEntries, ")
                    .append("debitAccount.id AS debitAccountId, debitAccount.name as debitAccountName, debitAccount.gl_code as debitAccountGLCode, ")
                    .append("creditAccount.id AS creditAccountId, creditAccount.name as creditAccountName, creditAccount.gl_code as creditAccountGLCode")
                    .append(" from m_office AS office, acc_accounting_rule AS rule ")
                    .append(" LEFT JOIN acc_gl_account AS creditAccount ON rule.credit_account_id = creditAccount.id ")
                    .append(" LEFT JOIN acc_gl_account AS debitAccount ON rule.debit_account_id = debitAccount.id ")
                    .append("WHERE office.id=rule.office_id ");
            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
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
                        creditAccounts = this.isAssociationParametersExists ? this.glAccountReadPlatformService.retrieveAccountsByTagId(id,
                                JournalEntryType.CREDIT.getValue()) : null;
                    } else {
                        creditTags = null;
                        final GLAccountDataForLookup creditAccount = new GLAccountDataForLookup(accountToCreditId, creditAccountName,
                                creditAccountGLCode);
                        creditAccounts = new ArrayList<>(Arrays.asList(creditAccount));
                    }
                    if (accountToDebitId == null) {
                        debitTags = !this.isAssociationParametersExists ? getCreditOrDebitTags(id, JournalEntryType.DEBIT.getValue())
                                : null;
                        debitAccounts = this.isAssociationParametersExists ? this.glAccountReadPlatformService.retrieveAccountsByTagId(id,
                                JournalEntryType.DEBIT.getValue()) : null;
                    } else {
                        debitTags = null;
                        final GLAccountDataForLookup debitAccount = new GLAccountDataForLookup(accountToDebitId, debitAccountName,
                                debitAccountGLCode);
                        debitAccounts = new ArrayList<>(Arrays.asList(debitAccount));
                    }
                    accountingRuleData = new AccountingRuleData(id, officeId, officeName, name, description, systemDefined,
                            allowMultipleDebitEntries, allowMultipleCreditEntries, creditTags, debitTags, creditAccounts, debitAccounts);
                }

                extractedData.put(id, accountingRuleData);
            }
            return extractedData;
        }

        private List<AccountingTagRuleData> getCreditOrDebitTags(final Long creditOrDebitAccount, final Integer transactionType) {
            final AccountingTagRuleDataMapper mapper = new AccountingTagRuleDataMapper();
            final String taggedAccountsSchema = "Select " + mapper.taggedAccountSchema() + " where rule.id = ? and tag.acc_type_enum=?";
            return this.jdbcTemplate.query(taggedAccountsSchema, mapper, new Object[] { creditOrDebitAccount, transactionType });
        }

    }

    @Override
    public List<AccountingRuleData> retrieveAllAccountingRules(final String hierarchySearchString,
            final boolean isAssociationParametersExists) {
        final AccountingRuleDataExtractor resultSetExtractor = new AccountingRuleDataExtractor(this.jdbcTemplate,
                this.glAccountReadPlatformService, isAssociationParametersExists);
        Object[] arguments = new Object[] {};
        String sql = "select " + resultSetExtractor.schema() + " and system_defined=0 ";
        if (hierarchySearchString != null) {
            sql = sql + " and office.hierarchy like ?";
            arguments = new Object[] { hierarchySearchString };
        }
        sql = sql + " order by rule.id asc";
        final Map<Long, AccountingRuleData> extractedData = this.jdbcTemplate.query(sql, resultSetExtractor, arguments);
        return new ArrayList<>(extractedData.values());
    }

    @Override
    public AccountingRuleData retrieveAccountingRuleById(final Long accountingRuleId) {
        try {
            final AccountingRuleDataExtractor resultSetExtractor = new AccountingRuleDataExtractor(this.jdbcTemplate,
                    this.glAccountReadPlatformService, false);
            final String sql = "select " + resultSetExtractor.schema() + " and rule.id = ?";

            final Map<Long, AccountingRuleData> extractedData = this.jdbcTemplate.query(sql, resultSetExtractor,
                    new Object[] { accountingRuleId });
            final AccountingRuleData accountingRuleData = extractedData.get(accountingRuleId);
            if (accountingRuleData == null) { throw new AccountingRuleNotFoundException(accountingRuleId); }
            return accountingRuleData;
        } catch (final EmptyResultDataAccessException e) {
            throw new AccountingRuleNotFoundException(accountingRuleId);
        }
    }

    private static final class AccountingTagRuleDataMapper implements RowMapper<AccountingTagRuleData> {

        public String taggedAccountSchema() {
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
            return new AccountingTagRuleData(id, tag, transactionTypeEnum);
        }

    }
}
