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
package org.apache.fineract;

import static org.mockito.Mockito.mock;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.boot.AbstractApplicationConfiguration;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.database.DatabaseIndependentQueryService;
import org.apache.fineract.infrastructure.core.service.migration.ExtendedSpringLiquibaseFactory;
import org.apache.fineract.infrastructure.core.service.migration.TenantDataSourceFactory;
import org.apache.fineract.infrastructure.core.service.migration.TenantDatabaseStateVerifier;
import org.apache.fineract.infrastructure.core.service.migration.TenantDatabaseUpgradeService;
import org.apache.fineract.infrastructure.jobs.service.JobRegisterService;
import org.apache.fineract.infrastructure.security.service.TenantDetailsService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring @Configuration which does not require a running database. It also does not load any job configuration (as they
 * are in the DB), thus nor starts any background jobs. For some integration tests, this may be perfectly sufficient
 * (and faster to run such tests).
 */
@EnableConfigurationProperties({ FineractProperties.class })
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestConfiguration extends AbstractApplicationConfiguration {

    @Bean
    public TenantDataSourceFactory tenantDataSourceFactory() {
        return new TenantDataSourceFactory(null) {

            @Override
            public DataSource create(FineractPlatformTenant tenant) {
                return mock(DataSource.class);
            }
        };
    }

    @Bean
    public HikariDataSource tenantDataSource() {
        return mock(HikariDataSource.class, Mockito.RETURNS_MOCKS);
    }

    @Bean
    public TenantDetailsService tenantDetailsService() {
        return mock(TenantDetailsService.class, Mockito.RETURNS_MOCKS);
    }

    @Bean
    public ExtendedSpringLiquibaseFactory liquibaseFactory() {
        return mock(ExtendedSpringLiquibaseFactory.class, Mockito.RETURNS_MOCKS);
    }

    @Bean
    public DatabaseIndependentQueryService databaseIndependentQueryService() {
        return mock(DatabaseIndependentQueryService.class, Mockito.RETURNS_MOCKS);
    }

    @Bean
    public TenantDatabaseStateVerifier tenantDatabaseStateVerifier(DatabaseIndependentQueryService databaseIndependentQueryService,
            LiquibaseProperties liquibaseProperties, FineractProperties fineractProperties) {
        return new TenantDatabaseStateVerifier(liquibaseProperties, databaseIndependentQueryService);
    }

    /**
     * Override TenantDatabaseUpgradeService binding, because the real one has a @PostConstruct upgradeAllTenants()
     * which accesses the database on start-up.
     */
    @Bean
    public TenantDatabaseUpgradeService tenantDatabaseUpgradeService(TenantDetailsService tenantDetailsService,
            HikariDataSource tenantDataSource, TenantDatabaseStateVerifier tenantDatabaseStateVerifier,
            ExtendedSpringLiquibaseFactory liquibaseFactory, TenantDataSourceFactory tenantDataSourceFactory,
            FineractProperties fineractProperties) {
        return new TenantDatabaseUpgradeService(tenantDetailsService, tenantDataSource, fineractProperties, tenantDatabaseStateVerifier,
                liquibaseFactory, tenantDataSourceFactory);
    }

    /**
     * Override JobRegisterService binding, because the real JobRegisterServiceImpl has a @PostConstruct loadAllJobs()
     * which accesses the database on start-up.
     */
    @Bean
    public JobRegisterService jobRegisterServiceImpl() {
        JobRegisterService mockJobRegisterService = mock(JobRegisterService.class);
        return mockJobRegisterService;
    }

    /**
     * DataSource with Mockito RETURNS_MOCKS black magic.
     */
    @Bean
    public DataSource hikariTenantDataSource() {
        DataSource mockDataSource = mock(DataSource.class, Mockito.RETURNS_MOCKS);
        return mockDataSource;
    }
}
