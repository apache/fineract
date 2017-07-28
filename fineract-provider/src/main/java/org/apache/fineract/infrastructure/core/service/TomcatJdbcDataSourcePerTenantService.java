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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.fineract.infrastructure.core.boot.JDBCDriverConfig;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implementation that returns a new or existing tomcat 7 jdbc connection pool
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
    private JDBCDriverConfig driverConfig ;
    
    @Autowired
    public TomcatJdbcDataSourcePerTenantService(final @Qualifier("tenantDataSourceJndi") DataSource tenantDataSource) {
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
                // if tenantConnection information available switch to
                // appropriate
                // datasource
                // for that tenant.
                if (this.tenantToDataSourceMap.containsKey(tenantConnection.getConnectionId())) {
                    tenantDataSource = this.tenantToDataSourceMap.get(tenantConnection.getConnectionId());
                } else {
                    tenantDataSource = createNewDataSourceFor(tenantConnection);
                    this.tenantToDataSourceMap.put(tenantConnection.getConnectionId(), tenantDataSource);
                }
            }
        }

        return tenantDataSource;
    }

    // creates the data source oltp and report databases
    private DataSource createNewDataSourceFor(final FineractPlatformTenantConnection tenantConnectionObj) {
        // see
        // http://www.tomcatexpert.com/blog/2010/04/01/configuring-jdbc-pool-high-concurrency

        // see also org.apache.fineract.DataSourceProperties.setDefaults()
    	 String jdbcUrl = this.driverConfig.constructProtocol(tenantConnectionObj.getSchemaServer(), tenantConnectionObj.getSchemaServerPort(), tenantConnectionObj.getSchemaName()) ;
        //final String jdbcUrl = tenantConnectionObj.databaseURL();
        final PoolConfiguration poolConfiguration = new PoolProperties();
        poolConfiguration.setDriverClassName(this.driverConfig.getDriverClassName());
        poolConfiguration.setName(tenantConnectionObj.getSchemaName() + "_pool");
        poolConfiguration.setUrl(jdbcUrl);
        poolConfiguration.setUsername(tenantConnectionObj.getSchemaUsername());
        poolConfiguration.setPassword(tenantConnectionObj.getSchemaPassword());

        poolConfiguration.setInitialSize(tenantConnectionObj.getInitialSize());

        poolConfiguration.setTestOnBorrow(tenantConnectionObj.isTestOnBorrow());
        poolConfiguration.setValidationQuery("SELECT 1");
        poolConfiguration.setValidationInterval(tenantConnectionObj.getValidationInterval());

        poolConfiguration.setRemoveAbandoned(tenantConnectionObj.isRemoveAbandoned());
        poolConfiguration.setRemoveAbandonedTimeout(tenantConnectionObj.getRemoveAbandonedTimeout());
        poolConfiguration.setLogAbandoned(tenantConnectionObj.isLogAbandoned());
        poolConfiguration.setAbandonWhenPercentageFull(tenantConnectionObj.getAbandonWhenPercentageFull());
        poolConfiguration.setDefaultAutoCommit(true);
        
        /**
         * Vishwas- Do we need to enable the below properties and add
         * ResetAbandonedTimer for long running batch Jobs?
         **/
        // poolConfiguration.setMaxActive(tenant.getMaxActive());
        // poolConfiguration.setMinIdle(tenant.getMinIdle());
        // poolConfiguration.setMaxIdle(tenant.getMaxIdle());

        // poolConfiguration.setSuspectTimeout(tenant.getSuspectTimeout());
        // poolConfiguration.setTimeBetweenEvictionRunsMillis(tenant.getTimeBetweenEvictionRunsMillis());
        // poolConfiguration.setMinEvictableIdleTimeMillis(tenant.getMinEvictableIdleTimeMillis());

        poolConfiguration.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport");

        return new org.apache.tomcat.jdbc.pool.DataSource(poolConfiguration);
    }
}