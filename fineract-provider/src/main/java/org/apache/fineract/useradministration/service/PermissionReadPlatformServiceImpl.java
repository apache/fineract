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
import java.util.Collection;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.data.PermissionData;
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
    public PermissionReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<PermissionData> retrieveAllPermissions() {

        this.context.authenticatedUser();

        final PermissionUsageDataMapper mapper = new PermissionUsageDataMapper();
        final String sql = mapper.permissionSchema();
        logger.info("retrieveAllPermissions: " + sql);
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    @Override
    public Collection<PermissionData> retrieveAllMakerCheckerablePermissions() {

        this.context.authenticatedUser();

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