/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.data.PaginationParameters;
import org.mifosplatform.infrastructure.core.data.PaginationParametersDataValidator;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.core.service.SearchParameters;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.calendar.service.CalendarEnumerations;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.domain.ClientEnumerations;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.group.api.GroupingTypesApiConstants;
import org.mifosplatform.portfolio.group.data.CenterData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.data.GroupTimelineData;
import org.mifosplatform.portfolio.group.data.StaffCenterData;
import org.mifosplatform.portfolio.group.domain.GroupTypes;
import org.mifosplatform.portfolio.group.domain.GroupingTypeEnumerations;
import org.mifosplatform.portfolio.group.exception.CenterNotFoundException;
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
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    // data mappers
    private final AllGroupTypesDataMapper allGroupTypesDataMapper = new AllGroupTypesDataMapper();

    // data mappers
    private final CenterDataMapper centerMapper = new CenterDataMapper();
    private final GroupDataMapper groupDataMapper = new GroupDataMapper();

    private final PaginationHelper<CenterData> paginationHelper = new PaginationHelper<>();
    private final PaginationParametersDataValidator paginationParametersDataValidator;
    private final static Set<String> supportedOrderByValues = new HashSet<>(Arrays.asList("id", "name", "officeId", "officeName"));

    @Autowired
    public CenterReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final ClientReadPlatformService clientReadPlatformService, final OfficeReadPlatformService officeReadPlatformService,
            final StaffReadPlatformService staffReadPlatformService, final CodeValueReadPlatformService codeValueReadPlatformService,
            final PaginationParametersDataValidator paginationParametersDataValidator) {
        this.context = context;
        this.clientReadPlatformService = clientReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.officeReadPlatformService = officeReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.paginationParametersDataValidator = paginationParametersDataValidator;
    }

    // 'g.' preffix because of ERROR 1052 (23000): Column 'column_name' in where
    // clause is ambiguous
    // caused by the same name of columns in m_office and m_group tables
    private String getCenterExtraCriteria(final SearchParameters searchCriteria) {

        StringBuffer extraCriteria = new StringBuffer(200);
        extraCriteria.append(" and g.level_id = " + GroupTypes.CENTER.getId());

        String sqlQueryCriteria = searchCriteria.getSqlSearch();
        if (StringUtils.isNotBlank(sqlQueryCriteria)) {
            sqlQueryCriteria = sqlQueryCriteria.replaceAll(" display_name ", " g.display_name ");
            sqlQueryCriteria = sqlQueryCriteria.replaceAll("display_name ", "g.display_name ");
            extraCriteria.append(" and (").append(sqlQueryCriteria).append(") ");
        }

        final Long officeId = searchCriteria.getOfficeId();
        if (officeId != null) {
            extraCriteria.append(" and g.office_id = ").append(officeId);
        }

        final String externalId = searchCriteria.getExternalId();
        if (externalId != null) {
            extraCriteria.append(" and g.external_id = ").append(ApiParameterHelper.sqlEncodeString(externalId));
        }

        final String name = searchCriteria.getName();
        if (name != null) {
            extraCriteria.append(" and g.display_name like ").append(ApiParameterHelper.sqlEncodeString(name + "%"));
        }

        final String hierarchy = searchCriteria.getHierarchy();
        if (hierarchy != null) {
            extraCriteria.append(" and o.hierarchy like ").append(ApiParameterHelper.sqlEncodeString(hierarchy + "%"));
        }

        if (StringUtils.isNotBlank(extraCriteria.toString())) {
            extraCriteria.delete(0, 4);
        }

        final Long staffId = searchCriteria.getStaffId();
        if (staffId != null) {
            extraCriteria.append(" and g.staff_id = ").append(staffId);
        }

        return extraCriteria.toString();
    }

    private static final String sqlQuery = "g.id as id, g.external_id as externalId, g.display_name as name, "
            + "g.office_id as officeId, o.name as officeName, " //
            + "g.staff_id as staffId, s.display_name as staffName, " //
            + "g.status_enum as statusEnum, g.activation_date as activationDate, " //
            + "g.hierarchy as hierarchy, " //
            + "g.closedon_date as closedOnDate, " + "g.submittedon_date as submittedOnDate, " + "sbu.username as submittedByUsername, "
            + "sbu.firstname as submittedByFirstname, " + "sbu.lastname as submittedByLastname, " + "clu.username as closedByUsername, "
            + "clu.firstname as closedByFirstname, " + "clu.lastname as closedByLastname, " + "acu.username as activatedByUsername, "
            + "acu.firstname as activatedByFirstname, "
            + "acu.lastname as activatedByLastname "
            + "from m_group g " //
            + "join m_office o on o.id = g.office_id " + "left join m_staff s on s.id = g.staff_id "
            + "left join m_group pg on pg.id = g.parent_id " + "left join m_appuser sbu on sbu.id = g.submittedon_userid "
            + "left join m_appuser acu on acu.id = g.activatedon_userid " + "left join m_appuser clu on clu.id = g.closedon_userid ";

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

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");

            final GroupTimelineData timeline = new GroupTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname,
                    submittedByLastname, activationDate, activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate,
                    closedByUsername, closedByFirstname, closedByLastname);

            return CenterData.instance(id, name, externalId, status, activationDate, officeId, officeName, staffId, staffName, hierarchy,
                    timeline, null);
        }
    }

    private static final class CenterCalendarDataMapper implements RowMapper<CenterData> {

        private final String schemaSql;

        public CenterCalendarDataMapper() {

            schemaSql = " select g.id as id, g.display_name as name, g.office_id as officeId, g.staff_id as staffId, s.display_name as staffName, g.external_id as externalId, "
                    + " g.status_enum as statusEnum, g.activation_date as activationDate, g.hierarchy as hierarchy,  "
                    + " c.id as calendarId, ci.id as calendarInstanceId, ci.entity_id as entityId,  "
                    + " ci.entity_type_enum as entityTypeId, c.title as title,  c.description as description,  "
                    + " c.location as location, c.start_date as startDate, c.end_date as endDate, c.recurrence as recurrence  "
                    + " from m_calendar c join m_calendar_instance ci on ci.calendar_id=c.id and ci.entity_type_enum=4 join m_group g  "
                    + " on g.id = ci.entity_id join m_staff s on g.staff_id = s.id where g.office_id=? ";
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
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");
            final String hierarchy = rs.getString("hierarchy");

            final Long calendarId = rs.getLong("calendarId");
            final Long calendarInstanceId = rs.getLong("calendarInstanceId");
            final Long entityId = rs.getLong("entityId");
            final Integer entityTypeId = rs.getInt("entityTypeId");
            final EnumOptionData entityType = CalendarEnumerations.calendarEntityType(entityTypeId);
            final String title = rs.getString("title");
            final String description = rs.getString("description");
            final String location = rs.getString("location");
            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");
            final String recurrence = rs.getString("recurrence");

            CalendarData calendarData = CalendarData.instance(calendarId, calendarInstanceId, entityId, entityType, title, description,
                    location, startDate, endDate, null, null, false, recurrence, null, null, null, null, null, null, null, null, null,
                    null, null, null, null);
            return CenterData.instance(id, name, externalId, status, activationDate, officeId, null, staffId, staffName, hierarchy, null,
                    calendarData);
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

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");

            final GroupTimelineData timeline = new GroupTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname,
                    submittedByLastname, activationDate, activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate,
                    closedByUsername, closedByFirstname, closedByLastname);

            return GroupGeneralData.instance(id, name, externalId, status, activationDate, officeId, officeName, null, null, staffId,
                    staffName, hierarchy, timeline);
        }
    }

    @Override
    public Page<CenterData> retrievePagedAll(final SearchParameters searchParameters, final PaginationParameters parameters) {

        this.paginationParametersDataValidator.validateParameterValues(parameters, supportedOrderByValues, "audits");
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.centerMapper.schema());
        sqlBuilder.append(" where o.hierarchy like ?");

        final String extraCriteria = getCenterExtraCriteria(searchParameters);

        if (StringUtils.isNotBlank(extraCriteria)) {
            sqlBuilder.append(" and (").append(extraCriteria).append(")");
        }

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy()).append(' ').append(searchParameters.getSortOrder());
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                new Object[] { hierarchySearchString }, this.centerMapper);
    }

    @Override
    public Collection<CenterData> retrieveAll(SearchParameters searchParameters, PaginationParameters parameters) {

        this.paginationParametersDataValidator.validateParameterValues(parameters, supportedOrderByValues, "audits");
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(this.centerMapper.schema());
        sqlBuilder.append(" where o.hierarchy like ?");

        final String extraCriteria = getCenterExtraCriteria(searchParameters);

        if (StringUtils.isNotBlank(extraCriteria)) {
            sqlBuilder.append(" and (").append(extraCriteria).append(")");
        }

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy()).append(' ').append(searchParameters.getSortOrder());
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        return this.jdbcTemplate.query(sqlBuilder.toString(), this.centerMapper, new Object[] { hierarchySearchString });
    }

    @Override
    public Collection<CenterData> retrieveAllForDropdown(final Long officeId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + this.centerMapper.schema()
                + " where g.office_id = ? and g.parent_id is null and g.level_Id = ? and o.hierarchy like ? order by g.hierarchy";

        return this.jdbcTemplate.query(sql, this.centerMapper, new Object[] { officeId, GroupTypes.CENTER.getId(), hierarchySearchString });
    }

    @Override
    public CenterData retrieveTemplate(final Long officeId, final boolean staffInSelectedOfficeOnly) {

        final Long officeIdDefaulted = defaultToUsersOfficeIfNull(officeId);

        final Collection<OfficeData> officeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        final boolean loanOfficersOnly = false;
        Collection<StaffData> staffOptions = null;
        if (staffInSelectedOfficeOnly) {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(officeIdDefaulted);
        } else {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(officeIdDefaulted,
                    loanOfficersOnly);
        }

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

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + this.allGroupTypesDataMapper.schema()
                + " where g.office_id = ? and g.parent_id is null and g.level_Id = ? and o.hierarchy like ? order by g.hierarchy";

        return this.jdbcTemplate.query(sql, this.allGroupTypesDataMapper, new Object[] { defaultOfficeId, GroupTypes.GROUP.getId(),
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
            final AppUser currentUser = this.context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            final String sql = "select " + this.centerMapper.schema() + " where g.id = ? and o.hierarchy like ?";
            return this.jdbcTemplate.queryForObject(sql, this.centerMapper, new Object[] { centerId, hierarchySearchString });

        } catch (final EmptyResultDataAccessException e) {
            throw new CenterNotFoundException(centerId);
        }
    }

    @Override
    public GroupGeneralData retrieveCenterGroupTemplate(final Long centerId) {

        final CenterData center = retrieveOne(centerId);

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
                officeOptions, staffOptions, clientOptions, null);
    }

    @Override
    public Collection<GroupGeneralData> retrieveAssociatedGroups(final Long centerId) {
        final String sql = "select " + this.groupDataMapper.schema() + " where g.parent_id = ? ";
        return this.jdbcTemplate.query(sql, this.groupDataMapper, new Object[] { centerId });
    }

    @Override
    public CenterData retrieveCenterWithClosureReasons() {
        final List<CodeValueData> closureReasons = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(GroupingTypesApiConstants.CENTER_CLOSURE_REASON));
        return CenterData.withClosureReasons(closureReasons);
    }

    @Override
    public Collection<StaffCenterData> retriveAllCentersByMeetingDate(final Long officeId, final Date meetingDate, final Long staffId) {
        validateForGenerateCollectionSheet(staffId);
        final CenterCalendarDataMapper centerCalendarMapper = new CenterCalendarDataMapper();
        String sql = centerCalendarMapper.schema();
        Collection<CenterData> centerDataArray = null;

        if (staffId != null) {
            sql += " and g.staff_id=? ";
            centerDataArray = this.jdbcTemplate.query(sql, centerCalendarMapper, new Object[] { officeId, staffId });
        } else {
            centerDataArray = this.jdbcTemplate.query(sql, centerCalendarMapper, new Object[] { officeId });
        }

        Collection<StaffCenterData> staffCenterDataArray = new ArrayList<>();
        Boolean flag = false;
        for (CenterData centerData : centerDataArray) {
            if (centerData.getCollectionMeetingCalendar().isValidRecurringDate(new LocalDate(meetingDate))) {
                if (staffCenterDataArray.size() <= 0) {
                    Collection<CenterData> meetingFallCenter = new ArrayList<>();
                    meetingFallCenter.add(centerData);
                    staffCenterDataArray.add(StaffCenterData.instance(centerData.staffId(), centerData.getStaffName(), meetingFallCenter));
                } else {
                    for (StaffCenterData staffCenterData : staffCenterDataArray) {
                        flag = false;
                        if (staffCenterData.getStaffId().equals(centerData.staffId())) {
                            staffCenterData.getMeetingFallCenters().add(centerData);
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        Collection<CenterData> meetingFallCenter = new ArrayList<>();
                        meetingFallCenter.add(centerData);
                        staffCenterDataArray.add(StaffCenterData.instance(centerData.staffId(), centerData.getStaffName(),
                                meetingFallCenter));
                    }
                }

            }
        }
        return staffCenterDataArray;
    }

    public void validateForGenerateCollectionSheet(final Long staffId) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("productivecollectionsheet");
        baseDataValidator.reset().parameter("staffId").value(staffId).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }

    }
}