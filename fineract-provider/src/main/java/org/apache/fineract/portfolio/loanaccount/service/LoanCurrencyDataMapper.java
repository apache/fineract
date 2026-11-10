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
package org.apache.fineract.portfolio.loanaccount.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.springframework.jdbc.core.RowMapper;

final class LoanCurrencyDataMapper implements RowMapper<CurrencyData> {

    private final DatabaseSpecificSQLGenerator sqlGenerator;

    LoanCurrencyDataMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
    }

    public String schema() {
        return " l.currency_code as currencyCode, l.currency_digits as currencyDigits, l.currency_multiplesof as inMultiplesOf, rc."
                + sqlGenerator.escape("name")
                + " as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode from m_loan l join m_currency rc on rc."
                + sqlGenerator.escape("code") + " = l.currency_code ";
    }

    @Override
    public CurrencyData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
        final String currencyCode = rs.getString("currencyCode");
        final String currencyName = rs.getString("currencyName");
        final String currencyNameCode = rs.getString("currencyNameCode");
        final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
        final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
        final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
        return new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol, currencyNameCode);
    }
}
