/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.security.service.TenantDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.googlecode.flyway.core.Flyway;

/**
 * A JDBC implementation of {@link TenantDetailsService} for loading a tenants
 * details by a <code>tenantIdentifier</code>.
 */
@Service
public class TenantUpgradeService implements ITenantUpgradeService {

    private final TenantDetailsService tenantDetailsService;

    @Autowired
    public TenantUpgradeService(TenantDetailsService detailsService) {
        this.tenantDetailsService = detailsService;
    }

    @Override
    @PostConstruct
    public void upgradeAllTenants() {
        List<MifosPlatformTenant> tenants = tenantDetailsService.findAllTenants();
        for (MifosPlatformTenant tenant : tenants) {
            if (tenant.isAutoUpdateEnabled()) {
                Flyway flyway = new Flyway();
                flyway.setDataSource(tenant.databaseURL(), tenant.getSchemaUsername(), tenant.getSchemaPassword());
                flyway.setLocations("sql");
                flyway.migrate();
            }
        }
    }
}