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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.security.service.TenantDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TenantDatabaseUpgradeServiceTest {

    @Mock
    private TenantDetailsService tenantDetailsService;
    @Mock
    private HikariDataSource tenantDataSource;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private TenantDatabaseStateVerifier databaseStateVerifier;
    @Mock
    private ExtendedSpringLiquibaseFactory liquibaseFactory;
    @Mock
    private TenantDataSourceFactory tenantDataSourceFactory;

    private TenantDatabaseUpgradeService underTest;

    @BeforeEach
    public void setUp() {
        underTest = new TenantDatabaseUpgradeService(tenantDetailsService, tenantDataSource, fineractProperties, databaseStateVerifier,
                liquibaseFactory, tenantDataSourceFactory);
    }

    @Test
    public void testAfterPropertiesSetDoesNotDoAnythingWhenLiquibaseIsDisabled() throws Exception {
        // given
        given(databaseStateVerifier.isLiquibaseDisabled()).willReturn(true);
        // when
        underTest.afterPropertiesSet();
        // then
        verify(databaseStateVerifier).isLiquibaseDisabled();
        verifyNoMoreInteractions(databaseStateVerifier);
        verifyNoInteractions(tenantDetailsService, tenantDataSource, fineractProperties, liquibaseFactory, tenantDataSourceFactory);
    }

    @Test
    public void testAfterPropertiesSetGetsLiquibaseAppliedWhenStartingFromScratch() throws Exception {
        // given
        ExtendedSpringLiquibase initialTenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);
        ExtendedSpringLiquibase tenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);
        given(databaseStateVerifier.isLiquibaseDisabled()).willReturn(false);
        given(databaseStateVerifier.isFirstLiquibaseMigration(tenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isFlywayPresent(tenantDataSource)).willReturn(false);
        given(fineractProperties.getTenant()).willReturn(new FineractProperties.FineractTenantProperties());
        given(liquibaseFactory.create(tenantDataSource, "tenant_store_db", "initial_switch")).willReturn(initialTenantStoreLiquibase);
        given(liquibaseFactory.create(tenantDataSource, "tenant_store_db")).willReturn(tenantStoreLiquibase);

        ExtendedSpringLiquibase initialTenantLiquibase = mock(ExtendedSpringLiquibase.class);
        ExtendedSpringLiquibase tenantLiquibase = mock(ExtendedSpringLiquibase.class);
        FineractPlatformTenant defaultTenant = mock(FineractPlatformTenant.class);
        List<FineractPlatformTenant> allTenants = List.of(defaultTenant);
        DataSource defaultTenantDataSource = mock(DataSource.class);
        given(databaseStateVerifier.isFirstLiquibaseMigration(defaultTenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isFlywayPresent(defaultTenantDataSource)).willReturn(false);
        given(tenantDetailsService.findAllTenants()).willReturn(allTenants);
        given(tenantDataSourceFactory.create(defaultTenant)).willReturn(defaultTenantDataSource);
        given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db", "initial_switch")).willReturn(initialTenantLiquibase);
        given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db")).willReturn(tenantLiquibase);
        // when
        underTest.afterPropertiesSet();
        // then
        verify(initialTenantStoreLiquibase).afterPropertiesSet();
        verify(tenantStoreLiquibase).afterPropertiesSet();
        verify(initialTenantLiquibase).afterPropertiesSet();
        verify(tenantLiquibase).afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetSyncsTheInitialChangeLogAndAppliesRemainingChangesWhenFlywayIsOnLatestVersion() throws Exception {
        // given
        ExtendedSpringLiquibase initialTenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);
        ExtendedSpringLiquibase tenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);
        given(databaseStateVerifier.isLiquibaseDisabled()).willReturn(false);
        given(databaseStateVerifier.isFirstLiquibaseMigration(tenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isFlywayPresent(tenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isTenantStoreOnLatestUpgradableVersion(tenantDataSource)).willReturn(true);
        given(fineractProperties.getTenant()).willReturn(new FineractProperties.FineractTenantProperties());
        given(liquibaseFactory.create(tenantDataSource, "tenant_store_db", "initial_switch")).willReturn(initialTenantStoreLiquibase);
        given(liquibaseFactory.create(tenantDataSource, "tenant_store_db")).willReturn(tenantStoreLiquibase);

        ExtendedSpringLiquibase initialTenantLiquibase = mock(ExtendedSpringLiquibase.class);
        ExtendedSpringLiquibase tenantLiquibase = mock(ExtendedSpringLiquibase.class);
        FineractPlatformTenant defaultTenant = mock(FineractPlatformTenant.class);
        List<FineractPlatformTenant> allTenants = List.of(defaultTenant);
        DataSource defaultTenantDataSource = mock(DataSource.class);
        given(databaseStateVerifier.isFirstLiquibaseMigration(defaultTenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isFlywayPresent(defaultTenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isTenantOnLatestUpgradableVersion(defaultTenantDataSource)).willReturn(true);
        given(tenantDetailsService.findAllTenants()).willReturn(allTenants);
        given(tenantDataSourceFactory.create(defaultTenant)).willReturn(defaultTenantDataSource);
        given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db", "initial_switch")).willReturn(initialTenantLiquibase);
        given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db")).willReturn(tenantLiquibase);
        // when
        underTest.afterPropertiesSet();
        // then
        verify(initialTenantStoreLiquibase).changeLogSync();
        verify(tenantStoreLiquibase).afterPropertiesSet();
        verify(initialTenantLiquibase).changeLogSync();
        verify(tenantLiquibase).afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesSetFailsWhenTenantStoreFlywayIsNotOnLatestVersion() throws Exception {
        // given
        ExtendedSpringLiquibase initialTenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);
        ExtendedSpringLiquibase tenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);
        given(databaseStateVerifier.isLiquibaseDisabled()).willReturn(false);
        given(databaseStateVerifier.isFirstLiquibaseMigration(tenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isFlywayPresent(tenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isTenantStoreOnLatestUpgradableVersion(tenantDataSource)).willReturn(false);
        given(fineractProperties.getTenant()).willReturn(new FineractProperties.FineractTenantProperties());
        given(liquibaseFactory.create(tenantDataSource, "tenant_store_db", "initial_switch")).willReturn(initialTenantStoreLiquibase);
        given(liquibaseFactory.create(tenantDataSource, "tenant_store_db")).willReturn(tenantStoreLiquibase);

        ExtendedSpringLiquibase initialTenantLiquibase = mock(ExtendedSpringLiquibase.class);
        ExtendedSpringLiquibase tenantLiquibase = mock(ExtendedSpringLiquibase.class);
        FineractPlatformTenant defaultTenant = mock(FineractPlatformTenant.class);
        List<FineractPlatformTenant> allTenants = List.of(defaultTenant);
        DataSource defaultTenantDataSource = mock(DataSource.class);
        given(databaseStateVerifier.isFirstLiquibaseMigration(defaultTenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isFlywayPresent(defaultTenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isTenantOnLatestUpgradableVersion(defaultTenantDataSource)).willReturn(true);
        given(tenantDetailsService.findAllTenants()).willReturn(allTenants);
        given(tenantDataSourceFactory.create(defaultTenant)).willReturn(defaultTenantDataSource);
        given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db", "initial_switch")).willReturn(initialTenantLiquibase);
        given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db")).willReturn(tenantLiquibase);
        // when
        SchemaUpgradeNeededException result = assertThrows(SchemaUpgradeNeededException.class, () -> underTest.afterPropertiesSet());
        // then
        assertThat(result).isNotNull();
        verify(liquibaseFactory).create(eq(tenantDataSource), any());
        verifyNoMoreInteractions(liquibaseFactory);
        verifyNoInteractions(initialTenantStoreLiquibase, tenantStoreLiquibase, initialTenantLiquibase, tenantLiquibase);
    }

    @Test
    public void testAfterPropertiesSetFailsWhenTenantFlywayIsNotOnLatestVersion() throws Exception {
        // given
        ExtendedSpringLiquibase initialTenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);
        ExtendedSpringLiquibase tenantStoreLiquibase = mock(ExtendedSpringLiquibase.class);
        given(databaseStateVerifier.isLiquibaseDisabled()).willReturn(false);
        given(databaseStateVerifier.isFirstLiquibaseMigration(tenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isFlywayPresent(tenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isTenantStoreOnLatestUpgradableVersion(tenantDataSource)).willReturn(true);
        given(fineractProperties.getTenant()).willReturn(new FineractProperties.FineractTenantProperties());
        given(liquibaseFactory.create(tenantDataSource, "tenant_store_db", "initial_switch")).willReturn(initialTenantStoreLiquibase);
        given(liquibaseFactory.create(tenantDataSource, "tenant_store_db")).willReturn(tenantStoreLiquibase);

        ExtendedSpringLiquibase initialTenantLiquibase = mock(ExtendedSpringLiquibase.class);
        ExtendedSpringLiquibase tenantLiquibase = mock(ExtendedSpringLiquibase.class);
        FineractPlatformTenant defaultTenant = mock(FineractPlatformTenant.class);
        List<FineractPlatformTenant> allTenants = List.of(defaultTenant);
        DataSource defaultTenantDataSource = mock(DataSource.class);
        given(databaseStateVerifier.isFirstLiquibaseMigration(defaultTenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isFlywayPresent(defaultTenantDataSource)).willReturn(true);
        given(databaseStateVerifier.isTenantOnLatestUpgradableVersion(defaultTenantDataSource)).willReturn(false);
        given(tenantDetailsService.findAllTenants()).willReturn(allTenants);
        given(tenantDataSourceFactory.create(defaultTenant)).willReturn(defaultTenantDataSource);
        given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db", "initial_switch")).willReturn(initialTenantLiquibase);
        given(liquibaseFactory.create(defaultTenantDataSource, "tenant_db")).willReturn(tenantLiquibase);
        // when
        SchemaUpgradeNeededException result = assertThrows(SchemaUpgradeNeededException.class, () -> underTest.afterPropertiesSet());
        // then
        assertThat(result).isNotNull();
        verify(liquibaseFactory, times(2)).create(eq(tenantDataSource), any());
        verify(liquibaseFactory).create(eq(defaultTenantDataSource), any());
        verifyNoMoreInteractions(liquibaseFactory);
        verify(initialTenantStoreLiquibase).changeLogSync();
        verify(tenantStoreLiquibase).afterPropertiesSet();
        verifyNoInteractions(initialTenantLiquibase, tenantLiquibase);
    }
}
