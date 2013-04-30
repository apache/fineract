/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.accounting.rule.data.AccountingRuleData;
import org.mifosplatform.accounting.rule.exception.AccountingRuleNotFoundException;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AccountingRuleReadPlatformServiceImpl implements AccountingRuleReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AccountingRuleReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class AccountingRuleMapper implements RowMapper<AccountingRuleData> {

        private final String schemaSql;

        public AccountingRuleMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("select rule.id as id,rule.name as name, rule.office_id as officeId,office.name as officeName,")
                    .append(" rule.debit_account_id as debitAccountId,rule.credit_account_id as creditAccountId,")
                    .append(" rule.description as description, rule.system_defined as systemDefined ").append("from ")
                    .append(" acc_accounting_rule rule left join m_office office on rule.office_id=office.id");
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

            return new AccountingRuleData(id, officeId, officeName, accountToDebitId, accountToCreditId, name, description, systemDefined);
        }
    }

    @Override
    public List<AccountingRuleData> retrieveAllAccountingRules(Long officeId) {
        final AccountingRuleMapper rm = new AccountingRuleMapper();
        String sql = "select " + rm.schema() + " where system_defined=0 and office.id = ?";
        sql = sql + " order by rule.id asc";
        return this.jdbcTemplate.query(sql, rm, new Object[] { officeId });
    }

    @Override
    public AccountingRuleData retrieveAccountingRuleById(long accountingRuleId) {
        try {

            final AccountingRuleMapper rm = new AccountingRuleMapper();
            final String sql = "select " + rm.schema() + " and glClosure.id = ?";

            final AccountingRuleData accountingRuleData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { accountingRuleId });
            return accountingRuleData;
        } catch (final EmptyResultDataAccessException e) {
            throw new AccountingRuleNotFoundException(accountingRuleId);
        }
    }

}
