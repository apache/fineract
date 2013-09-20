package org.mifosplatform.portfolio.accountdetails.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.accountdetails.data.AccountSummaryCollectionData;
import org.mifosplatform.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.mifosplatform.portfolio.accountdetails.data.SavingsAccountSummaryData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.data.LoanStatusEnumData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsReadPlatformServiceJpaRepositoryImpl implements AccountDetailsReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;

    @Autowired
    public AccountDetailsReadPlatformServiceJpaRepositoryImpl(final ClientReadPlatformService clientReadPlatformService,
            final RoutingDataSource dataSource, final GroupReadPlatformService groupReadPlatformService) {
        this.clientReadPlatformService = clientReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.groupReadPlatformService = groupReadPlatformService;
    }

    @Override
    public AccountSummaryCollectionData retrieveClientAccountDetails(final Long clientId) {
        // Check if client exists
        this.clientReadPlatformService.retrieveOne(clientId);
        final String loanwhereClause = " where l.client_id = ?";
        final String savingswhereClause = " where sa.client_id = ? order by sa.status_enum ASC, sa.account_no ASC";
        final List<LoanAccountSummaryData> loanAccounts = retrieveLoanAccountDetails(loanwhereClause, new Object[] { clientId });
        final List<SavingsAccountSummaryData> savingsAccounts = retrieveAccountDetails(savingswhereClause, new Object[] { clientId });
        return new AccountSummaryCollectionData(loanAccounts, savingsAccounts);
    }

    @Override
    public AccountSummaryCollectionData retrieveGroupAccountDetails(final Long groupId) {
        // Check if group exists
        this.groupReadPlatformService.retrieveOne(groupId);
        final String loanWhereClauseForGroup = " where l.group_id = ? and l.client_id is null";
        final String loanWhereClauseForMembers = " where l.group_id = ? and l.client_id is not null";
        final String savingswhereClauseForGroup = " where sa.group_id = ? and sa.client_id is null order by sa.status_enum ASC, sa.account_no ASC";
        final String savingswhereClauseForMembers = " where sa.group_id = ? and sa.client_id is not null order by sa.status_enum ASC, sa.account_no ASC";
        final List<LoanAccountSummaryData> groupLoanAccounts = retrieveLoanAccountDetails(loanWhereClauseForGroup, new Object[] { groupId });
        final List<SavingsAccountSummaryData> groupSavingsAccounts = retrieveAccountDetails(savingswhereClauseForGroup,
                new Object[] { groupId });
        final List<LoanAccountSummaryData> memberLoanAccounts = retrieveLoanAccountDetails(loanWhereClauseForMembers,
                new Object[] { groupId });
        final List<SavingsAccountSummaryData> memberSavingsAccounts = retrieveAccountDetails(savingswhereClauseForMembers,
                new Object[] { groupId });
        return new AccountSummaryCollectionData(groupLoanAccounts, groupSavingsAccounts, memberLoanAccounts, memberSavingsAccounts);
    }

    @Override
    public Collection<LoanAccountSummaryData> retrieveClientLoanAccountsByLoanOfficerId(final Long clientId, final Long loanOfficerId) {
        // Check if client exists
        this.clientReadPlatformService.retrieveOne(clientId);
        final String loanWhereClause = " where l.client_id = ? and l.loan_officer_id = ?";
        return retrieveLoanAccountDetails(loanWhereClause, new Object[] { clientId, loanOfficerId });
    }

    @Override
    public Collection<LoanAccountSummaryData> retrieveGroupLoanAccountsByLoanOfficerId(final Long groupId, final Long loanOfficerId) {
        // Check if group exists
        this.groupReadPlatformService.retrieveOne(groupId);
        final String loanWhereClause = " where l.group_id = ? and l.client_id is null and l.loan_officer_id = ?";
        return retrieveLoanAccountDetails(loanWhereClause, new Object[] { groupId, loanOfficerId });
    }

    private List<LoanAccountSummaryData> retrieveLoanAccountDetails(final String loanwhereClause, final Object[] inputs) {
        final LoanAccountSummaryDataMapper rm = new LoanAccountSummaryDataMapper();
        final String sql = "select " + rm.loanAccountSummarySchema() + loanwhereClause;
        return this.jdbcTemplate.query(sql, rm, inputs);
    }

    /**
     * @param entityId
     * @return
     */
    private List<SavingsAccountSummaryData> retrieveAccountDetails(final String savingswhereClause, final Object[] inputs) {
        final SavingsAccountSummaryDataMapper savingsAccountSummaryDataMapper = new SavingsAccountSummaryDataMapper();
        final String savingsSql = "select " + savingsAccountSummaryDataMapper.schema() + savingswhereClause;
        return this.jdbcTemplate.query(savingsSql, savingsAccountSummaryDataMapper, inputs);
    }

    private static final class SavingsAccountSummaryDataMapper implements RowMapper<SavingsAccountSummaryData> {

        final String schemaSql;

        public SavingsAccountSummaryDataMapper() {
            final StringBuilder accountsSummary = new StringBuilder();
            accountsSummary.append("sa.id as id, sa.account_no as accountNo, sa.external_id as externalId, sa.status_enum as statusEnum, ");
            accountsSummary.append("sa.account_type_enum as accountType, ");
            accountsSummary.append("sa.account_balance_derived as accountBalance, ");
            accountsSummary
                    .append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
            accountsSummary.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            accountsSummary.append("curr.display_symbol as currencyDisplaySymbol, ");
            accountsSummary.append("sa.product_id as productId, p.name as productName ");
            accountsSummary.append("from m_savings_account sa ");
            accountsSummary.append("join m_savings_product as p on p.id = sa.product_id ");
            accountsSummary.append("join m_currency curr on curr.code = sa.currency_code ");

            this.schemaSql = accountsSummary.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String productName = rs.getString("productName");
            final Integer statusId = JdbcSupport.getInteger(rs, "statusEnum");
            final BigDecimal accountBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "accountBalance");
            final SavingsAccountStatusEnumData status = SavingsEnumerations.status(statusId);
            final Integer accountType = JdbcSupport.getInteger(rs, "accountType");
            final EnumOptionData accountTypeData = AccountEnumerations.loanType(accountType);

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            return new SavingsAccountSummaryData(id, accountNo, externalId, productId, productName, status, currency, accountBalance,
                    accountTypeData);
        }
    }

    private static final class LoanAccountSummaryDataMapper implements RowMapper<LoanAccountSummaryData> {

        public String loanAccountSummarySchema() {

            final StringBuilder accountsSummary = new StringBuilder("l.id as id, l.account_no as accountNo, l.external_id as externalId,");
            accountsSummary.append("l.product_id as productId, lp.name as productName,")
                    .append("l.loan_status_id as statusId, l.loan_type_enum as loanType, ").append("l.loan_product_counter as loanCycle ")
                    .append(" from m_loan l ").append("LEFT JOIN m_product_loan AS lp ON lp.id = l.product_id");

            return accountsSummary.toString();
        }

        @Override
        public LoanAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String loanProductName = rs.getString("productName");
            final Integer loanStatusId = JdbcSupport.getInteger(rs, "statusId");
            final LoanStatusEnumData loanStatus = LoanEnumerations.status(loanStatusId);
            final Integer loanTypeId = JdbcSupport.getInteger(rs, "loanType");
            final EnumOptionData loanType = AccountEnumerations.loanType(loanTypeId);
            final Integer loanCycle = JdbcSupport.getInteger(rs, "loanCycle");

            return new LoanAccountSummaryData(id, accountNo, externalId, productId, loanProductName, loanStatus, loanType, loanCycle);
        }
    }

}
