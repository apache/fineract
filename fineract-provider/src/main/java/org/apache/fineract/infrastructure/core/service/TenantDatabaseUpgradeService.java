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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.fineract.infrastructure.core.boot.JDBCDriverConfig;
import org.apache.fineract.infrastructure.core.boot.db.TenantDataSourcePortFixService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.security.service.TenantDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;

/**
 * A service that picks up on tenants that are configured to auto-update their
 * specific schema on application startup.
 */
@Service
public class TenantDatabaseUpgradeService {

    private final TenantDetailsService tenantDetailsService;
    protected final DataSource tenantDataSource;
    protected final TenantDataSourcePortFixService tenantDataSourcePortFixService;
    
    @Autowired private JDBCDriverConfig driverConfig ;
    
    @Autowired
    public TenantDatabaseUpgradeService(final TenantDetailsService detailsService,
            @Qualifier("tenantDataSourceJndi") final DataSource dataSource, TenantDataSourcePortFixService tenantDataSourcePortFixService) {
        this.tenantDetailsService = detailsService;
        this.tenantDataSource = dataSource;
        this.tenantDataSourcePortFixService = tenantDataSourcePortFixService;
    }

    @PostConstruct
    public void upgradeAllTenants() {
        upgradeTenantDB();
        final List<FineractPlatformTenant> tenants = this.tenantDetailsService.findAllTenants();
        for (final FineractPlatformTenant tenant : tenants) {
            final FineractPlatformTenantConnection connection = tenant.getConnection();
            if (connection.isAutoUpdateEnabled()) {
                final Flyway flyway = new Flyway();
                String connectionProtocol = driverConfig.constructProtocol(connection.getSchemaServer(), connection.getSchemaServerPort(), connection.getSchemaName()) ;
                DriverDataSource source = new DriverDataSource(driverConfig.getDriverClassName(), connectionProtocol, connection.getSchemaUsername(), connection.getSchemaPassword()) ;
                flyway.setDataSource(source);
                flyway.setLocations("sql/migrations/core_db");
                flyway.setOutOfOrder(true);
                try {
                    flyway.migrate();
                } catch (FlywayException e) {
                    String betterMessage = e.getMessage() + "; for Tenant DB URL: " + connectionProtocol + ", username: "
                            + connection.getSchemaUsername();
                    throw new FlywayException(betterMessage, e.getCause());
                }
            }
        }
    }

    /**
     * Initializes, and if required upgrades (using Flyway) the Tenant DB
     * itself.
     */
    private void upgradeTenantDB() {
        final Flyway flyway = new Flyway();
        flyway.setDataSource(tenantDataSource);
        flyway.setLocations("sql/migrations/list_db");
        flyway.setOutOfOrder(true);
        flyway.migrate();

        tenantDataSourcePortFixService.fixUpTenantsSchemaServerPort();
    }
}