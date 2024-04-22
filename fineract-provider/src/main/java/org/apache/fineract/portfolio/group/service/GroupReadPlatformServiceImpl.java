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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.data.PaginationParametersDataValidator;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
public class GroupReadPlatformServiceImpl implements GroupReadPlatformService {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String OFFICE_ID = "officeId";
    public static final String OFFICE_NAME = "officeName";
    private static final AllGroupTypesDataMapper ALL_GROUP_TYPES_DATA_MAPPER = new AllGroupTypesDataMapper();
    private static final Set<String> SUPPORTED_ORDER_BY_VALUES = new HashSet<>(Arrays.asList(ID, NAME, OFFICE_ID, OFFICE_NAME));
    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final CenterReadPlatformService centerReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final PaginationParametersDataValidator paginationParametersDataValidator;
    private final ColumnValidator columnValidator;

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

        this.paginationParametersDataValidator.validateParameterValues(parameters, SUPPORTED_ORDER_BY_VALUES, "audits");
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append(ALL_GROUP_TYPES_DATA_MAPPER.schema());

        final SQLBuilder extraCriteria = getGroupExtraCriteria(searchParameters);
        extraCriteria.addCriteria(" o.hierarchy like ", hierarchySearchString);
        sqlBuilder.append(" ").append(extraCriteria.getSQLTemplate());
        if (parameters.hasOrderBy()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy()).append(' ').append(searchParameters.getSortOrder());
            this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy(),
                    searchParameters.getSortOrder());
        }

        if (parameters.hasLimit()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.hasOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), extraCriteria.getArguments(),
                ALL_GROUP_TYPES_DATA_MAPPER);
    }

    @Override
    public Collection<GroupGeneralData> retrieveAll(SearchParameters searchParameters, final PaginationParameters parameters) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(ALL_GROUP_TYPES_DATA_MAPPER.schema());
        final SQLBuilder extraCriteria = getGroupExtraCriteria(searchParameters);
        extraCriteria.addCriteria("o.hierarchy like ", hierarchySearchString);

        sqlBuilder.append(" ").append(extraCriteria.getSQLTemplate());

        if (searchParameters != null && searchParameters.getOrphansOnly()) {
            sqlBuilder.append(" and g.parent_id is NULL");
        }

        if (parameters != null) {
            if (parameters.hasOrderBy()) {
                sqlBuilder.append(parameters.orderBySql());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), parameters.orderBySql());
            }

            if (parameters.hasLimit()) {
                sqlBuilder.append(parameters.limitSql());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), parameters.limitSql());
            }
        }

        return this.jdbcTemplate.query(sqlBuilder.toString(), ALL_GROUP_TYPES_DATA_MAPPER, extraCriteria.getArguments()); // NOSONAR
    }

    // 'g.' preffix because of ERROR 1052 (23000): Column 'column_name' in where
    // clause is ambiguous
    // caused by the same name of columns in m_office and m_group tables
    private SQLBuilder getGroupExtraCriteria(final SearchParameters searchCriteria) {

        SQLBuilder extraCriteria = new SQLBuilder();
        if (searchCriteria == null) {
            return extraCriteria;
        }
        extraCriteria.addCriteria("g.level_Id = ", GroupTypes.GROUP.getId());

        extraCriteria.addNonNullCriteria(" g.office_id = ", searchCriteria.getOfficeId());

        extraCriteria.addNonNullCriteria(" g.external_id =", searchCriteria.getExternalId());

        final String name = searchCriteria.getName();
        if (name != null) {
            extraCriteria.addNonNullCriteria("g.display_name like", "%" + name + "%");
        }

        final String hierarchy = searchCriteria.getHierarchy();
        if (hierarchy != null) {
            extraCriteria.addNonNullCriteria("o.hierarchy like ", hierarchy + "%");
        }
        extraCriteria.addNonNullCriteria("g.staff_id =", searchCriteria.getStaffId());

        return extraCriteria;
    }

    @Override
    public GroupGeneralData retrieveOne(final Long groupId) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            final String sql = "select " + ALL_GROUP_TYPES_DATA_MAPPER.schema() + " where g.id = ? and o.hierarchy like ?";
            return this.jdbcTemplate.queryForObject(sql, ALL_GROUP_TYPES_DATA_MAPPER, groupId, hierarchySearchString); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new GroupNotFoundException(groupId, e);
        }
    }

    @Override
    public Collection<GroupGeneralData> retrieveGroupsForLookup(final Long officeId) {
        this.context.authenticatedUser();
        final GroupLookupDataMapper rm = new GroupLookupDataMapper();
        final String sql = "Select " + rm.schema() + " and g.office_id=?";
        return this.jdbcTemplate.query(sql, rm, officeId); // NOSONAR
    }

    @Override
    public GroupGeneralData retrieveGroupWithClosureReasons() {
        final List<CodeValueData> closureReasons = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(GroupingTypesApiConstants.GROUP_CLOSURE_REASON));
        return GroupGeneralData.withClosureReasons(closureReasons);
    }

    private static final class GroupLookupDataMapper implements RowMapper<GroupGeneralData> {

        public static final String G_ID_AS_ID_G_ACCOUNT_NO_AS_ACCOUNT_NO_G_DISPLAY_NAME_AS_DISPLAY_NAME_FROM_M_GROUP_G_WHERE_G_LEVEL_ID_2 = "g.id as id, g.account_no as accountNo, g.display_name as displayName from m_group g where g.level_id = 2 ";

        public String schema() {
            return G_ID_AS_ID_G_ACCOUNT_NO_AS_ACCOUNT_NO_G_DISPLAY_NAME_AS_DISPLAY_NAME_FROM_M_GROUP_G_WHERE_G_LEVEL_ID_2;
        }

        @Override
        public GroupGeneralData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, ID);
            final String accountNo = rs.getString("accountNo");
            final String displayName = rs.getString("displayName");
            return GroupGeneralData.lookup(id, accountNo, displayName);
        }
    }

}
