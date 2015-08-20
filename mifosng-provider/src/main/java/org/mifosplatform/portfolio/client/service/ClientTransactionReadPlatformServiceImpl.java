/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.client.data.ClientTransactionData;
import org.mifosplatform.portfolio.client.domain.ClientEnumerations;
import org.mifosplatform.portfolio.client.domain.ClientTransactionType;
import org.mifosplatform.portfolio.client.exception.ClientTransactionNotFoundException;
import org.mifosplatform.portfolio.paymentdetail.data.PaymentDetailData;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientTransactionReadPlatformServiceImpl implements ClientTransactionReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ClientTransactionMapper clientTransactionMapper;

    @Autowired
    public ClientTransactionReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientTransactionMapper = new ClientTransactionMapper();

    }

    private static final class ClientTransactionMapper implements RowMapper<ClientTransactionData> {

        private final String schemaSql;

        public ClientTransactionMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("tr.id as transactionId, tr.transaction_type_enum as transactionType,  ");
            sqlBuilder.append("tr.transaction_date as transactionDate, tr.amount as transactionAmount, ");
            sqlBuilder.append("tr.created_date as submittedOnDate, tr.is_reversed as reversed, ");
            sqlBuilder.append("tr.external_id as externalId, o.name as officeName, o.id as officeId, ");
            sqlBuilder.append("c.id as clientId, c.account_no as accountNo, ccpb.client_charge_id as clientChargeId, ");
            sqlBuilder.append("pd.payment_type_id as paymentType,pd.account_number as accountNumber,pd.check_number as checkNumber, ");
            sqlBuilder.append("pd.receipt_number as receiptNumber, pd.bank_number as bankNumber,pd.routing_code as routingCode,  ");
            sqlBuilder.append(
                    "tr.currency_code as currencyCode, curr.decimal_places as currencyDigits, curr.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode,  ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol,  ");
            sqlBuilder.append("pt.value as paymentTypeName  ");
            sqlBuilder.append("from m_client c  ");
            sqlBuilder.append("join m_client_transaction tr on tr.client_id = c.id ");
            sqlBuilder.append("join m_currency curr on curr.code = tr.currency_code ");
            sqlBuilder.append("left join m_payment_detail pd on tr.payment_detail_id = pd.id  ");
            sqlBuilder.append("left join m_payment_type pt  on pd.payment_type_id = pt.id ");
            sqlBuilder.append("left join m_office o on o.id = tr.office_id ");
            sqlBuilder.append("left join m_client_charge_paid_by ccpb on ccpb.client_transaction_id = tr.id ");
            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public ClientTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("transactionId");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final String externalId = rs.getString("externalId");
            final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
            final EnumOptionData transactionType = ClientEnumerations.clientTransactionType(transactionTypeInt);

            final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transactionAmount");
            final boolean reversed = rs.getBoolean("reversed");

            PaymentDetailData paymentDetailData = null;
            if (ClientTransactionType.fromInt(transactionType.getId().intValue()).equals(ClientTransactionType.PAY_CHARGE)) {
                final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentType");
                if (paymentTypeId != null) {
                    final String typeName = rs.getString("paymentTypeName");
                    final PaymentTypeData paymentType = PaymentTypeData.instance(paymentTypeId, typeName);
                    final String accountNumber = rs.getString("accountNumber");
                    final String checkNumber = rs.getString("checkNumber");
                    final String routingCode = rs.getString("routingCode");
                    final String receiptNumber = rs.getString("receiptNumber");
                    final String bankNumber = rs.getString("bankNumber");
                    paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                            bankNumber);
                }
            }

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                    currencyNameCode);

            return ClientTransactionData.create(id, officeId, officeName, transactionType, date, currency, paymentDetailData, amount,
                    externalId, submittedOnDate, reversed);
        }
    }

    @Override
    public Collection<ClientTransactionData> retrieveAllTransactions(Long clientId) {
        Long chargeId = null;
        return retrieveAllTransactions(clientId, chargeId);
    }

    @Override
    public Collection<ClientTransactionData> retrieveAllTransactions(Long clientId, Long chargeId) {
        Object[] parameters = new Object[1];
        String sql = "select " + this.clientTransactionMapper.schema() + " where c.id = ? ";
        if (chargeId != null) {
            parameters = new Object[2];
            parameters[1] = chargeId;
            sql = sql + " and ccpb.client_charge_id = ?";
        }
        parameters[0] = clientId;
        sql = sql + " order by tr.transaction_date DESC, tr.created_date DESC, tr.id DESC";
        return this.jdbcTemplate.query(sql, this.clientTransactionMapper, parameters);
    }

    @Override
    public ClientTransactionData retrieveTransaction(Long clientId, Long transactionId) {
        try {
            final String sql = "select " + this.clientTransactionMapper.schema() + " where c.id = ? and tr.id= ?";
            return this.jdbcTemplate.queryForObject(sql, this.clientTransactionMapper, new Object[] { clientId, transactionId });
        } catch (final EmptyResultDataAccessException e) {
            throw new ClientTransactionNotFoundException(clientId, transactionId);
        }
    }

}
