
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

package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.accountdetails.data.SavingsSummaryCustom;
import org.apache.fineract.portfolio.accountdetails.service.AccountEnumerations;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.savings.data.GSIMContainer;
import org.apache.fineract.portfolio.savings.data.GroupSavingsIndividualMonitoringAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountApplicationTimelineData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSubStatusEnumData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@RequiredArgsConstructor
public class GSIMReadPlatformServiceImpl implements GSIMReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ColumnValidator columnValidator;

    private static final class GSIMFieldsMapper implements RowMapper<GroupSavingsIndividualMonitoringAccountData> {

        public String schema() {
            return "gsim.id as gsimId,sv.group_id as groupId,sv.client_id as clientId,gsim.account_number as accountNumber, sv.id as childAccountId,sv.account_no as childAccountNumber,sv.account_balance_derived as childBalance,gsim.parent_deposit as parentBalance,gsim.child_accounts_count as childAccountsCount,"
                    + "gsim.savings_status_id as savingsStatus from gsim_accounts gsim,m_savings_account sv where gsim.id=sv.gsim_id";
        }

        @Override
        public GroupSavingsIndividualMonitoringAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {

            final BigDecimal gsimId = rs.getBigDecimal("gsimId");
            final BigDecimal groupId = rs.getBigDecimal("groupId");
            final BigDecimal clientId = rs.getBigDecimal("clientId");
            final String accountNumber = rs.getString("accountNumber");
            final BigDecimal childAccountId = rs.getBigDecimal("childAccountId");
            final String childAccountNumber = rs.getString("childAccountNumber");
            final Long childAccountsCount = rs.getLong("childAccountsCount");
            final BigDecimal parentBalance = rs.getBigDecimal("parentBalance");
            final BigDecimal childBalance = rs.getBigDecimal("childBalance");
            final String savingsStatus = SavingsAccountStatusType.fromInt((int) rs.getLong("savingsStatus")).toString();
            return GroupSavingsIndividualMonitoringAccountData.getInstance2(gsimId, groupId, clientId, accountNumber, childAccountId,
                    childAccountNumber, parentBalance, childBalance, childAccountsCount, savingsStatus);
        }
    }

    private static final class GSIMMapper implements RowMapper<GroupSavingsIndividualMonitoringAccountData> {

        public String schema() {
            return "gsim.id as gsimId,gsim.group_id as groupId,gsim.account_number as accountNumber,gsim.parent_deposit as parentDeposit,gsim.child_accounts_count as childAccountsCount,"
                    + "gsim.savings_status_id as savingsStatus from gsim_accounts gsim";
        }

        @Override
        public GroupSavingsIndividualMonitoringAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {

            final BigDecimal glimId = rs.getBigDecimal("gsimId");

            final BigDecimal groupId = rs.getBigDecimal("groupId");

            final String accountNumber = rs.getString("accountNumber");

            final BigDecimal parentDeposit = rs.getBigDecimal("parentDeposit");

            final String loanStatus = LoanStatus.fromInt((int) rs.getLong("savingsStatus")).toString();

            return GroupSavingsIndividualMonitoringAccountData.getInstance1(glimId, groupId, accountNumber, parentDeposit, loanStatus);

        }
    }

    @Override
    public Collection<GSIMContainer> findGSIMAccountContainerByGroupId(Long groupId) {
        this.context.authenticatedUser();
        Collection<GroupSavingsIndividualMonitoringAccountData> gsimInfo = findGSIMAccountsByGroupId(groupId + "");

        // List<LoanAccountSummaryData> glimAccounts =
        // retrieveLoanAccountDetails(loanWhereClauseForGroupAndLoanType, new
        // Object[] { groupId });
        String savingswhereClauseForGroup;

        List<GSIMContainer> gsimAccounts = new ArrayList<GSIMContainer>();
        List<SavingsSummaryCustom> childSavings;
        for (GroupSavingsIndividualMonitoringAccountData gsimAccount : gsimInfo) {
            BigDecimal gsimId = gsimAccount.getGsimId();
            savingswhereClauseForGroup = " where sa.group_id = ? and sa.gsim_id = ? order by sa.status_enum ASC, sa.account_no ASC";

            childSavings = retrieveAccountDetails(savingswhereClauseForGroup, new Object[] { groupId, gsimId });

            gsimAccounts.add(new GSIMContainer(gsimAccount.getGsimId(), gsimAccount.getGroupId(), gsimAccount.getAccountNumber(),
                    childSavings, gsimAccount.getParentDeposit(), gsimAccount.getSavingsStatus()));
        }

        return gsimAccounts;
    }

    @Override
    public Collection<GSIMContainer> findGsimAccountContainerbyGsimAccountNumber(String accountNumber) {
        this.context.authenticatedUser();
        Collection<GroupSavingsIndividualMonitoringAccountData> gsimInfo = findGsimAccountByParentAccountNumber(accountNumber);

        // List<LoanAccountSummaryData> glimAccounts =
        // retrieveLoanAccountDetails(loanWhereClauseForGroupAndLoanType, new
        // Object[] { groupId });
        final String savingswhereClauseForGroup = " where gsim.account_number = ? order by sa.status_enum ASC, sa.account_no ASC";

        List<GSIMContainer> gsimAccounts = new ArrayList<GSIMContainer>();
        for (GroupSavingsIndividualMonitoringAccountData gsimAccount : gsimInfo) {

            List<SavingsSummaryCustom> childSavings = retrieveAccountDetails(savingswhereClauseForGroup, new Object[] { accountNumber });

            gsimAccounts.add(new GSIMContainer(gsimAccount.getGsimId(), gsimAccount.getGroupId(), gsimAccount.getAccountNumber(),
                    childSavings, gsimAccount.getParentDeposit(), gsimAccount.getSavingsStatus()));
        }

        return gsimAccounts;
    }

    @Override
    public List<GSIMContainer> findGsimAccountContainerbyGsimAccountId(Long parentAccountId) {
        this.context.authenticatedUser();
        GroupSavingsIndividualMonitoringAccountData gsimAccount = findGSIMAccountByGSIMId(parentAccountId);

        // List<LoanAccountSummaryData> glimAccounts =
        // retrieveLoanAccountDetails(loanWhereClauseForGroupAndLoanType, new
        // Object[] { groupId });
        final String savingswhereClauseForGroup = " where sa.gsim_id = ? order by sa.status_enum ASC, sa.account_no ASC";

        List<SavingsSummaryCustom> childSavings = retrieveAccountDetails(savingswhereClauseForGroup, new Object[] { parentAccountId });

        List<GSIMContainer> parentGsim = new ArrayList<GSIMContainer>();

        parentGsim.add(new GSIMContainer(gsimAccount.getGsimId(), gsimAccount.getGroupId(), gsimAccount.getAccountNumber(), childSavings,
                gsimAccount.getParentDeposit(), gsimAccount.getSavingsStatus()));

        return parentGsim;

    }

    @Override
    public Collection<GroupSavingsIndividualMonitoringAccountData> findGSIMAccountsByGSIMId(final Long gsimId) {
        this.context.authenticatedUser();

        final GSIMFieldsMapper rm = new GSIMFieldsMapper();
        final String sql = "select " + rm.schema() + " and gsim.id=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { gsimId }); // NOSONAR
    }

    @Override
    public GroupSavingsIndividualMonitoringAccountData findGSIMAccountByGSIMId(final Long gsimId) {
        this.context.authenticatedUser();

        final GSIMMapper rm = new GSIMMapper();
        final String sql = "select " + rm.schema() + " where gsim.id=?";

        return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { gsimId }); // NOSONAR
    }

    @Override
    public Collection<GroupSavingsIndividualMonitoringAccountData> findGSIMAccountsByGroupId(String groupId) {
        this.context.authenticatedUser();

        final GSIMMapper rm = new GSIMMapper();
        final String sql = "select " + rm.schema() + " where gsim.group_id=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { Long.parseLong(groupId) }); // NOSONAR
    }

    @Override
    public Collection<GroupSavingsIndividualMonitoringAccountData> findGsimAccountByParentAccountNumber(String parentAccountIds) {
        this.context.authenticatedUser();

        final GSIMMapper rm = new GSIMMapper();
        final String sql = "select " + rm.schema() + " where gsim.account_number=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { parentAccountIds }); // NOSONAR
    }

    @Override
    public Collection<GroupSavingsIndividualMonitoringAccountData> findGsimAccountByGroupIdandAccountNo(String groupId, String accountNo) {
        this.context.authenticatedUser();

        GSIMMapper rm = new GSIMMapper();

        final String sql = "select " + rm.schema() + " where gsim.group_id=? and gsim.account_number=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { groupId, accountNo });// NOSONAR
    }

    private List<SavingsSummaryCustom> retrieveAccountDetails(final String savingswhereClause, final Object[] inputs) {
        final SavingsAccountSummaryDataMapper savingsAccountSummaryDataMapper = new SavingsAccountSummaryDataMapper();
        final String savingsSql = "select " + savingsAccountSummaryDataMapper.schema() + savingswhereClause;
        this.columnValidator.validateSqlInjection(savingsAccountSummaryDataMapper.schema(), savingswhereClause);
        return this.jdbcTemplate.query(savingsSql, savingsAccountSummaryDataMapper, inputs); // NOSONAR
    }

    private static final class SavingsAccountSummaryDataMapper implements RowMapper<SavingsSummaryCustom> {

        final String schemaSql;

        SavingsAccountSummaryDataMapper() {
            final StringBuilder accountsSummary = new StringBuilder();
            accountsSummary.append(
                    "sa.id as id, CONCAT('(',clnt.id,') ',clnt.display_name) as displayName,sa.account_no as accountNo, sa.external_id as externalId, sa.gsim_id as gsimId,gsim.account_number as parentAccountNo,sa.status_enum as statusEnum, ");
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
            accountsSummary.append("(select coalesce(max(sat.transaction_date),sa.activatedon_date) ");
            accountsSummary.append("from m_savings_account_transaction as sat ");
            accountsSummary.append("where sat.is_reversed = false ");
            accountsSummary.append("and sat.transaction_type_enum in (1,2) ");
            accountsSummary.append("and sat.savings_account_id = sa.id) as lastActiveTransactionDate, ");

            accountsSummary.append("sa.closedon_date as closedOnDate,");
            accountsSummary.append("cbu.username as closedByUsername,");
            accountsSummary.append("cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname,");

            accountsSummary.append(
                    "sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
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
            accountsSummary.append("left join gsim_accounts gsim on gsim.id=sa.gsim_id ");
            accountsSummary.append("left join m_client clnt on clnt.id=sa.client_id ");

            this.schemaSql = accountsSummary.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsSummaryCustom mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String displayName = rs.getString("displayName");
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
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                    currencyNameCode);

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
                    submittedByUsername, submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername, rejectedByFirstname,
                    rejectedByLastname, withdrawnOnDate, withdrawnByUsername, withdrawnByFirstname, withdrawnByLastname, approvedOnDate,
                    approvedByUsername, approvedByFirstname, approvedByLastname, activatedOnDate, activatedByUsername, activatedByFirstname,
                    activatedByLastname, closedOnDate, closedByUsername, closedByFirstname, closedByLastname);

            return new SavingsSummaryCustom(id, displayName, accountNo, externalId, productId, productName, shortProductName, status,
                    currency, accountBalance, accountTypeData, timeline, depositTypeData, subStatus, lastActiveTransactionDate);
        }
    }
}
