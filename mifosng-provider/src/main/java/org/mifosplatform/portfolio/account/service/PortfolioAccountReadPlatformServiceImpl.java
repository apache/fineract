/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.data.PortfolioAccountData;
import org.mifosplatform.portfolio.account.exception.AccountTransferNotFoundException;
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

    @Autowired
    public PortfolioAccountReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.savingsAccountMapper = new PortfolioSavingsAccountMapper();
        this.loanAccountMapper = new PortfolioLoanAccountMapper();
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
                        sqlParams = new Object[] {accountId, currencyCode};
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
    public Collection<PortfolioAccountData> retrieveAllForLookup(final Integer accountTypeId, final Long clientId, long[] accountStatus) {
        return retrieveAllForLookup(accountTypeId, clientId, null, accountStatus);
    }

    @Override
    public Collection<PortfolioAccountData> retrieveAllForLookup(final Integer accountTypeId, final Long clientId,
            final String currencyCode, long[] accountStatus) {

        Object[] sqlParams = new Object[] { clientId };
        Collection<PortfolioAccountData> accounts = null;
        String sql = null;
        String defaultAccountStatus = "300";
        if (accountStatus != null) {
            for (long status : accountStatus) {
                defaultAccountStatus += ", " + status;
            }
            defaultAccountStatus = defaultAccountStatus.substring(defaultAccountStatus.indexOf(",")+1);
        }
        final PortfolioAccountType accountType = PortfolioAccountType.fromInt(accountTypeId);
        switch (accountType) {
            case INVALID:
            break;
            case LOAN:
                sql = "select " + this.loanAccountMapper.schema() + " where la.client_id = ? and la.loan_status_id in ("
                        + defaultAccountStatus.toString() + ")";
                if (currencyCode != null) {
                    sql += " and la.currency_code = ?";
                    sqlParams = new Object[] { clientId, currencyCode };
                }

                accounts = this.jdbcTemplate.query(sql, this.loanAccountMapper, sqlParams);
            break;
            case SAVINGS:
                sql = "select " + this.savingsAccountMapper.schema() + " where sa.client_id = ? and sa.status_enum in ("
                        + defaultAccountStatus.toString() + ")";
                if (currencyCode != null) {
                    sql += " and sa.currency_code = ?";
                    sqlParams = new Object[] { clientId, currencyCode };
                }

                accounts = this.jdbcTemplate.query(sql, this.savingsAccountMapper, sqlParams);
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
}