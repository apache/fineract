/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.service;

import org.mifosplatform.infrastructure.configuration.data.GlobalConfigurationData;
import org.mifosplatform.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.api.DataTableApiConstant;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class ConfigurationReadPlatformServiceImpl implements ConfigurationReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final RowMapper<GlobalConfigurationPropertyData> rm;

    @Autowired
    public ConfigurationReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        this.rm = new GlobalConfigurationRowMapper();
    }

    @Override
    public GlobalConfigurationData retrieveGlobalConfiguration(final boolean survey) {

        this.context.authenticatedUser();

        String sql = "SELECT c.id, c.name, c.enabled, c.value, c.description FROM c_configuration c ";

        if(survey)
        {
             sql += " JOIN x_registered_table on x_registered_table.registered_table_name = c.name ";
             sql += " WHERE x_registered_table.category ="+DataTableApiConstant.CATEGORY_PPI;

        }

        sql += "  order by c.id";
        final List<GlobalConfigurationPropertyData> globalConfiguration = this.jdbcTemplate.query(sql, this.rm, new Object[] {});

        return new GlobalConfigurationData(globalConfiguration);
    }
    
    @Override
    public GlobalConfigurationPropertyData retrieveGlobalConfiguration(Long configId) {

        this.context.authenticatedUser();

        final String sql = "SELECT c.id, c.name, c.enabled, c.value, c.description FROM "
        		+ "c_configuration c where c.id=? order by c.id";
        final GlobalConfigurationPropertyData globalConfiguration = this.jdbcTemplate.queryForObject(sql, this.rm, new Object[] {configId});

        return globalConfiguration;
    }



    private static final class GlobalConfigurationRowMapper implements RowMapper<GlobalConfigurationPropertyData> {

        
        @Override
        public GlobalConfigurationPropertyData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String name = rs.getString("name");
            final boolean enabled = rs.getBoolean("enabled");
            final Long value = rs.getLong("value");
            final String description = rs.getString("description");
            final Long id = rs.getLong("id");

            return new GlobalConfigurationPropertyData(name, enabled, value, id, description);
        }
    }

}