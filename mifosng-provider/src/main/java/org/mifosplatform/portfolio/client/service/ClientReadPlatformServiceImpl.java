package org.mifosplatform.portfolio.client.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientAccountSummaryCollectionData;
import org.mifosplatform.portfolio.client.data.ClientAccountSummaryData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.data.ClientIdentifierData;
import org.mifosplatform.portfolio.client.data.ClientLookup;
import org.mifosplatform.portfolio.client.data.NoteData;
import org.mifosplatform.portfolio.client.domain.NoteEnumerations;
import org.mifosplatform.portfolio.client.exception.ClientIdentifierNotFoundException;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.exception.NoteNotFoundException;
import org.mifosplatform.useradministration.data.AppUserData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.service.AppUserReadPlatformService;
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
    private final AppUserReadPlatformService appUserReadPlatformService;

    @Autowired
    public ClientReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final OfficeReadPlatformService officeReadPlatformService, final AppUserReadPlatformService appUserReadPlatformService) {
        this.context = context;
        this.officeReadPlatformService = officeReadPlatformService;
        this.appUserReadPlatformService = appUserReadPlatformService;
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

        sql += " order by c.lastname ASC, c.firstname ASC";

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

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { hierarchySearchString });
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @Override
    public Collection<ClientLookup> retrieveAllIndividualClientsForLookup(String extraCriteria) {

        this.context.authenticatedUser();

        ClientLookupMapper rm = new ClientLookupMapper();

        String sql = "select " + rm.clientLookupSchema();

        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " and (" + extraCriteria + ")";
        }

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public Collection<ClientLookup> retrieveAllIndividualClientsForLookupByOfficeId(Long officeId) {
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
            return "c.office_id as officeId, o.name as officeName, c.id as id, c.firstname as firstname, c.lastname as lastname, c.display_name as displayName, "
                    + "c.external_id as externalId, c.joined_date as joinedDate, c.image_key as imagekey from m_client c join m_office o on o.id = c.office_id "
                    + " where o.hierarchy like ? and c.is_deleted=0 ";
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final Long id = JdbcSupport.getLong(rs, "id");
            String firstname = rs.getString("firstname");
            if (StringUtils.isBlank(firstname)) {
                firstname = "";
            }
            final String lastname = rs.getString("lastname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final LocalDate joinedDate = JdbcSupport.getLocalDate(rs, "joinedDate");
            final String imageKey = rs.getString("imageKey");
            final String officeName = rs.getString("officeName");

            return new ClientData(officeId, officeName, id, firstname, lastname, displayName, externalId, joinedDate, imageKey, null, null,
                    null);
        }

    }

    private static final class ClientLookupMapper implements RowMapper<ClientLookup> {

        public String clientLookupSchema() {
            return "c.id as id, c.firstname as firstname, c.lastname as lastname, " + "c.office_id as officeId, o.name as officeName "
                    + "from m_client c join m_office o on o.id = c.office_id where c.is_deleted=0 ";
        }

        @Override
        public ClientLookup mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String firstname = rs.getString("firstname");
            String lastname = rs.getString("lastname");
            if (StringUtils.isBlank(firstname)) {
                firstname = "";
            }

            Long officeId = rs.getLong("officeId");
            String officeName = rs.getString("officeName");

            return new ClientLookup(id, firstname, lastname, officeId, officeName);
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

            ClientDespoitAccountSummaryDataMapper depositAccountMapper = new ClientDespoitAccountSummaryDataMapper();

            String depositAccountsSql = "select " + depositAccountMapper.schema() + " where da.client_id = ? and da.is_deleted=0";
            List<ClientAccountSummaryData> depositAccountResults = this.jdbcTemplate.query(depositAccountsSql, depositAccountMapper,
                    new Object[] { clientId });
            if (depositAccountResults != null) {
                for (ClientAccountSummaryData row : depositAccountResults) {

                    if (row.accountStatusId() == 100) {
                        pendingApprovalDepositAccounts.add(row);
                    } else if (row.accountStatusId() == 300) {
                        approvedDepositAccounts.add(row);
                    } else if (row.accountStatusId() == 400) {
                        withdrawnByClientDespositAccounts.add(row);
                    } else if (row.accountStatusId() == 500) {
                        rejectedDepositAccounts.add(row);
                    } else if (row.accountStatusId() == 600) {
                        closedDepositAccounts.add(row);
                    } else if (row.accountStatusId() == 700) {
                        maturedDepositAccounts.add(row);
                    } else if (row.accountStatusId() == 800) {
                        preclosedDepositAccounts.add(row);
                    }
                }
            }
            
            List<ClientAccountSummaryData> pendingApprovalSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> approvedSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> withdrawnByClientSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> rejectedSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            List<ClientAccountSummaryData> closedSavingAccounts = new ArrayList<ClientAccountSummaryData>();
            
            ClientSavingAccountSummaryDataMapper clientSavingAccountSummaryDataMapper = new ClientSavingAccountSummaryDataMapper();
            String savingAccountsSql = "select " + clientSavingAccountSummaryDataMapper.schema() + " where sa.client_id=? and sa.is_deleted=0";
            List<ClientAccountSummaryData> savingAccountsResults = this.jdbcTemplate.query(savingAccountsSql, clientSavingAccountSummaryDataMapper, new Object[] {clientId});
            
            if (savingAccountsResults != null) {
				for (ClientAccountSummaryData account : savingAccountsResults){
					if (account.accountStatusId() == 100)
						pendingApprovalSavingAccounts.add(account);
					else if (account.accountStatusId() ==300)
						approvedSavingAccounts.add(account);
					else if (account.accountStatusId() == 400)
						withdrawnByClientSavingAccounts.add(account);
					else if (account.accountStatusId() == 500)
						rejectedSavingAccounts.add(account);
					else if (account.accountStatusId() == 600)
						closedSavingAccounts.add(account);
				}
			}

            return new ClientAccountSummaryCollectionData(pendingApprovalLoans, awaitingDisbursalLoans, openLoans, closedLoans,
                    pendingApprovalDepositAccounts, approvedDepositAccounts, withdrawnByClientDespositAccounts, rejectedDepositAccounts,
                    closedDepositAccounts, preclosedDepositAccounts, maturedDepositAccounts,
                    pendingApprovalSavingAccounts,approvedSavingAccounts,withdrawnByClientSavingAccounts,rejectedSavingAccounts,closedSavingAccounts);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @Override
    public Collection<ClientAccountSummaryData> retrieveClientLoanAccountsByLoanOfficerId(Long clientId, Long loanOfficerId) {

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

            StringBuilder accountsSummary = new StringBuilder("l.id as id, l.external_id as externalId,");
            accountsSummary.append("l.product_id as productId, lp.name as productName,").append("l.loan_status_id as statusId ")
                    .append("from m_loan l ").append("LEFT JOIN m_product_loan AS lp ON lp.id = l.product_id ");

            return accountsSummary.toString();
        }

        @Override
        public ClientAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = JdbcSupport.getLong(rs, "id");
            String externalId = rs.getString("externalId");
            Long productId = JdbcSupport.getLong(rs, "productId");
            String loanProductName = rs.getString("productName");
            Integer loanStatusId = JdbcSupport.getInteger(rs, "statusId");

            return new ClientAccountSummaryData(id, externalId, productId, loanProductName, loanStatusId);
        }
    }

    private static final class ClientDespoitAccountSummaryDataMapper implements RowMapper<ClientAccountSummaryData> {

        public String schema() {

            StringBuilder accountsSummary = new StringBuilder("da.id as id, da.external_id as externalId,");
            accountsSummary.append("da.product_id as productId, dp.name as productName,").append("da.status_enum as statusId ")
                    .append("from m_deposit_account da ").append("LEFT JOIN m_product_deposit AS dp ON dp.id = da.product_id ");

            return accountsSummary.toString();
        }

        @Override
        public ClientAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = JdbcSupport.getLong(rs, "id");
            String externalId = rs.getString("externalId");
            Long productId = JdbcSupport.getLong(rs, "productId");
            String productName = rs.getString("productName");
            Integer accountStatusId = JdbcSupport.getInteger(rs, "statusId");

            return new ClientAccountSummaryData(id, externalId, productId, productName, accountStatusId);
        }
    }

    @Override
    public NoteData retrieveClientNote(Long clientId, Long noteId) {

        try {
            context.authenticatedUser();

            // Check if client exists
            retrieveIndividualClient(clientId);

            // FIXME - use join on sql query to fetch user information for note
            // rather than fetching users?
            Collection<AppUserData> allUsers = this.appUserReadPlatformService.retrieveAllUsers();

            NoteMapper noteMapper = new NoteMapper(allUsers);

            String sql = "select " + noteMapper.schema() + " where n.client_id = ? and n.id = ?";

            return this.jdbcTemplate.queryForObject(sql, noteMapper, new Object[] { clientId, noteId });
        } catch (EmptyResultDataAccessException e) {
            throw new NoteNotFoundException(clientId);
        }
    }

    @Override
    public Collection<NoteData> retrieveAllClientNotes(Long clientId) {

        context.authenticatedUser();

        // Check if client exists
        retrieveIndividualClient(clientId);

        // FIXME - use join on sql query to fetch user information for note
        // rather than fetching users?
        Collection<AppUserData> allUsers = this.appUserReadPlatformService.retrieveAllUsers();

        NoteMapper noteMapper = new NoteMapper(allUsers);

        String sql = "select " + noteMapper.schema() + " where n.client_id = ? order by n.created_date DESC";

        return this.jdbcTemplate.query(sql, noteMapper, new Object[] { clientId });
    }

    private static final class NoteMapper implements RowMapper<NoteData> {

        private final Collection<AppUserData> allUsers;

        public NoteMapper(Collection<AppUserData> allUsers) {
            this.allUsers = allUsers;
        }

        public String schema() {
            return "n.id as id, n.client_id as clientId, n.loan_id as loanId, n.loan_transaction_id as transactionId, n.note_type_enum as noteTypeEnum, n.note as note, "
                    + "n.created_date as createdDate, n.createdby_id as createdById, n.lastmodified_date as lastModifiedDate, n.lastmodifiedby_id as lastModifiedById"
                    + " from m_note n";
        }

        @Override
        public NoteData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = JdbcSupport.getLong(rs, "id");
            Long clientId = JdbcSupport.getLong(rs, "clientId");
            Long loanId = JdbcSupport.getLong(rs, "loanId");
            Long transactionId = JdbcSupport.getLong(rs, "transactionId");
            Integer noteTypeId = JdbcSupport.getInteger(rs, "noteTypeEnum");
            EnumOptionData noteType = NoteEnumerations.noteType(noteTypeId);
            String note = rs.getString("note");

            DateTime createdDate = JdbcSupport.getDateTime(rs, "createdDate");
            Long createdById = JdbcSupport.getLong(rs, "createdById");
            String createdByUsername = findUserById(createdById, allUsers);

            DateTime lastModifiedDate = JdbcSupport.getDateTime(rs, "lastModifiedDate");
            Long lastModifiedById = JdbcSupport.getLong(rs, "lastModifiedById");
            String updatedByUsername = findUserById(createdById, allUsers);

            return new NoteData(id, clientId, loanId, transactionId, noteType, note, createdDate, createdById, createdByUsername,
                    lastModifiedDate, lastModifiedById, updatedByUsername);
        }

        private String findUserById(final Long createdById, final Collection<AppUserData> allUsers) {
            String username = "";
            for (AppUserData appUserData : allUsers) {
                if (appUserData.hasIdentifyOf(createdById)) {
                    username = appUserData.username();
                    break;
                }
            }
            return username;
        }
    }

    @Override
    public Collection<ClientIdentifierData> retrieveClientIdentifiers(final Long clientId) {

        AppUser currentUser = context.authenticatedUser();
        String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = hierarchy + "%";

        ClientIdentityMapper rm = new ClientIdentityMapper();

        String sql = "select " + rm.schema();

        sql += " order by ci.id";

        return this.jdbcTemplate.query(sql, rm, new Object[] { clientId, hierarchySearchString });
    }

    @Override
    public ClientIdentifierData retrieveClientIdentifier(final Long clientId, final Long clientIdentifierId) {
        try {
            AppUser currentUser = context.authenticatedUser();
            String hierarchy = currentUser.getOffice().getHierarchy();
            String hierarchySearchString = hierarchy + "%";

            ClientIdentityMapper rm = new ClientIdentityMapper();

            String sql = "select " + rm.schema();

            sql += " and ci.id = ?";

            final ClientIdentifierData clientIdentifierData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { clientId,
                    hierarchySearchString, clientIdentifierId });

            return clientIdentifierData;
        } catch (EmptyResultDataAccessException e) {
            throw new ClientIdentifierNotFoundException(clientIdentifierId);
        }

    }

    private static final class ClientIdentityMapper implements RowMapper<ClientIdentifierData> {

        public ClientIdentityMapper() {}

        public String schema() {
            return "ci.id as id, ci.client_id as clientId, ci.document_type_id as documentTypeId, ci.document_key as documentKey,"
                    + " ci.description as description, cv.code_value as documentType "
                    + " from m_client_identifier ci, m_client c, m_office o, m_code_value cv"
                    + " where ci.client_id=c.id and c.office_id=o.id" + " and ci.document_type_id=cv.id"
                    + " and ci.client_id = ? and o.hierarchy like ? ";
        }

        @Override
        public ClientIdentifierData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long documentTypeId = JdbcSupport.getLong(rs, "documentTypeId");
            final String documentKey = rs.getString("documentKey");
            final String description = rs.getString("description");
            final String documentTypeName = rs.getString("documentType");

            return ClientIdentifierData.singleItem(id, clientId, documentTypeId, documentKey, description, documentTypeName);
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
            return "c.id as id, c.firstname as firstname, c.lastname as lastname, " + "c.office_id as officeId, o.name as officeName "
                    + "from m_client c, m_office o, m_client_identifier ci " + "where o.id = c.office_id and c.id=ci.client_id "
                    + "and ci.document_type_id= ? and ci.document_key like ?";
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            String firstname = rs.getString("firstname");
            if (StringUtils.isBlank(firstname)) {
                firstname = "";
            }
            final String lastname = rs.getString("lastname");

            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            return ClientData.clientIdentifier(id, firstname, lastname, officeId, officeName);
        }
    }
    
    
     private static final class ClientSavingAccountSummaryDataMapper implements RowMapper<ClientAccountSummaryData> {

        public String schema() {

            StringBuilder accountsSummary = new StringBuilder("sa.id as id, sa.external_id as externalId,");
            accountsSummary.append("sa.product_id as productId, sp.name as productName,").append("sa.status_enum as statusId ")
                    .append("from m_saving_account sa ").append("LEFT JOIN m_product_savings AS sp ON sp.id = sa.product_id ");

            return accountsSummary.toString();
        }

        @Override
        public ClientAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = JdbcSupport.getLong(rs, "id");
            String externalId = rs.getString("externalId");
            Long productId = JdbcSupport.getLong(rs, "productId");
            String productName = rs.getString("productName");
            Integer accountStatusId = JdbcSupport.getInteger(rs, "statusId");

            return new ClientAccountSummaryData(id, externalId, productId, productName, accountStatusId);
        }
    }
    
}