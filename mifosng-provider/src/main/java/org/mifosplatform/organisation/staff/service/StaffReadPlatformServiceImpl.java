/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.staff.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.exception.StaffNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class StaffReadPlatformServiceImpl implements StaffReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final StaffLookupMapper lookupMapper = new StaffLookupMapper();

    @Autowired
    public StaffReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class StaffMapper implements RowMapper<StaffData> {

        public String schema() {
            return " s.id as id,s.office_id as officeId, o.name as officeName, s.firstname as firstname, s.lastname as lastname,"
                    + " s.display_name as displayName, s.is_loan_officer as isLoanOfficer from m_staff s "
                    + " join m_office o on o.id = s.office_id";
        }

        @Override
        public StaffData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final boolean isLoanOfficer = rs.getBoolean("isLoanOfficer");
            final String officeName = rs.getString("officeName");

            return StaffData.instance(id, firstname, lastname, displayName, officeId, officeName, isLoanOfficer);
        }
    }

    private static final class StaffLookupMapper implements RowMapper<StaffData> {

        private final String schemaSql;

        public StaffLookupMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(100);
            sqlBuilder.append("s.id as id, s.display_name as displayName ");
            sqlBuilder.append("from m_staff s ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public StaffData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String displayName = rs.getString("displayName");

            return StaffData.lookup(id, displayName);
        }
    }

    @Override
    public Collection<StaffData> retrieveAllStaff(final String extraCriteria) {

        final StaffMapper rm = new StaffMapper();
        String sql = "select " + rm.schema();
        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " where " + extraCriteria;
        }
        sql = sql + " order by s.lastname";
        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public Collection<StaffData> retrieveAllLoanOfficersByOffice(final Long officeId) {
        return retrieveAllStaff(" office_id=" + officeId + " and is_loan_officer=1");
    }

    @Override
    public Collection<StaffData> retrieveAllStaffForDropdown(final Long officeId) {

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);

        final String sql = "select " + this.lookupMapper.schema() + " where s.office_id = ?";

        return this.jdbcTemplate.query(sql, this.lookupMapper, new Object[] { defaultOfficeId });
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public StaffData retrieveStaff(final Long staffId) {

        try {
            context.authenticatedUser();

            final StaffMapper rm = new StaffMapper();
            final String sql = "select " + rm.schema() + " where s.id = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { staffId });
        } catch (EmptyResultDataAccessException e) {
            throw new StaffNotFoundException(staffId);
        }
    }
}