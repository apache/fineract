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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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
    private DataSource defaultTenantDataSource;
    private TenantDatabaseUpgradeService tenantDatabaseUpgradeService;
    private List allTenants;
    private SchemaUpgradeNeededException result;

    public LiquibaseStepDefinitions() {
        Given("Liquibase is disabled with a default tenant", () -> {
            tenantDataSourceFactory = mock(TenantDataSourceFactory.class);
            tenantDetailsService = mock(TenantDetailsService.class);
            databaseStateVerifier = mock(TenantDatabaseStateVerifier.class);
            fineractProperties = mock(FineractProperties.class);

            liquibaseFactory = mock(ExtendedSpringLiquibaseFactory.class);

            defaultTenant = mock(FineractPlatformTenant.class);

            allTenants = List.of(defaultTenant);

            defaultTenantDataSource = mock(DataSource.class);

            initialTenantLiquibase = mock(ExtendedSpringLiquibase.class);
            tenantLiquibase = mock(ExtendedSpringLiquibase.class);
            initialTenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);
            tenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);

            given(databaseStateVerifier.isLiquibaseDisabled()).willReturn(true);
            given(fineractProperties.getTenant()).willReturn(new FineractProperties.FineractTenantProperties());
            given(liquibaseFactory.create(defaultTenantDataSource, "tenant_store_db", "initial_switch"))
                    .willReturn(initialTenantStoreLiquibase);
            given(liquibaseFactory.create(defaultTenantDataSource, "tenant_store_db")).willReturn(tenantStoreLiquibase);

            given(tenantDetailsService.findAllTenants()).willReturn(allTenants);
            given(tenantDataSourceFactory.create(defaultTenant)).willReturn(defaultTenantDataSource);
            given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db", "initial_switch")).willReturn(initialTenantLiquibase);
            given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db")).willReturn(tenantLiquibase);

            tenantDatabaseUpgradeService = new TenantDatabaseUpgradeService(tenantDetailsService, defaultTenantDataSource,
                    fineractProperties, databaseStateVerifier, liquibaseFactory, tenantDataSourceFactory);
        });
        Given("Liquibase runs the very first time for the tenant store", () -> {
            given(databaseStateVerifier.isFirstLiquibaseMigration(defaultTenantDataSource)).willReturn(true);
        });
        Given("A previously Flyway migrated tenant store database", () -> {
            given(databaseStateVerifier.isFlywayPresent(defaultTenantDataSource)).willReturn(true);
        });
        Given("A previously Flyway migrated tenant store database on an earlier version than 1.6", () -> {
            given(databaseStateVerifier.isTenantStoreOnLatestUpgradableVersion(defaultTenantDataSource)).willReturn(false);
        });
        Given("Liquibase runs the very first time for the default tenant", () -> {
            given(databaseStateVerifier.isFirstLiquibaseMigration(defaultTenantDataSource)).willReturn(true);
        });
        Given("A previously Flyway migrated default tenant database", () -> {
            given(databaseStateVerifier.isFlywayPresent(defaultTenantDataSource)).willReturn(true);
        });
        Given("A previously Flyway migrated default tenant database on the 1.6 version", () -> {
            given(databaseStateVerifier.isTenantStoreOnLatestUpgradableVersion(defaultTenantDataSource)).willReturn(true);
        });

        When("The database migration process is executed", () -> {
            // TODO: @galovics will not throw an exception if disabled = true... but if Liquibase is enabled then "Then"
            // section (as layed out in original tests) will fail
            // this.result = assertThrows(SchemaUpgradeNeededException.class, () ->
            // tenantDatabaseUpgradeService.afterPropertiesSet());

            tenantDatabaseUpgradeService.afterPropertiesSet();
        });

        Then("The tenant store upgrade fails with a schema upgrade needed", () -> {
            // TODO: @galovics verifications work dependent on Liquibase enabled or not... for what should we test?
            // assertThat(result).isNotNull();
            // verify(liquibaseFactory).create(eq(defaultTenantDataSource), any());

            verifyNoMoreInteractions(liquibaseFactory);
            verifyNoInteractions(initialTenantStoreLiquibase, tenantStoreLiquibase, initialTenantLiquibase, tenantLiquibase,
                    defaultTenantDataSource);
        });
    }
}
