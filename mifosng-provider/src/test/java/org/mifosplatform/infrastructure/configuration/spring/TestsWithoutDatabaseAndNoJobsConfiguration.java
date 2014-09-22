/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.spring;

import javax.sql.DataSource;

import org.mifosplatform.infrastructure.core.boot.AbstractApplicationConfiguration;
import org.mifosplatform.infrastructure.core.service.TenantDatabaseUpgradeService;
import org.mifosplatform.infrastructure.jobs.service.JobRegisterService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;

/**
 * Spring @Configuration which does not require a running database. It also does
 * not load any job configuration (as they are in the DB), thus nor starts any
 * background jobs. For some integration tests, this may be perfectly sufficient
 * (and faster to run such tests).
 */
public class TestsWithoutDatabaseAndNoJobsConfiguration extends AbstractApplicationConfiguration {

    /**
     * Override TenantDatabaseUpgradeService binding, because the real one has a @PostConstruct
     * upgradeAllTenants() which accesses the database on start-up.
     */
    @Bean
    public TenantDatabaseUpgradeService tenantDatabaseUpgradeService() {
        return new TenantDatabaseUpgradeService(null, null, null) {
            @Override
            public void upgradeAllTenants() {
                // NOOP
            }
        };
    }

    /**
     * Override JobRegisterService binding, because the real
     * JobRegisterServiceImpl has a @PostConstruct loadAllJobs() which accesses
     * the database on start-up.
     */
    @Bean
    public JobRegisterService jobRegisterServiceImpl() {
        JobRegisterService mockJobRegisterService = Mockito.mock(JobRegisterService.class);
        return mockJobRegisterService;
    }

    /**
     * DataSource with Mockito RETURNS_MOCKS black magic.
     */
    @Bean
    public DataSource tenantDataSourceJndi() {
        DataSource mockDataSource = Mockito.mock(DataSource.class, Mockito.RETURNS_MOCKS);
        return mockDataSource;
    }
}
