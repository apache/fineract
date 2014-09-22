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
            synchronized (this.tenantToDataSourceMap) {
                // if tenant information available switch to appropriate
                // datasource
                // for that tenant.
                if (this.tenantToDataSourceMap.containsKey(tenant.getId())) {
                    tenantDataSource = this.tenantToDataSourceMap.get(tenant.getId());
                } else {
                    tenantDataSource = createNewDataSourceFor(tenant);
                    this.tenantToDataSourceMap.put(tenant.getId(), tenantDataSource);
                }
            }
        }

        return tenantDataSource;
    }

    private DataSource createNewDataSourceFor(final MifosPlatformTenant tenant) {
        // see
        // http://www.tomcatexpert.com/blog/2010/04/01/configuring-jdbc-pool-high-concurrency

	// see also org.mifosplatform.DataSourceProperties.setMifosDefaults()

        final String jdbcUrl = tenant.databaseURL();

        final PoolConfiguration poolConfiguration = new PoolProperties();
        poolConfiguration.setDriverClassName("com.mysql.jdbc.Driver");
        poolConfiguration.setName(tenant.getSchemaName() + "_pool");
        poolConfiguration.setUrl(jdbcUrl);
        poolConfiguration.setUsername(tenant.getSchemaUsername());
        poolConfiguration.setPassword(tenant.getSchemaPassword());

        poolConfiguration.setInitialSize(tenant.getInitialSize());

        poolConfiguration.setTestOnBorrow(tenant.isTestOnBorrow());
        poolConfiguration.setValidationQuery("SELECT 1");
        poolConfiguration.setValidationInterval(tenant.getValidationInterval());

        poolConfiguration.setRemoveAbandoned(tenant.isRemoveAbandoned());
        poolConfiguration.setRemoveAbandonedTimeout(tenant.getRemoveAbandonedTimeout());
        poolConfiguration.setLogAbandoned(tenant.isLogAbandoned());
        poolConfiguration.setAbandonWhenPercentageFull(tenant.getAbandonWhenPercentageFull());

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