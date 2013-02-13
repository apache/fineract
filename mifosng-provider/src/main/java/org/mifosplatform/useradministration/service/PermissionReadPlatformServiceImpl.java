/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.data.PermissionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PermissionReadPlatformServiceImpl implements PermissionReadPlatformService {

    private final static Logger logger = LoggerFactory.getLogger(PermissionReadPlatformService.class);

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

        final PermissionUsageDataMapper mapper = new PermissionUsageDataMapper();
        final String sql = mapper.permissionSchema();
        logger.info("retrieveAllPermissions: " + sql);
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    @Override
    public Collection<PermissionData> retrieveAllMakerCheckerablePermissions() {

        context.authenticatedUser();

        final PermissionUsageDataMapper mapper = new PermissionUsageDataMapper();
        final String sql = mapper.makerCheckerablePermissionSchema();
        logger.info("retrieveAllMakerCheckerablePermissions: " + sql);

        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    @Override
    public Collection<PermissionData> retrieveAllRolePermissions(final Long roleId) {

        final PermissionUsageDataMapper mapper = new PermissionUsageDataMapper();
        final String sql = mapper.rolePermissionSchema();
        logger.info("retrieveAllRolePermissions: " + sql);

        return this.jdbcTemplate.query(sql, mapper, new Object[] { roleId });
    }

    private static final class PermissionUsageDataMapper implements RowMapper<PermissionData> {

        @Override
        public PermissionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String grouping = rs.getString("grouping");
            final String code = rs.getString("code");
            final String entityName = rs.getString("entityName");
            final String actionName = rs.getString("actionName");
            final Boolean selected = rs.getBoolean("selected");

            return PermissionData.instance(grouping, code, entityName, actionName, selected);
        }

        public String permissionSchema() {
            /* get all non-CHECKER permissions */
            return "select p.grouping, p.code, p.entity_name as entityName, p.action_name as actionName, true as selected"
                    + " from m_permission p " + " where code not like '%\\_CHECKER'"
                    + " order by p.grouping, ifnull(entity_name, ''), p.code";
        }

        public String makerCheckerablePermissionSchema() {
            /*
             * get all 'Maker-Checkerable' permissions - Maintenance permissions
             * (i.e. exclude the 'special' grouping, the READ permissions and
             * the CHECKER permissions
             */

            return "select p.grouping, p.code, p.entity_name as entityName, p.action_name as actionName, p.can_maker_checker as selected"
                    + " from m_permission p " + " where grouping != 'special' and code not like 'READ_%' and code not like '%\\_CHECKER'"
                    + " order by p.grouping, ifnull(entity_name, ''), p.code";
        }

        public String rolePermissionSchema() {
            return "select p.grouping, p.code, p.entity_name as entityName, p.action_name as actionName, if(isnull(rp.role_id), false, true) as selected "
                    + " from m_permission p "
                    + " left join m_role_permission rp on rp.permission_id = p.id and rp.role_id = ? "
                    + " order by p.grouping, ifnull(entity_name, ''), p.code";
        }
    }

}