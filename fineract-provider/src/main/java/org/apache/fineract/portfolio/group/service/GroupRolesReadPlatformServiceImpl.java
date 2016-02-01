/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.group.data.GroupRoleData;
import org.mifosplatform.portfolio.group.exception.GroupRoleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class GroupRolesReadPlatformServiceImpl implements GroupRolesReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public GroupRolesReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<GroupRoleData> retrieveGroupRoles(final Long groupId) {
        this.context.authenticatedUser();
        final GroupRolesDataMapper mapper = new GroupRolesDataMapper();
        final String sql = "Select " + mapper.schema() + " where role.group_id=?";
        return this.jdbcTemplate.query(sql, mapper, new Object[] { groupId });
    }

    @Override
    public GroupRoleData retrieveGroupRole(final Long groupId, final Long roleId) {
        try {
            this.context.authenticatedUser();
            final GroupRolesDataMapper mapper = new GroupRolesDataMapper();
            final String sql = "Select " + mapper.schema() + " where role.group_id=? and role.id=?";
            return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { groupId, roleId });
        } catch (final EmptyResultDataAccessException e) {
            throw new GroupRoleNotFoundException(roleId);
        }
    }

    private static final class GroupRolesDataMapper implements RowMapper<GroupRoleData> {

        public final String schema() {
            return " role.id AS id, role.client_id AS clientId, c.display_name as clientName, role.role_cv_id AS roleId, cv.code_value AS roleName"
                    + " from m_code_value cv join m_group_roles role on role.role_cv_id = cv.id left join m_client c on c.id = role.client_id ";
        }

        @Override
        public GroupRoleData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("clientName");
            final Long roleId = JdbcSupport.getLong(rs, "roleId");
            final String roleName = rs.getString("roleName");
            final CodeValueData role = CodeValueData.instance(roleId, roleName);
            return new GroupRoleData(id, role, clientId, clientName);
        }

    }

}
