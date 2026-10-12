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
package org.apache.fineract.infrastructure.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.apache.fineract.infrastructure.core.config.jpa.JpaTransactionConfig;
import org.apache.fineract.infrastructure.core.persistence.TransactionLifecycleCallback;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Locks in the invariant that keeps mixed JPA/JDBC usage consistent: the primary (JPA) transaction manager must NOT
 * have a {@link javax.sql.DataSource} bound, while the dedicated JDBC transaction manager must.
 *
 * <p>
 * Why this matters: Spring's {@link JpaTransactionManager} only binds the EclipseLink JDBC connection as a thread-bound
 * resource (so that a {@code JdbcTemplate} on the same {@code DataSource} reuses it) when
 * {@code getDataSource() != null}. Fineract deliberately leaves the JPA manager's {@code DataSource} unset, so plain
 * JDBC access pulls its own connection from the pool and does not share EclipseLink's transactional connection. That
 * separation is precisely what makes it safe to run EclipseLink with {@code lazyDatabaseTransaction = true} (which
 * defers - and with the lock-free dialect, unlocks - acquisition of EclipseLink's own JDBC connection): there is no
 * JPA/JDBC connection sharing to compromise.
 *
 * <p>
 * If someone later calls {@code setDataSource(...)} on the JPA transaction manager to make JPA and JDBC atomic, this
 * test fails on purpose - that change would reintroduce connection sharing and {@code lazyDatabaseTransaction} would
 * then be the wrong setting (eager mode would be required instead).
 */
public class TransactionManagerDataSourceInvariantTest {

    @Test
    void jpaTransactionManagerMustNotHaveDataSourceSoJdbcDoesNotShareEclipseLinkConnection() {
        PlatformTransactionManager transactionManager = new JpaTransactionConfig().jpaTransactionManager(writeModeProperties(),
                noTransactionManagerCustomizers(), noCallbacks());

        assertThat(transactionManager).isInstanceOf(JpaTransactionManager.class);
        assertThat(((JpaTransactionManager) transactionManager).getDataSource())
                .as("JPA transaction manager must not bind a DataSource; otherwise JdbcTemplate would share EclipseLink's "
                        + "JDBC connection and lazyDatabaseTransaction could compromise JPA/JDBC consistency")
                .isNull();
    }

    @Test
    public void jdbcTransactionManagerMustBindDataSourceSoPlainJdbcParticipatesInTransactions() {
        RoutingDataSource dataSource = mock(RoutingDataSource.class);

        PlatformTransactionManager transactionManager = new JdbcTransactionConfig().jdbcTransactionManager(writeModeProperties(),
                dataSource, noTransactionManagerCustomizers(), noCallbacks());

        assertThat(transactionManager).isInstanceOf(DataSourceTransactionManager.class);
        assertThat(((DataSourceTransactionManager) transactionManager).getDataSource())
                .as("JDBC transaction manager must bind the routing DataSource so JdbcTemplate work participates in its transaction")
                .isSameAs(dataSource);
    }

    private static FineractProperties writeModeProperties() {
        FineractProperties properties = new FineractProperties();
        FineractProperties.FineractModeProperties mode = new FineractProperties.FineractModeProperties();
        mode.setWriteEnabled(true);
        properties.setMode(mode);
        return properties;
    }

    // Mirrors production: Spring Boot's default TransactionManagerCustomizers do not set a DataSource on the manager.
    // ifAvailable(...) on a Mockito mock is a no-op, so no customizer is applied - we assert the config's own
    // behaviour.
    @SuppressWarnings("unchecked")
    private static ObjectProvider<TransactionManagerCustomizers> noTransactionManagerCustomizers() {
        return mock(ObjectProvider.class);
    }

    private static List<TransactionLifecycleCallback> noCallbacks() {
        return List.of();
    }
}
