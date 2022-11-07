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
package org.apache.fineract.accounting.financialactivityaccount.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.financialactivityaccount.data.FinancialActivityAccountData;
import org.apache.fineract.accounting.financialactivityaccount.data.FinancialActivityData;
import org.apache.fineract.accounting.financialactivityaccount.exception.FinancialActivityAccountNotFoundException;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinancialActivityAccountReadPlatformServiceImpl implements FinancialActivityAccountReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final FinancialActivityAccountMapper financialActivityAccountMapper = new FinancialActivityAccountMapper();

    @Override
    public List<FinancialActivityAccountData> retrieveAll() {
        String sql = "select " + financialActivityAccountMapper.schema();
        return this.jdbcTemplate.query(sql, financialActivityAccountMapper, new Object[] {}); // NOSONAR
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
            throw new FinancialActivityAccountNotFoundException(financialActivityAccountId, e);
        }
    }

    @Override
    public FinancialActivityAccountData addTemplateDetails(FinancialActivityAccountData financialActivityAccountData) {
        final Map<String, List<GLAccountData>> accountOptions = this.accountingDropdownReadPlatformService.retrieveAccountMappingOptions();
        financialActivityAccountData.setGlAccountOptions(accountOptions);
        financialActivityAccountData.setFinancialActivityOptions(FinancialActivity.getAllFinancialActivities());
        return financialActivityAccountData;
    }

    @Override
    public FinancialActivityAccountData getFinancialActivityAccountTemplate() {
        FinancialActivityAccountData financialActivityAccountData = new FinancialActivityAccountData();
        return addTemplateDetails(financialActivityAccountData);
    }

    private static final class FinancialActivityAccountMapper implements RowMapper<FinancialActivityAccountData> {

        private final String sql;

        FinancialActivityAccountMapper() {
            StringBuilder sb = new StringBuilder(300);
            sb.append(
                    " faa.id as id, faa.financial_activity_type as financialActivityId, glaccount.id as glAccountId,glaccount.name as glAccountName,glaccount.gl_code as glCode  ");
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

            final GLAccountData glAccountData = new GLAccountData().setId(glAccountId).setName(glAccountName).setGlCode(glCode);

            final FinancialActivityData financialActivityData = FinancialActivity.toFinancialActivityData(financialActivityId);

            final FinancialActivityAccountData financialActivityAccountData = new FinancialActivityAccountData(id, financialActivityData,
                    glAccountData);
            return financialActivityAccountData;
        }
    }

}
