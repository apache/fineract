/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.useradministration.data.RoleData;
import org.mifosplatform.useradministration.exception.RoleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class RoleReadPlatformServiceImpl implements RoleReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final RoleMapper roleRowMapper;

    @Autowired
    public RoleReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.roleRowMapper = new RoleMapper();
    }

    @Override
    public Collection<RoleData> retrieveAll() {
        final String sql = "select " + this.roleRowMapper.schema() + " order by r.id";

        return this.jdbcTemplate.query(sql, this.roleRowMapper);
    }

    @Override
    public Collection<RoleData> retrieveAllActiveRoles() {
        final String sql = "select " + this.roleRowMapper.schema() + " where r.is_disabled = 0 order by r.id";

        return this.jdbcTemplate.query(sql, this.roleRowMapper);
    }

    @Override
    public RoleData retrieveOne(final Long id) {

        try {
            final String sql = "select " + this.roleRowMapper.schema() + " where r.id=?";

            return this.jdbcTemplate.queryForObject(sql, this.roleRowMapper, new Object[] { id });
        } catch (final EmptyResultDataAccessException e) {
            throw new RoleNotFoundException(id);
        }
    }

    protected static final class RoleMapper implements RowMapper<RoleData> {

        @Override
        public RoleData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final Boolean disabled = rs.getBoolean("disabled");
            
            return new RoleData(id, name, description, disabled);
        }

        public String schema() {
            return " r.id as id, r.name as name, r.description as description, r.is_disabled as disabled from m_role r";
        }
    }

    @Override
    public Collection<RoleData> retrieveAppUserRoles(final Long appUserId) {
        final String sql = "select " + this.roleRowMapper.schema() + " inner join m_appuser_role"
                + " ar on ar.role_id = r.id where ar.appuser_id= ?";

        return this.jdbcTemplate.query(sql, this.roleRowMapper, new Object[] { appUserId });
    }
}