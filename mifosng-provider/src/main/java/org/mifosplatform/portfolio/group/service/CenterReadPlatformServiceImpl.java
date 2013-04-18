/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.domain.ClientEnumerations;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.group.data.CenterData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.domain.GroupTypes;
import org.mifosplatform.portfolio.group.domain.GroupingTypeEnumerations;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class CenterReadPlatformServiceImpl implements CenterReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;

    // data mappers
    private final AllGroupTypesDataMapper allGroupTypesDataMapper = new AllGroupTypesDataMapper();

    // data mappers
    private final CenterDataMapper centerMapper = new CenterDataMapper();
    private final GroupDataMapper groupDataMapper = new GroupDataMapper();

    @Autowired
    public CenterReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final ClientReadPlatformService clientReadPlatformService, final OfficeReadPlatformService officeReadPlatformService,
            final StaffReadPlatformService staffReadPlatformService) {
        this.context = context;
        this.clientReadPlatformService = clientReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.officeReadPlatformService = officeReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
    }

    // 'g.' preffix because of ERROR 1052 (23000): Column 'column_name' in where
    // clause is ambiguous
    // caused by the same name of columns in m_office and m_group tables
    private String getCenterExtraCriteria(final SearchParameters searchCriteria) {

        String extraCriteria = " and g.level_id = " + GroupTypes.CENTER.getId() + " ";

        String sqlQueryCriteria = searchCriteria.getSqlSearch();
        if (StringUtils.isNotBlank(sqlQueryCriteria)) {
            sqlQueryCriteria = sqlQueryCriteria.replaceAll(" display_name ", " g.display_name ");
            sqlQueryCriteria = sqlQueryCriteria.replaceAll("display_name ", "g.display_name ");
            extraCriteria = " and (" + sqlQueryCriteria + ")";
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
            extraCriteria += " and g.display_name like " + ApiParameterHelper.sqlEncodeString(name + "%");
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

    private static final String sqlQuery = "g.id as id, g.external_id as externalId, g.display_name as name, "
            + "g.office_id as officeId, o.name as officeName, " //
            + "g.staff_id as staffId, s.display_name as staffName, " //
            + "g.status_enum as statusEnum, g.activation_date as activationDate, " //
            + "g.hierarchy as hierarchy " //
            + "from m_group g " //
            + "join m_office o on o.id = g.office_id " + "left join m_staff s on s.id = g.staff_id ";

    private static final class CenterDataMapper implements RowMapper<CenterData> {

        private final String schemaSql;

        public CenterDataMapper() {

            this.schemaSql = sqlQuery;
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public CenterData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = GroupingTypeEnumerations.status(statusEnum);
            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final String externalId = rs.getString("externalId");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");
            final String hierarchy = rs.getString("hierarchy");

            return CenterData.instance(id, name, externalId, status, activationDate, officeId, officeName, staffId, staffName, hierarchy);
        }
    }

    private static final class GroupDataMapper implements RowMapper<GroupGeneralData> {

        private final String schemaSql;

        public GroupDataMapper() {

            this.schemaSql = sqlQuery;
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public GroupGeneralData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String externalId = rs.getString("externalId");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);
            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");

            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");
            final String hierarchy = rs.getString("hierarchy");

            return GroupGeneralData.instance(id, name, externalId, status, activationDate, officeId, officeName, null, null, staffId,
                    staffName, hierarchy);
        }
    }

    @Override
    public Collection<CenterData> retrieveAll(final SearchParameters searchCriteria) {

        this.context.authenticatedUser();

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        String sql = "select " + centerMapper.schema() + " where o.hierarchy like ?";

        final String extraCriteria = getCenterExtraCriteria(searchCriteria);

        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " and (" + extraCriteria + ")";
        }

        sql += " order by g.hierarchy";

        return this.jdbcTemplate.query(sql, centerMapper, new Object[] { hierarchySearchString });
    }

    @Override
    public Collection<CenterData> retrieveAllForDropdown(final Long officeId) {

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + centerMapper.schema()
                + " where g.office_id = ? and g.parent_id is null and g.level_Id = ? and o.hierarchy like ? order by g.hierarchy";

        return this.jdbcTemplate.query(sql, centerMapper, new Object[] { officeId, GroupTypes.CENTER.getId(), hierarchySearchString });
    }

    @Override
    public CenterData retrieveTemplate(final Long officeId) {

        final Long officeIdDefaulted = defaultToUsersOfficeIfNull(officeId);

        final Collection<OfficeData> officeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        Collection<StaffData> staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(officeIdDefaulted);
        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }

        Collection<GroupGeneralData> groupMembersOptions = retrieveAllGroupsForCenterDropdown(officeIdDefaulted);
        if (CollectionUtils.isEmpty(groupMembersOptions)) {
            groupMembersOptions = null;
        }

        // final boolean clientPendingApprovalAllowed =
        // this.configurationDomainService.isClientPendingApprovalAllowedEnabled();

        return CenterData.template(officeIdDefaulted, new LocalDate(), officeOptions, staffOptions, groupMembersOptions);
    }

    private Collection<GroupGeneralData> retrieveAllGroupsForCenterDropdown(final Long officeId) {

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + allGroupTypesDataMapper.schema()
                + " where g.office_id = ? and g.parent_id is null and g.level_Id = ? and o.hierarchy like ? order by g.hierarchy";

        return this.jdbcTemplate.query(sql, allGroupTypesDataMapper, new Object[] { defaultOfficeId, GroupTypes.GROUP.getId(),
                hierarchySearchString });
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public CenterData retrieveOne(final Long centerId) {

        try {
            final AppUser currentUser = context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            String sql = "select " + this.centerMapper.schema() + " where g.id = ? and o.hierarchy like ?";
            return this.jdbcTemplate.queryForObject(sql, this.centerMapper, new Object[] { centerId, hierarchySearchString });

        } catch (EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(centerId);
        }
    }

    @Override
    public GroupGeneralData retrieveCenterGroupTemplate(final Long centerId) {

        final CenterData center = this.retrieveOne(centerId);

        final Long centerOfficeId = center.officeId();
        final OfficeData centerOffice = this.officeReadPlatformService.retrieveOffice(centerOfficeId);

        StaffData staff = null;
        final Long staffId = center.staffId();
        String staffName = null;
        if (staffId != null) {
            staff = this.staffReadPlatformService.retrieveStaff(staffId);
            staffName = staff.getDisplayName();
        }

        final Collection<CenterData> centerOptions = Arrays.asList(center);
        final Collection<OfficeData> officeOptions = Arrays.asList(centerOffice);

        Collection<StaffData> staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(centerOfficeId);
        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }

        Collection<ClientData> clientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(centerOfficeId);
        if (CollectionUtils.isEmpty(clientOptions)) {
            clientOptions = null;
        }

        return GroupGeneralData.template(centerOfficeId, center.getId(), center.getName(), staffId, staffName, centerOptions,
                officeOptions, staffOptions, clientOptions);
    }

    @Override
    public Collection<GroupGeneralData> retrieveAssociatedGroups(final Long centerId) {
        String sql = "select " + this.groupDataMapper.schema() + " where g.parent_id = ? ";
        return this.jdbcTemplate.query(sql, this.groupDataMapper, new Object[] { centerId });
    }
}