/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.security.exception.InvalidTenantIdentiferException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * A JDBC implementation of {@link TenantDetailsService} for loading a tenants
 * details by a <code>tenantIdentifier</code>.
 */
@Service
public class JdbcTenantDetailsService implements TenantDetailsService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTenantDetailsService(@Qualifier("tenantDataSourceJndi") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class TenantMapper implements RowMapper<MifosPlatformTenant> {

        private StringBuilder sqlBuilder = new StringBuilder(
                "id, name,identifier, schema_name as schemaName, schema_server as schemaServer, schema_server_port as schemaServerPort, auto_update as autoUpdate, ")//
                .append(" schema_username as schemaUsername, schema_password as schemaPassword , timezone_id as timezoneId ")//
                .append(" from tenants t");//

        public String schema() {
            return sqlBuilder.toString();
        }

        @Override
        public MifosPlatformTenant mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String tenantIdentifier = rs.getString("identifier");
            String name = rs.getString("name");
            String schemaName = rs.getString("schemaName");
            String schemaServer = rs.getString("schemaServer");
            String schemaServerPort = rs.getString("schemaServerPort");
            String schemaUsername = rs.getString("schemaUsername");
            String schemaPassword = rs.getString("schemaPassword");
            String timezoneId = rs.getString("timezoneId");
            boolean autoUpdateEnabled = rs.getBoolean("autoUpdate");

            return new MifosPlatformTenant(id, tenantIdentifier,name, schemaName, schemaServer, schemaServerPort, schemaUsername, schemaPassword,
                    timezoneId, autoUpdateEnabled);
        }
    }

    @Override
    public MifosPlatformTenant loadTenantById(final String tenantIdentifier) {

        try {
            TenantMapper rm = new TenantMapper();
            String sql = "select  " + rm.schema() + " where t.identifier like ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { tenantIdentifier });
        } catch (EmptyResultDataAccessException e) {
            throw new InvalidTenantIdentiferException("The tenant identifier: " + tenantIdentifier + " is not valid.");
        }
    }

    @Override
    public List<MifosPlatformTenant> findAllTenants() {
        TenantMapper rm = new TenantMapper();
        String sql = "select  " + rm.schema();

        List<MifosPlatformTenant> mifosPlatformTenants = jdbcTemplate.query(sql, rm, new Object[] {});
        return mifosPlatformTenants;
    }
}