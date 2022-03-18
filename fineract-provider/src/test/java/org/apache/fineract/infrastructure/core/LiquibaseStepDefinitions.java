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
package org.apache.fineract.infrastructure.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.cucumber.java8.En;
import java.util.List;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.migration.ExtendedSpringLiquibase;
import org.apache.fineract.infrastructure.core.service.migration.ExtendedSpringLiquibaseFactory;
import org.apache.fineract.infrastructure.core.service.migration.SchemaUpgradeNeededException;
import org.apache.fineract.infrastructure.core.service.migration.TenantDataSourceFactory;
import org.apache.fineract.infrastructure.core.service.migration.TenantDatabaseStateVerifier;
import org.apache.fineract.infrastructure.core.service.migration.TenantDatabaseUpgradeService;
import org.apache.fineract.infrastructure.security.service.TenantDetailsService;

public class LiquibaseStepDefinitions implements En {

    private TenantDataSourceFactory tenantDataSourceFactory;
    private TenantDetailsService tenantDetailsService;
    private TenantDatabaseStateVerifier databaseStateVerifier;
    private FineractProperties fineractProperties;
    private ExtendedSpringLiquibaseFactory liquibaseFactory;
    private ExtendedSpringLiquibase initialTenantStoreLiquibase;
    private ExtendedSpringLiquibase tenantStoreLiquibase;
    private ExtendedSpringLiquibase initialTenantLiquibase;
    private ExtendedSpringLiquibase tenantLiquibase;
    private FineractPlatformTenant defaultTenant;
    private DataSource tenantStoreDataSource;
    private TenantDatabaseUpgradeService tenantDatabaseUpgradeService;
    private List<FineractPlatformTenant> allTenants;
    private SchemaUpgradeNeededException executionException;
    private DataSource defaultTenantDataSource;

    public LiquibaseStepDefinitions() {
        Given("Liquibase is disabled with a default tenant", () -> {
            initializeLiquibase(false);
        });
        Given("Liquibase is enabled with a default tenant", () -> {
            initializeLiquibase(true);
        });
        Given("Liquibase runs the very first time for the tenant store", () -> {
            given(databaseStateVerifier.isFirstLiquibaseMigration(tenantStoreDataSource)).willReturn(true);
        });
        Given("A previously Flyway migrated tenant store database on the latest version", () -> {
            given(databaseStateVerifier.isFlywayPresent(tenantStoreDataSource)).willReturn(true);
            given(databaseStateVerifier.isTenantStoreOnLatestUpgradableVersion(tenantStoreDataSource)).willReturn(true);
        });
        Given("A previously Flyway migrated tenant store database on an earlier version", () -> {
            given(databaseStateVerifier.isFlywayPresent(tenantStoreDataSource)).willReturn(true);
            given(databaseStateVerifier.isTenantStoreOnLatestUpgradableVersion(tenantStoreDataSource)).willReturn(false);
        });
        Given("Liquibase runs the very first time for the default tenant", () -> {
            given(databaseStateVerifier.isFirstLiquibaseMigration(defaultTenantDataSource)).willReturn(true);
        });
        Given("A previously Flyway migrated default tenant database on the latest version", () -> {
            given(databaseStateVerifier.isFlywayPresent(defaultTenantDataSource)).willReturn(true);
            given(databaseStateVerifier.isTenantOnLatestUpgradableVersion(defaultTenantDataSource)).willReturn(true);
        });
        Given("A previously Flyway migrated default tenant database on an earlier version", () -> {
            given(databaseStateVerifier.isFlywayPresent(defaultTenantDataSource)).willReturn(true);
            given(databaseStateVerifier.isTenantOnLatestUpgradableVersion(defaultTenantDataSource)).willReturn(false);
        });

        When("The database migration process is executed", () -> {
            try {
                tenantDatabaseUpgradeService.afterPropertiesSet();
            } catch (SchemaUpgradeNeededException e) {
                executionException = e;
            }
        });

        Then("The database migration did not do anything", () -> {
            verify(databaseStateVerifier).isLiquibaseDisabled();
            verifyNoMoreInteractions(databaseStateVerifier);
            verifyNoInteractions(tenantDetailsService, tenantStoreDataSource, fineractProperties, liquibaseFactory,
                    tenantDataSourceFactory);
        });

        Then("The tenant store upgrade fails with a schema upgrade needed", () -> {
            assertThat(executionException).isNotNull();
            verify(liquibaseFactory).create(eq(tenantStoreDataSource), any());
            verifyNoMoreInteractions(liquibaseFactory);
            verifyNoInteractions(initialTenantStoreLiquibase, tenantStoreLiquibase, initialTenantLiquibase, tenantLiquibase);
        });

        Then("The default tenant upgrade fails with a schema upgrade needed", () -> {
            assertThat(executionException).isNotNull();
            verify(liquibaseFactory, times(2)).create(eq(tenantStoreDataSource), any());
            verify(liquibaseFactory).create(eq(defaultTenantDataSource), any());
            verifyNoMoreInteractions(liquibaseFactory);
            verify(initialTenantStoreLiquibase).changeLogSync();
            verify(tenantStoreLiquibase).afterPropertiesSet();
            verifyNoInteractions(initialTenantLiquibase, tenantLiquibase);
        });

        Then("The tenant store and the default tenant gets upgraded from scratch", () -> {
            verify(initialTenantStoreLiquibase).afterPropertiesSet();
            verify(tenantStoreLiquibase).afterPropertiesSet();
            verify(initialTenantLiquibase).afterPropertiesSet();
            verify(tenantLiquibase).afterPropertiesSet();
        });

        Then("The tenant store and the default tenant gets synced and then upgraded", () -> {
            verify(initialTenantStoreLiquibase).changeLogSync();
            verify(tenantStoreLiquibase).afterPropertiesSet();
            verify(initialTenantLiquibase).changeLogSync();
            verify(tenantLiquibase).afterPropertiesSet();
        });
    }

