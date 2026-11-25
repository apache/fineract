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

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.springframework.jdbc.core.RowMapper;

final class SavingsAccountTransactionTemplateMapper implements RowMapper<SavingsAccountTransactionData> {

    private final String schemaSql;

    SavingsAccountTransactionTemplateMapper() {
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("sa.id as id, sa.account_no as accountNo, ");
        sqlBuilder.append(
                "sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
        sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
        sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
        sqlBuilder.append("sa.min_required_opening_balance as minRequiredOpeningBalance ");
        sqlBuilder.append("from m_savings_account sa ");
        sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");

        this.schemaSql = sqlBuilder.toString();
    }

    String schema() {
        return this.schemaSql;
    }

    @Override
    public SavingsAccountTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

        final Long savingsId = rs.getLong("id");
        final String accountNo = rs.getString("accountNo");

        final String currencyCode = rs.getString("currencyCode");
        final String currencyName = rs.getString("currencyName");
        final String currencyNameCode = rs.getString("currencyNameCode");
        final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
        final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
        final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
        final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                currencyNameCode);

        return SavingsAccountTransactionData.template(savingsId, accountNo, DateUtils.getBusinessLocalDate(), currency);
    }
}
