/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenantConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implementation that returns a new or existing tomcat 7 jdbc connection pool
 * datasource based on the tenant details stored in a {@link ThreadLocal}
 * variable for this request.
 * 
 * {@link ThreadLocalContextUtil} is used to retrieve the
 * {@link MifosPlatformTenant} for the request.
 */
@Service
public class TomcatJdbcDataSourcePerTenantService implements RoutingDataSourceService {

    private final Map<Long, DataSource> tenantToDataSourceMap = new HashMap<>(1);
    private final DataSource tenantDataSource;

    @Autowired
    public TomcatJdbcDataSourcePerTenantService(final @Qualifier("tenantDataSourceJndi") DataSource tenantDataSource) {
        this.tenantDataSource = tenantDataSource;
    }

    @Override
    public DataSource retrieveDataSource() {

        // default to tenant database datasource
        DataSource tenantDataSource = this.tenantDataSource;

        final MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant(); 
        if (tenant != null) {
            final MifosPlatformTenantConnection tenantConnection = tenant.getConnection();

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
    private DataSource createNewDataSourceFor(final MifosPlatformTenantConnection tenantConnectionObj) {
        // see
        // http://www.tomcatexpert.com/blog/2010/04/01/configuring-jdbc-pool-high-concurrency

        // see also org.mifosplatform.DataSourceProperties.setMifosDefaults()

        final String jdbcUrl = tenantConnectionObj.databaseURL();
        final PoolConfiguration poolConfiguration = new PoolProperties();
        poolConfiguration.setDriverClassName("com.mysql.jdbc.Driver");
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