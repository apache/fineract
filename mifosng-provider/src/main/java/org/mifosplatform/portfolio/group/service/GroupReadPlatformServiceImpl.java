package org.mifosplatform.portfolio.group.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientLookup;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.client.service.LoanStatusMapper;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryData;
import org.mifosplatform.portfolio.group.data.GroupData;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class GroupReadPlatformServiceImpl implements GroupReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;

    @Autowired
    public GroupReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            ClientReadPlatformService clientReadPlatformService, final OfficeReadPlatformService officeReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientReadPlatformService = clientReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
    }

    @Override
    public Collection<GroupData> retrieveAllGroups(final String extraCriteria) {

        this.context.authenticatedUser();

        GroupMapper rm = new GroupMapper();

        String sql = "select " + rm.groupSchema() + " where g.is_deleted=0";

        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " and (" + extraCriteria + ")";
        }

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public GroupData retrieveGroup(final Long groupId) {

        try {
            this.context.authenticatedUser();

            GroupMapper rm = new GroupMapper();

            String sql = "select " + rm.groupSchema() + " where g.id = ? and g.is_deleted=0";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { groupId });
        } catch (EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
    }

    @Override
    public GroupData retrieveNewGroupDetails(final Long officeId) {

        List<ClientLookup> allowedClients = new ArrayList<ClientLookup>(
                this.clientReadPlatformService.retrieveAllIndividualClientsForLookupByOfficeId(officeId));

        List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>(officeReadPlatformService.retrieveAllOfficesForLookup());

        return new GroupData(officeId, allowedClients, allowedOffices);
    }

    private static final class GroupMapper implements RowMapper<GroupData> {

        public String groupSchema() {
            return "g.office_id as officeId, o.name as officeName, g.id as id, g.external_id as externalId, "
                    + "g.name as name from m_group g join m_office o on o.id = g.office_id";
        }

        @Override
        public GroupData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String name = rs.getString("name");
            String externalId = rs.getString("externalId");

            Long officeId = rs.getLong("officeId");
            String officeName = rs.getString("officeName");

            return new GroupData(id, officeId, officeName, name, externalId);
        }

    }

    @Override
    public Collection<ClientLookup> retrieveClientMembers(Long groupId) {

        this.context.authenticatedUser();

        ClientMemberSummaryDataMapper rm = new ClientMemberSummaryDataMapper();

        String sql = "select " + rm.clientMemberSummarySchema() + " where cm.is_deleted = 0 and pgc.group_id = ?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { groupId });
    }

    private static final class ClientMemberSummaryDataMapper implements RowMapper<ClientLookup> {

        public String clientMemberSummarySchema() {
            return "cm.id, cm.display_name as displayName from m_client cm INNER JOIN m_group_client pgc ON pgc.client_id = cm.id";
        }

        @Override
        public ClientLookup mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String displayName = rs.getString("displayName");

            return ClientLookup.create(id, displayName);
        }

    }

    @Override
    public GroupAccountSummaryCollectionData retrieveGroupAccountDetails(Long groupId) {
        try {
            this.context.authenticatedUser();

            // Check if group exists
            retrieveGroup(groupId);

            List<GroupAccountSummaryData> pendingApprovalLoans = new ArrayList<GroupAccountSummaryData>();
            List<GroupAccountSummaryData> awaitingDisbursalLoans = new ArrayList<GroupAccountSummaryData>();
            List<GroupAccountSummaryData> openLoans = new ArrayList<GroupAccountSummaryData>();
            List<GroupAccountSummaryData> closedLoans = new ArrayList<GroupAccountSummaryData>();

            GroupLoanAccountSummaryDataMapper rm = new GroupLoanAccountSummaryDataMapper();

            String sql = "select " + rm.loanAccountSummarySchema() + " where l.group_id = ? and l.client_id is null";

            List<GroupAccountSummaryData> results = this.jdbcTemplate.query(sql, rm, new Object[] { groupId });
            if (results != null) {
                for (GroupAccountSummaryData row : results) {

                    LoanStatusMapper statusMapper = new LoanStatusMapper(row.getAccountStatusId());

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

            List<GroupAccountSummaryData> pendingApprovalIndividualLoans = new ArrayList<GroupAccountSummaryData>();
            List<GroupAccountSummaryData> awaitingDisbursalIndividualLoans = new ArrayList<GroupAccountSummaryData>();
            List<GroupAccountSummaryData> openIndividualLoans = new ArrayList<GroupAccountSummaryData>();
            List<GroupAccountSummaryData> closedIndividualLoans = new ArrayList<GroupAccountSummaryData>();

            sql = "select " + rm.loanAccountSummarySchema() + " where l.group_id = ? and l.client_id is not null";

            results = this.jdbcTemplate.query(sql, rm, new Object[] { groupId });
            if (results != null) {
                for (GroupAccountSummaryData row : results) {

                    LoanStatusMapper statusMapper = new LoanStatusMapper(row.getAccountStatusId());

                    if (statusMapper.isOpen()) {
                        openIndividualLoans.add(row);
                    } else if (statusMapper.isAwaitingDisbursal()) {
                        awaitingDisbursalIndividualLoans.add(row);
                    } else if (statusMapper.isPendingApproval()) {
                        pendingApprovalIndividualLoans.add(row);
                    } else {
                        closedIndividualLoans.add(row);
                    }
                }
            }

            return new GroupAccountSummaryCollectionData(pendingApprovalLoans, awaitingDisbursalLoans, openLoans, closedLoans,
                    pendingApprovalIndividualLoans, awaitingDisbursalIndividualLoans, openIndividualLoans, closedIndividualLoans);
        } catch (EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
    }

    @Override
    public Collection<GroupAccountSummaryData> retrieveGroupLoanAccountsByLoanOfficerId(Long groupId, Long loanOfficerId) {

        this.context.authenticatedUser();

        // Check if group exists
        retrieveGroup(groupId);

        GroupLoanAccountSummaryDataMapper rm = new GroupLoanAccountSummaryDataMapper();

        String sql = "select " + rm.loanAccountSummarySchema() + " where l.group_id = ? and l.client_id is null and l.loan_officer_id = ?";

        List<GroupAccountSummaryData> loanAccounts = this.jdbcTemplate.query(sql, rm, new Object[] { groupId, loanOfficerId });

        return loanAccounts;
    }

    private static final class GroupLoanAccountSummaryDataMapper implements RowMapper<GroupAccountSummaryData> {

        public String loanAccountSummarySchema() {

            StringBuilder accountsSummary = new StringBuilder("l.id as id, l.external_id as externalId,");
            accountsSummary.append("l.product_id as productId, lp.name as productName,").append("l.loan_status_id as statusId ")
                    .append("from m_loan l ").append("LEFT JOIN m_product_loan AS lp ON lp.id = l.product_id ");

            return accountsSummary.toString();
        }

        @Override
        public GroupAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = JdbcSupport.getLong(rs, "id");
            String externalId = rs.getString("externalId");
            Long productId = JdbcSupport.getLong(rs, "productId");
            String loanProductName = rs.getString("productName");
            Integer loanStatusId = JdbcSupport.getInteger(rs, "statusId");

            return new GroupAccountSummaryData(id, externalId, productId, loanProductName, loanStatusId);
        }
    }
}
