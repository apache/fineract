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
package org.apache.fineract.infrastructure.core.service.migration;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.security.service.TenantDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * A service that picks up on tenants that are configured to auto-update their specific schema on application startup.
 */
@Service
public class TenantDatabaseUpgradeService implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(TenantDatabaseUpgradeService.class);
    private static final String TENANT_STORE_DB_CONTEXT = "tenant_store_db";
    private static final String INITIAL_SWITCH_CONTEXT = "initial_switch";
    private static final String TENANT_DB_CONTEXT = "tenant_db";

    private final TenantDetailsService tenantDetailsService;
    private final DataSource tenantDataSource;
    private final FineractProperties fineractProperties;
    private final TenantDatabaseStateVerifier databaseStateVerifier;
    private final ExtendedSpringLiquibaseFactory liquibaseFactory;
    private final TenantDataSourceFactory tenantDataSourceFactory;

    @Autowired
    public TenantDatabaseUpgradeService(final TenantDetailsService detailsService,
            @Qualifier("hikariTenantDataSource") final DataSource tenantDataSource, final FineractProperties fineractProperties,
            TenantDatabaseStateVerifier databaseStateVerifier, ExtendedSpringLiquibaseFactory liquibaseFactory,
            TenantDataSourceFactory tenantDataSourceFactory) {
        this.tenantDetailsService = detailsService;
        this.tenantDataSource = tenantDataSource;
        this.fineractProperties = fineractProperties;
        this.databaseStateVerifier = databaseStateVerifier;
        this.liquibaseFactory = liquibaseFactory;
        this.tenantDataSourceFactory = tenantDataSourceFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (databaseStateVerifier.isLiquibaseDisabled() || !fineractProperties.getMode().isWriteEnabled()) {
            LOG.warn("Liquibase is disabled. Not upgrading any database.");
            if (!fineractProperties.getMode().isWriteEnabled()) {
                LOG.warn("Liquibase is disabled because the current instance is configured as a non-write Fineract instance");
            }
            return;
        }
        try {
            upgradeTenantStore();
            upgradeIndividualTenants();
        } catch (LiquibaseException e) {
            throw new RuntimeException("Error while migrating the schema", e);
        }
    }

    private void upgradeTenantStore() throws LiquibaseException {
        LOG.warn("Upgrading tenant store DB at {}:{}", fineractProperties.getTenant().getHost(), fineractProperties.getTenant().getPort());
        logTenantStoreDetails();
        if (databaseStateVerifier.isFirstLiquibaseMigration(tenantDataSource)) {
            ExtendedSpringLiquibase liquibase = liquibaseFactory.create(tenantDataSource, TENANT_STORE_DB_CONTEXT, INITIAL_SWITCH_CONTEXT);
            applyInitialLiquibase(tenantDataSource, liquibase, "tenant store",
                    (ds) -> !databaseStateVerifier.isTenantStoreOnLatestUpgradableVersion(ds));
        }
        SpringLiquibase liquibase = liquibaseFactory.create(tenantDataSource, TENANT_STORE_DB_CONTEXT);
        liquibase.afterPropertiesSet();
        LOG.warn("Tenant store upgrade finished");
    }

    private void logTenantStoreDetails() {
        LOG.info("- fineract.tenant.username: {}", fineractProperties.getTenant().getUsername());
        LOG.info("- fineract.tenant.password: ****");
        LOG.info("- fineract.tenant.parameters: {}", fineractProperties.getTenant().getParameters());
        LOG.info("- fineract.tenant.timezone: {}", fineractProperties.getTenant().getTimezone());
        LOG.info("- fineract.tenant.description: {}", fineractProperties.getTenant().getDescription());
        LOG.info("- fineract.tenant.identifier: {}", fineractProperties.getTenant().getIdentifier());
        LOG.info("- fineract.tenant.name: {}", fineractProperties.getTenant().getName());
    }

    private void upgradeIndividualTenants() throws LiquibaseException {
        LOG.warn("Upgrading all tenants");
        List<FineractPlatformTenant> tenants = tenantDetailsService.findAllTenants();
        if (isNotEmpty(tenants)) {
            for (FineractPlatformTenant tenant : tenants) {
                upgradeIndividualTenant(tenant);
            }
        }
        LOG.warn("Tenant upgrades have finished");
    }

    private void upgradeIndividualTenant(FineractPlatformTenant tenant) throws LiquibaseException {
        LOG.info("Upgrade for tenant {} has started", tenant.getTenantIdentifier());
        DataSource tenantDataSource = tenantDataSourceFactory.create(tenant);
        if (databaseStateVerifier.isFirstLiquibaseMigration(tenantDataSource)) {
            ExtendedSpringLiquibase liquibase = liquibaseFactory.create(tenantDataSource, TENANT_DB_CONTEXT, INITIAL_SWITCH_CONTEXT);
            applyInitialLiquibase(tenantDataSource, liquibase, tenant.getTenantIdentifier(),
                    (ds) -> !databaseStateVerifier.isTenantOnLatestUpgradableVersion(ds));
        }
        SpringLiquibase tenantLiquibase = liquibaseFactory.create(tenantDataSource, TENANT_DB_CONTEXT);
        tenantLiquibase.afterPropertiesSet();
        LOG.info("Upgrade for tenant {} has finished", tenant.getTenantIdentifier());
    }

    private void applyInitialLiquibase(DataSource dataSource, ExtendedSpringLiquibase liquibase, String id,
            Function<DataSource, Boolean> isUpgradableFn) throws LiquibaseException {
        if (databaseStateVerifier.isFlywayPresent(dataSource)) {
            if (isUpgradableFn.apply(dataSource)) {
                LOG.warn("Cannot proceed with upgrading database {}", id);
                LOG.warn("It seems the database doesn't have the latest schema changes applied until the 1.6 release");
                throw new SchemaUpgradeNeededException("Make sure to upgrade to Fineract 1.6 first and then to a newer version");
            }
            LOG.warn("This is the first Liquibase migration for {}. We'll sync the changelog for you and then apply everything else", id);
            liquibase.changeLogSync();
            LOG.warn("Liquibase changelog sync is complete");
        } else {
            liquibase.afterPropertiesSet();
        }
    }
}
