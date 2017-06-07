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
package org.apache.fineract.portfolio.account.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.exception.AccountTransferNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PortfolioAccountReadPlatformServiceImpl implements PortfolioAccountReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    // mapper
    private final PortfolioSavingsAccountMapper savingsAccountMapper;
    private final PortfolioLoanAccountMapper loanAccountMapper;
    private final PortfolioLoanAccountRefundByTransferMapper accountRefundByTransferMapper;

    @Autowired
    public PortfolioAccountReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.savingsAccountMapper = new PortfolioSavingsAccountMapper();
        this.loanAccountMapper = new PortfolioLoanAccountMapper();
        this.accountRefundByTransferMapper = new PortfolioLoanAccountRefundByTransferMapper();
    }

    @Override
    public PortfolioAccountData retrieveOne(final Long accountId, final Integer accountTypeId) {
        return retrieveOne(accountId, accountTypeId, null);
    }

    @Override
    public PortfolioAccountData retrieveOne(final Long accountId, final Integer accountTypeId, final String currencyCode) {

        Object[] sqlParams = new Object[] { accountId };
        PortfolioAccountData accountData = null;
        try {
            String sql = null;
            final PortfolioAccountType accountType = PortfolioAccountType.fromInt(accountTypeId);
            switch (accountType) {
                case INVALID:
                break;
                case LOAN:

                    sql = "select " + this.loanAccountMapper.schema() + " where la.id = ?";
                    if (currencyCode != null) {
                        sql += " and la.currency_code = ?";
                        sqlParams = new Object[] { accountId, currencyCode };
                    }

                    accountData = this.jdbcTemplate.queryForObject(sql, this.loanAccountMapper, sqlParams);
                break;
                case SAVINGS:
                    sql = "select " + this.savingsAccountMapper.schema() + " where sa.id = ?";
                    if (currencyCode != null) {
                        sql += " and sa.currency_code = ?";
                        sqlParams = new Object[] { accountId, currencyCode };
                    }

                    accountData = this.jdbcTemplate.queryForObject(sql, this.savingsAccountMapper, sqlParams);
                break;
                default:
                break;
            }
        } catch (final EmptyResultDataAccessException e) {
            throw new AccountTransferNotFoundException(accountId);
        }

        return accountData;
    }

    @Override
    public Collection<PortfolioAccountData> retrieveAllForLookup(final PortfolioAccountDTO portfolioAccountDTO) {

        final List<Object> sqlParams = new ArrayList<>();
        //sqlParams.add(portfolioAccountDTO.getClientId());
        Collection<PortfolioAccountData> accounts = null;
        String sql = null;
        String defaultAccountStatus = "300";
        if (portfolioAccountDTO.getAccountStatus() != null) {
            for (final long status : portfolioAccountDTO.getAccountStatus()) {
                defaultAccountStatus += ", " + status;
            }
            defaultAccountStatus = defaultAccountStatus.substring(defaultAccountStatus.indexOf(",") + 1);
        }
        final PortfolioAccountType accountType = PortfolioAccountType.fromInt(portfolioAccountDTO.getAccountTypeId());
        switch (accountType) {
            case INVALID:
            break;
            case LOAN:
                sql = "select " + this.loanAccountMapper.schema() + " where ";
                if (portfolioAccountDTO.getClientId() != null) {
                    sql += " la.client_id = ? and la.loan_status_id in (" + defaultAccountStatus.toString() + ") ";
                    sqlParams.add(portfolioAccountDTO.getClientId());
                } else {
                    sql += " la.loan_status_id in (" + defaultAccountStatus.toString() + ") ";
                }
                if (portfolioAccountDTO.getCurrencyCode() != null) {
                    sql += " and la.currency_code = ?";
                    sqlParams.add(portfolioAccountDTO.getCurrencyCode());
                }

                accounts = this.jdbcTemplate.query(sql, this.loanAccountMapper, sqlParams.toArray());
            break;
            case SAVINGS:
                sql = "select " + this.savingsAccountMapper.schema() + " where ";
                if (portfolioAccountDTO.getClientId() != null) {
                    sql += " sa.client_id = ? and sa.status_enum in (" + defaultAccountStatus.toString() + ") ";
                    sqlParams.add(portfolioAccountDTO.getClientId());
                } else {
                    sql += " sa.status_enum in (" + defaultAccountStatus.toString() + ") ";
                }
                if (portfolioAccountDTO.getCurrencyCode() != null) {
                    sql += " and sa.currency_code = ?";
                    sqlParams.add(portfolioAccountDTO.getCurrencyCode());
                }

                if (portfolioAccountDTO.getDepositType() != null) {
                    sql += " and sa.deposit_type_enum = ?";
                    sqlParams.add(portfolioAccountDTO.getDepositType());
                }
                
                if(portfolioAccountDTO.isExcludeOverDraftAccounts()){
                    sql += " and sa.allow_overdraft = 0";
                }
                
                if(portfolioAccountDTO.getClientId() == null && portfolioAccountDTO.getGroupId() != null){
                    sql += " and sa.group_id = ? ";
                    sqlParams.add(portfolioAccountDTO.getGroupId());
                }
                
                accounts = this.jdbcTemplate.query(sql, this.savingsAccountMapper, sqlParams.toArray());
            break;
            default:
            break;
        }

        return accounts;
    }

    private static final class PortfolioSavingsAccountMapper implements RowMapper<PortfolioAccountData> {

        private final String schemaSql;

        public PortfolioSavingsAccountMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sa.id as id, sa.account_no as accountNo, sa.external_id as externalId, ");
            sqlBuilder.append("c.id as clientId, c.display_name as clientName, ");
            sqlBuilder.append("g.id as groupId, g.display_name as groupName, ");
            sqlBuilder.append("sp.id as productId, sp.name as productName, ");
            sqlBuilder.append("s.id as fieldOfficerId, s.display_name as fieldOfficerName, ");
            sqlBuilder.append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits,");
            sqlBuilder.append("sa.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol ");
            sqlBuilder.append("from m_savings_account sa ");
            sqlBuilder.append("join m_savings_product sp ON sa.product_id = sp.id ");
            sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");
            sqlBuilder.append("left join m_client c ON c.id = sa.client_id ");
            sqlBuilder.append("left join m_group g ON g.id = sa.group_id ");
            sqlBuilder.append("left join m_staff s ON s.id = sa.field_officer_id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public PortfolioAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("clientName");

            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");

            final Long fieldOfficerId = rs.getLong("fieldOfficerId");
            final String fieldOfficerName = rs.getString("fieldOfficerName");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMulitplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMulitplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            return new PortfolioAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                    fieldOfficerId, fieldOfficerName, currency);
        }
    }

    private static final class PortfolioLoanAccountMapper implements RowMapper<PortfolioAccountData> {

        private final String schemaSql;

        public PortfolioLoanAccountMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("la.id as id, la.account_no as accountNo, la.external_id as externalId, ");
            sqlBuilder.append("c.id as clientId, c.display_name as clientName, ");
            sqlBuilder.append("g.id as groupId, g.display_name as groupName, ");
            sqlBuilder.append("lp.id as productId, lp.name as productName, ");
            sqlBuilder.append("s.id as fieldOfficerId, s.display_name as fieldOfficerName, ");
            sqlBuilder.append("la.currency_code as currencyCode, la.currency_digits as currencyDigits,");
            sqlBuilder.append("la.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("la.total_overpaid_derived as totalOverpaid, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol ");
            sqlBuilder.append("from m_loan la ");
            sqlBuilder.append("join m_product_loan lp ON la.product_id = lp.id ");
            sqlBuilder.append("join m_currency curr on curr.code = la.currency_code ");
            sqlBuilder.append("left join m_client c ON c.id = la.client_id ");
            sqlBuilder.append("left join m_group g ON g.id = la.group_id ");
            sqlBuilder.append("left join m_staff s ON s.id = la.loan_officer_id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public PortfolioAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("clientName");

            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");

            final Long fieldOfficerId = rs.getLong("fieldOfficerId");
            final String fieldOfficerName = rs.getString("fieldOfficerName");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMulitplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final BigDecimal amtForTransfer = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalOverpaid");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMulitplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            return new PortfolioAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                    fieldOfficerId, fieldOfficerName, currency, amtForTransfer);
        }
    }
    
    private static final class PortfolioLoanAccountRefundByTransferMapper implements RowMapper<PortfolioAccountData> {

        private final String schemaSql;

        public PortfolioLoanAccountRefundByTransferMapper() {
            
            final StringBuilder amountQueryString = new StringBuilder(400);
            amountQueryString.append("(select (SUM(ifnull(mr.principal_completed_derived, 0)) +"); 
            amountQueryString.append("SUM(ifnull(mr.interest_completed_derived, 0)) + "); 
             amountQueryString.append("SUM(ifnull(mr.fee_charges_completed_derived, 0)) + "); 
             amountQueryString.append(" SUM(ifnull(mr.penalty_charges_completed_derived, 0))) as total_in_advance_derived"); 
             amountQueryString.append(" from m_loan ml INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id"); 
             amountQueryString.append(" where ml.id=? and ml.loan_status_id = 300"); 
             amountQueryString.append("  and  mr.duedate >= CURDATE() group by ml.id having"); 
             amountQueryString.append(" (SUM(ifnull(mr.principal_completed_derived, 0)) + "); 
             amountQueryString.append(" SUM(ifnull(mr.interest_completed_derived, 0)) + "); 
             amountQueryString.append("SUM(ifnull(mr.fee_charges_completed_derived, 0)) + "); 
             amountQueryString.append("SUM(ifnull(mr.penalty_charges_completed_derived, 0))) > 0) as totalOverpaid ");

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("la.id as id, la.account_no as accountNo, la.external_id as externalId, ");
            sqlBuilder.append("c.id as clientId, c.display_name as clientName, ");
            sqlBuilder.append("g.id as groupId, g.display_name as groupName, ");
            sqlBuilder.append("lp.id as productId, lp.name as productName, ");
            sqlBuilder.append("s.id as fieldOfficerId, s.display_name as fieldOfficerName, ");
            sqlBuilder.append("la.currency_code as currencyCode, la.currency_digits as currencyDigits,");
            sqlBuilder.append("la.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append(amountQueryString.toString());
            sqlBuilder.append(", ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol ");
            sqlBuilder.append("from m_loan la ");
            sqlBuilder.append("join m_product_loan lp ON la.product_id = lp.id ");
            sqlBuilder.append("join m_currency curr on curr.code = la.currency_code ");
            sqlBuilder.append("left join m_client c ON c.id = la.client_id ");
            sqlBuilder.append("left join m_group g ON g.id = la.group_id ");
            sqlBuilder.append("left join m_staff s ON s.id = la.loan_officer_id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public PortfolioAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("clientName");

            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");

            final Long fieldOfficerId = rs.getLong("fieldOfficerId");
            final String fieldOfficerName = rs.getString("fieldOfficerName");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMulitplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final BigDecimal amtForTransfer = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalOverpaid");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMulitplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            return new PortfolioAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                    fieldOfficerId, fieldOfficerName, currency, amtForTransfer);
        }
    }
    
    @Override
    public PortfolioAccountData retrieveOneByPaidInAdvance(Long accountId, Integer accountTypeId) {
        // TODO Auto-generated method stub
        Object[] sqlParams = new Object[] { accountId , accountId};
        PortfolioAccountData accountData = null;
        //String currencyCode = null;
        try {
            String sql = null;
            //final PortfolioAccountType accountType = PortfolioAccountType.fromInt(accountTypeId);
           
                    sql = "select " + this.accountRefundByTransferMapper.schema() + " where la.id = ?";
                  /*  if (currencyCode != null) {
                        sql += " and la.currency_code = ?";
                        sqlParams = new Object[] {accountId , accountId,currencyCode };
                    }*/

                    accountData = this.jdbcTemplate.queryForObject(sql, this.accountRefundByTransferMapper, sqlParams);
         
        } catch (final EmptyResultDataAccessException e) {
            throw new AccountTransferNotFoundException(accountId);
        }

        return accountData;
    }
}