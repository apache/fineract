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
import org.mifosplatform.organisation.office.data.OfficeLookup;
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
import org.mifosplatform.portfolio.group.data.GroupLookupData;
import org.mifosplatform.portfolio.group.data.GroupSummaryData;
import org.mifosplatform.portfolio.group.exception.GroupLevelNotFoundException;
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

        GroupDataMapper rm = new GroupDataMapper();

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

            GroupDataMapper rm = new GroupDataMapper();

            String sql = "select " + rm.groupSchema() + " where g.id = ? and g.is_deleted=0";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { groupId });
        } catch (EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
    }

    @Override
    public Collection<GroupLookupData> retrieveAllGroupsbyOfficeIdAndLevelId(final Long officeId, final Long levelId) {

        try {
            this.context.authenticatedUser();

            GroupLookupMapper rm = new GroupLookupMapper();

            String sql = "select " + rm.groupLookupSchema() + " from m_group g where g.office_id = ? and level_id = ? and g.is_deleted=0";

            return this.jdbcTemplate.query(sql, rm, new Object[] { officeId, levelId });

        } catch (EmptyResultDataAccessException e) {

            // TODO need throw proper exception
            throw new GroupNotFoundException(officeId);
        }
    }

    @Override
    public Collection<GroupLookupData> retrieveChildGroupsbyGroupId(final Long groupId) {

        try {
            this.context.authenticatedUser();

            GroupLookupMapper rm = new GroupLookupMapper();

            String sql = "select " + rm.groupLookupSchema()
                    + " from m_group pg join m_group g where pg.id = g.parent_id and pg.id = ? and g.is_deleted=0";

            return this.jdbcTemplate.query(sql, rm, new Object[] { groupId });

        } catch (EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
    }

    @Override
    public GroupLevelData retrieveGroupLevelDetails(final Long levelId) {

        try {
            this.context.authenticatedUser();

            GroupLevelDataMapper rm = new GroupLevelDataMapper();

            String sql = "select " + rm.groupLevelSchema() + " where gl.id = ? ";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { levelId });

        } catch (EmptyResultDataAccessException e) {
            throw new GroupLevelNotFoundException(levelId);
        }
    }

    @Override
    public GroupData retrieveNewGroupDetails(final Long officeId, final Long levelId) {

        GroupLevelData groupLevelData = this.retrieveGroupLevelDetails(levelId);

        if (groupLevelData == null) { throw new GroupLevelNotFoundException(levelId); }

        List<ClientLookup> allowedClients = null;

        if (groupLevelData.isCanHaveClients()) {
            allowedClients = new ArrayList<ClientLookup>(
                    this.clientReadPlatformService.retrieveAllIndividualClientsForLookupByOfficeId(officeId));
        }

        List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>(officeReadPlatformService.retrieveAllOfficesForLookup());

        List<GroupLookupData> allowedParentGroups = null;

        if (groupLevelData.getParentLevelId() != null) {
            allowedParentGroups = new ArrayList<GroupLookupData>(this.retrieveAllGroupsbyOfficeIdAndLevelId(officeId,
                    groupLevelData.getParentLevelId()));
        }

        List<StaffData> allowedStaffs = new ArrayList<StaffData>(this.retrieveStaffsbyOfficeId(officeId));

        return new GroupData(officeId, allowedClients, allowedOffices, allowedParentGroups, groupLevelData, allowedStaffs);
    }

    @Override
    public GroupData retrieveGroupDetails(final Long groupId, final boolean template) {

        GroupData group = this.retrieveGroup(groupId);
        final Collection<ClientLookup> clientMembers = this.retrieveClientMembers(groupId);
        Collection<ClientLookup> availableClients = null;
        Collection<OfficeLookup> allowedOffices = null;
        Collection<GroupLookupData> allowedParentGroups = null;
        GroupLevelData groupLevelData = this.retrieveGroupLevelDetails(group.getGroupLevel());
        ;
        Collection<StaffData> allowedStaffs = null;
        Collection<GroupLookupData> childGroups = this.retrieveChildGroupsbyGroupId(groupId);

        final Long totalActiveClients = this.retrieveTotalClients(groupId);
        final Long totalChildGroups = this.retrieveTotalNoOfChildGroups(groupId);
        Collection<MoneyData> totalLoanPortfolio = this.retrieveGroupLoanPortfolio(groupId);

        GroupSummaryData groupSummaryData = new GroupSummaryData(totalActiveClients, totalChildGroups, totalLoanPortfolio, null);

        group = new GroupData(group, clientMembers, availableClients, allowedOffices, allowedParentGroups, groupLevelData, allowedStaffs,
                childGroups, groupSummaryData);

        if (template) {

            if (groupLevelData.isCanHaveClients()) {
                availableClients = this.clientReadPlatformService.retrieveAllIndividualClientsForLookupByOfficeId(group.getOfficeId());
                availableClients.removeAll(group.clientMembers());
            }
            allowedOffices = officeReadPlatformService.retrieveAllOfficesForLookup();
            if (groupLevelData.getParentLevelId() != null) {
                allowedParentGroups = this.retrieveAllGroupsbyOfficeIdAndLevelId(group.getOfficeId(), groupLevelData.getParentLevelId());
            }

            allowedStaffs = this.retrieveStaffsbyOfficeId(group.getOfficeId());

            group = new GroupData(group, group.clientMembers(), availableClients, allowedOffices, allowedParentGroups, groupLevelData,
                    allowedStaffs, childGroups, groupSummaryData);
        }

        return group;
    }

    @Override
    public Collection<StaffData> retrieveStaffsbyOfficeId(final Long officeId) {

        try {
            this.context.authenticatedUser();

            StaffDataMapper rm = new StaffDataMapper();

            String sql = "select " + rm.staffDataSchema() + " where o.id = ?";

            return this.jdbcTemplate.query(sql, rm, new Object[] { officeId });

        } catch (EmptyResultDataAccessException e) {
            // TODO throw Staff not found in given office
            throw new StaffNotFoundException(officeId);
        }
    }

    @Override
    public Long retrieveTotalClients(final Long groupId) {

        this.context.authenticatedUser();

        String groupHierarchy = "." +  groupId + "%";
        String sqlTotalClients = "SELECT count(gc.client_id) FROM m_group_client gc JOIN m_group g "
                + "ON gc.group_id = g.id WHERE g.hierarchy LIKE ? ";
        return this.jdbcTemplate.queryForLong(sqlTotalClients, new Object[] { groupHierarchy });

    }

    @Override
    public Long retrieveTotalNoOfChildGroups(final Long groupId) {

        this.context.authenticatedUser();
        String sqlTotalClients = "SELECT count(gc.id) FROM m_group g JOIN m_group gc WHERE g.id = gc.parent_id and g.id = ?";
        return this.jdbcTemplate.queryForLong(sqlTotalClients, new Object[] { groupId });

    }

    @Override
    public Collection<MoneyData> retrieveGroupLoanPortfolio(final Long groupId) {

        this.context.authenticatedUser();

        MoneyDataMapper rm = new MoneyDataMapper();

        String groupHierarchy = "." +  groupId + "%";
        String sql = "select " + rm.moneyDataSchema() + " FROM m_group_client gc JOIN m_group g ON gc.group_id = g.id "
                + "JOIN m_loan l ON l.client_id = gc.client_id LEFT JOIN m_organisation_currency oc ON oc.code = l.currency_code "
                + " WHERE g.hierarchy LIKE ? GROUP BY l.currency_code ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { groupHierarchy });

    }

    private static final class MoneyDataMapper implements RowMapper<MoneyData> {

        public String moneyDataSchema() {
            return "l.currency_code as currencyCode, oc.decimal_places as decimalPlaces, sum(l.principal_amount) as amount";
        }

        @Override
        public MoneyData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            String currencyCode = rs.getString("currencyCode");
            Integer decimalPlaces = rs.getInt("decimalPlaces");
            BigDecimal amount = rs.getBigDecimal("amount");

            return new MoneyData(currencyCode, amount, decimalPlaces);
        }

    }

    private static final class GroupDataMapper implements RowMapper<GroupData> {

        public String groupSchema() {
            return "g.office_id as officeId, g.level_id as groupLevel , g.parent_id as parentId , o.name as officeName,"
                    + " g.id as id, g.external_id as externalId, g.name as name , s.display_name as staffName , pg.name as"
                    + " parentName , g.staff_id as staffId from m_group g join m_office o on o.id = g.office_id left join "
                    + "m_staff s on s.id = g.staff_id left join m_group pg on pg.id = g.parent_id";
        }

        @Override
        public GroupData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String name = rs.getString("name");
            String externalId = rs.getString("externalId");
            Long officeId = rs.getLong("officeId");
            String officeName = rs.getString("officeName");
            Long groupLevel = rs.getLong("groupLevel");
            Long parentId = rs.getLong("parentId");
            String parentName = rs.getString("parentName");
            Long staffId = rs.getLong("staffId");
            String staffName = rs.getString("staffName");

            return new GroupData(id, officeId, officeName, name, externalId, groupLevel, parentId, parentName, staffId, staffName);
        }

    }

    private static final class GroupLookupMapper implements RowMapper<GroupLookupData> {

        public String groupLookupSchema() {
            return "g.id as id, g.name as name ";
        }

        @Override
        public GroupLookupData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String name = rs.getString("name");

            return new GroupLookupData(id, name);
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
        public GroupLevelData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            Long levelId = rs.getLong("id");
            String levelName = rs.getString("levelName");
            Long parentLevelId = rs.getLong("parentLevelId");
            String parentLevelName = rs.getString("parentName");
            Long childLevelId = rs.getLong("childLevelId");
            String childLevelName = rs.getString("childLevelName");
            boolean superParent = rs.getBoolean("superParent");
            boolean recursable = rs.getBoolean("recursable");
            boolean canHaveClients = rs.getBoolean("canHaveClients");

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
        public StaffData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String firstname = rs.getString("firstname");
            String lastname = rs.getString("lastname");
            String displayName = rs.getString("displayName");
            Long officeId = rs.getLong("officeId");
            String officeName = rs.getString("officeName");
            boolean isLoanOfficer = rs.getBoolean("isLoanOfficer");

            return StaffData.instance(id, firstname, lastname, displayName, officeId, officeName, isLoanOfficer);
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
