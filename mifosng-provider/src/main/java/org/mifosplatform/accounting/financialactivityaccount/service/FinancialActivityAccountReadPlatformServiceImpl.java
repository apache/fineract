/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.financialactivityaccount.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import org.mifosplatform.accounting.common.AccountingDropdownReadPlatformService;
import org.mifosplatform.accounting.financialactivityaccount.data.FinancialActivityAccountData;
import org.mifosplatform.accounting.financialactivityaccount.data.FinancialActivityData;
import org.mifosplatform.accounting.financialactivityaccount.exception.FinancialActivityAccountNotFoundException;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class FinancialActivityAccountReadPlatformServiceImpl implements FinancialActivityAccountReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final FinancialActivityAccountMapper financialActivityAccountMapper;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;

    @Autowired
    public FinancialActivityAccountReadPlatformServiceImpl(final RoutingDataSource dataSource,
            final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService) {
        financialActivityAccountMapper = new FinancialActivityAccountMapper();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.accountingDropdownReadPlatformService = accountingDropdownReadPlatformService;
    }

    @Override
    public List<FinancialActivityAccountData> retrieveAll() {
        String sql = "select " + financialActivityAccountMapper.schema();
        return this.jdbcTemplate.query(sql, financialActivityAccountMapper, new Object[] {});
    }

    @Override
    public FinancialActivityAccountData retrieve(Long financialActivityAccountId) {
        try {
            StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.financialActivityAccountMapper.schema());
            sqlBuilder.append(" where faa.id=?");
            return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), this.financialActivityAccountMapper,
                    new Object[] { financialActivityAccountId });
        } catch (final EmptyResultDataAccessException e) {
            throw new FinancialActivityAccountNotFoundException(financialActivityAccountId);
        }
    }

    @Override
    public FinancialActivityAccountData addTemplateDetails(FinancialActivityAccountData financialActivityAccountData) {
        final Map<String, List<GLAccountData>> accountOptions = this.accountingDropdownReadPlatformService.retrieveAccountMappingOptions();
        financialActivityAccountData.setAccountingMappingOptions(accountOptions);
        financialActivityAccountData.setFinancialActivityOptions(FINANCIAL_ACTIVITY.getAllFinancialActivities());
        return financialActivityAccountData;
    }

    @Override
    public FinancialActivityAccountData getFinancialActivityAccountTemplate() {
        FinancialActivityAccountData financialActivityAccountData = new FinancialActivityAccountData();
        return addTemplateDetails(financialActivityAccountData);
    }

    private static final class FinancialActivityAccountMapper implements RowMapper<FinancialActivityAccountData> {

        private final String sql;

        public FinancialActivityAccountMapper() {
            StringBuilder sb = new StringBuilder(300);
            sb.append(" faa.id as id, faa.financial_activity_type as financialActivityId, glaccount.id as glAccountId,glaccount.name as glAccountName,glaccount.gl_code as glCode  ");
            sb.append(" from acc_gl_financial_activity_account faa ");
            sb.append(" join acc_gl_account glaccount on glaccount.id = faa.gl_account_id");
            sql = sb.toString();
        }

        public String schema() {
            return sql;
        }

        @Override
        public FinancialActivityAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final Long glAccountId = JdbcSupport.getLong(rs, "glAccountId");
            final Integer financialActivityId = JdbcSupport.getInteger(rs, "financialActivityId");
            final String glAccountName = rs.getString("glAccountName");
            final String glCode = rs.getString("glCode");

            final GLAccountData glAccountData = new GLAccountData(glAccountId, glAccountName, glCode);
            final FinancialActivityData financialActivityData = FINANCIAL_ACTIVITY.toFinancialActivityData(financialActivityId);

            final FinancialActivityAccountData financialActivityAccountData = new FinancialActivityAccountData(id, financialActivityData,
                    glAccountData);
            return financialActivityAccountData;
        }
    }

}
