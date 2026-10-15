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
package org.apache.fineract.useradministration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@RequiredArgsConstructor
public class AppUserReadPlatformServiceImpl implements AppUserReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final RoleReadPlatformService roleReadPlatformService;
    private final AppUserRepository appUserRepository;

    /*
     * used for caching in spring expression language.
     */
    public PlatformSecurityContext getContext() {
        return this.context;
    }

    @Override
    @Cacheable(value = "users", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy())")
    public Collection<AppUserData> retrieveAllUsers() {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final AppUserMapper mapper = new AppUserMapper(this.roleReadPlatformService);
        final String sql = "select " + mapper.schema();

        return this.jdbcTemplate.query(sql, mapper, new Object[] { hierarchySearchString }); // NOSONAR
    }

    @Override
    public Collection<AppUserData> retrieveSearchTemplate() {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final AppUserLookupMapper mapper = new AppUserLookupMapper();
        final String sql = "select " + mapper.schema();

        return this.jdbcTemplate.query(sql, mapper, new Object[] { hierarchySearchString }); // NOSONAR
    }

    @Override
    public AppUserData retrieveNewUserDetails() {

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
        final Collection<RoleData> availableRoles = this.roleReadPlatformService.retrieveAllActiveRoles();

        return AppUserData.template(offices, availableRoles);
    }

    @Override
    public AppUserData retrieveUser(final Long userId) {

        final String callerHierarchy = this.context.authenticatedUser().getOffice().getHierarchy();

        final AppUser user = this.appUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (user.isDeleted()) {
            throw new UserNotFoundException(userId);
        }
        // Scope the target user to the caller's office hierarchy, as retrieveAllUsers does. The linked staff below
        // is read without a hierarchy filter, so this check is what keeps users outside the caller's tree unreadable.
        final String userHierarchy = user.getOffice().getHierarchy();
        if (userHierarchy == null || callerHierarchy == null || !userHierarchy.startsWith(callerHierarchy)) {
            throw new UserNotFoundException(userId);
        }

        final Collection<RoleData> availableRoles = this.roleReadPlatformService.retrieveAll();

        final Collection<RoleData> selectedUserRoles = new ArrayList<>();
        final Set<Role> userRoles = user.getRoles();
        for (final Role role : userRoles) {
            selectedUserRoles.add(role.toData());
        }

        availableRoles.removeAll(selectedUserRoles);

        final StaffData linkedStaff;
        if (user.getStaff() != null) {
            final String sql = "select " + LinkedStaffMapper.SCHEMA
                    + " from m_staff s join m_office so on so.id = s.office_id where s.id = ?";
            linkedStaff = this.jdbcTemplate.query(sql, (rs, rowNum) -> LinkedStaffMapper.map(rs), user.getStaffId()).stream().findFirst()
                    .orElse(null);
        } else {
            linkedStaff = null;
        }

        AppUserData retUser = AppUserData.instance(user.getId(), user.getUsername(), user.getEmail(), user.getOffice().getId(),
                user.getOffice().getName(), user.getFirstname(), user.getLastname(), availableRoles, selectedUserRoles, linkedStaff,
                user.getPasswordNeverExpires());

        return retUser;
    }

    private static final class AppUserMapper implements RowMapper<AppUserData> {

        private final RoleReadPlatformService roleReadPlatformService;

        AppUserMapper(final RoleReadPlatformService roleReadPlatformService) {
            this.roleReadPlatformService = roleReadPlatformService;
        }

        @Override
        public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String username = rs.getString("username");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String email = rs.getString("email");
            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");
            final Boolean passwordNeverExpire = rs.getBoolean("passwordNeverExpires");
            final Collection<RoleData> selectedRoles = this.roleReadPlatformService.retrieveAppUserRoles(id);

            final StaffData linkedStaff = LinkedStaffMapper.map(rs);
            return AppUserData.instance(id, username, email, officeId, officeName, firstname, lastname, null, selectedRoles, linkedStaff,
                    passwordNeverExpire);
        }

        public String schema() {
            return " u.id as id, u.username as username, u.firstname as firstname, u.lastname as lastname, u.email as email, u.password_never_expires as passwordNeverExpires, "
                    + " u.office_id as officeId, o.name as officeName, " + LinkedStaffMapper.SCHEMA + " from m_appuser u "
                    + " join m_office o on o.id = u.office_id left join m_staff s on s.id = u.staff_id left join m_office so on so.id = s.office_id"
                    + " where o.hierarchy like ? and u.is_deleted=false order by u.username";
        }

    }

    private static final class LinkedStaffMapper {

        static final String SCHEMA = "s.id as staffId, s.office_id as staffOfficeId, so.name as staffOfficeName, s.firstname as staffFirstname,"
                + " s.lastname as staffLastname, s.display_name as staffDisplayName, s.is_loan_officer as staffIsLoanOfficer,"
                + " s.external_id as staffExternalId, s.mobile_no as staffMobileNo, s.is_active as staffIsActive, s.joining_date as staffJoiningDate";

        private LinkedStaffMapper() {}

        static StaffData map(final ResultSet rs) throws SQLException {
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            if (staffId == null) {
                return null;
            }
            final LocalDate joiningDate = JdbcSupport.getLocalDate(rs, "staffJoiningDate");
            return StaffData.builder().id(staffId).firstname(rs.getString("staffFirstname")).lastname(rs.getString("staffLastname"))
                    .displayName(rs.getString("staffDisplayName")).officeId(rs.getLong("staffOfficeId"))
                    .officeName(rs.getString("staffOfficeName")).isLoanOfficer(rs.getBoolean("staffIsLoanOfficer"))
                    .externalId(rs.getString("staffExternalId")).mobileNo(rs.getString("staffMobileNo"))
                    .isActive(rs.getBoolean("staffIsActive")).joiningDate(joiningDate).build();
        }
    }

    private static final class AppUserLookupMapper implements RowMapper<AppUserData> {

        @Override
        public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String username = rs.getString("username");

            return AppUserData.dropdown(id, username);
        }

        public String schema() {
            return " u.id as id, u.username as username from m_appuser u "
                    + " join m_office o on o.id = u.office_id where o.hierarchy like ? and u.is_deleted=false order by u.username";
        }
    }

    @Override
    public boolean isUsernameExist(String username) {
        String sql = "select count(*) from m_appuser where username = ?";
        Object[] params = new Object[] { username };
        int count = this.jdbcTemplate.queryForObject(sql, Integer.class, params);
        return count != 0;
    }
}
