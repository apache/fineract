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
package org.apache.fineract.infrastructure.configuration.spring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariConfig;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.boot.AbstractApplicationConfiguration;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.migration.TenantDataSourceFactory;
import org.apache.fineract.infrastructure.core.service.migration.TenantDatabaseUpgradeService;
import org.apache.fineract.infrastructure.jobs.service.JobRegisterService;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring @Configuration which does not require a running database. It also does not load any job configuration (as they
 * are in the DB), thus nor starts any background jobs. For some integration tests, this may be perfectly sufficient
 * (and faster to run such tests).
 */
@EnableConfigurationProperties({ FineractProperties.class })
public class TestsWithoutDatabaseAndNoJobsConfiguration extends AbstractApplicationConfiguration {

    @Bean
    public HikariConfig hikariConfig() {
        HikariConfig mock = mock(HikariConfig.class);
        when(mock.getDriverClassName()).thenReturn("org.mariadb.jdbc.Driver");
        return mock;
    }

    @Bean
    public TenantDataSourceFactory tenantDataSourceFactory() {
        return new TenantDataSourceFactory(null) {

            @Override
            public DataSource create(FineractPlatformTenant tenant) {
                return mock(DataSource.class);
            }
        };
    }

    /**
     * Override TenantDatabaseUpgradeService binding, because the real one has a @PostConstruct upgradeAllTenants()
     * which accesses the database on start-up.
     */
    @Bean
    public TenantDatabaseUpgradeService tenantDatabaseUpgradeService() {
        return new TenantDatabaseUpgradeService(null, null, null, null, null, null) {

            @Override
            public void afterPropertiesSet() {
                // NOOP
            }
        };
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

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
