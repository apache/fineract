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
package org.apache.fineract.portfolio.group.service;

import java.math.BigDecimal;
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
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.data.PaginationParametersDataValidator;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.service.CalendarEnumerations;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.apache.fineract.portfolio.group.data.CenterData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.data.GroupTimelineData;
import org.apache.fineract.portfolio.group.data.StaffCenterData;
import org.apache.fineract.portfolio.group.domain.GroupTypes;
import org.apache.fineract.portfolio.group.domain.GroupingTypeEnumerations;
import org.apache.fineract.portfolio.group.exception.CenterNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
    private final ConfigurationDomainService configurationDomainService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

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
            final PaginationParametersDataValidator paginationParametersDataValidator, final ConfigurationDomainService configurationDomainService,
            final CalendarReadPlatformService calendarReadPlatformService) {
        this.context = context;
        this.clientReadPlatformService = clientReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.officeReadPlatformService = officeReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.paginationParametersDataValidator = paginationParametersDataValidator;
        this.configurationDomainService = configurationDomainService;
        this.calendarReadPlatformService = calendarReadPlatformService;
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

    private static final String sqlQuery = "g.id as id, g.account_no as accountNo, g.external_id as externalId, g.display_name as name, "
            + "g.office_id as officeId, o.name as officeName, " //
            + "g.staff_id as staffId, s.display_name as staffName, " //
            + "g.status_enum as statusEnum, g.activation_date as activationDate, " //
            + "g.hierarchy as hierarchy, " //
            + "g.level_id as groupLevel," //
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
            final String accountNo = rs.getString("accountNo");
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

            return CenterData.instance(id, accountNo, name, externalId, status, activationDate, officeId, officeName, staffId, staffName,
                    hierarchy, timeline, null, null, null, null, null);
        }
    }

    private static final class CenterCalendarDataMapper implements RowMapper<CenterData> {

        private final String schemaSql;

        public CenterCalendarDataMapper() {

            schemaSql = "select ce.id as id, g.account_no as accountNo,"
                    + "ce.display_name as name, g.office_id as officeId, g.staff_id as staffId, s.display_name as staffName,"
                    + " g.external_id as externalId,  g.status_enum as statusEnum, g.activation_date as activationDate,"
                    + " g.hierarchy as hierarchy,   c.id as calendarId, ci.id as calendarInstanceId, ci.entity_id as entityId,"
                    + " ci.entity_type_enum as entityTypeId, c.title as title,  c.description as description,"
                    + "c.location as location, c.start_date as startDate, c.end_date as endDate, c.recurrence as recurrence,c.meeting_time as meetingTime,"
                    + "sum(if(l.loan_status_id=300 and lrs.duedate = ?,"
                    + "(ifnull(lrs.principal_amount,0)) + (ifnull(lrs.interest_amount,0)),0)) as installmentDue,"
                    + "sum(if(l.loan_status_id=300 and lrs.duedate = ?,"
                    + "(ifnull(lrs.principal_completed_derived,0)) + (ifnull(lrs.interest_completed_derived,0)),0)) as totalCollected,"
                    + "sum(if(l.loan_status_id=300 and lrs.duedate <= ?, (ifnull(lrs.principal_amount,0)) + (ifnull(lrs.interest_amount,0)),0))"
                    + "- sum(if(l.loan_status_id=300 and lrs.duedate <= ?, (ifnull(lrs.principal_completed_derived,0)) + (ifnull(lrs.interest_completed_derived,0)),0)) as totaldue, "
                    + "sum(if(l.loan_status_id=300 and lrs.duedate < ?, (ifnull(lrs.principal_amount,0)) + (ifnull(lrs.interest_amount,0)),0))"
                    + "- sum(if(l.loan_status_id=300 and lrs.duedate < ?, (ifnull(lrs.principal_completed_derived,0)) + (ifnull(lrs.interest_completed_derived,0)),0)) as totaloverdue"
                    + " from m_calendar c join m_calendar_instance ci on ci.calendar_id=c.id and ci.entity_type_enum=4"
                    + " join m_group ce on ce.id = ci.entity_id" + " join m_group g   on g.parent_id = ce.id"
                    + " join m_group_client gc on gc.group_id=g.id" + " join m_client cl on cl.id=gc.client_id"
                    + " join m_loan l on l.client_id = cl.id"
                    + " join m_loan_repayment_schedule lrs on lrs.loan_id=l.id join m_staff s on g.staff_id = s.id"
                    + " where g.office_id=?";
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public CenterData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
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
            final LocalTime meetingTime = JdbcSupport.getLocalTime(rs,"meetingTime");
            final BigDecimal totalCollected = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalCollected");
            final BigDecimal totalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOverdue");
            final BigDecimal totaldue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totaldue");
            final BigDecimal installmentDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "installmentDue");
            Integer monthOnDay = CalendarUtils.getMonthOnDay(recurrence);

            CalendarData calendarData = CalendarData.instance(calendarId, calendarInstanceId, entityId, entityType, title, description,
                    location, startDate, endDate, null, null, false, recurrence, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, meetingTime, monthOnDay);

            return CenterData.instance(id, accountNo, name, externalId, status, activationDate, officeId, null, staffId, staffName,
                    hierarchy, null, calendarData, totalCollected, totalOverdue, totaldue, installmentDue);
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
            final String accountNo = rs.getString("accountNo");
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
            final String groupLevel = rs.getString("groupLevel");

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

            return GroupGeneralData.instance(id, accountNo, name, externalId, status, activationDate, officeId, officeName, null, null, staffId,
                    staffName, hierarchy, groupLevel, timeline);
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
        final Collection<GroupGeneralData> groupMembersOptions = null;
        final String accountNo = null;
        final BigDecimal totalCollected = null;
        final BigDecimal totalOverdue = null;
        final BigDecimal totaldue = null;
        final BigDecimal installmentDue = null;

        // final boolean clientPendingApprovalAllowed =
        // this.configurationDomainService.isClientPendingApprovalAllowedEnabled();

        return CenterData.template(officeIdDefaulted, accountNo, new LocalDate(), officeOptions, staffOptions, groupMembersOptions,
                totalCollected, totalOverdue, totaldue, installmentDue);
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

        return GroupGeneralData.template(centerOfficeId, center.getId(), center.getAccountNo(), center.getName(), staffId, staffName, centerOptions,
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
        LocalDate localDate = new LocalDate(meetingDate);
        final CenterCalendarDataMapper centerCalendarMapper = new CenterCalendarDataMapper();
        String passeddate = formatter.print(localDate);
        if (!isCentersHavingMeetingFrequencyBasedOnOfficeIdAndMeetingDate(officeId, passeddate, staffId)) { throw new GeneralPlatformDomainRuleException(
                "error.msg.collection.sheet.can.not.be.selected.meeting.date.not.attached.to.any.center",
                "Collection sheet can not be generated without meeting date " + passeddate + " attached to any center ", passeddate); }
        String sql = centerCalendarMapper.schema();
        Collection<CenterData> centerDataArray = null;
        if (staffId != null) {
            sql += " and g.staff_id=? ";
            sql += "and lrs.duedate<='" + passeddate + "' and l.loan_type_enum=3";
            sql += " group by c.id,ci.id";
            centerDataArray = this.jdbcTemplate.query(sql, centerCalendarMapper, new Object[] { passeddate, passeddate, passeddate, passeddate,
                    passeddate, passeddate, officeId, staffId });
        } else {
            centerDataArray = this.jdbcTemplate.query(sql, centerCalendarMapper, new Object[] { passeddate, passeddate, passeddate, passeddate,
                    passeddate, passeddate, officeId });
        }

        Collection<StaffCenterData> staffCenterDataArray = new ArrayList<>();
        Boolean flag = false;
        Integer numberOfDays = 0;
        boolean isSkipRepaymentOnFirstMonthEnabled = this.configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
        if (isSkipRepaymentOnFirstMonthEnabled) {
            numberOfDays = this.configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue();
        }
        for (CenterData centerData : centerDataArray) {
            if (centerData.getCollectionMeetingCalendar().isValidRecurringDate(new LocalDate(meetingDate), isSkipRepaymentOnFirstMonthEnabled, numberOfDays)) {
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

    @SuppressWarnings("deprecation")
    private boolean isCentersHavingMeetingFrequencyBasedOnOfficeIdAndMeetingDate(final Long officeId, final String passeddate,
            final Long staffId) {
        final StringBuilder sb = new StringBuilder(100);
        sb.append("SELECT COUNT(*) ");
        sb.append("FROM m_calendar c ");
        sb.append("JOIN m_calendar_instance ci ON ci.calendar_id=c.id AND ci.entity_type_enum=4 ");
        sb.append("JOIN m_group ce ON ce.id = ci.entity_id ");
        sb.append("JOIN m_group g ON g.parent_id = ce.id ");
        sb.append("WHERE c.start_date = ? AND g.office_id= ? ");
        int i = 0;
        if (staffId != null) {
            sb.append(" AND g.staff_id = ? ");
            i = this.jdbcTemplate.queryForInt(sb.toString(), new Object[] { passeddate, officeId, staffId });
        } else {
            i = this.jdbcTemplate.queryForInt(sb.toString(), new Object[] { passeddate, officeId });
        }
        if (i > 0) { return true; }
        return false;
    }

    public void validateForGenerateCollectionSheet(final Long staffId) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("productivecollectionsheet");
        baseDataValidator.reset().parameter("staffId").value(staffId).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }

    }
}