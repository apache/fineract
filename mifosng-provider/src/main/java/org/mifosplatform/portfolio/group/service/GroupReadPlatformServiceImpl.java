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
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.MoneyData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.exception.StaffNotFoundException;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.client.service.LoanStatusMapper;
import org.mifosplatform.portfolio.group.data.CenterData;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.data.GroupLevelData;
import org.mifosplatform.portfolio.group.data.GroupTypes;
import org.mifosplatform.portfolio.group.exception.GroupLevelNotFoundException;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class GroupReadPlatformServiceImpl implements GroupReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final AllGroupTypesDataMapper allGroupTypesDataMapper = new AllGroupTypesDataMapper();
    private final CenterReadPlatformService centerReadPlatformService;

    @Autowired
    public GroupReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final CenterReadPlatformService centerReadPlatformService, final ClientReadPlatformService clientReadPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final StaffReadPlatformService staffReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.centerReadPlatformService = centerReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
    }

    @Override
    public GroupGeneralData retrieveTemplate(final Long officeId, final boolean isCenterGroup) {

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);

        Collection<CenterData> centerOptions = null;
        if (isCenterGroup) {
            centerOptions = this.centerReadPlatformService.retrieveAllForDropdown(defaultOfficeId);
        }

        final Collection<OfficeData> officeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
        Collection<StaffData> staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(defaultOfficeId);
        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }

        Collection<ClientData> clientOptions = this.clientReadPlatformService
                .retrieveAllIndividualClientsForLookupByOfficeId(defaultOfficeId);
        if (CollectionUtils.isEmpty(clientOptions)) {
            clientOptions = null;
        }

        final Long centerId = null;
        final String centerName = null;
        final Long staffId = null;
        final String staffName = null;

        return GroupGeneralData.template(defaultOfficeId, centerId, centerName, staffId, staffName, centerOptions, officeOptions,
                staffOptions, clientOptions);
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public Collection<GroupGeneralData> retrieveAll(final SearchParameters searchCriteria) {

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        String sql = "select " + this.allGroupTypesDataMapper.schema() + " where o.hierarchy like ?";

        final String extraCriteria = getGroupExtraCriteria(searchCriteria);
        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " and (" + extraCriteria + ")";
        }

        sql += " order by g.hierarchy";

        return this.jdbcTemplate.query(sql, this.allGroupTypesDataMapper, new Object[] { hierarchySearchString });
    }

    // 'g.' preffix because of ERROR 1052 (23000): Column 'column_name' in where
    // clause is ambiguous
    // caused by the same name of columns in m_office and m_group tables
    private String getGroupExtraCriteria(final SearchParameters searchCriteria) {

        String extraCriteria = " and g.level_Id = " + GroupTypes.GROUP.getId();

        String sqlSearch = searchCriteria.getSqlSearch();
        if (sqlSearch != null) {
            sqlSearch = sqlSearch.replaceAll(" name ", " g.name ");
            sqlSearch = sqlSearch.replaceAll("name ", "g.name ");
            extraCriteria = " and (" + sqlSearch + ")";
        }

        final Long officeId = searchCriteria.getOfficeId();
        if (officeId != null) {
            extraCriteria += " and g.office_id = " + officeId;
        }

        final String externalId = searchCriteria.getExternalId();
        if (externalId != null) {
            extraCriteria += " and g.external_id = " + ApiParameterHelper.sqlEncodeString(externalId);
        }

        final String name = searchCriteria.getName();
        if (name != null) {
            extraCriteria += " and g.name like " + ApiParameterHelper.sqlEncodeString("%" + name + "%");
        }

        final String hierarchy = searchCriteria.getHierarchy();
        if (hierarchy != null) {
            extraCriteria += " and o.hierarchy like " + ApiParameterHelper.sqlEncodeString(hierarchy + "%");
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }

        return extraCriteria;
    }

    @Override
    public GroupGeneralData retrieveOne(final Long groupId) {

        try {
            final AppUser currentUser = context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            String sql = "select " + this.allGroupTypesDataMapper.schema() + " where g.id = ? and o.hierarchy like ?";
            return this.jdbcTemplate.queryForObject(sql, this.allGroupTypesDataMapper, new Object[] { groupId, hierarchySearchString });
        } catch (EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
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
    public Long getLevelIdByGroupId(final Long groupId) {

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

    // GroupTopOfHierarchyData
    private static final class AllGroupTypesDataMapper implements RowMapper<GroupGeneralData> {

        private final String schemaSql;

        public AllGroupTypesDataMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("g.id as id, g.external_id as externalId, g.name as name, ");
            sqlBuilder.append("g.office_id as officeId, o.name as officeName, ");
            sqlBuilder.append("g.parent_id as parentId, pg.name as parentName, ");
            sqlBuilder.append("g.staff_id as staffId, s.display_name as staffName, ");
            sqlBuilder.append("g.hierarchy as hierarchy ");
            sqlBuilder.append("from m_group g ");
            sqlBuilder.append("join m_office o on o.id = g.office_id ");
            sqlBuilder.append("left join m_staff s on s.id = g.staff_id ");
            sqlBuilder.append("left join m_group pg on pg.id = g.parent_id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public GroupGeneralData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String externalId = rs.getString("externalId");
            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");
            final Long parentId = JdbcSupport.getLong(rs, "parentId");
            final String parentName = rs.getString("parentName");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");
            final String hierarchy = rs.getString("hierarchy");

            return GroupGeneralData.instance(id, name, externalId, officeId, officeName, parentId, parentName, staffId, staffName,
                    hierarchy);
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
    public GroupAccountSummaryCollectionData retrieveGroupAccountDetails(final Long groupId) {
        try {
            this.context.authenticatedUser();

            // Check if group exists
            // retrieveGroup(groupId);

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
        // retrieveGroup(groupId);

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