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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.data.PaginationParametersDataValidator;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.infrastructure.security.utils.SQLInjectionValidator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.apache.fineract.portfolio.group.data.CenterData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.domain.GroupTypes;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
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
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final CenterReadPlatformService centerReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    private final AllGroupTypesDataMapper allGroupTypesDataMapper = new AllGroupTypesDataMapper();
    private final PaginationHelper<GroupGeneralData> paginationHelper = new PaginationHelper<>();
    private final PaginationParametersDataValidator paginationParametersDataValidator;
    private final ColumnValidator columnValidator;

    private final static Set<String> supportedOrderByValues = new HashSet<>(Arrays.asList("id", "name", "officeId", "officeName"));

    @Autowired
    public GroupReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final CenterReadPlatformService centerReadPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final StaffReadPlatformService staffReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final PaginationParametersDataValidator paginationParametersDataValidator,
            final ColumnValidator columnValidator) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.centerReadPlatformService = centerReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.paginationParametersDataValidator = paginationParametersDataValidator;
        this.columnValidator = columnValidator;
    }

    @Override
    public GroupGeneralData retrieveTemplate(final Long officeId, final boolean isCenterGroup, final boolean staffInSelectedOfficeOnly) {

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);

        Collection<CenterData> centerOptions = null;
        if (isCenterGroup) {
            centerOptions = this.centerReadPlatformService.retrieveAllForDropdown(defaultOfficeId);
        }

        final Collection<OfficeData> officeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        final boolean loanOfficersOnly = false;
        Collection<StaffData> staffOptions = null;
        if (staffInSelectedOfficeOnly) {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(defaultOfficeId);
        } else {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(defaultOfficeId,
                    loanOfficersOnly);
        }

        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }

        final Collection<CodeValueData> availableRoles = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(GroupingTypesApiConstants.GROUP_ROLE_NAME);

        final Long centerId = null;
        final String accountNo = null;
        final String centerName = null;
        final Long staffId = null;
        final String staffName = null;
        final Collection<ClientData> clientOptions = null;
        
        return GroupGeneralData.template(defaultOfficeId, centerId, accountNo, centerName, staffId, staffName, centerOptions, officeOptions,
                staffOptions, clientOptions, availableRoles);
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public Page<GroupGeneralData> retrievePagedAll(final SearchParameters searchParameters, final PaginationParameters parameters) {

        this.paginationParametersDataValidator.validateParameterValues(parameters, supportedOrderByValues, "audits");
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.allGroupTypesDataMapper.schema());
        sqlBuilder.append(" where o.hierarchy like ?");
        List<Object> paramList = new ArrayList<>(
                Arrays.asList(hierarchySearchString));
        final String extraCriteria = getGroupExtraCriteria(this.allGroupTypesDataMapper.schema(), paramList, searchParameters);
        this.columnValidator.validateSqlInjection(sqlBuilder.toString(), extraCriteria);
        if (StringUtils.isNotBlank(extraCriteria)) {
            sqlBuilder.append(" and (").append(extraCriteria).append(")");
        }

        if (parameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy()).append(' ').append(searchParameters.getSortOrder());
            this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy(),
            		searchParameters.getSortOrder());
        }

        if (parameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
        		paramList.toArray(), this.allGroupTypesDataMapper);
    }

    @Override
    public Collection<GroupGeneralData> retrieveAll(SearchParameters searchParameters, final PaginationParameters parameters) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(this.allGroupTypesDataMapper.schema());
        sqlBuilder.append(" where o.hierarchy like ?");
        List<Object> paramList = new ArrayList<>(
                Arrays.asList(hierarchySearchString));
        if (searchParameters!=null) {
            final String extraCriteria = getGroupExtraCriteria(this.allGroupTypesDataMapper.schema(), paramList, searchParameters);

            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" and (").append(extraCriteria).append(")");
            }
        }
        if (parameters!=null) {
            if (parameters.isOrderByRequested()) {
                sqlBuilder.append(parameters.orderBySql());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), parameters.orderBySql());
            }

            if (parameters.isLimited()) {
                sqlBuilder.append(parameters.limitSql());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), parameters.limitSql());
            }
        }
        return this.jdbcTemplate.query(sqlBuilder.toString(), this.allGroupTypesDataMapper, paramList.toArray());
    }

    // 'g.' preffix because of ERROR 1052 (23000): Column 'column_name' in where
    // clause is ambiguous
    // caused by the same name of columns in m_office and m_group tables
    private String getGroupExtraCriteria(String schemaSql, List<Object> paramList, final SearchParameters searchCriteria) {

        StringBuffer extraCriteria = new StringBuffer(200);
        extraCriteria.append(" and g.level_Id = ").append(GroupTypes.GROUP.getId());
            String sqlSearch = searchCriteria.getSqlSearch();
            if (sqlSearch != null) {
                SQLInjectionValidator.validateSQLInput(sqlSearch);
                sqlSearch = sqlSearch.replaceAll(" display_name ", " g.display_name ");
                sqlSearch = sqlSearch.replaceAll("display_name ", "g.display_name ");
                extraCriteria.append(" and ( ").append(sqlSearch).append(") ");
                this.columnValidator.validateSqlInjection(schemaSql, sqlSearch);
            }

            final Long officeId = searchCriteria.getOfficeId();
            if (officeId != null) {
                paramList.add(officeId);
                extraCriteria.append(" and g.office_id = ? ");
            }

            final String externalId = searchCriteria.getExternalId();
            if (externalId != null) {
                paramList.add(ApiParameterHelper.sqlEncodeString(externalId));
                extraCriteria.append(" and g.external_id = ? ");
            }

        final String name = searchCriteria.getName();
        if (name != null) {
        	paramList.add("%" + name + "%");
            extraCriteria.append(" and g.display_name like ? ");
        }

            final String hierarchy = searchCriteria.getHierarchy();
            if (hierarchy != null) {
                paramList.add(ApiParameterHelper.sqlEncodeString(hierarchy + "%"));
                extraCriteria.append(" and o.hierarchy like ? ");
            }

            if (searchCriteria.isStaffIdPassed()) {
                paramList.add(searchCriteria.getStaffId());
                extraCriteria.append(" and g.staff_id = ? ");
            }

            if (StringUtils.isNotBlank(extraCriteria.toString())) {
                extraCriteria.delete(0, 4);
            }

            final Long staffId = searchCriteria.getStaffId();
            if (staffId != null) {
                paramList.add(staffId);
                extraCriteria.append(" and g.staff_id = ? ");
            }

            if (searchCriteria.isOrphansOnly()) {
                extraCriteria.append(" and g.parent_id IS NULL");
            }
        return extraCriteria.toString();
    }

    @Override
    public GroupGeneralData retrieveOne(final Long groupId) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            final String sql = "select " + this.allGroupTypesDataMapper.schema() + " where g.id = ? and o.hierarchy like ?";
            return this.jdbcTemplate.queryForObject(sql, this.allGroupTypesDataMapper, new Object[] { groupId, hierarchySearchString });
        } catch (final EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId);
        }
    }

    @Override
    public Collection<GroupGeneralData> retrieveGroupsForLookup(final Long officeId) {
        this.context.authenticatedUser();
        final GroupLookupDataMapper rm = new GroupLookupDataMapper();
        final String sql = "Select " + rm.schema() + " and g.office_id=?";
        return this.jdbcTemplate.query(sql, rm, new Object[] { officeId });
    }

    private static final class GroupLookupDataMapper implements RowMapper<GroupGeneralData> {

        public final String schema() {
            return "g.id as id, g.account_no as accountNo, g.display_name as displayName from m_group g where g.level_id = 2 ";
        }

        @Override
        public GroupGeneralData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String displayName = rs.getString("displayName");
            return GroupGeneralData.lookup(id, accountNo, displayName);
        }
    }

    @Override
    public GroupGeneralData retrieveGroupWithClosureReasons() {
        final List<CodeValueData> closureReasons = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(GroupingTypesApiConstants.GROUP_CLOSURE_REASON));
        return GroupGeneralData.withClosureReasons(closureReasons);
    }

}