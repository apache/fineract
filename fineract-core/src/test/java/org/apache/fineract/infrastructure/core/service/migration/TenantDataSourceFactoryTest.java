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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.core.service.database.DatabasePasswordEncryptor;
import org.junit.jupiter.api.Test;

class TenantDataSourceFactoryTest {

    @Test
    void tenantDataSourceInheritsTransactionIsolation() {
        try (HikariDataSource created = create("TRANSACTION_REPEATABLE_READ")) {
            assertEquals("TRANSACTION_REPEATABLE_READ", created.getTransactionIsolation());
        }
    }

    @Test
    void tenantDataSourceKeepsDriverDefaultWhenIsolationNotConfigured() {
        try (HikariDataSource created = create(null)) {
            assertNull(created.getTransactionIsolation());
        }
    }

    private HikariDataSource create(String isolation) {
        HikariConfig hikariConfig = mock(HikariConfig.class);
        when(hikariConfig.getDriverClassName()).thenReturn("org.postgresql.Driver");
        HikariDataSource tenantStore = mock(HikariDataSource.class);
        when(tenantStore.getDriverClassName()).thenReturn(StubDriver.class.getName());
        when(tenantStore.getDataSourceProperties()).thenReturn(new Properties());
        when(tenantStore.getMinimumIdle()).thenReturn(1);
        when(tenantStore.getMaximumPoolSize()).thenReturn(2);
        when(tenantStore.getIdleTimeout()).thenReturn(60000L);
        when(tenantStore.getTransactionIsolation()).thenReturn(isolation);

        DatabasePasswordEncryptor encryptor = mock(DatabasePasswordEncryptor.class);
        when(encryptor.isMasterPasswordHashValid("hash")).thenReturn(true);
        when(encryptor.decrypt("pwd")).thenReturn("pwd");

        FineractPlatformTenantConnection tenantConnection = mock(FineractPlatformTenantConnection.class);
        when(tenantConnection.getMasterPasswordHash()).thenReturn("hash");
        when(tenantConnection.getSchemaUsername()).thenReturn("user");
        when(tenantConnection.getSchemaPassword()).thenReturn("pwd");
        when(tenantConnection.getSchemaServer()).thenReturn("localhost");
        when(tenantConnection.getSchemaServerPort()).thenReturn("5432");
        when(tenantConnection.getSchemaName()).thenReturn("default");
        FineractPlatformTenant tenant = mock(FineractPlatformTenant.class);
        when(tenant.getConnection()).thenReturn(tenantConnection);

        return new TenantDataSourceFactory(hikariConfig, tenantStore, encryptor).create(tenant);
    }

    public static class StubDriver implements Driver {

        @Override
        public Connection connect(String url, Properties info) {
            return null;
        }

        @Override
        public boolean acceptsURL(String url) {
            return false;
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() {
            return null;
        }
    }
}
