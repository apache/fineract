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
package org.apache.fineract.infrastructure.configuration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationData;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
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
    public ConfigurationReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        this.rm = new GlobalConfigurationRowMapper();
    }

    @Override
    public GlobalConfigurationData retrieveGlobalConfiguration(final boolean survey) {

        this.context.authenticatedUser();

        String sql = "SELECT c.id, c.name, c.enabled, c.value, c.date_value, c.description,c.string_value, c.is_trap_door FROM c_configuration c ";

        if (survey) {
            sql += " JOIN x_registered_table on x_registered_table.registered_table_name = c.name ";
            sql += " WHERE x_registered_table.category = ?";

        }

        sql += "  order by c.id";
        final List<GlobalConfigurationPropertyData> globalConfiguration = this.jdbcTemplate.query(sql, this.rm,
                survey ? new Object[] { DataTableApiConstant.CATEGORY_PPI } : new Object[] {});

        return new GlobalConfigurationData(globalConfiguration);
    }

    @Override
    public GlobalConfigurationPropertyData retrieveGlobalConfiguration(final String name) {

        this.context.authenticatedUser();

        final String sql = "SELECT c.id, c.name, c.enabled, c.value, c.date_value, c.string_value, c.description, c.is_trap_door FROM "
                + "c_configuration c where c.name=? order by c.id";
        final GlobalConfigurationPropertyData globalConfiguration = this.jdbcTemplate.queryForObject(sql, this.rm, new Object[] { name });

        return globalConfiguration;
    }

    @Override
    public GlobalConfigurationPropertyData retrieveGlobalConfiguration(Long configId) {

        this.context.authenticatedUser();

        final String sql = "SELECT c.id, c.name, c.enabled, c.value, c.date_value, c.string_value ,c.description, c.is_trap_door FROM "
                + "c_configuration c where c.id=? order by c.id";
        final GlobalConfigurationPropertyData globalConfiguration = this.jdbcTemplate.queryForObject(sql, this.rm,
                new Object[] { configId });

        return globalConfiguration;
    }

    private static final class GlobalConfigurationRowMapper implements RowMapper<GlobalConfigurationPropertyData> {

        @Override
        public GlobalConfigurationPropertyData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {

            final String name = rs.getString("name");
            final boolean enabled = rs.getBoolean("enabled");
            final Long value = rs.getLong("value");
            final Date dateValue = rs.getDate("date_value");
            final String stringValue = rs.getString("string_value");
            final String description = rs.getString("description");
            final Long id = rs.getLong("id");
            final boolean isTrapDoor = rs.getBoolean("is_trap_door");
            return new GlobalConfigurationPropertyData(name, enabled, value, dateValue, stringValue, id, description, isTrapDoor);
        }
    }

}
