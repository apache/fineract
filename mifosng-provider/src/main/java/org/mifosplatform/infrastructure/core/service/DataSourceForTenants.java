/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
