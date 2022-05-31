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
package org.apache.fineract.infrastructure.core.service;

import static org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection.toJdbcUrl;
import static org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection.toProtocol;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.security.constants.TenantConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * Factory class to get data source service based on the details stored in {@link FineractPlatformTenantConnection}
 * variable
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataSourcePerTenantServiceFactory {

    private final HikariConfig hikariConfig;
    private final FineractProperties fineractProperties;
    private final ApplicationContext context;

    public DataSource createNewDataSourceFor(final DataSource tenantDataSource, final FineractPlatformTenantConnection tenantConnection) {
        String protocol = toProtocol(tenantDataSource);
        // Default properties for Writing
        String schemaServer = tenantConnection.getSchemaServer();
        String schemaPort = tenantConnection.getSchemaServerPort();
        String schemaName = tenantConnection.getSchemaName();
        String schemaUsername = tenantConnection.getSchemaUsername();
        String schemaPassword = tenantConnection.getSchemaPassword();
        String schemaConnectionParameters = tenantConnection.getSchemaConnectionParameters();
        // Properties to ReadOnly case
        if (this.fineractProperties.getMode().isReadOnlyMode()) {
            schemaServer = getPropertyValue(tenantConnection.getReadOnlySchemaServer(), TenantConstants.PROPERTY_RO_SCHEMA_SERVER_NAME,
                    schemaServer);
            schemaPort = getPropertyValue(tenantConnection.getReadOnlySchemaServerPort(), TenantConstants.PROPERTY_RO_SCHEMA_SERVER_PORT,
                    schemaPort);
            schemaName = getPropertyValue(tenantConnection.getReadOnlySchemaName(), TenantConstants.PROPERTY_RO_SCHEMA_SCHEMA_NAME,
                    schemaName);
            schemaUsername = getPropertyValue(tenantConnection.getReadOnlySchemaUsername(), TenantConstants.PROPERTY_RO_SCHEMA_USERNAME,
                    schemaUsername);
            schemaPassword = getPropertyValue(tenantConnection.getReadOnlySchemaPassword(), TenantConstants.PROPERTY_RO_SCHEMA_PASSWORD,
                    schemaPassword);
            schemaConnectionParameters = getPropertyValue(tenantConnection.getReadOnlySchemaConnectionParameters(),
                    TenantConstants.PROPERTY_RO_SCHEMA_CONNECTION_PARAMETERS, schemaConnectionParameters);
        }
        String jdbcUrl = toJdbcUrl(protocol, schemaServer, schemaPort, schemaName, schemaConnectionParameters);
        log.debug("{}", jdbcUrl);

        HikariConfig config = new HikariConfig();
        config.setReadOnly(this.fineractProperties.getMode().isReadOnlyMode());
        config.setDriverClassName(hikariConfig.getDriverClassName());
        config.setPoolName(schemaName + "_pool");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(schemaUsername);
        config.setPassword(schemaPassword);
        config.setMinimumIdle(tenantConnection.getInitialSize());
        config.setMaximumPoolSize(tenantConnection.getMaxActive());
        config.setConnectionTestQuery(hikariConfig.getConnectionTestQuery());
        config.setValidationTimeout(tenantConnection.getValidationInterval());
        config.setAutoCommit(hikariConfig.isAutoCommit());

        // https://github.com/brettwooldridge/HikariCP/wiki/MBean-(JMX)-Monitoring-and-Management
        config.setRegisterMbeans(true);

        // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        // These are the properties for each Tenant DB; the same configuration
        // is also in src/main/resources/META-INF/spring/hikariDataSource.xml
        // for the all Tenants DB -->
        config.setDataSourceProperties(hikariConfig.getDataSourceProperties());

        return new HikariDataSource(config);
    }

    private String getPropertyValue(final String baseValue, final String propertyName, final String defaultValue) {
        // If the property already has set, return It
        if (null != baseValue) {
            return baseValue;
        }
        if (this.context == null) {
            return defaultValue;
        }
        return this.context.getEnvironment().getProperty(propertyName, defaultValue);
    }

}
