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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.boot.JDBCDriverConfig;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implementation that returns a new or existing connection pool
 * datasource based on the tenant details stored in a {@link ThreadLocal}
 * variable for this request.
 *
 * {@link ThreadLocalContextUtil} is used to retrieve the
 * {@link FineractPlatformTenant} for the request.
 */
@Service
public class TomcatJdbcDataSourcePerTenantService implements RoutingDataSourceService {

    private final Map<Long, DataSource> tenantToDataSourceMap = new HashMap<>(1);
    private final DataSource tenantDataSource;

    @Autowired
    private JDBCDriverConfig driverConfig;

    @Autowired
    public TomcatJdbcDataSourcePerTenantService(final @Qualifier("hikariTenantDataSource") DataSource tenantDataSource) {
        this.tenantDataSource = tenantDataSource;
    }

    @Override
    public DataSource retrieveDataSource() {
        // default to tenant database datasource
        DataSource tenantDataSource = this.tenantDataSource;

        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        if (tenant != null) {
            final FineractPlatformTenantConnection tenantConnection = tenant.getConnection();

            synchronized (this.tenantToDataSourceMap) {
                // if tenantConnection information available switch to the appropriate datasource for that tenant.
                DataSource possibleDS = this.tenantToDataSourceMap.get(tenantConnection.getConnectionId());
                if (possibleDS != null) {
                    tenantDataSource = possibleDS;
                } else {
                    tenantDataSource = createNewDataSourceFor(tenantConnection);
                    this.tenantToDataSourceMap.put(tenantConnection.getConnectionId(), tenantDataSource);
                }
            }
        }

        return tenantDataSource;
    }

    // creates the tenant data source for the oltp and report database
    private DataSource createNewDataSourceFor(final FineractPlatformTenantConnection tenantConnectionObj) {
        String jdbcUrl = this.driverConfig.constructProtocol(tenantConnectionObj.getSchemaServer(), tenantConnectionObj.getSchemaServerPort(), tenantConnectionObj.getSchemaName());

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(this.driverConfig.getDriverClassName());
        config.setPoolName(tenantConnectionObj.getSchemaName() + "_pool");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(tenantConnectionObj.getSchemaUsername());
        config.setPassword(tenantConnectionObj.getSchemaPassword());
        config.setMinimumIdle(tenantConnectionObj.getInitialSize());
        config.setMaximumPoolSize(tenantConnectionObj.getMaxActive());
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(tenantConnectionObj.getValidationInterval());
        config.setAutoCommit(true);

        // https://github.com/brettwooldridge/HikariCP/wiki/MBean-(JMX)-Monitoring-and-Management
        config.setRegisterMbeans(true);

        // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        // These are the properties for each Tenant DB; the same configuration is also in src/main/resources/META-INF/spring/hikariDataSource.xml for the all Tenants DB -->
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // https://github.com/brettwooldridge/HikariCP/wiki/JDBC-Logging#mysql-connectorj
        config.addDataSourceProperty("logger", "com.mysql.jdbc.log.StandardLogger");
        config.addDataSourceProperty("logSlowQueries", "true");
        config.addDataSourceProperty("dumpQueriesOnException", "true");

        return new HikariDataSource(config);
    }
}