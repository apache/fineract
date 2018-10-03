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
package org.apache.fineract.portfolio.accountdetails.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.accountdetails.data.AccountSummaryCollectionData;
import org.apache.fineract.portfolio.accountdetails.data.GuarantorAccountSummaryData;
import org.apache.fineract.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.apache.fineract.portfolio.accountdetails.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.accountdetails.data.ShareAccountSummaryData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.savings.data.SavingsAccountApplicationTimelineData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSubStatusEnumData;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountApplicationTimelineData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountStatusEnumData;
import org.apache.fineract.portfolio.shareaccounts.service.SharesEnumerations;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsReadPlatformServiceJpaRepositoryImpl implements AccountDetailsReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final ColumnValidator columnValidator;

    @Autowired
    public AccountDetailsReadPlatformServiceJpaRepositoryImpl(final ClientReadPlatformService clientReadPlatformService,
            final RoutingDataSource dataSource, final GroupReadPlatformService groupReadPlatformService,
            final ColumnValidator columnValidator) {
        this.clientReadPlatformService = clientReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.groupReadPlatformService = groupReadPlatformService;
        this.columnValidator = columnValidator;
    }

    @Override
    public AccountSummaryCollectionData retrieveClientAccountDetails(final Long clientId) {
        // Check if client exists
        this.clientReadPlatformService.retrieveOne(clientId);
        final String loanwhereClause = " where l.client_id = ?";
        final String savingswhereClause = " where sa.client_id = ? order by sa.status_enum ASC, sa.account_no ASC";
        final String guarantorWhereClause = " where g.entity_id = ? and g.is_active = 1 order by l.account_no ASC";

        final List<LoanAccountSummaryData> loanAccounts = retrieveLoanAccountDetails(loanwhereClause, new Object[] { clientId });
        final List<SavingsAccountSummaryData> savingsAccounts = retrieveAccountDetails(savingswhereClause, new Object[] { clientId });
        final List<ShareAccountSummaryData> shareAccounts = retrieveShareAccountDetails(clientId) ;
        final List<GuarantorAccountSummaryData> guarantorloanAccounts = retrieveGuarantorLoanAccountDetails(
				guarantorWhereClause, new Object[] { clientId });
        return new AccountSummaryCollectionData(loanAccounts, savingsAccounts, shareAccounts, guarantorloanAccounts);
    }

    @Override
    public AccountSummaryCollectionData retrieveGroupAccountDetails(final Long groupId) {
        // Check if group exists
        this.groupReadPlatformService.retrieveOne(groupId);
        final String loanWhereClauseForGroup = " where l.group_id = ? and l.client_id is null";
        final String loanWhereClauseForMembers = " where l.group_id = ? and l.client_id is not null";
        final String savingswhereClauseForGroup = " where sa.group_id = ? and sa.client_id is null order by sa.status_enum ASC, sa.account_no ASC";
        final String savingswhereClauseForMembers = " where sa.group_id = ? and sa.client_id is not null order by sa.status_enum ASC, sa.account_no ASC";
        final String guarantorWhereClauseForGroup = " where l.group_id = ? and l.client_id is null and g.is_active = 1 order by l.account_no ASC";
        final String guarantorWhereClauseForMembers = " where l.group_id = ? and l.client_id is not null and g.is_active = 1 order by l.account_no ASC";

        final List<LoanAccountSummaryData> groupLoanAccounts = retrieveLoanAccountDetails(loanWhereClauseForGroup, new Object[] { groupId });
        final List<SavingsAccountSummaryData> groupSavingsAccounts = retrieveAccountDetails(savingswhereClauseForGroup,
                new Object[] { groupId });
        final List<GuarantorAccountSummaryData> groupGuarantorloanAccounts = retrieveGuarantorLoanAccountDetails(
        		guarantorWhereClauseForGroup, new Object[] { groupId });
        final List<LoanAccountSummaryData> memberLoanAccounts = retrieveLoanAccountDetails(loanWhereClauseForMembers,
                new Object[] { groupId });
        final List<SavingsAccountSummaryData> memberSavingsAccounts = retrieveAccountDetails(savingswhereClauseForMembers,
                new Object[] { groupId });
        final List<GuarantorAccountSummaryData> memberGuarantorloanAccounts = retrieveGuarantorLoanAccountDetails(
        		guarantorWhereClauseForMembers, new Object[] { groupId });
        return new AccountSummaryCollectionData(groupLoanAccounts, groupSavingsAccounts, groupGuarantorloanAccounts, memberLoanAccounts, memberSavingsAccounts, memberGuarantorloanAccounts);
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

    @Override public Collection<LoanAccountSummaryData> retrieveClientActiveLoanAccountSummary(final Long clientId) {
        final String loanWhereClause = " where l.client_id = ? and l.loan_status_id = 300 ";
        return retrieveLoanAccountDetails(loanWhereClause, new Object[] { clientId });
    }

    private List<LoanAccountSummaryData> retrieveLoanAccountDetails(final String loanwhereClause, final Object[] inputs) {
        final LoanAccountSummaryDataMapper rm = new LoanAccountSummaryDataMapper();
        final String sql = "select " + rm.loanAccountSummarySchema() + loanwhereClause;
        this.columnValidator.validateSqlInjection(rm.loanAccountSummarySchema(), loanwhereClause);
        return this.jdbcTemplate.query(sql, rm, inputs);
    }

    /**
     * @param entityId
     * @return
     */
    private List<SavingsAccountSummaryData> retrieveAccountDetails(final String savingswhereClause, final Object[] inputs) {
        final SavingsAccountSummaryDataMapper savingsAccountSummaryDataMapper = new SavingsAccountSummaryDataMapper();
        final String savingsSql = "select " + savingsAccountSummaryDataMapper.schema() + savingswhereClause;
        this.columnValidator.validateSqlInjection(savingsAccountSummaryDataMapper.schema() , savingswhereClause);
        return this.jdbcTemplate.query(savingsSql, savingsAccountSummaryDataMapper, inputs);
    }

    private List<ShareAccountSummaryData> retrieveShareAccountDetails(final Long clientId) {
    	final ShareAccountSummaryDataMapper mapper = new ShareAccountSummaryDataMapper() ;
    	final String query = "select " + mapper.schema() + " where sa.client_id = ?" ;
    	  return this.jdbcTemplate.query(query, mapper, new Object [] {clientId});
    }
    
    private List<GuarantorAccountSummaryData> retrieveGuarantorLoanAccountDetails(
			final String loanwhereClause, final Object[] inputs) {
		final GuarantorLoanAccountSummaryDataMapper rm = new GuarantorLoanAccountSummaryDataMapper();
		final String sql = "select " + rm.guarantorLoanAccountSummarySchema()
				+ loanwhereClause;
		return this.jdbcTemplate.query(sql, rm, inputs);
	}
    
    private final static class ShareAccountSummaryDataMapper implements RowMapper<ShareAccountSummaryData> {

    	private final String schema ;
    	
    	ShareAccountSummaryDataMapper() {
    		final StringBuffer buff = new StringBuffer()
    		.append("sa.id as id, sa.external_id as externalId, sa.status_enum as statusEnum, ")
    		.append("sa.account_no as accountNo, sa.total_approved_shares as approvedShares, sa.total_pending_shares as pendingShares, ")
    		.append("sa.savings_account_id as savingsAccountNo, sa.minimum_active_period_frequency as minimumactivePeriod,")
    		.append("sa.minimum_active_period_frequency_enum as minimumactivePeriodEnum,") 
    		.append("sa.lockin_period_frequency as lockinPeriod, sa.lockin_period_frequency_enum as lockinPeriodEnum, ")
    		.append("sa.submitted_date as submittedDate, sbu.username as submittedByUsername, ")
    		.append("sbu.firstname as submittedByFirstname, sbu.lastname as submittedByLastname, ") 
    		.append("sa.rejected_date as rejectedDate, rbu.username as rejectedByUsername, ")
    		.append("rbu.firstname as rejectedByFirstname, rbu.lastname as rejectedByLastname, ")
    		.append("sa.approved_date as approvedDate, abu.username as approvedByUsername, ")
    		.append("abu.firstname as approvedByFirstname, abu.lastname as approvedByLastname, ")
    		.append("sa.activated_date as activatedDate, avbu.username as activatedByUsername, ")
    		.append("avbu.firstname as activatedByFirstname, avbu.lastname as activatedByLastname, ")
    		.append("sa.closed_date as closedDate, cbu.username as closedByUsername, ")
    		.append("cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname, ")
    		.append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ")
    		.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ")
    		.append("curr.display_symbol as currencyDisplaySymbol, sa.product_id as productId, p.name as productName, p.short_name as shortProductName ")
    		.append("from m_share_account sa ")
    		.append("join m_share_product as p on p.id = sa.product_id ")
    		.append("join m_currency curr on curr.code = sa.currency_code ")
    		.append("left join m_appuser sbu on sbu.id = sa.submitted_userid ")
    		.append("left join m_appuser rbu on rbu.id = sa.rejected_userid ")
    		.append("left join m_appuser abu on abu.id = sa.approved_userid ")
    		.append("left join m_appuser avbu on avbu.id = sa.activated_userid ")
    		.append("left join m_appuser cbu on cbu.id = sa.closed_userid ") ;
    		schema = buff.toString() ;
		}
		@Override
		public ShareAccountSummaryData mapRow(ResultSet rs, int rowNum)
				throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final Long approvedShares = JdbcSupport.getLong(rs, "approvedShares");
            final Long pendingShares = JdbcSupport.getLong(rs, "pendingShares");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String productName = rs.getString("productName");
            final String shortProductName = rs.getString("shortProductName");
            final Integer statusId = JdbcSupport.getInteger(rs, "statusEnum");
            final ShareAccountStatusEnumData status = SharesEnumerations.status(statusId) ;
            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final LocalDate rejectedOnDate = JdbcSupport.getLocalDate(rs, "rejectedDate");
            final String rejectedByUsername = rs.getString("rejectedByUsername");
            final String rejectedByFirstname = rs.getString("rejectedByFirstname");
            final String rejectedByLastname = rs.getString("rejectedByLastname");

            final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, "approvedDate");
            final String approvedByUsername = rs.getString("approvedByUsername");
            final String approvedByFirstname = rs.getString("approvedByFirstname");
            final String approvedByLastname = rs.getString("approvedByLastname");

            final LocalDate activatedOnDate = JdbcSupport.getLocalDate(rs, "activatedDate");
            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final ShareAccountApplicationTimelineData timeline = new ShareAccountApplicationTimelineData(submittedOnDate,
                    submittedByUsername, submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername,
                    rejectedByFirstname, rejectedByLastname, approvedOnDate, approvedByUsername, approvedByFirstname, approvedByLastname, activatedOnDate,
                    activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate, closedByUsername, closedByFirstname,
                    closedByLastname);

            return new ShareAccountSummaryData(id, accountNo, externalId, productId, productName, shortProductName, status, currency,
                    approvedShares, pendingShares, timeline);
        }
    	
		public String schema() {
			return this.schema ;
		}
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

            accountsSummary.append("sa.sub_status_enum as subStatusEnum, ");
            accountsSummary.append("(select IFNULL(max(sat.transaction_date),sa.activatedon_date) ");
            accountsSummary.append("from m_savings_account_transaction as sat ");
            accountsSummary.append("where sat.is_reversed = 0 ");
            accountsSummary.append("and sat.transaction_type_enum in (1,2) ");
            accountsSummary.append("and sat.savings_account_id = sa.id) as lastActiveTransactionDate, ");

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
            final Integer subStatusEnum = JdbcSupport.getInteger(rs, "subStatusEnum");
            final SavingsAccountSubStatusEnumData subStatus = SavingsEnumerations.subStatus(subStatusEnum);
            
            final LocalDate lastActiveTransactionDate = JdbcSupport.getLocalDate(rs, "lastActiveTransactionDate");

            final SavingsAccountApplicationTimelineData timeline = new SavingsAccountApplicationTimelineData(submittedOnDate,
                    submittedByUsername, submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername,
                    rejectedByFirstname, rejectedByLastname, withdrawnOnDate, withdrawnByUsername, withdrawnByFirstname,
                    withdrawnByLastname, approvedOnDate, approvedByUsername, approvedByFirstname, approvedByLastname, activatedOnDate,
                    activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate, closedByUsername, closedByFirstname,
                    closedByLastname);

            return new SavingsAccountSummaryData(id, accountNo, externalId, productId, productName, shortProductName, status, currency, accountBalance,
                    accountTypeData, timeline, depositTypeData, subStatus, lastActiveTransactionDate);
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
    private static final class GuarantorLoanAccountSummaryDataMapper implements
		RowMapper<GuarantorAccountSummaryData> {

	public String guarantorLoanAccountSummarySchema() {

		final StringBuilder accountsSummary = new StringBuilder(
				"l.id as id, l.account_no as accountNo, l.external_id as externalId,");
		accountsSummary
				.append(" l.product_id as productId, lp.name as productName, lp.short_name as shortProductName,")
				.append(" l.loan_status_id as statusId, l.loan_type_enum as loanType,")

				.append("l.principal_disbursed_derived as originalLoan,")
				.append("l.total_outstanding_derived as loanBalance,")
				.append("l.total_repayment_derived as amountPaid,")

				.append(" l.loan_product_counter as loanCycle,")

				.append(" l.submittedon_date as submittedOnDate,")

				.append(" l.rejectedon_date as rejectedOnDate,")
				.append(" l.withdrawnon_date as withdrawnOnDate,")
				.append(" l.approvedon_date as approvedOnDate,")
				.append(" l.expected_disbursedon_date as expectedDisbursementDate, l.disbursedon_date as actualDisbursementDate,")
				.append(" l.closedon_date as closedOnDate,")
				.append(" la.overdue_since_date_derived as overdueSinceDate,")
				.append(" l.writtenoffon_date as writtenOffOnDate, l.expected_maturedon_date as expectedMaturityDate,")
				.append(" g.is_active as isActive,")
				.append(" cv.code_value as relationship,")
				.append(" sa.on_hold_funds_derived")
				.append(" from m_loan l ")
				.append(" join m_guarantor as g on g.loan_id = l.id ")
				.append(" join m_client as c on c.id = g.entity_id ")
				.append(" LEFT JOIN m_product_loan AS lp ON lp.id = l.product_id")
				.append(" left join m_loan_arrears_aging la on la.loan_id = l.id")
				.append(" left join m_code_value cv ON cv.id = g.client_reln_cv_id")
				.append(" left join m_savings_account sa on sa.client_id = c.id")

		;

		return accountsSummary.toString();
	}

	@Override
	public GuarantorAccountSummaryData mapRow(final ResultSet rs,
			@SuppressWarnings("unused") final int rowNum)
			throws SQLException {

		final Long id = JdbcSupport.getLong(rs, "id");
		final String accountNo = rs.getString("accountNo");
		final String externalId = rs.getString("externalId");
		final Long productId = JdbcSupport.getLong(rs, "productId");
		final String loanProductName = rs.getString("productName");
		final String shortLoanProductName = rs
				.getString("shortProductName");
		final Integer loanStatusId = JdbcSupport.getInteger(rs, "statusId");
		final LoanStatusEnumData loanStatus = LoanEnumerations
				.status(loanStatusId);
		final Integer loanTypeId = JdbcSupport.getInteger(rs, "loanType");
		final EnumOptionData loanType = AccountEnumerations
				.loanType(loanTypeId);
		final Integer loanCycle = JdbcSupport.getInteger(rs, "loanCycle");

		final BigDecimal originalLoan = JdbcSupport
				.getBigDecimalDefaultToNullIfZero(rs, "originalLoan");
		final BigDecimal loanBalance = JdbcSupport
				.getBigDecimalDefaultToNullIfZero(rs, "loanBalance");
		final BigDecimal amountPaid = JdbcSupport
				.getBigDecimalDefaultToNullIfZero(rs, "amountPaid");
		final BigDecimal onHoldAmount = JdbcSupport
				.getBigDecimalDefaultToNullIfZero(rs,
						"on_hold_funds_derived");

		final LocalDate overdueSinceDate = JdbcSupport.getLocalDate(rs,
				"overdueSinceDate");
		Boolean inArrears = true;
		if (overdueSinceDate == null) {
			inArrears = false;
		}

		final Boolean isActive = rs.getBoolean("isActive");

		final String relationship = rs.getString("relationship");
		return new GuarantorAccountSummaryData(id, accountNo, externalId,
				productId, loanProductName, shortLoanProductName,
				loanStatus, loanType, loanCycle, inArrears, originalLoan,
				loanBalance, amountPaid, isActive, relationship,
				onHoldAmount);
	}
	
    }

}