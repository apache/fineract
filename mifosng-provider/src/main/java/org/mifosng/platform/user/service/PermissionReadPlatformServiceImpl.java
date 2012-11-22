package org.mifosng.platform.user.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.api.data.PermissionUsageData;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PermissionReadPlatformServiceImpl implements PermissionReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public PermissionReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<PermissionData> retrieveAllPermissions() {

        context.authenticatedUser();

        final PermissionMapper mapper = new PermissionMapper();
        final String sql = "select " + mapper.schema() + " order by p.id";

        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    private static final class PermissionMapper implements RowMapper<PermissionData> {

        @Override
        public PermissionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final String code = rs.getString("code");

            return new PermissionData(id, name, description, code);
        }

        public String schema() {
            return " p.id as id, p.default_name as name, p.default_description as description, p.code as code from m_permission p ";
        }
    }

    @Override
    public Collection<PermissionUsageData> retrieveAllRolePermissions(final Long roleId) {

        final PermissionUsageDataMapper mapper = new PermissionUsageDataMapper();
        final String sql = mapper.schema();

        return this.jdbcTemplate.query(sql, mapper, new Object[] { roleId });
    }

    private static final class PermissionUsageDataMapper implements RowMapper<PermissionUsageData> {

        @Override
        public PermissionUsageData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String grouping = rs.getString("grouping");
            final String code = rs.getString("code");
            final String entityName = rs.getString("entityName");
            final String actionName = rs.getString("actionName");
            final Boolean selected = rs.getBoolean("selected");

            return new PermissionUsageData(grouping, code, entityName, actionName, selected);
        }

        public String schema() {
            return "select p.grouping, p.code, p.entity_name as entityName, p.action_name as actionName, if(isnull(rp.role_id), false, true) as selected "
                    + "from m_permission p "
                    + "left join m_role_permission rp on rp.permission_id = p.id and rp.role_id = ? "
                    + "order by p.grouping, ifnull(entity_name, ''), p.code";
        }
    }

}