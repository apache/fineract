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
import java.util.Collection;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.group.data.GroupLevelData;
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
