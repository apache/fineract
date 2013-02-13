/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.infrastructure.configuration.data.GlobalConfigurationData;
import org.mifosplatform.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationReadPlatformServiceImpl implements ConfigurationReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final RowMapper<GlobalConfigurationPropertyData> rm;

    @Autowired
    public ConfigurationReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        rm = new GlobalConfigurationRowMapper();
    }

    @Override
    public GlobalConfigurationData retrieveGlobalConfiguration() {

        context.authenticatedUser();

        final String sql = "SELECT c.name, c.enabled FROM c_configuration c order by c.id";
        final List<GlobalConfigurationPropertyData> globalConfiguration = this.jdbcTemplate.query(sql, rm, new Object[] {});

        return new GlobalConfigurationData(globalConfiguration);
    }

    private static final class GlobalConfigurationRowMapper implements RowMapper<GlobalConfigurationPropertyData> {

        @Override
        public GlobalConfigurationPropertyData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String name = rs.getString("name");
            final boolean enabled = rs.getBoolean("enabled");

            return new GlobalConfigurationPropertyData(name, enabled);
        }
    }
}