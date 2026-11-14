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
package org.apache.fineract.portfolio.savings.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.springframework.jdbc.core.RowMapper;

public class AccountTransferMapper implements RowMapper<AccountTransferDTO> {

    public static final String FROM_ACC = "fromAcc";
    public static final String TO_ACC = "toAcc";
    public static final String AMOUNT = "amount";
    public static final String TRANSACTION_DATE = "transactionDate";
    public static final String TRANSFER_INTEREST_TO_SAVINGS = "transfer interest to savings";
    private final String schemaSql;

    public AccountTransferMapper() {
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append(
                "sa.id as fromAcc ,aa.linked_savings_account_id as toAcc,st.amount as amount, st.transaction_date as transactionDate ")
                .append(" from m_deposit_account_term_and_preclosure da ")
                .append(" inner join m_savings_account sa on da.savings_account_id = sa.id")
                .append(" inner join m_savings_account_transaction st on st.savings_account_id = sa.id")
                .append(" inner join m_portfolio_account_associations aa on aa.savings_account_id=sa.id");
        schemaSql = sqlBuilder.toString();
    }

    public String schema() {
        return this.schemaSql;
    }

    @Override
    public AccountTransferDTO mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
        final Long fromAccountId = rs.getLong(FROM_ACC);
        final Long toAccountId = rs.getLong(TO_ACC);
        final BigDecimal transactionAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, AMOUNT);
        final boolean isRegularTransaction = false;
        final boolean isExceptionForBalanceCheck = false;
        final LocalDate transactionDate = JdbcSupport.getLocalDate(rs, TRANSACTION_DATE);
        return new AccountTransferDTO(transactionDate, transactionAmount, PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS,
                fromAccountId, toAccountId, TRANSFER_INTEREST_TO_SAVINGS, null, null, null, null, null, null, null,
                AccountTransferType.INTEREST_TRANSFER.getValue(), null, null, ExternalId.empty(), null, null, null, isRegularTransaction,
                isExceptionForBalanceCheck);
    }
}
