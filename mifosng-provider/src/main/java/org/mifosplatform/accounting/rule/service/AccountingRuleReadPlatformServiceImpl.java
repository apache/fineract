/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.glaccount.data.GLAccountDataForLookup;
import org.mifosplatform.accounting.glaccount.service.GLAccountReadPlatformService;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryType;
import org.mifosplatform.accounting.rule.data.AccountingRuleData;
import org.mifosplatform.accounting.rule.data.AccountingTagRuleData;
import org.mifosplatform.accounting.rule.exception.AccountingRuleNotFoundException;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
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
    public AccountingRuleReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource,
            final GLAccountReadPlatformService glAccountReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.glAccountReadPlatformService = glAccountReadPlatformService;
    }

    // Will remove the old one if everything goes well.
/*    private static final class AccountingRuleMapper implements RowMapper<AccountingRuleData> {

        private final String schemaSql;

        public AccountingRuleMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder
                    .append("distinct rule.id as id,rule.name as name, rule.office_id as officeId,office.name as officeName,")
                    .append(" rule.debit_account_id as debitAccountId,rule.credit_account_id as creditAccountId,")
                    .append(" rule.description as description, rule.system_defined as systemDefined, ")
                    .append("debitAccount.glName as debitAccountName, debitAccount.glCode as debitAccountGLCode,")
                    .append("creditAccount.glName as creditAccountName, creditAccount.glCode as creditAccountGLCode ")
                    .append("from (Select gl.id as id,gl.name as glName, gl.gl_code as glCode from acc_gl_account gl,acc_accounting_rule rule ")
                    .append("where gl.id=rule.debit_account_id ) as debitAccount,")
                    .append("(Select gl.id as id,gl.name as glName, gl.gl_code as glCode from acc_gl_account gl,acc_accounting_rule rule ")
                    .append("where gl.id=rule.credit_account_id ) as creditAccount,")
                    .append("acc_accounting_rule rule left join m_office office on rule.office_id=office.id ")
                    .append("where debitAccount.id=rule.debit_account_id and creditAccount.id = rule.credit_account_id");
            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public AccountingRuleData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final Long accountToDebitId = rs.getLong("debitAccountId");
            final Long accountToCreditId = rs.getLong("creditAccountId");
            final boolean systemDefined = rs.getBoolean("systemDefined");
            final String debitAccountName = rs.getString("debitAccountName");
            final String creditAccountName = rs.getString("creditAccountName");
            final String debitAccountGLCode = rs.getString("debitAccountGLCode");
            final String creditAccountGLCode = rs.getString("creditAccountGLCode");

            final GLAccountData debitAccountData = new GLAccountData(accountToDebitId, debitAccountName, debitAccountGLCode);
            final GLAccountData creditAccountData = new GLAccountData(accountToCreditId, creditAccountName, creditAccountGLCode);

            return new AccountingRuleData(id, officeId, officeName, name, description, systemDefined, debitAccountData, creditAccountData);
        }
    }*/

    private static final class AccountingRuleDataExtractor implements ResultSetExtractor<Map<Long, AccountingRuleData>> {

        private final String schemaSql;
        private final JdbcTemplate jdbcTemplate;
        private final GLAccountReadPlatformService glAccountReadPlatformService;

        public AccountingRuleDataExtractor(final JdbcTemplate jdbcTemplate, final GLAccountReadPlatformService glAccountReadPlatformService) {
            this.jdbcTemplate = jdbcTemplate;
            this.glAccountReadPlatformService = glAccountReadPlatformService;
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder
                    .append(" rule.id as id,rule.name as name, rule.office_id as officeId,office.name as officeName,")
                    .append(" rule.description as description, rule.system_defined as systemDefined, ")
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
            final Map<Long, AccountingRuleData> extractedData = new HashMap<Long, AccountingRuleData>();

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
                    final String debitAccountName = rs.getString("debitAccountName");
                    final String creditAccountName = rs.getString("creditAccountName");
                    final String debitAccountGLCode = rs.getString("debitAccountGLCode");
                    final String creditAccountGLCode = rs.getString("creditAccountGLCode");

                    final GLAccountData debitAccountData = accountToDebitId == null ? null : new GLAccountData(accountToDebitId,
                            debitAccountName, debitAccountGLCode);
                    final GLAccountData creditAccountData = accountToCreditId == null ? null : new GLAccountData(accountToCreditId,
                            creditAccountName, creditAccountGLCode);
                    accountingRuleData = new AccountingRuleData(id, officeId, officeName, name, description, systemDefined,
                            debitAccountData, creditAccountData);
                }
                if (accountingRuleData.getCreditAccountHead() == null) {
                    final List<AccountingTagRuleData> creditTags = getCreditOrDebitTags(id, JournalEntryType.CREDIT.getValue());
                    final List<GLAccountDataForLookup> creditAccounts = this.glAccountReadPlatformService.retrieveAccountsByTagId(id,
                            JournalEntryType.CREDIT.getValue());
                    accountingRuleData = new AccountingRuleData(accountingRuleData, creditTags, creditAccounts,
                            accountingRuleData.getDebitTags(), accountingRuleData.getDebitAccounts());
                }
                if (accountingRuleData.getDebitAccountHead() == null) {
                    final List<AccountingTagRuleData> debitTags = getCreditOrDebitTags(id, JournalEntryType.DEBIT.getValue());
                    final List<GLAccountDataForLookup> debitAccounts = this.glAccountReadPlatformService.retrieveAccountsByTagId(id,
                            JournalEntryType.DEBIT.getValue());
                    accountingRuleData = new AccountingRuleData(accountingRuleData, accountingRuleData.getCreditTags(),
                            accountingRuleData.getCreditAccounts(), debitTags, debitAccounts);
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
    public List<AccountingRuleData> retrieveAllAccountingRules(final Long officeId) {
        final AccountingRuleDataExtractor resultSetExtractor = new AccountingRuleDataExtractor(this.jdbcTemplate,
                this.glAccountReadPlatformService);
        Object[] arguments = new Object[] {};
        String sql = "select " + resultSetExtractor.schema() + " and system_defined=0 ";
        if (officeId != null) {
            sql = sql + " and office.id = ?";
            arguments = new Object[] { officeId };
        }
        sql = sql + " order by rule.id asc";
        final Map<Long, AccountingRuleData> extractedData = this.jdbcTemplate.query(sql, resultSetExtractor, arguments);
        return new ArrayList<AccountingRuleData>(extractedData.values());
    }

    @Override
    public AccountingRuleData retrieveAccountingRuleById(final Long accountingRuleId) {
        try {
            final AccountingRuleDataExtractor resultSetExtractor = new AccountingRuleDataExtractor(this.jdbcTemplate,
                    this.glAccountReadPlatformService);
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
            return " tag.id as id,tag.tag_id as tagId, tag.acc_type_enum as transactionType from acc_rule_tags tag "
                    + "join acc_accounting_rule rule on tag.acc_rule_id=rule.id ";
        }

        @Override
        public AccountingTagRuleData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long tagId = rs.getLong("tagId");
            final Integer transactionType = JdbcSupport.getInteger(rs, "transactionType");
            final EnumOptionData transactionTypeEnum = AccountingEnumerations.journalEntryType(transactionType);
            return new AccountingTagRuleData(id, tagId, transactionTypeEnum);
        }

    }
}
