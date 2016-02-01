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
package org.apache.fineract.portfolio.client.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.client.data.ClientTransactionData;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.client.domain.ClientTransactionType;
import org.apache.fineract.portfolio.client.exception.ClientTransactionNotFoundException;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientTransactionReadPlatformServiceImpl implements ClientTransactionReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ClientTransactionMapper clientTransactionMapper;
    private final PaginationHelper<ClientTransactionData> paginationHelper;

    @Autowired
    public ClientTransactionReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientTransactionMapper = new ClientTransactionMapper();
        this.paginationHelper = new PaginationHelper<>();
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
    public Page<ClientTransactionData> retrieveAllTransactions(Long clientId, SearchParameters searchParameters) {
        Object[] parameters = new Object[1];
        final StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ").append(this.clientTransactionMapper.schema()).append(" where c.id = ? ");
        parameters[0] = clientId;
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        sqlBuilder.append(" order by tr.transaction_date DESC, tr.created_date DESC, tr.id DESC ");

        // apply limit and offsets
        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), parameters,
                this.clientTransactionMapper);
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
