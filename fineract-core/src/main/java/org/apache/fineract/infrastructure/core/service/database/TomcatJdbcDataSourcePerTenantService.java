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
package org.apache.fineract.infrastructure.core.service.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * Implementation that returns a new or existing connection pool datasource based on the tenant details stored in a
 * {@link ThreadLocal} variable for this request.
 *
 * {@link ThreadLocalContextUtil} is used to retrieve the {@link FineractPlatformTenant} for the request.
 */
@Slf4j
@Service
public class TomcatJdbcDataSourcePerTenantService implements RoutingDataSourceService, ApplicationListener<ContextRefreshedEvent> {

    private static final Map<Long, DataSource> TENANT_TO_DATA_SOURCE_MAP = new ConcurrentHashMap<>();
    private final DataSource tenantDataSource;
    private final TenantDetailsService tenantDetailsService;

    private final DataSourcePerTenantServiceFactory dataSourcePerTenantServiceFactory;

    @Autowired
    public TomcatJdbcDataSourcePerTenantService(final @Qualifier("hikariTenantDataSource") DataSource tenantDataSource,
            final DataSourcePerTenantServiceFactory dataSourcePerTenantServiceFactory, final TenantDetailsService tenantDetailsService) {
        this.tenantDataSource = tenantDataSource;
        this.dataSourcePerTenantServiceFactory = dataSourcePerTenantServiceFactory;
        this.tenantDetailsService = tenantDetailsService;
    }

    @Override
    public DataSource retrieveDataSource() {
        // default to tenant database datasource
        DataSource actualDataSource = this.tenantDataSource;

        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        if (tenant != null) {
            final FineractPlatformTenantConnection tenantConnection = tenant.getConnection();
            Long tenantConnectionKey = tenantConnection.getConnectionId();
            // if tenantConnection information available switch to the
            // appropriate datasource for that tenant.
            actualDataSource = TENANT_TO_DATA_SOURCE_MAP.computeIfAbsent(tenantConnectionKey,
                    (key) -> dataSourcePerTenantServiceFactory.createNewDataSourceFor(tenantConnection));

        }

        return actualDataSource;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final List<FineractPlatformTenant> allTenants = tenantDetailsService.findAllTenants();
        for (final FineractPlatformTenant tenant : allTenants) {
            initializeDataSourceConnection(tenant);
        }
    }

    private void initializeDataSourceConnection(FineractPlatformTenant tenant) {
        log.debug("Initializing database connection for {}", tenant.getName());
        final FineractPlatformTenantConnection tenantConnection = tenant.getConnection();
        Long tenantConnectionKey = tenantConnection.getConnectionId();
        TENANT_TO_DATA_SOURCE_MAP.computeIfAbsent(tenantConnectionKey, (key) -> {
            DataSource tenantSpecificDataSource = dataSourcePerTenantServiceFactory.createNewDataSourceFor(tenantConnection);
            try (Connection connection = tenantSpecificDataSource.getConnection()) {
                String url = connection.getMetaData().getURL();
                log.debug("Established database connection with URL {}", url);
            } catch (SQLException e) {
                log.error("Error while initializing database connection for {}", tenant.getName(), e);
            }
            return tenantSpecificDataSource;
        });
        log.debug("Database connection for {} initialized", tenant.getName());

    }
}
