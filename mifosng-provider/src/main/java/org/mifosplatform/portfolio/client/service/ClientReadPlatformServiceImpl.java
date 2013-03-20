/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientAccountSummaryCollectionData;
import org.mifosplatform.portfolio.client.data.ClientAccountSummaryData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.data.ClientLookup;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.group.data.GroupLookup;
import org.mifosplatform.portfolio.loanaccount.data.LoanStatusEnumData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientReadPlatformServiceImpl implements ClientReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;

    @Autowired
    public ClientReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final OfficeReadPlatformService officeReadPlatformService) {
        this.context = context;
        this.officeReadPlatformService = officeReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<ClientData> retrieveAllIndividualClients(final String extraCriteria) {

        AppUser currentUser = context.authenticatedUser();
        String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = hierarchy + "%";

        ClientMapper rm = new ClientMapper();

        String sql = "select " + rm.clientSchema();

        if (StringUtils.isNotBlank(extraCriteria)) sql += " and (" + extraCriteria + ")";

        sql += " order by c.display_name ASC, c.account_no ASC";

        return this.jdbcTemplate.query(sql, rm, new Object[] { hierarchySearchString });
    }

    @Override
    public ClientData retrieveIndividualClient(final Long clientId) {

        try {

            AppUser currentUser = context.authenticatedUser();
            String hierarchy = currentUser.getOffice().getHierarchy();
            String hierarchySearchString = hierarchy + "%";

            ClientMapper rm = new ClientMapper();
            String sql = "select " + rm.clientSchema() + " and c.id = " + clientId;
            ClientData clientData =  this.jdbcTemplate.queryForObject(sql, rm, new Object[] { hierarchySearchString });

            ParentGroupsMapper cgrm = new ParentGroupsMapper();
            String cgSql = "select " + cgrm.parentGroupsSchema();
            Collection<GroupLookup> parentGroups = this.jdbcTemplate.query(cgSql, cgrm, new Object[] { clientId }); 
            
            return ClientData.setParentGroups(clientData , parentGroups);
            
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @Override
    public Collection<ClientLookup> retrieveAllIndividualClientsForLookup(final String extraCriteria) {

        this.context.authenticatedUser();

        ClientLookupMapper rm = new ClientLookupMapper();

        String sql = "select " + rm.clientLookupSchema();

        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " and (" + extraCriteria + ")";
        }

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public Collection<ClientLookup> retrieveAllIndividualClientsForLookupByOfficeId(final Long officeId) {
        this.context.authenticatedUser();

        ClientLookupMapper rm = new ClientLookupMapper();

        String sql = "select " + rm.clientLookupSchema() + " and c.office_id = " + officeId;

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public ClientData retrieveNewClientDetails() {

        final AppUser currentUser = context.authenticatedUser();

        final List<OfficeLookup> offices = new ArrayList<OfficeLookup>(officeReadPlatformService.retrieveAllOfficesForLookup());
        final Long officeId = currentUser.getOffice().getId();

        return ClientData.template(officeId, new LocalDate(), offices);
    }

    private static final class ClientMapper implements RowMapper<ClientData> {

        public String clientSchema() {
            return "c.account_no as accountNo, c.office_id as officeId, o.name as officeName, c.id as id, "
                    + "c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, "
                    + "c.fullname as fullname, c.display_name as displayName, "
                    + "c.external_id as externalId, c.joined_date as joinedDate, c.image_key as imagekey from m_client c join m_office o on o.id = c.office_id "
                    + " where o.hierarchy like ? and c.is_deleted=0 ";
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");
            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final Long id = JdbcSupport.getLong(rs, "id");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final LocalDate joinedDate = JdbcSupport.getLocalDate(rs, "joinedDate");
            final String imageKey = rs.getString("imageKey");
            final String officeName = rs.getString("officeName");

            return new ClientData(accountNo, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName, externalId,
                    joinedDate, imageKey, null, null, null ,null);
        }

    }

    private static final class ParentGroupsMapper implements RowMapper<GroupLookup> {

        public String parentGroupsSchema() {
            return "gp.id As groupId , gp.name As groupName from m_client cl JOIN m_group_client gc ON cl.id = gc.client_id "
                    + "JOIN m_group gp ON gp.id = gc.group_id WHERE cl.id  = ? AND gp.is_deleted = 0 ";
       }

        @Override
        public GroupLookup mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");

            return new GroupLookup(groupId, groupName);
        }

    }

    private static final class ClientLookupMapper implements RowMapper<ClientLookup> {

        public String clientLookupSchema() {
            return "c.id as id, c.display_name as displayName, " + "c.office_id as officeId, o.name as officeName "
                    + "from m_client c join m_office o on o.id = c.office_id where c.is_deleted=0 ";
        }

        @Override
        public ClientLookup mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String displayName = rs.getString("displayName");
            Long officeId = rs.getLong("officeId");
            String officeName = rs.getString("officeName");

            return ClientLookup.template(id, displayName, officeId, officeName);
        }
    }

    @Override
    public ClientAccountSummaryCollectionData retrieveClientAccountDetails(final Long clientId) {

        try {
            this.context.authenticatedUser();

            // Check if client exists
            retrieveIndividualClient(clientId);

            List<ClientAccountSummaryData> pendingApprovalLoans = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> awaitingDisbursalLoans = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> openLoans = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> closedLoans = new ArrayList<ClientAccountSummaryData>();

            ClientLoanAccountSummaryDataMapper rm = new ClientLoanAccountSummaryDataMapper();

            String sql = "select " + rm.loanAccountSummarySchema() + " where l.client_id = ?";

            List<ClientAccountSummaryData> results = this.jdbcTemplate.query(sql, rm, new Object[] { clientId });
            if (results != null) {
                for (ClientAccountSummaryData row : results) {

                    LoanStatusMapper statusMapper = new LoanStatusMapper(row.accountStatusId());

                    if (statusMapper.isOpen()) {
                        openLoans.add(row);
                    } else if (statusMapper.isAwaitingDisbursal()) {
                        awaitingDisbursalLoans.add(row);
                    } else if (statusMapper.isPendingApproval()) {
                        pendingApprovalLoans.add(row);
                    } else {
                        closedLoans.add(row);
                    }
                }
            }

            List<ClientAccountSummaryData> pendingApprovalDepositAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> approvedDepositAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> withdrawnByClientDespositAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> closedDepositAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> rejectedDepositAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> preclosedDepositAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> maturedDepositAccounts = new ArrayList<ClientAccountSummaryData>();

            // ClientDespoitAccountSummaryDataMapper depositAccountMapper = new
            // ClientDespoitAccountSummaryDataMapper();
            //
            // String depositAccountsSql = "select " +
            // depositAccountMapper.schema() +
            // " where da.client_id = ? and da.is_deleted=0";
            // List<ClientAccountSummaryData> depositAccountResults =
            // this.jdbcTemplate.query(depositAccountsSql, depositAccountMapper,
            // new Object[] { clientId });
            // if (depositAccountResults != null) {
            // for (ClientAccountSummaryData row : depositAccountResults) {
            //
            // if (row.accountStatusId() == 100) {
            // pendingApprovalDepositAccounts.add(row);
            // } else if (row.accountStatusId() == 300) {
            // approvedDepositAccounts.add(row);
            // } else if (row.accountStatusId() == 400) {
            // withdrawnByClientDespositAccounts.add(row);
            // } else if (row.accountStatusId() == 500) {
            // rejectedDepositAccounts.add(row);
            // } else if (row.accountStatusId() == 600) {
            // closedDepositAccounts.add(row);
            // } else if (row.accountStatusId() == 700) {
            // maturedDepositAccounts.add(row);
            // } else if (row.accountStatusId() == 800) {
            // preclosedDepositAccounts.add(row);
            // }
            // }
            // }

            List<ClientAccountSummaryData> pendingApprovalSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> approvedSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> withdrawnByClientSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> rejectedSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> closedSavingAccounts = new ArrayList<ClientAccountSummaryData>();

            // ClientSavingAccountSummaryDataMapper
            // clientSavingAccountSummaryDataMapper = new
            // ClientSavingAccountSummaryDataMapper();
            // String savingAccountsSql = "select " +
            // clientSavingAccountSummaryDataMapper.schema()
            // + " where sa.client_id=? and sa.is_deleted=0";
            // List<ClientAccountSummaryData> savingAccountsResults =
            // this.jdbcTemplate.query(savingAccountsSql,
            // clientSavingAccountSummaryDataMapper, new Object[] { clientId });
            //
            // if (savingAccountsResults != null) {
            // for (ClientAccountSummaryData account : savingAccountsResults) {
            // if (account.accountStatusId() == 100)
            // pendingApprovalSavingAccounts.add(account);
            // else if (account.accountStatusId() == 300)
            // approvedSavingAccounts.add(account);
            // else if (account.accountStatusId() == 400)
            // withdrawnByClientSavingAccounts.add(account);
            // else if (account.accountStatusId() == 500)
            // rejectedSavingAccounts.add(account);
            // else if (account.accountStatusId() == 600)
            // closedSavingAccounts.add(account);
            // }
            // }

            return new ClientAccountSummaryCollectionData(pendingApprovalLoans, awaitingDisbursalLoans, openLoans, closedLoans,
                    pendingApprovalDepositAccounts, approvedDepositAccounts, withdrawnByClientDespositAccounts, rejectedDepositAccounts,
                    closedDepositAccounts, preclosedDepositAccounts, maturedDepositAccounts, pendingApprovalSavingAccounts,
                    approvedSavingAccounts, withdrawnByClientSavingAccounts, rejectedSavingAccounts, closedSavingAccounts);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @Override
    public Collection<ClientAccountSummaryData> retrieveClientLoanAccountsByLoanOfficerId(final Long clientId, final Long loanOfficerId) {

        this.context.authenticatedUser();

        // Check if client exists
        retrieveIndividualClient(clientId);

        ClientLoanAccountSummaryDataMapper rm = new ClientLoanAccountSummaryDataMapper();

        String sql = "select " + rm.loanAccountSummarySchema() + " where l.client_id = ? and l.loan_officer_id = ?";

        List<ClientAccountSummaryData> loanAccounts = this.jdbcTemplate.query(sql, rm, new Object[] { clientId, loanOfficerId });

        return loanAccounts;
    }

    private static final class ClientLoanAccountSummaryDataMapper implements RowMapper<ClientAccountSummaryData> {

        public String loanAccountSummarySchema() {

            StringBuilder accountsSummary = new StringBuilder("l.id as id, l.account_no as accountNo, l.external_id as externalId,");
            accountsSummary.append("l.product_id as productId, lp.name as productName,").append("l.loan_status_id as statusId ")
                    .append("from m_loan l ").append("LEFT JOIN m_product_loan AS lp ON lp.id = l.product_id ");

            return accountsSummary.toString();
        }

        @Override
        public ClientAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String loanProductName = rs.getString("productName");
            final Integer loanStatusId = JdbcSupport.getInteger(rs, "statusId");
            final LoanStatusEnumData loanStatus = LoanEnumerations.status(loanStatusId);

            return new ClientAccountSummaryData(id, accountNo, externalId, productId, loanProductName, loanStatus);
        }
    }

    @Override
    public ClientData retrieveClientByIdentifier(final Long identifierTypeId, final String identifierKey) {
        try {
            final ClientIdentifierMapper mapper = new ClientIdentifierMapper();

            final String sql = "select " + mapper.clientLookupByIdentifierSchema();

            return jdbcTemplate.queryForObject(sql, mapper, new Object[] { identifierTypeId, identifierKey });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final class ClientIdentifierMapper implements RowMapper<ClientData> {

        public String clientLookupByIdentifierSchema() {
            return "c.id as id, c.account_no as accountNo, c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, "
                    + "c.fullname as fullname, c.display_name as displayName," + "c.office_id as officeId, o.name as officeName "
                    + " from m_client c, m_office o, m_client_identifier ci " + "where o.id = c.office_id and c.id=ci.client_id "
                    + "and ci.document_type_id= ? and ci.document_key like ?";
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");

            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            return ClientData.clientIdentifier(id, accountNo, firstname, middlename, lastname, fullname, displayName, officeId, officeName);
        }
    }
}