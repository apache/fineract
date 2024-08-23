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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;

import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import javax.sql.DataSource;
import liquibase.change.custom.CustomTaskChange;
import okhttp3.OkHttpClient;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.database.DatabaseIndependentQueryService;
import org.apache.fineract.infrastructure.core.service.database.DatabasePasswordEncryptor;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.core.service.migration.ExtendedSpringLiquibaseFactory;
import org.apache.fineract.infrastructure.core.service.migration.TenantDataSourceFactory;
import org.apache.fineract.infrastructure.core.service.migration.TenantDatabaseStateVerifier;
import org.apache.fineract.infrastructure.core.service.migration.TenantDatabaseUpgradeService;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.jobs.ScheduledJobRunnerConfig;
import org.apache.fineract.infrastructure.jobs.service.JobRegisterService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring @Configuration which does not require a running database. It also does not load any job configuration (as they
 * are in the DB), thus nor starts any background jobs. For some integration tests, this may be perfectly sufficient
 * (and faster to run such tests).
 */
@Configuration
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class, GsonAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
        LiquibaseAutoConfiguration.class, BatchAutoConfiguration.class })
@EnableTransactionManagement
@EnableWebSecurity
@EnableConfigurationProperties({ FineractProperties.class, LiquibaseProperties.class })
@ComponentScan(basePackages = "org.apache.fineract", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ScheduledJobRunnerConfig.class) })
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestConfiguration {

    @Bean
    public TenantDataSourceFactory tenantDataSourceFactory(DatabasePasswordEncryptor databasePasswordEncryptor) {
        return new TenantDataSourceFactory(null, databasePasswordEncryptor) {

            @Override
            public DataSource create(FineractPlatformTenant tenant) {
                return mock(DataSource.class);
            }
        };
    }

    @Primary
    @Bean
    public HikariDataSource tenantDataSource() {
        HikariDataSource mockDataSource = mock(HikariDataSource.class, Mockito.RETURNS_MOCKS);
        return mockDataSource;
    }

    /**
     * DataSource with Mockito RETURNS_MOCKS black magic.
     */
    @Bean
    public DataSource hikariTenantDataSource() {
        HikariDataSource mockDataSource = mock(HikariDataSource.class, Mockito.RETURNS_MOCKS);
        return mockDataSource;
    }

    @Primary
    @Bean
    public DatabaseTypeResolver databaseTypeResolver() {
        DatabaseTypeResolver mock = mock(DatabaseTypeResolver.class, RETURNS_MOCKS);
        given(mock.databaseType()).willReturn(DatabaseType.POSTGRESQL);
        given(mock.isPostgreSQL()).willReturn(true);
        return mock;
    }

    @Primary
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
            LiquibaseProperties liquibaseProperties, DatabaseTypeResolver databaseTypeResolver) {
        return new TenantDatabaseStateVerifier(liquibaseProperties, databaseIndependentQueryService, databaseTypeResolver);
    }

    /**
     * Override TenantDatabaseUpgradeService binding, because the real one has a @PostConstruct upgradeAllTenants()
     * which accesses the database on start-up.
     */
    @Bean
    public TenantDatabaseUpgradeService tenantDatabaseUpgradeService(TenantDetailsService tenantDetailsService,
            HikariDataSource tenantDataSource, TenantDatabaseStateVerifier tenantDatabaseStateVerifier,
            ExtendedSpringLiquibaseFactory liquibaseFactory, TenantDataSourceFactory tenantDataSourceFactory,
            FineractProperties fineractProperties, Environment environment,
            List<CustomTaskChange> customTaskChangesForDependencyInjection) {
        return new TenantDatabaseUpgradeService(tenantDetailsService, tenantDataSource, fineractProperties, tenantDatabaseStateVerifier,
                liquibaseFactory, tenantDataSourceFactory, environment, customTaskChangesForDependencyInjection);
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

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return mock(JdbcTemplate.class);
    }

    @Primary
    @Bean
    public JobExplorer jobExplorer() {
        return mock(JobExplorer.class, RETURNS_MOCKS);
    }

    @Primary
    @Bean
    public JobLauncher jobLauncher() {
        return mock(JobLauncher.class, RETURNS_MOCKS);
    }

    @Primary
    @Bean
    public JobRegistry jobRegistry() {
        return mock(JobRegistry.class, RETURNS_MOCKS);
    }

    @Primary
    @Bean
    public JobRepository jobRepository() {
        return mock(JobRepository.class, RETURNS_MOCKS);
    }

    @Primary
    @Bean
    public JobOperator jobOperator() {
        return mock(JobOperator.class, RETURNS_MOCKS);
    }

    @Primary
    @Bean
    public OkHttpClient okHttpClient() {
        return mock(OkHttpClient.class, RETURNS_MOCKS);
    }

    @Primary
    @Bean
    public GenericDataService genericDataService() {
        return mock(GenericDataService.class, RETURNS_MOCKS);
    }
}
