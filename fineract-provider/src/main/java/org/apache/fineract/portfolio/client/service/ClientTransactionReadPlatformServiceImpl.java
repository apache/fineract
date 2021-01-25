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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.client.data.ClientSavingsAccountTransactionData;
import org.apache.fineract.portfolio.client.data.ClientTransactionData;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.client.domain.ClientTransactionType;
import org.apache.fineract.portfolio.client.exception.ClientTransactionNotFoundException;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class ClientTransactionReadPlatformServiceImpl implements ClientTransactionReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ClientTransactionMapper clientTransactionMapper;
    private final ClientSavingsAccountTransactionMapper clientSavingsAccountTransactionMapper;
    private final PaginationHelper<ClientTransactionData> paginationHelper;
    private final PaginationHelper<ClientSavingsAccountTransactionData> paginationSavingsAccountHelper;

    @Autowired
    public ClientTransactionReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientTransactionMapper = new ClientTransactionMapper(dataSource);
        this.clientSavingsAccountTransactionMapper = new ClientSavingsAccountTransactionMapper(dataSource);
        this.paginationHelper = new PaginationHelper<>();
        this.paginationSavingsAccountHelper = new PaginationHelper<>();
    }

    private static final class ClientTransactionMapper implements RowMapper<ClientTransactionData> {

        private static final String SCHEMA_SQL_COUNT_SELECTION = "SELECT COUNT(tr.id) ";
        private static final String SCHEMA_SQL_SELECTION_PART = "SELECT tr.id as transactionId, tr.transaction_type_enum as transactionType,  "
                + "tr.transaction_date as transactionDate, tr.amount as transactionAmount, "
                + "tr.created_date as submittedOnDate, tr.is_reversed as reversed, "
                + "tr.external_id as externalId, o.name as officeName, o.id as officeId, "
                + "c.id as clientId, c.account_no as accountNo, ccpb.client_charge_id as clientChargeId, "
                + "pd.payment_type_id as paymentType,pd.account_number as accountNumber,pd.check_number as checkNumber, "
                + "pd.receipt_number as receiptNumber, pd.bank_number as bankNumber,pd.routing_code as routingCode,  "
                + "tr.currency_code as currencyCode, curr.decimal_places as currencyDigits, curr.currency_multiplesof as inMultiplesOf, "
                + "curr.name as currencyName, curr.internationalized_name_code as currencyNameCode,  "
                + "curr.display_symbol as currencyDisplaySymbol,  " + "pt.value as paymentTypeName  ";
        private static final String SCHEMA_SQL_FROM_PART = " from m_client c  ";
        private static final String SCHEMA_SQL_JOIN_PART = "join m_client_transaction tr on tr.client_id = c.id "
                + "join m_currency curr on curr.code = tr.currency_code "
                + "left join m_payment_detail pd on tr.payment_detail_id = pd.id  "
                + "left join m_payment_type pt  on pd.payment_type_id = pt.id " + "left join m_office o on o.id = tr.office_id "
                + "left join m_client_charge_paid_by ccpb on ccpb.client_transaction_id = tr.id ";
        private static final String SCHEMA_SQL_JOIN_DATA_TABLES = "join m_savings_account sav on sav.client_id = c.id "
                + "join m_savings_account_transaction savt on savt.savings_account_id = sav.id ";

        private final String schemaSql;
        private String sqlCountRows;
        private final DataSource dataSource;
        private Map<String, Map<String, List<Object>>> filter;

        ClientTransactionMapper(DataSource dataSource) {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(SCHEMA_SQL_SELECTION_PART);
            sqlBuilder.append(SCHEMA_SQL_FROM_PART);
            sqlBuilder.append(SCHEMA_SQL_JOIN_PART);

            final StringBuilder sqlCountRowsBuilder = new StringBuilder(400);
            sqlCountRowsBuilder.append(SCHEMA_SQL_COUNT_SELECTION);
            sqlCountRowsBuilder.append(SCHEMA_SQL_FROM_PART);
            sqlCountRowsBuilder.append(SCHEMA_SQL_JOIN_PART);

            this.schemaSql = sqlBuilder.toString();
            this.sqlCountRows = sqlCountRowsBuilder.toString();
            this.dataSource = dataSource;
        }

        public String schema() {
            return this.schemaSql;
        }

        public String getSqlCountRows() {
            return sqlCountRows;
        }

        public String schema(Map<String, Map<String, List<Object>>> filter) {
            this.filter = filter;
            if (filter.isEmpty()) {
                return schema();
            }
            final StringBuilder sqlCountRowsBuilder = new StringBuilder(400);
            sqlCountRowsBuilder.append(this.sqlCountRows);

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(SCHEMA_SQL_SELECTION_PART);
            for (String tableName : filter.keySet()) {
                List<String> columnNames = getTableColumns(this.dataSource, tableName);
                for (String columnName : columnNames) {
                    sqlBuilder.append(", " + tableName + "." + columnName + " as " + tableName + "_" + columnName);
                }
            }
            sqlBuilder.append(SCHEMA_SQL_FROM_PART);
            sqlBuilder.append(SCHEMA_SQL_JOIN_PART);
            sqlBuilder.append(SCHEMA_SQL_JOIN_DATA_TABLES);
            sqlCountRowsBuilder.append(SCHEMA_SQL_JOIN_DATA_TABLES);
            for (String tableName : filter.keySet()) {
                String joinTable = "join  " + tableName + "  on " + tableName + ".savings_account_transaction_id = savt.id ";
                sqlBuilder.append(joinTable);
                sqlCountRowsBuilder.append(joinTable);
                Map<String, List<Object>> columnFilters = filter.get(tableName);
                for (String columnName : columnFilters.keySet()) {
                    List list = columnFilters.get(columnName);
                    if (list.size() == 1) {
                        String where = " and " + tableName + "." + columnName + " = " + list.get(0) + " ";
                        sqlBuilder.append(where);
                        sqlCountRowsBuilder.append(where);
                        continue;
                    }
                    if (list.size() == 2) {
                        String where = " and " + tableName + "." + columnName + " between " + list.get(0) + " and " + list.get(1) + " ";
                        sqlBuilder.append(where);
                        sqlCountRowsBuilder.append(where);
                    }
                }
            }
            this.sqlCountRows = sqlCountRowsBuilder.toString();
            return sqlBuilder.toString();
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
            Map<String, Map<String, Object>> dataTablesMap = new HashMap<>();
            for (String tableName : filter.keySet()) {
                Map<String, Object> map = new HashMap<>();
                List<String> columnNames = getTableColumns(this.dataSource, tableName);
                for (String column : columnNames) {
                    map.put(column, rs.getObject(tableName + "_" + column));
                }
                dataTablesMap.put(tableName, map);
            }

            return ClientTransactionData.create(id, officeId, officeName, transactionType, date, currency, paymentDetailData, amount,
                    externalId, submittedOnDate, reversed, dataTablesMap);
        }

    }

    private static final class ClientSavingsAccountTransactionMapper implements RowMapper<ClientSavingsAccountTransactionData> {

        private static final String SCHEMA_SQL_COUNT_SELECTION = "SELECT COUNT(tr.id) ";
        private static final String SCHEMA_SQL_SELECTION_PART = "SELECT tr.id as transactionId, tr.transaction_type_enum as transactionType, "
                + " tr.transaction_date as transactionDate, tr.amount as transactionAmount, "
                + " tr.created_date as submittedOnDate, tr.running_balance_derived as runningBalance, "
                + " tr.is_reversed as reversed, sa.id as savingsId, sa.account_no as accountNo, "
                + " sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, "
                + " curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, "
                + " curr.display_symbol as currencyDisplaySymbol, tr.is_manual as postInterestAsOn  ";
        private static final String SCHEMA_SQL_FROM_PART = " from m_savings_account sa  ";
        private static final String SCHEMA_SQL_JOIN_PART = "join m_savings_account_transaction tr on tr.savings_account_id = sa.id "
                + " join m_currency curr on curr.code = sa.currency_code ";

        private final String schemaSql;
        private String sqlCountRows;
        private final DataSource dataSource;
        private Map<String, Map<String, List<Object>>> filter;

        ClientSavingsAccountTransactionMapper(DataSource dataSource) {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(SCHEMA_SQL_SELECTION_PART);
            sqlBuilder.append(SCHEMA_SQL_FROM_PART);
            sqlBuilder.append(SCHEMA_SQL_JOIN_PART);

            final StringBuilder sqlCountRowsBuilder = new StringBuilder(400);
            sqlCountRowsBuilder.append(SCHEMA_SQL_COUNT_SELECTION);
            sqlCountRowsBuilder.append(SCHEMA_SQL_FROM_PART);
            sqlCountRowsBuilder.append(SCHEMA_SQL_JOIN_PART);

            this.schemaSql = sqlBuilder.toString();
            this.sqlCountRows = sqlCountRowsBuilder.toString();
            this.dataSource = dataSource;
        }

        public String schema() {
            return this.schemaSql;
        }

        public String getSqlCountRows() {
            return sqlCountRows;
        }

        public String schema(Map<String, Map<String, List<Object>>> filter) {
            this.filter = filter;
            if (filter.isEmpty()) {
                return schema();
            }
            final StringBuilder sqlCountRowsBuilder = new StringBuilder(400);
            sqlCountRowsBuilder.append(this.sqlCountRows);

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(SCHEMA_SQL_SELECTION_PART);
            for (String tableName : filter.keySet()) {
                List<String> columnNames = getTableColumns(this.dataSource, tableName);
                for (String columnName : columnNames) {
                    sqlBuilder.append(", " + tableName + "." + columnName + " as " + tableName + "_" + columnName);
                }
            }
            sqlBuilder.append(SCHEMA_SQL_FROM_PART);
            sqlBuilder.append(SCHEMA_SQL_JOIN_PART);

            for (String tableName : filter.keySet()) {
                String joinTable = "join  " + tableName + "  on " + tableName + ".savings_account_transaction_id = tr.id ";
                sqlBuilder.append(joinTable);
                sqlCountRowsBuilder.append(joinTable);
                Map<String, List<Object>> columnFilters = filter.get(tableName);
                for (String columnName : columnFilters.keySet()) {
                    List list = columnFilters.get(columnName);
                    if (list.size() == 1) {
                        String where = " and " + tableName + "." + columnName + " = " + list.get(0) + " ";
                        sqlBuilder.append(where);
                        sqlCountRowsBuilder.append(where);
                        continue;
                    }
                    if (list.size() == 2) {
                        String where = " and " + tableName + "." + columnName + " between " + list.get(0) + " and " + list.get(1) + " ";
                        sqlBuilder.append(where);
                        sqlCountRowsBuilder.append(where);
                    }
                }
            }
            return sqlBuilder.toString();
        }

        @Override
        public ClientSavingsAccountTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long id = rs.getLong("transactionId");
            final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
            final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(transactionTypeInt);

            final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transactionAmount");
            final BigDecimal outstandingChargeAmount = null;
            final BigDecimal runningBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "runningBalance");
            final boolean reversed = rs.getBoolean("reversed");

            final Long savingsId = rs.getLong("savingsId");
            final String accountNo = rs.getString("accountNo");
            final boolean postInterestAsOn = rs.getBoolean("postInterestAsOn");
            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                    currencyNameCode);

            Map<String, Map<String, Object>> dataTablesMap = new HashMap<>();
            for (String tableName : filter.keySet()) {
                Map<String, Object> map = new HashMap<>();
                List<String> columnNames = getTableColumns(this.dataSource, tableName);
                for (String column : columnNames) {
                    map.put(column, rs.getObject(tableName + "_" + column));
                }
                dataTablesMap.put(tableName, map);
            }

            return ClientSavingsAccountTransactionData.create(id, transactionType, savingsId, accountNo, date, currency, amount,
                    outstandingChargeAmount, runningBalance, reversed, submittedOnDate, postInterestAsOn, dataTablesMap);
        }

    }

    @SuppressWarnings("unchecked")
    public static List<String> getTableColumns(DataSource dataSource, String tableName) {
        try {
            return JdbcUtils.extractDatabaseMetaData(dataSource, (dbmd) -> {
                ResultSet rs = dbmd.getColumns("", "%", tableName + "%", null);
                List<String> list = new ArrayList();
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    list.add(columnName);
                }
                return list;
            });
        } catch (MetaDataAccessException ex) {
            throw new RuntimeException("Get table column name list failed", ex);
        }
    }

    @Override
    public Page<ClientTransactionData> retrieveAllTransactions(Long clientId, SearchParameters searchParameters) {
        final StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(this.clientTransactionMapper.schema(searchParameters.getDataTableFilters()))
                .append(" where c.id = ? order by tr.transaction_date DESC, tr.created_date DESC, tr.id DESC ");

        // apply limit and offsets
        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        Object[] parameters = new Object[1];
        parameters[0] = clientId;

        String sqlCountRows = this.clientTransactionMapper.getSqlCountRows() + " where c.id = " + clientId;
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), parameters,
                this.clientTransactionMapper);
    }

    @Override
    public Collection<ClientTransactionData> retrieveAllTransactions(Long clientId, Long chargeId) {
        Object[] parameters = new Object[1];
        String sql = this.clientTransactionMapper.schema() + " where c.id = ? ";
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
            final String sql = this.clientTransactionMapper.schema() + " where c.id = ? and tr.id= ?";
            return this.jdbcTemplate.queryForObject(sql, this.clientTransactionMapper, new Object[] { clientId, transactionId });
        } catch (final EmptyResultDataAccessException e) {
            throw new ClientTransactionNotFoundException(clientId, transactionId, e);
        }
    }

    @Override
    public Page<ClientSavingsAccountTransactionData> retrieveAllSavingsAccountTransactions(Long clientId,
            SearchParameters searchParameters) {
        final StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append(this.clientSavingsAccountTransactionMapper.schema(searchParameters.getDataTableFilters()))
                .append(" where sa.client_id = ? order by tr.transaction_date DESC, tr.created_date DESC, tr.id DESC ");

        // apply limit and offsets
        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        Object[] parameters = new Object[1];
        parameters[0] = clientId;

        String sqlCountRows = this.clientSavingsAccountTransactionMapper.getSqlCountRows() + " where sa.client_id = " + clientId;
        return this.paginationSavingsAccountHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), parameters,
                this.clientSavingsAccountTransactionMapper);
    }
}
