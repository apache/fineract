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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.db.DatabaseConnectionProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.core.service.DataSourcePerTenantServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestPropertySource("classpath:application-test.properties")
public class TomcatJdbcDataSourcePerTenantServiceTest {

    @Mock
    private FineractProperties fineractProperties;

    @Mock
    private HikariConfig hikariConfig;

    @Mock
    private Connection connection;

    @Mock
    private FineractPlatformTenant defaultTenant;

    @Mock
    private FineractPlatformTenantConnection tenantConnection;

    @InjectMocks
    private DataSourcePerTenantServiceFactory underTest;

    private DataSource dataSource;
    private DataSource tenantDataSource;
    private DatabaseConnectionProperties databaseConnectionProperties;

    @BeforeEach
    void setUp() throws SQLException {
        this.databaseConnectionProperties = DatabaseConnectionProperties.instance(System.getProperty("dbType"));

        tenantDataSource = mock(HikariDataSource.class);
        given(tenantDataSource.getConnection()).willReturn(connection);
        given(connection.getMetaData()).willReturn(mock(DatabaseMetaData.class));
        given(connection.getMetaData().getURL()).willReturn(databaseConnectionProperties.getJdbcUrl());

        given(tenantConnection.getSchemaServer()).willReturn(databaseConnectionProperties.getSchemaServer());
        given(tenantConnection.getSchemaServerPort()).willReturn(databaseConnectionProperties.getSchemaPort());
        given(tenantConnection.getSchemaUsername()).willReturn(databaseConnectionProperties.getSchemaUsername());
        given(tenantConnection.getSchemaPassword()).willReturn(databaseConnectionProperties.getSchemaPassword());
        given(tenantConnection.getSchemaName()).willReturn(databaseConnectionProperties.getSchemaName());

        given(defaultTenant.getConnection()).willReturn(tenantConnection);
        given(defaultTenant.getConnection().getMaxActive()).willReturn(5);
        given(defaultTenant.getConnection().getValidationInterval()).willReturn(500L);

        given(hikariConfig.getInitializationFailTimeout()).willReturn(0L);
        given(hikariConfig.getDriverClassName()).willReturn(databaseConnectionProperties.getDriverClassName());
        given(hikariConfig.getConnectionTestQuery()).willReturn(databaseConnectionProperties.getConnectionTestQuery());
        given(hikariConfig.getDataSourceProperties()).willReturn(mock(Properties.class));
        given(hikariConfig.isAutoCommit()).willReturn(true);
    }

    @Test
    void testRetrieveDataSource_ShouldCreateDataSource_WhenFineractIsInAllMode() {
        // given
        FineractProperties.FineractModeProperties modeProperties = createModeProps(true, true, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);

        // when
        dataSource = underTest.createNewDataSourceFor(tenantDataSource, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
    }

    @Test
    void testRetrieveDataSource_ShouldCreateDataSource_WhenFineractIsInReadOnlyMode() {
        // given
        FineractProperties.FineractModeProperties modeProperties = createModeProps(true, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);

        // when
        dataSource = underTest.createNewDataSourceFor(tenantDataSource, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
    }

    @Test
    void testRetrieveDataSource_ShouldCreateDataSource_WhenFineractIsInBatchMode() {
        // given
        FineractProperties.FineractModeProperties modeProperties = createModeProps(false, false, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);

        // when
        dataSource = underTest.createNewDataSourceFor(tenantDataSource, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
    }

    private FineractProperties.FineractModeProperties createModeProps(boolean readEnabled, boolean writeEnabled, boolean batchEnabled) {
        FineractProperties.FineractModeProperties modeProperties = new FineractProperties.FineractModeProperties();
        modeProperties.setReadEnabled(readEnabled);
        modeProperties.setWriteEnabled(writeEnabled);
        modeProperties.setBatchEnabled(batchEnabled);
        return modeProperties;
    }

}
