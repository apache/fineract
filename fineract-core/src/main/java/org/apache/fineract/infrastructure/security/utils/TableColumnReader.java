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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Reads the column names of a table from the JDBC metadata, cached per tenant and table in the
 * {@value #TABLE_COLUMNS_CACHE_NAME} platform cache.
 * <p>
 * The metadata lookup is a catalog query of about a hundred rows, and every sorted listing runs it through
 * {@link ColumnValidator}. The cache follows the platform cache mode: it is a no-op with the default no-cache mode and
 * expires with the configured TTL in single-node mode. Single-node entries are local to each instance, so after a
 * datatable DDL other instances see the new columns once their entry expires.
 */
@Component
@Slf4j
public class TableColumnReader {

    public static final String TABLE_COLUMNS_CACHE_NAME = "tableColumns";

    private final JdbcTemplate jdbcTemplate;
    private final CacheManager cacheManager;

    @Autowired
    public TableColumnReader(final JdbcTemplate jdbcTemplate, @Qualifier("runtimeDelegatingCacheManager") final CacheManager cacheManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.cacheManager = cacheManager;
    }

    /**
     * Returns the column names of the table, or an empty set when the table does not exist. Empty results are not
     * cached, so a datatable created afterwards is picked up without an eviction.
     */
    @Cacheable(cacheNames = TABLE_COLUMNS_CACHE_NAME, key = "T(org.apache.fineract.infrastructure.security.utils.TableColumnReader).cacheKey(#tableName)", unless = "#result.isEmpty()")
    public Set<String> readColumns(final String tableName) {
        final DataSource dataSource = Objects.requireNonNull(this.jdbcTemplate.getDataSource());
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            final DatabaseMetaData dbMetaData = connection.getMetaData();
            try (ResultSet resultSet = dbMetaData.getColumns(null, null, tableName, null)) {
                return Set.copyOf(getTableColumns(resultSet));
            }
        } catch (SQLException e) {
            throw new PlatformApiDataValidationException("error.msg.database.access.error",
                    "Database access error during column validation", e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    /**
     * Drops the cached columns of the table for the current tenant. Called after any DDL on the table.
     * <p>
     * Inside a transaction the entry is evicted now and again after the commit: on PostgreSQL the DDL is transactional,
     * so a concurrent lookup before the commit still reads the old structure and would cache it again.
     */
    public void evict(final String tableName) {
        final String key = cacheKey(tableName);
        evictKey(key);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    evictKey(key);
                }
            });
        }
    }

    /** Cache key of a table for the current tenant; fails when there is no tenant in context. */
    public static String cacheKey(final String tableName) {
        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        if (tenant == null) {
            throw new IllegalStateException("No tenant in context for the table column lookup of " + tableName);
        }
        return tenant.getTenantIdentifier() + "_" + tableName.toLowerCase(Locale.ROOT);
    }

    private void evictKey(final String key) {
        final Cache cache = cacheManager.getCache(TABLE_COLUMNS_CACHE_NAME);
        if (cache != null) {
            cache.evict(key);
        }
    }

    private Set<String> getTableColumns(final ResultSet rs) {
        final Set<String> columns = new HashSet<>();
        try {
            while (rs.next()) {
                columns.add(rs.getString("column_name"));
            }
        } catch (SQLException e) {
            log.error("Problem occurred in getTableColumns function", e);
        }
        return columns;
    }
}
