/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accountdetails.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.accountdetails.data.AccountSummaryCollectionData;
import org.mifosplatform.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.mifosplatform.portfolio.accountdetails.data.SavingsAccountSummaryData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.mifosplatform.portfolio.loanaccount.data.LoanStatusEnumData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.portfolio.savings.data.SavingsAccountApplicationTimelineData;
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

            accountsSummary.append("sa.submittedon_date as submittedOnDate,");
            accountsSummary.append("sbu.username as submittedByUsername,");
            accountsSummary.append("sbu.firstname as submittedByFirstname, sbu.lastname as submittedByLastname,");

            accountsSummary.append("sa.rejectedon_date as rejectedOnDate,");
            accountsSummary.append("rbu.username as rejectedByUsername,");
            accountsSummary.append("rbu.firstname as rejectedByFirstname, rbu.lastname as rejectedByLastname,");

            accountsSummary.append("sa.withdrawnon_date as withdrawnOnDate,");
            accountsSummary.append("wbu.username as withdrawnByUsername,");
            accountsSummary.append("wbu.firstname as withdrawnByFirstname, wbu.lastname as withdrawnByLastname,");

            accountsSummary.append("sa.approvedon_date as approvedOnDate,");
            accountsSummary.append("abu.username as approvedByUsername,");
            accountsSummary.append("abu.firstname as approvedByFirstname, abu.lastname as approvedByLastname,");

            accountsSummary.append("sa.activatedon_date as activatedOnDate,");
            accountsSummary.append("avbu.username as activatedByUsername,");
            accountsSummary.append("avbu.firstname as activatedByFirstname, avbu.lastname as activatedByLastname,");

            accountsSummary.append("sa.closedon_date as closedOnDate,");
            accountsSummary.append("cbu.username as closedByUsername,");
            accountsSummary.append("cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname,");

            accountsSummary
                    .append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
            accountsSummary.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            accountsSummary.append("curr.display_symbol as currencyDisplaySymbol, ");
            accountsSummary.append("sa.product_id as productId, p.name as productName, p.short_name as shortProductName, ");
            accountsSummary.append("sa.deposit_type_enum as depositType ");
            accountsSummary.append("from m_savings_account sa ");
            accountsSummary.append("join m_savings_product as p on p.id = sa.product_id ");
            accountsSummary.append("join m_currency curr on curr.code = sa.currency_code ");
            accountsSummary.append("left join m_appuser sbu on sbu.id = sa.submittedon_userid ");
            accountsSummary.append("left join m_appuser rbu on rbu.id = sa.rejectedon_userid ");
            accountsSummary.append("left join m_appuser wbu on wbu.id = sa.withdrawnon_userid ");
            accountsSummary.append("left join m_appuser abu on abu.id = sa.approvedon_userid ");
            accountsSummary.append("left join m_appuser avbu on rbu.id = sa.activatedon_userid ");
            accountsSummary.append("left join m_appuser cbu on cbu.id = sa.closedon_userid ");

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
            final String shortProductName = rs.getString("shortProductName");
            final Integer statusId = JdbcSupport.getInteger(rs, "statusEnum");
            final BigDecimal accountBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "accountBalance");
            final SavingsAccountStatusEnumData status = SavingsEnumerations.status(statusId);
            final Integer accountType = JdbcSupport.getInteger(rs, "accountType");
            final EnumOptionData accountTypeData = AccountEnumerations.loanType(accountType);
            final Integer depositTypeId = JdbcSupport.getInteger(rs, "depositType");
            final EnumOptionData depositTypeData = SavingsEnumerations.depositType(depositTypeId);
            
            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final LocalDate rejectedOnDate = JdbcSupport.getLocalDate(rs, "rejectedOnDate");
            final String rejectedByUsername = rs.getString("rejectedByUsername");
            final String rejectedByFirstname = rs.getString("rejectedByFirstname");
            final String rejectedByLastname = rs.getString("rejectedByLastname");

            final LocalDate withdrawnOnDate = JdbcSupport.getLocalDate(rs, "withdrawnOnDate");
            final String withdrawnByUsername = rs.getString("withdrawnByUsername");
            final String withdrawnByFirstname = rs.getString("withdrawnByFirstname");
            final String withdrawnByLastname = rs.getString("withdrawnByLastname");

            final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, "approvedOnDate");
            final String approvedByUsername = rs.getString("approvedByUsername");
            final String approvedByFirstname = rs.getString("approvedByFirstname");
            final String approvedByLastname = rs.getString("approvedByLastname");

            final LocalDate activatedOnDate = JdbcSupport.getLocalDate(rs, "activatedOnDate");
            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final SavingsAccountApplicationTimelineData timeline = new SavingsAccountApplicationTimelineData(submittedOnDate,
                    submittedByUsername, submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername,
                    rejectedByFirstname, rejectedByLastname, withdrawnOnDate, withdrawnByUsername, withdrawnByFirstname,
                    withdrawnByLastname, approvedOnDate, approvedByUsername, approvedByFirstname, approvedByLastname, activatedOnDate,
                    activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate, closedByUsername, closedByFirstname,
                    closedByLastname);

            return new SavingsAccountSummaryData(id, accountNo, externalId, productId, productName, shortProductName, status, currency, accountBalance,
                    accountTypeData, timeline, depositTypeData);
        }
    }

    private static final class LoanAccountSummaryDataMapper implements RowMapper<LoanAccountSummaryData> {

        public String loanAccountSummarySchema() {

            final StringBuilder accountsSummary = new StringBuilder("l.id as id, l.account_no as accountNo, l.external_id as externalId,");
            accountsSummary
                    .append(" l.product_id as productId, lp.name as productName, lp.short_name as shortProductName,")
                    .append(" l.loan_status_id as statusId, l.loan_type_enum as loanType,")
                    
                    .append("l.principal_disbursed_derived as originalLoan,")
                    .append("l.total_outstanding_derived as loanBalance,")
                    .append("l.total_repayment_derived as amountPaid,")
                    
                    .append(" l.loan_product_counter as loanCycle,")

                    .append(" l.submittedon_date as submittedOnDate,")
                    .append(" sbu.username as submittedByUsername, sbu.firstname as submittedByFirstname, sbu.lastname as submittedByLastname,")

                    .append(" l.rejectedon_date as rejectedOnDate,")
                    .append(" rbu.username as rejectedByUsername, rbu.firstname as rejectedByFirstname, rbu.lastname as rejectedByLastname,")

                    .append(" l.withdrawnon_date as withdrawnOnDate,")
                    .append(" wbu.username as withdrawnByUsername, wbu.firstname as withdrawnByFirstname, wbu.lastname as withdrawnByLastname,")

                    .append(" l.approvedon_date as approvedOnDate,")
                    .append(" abu.username as approvedByUsername, abu.firstname as approvedByFirstname, abu.lastname as approvedByLastname,")

                    .append(" l.expected_disbursedon_date as expectedDisbursementDate, l.disbursedon_date as actualDisbursementDate,")
                    .append(" dbu.username as disbursedByUsername, dbu.firstname as disbursedByFirstname, dbu.lastname as disbursedByLastname,")

                    .append(" l.closedon_date as closedOnDate,")
                    .append(" cbu.username as closedByUsername, cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname,")
                    .append(" la.overdue_since_date_derived as overdueSinceDate,")
                    .append(" l.writtenoffon_date as writtenOffOnDate, l.expected_maturedon_date as expectedMaturityDate")

                    .append(" from m_loan l ").append("LEFT JOIN m_product_loan AS lp ON lp.id = l.product_id")
                    .append(" left join m_appuser sbu on sbu.id = l.submittedon_userid")
                    .append(" left join m_appuser rbu on rbu.id = l.rejectedon_userid")
                    .append(" left join m_appuser wbu on wbu.id = l.withdrawnon_userid")
                    .append(" left join m_appuser abu on abu.id = l.approvedon_userid")
                    .append(" left join m_appuser dbu on dbu.id = l.disbursedon_userid")
                    .append(" left join m_appuser cbu on cbu.id = l.closedon_userid")
                    .append(" left join m_loan_arrears_aging la on la.loan_id = l.id");

            return accountsSummary.toString();
        }

        @Override
        public LoanAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String loanProductName = rs.getString("productName");
            final String shortLoanProductName = rs.getString("shortProductName");
            final Integer loanStatusId = JdbcSupport.getInteger(rs, "statusId");
            final LoanStatusEnumData loanStatus = LoanEnumerations.status(loanStatusId);
            final Integer loanTypeId = JdbcSupport.getInteger(rs, "loanType");
            final EnumOptionData loanType = AccountEnumerations.loanType(loanTypeId);
            final Integer loanCycle = JdbcSupport.getInteger(rs, "loanCycle");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final LocalDate rejectedOnDate = JdbcSupport.getLocalDate(rs, "rejectedOnDate");
            final String rejectedByUsername = rs.getString("rejectedByUsername");
            final String rejectedByFirstname = rs.getString("rejectedByFirstname");
            final String rejectedByLastname = rs.getString("rejectedByLastname");

            final LocalDate withdrawnOnDate = JdbcSupport.getLocalDate(rs, "withdrawnOnDate");
            final String withdrawnByUsername = rs.getString("withdrawnByUsername");
            final String withdrawnByFirstname = rs.getString("withdrawnByFirstname");
            final String withdrawnByLastname = rs.getString("withdrawnByLastname");

            final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, "approvedOnDate");
            final String approvedByUsername = rs.getString("approvedByUsername");
            final String approvedByFirstname = rs.getString("approvedByFirstname");
            final String approvedByLastname = rs.getString("approvedByLastname");

            final LocalDate expectedDisbursementDate = JdbcSupport.getLocalDate(rs, "expectedDisbursementDate");
            final LocalDate actualDisbursementDate = JdbcSupport.getLocalDate(rs, "actualDisbursementDate");
            final String disbursedByUsername = rs.getString("disbursedByUsername");
            final String disbursedByFirstname = rs.getString("disbursedByFirstname");
            final String disbursedByLastname = rs.getString("disbursedByLastname");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");
            
            final BigDecimal originalLoan = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,"originalLoan");
            final BigDecimal loanBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,"loanBalance");
            final BigDecimal amountPaid = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,"amountPaid");

            final LocalDate writtenOffOnDate = JdbcSupport.getLocalDate(rs, "writtenOffOnDate");

            final LocalDate expectedMaturityDate = JdbcSupport.getLocalDate(rs, "expectedMaturityDate");

            final LocalDate overdueSinceDate = JdbcSupport.getLocalDate(rs, "overdueSinceDate");
            Boolean inArrears = true;
            if (overdueSinceDate == null) {
                inArrears = false;
            }

            final LoanApplicationTimelineData timeline = new LoanApplicationTimelineData(submittedOnDate, submittedByUsername,
                    submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername, rejectedByFirstname, rejectedByLastname,
                    withdrawnOnDate, withdrawnByUsername, withdrawnByFirstname, withdrawnByLastname, approvedOnDate, approvedByUsername,
                    approvedByFirstname, approvedByLastname, expectedDisbursementDate, actualDisbursementDate, disbursedByUsername,
                    disbursedByFirstname, disbursedByLastname, closedOnDate, closedByUsername, closedByFirstname, closedByLastname,
                    expectedMaturityDate, writtenOffOnDate, closedByUsername, closedByFirstname, closedByLastname);

            return new LoanAccountSummaryData(id, accountNo, externalId, productId, loanProductName, shortLoanProductName, loanStatus, loanType, loanCycle,
                    timeline, inArrears,originalLoan,loanBalance,amountPaid);
        }
    }

}
