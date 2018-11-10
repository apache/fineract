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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.savings.data.DepositAccountOnHoldTransactionData;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DepositAccountOnHoldTransactionReadPlatformServiceImpl implements DepositAccountOnHoldTransactionReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ColumnValidator columnValidator;
    private final PaginationHelper<DepositAccountOnHoldTransactionData> paginationHelper = new PaginationHelper<>();
    private final DepositAccountOnHoldTransactionsMapper mapper;

    @Autowired
    public DepositAccountOnHoldTransactionReadPlatformServiceImpl(final RoutingDataSource dataSource,
    		final ColumnValidator columnValidator) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        mapper = new DepositAccountOnHoldTransactionsMapper();
        this.columnValidator = columnValidator;
    }

    @Override
    public Page<DepositAccountOnHoldTransactionData> retriveAll(Long savingsId, Long guarantorFundingId, SearchParameters searchParameters) {
        final StringBuilder sqlBuilder = new StringBuilder(200);
        List<Long> paramObj = new ArrayList<>(2);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.mapper.schema());

        sqlBuilder.append(" where tr.savings_account_id = ? ");
        paramObj.add(savingsId);
        if (guarantorFundingId != null) {
            sqlBuilder.append(" and gt.guarantor_fund_detail_id = ? ");
            paramObj.add(guarantorFundingId);
        }

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
            this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());

            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
            }
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        final Object[] finalObjectArray = paramObj.toArray();
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), finalObjectArray, this.mapper);

    }

    private static final class DepositAccountOnHoldTransactionsMapper implements RowMapper<DepositAccountOnHoldTransactionData> {

        private final String schemaSql;

        public DepositAccountOnHoldTransactionsMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(" tr.id as transactionId, tr.transaction_type_enum as transactionType, ");
            sqlBuilder.append(" tr.transaction_date as transactionDate, tr.amount as transactionAmount,");
            sqlBuilder.append(" tr.is_reversed as reversed, sa.account_no as savingsAccNum, ");
            sqlBuilder.append("sc.display_name as savingsClientName, ml.id as loanid, sa.id as savingid, ");
            sqlBuilder.append(" ml.account_no as loanAccountNum, lc.display_name as loanClientName");
            sqlBuilder.append(" from m_savings_account sa  ");
            sqlBuilder.append(" join m_deposit_account_on_hold_transaction tr on sa.id = tr.savings_account_id ");
            sqlBuilder.append(" join m_client sc on sc.id = sa.client_id");
            sqlBuilder.append(" left join m_guarantor_transaction gt on gt.deposit_on_hold_transaction_id = tr.id ");
            sqlBuilder.append(" left join m_guarantor_funding_details mgfd on mgfd.id=gt.guarantor_fund_detail_id");
            sqlBuilder.append(" left join m_portfolio_account_associations  pa on pa.id=mgfd.account_associations_id");
            sqlBuilder.append(" left join m_loan ml on ml.id = pa.loan_account_id");
            sqlBuilder.append(" left join m_client lc on lc.id = ml.client_id");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public DepositAccountOnHoldTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long id = rs.getLong("transactionId");
            final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
            final EnumOptionData transactionType = SavingsEnumerations.onHoldTransactionType(transactionTypeInt);

            final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transactionAmount");
            final boolean reversed = rs.getBoolean("reversed");
            final String savingsAccountNum = rs.getString("savingsAccNum");
            final Long savingsId = rs.getLong("savingid");
            final String savingsClientName = rs.getString("savingsClientName");
            final String loanAccountNum = rs.getString("loanAccountNum");
            final Long loanId = rs.getLong("loanid");
            final String loanClientName = rs.getString("loanClientName");
            return DepositAccountOnHoldTransactionData.instance(id, amount, transactionType, date, reversed, savingsId, savingsAccountNum,
                    savingsClientName, loanId, loanAccountNum, loanClientName);
        }
    }

}
