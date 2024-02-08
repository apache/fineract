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
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;
import liquibase.change.custom.CustomTaskChange;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * A service that picks up on tenants that are configured to auto-update their specific schema on application startup.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantDatabaseUpgradeService implements InitializingBean {

    private static final String TENANT_STORE_DB_CONTEXT = "tenant_store_db";
    private static final String INITIAL_SWITCH_CONTEXT = "initial_switch";
    private static final String TENANT_DB_CONTEXT = "tenant_db";
    private static final String CUSTOM_CHANGELOG_CONTEXT = "custom_changelog";

    private final TenantDetailsService tenantDetailsService;
    @Qualifier("hikariTenantDataSource")
    private final DataSource tenantDataSource;
    private final FineractProperties fineractProperties;
    private final TenantDatabaseStateVerifier databaseStateVerifier;
    private final ExtendedSpringLiquibaseFactory liquibaseFactory;
    private final TenantDataSourceFactory tenantDataSourceFactory;
    private final Environment environment;

    // DO NOT REMOVE! Required for liquibase custom task initialization
    private final List<CustomTaskChange> customTaskChangesForDependencyInjection;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (notLiquibaseOnlyMode()) {
            if (databaseStateVerifier.isLiquibaseDisabled() || !fineractProperties.getMode().isWriteEnabled()) {
                log.warn("Liquibase is disabled. Not upgrading any database.");
                if (!fineractProperties.getMode().isWriteEnabled()) {
                    log.warn("Liquibase is disabled because the current instance is configured as a non-write Fineract instance");
                }
                return;
            }
        }
        try {
            upgradeTenantStore();
            upgradeIndividualTenants();
        } catch (LiquibaseException e) {
            throw new RuntimeException("Error while migrating the schema", e);
        }
    }

    private boolean notLiquibaseOnlyMode() {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        return !activeProfiles.contains(FineractProfiles.LIQUIBASE_ONLY);
    }

    private void upgradeTenantStore() throws LiquibaseException {
        log.info("Upgrading tenant store DB at {}:{}", fineractProperties.getTenant().getHost(), fineractProperties.getTenant().getPort());
        logTenantStoreDetails();
        if (databaseStateVerifier.isFirstLiquibaseMigration(tenantDataSource)) {
            ExtendedSpringLiquibase liquibase = liquibaseFactory.create(tenantDataSource, TENANT_STORE_DB_CONTEXT, INITIAL_SWITCH_CONTEXT);
            applyInitialLiquibase(tenantDataSource, liquibase, "tenant store",
                    (ds) -> !databaseStateVerifier.isTenantStoreOnLatestUpgradableVersion(ds));
        }
        SpringLiquibase liquibase = liquibaseFactory.create(tenantDataSource, TENANT_STORE_DB_CONTEXT);
        liquibase.afterPropertiesSet();
        log.info("Tenant store upgrade finished");
    }

    private void logTenantStoreDetails() {
        FineractProperties.FineractTenantProperties tenant = fineractProperties.getTenant();
        log.info("- fineract.tenant.username: {}", tenant.getUsername());
        log.info("- fineract.tenant.password: ****");
        log.info("- fineract.tenant.parameters: {}", tenant.getParameters());
        log.info("- fineract.tenant.timezone: {}", tenant.getTimezone());
        log.info("- fineract.tenant.description: {}", tenant.getDescription());
        log.info("- fineract.tenant.identifier: {}", tenant.getIdentifier());
        log.info("- fineract.tenant.name: {}", tenant.getName());

        String readOnlyUsername = tenant.getReadOnlyUsername();
        if (isNotBlank(readOnlyUsername)) {
            log.info("- fineract.tenant.readonly.username: {}", readOnlyUsername);
            log.info("- fineract.tenant.readonly.password: {}", isNotBlank(tenant.getReadOnlyPassword()) ? "****" : "");
            log.info("- fineract.tenant.readonly.parameters: {}", tenant.getReadOnlyParameters());
            log.info("- fineract.tenant.readonly.name: {}", tenant.getReadOnlyName());
        }

    }

    private void upgradeIndividualTenants() throws LiquibaseException {
        log.info("Upgrading all tenants");
        List<FineractPlatformTenant> tenants = tenantDetailsService.findAllTenants();
        if (isNotEmpty(tenants)) {
            for (FineractPlatformTenant tenant : tenants) {
                upgradeIndividualTenant(tenant);
            }
        }
        log.info("Tenant upgrades have finished");
    }

    /**
     * Upgrade each tenant's database
     *
     * Good to know: Each tenant's identifier is provided as a context variable to avoid caching of the liquibase
     * migration (it was introduced as part of v4.21.0)
     *
     * @param tenant
     * @throws LiquibaseException
     */
    private void upgradeIndividualTenant(FineractPlatformTenant tenant) throws LiquibaseException {
        log.info("Upgrade for tenant {} has started", tenant.getTenantIdentifier());
        DataSource tenantDataSource = tenantDataSourceFactory.create(tenant);
        if (databaseStateVerifier.isFirstLiquibaseMigration(tenantDataSource)) {
            ExtendedSpringLiquibase liquibase = liquibaseFactory.create(tenantDataSource, TENANT_DB_CONTEXT, INITIAL_SWITCH_CONTEXT,
                    tenant.getTenantIdentifier());
            applyInitialLiquibase(tenantDataSource, liquibase, tenant.getTenantIdentifier(),
                    (ds) -> !databaseStateVerifier.isTenantOnLatestUpgradableVersion(ds));
        }
        SpringLiquibase tenantLiquibase = liquibaseFactory.create(tenantDataSource, TENANT_DB_CONTEXT, tenant.getTenantIdentifier());
        tenantLiquibase.afterPropertiesSet();
        SpringLiquibase customChangelogLiquibase = liquibaseFactory.create(tenantDataSource, TENANT_DB_CONTEXT, CUSTOM_CHANGELOG_CONTEXT,
                tenant.getTenantIdentifier());
        customChangelogLiquibase.afterPropertiesSet();
        log.info("Upgrade for tenant {} has finished", tenant.getTenantIdentifier());
    }

    private void applyInitialLiquibase(DataSource dataSource, ExtendedSpringLiquibase liquibase, String id,
            Function<DataSource, Boolean> isUpgradableFn) throws LiquibaseException {
        if (databaseStateVerifier.isFlywayPresent(dataSource)) {
            if (isUpgradableFn.apply(dataSource)) {
                log.error("Cannot proceed with upgrading database {}", id);
                log.error("It seems the database doesn't have the latest schema changes applied until the 1.6 release");
                throw new SchemaUpgradeNeededException("Make sure to upgrade to Fineract 1.6 first and then to a newer version");
            }
            log.info("This is the first Liquibase migration for {}. We'll sync the changelog for you and then apply everything else", id);
            liquibase.changeLogSync();
            log.info("Liquibase changelog sync is complete");
        } else {
            liquibase.afterPropertiesSet();
        }
    }
}
