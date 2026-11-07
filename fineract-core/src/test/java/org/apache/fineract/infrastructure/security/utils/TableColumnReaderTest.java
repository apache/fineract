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
package org.apache.fineract.infrastructure.security.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

@SpringJUnitConfig(TableColumnReaderTest.Config.class)
class TableColumnReaderTest {

    private static final String TABLE = "m_client";

    @Configuration
    @EnableCaching
    static class Config {

        @Bean
        DatabaseMetaData metaData() {
            return mock(DatabaseMetaData.class);
        }

        @Bean
        JdbcTemplate jdbcTemplate(DatabaseMetaData metaData) throws SQLException {
            Connection connection = mock(Connection.class);
            when(connection.getMetaData()).thenReturn(metaData);
            DataSource dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenReturn(connection);
            JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
            when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
            return jdbcTemplate;
        }

        @Bean
        CacheManager runtimeDelegatingCacheManager() {
            return new ConcurrentMapCacheManager(TableColumnReader.TABLE_COLUMNS_CACHE_NAME);
        }

        @Bean
        TableColumnReader tableColumnReader(JdbcTemplate jdbcTemplate, CacheManager runtimeDelegatingCacheManager) {
            return new TableColumnReader(jdbcTemplate, runtimeDelegatingCacheManager);
        }
    }

    @Autowired
    private TableColumnReader underTest;
    @Autowired
    private DatabaseMetaData metaData;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() throws SQLException {
        reset(metaData);
        cacheManager.getCache(TableColumnReader.TABLE_COLUMNS_CACHE_NAME).clear();
        setTenant("tenant1");
        when(metaData.getColumns(isNull(), isNull(), eq(TABLE), isNull())).thenAnswer(inv -> columns("id", "display_name"));
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        List.copyOf(TransactionSynchronizationManager.getResourceMap().keySet()).forEach(TransactionSynchronizationManager::unbindResource);
        ThreadLocalContextUtil.reset();
    }

    @Test
    void readsTableMetadataOnceForRepeatedLookups() throws SQLException {
        assertThat(underTest.readColumns(TABLE)).containsExactlyInAnyOrder("id", "display_name");
        underTest.readColumns(TABLE);

        verifyMetadataReads(1);
    }

    @Test
    void readsTableMetadataAgainAfterEviction() throws SQLException {
        underTest.readColumns(TABLE);
        underTest.evict(TABLE);
        underTest.readColumns(TABLE);

        verifyMetadataReads(2);
    }

    @Test
    void evictsAgainAfterCommitWhatALookupBeforeTheCommitCachedAgain() throws SQLException {
        TransactionSynchronizationManager.initSynchronization();
        underTest.readColumns(TABLE);
        underTest.evict(TABLE);
        // A lookup between the DDL and the commit still sees the old structure and caches it again
        underTest.readColumns(TABLE);

        TransactionSynchronizationUtils.invokeAfterCommit(TransactionSynchronizationManager.getSynchronizations());
        underTest.readColumns(TABLE);

        verifyMetadataReads(3);
    }

    @Test
    void doesNotCacheUnknownTables() throws SQLException {
        when(metaData.getColumns(isNull(), isNull(), eq("m_unknown"), isNull())).thenAnswer(inv -> columns());

        assertThat(underTest.readColumns("m_unknown")).isEmpty();
        underTest.readColumns("m_unknown");

        verify(metaData, times(2)).getColumns(isNull(), isNull(), eq("m_unknown"), isNull());
    }

    @Test
    void keepsTheColumnsOfEachTenantApart() throws SQLException {
        underTest.readColumns(TABLE);
        setTenant("tenant2");
        underTest.readColumns(TABLE);

        verifyMetadataReads(2);
    }

    @Test
    void failsWithoutTenant() {
        ThreadLocalContextUtil.reset();

        assertThatThrownBy(() -> underTest.readColumns(TABLE)).isInstanceOf(IllegalStateException.class);
    }

    private void verifyMetadataReads(int count) throws SQLException {
        verify(metaData, times(count)).getColumns(isNull(), isNull(), eq(TABLE), isNull());
    }

    private static void setTenant(String identifier) {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, identifier, identifier, "Asia/Kolkata", null));
    }

    private static ResultSet columns(String... names) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        int[] cursor = { -1 };
        when(rs.next()).thenAnswer(inv -> ++cursor[0] < names.length);
        when(rs.getString(any(String.class))).thenAnswer(inv -> names[cursor[0]]);
        return rs;
    }
}
