package org.mifosplatform.infrastructure.core.service;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implementation that returns connection pool datasource for tenants database
 */
@Service
public class DataSourceForTenants implements RoutingDataSourceService {

    private final DataSource tenantDataSource;

    @Autowired
    public DataSourceForTenants(final @Qualifier("tenantDataSourceJndi") DataSource tenantDataSource) {
        this.tenantDataSource = tenantDataSource;
    }

    @Override
    public DataSource retrieveDataSource() {
        return this.tenantDataSource;
    }

}
