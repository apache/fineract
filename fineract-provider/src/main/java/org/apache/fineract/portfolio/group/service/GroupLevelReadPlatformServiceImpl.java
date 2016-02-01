/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.group.data.GroupLevelData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class GroupLevelReadPlatformServiceImpl implements GroupLevelReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GroupLevelReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<GroupLevelData> retrieveAllLevels() {
        this.context.authenticatedUser();

        final GroupLevelDataMapper rm = new GroupLevelDataMapper();
        final String sql = "select " + rm.groupLevelSchema();
        return this.jdbcTemplate.query(sql, rm);

    }

    private static final class GroupLevelDataMapper implements RowMapper<GroupLevelData> {

        public String groupLevelSchema() {
            return "gl.id as id, gl.level_name as levelName , gl.parent_id as parentLevelId , pgl.level_name as parentName , "
                    + "cgl.id as childLevelId,cgl.level_name as childLevelName,gl.super_parent as superParent ,"
                    + " gl.recursable as recursable , gl.can_have_clients as canHaveClients from m_group_level gl "
                    + " left join m_group_level pgl on pgl.id = gl.parent_id left join m_group_level cgl on gl.id = cgl.parent_id";
        }

        @Override
        public GroupLevelData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long levelId = rs.getLong("id");
            final String levelName = rs.getString("levelName");
            final Long parentLevelId = JdbcSupport.getLong(rs, "parentLevelId");
            final String parentLevelName = rs.getString("parentName");
            final Long childLevelId = JdbcSupport.getLong(rs, "childLevelId");
            final String childLevelName = rs.getString("childLevelName");
            final boolean superParent = rs.getBoolean("superParent");
            final boolean recursable = rs.getBoolean("recursable");
            final boolean canHaveClients = rs.getBoolean("canHaveClients");

            return new GroupLevelData(levelId, levelName, parentLevelId, parentLevelName, childLevelId, childLevelName, superParent,
                    recursable, canHaveClients);
        }

    }

}