    private void initializeLiquibase(boolean liquibaseEnabled) {
        tenantDataSourceFactory = mock(TenantDataSourceFactory.class);
        tenantDetailsService = mock(TenantDetailsService.class);
        databaseStateVerifier = mock(TenantDatabaseStateVerifier.class);
        fineractProperties = mock(FineractProperties.class);

        liquibaseFactory = mock(ExtendedSpringLiquibaseFactory.class);

        defaultTenant = mock(FineractPlatformTenant.class);

        allTenants = List.of(defaultTenant);

        tenantStoreDataSource = mock(DataSource.class);

        initialTenantLiquibase = mock(ExtendedSpringLiquibase.class);
        tenantLiquibase = mock(ExtendedSpringLiquibase.class);
        initialTenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);
        tenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);

        defaultTenantDataSource = mock(DataSource.class);

        given(databaseStateVerifier.isLiquibaseDisabled()).willReturn(!liquibaseEnabled);
        given(fineractProperties.getTenant()).willReturn(new FineractProperties.FineractTenantProperties());
        given(liquibaseFactory.create(tenantStoreDataSource, "tenant_store_db", "initial_switch")).willReturn(initialTenantStoreLiquibase);
        given(liquibaseFactory.create(tenantStoreDataSource, "tenant_store_db")).willReturn(tenantStoreLiquibase);

        given(tenantDetailsService.findAllTenants()).willReturn(allTenants);
        given(tenantDataSourceFactory.create(defaultTenant)).willReturn(defaultTenantDataSource);
        given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db", "initial_switch")).willReturn(initialTenantLiquibase);
        given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db")).willReturn(tenantLiquibase);

        tenantDatabaseUpgradeService = new TenantDatabaseUpgradeService(tenantDetailsService, tenantStoreDataSource, fineractProperties,
                databaseStateVerifier, liquibaseFactory, tenantDataSourceFactory);
    }
}
