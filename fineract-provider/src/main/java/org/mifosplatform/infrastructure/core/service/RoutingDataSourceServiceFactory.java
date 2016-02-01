/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Factory class to get data source service based on the details stored in
 * {@link ThreadLocal} variable for this request
 * 
 * {@link ThreadLocalContextUtil} is used to retrieve the Context
 * 
 */
@Component
public class RoutingDataSourceServiceFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public RoutingDataSourceService determineDataSourceService() {
        String serviceName = "tomcatJdbcDataSourcePerTenantService";
        if (ThreadLocalContextUtil.CONTEXT_TENANTS.equalsIgnoreCase(ThreadLocalContextUtil.getDataSourceContext())) {
            serviceName = "dataSourceForTenants";
        }
        return this.applicationContext.getBean(serviceName, RoutingDataSourceService.class);

    }
}
