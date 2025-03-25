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
package org.apache.fineract.portfolio.account.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.AccountTransferEnumerations;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public final class AccountTransfersMapper implements RowMapper<AccountTransferData> {

    private final String schemaSql;

    public AccountTransfersMapper() {
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("att.id as id, att.is_reversed as isReversed,");
        sqlBuilder.append("att.transaction_date as transferDate, att.amount as transferAmount,");
        sqlBuilder.append("att.description as transferDescription,");
        sqlBuilder.append("att.currency_code as currencyCode, att.currency_digits as currencyDigits,");
        sqlBuilder.append("att.currency_multiplesof as inMultiplesOf, ");
        sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
        sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
        sqlBuilder.append("fromoff.id as fromOfficeId, fromoff.name as fromOfficeName,");
        sqlBuilder.append("tooff.id as toOfficeId, tooff.name as toOfficeName,");
        sqlBuilder.append("fromclient.id as fromClientId, fromclient.display_name as fromClientName,");
        sqlBuilder.append("toclient.id as toClientId, toclient.display_name as toClientName,");
        sqlBuilder.append("fromsavacc.id as fromSavingsAccountId, fromsavacc.account_no as fromSavingsAccountNo,");
        sqlBuilder.append("fromloanacc.id as fromLoanAccountId, fromloanacc.account_no as fromLoanAccountNo,");
        sqlBuilder.append("tosavacc.id as toSavingsAccountId, tosavacc.account_no as toSavingsAccountNo,");
        sqlBuilder.append("toloanacc.id as toLoanAccountId, toloanacc.account_no as toLoanAccountNo,");
        sqlBuilder.append("fromsavtran.id as fromSavingsAccountTransactionId,");
        sqlBuilder.append("fromsavtran.transaction_type_enum as fromSavingsAccountTransactionType,");
        sqlBuilder.append("tosavtran.id as toSavingsAccountTransactionId,");
        sqlBuilder.append("tosavtran.transaction_type_enum as toSavingsAccountTransactionType");
        sqlBuilder.append(" FROM m_account_transfer_transaction att ");
        sqlBuilder.append("left join m_account_transfer_details atd on atd.id = att.account_transfer_details_id ");
        sqlBuilder.append("join m_currency curr on curr.code = att.currency_code ");
        sqlBuilder.append("join m_office fromoff on fromoff.id = atd.from_office_id ");
        sqlBuilder.append("join m_office tooff on tooff.id = atd.to_office_id ");
        sqlBuilder.append("join m_client fromclient on fromclient.id = atd.from_client_id ");
        sqlBuilder.append("join m_client toclient on toclient.id = atd.to_client_id ");
        sqlBuilder.append("left join m_savings_account fromsavacc on fromsavacc.id = atd.from_savings_account_id ");
        sqlBuilder.append("left join m_loan fromloanacc on fromloanacc.id = atd.from_loan_account_id ");
        sqlBuilder.append("left join m_savings_account tosavacc on tosavacc.id = atd.to_savings_account_id ");
        sqlBuilder.append("left join m_loan toloanacc on toloanacc.id = atd.to_loan_account_id ");
        sqlBuilder.append("left join m_savings_account_transaction fromsavtran on fromsavtran.id = att.from_savings_transaction_id ");
        sqlBuilder.append("left join m_savings_account_transaction tosavtran on tosavtran.id = att.to_savings_transaction_id ");
        sqlBuilder.append("left join m_loan_transaction fromloantran on fromloantran.id = att.from_savings_transaction_id ");
        sqlBuilder.append("left join m_loan_transaction toloantran on toloantran.id = att.to_savings_transaction_id ");

        this.schemaSql = sqlBuilder.toString();
    }

    public String schema() {
        return this.schemaSql;
    }

    @Override
    public AccountTransferData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

        final Long id = rs.getLong("id");
        final boolean reversed = rs.getBoolean("isReversed");

        final LocalDate transferDate = JdbcSupport.getLocalDate(rs, "transferDate");
        final BigDecimal transferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transferAmount");
        final String transferDescription = rs.getString("transferDescription");

        final String currencyCode = rs.getString("currencyCode");
        final String currencyName = rs.getString("currencyName");
        final String currencyNameCode = rs.getString("currencyNameCode");
        final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
        final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
        final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
        final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                currencyNameCode);

        final Long fromOfficeId = JdbcSupport.getLong(rs, "fromOfficeId");
        final String fromOfficeName = rs.getString("fromOfficeName");
        final OfficeData fromOffice = OfficeData.dropdown(fromOfficeId, fromOfficeName, null);

        final Long toOfficeId = JdbcSupport.getLong(rs, "toOfficeId");
        final String toOfficeName = rs.getString("toOfficeName");
        final OfficeData toOffice = OfficeData.dropdown(toOfficeId, toOfficeName, null);

        final Long fromClientId = JdbcSupport.getLong(rs, "fromClientId");
        final String fromClientName = rs.getString("fromClientName");
        final ClientData fromClient = ClientData.lookup(fromClientId, fromClientName, fromOfficeId, fromOfficeName);

        final Long toClientId = JdbcSupport.getLong(rs, "toClientId");
        final String toClientName = rs.getString("toClientName");
        final ClientData toClient = ClientData.lookup(toClientId, toClientName, toOfficeId, toOfficeName);

        final Long fromSavingsAccountId = JdbcSupport.getLong(rs, "fromSavingsAccountId");
        final String fromSavingsAccountNo = rs.getString("fromSavingsAccountNo");
        final Long fromLoanAccountId = JdbcSupport.getLong(rs, "fromLoanAccountId");
        final String fromLoanAccountNo = rs.getString("fromLoanAccountNo");
        PortfolioAccountData fromAccount = null;
        EnumOptionData fromAccountType = null;
        if (fromSavingsAccountId != null) {
            fromAccount = PortfolioAccountData.lookup(fromSavingsAccountId, fromSavingsAccountNo);
            fromAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.SAVINGS);
        } else if (fromLoanAccountId != null) {
            fromAccount = PortfolioAccountData.lookup(fromLoanAccountId, fromLoanAccountNo);
            fromAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.LOAN);
        }

        PortfolioAccountData toAccount = null;
        EnumOptionData toAccountType = null;
        final Long toSavingsAccountId = JdbcSupport.getLong(rs, "toSavingsAccountId");
        final String toSavingsAccountNo = rs.getString("toSavingsAccountNo");
        final Long toLoanAccountId = JdbcSupport.getLong(rs, "toLoanAccountId");
        final String toLoanAccountNo = rs.getString("toLoanAccountNo");

        if (toSavingsAccountId != null) {
            toAccount = PortfolioAccountData.lookup(toSavingsAccountId, toSavingsAccountNo);
            toAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.SAVINGS);
        } else if (toLoanAccountId != null) {
            toAccount = PortfolioAccountData.lookup(toLoanAccountId, toLoanAccountNo);
            toAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.LOAN);
        }

        return AccountTransferData.instance(id, reversed, transferDate, currency, transferAmount, transferDescription, fromOffice, toOffice,
                fromClient, toClient, fromAccountType, fromAccount, toAccountType, toAccount);
    }
}
