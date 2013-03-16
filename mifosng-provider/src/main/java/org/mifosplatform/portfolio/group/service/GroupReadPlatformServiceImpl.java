/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.MoneyData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.exception.StaffNotFoundException;
import org.mifosplatform.portfolio.client.data.ClientLookup;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.client.service.LoanStatusMapper;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryData;
import org.mifosplatform.portfolio.group.data.GroupData;
import org.mifosplatform.portfolio.group.data.GroupLevelData;
import org.mifosplatform.portfolio.group.data.GroupLookup;
import org.mifosplatform.portfolio.group.data.GroupSummary;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.GroupLevelNotFoundException;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.useradministration.domain.AppUser;
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
    private final GroupRepository groupRepository;

    @Autowired
    public GroupReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final ClientReadPlatformService clientReadPlatformService, final OfficeReadPlatformService officeReadPlatformService,
            final GroupRepository groupRepository) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientReadPlatformService = clientReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.groupRepository = groupRepository;
    }

    @Override
    public Collection<GroupData> retrieveAllGroups(final String extraCriteria) {

        this.context.authenticatedUser();
        
        AppUser currentUser = context.authenticatedUser();
        String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = hierarchy + "%";

        final GroupDataMapper rm = new GroupDataMapper();

        String sql = "select " + rm.groupSchema() + " where o.hierarchy like ? and g.is_deleted=0 ";
        
        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " and (" + extraCriteria + ")";
        }

        sql += "order by g.hierarchy";
        
        return this.jdbcTemplate.query(sql, rm, new Object[] {hierarchySearchString});
    }

    @Override
    public GroupData retrieveGroup(final Long groupId) {

        try {
            this.context.authenticatedUser();

            final GroupDataMapper rm = new GroupDataMapper();

            final String sql = "select " + rm.groupSchema() + " where g.id = ? and g.is_deleted=0";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { groupId });
        } catch (final EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
    }

    @Override
    public Collection<GroupLookup> retrieveAllGroupsbyOfficeIdAndLevelId(final Long officeId, final Long levelId) {

        this.context.authenticatedUser();
        final GroupLookupMapper rm = new GroupLookupMapper();
        final String sql = "select " + rm.groupLookupSchema() + " from m_group g where g.office_id = ? and level_id = ? and g.is_deleted=0";
        return this.jdbcTemplate.query(sql, rm, new Object[] { officeId, levelId });
    }

    private GroupLookup retrieveGroupbyId(final Long parentGroupId) {
        try {
            this.context.authenticatedUser();
            final GroupLookupMapper rm = new GroupLookupMapper();
            final String sql = "select " + rm.groupLookupSchema() + " from m_group g where g.id = ? and g.is_deleted=0";
            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { parentGroupId });

        } catch (final EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(parentGroupId);
        }
    }

    @Override
    public Collection<GroupLookup> retrieveChildGroupsbyGroupId(final Long groupId) {

        this.context.authenticatedUser();
        final GroupLookupMapper rm = new GroupLookupMapper();
        final String sql = "select " + rm.groupLookupSchema()
                + " from m_group pg join m_group g where pg.id = g.parent_id and pg.id = ? and g.is_deleted=0";

        return this.jdbcTemplate.query(sql, rm, new Object[] { groupId });

    }

    @Override
    public GroupLevelData retrieveGroupLevelDetails(final Long levelId) {

        try {
            this.context.authenticatedUser();
            final GroupLevelDataMapper rm = new GroupLevelDataMapper();
            final String sql = "select " + rm.groupLevelSchema() + " where gl.id = ? ";
            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { levelId });

        } catch (final EmptyResultDataAccessException e) {
            throw new GroupLevelNotFoundException(levelId);
        }
    }

    @Override
    public GroupData retrieveNewGroupDetails(final Long officeId, final Long levelId) {

        final GroupLevelData groupLevelData = retrieveGroupLevelDetails(levelId);

        if (groupLevelData == null) { throw new GroupLevelNotFoundException(levelId); }

        List<ClientLookup> allowedClients = null;

        if (groupLevelData.isCanHaveClients()) {
            allowedClients = new ArrayList<ClientLookup>(
                    this.clientReadPlatformService.retrieveAllIndividualClientsForLookupByOfficeId(officeId));
        }

        final List<OfficeData> allowedOffices = new ArrayList<OfficeData>(this.officeReadPlatformService.retrieveAllOfficesForDropdown());

        List<GroupLookup> allowedParentGroups = null;

        if (groupLevelData.getParentLevelId() != null) {
            allowedParentGroups = new ArrayList<GroupLookup>(retrieveAllGroupsbyOfficeIdAndLevelId(officeId,
                    groupLevelData.getParentLevelId()));
        }

        final List<StaffData> allowedStaffs = new ArrayList<StaffData>(retrieveStaffsbyOfficeId(officeId));

        return new GroupData(officeId, allowedClients, allowedOffices, allowedParentGroups, groupLevelData, allowedStaffs);
    }

    @Override
    public GroupData retrieveNewChildGroupDetails(Long officeId, final Long levelId, final Long parentGroupId) {

        final GroupLevelData groupLevelData = retrieveGroupLevelDetails(levelId);
        if (groupLevelData == null) { throw new GroupLevelNotFoundException(levelId); }

        final Group group = this.groupRepository.findOne(parentGroupId);
        if (group == null || group.isDeleted()) { throw new GroupNotFoundException(parentGroupId); }

        if (officeId == null) {
            officeId = group.getOfficeId();
        }

        List<ClientLookup> allowedClients = null;

        if (groupLevelData.isCanHaveClients()) {
            allowedClients = new ArrayList<ClientLookup>(
                    this.clientReadPlatformService.retrieveAllIndividualClientsForLookupByOfficeId(officeId));
        }

        final List<OfficeData> allowedOffices = new ArrayList<OfficeData>();
        allowedOffices.add(this.officeReadPlatformService.retrieveOffice(officeId));

        final List<GroupLookup> allowedParentGroups = new ArrayList<GroupLookup>();
        allowedParentGroups.add(retrieveGroupbyId(parentGroupId));

        final List<StaffData> allowedStaffs = new ArrayList<StaffData>();
        allowedStaffs.addAll(retrieveStaffsbyOfficeId(officeId));

        return new GroupData(officeId, allowedClients, allowedOffices, allowedParentGroups, groupLevelData, allowedStaffs);
    }

    @Override
    public GroupData retrieveGroupDetails(final Long groupId, final boolean template) {

        GroupData group = retrieveGroup(groupId);
        final Collection<ClientLookup> clientMembers = retrieveClientMembers(groupId);
        Collection<ClientLookup> availableClients = null;
        Collection<OfficeData> allowedOffices = null;
        Collection<GroupLookup> allowedParentGroups = null;
        final GroupLevelData groupLevelData = retrieveGroupLevelDetails(getLevelIdByGroupId(groupId));
        ;
        Collection<StaffData> allowedStaffs = null;
        final Collection<GroupLookup> childGroups = retrieveChildGroupsbyGroupId(groupId);

        final Long totalActiveClients = retrieveTotalClients(group.getHierarchy());
        final Long totalChildGroups = retrieveTotalNoOfChildGroups(groupId);
        final Collection<MoneyData> totalLoanPortfolio = retrieveGroupLoanPortfolio(group.getHierarchy());

        final GroupSummary groupSummaryData = new GroupSummary(totalActiveClients, totalChildGroups, totalLoanPortfolio, null);

        group = new GroupData(group, clientMembers, availableClients, allowedOffices, allowedParentGroups, groupLevelData, allowedStaffs,
                childGroups, groupSummaryData);

        if (template) {

            if (groupLevelData.isCanHaveClients()) {
                availableClients = this.clientReadPlatformService.retrieveAllIndividualClientsForLookupByOfficeId(group.getOfficeId());
                availableClients.removeAll(group.clientMembers());
            }
            allowedOffices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
            if (groupLevelData.getParentLevelId() != null) {
                allowedParentGroups = retrieveAllGroupsbyOfficeIdAndLevelId(group.getOfficeId(), groupLevelData.getParentLevelId());
            }

            allowedStaffs = retrieveStaffsbyOfficeId(group.getOfficeId());

            group = new GroupData(group, group.clientMembers(), availableClients, allowedOffices, allowedParentGroups, groupLevelData,
                    allowedStaffs, childGroups, groupSummaryData);
        }

        return group;
    }

    @Override
    public Collection<StaffData> retrieveStaffsbyOfficeId(final Long officeId) {

        this.context.authenticatedUser();
        final StaffDataMapper rm = new StaffDataMapper();
        final String sql = "select " + rm.staffDataSchema() + " where o.id = ?";
        return this.jdbcTemplate.query(sql, rm, new Object[] { officeId });

    }

    @Override
    public StaffData retrieveStaffsbyId(final Long staffId) {

        try {
            this.context.authenticatedUser();
            final StaffDataMapper rm = new StaffDataMapper();
            final String sql = "select " + rm.staffDataSchema() + " where s.id = ?";
            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { staffId });

        } catch (final EmptyResultDataAccessException e) {
            throw new StaffNotFoundException(staffId);
        }
    }

    @Override
    public Long getLevelIdByGroupId(final Long groupId){

        this.context.authenticatedUser();
        final String sqlTotalClients = "SELECT g.level_Id as levelId FROM m_group g WHERE g.id = ?";
        return this.jdbcTemplate.queryForLong(sqlTotalClients, new Object[] { groupId });
        
    }
    
    @Override
    public Long retrieveTotalClients(final String hierarchy) {

        this.context.authenticatedUser();

        final String groupHierarchy = hierarchy + "%";
        final String sqlTotalClients = "SELECT count(gc.client_id) FROM m_group_client gc JOIN m_group g "
                + "ON gc.group_id = g.id WHERE g.hierarchy LIKE ? ";
        return this.jdbcTemplate.queryForLong(sqlTotalClients, new Object[] { groupHierarchy });

    }

    @Override
    public Long retrieveTotalNoOfChildGroups(final Long groupId) {

        this.context.authenticatedUser();
        final String sqlTotalClients = "SELECT count(gc.id) FROM m_group g JOIN m_group gc WHERE g.id = gc.parent_id and g.id = ?";
        return this.jdbcTemplate.queryForLong(sqlTotalClients, new Object[] { groupId });

    }

    @Override
    public Collection<MoneyData> retrieveGroupLoanPortfolio(final String hierarchy) {

        this.context.authenticatedUser();

        final MoneyDataMapper rm = new MoneyDataMapper();

        final String groupHierarchy = hierarchy + "%";
        final String sql = "select " + rm.moneyDataSchema() + " FROM m_group_client gc JOIN m_group g ON gc.group_id = g.id "
                + "JOIN m_loan l ON l.client_id = gc.client_id LEFT JOIN m_organisation_currency oc ON oc.code = l.currency_code "
                + " WHERE g.hierarchy LIKE ? GROUP BY l.currency_code ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { groupHierarchy });

    }

    private static final class MoneyDataMapper implements RowMapper<MoneyData> {

        public String moneyDataSchema() {
            return "l.currency_code as currencyCode, oc.decimal_places as decimalPlaces, sum(l.principal_amount) as amount";
        }

        @Override
        public MoneyData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String currencyCode = rs.getString("currencyCode");
            final Integer decimalPlaces = rs.getInt("decimalPlaces");
            final BigDecimal amount = rs.getBigDecimal("amount");

            return new MoneyData(currencyCode, amount, decimalPlaces);
        }

    }

    private static final class GroupDataMapper implements RowMapper<GroupData> {

        public String groupSchema() {
            return "g.office_id as officeId, g.parent_id as parentId , o.name as officeName,"
                    + " g.id as id, g.external_id as externalId, g.name as name , s.display_name as staffName , pg.name as"
                    + " parentName , g.staff_id as staffId , g.hierarchy as hierarchy from m_group g join m_office o on o.id = g.office_id left join"
                    + " m_staff s on s.id = g.staff_id left join m_group pg on pg.id = g.parent_id";
        }

        @Override
        public GroupData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String externalId = rs.getString("externalId");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final Long parentId = JdbcSupport.getLong(rs, "parentId");
            final String parentName = rs.getString("parentName");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");
            final String hierarchy = rs.getString("hierarchy");

            return new GroupData(id, officeId, officeName, name, externalId, parentId, parentName, staffId, staffName,
                    hierarchy);
        }

    }

    private static final class GroupLookupMapper implements RowMapper<GroupLookup> {

        public String groupLookupSchema() {
            return "g.id as id, g.name as name ";
        }

        @Override
        public GroupLookup mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");

            return new GroupLookup(id, name);
        }

    }

    private static final class GroupLevelDataMapper implements RowMapper<GroupLevelData> {

        public String groupLevelSchema() {
            return "gl.id as id, gl.level_name as levelName , gl.parent_id as parentLevelId , pgl.level_name as parentName , "
                    + "cgl.id as childLevelId,cgl.level_name as childLevelName,gl.super_parent as superParent ,"
                    + " gl.recursable as recursable , gl.can_have_clients as canHaveClients from m_group_level gl "
                    + " left join m_group_level pgl on pgl.id = gl.parent_id left join m_group_level cgl on gl.id = cgl.parent_id";
        }

        @Override
        public GroupLevelData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long levelId = rs.getLong("id");
            final String levelName = rs.getString("levelName");
            final Long parentLevelId = rs.getLong("parentLevelId");
            final String parentLevelName = rs.getString("parentName");
            final Long childLevelId = rs.getLong("childLevelId");
            final String childLevelName = rs.getString("childLevelName");
            final boolean superParent = rs.getBoolean("superParent");
            final boolean recursable = rs.getBoolean("recursable");
            final boolean canHaveClients = rs.getBoolean("canHaveClients");

            return new GroupLevelData(levelId, levelName, parentLevelId, parentLevelName, childLevelId, childLevelName, superParent,
                    recursable, canHaveClients);
        }

    }

    private static final class StaffDataMapper implements RowMapper<StaffData> {

        public String staffDataSchema() {
            return "s.id as id , s.is_loan_officer as isLoanOfficer , s.office_id as officeId , s.firstname as firstname, "
                    + "s.lastname as lastname , s.display_name as displayName , o.name as officeName from m_staff s join m_office o on s.office_id =o.id ";
        }

        @Override
        public StaffData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final boolean isLoanOfficer = rs.getBoolean("isLoanOfficer");

            return StaffData.instance(id, firstname, lastname, displayName, officeId, officeName, isLoanOfficer);
        }

    }

    @Override
    public Collection<ClientLookup> retrieveClientMembers(final Long groupId) {

        this.context.authenticatedUser();

        final ClientMemberSummaryDataMapper rm = new ClientMemberSummaryDataMapper();

        final String sql = "select " + rm.clientMemberSummarySchema() + " where cm.is_deleted = 0 and pgc.group_id = ?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { groupId });
    }

    private static final class ClientMemberSummaryDataMapper implements RowMapper<ClientLookup> {

        public String clientMemberSummarySchema() {
            return "cm.id, cm.display_name as displayName from m_client cm INNER JOIN m_group_client pgc ON pgc.client_id = cm.id";
        }

        @Override
        public ClientLookup mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String displayName = rs.getString("displayName");

            return ClientLookup.create(id, displayName);
        }

    }

    @Override
    public GroupAccountSummaryCollectionData retrieveGroupAccountDetails(final Long groupId) {
        try {
            this.context.authenticatedUser();

            // Check if group exists
            retrieveGroup(groupId);

            final List<GroupAccountSummaryData> pendingApprovalLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> awaitingDisbursalLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> openLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> closedLoans = new ArrayList<GroupAccountSummaryData>();

            final GroupLoanAccountSummaryDataMapper rm = new GroupLoanAccountSummaryDataMapper();

            String sql = "select " + rm.loanAccountSummarySchema() + " where l.group_id = ? and l.client_id is null";

            List<GroupAccountSummaryData> results = this.jdbcTemplate.query(sql, rm, new Object[] { groupId });
            if (results != null) {
                for (final GroupAccountSummaryData row : results) {

                    final LoanStatusMapper statusMapper = new LoanStatusMapper(row.getAccountStatusId());

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

            final List<GroupAccountSummaryData> pendingApprovalIndividualLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> awaitingDisbursalIndividualLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> openIndividualLoans = new ArrayList<GroupAccountSummaryData>();
            final List<GroupAccountSummaryData> closedIndividualLoans = new ArrayList<GroupAccountSummaryData>();

            sql = "select " + rm.loanAccountSummarySchema() + " where l.group_id = ? and l.client_id is not null";

            results = this.jdbcTemplate.query(sql, rm, new Object[] { groupId });
            if (results != null) {
                for (final GroupAccountSummaryData row : results) {

                    final LoanStatusMapper statusMapper = new LoanStatusMapper(row.getAccountStatusId());

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
        } catch (final EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
    }

    @Override
    public Collection<GroupAccountSummaryData> retrieveGroupLoanAccountsByLoanOfficerId(final Long groupId, final Long loanOfficerId) {

        this.context.authenticatedUser();

        // Check if group exists
        retrieveGroup(groupId);

        final GroupLoanAccountSummaryDataMapper rm = new GroupLoanAccountSummaryDataMapper();

        final String sql = "select " + rm.loanAccountSummarySchema()
                + " where l.group_id = ? and l.client_id is null and l.loan_officer_id = ?";

        final List<GroupAccountSummaryData> loanAccounts = this.jdbcTemplate.query(sql, rm, new Object[] { groupId, loanOfficerId });

        return loanAccounts;
    }

    private static final class GroupLoanAccountSummaryDataMapper implements RowMapper<GroupAccountSummaryData> {

        public String loanAccountSummarySchema() {

            final StringBuilder accountsSummary = new StringBuilder("l.id as id, l.external_id as externalId,");
            accountsSummary.append("l.product_id as productId, lp.name as productName,").append("l.loan_status_id as statusId ")
                    .append("from m_loan l ").append("LEFT JOIN m_product_loan AS lp ON lp.id = l.product_id ");

            return accountsSummary.toString();
        }

        @Override
        public GroupAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String loanProductName = rs.getString("productName");
            final Integer loanStatusId = JdbcSupport.getInteger(rs, "statusId");

            return new GroupAccountSummaryData(id, externalId, productId, loanProductName, loanStatusId);
        }
    }

}
