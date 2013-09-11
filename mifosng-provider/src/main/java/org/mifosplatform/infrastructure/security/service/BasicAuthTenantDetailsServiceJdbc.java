/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.security.exception.InvalidTenantIdentiferException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * A JDBC implementation of {@link BasicAuthTenantDetailsService} for loading a
 * tenants details by a <code>tenantIdentifier</code>.
 */
@Service
public class BasicAuthTenantDetailsServiceJdbc implements BasicAuthTenantDetailsService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BasicAuthTenantDetailsServiceJdbc(@Qualifier("tenantDataSourceJndi") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class TenantMapper implements RowMapper<MifosPlatformTenant> {

        private final StringBuilder sqlBuilder = new StringBuilder(
                "id, name,identifier, schema_name as schemaName, schema_server as schemaServer, schema_server_port as schemaServerPort, auto_update as autoUpdate, ")//
                .append(" schema_username as schemaUsername, schema_password as schemaPassword , timezone_id as timezoneId ")//
                .append(" from tenants t");//

        public String schema() {
            return this.sqlBuilder.toString();
        }

        @Override
        public MifosPlatformTenant mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String tenantIdentifier = rs.getString("identifier");
            final String name = rs.getString("name");
            final String schemaName = rs.getString("schemaName");
            final String schemaServer = rs.getString("schemaServer");
            final String schemaServerPort = rs.getString("schemaServerPort");
            final String schemaUsername = rs.getString("schemaUsername");
            final String schemaPassword = rs.getString("schemaPassword");
            final String timezoneId = rs.getString("timezoneId");
            final boolean autoUpdateEnabled = rs.getBoolean("autoUpdate");

            return new MifosPlatformTenant(id, tenantIdentifier, name, schemaName, schemaServer, schemaServerPort, schemaUsername,
                    schemaPassword, timezoneId, autoUpdateEnabled);
        }
    }

    @Override
    @Cacheable(value = "tenantsById")
    public MifosPlatformTenant loadTenantById(final String tenantIdentifier) {

        try {
            final TenantMapper rm = new TenantMapper();
            final String sql = "select  " + rm.schema() + " where t.identifier like ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { tenantIdentifier });
        } catch (final EmptyResultDataAccessException e) {
            throw new InvalidTenantIdentiferException("The tenant identifier: " + tenantIdentifier + " is not valid.");
        }
    }
}