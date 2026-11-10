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
package org.apache.fineract.infrastructure.core.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.persistence.visibility.VisibilityProbe;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Pins that {@link AfterTransactionWork} runs only once a transaction of {@link ExtendedJpaTransactionManager} has
 * completed and released its connection, on a real PostgreSQL connection pool.
 */
@Testcontainers(disabledWithoutDocker = true)
class AfterTransactionWorkConnectionTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private HikariDataSource pool;
    private LocalContainerEntityManagerFactoryBean factory;
    private ExtendedJpaTransactionManager jpaTransactionManager;
    private TransactionTemplate requiresNewJdbc;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        pool = new HikariDataSource();
        pool.setJdbcUrl(POSTGRES.getJdbcUrl());
        pool.setUsername(POSTGRES.getUsername());
        pool.setPassword(POSTGRES.getPassword());
        // Short enough that a test needing a second connection from a pool of one fails fast instead of waiting.
        pool.setConnectionTimeout(1_000);
        RoutingDataSource dataSource = new RoutingDataSource() {

            @Override
            public DataSource determineTargetDataSource() {
                return pool;
            }
        };
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP SCHEMA public CASCADE");
        jdbc.execute("CREATE SCHEMA public");
        jdbc.execute("create table visibility_probe (id bigint primary key, name varchar(255))");

        factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPersistenceUnitName("after-transaction-work-" + UUID.randomUUID());
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(VisibilityProbe.class.getPackageName());
        factory.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("eclipselink.weaving", "false", "eclipselink.logging.level", "SEVERE"));
        factory.afterPropertiesSet();
        // As JpaTransactionConfig and JdbcTransactionConfig configure them.
        jpaTransactionManager = new ExtendedJpaTransactionManager(false);
        jpaTransactionManager.setEntityManagerFactory(factory.getObject());
        jpaTransactionManager.setValidateExistingTransaction(true);
        jpaTransactionManager.afterPropertiesSet();
        ExtendedDataSourceTransactionManager jdbcTransactionManager = new ExtendedDataSourceTransactionManager(false);
        jdbcTransactionManager.setDataSource(dataSource);
        jdbcTransactionManager.setValidateExistingTransaction(true);
        requiresNewJdbc = new TransactionTemplate(jdbcTransactionManager);
        requiresNewJdbc.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @AfterEach
    void tearDown() {
        if (factory != null) {
            factory.destroy();
        }
        if (pool != null) {
            pool.close();
        }
    }

    @Test
    void workAfterCompletionRunsOnceTheTransactionHasReleasedItsConnection() {
        // One connection: the chunk transaction holds it, so the work can only run once it is given back.
        usePoolOfSize(1);
        AtomicBoolean ranInside = new AtomicBoolean();

        assertThatThrownBy(() -> jpa().executeWithoutResult(outer -> {
            persistProbe(1L, "jpa");
            AfterTransactionWork.runAfterCompletion(() -> requiresNewJdbc.executeWithoutResult(status -> insertProbe(2L, "after")));
            ranInside.set(probeNamesFromAnotherConnectionOrNull() != null);
            throw new IllegalStateException("rollback");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(ranInside).as("no second connection may be needed while the transaction is open").isFalse();
        assertThat(probeNames()).containsExactly("after");
    }

    @Test
    void workRegisteredByAnOuterTransactionWaitsForItNotForAnInnerOne() {
        AtomicBoolean ran = new AtomicBoolean();
        AtomicBoolean ranBeforeOuterCompleted = new AtomicBoolean();
        TransactionTemplate requiresNewJpa = jpa();
        requiresNewJpa.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        jpa().executeWithoutResult(outer -> {
            AfterTransactionWork.runAfterCompletion(() -> ran.set(true));
            requiresNewJpa.executeWithoutResult(inner -> persistProbe(1L, "inner"));
            ranBeforeOuterCompleted.set(ran.get());
        });

        assertThat(ranBeforeOuterCompleted).isFalse();
        assertThat(ran).isTrue();
    }

    @Test
    void workRunsInTheOrderItWasRegisteredAndAFailureDoesNotStopTheRest() {
        jpa().executeWithoutResult(outer -> {
            AfterTransactionWork.runAfterCompletion(() -> insertProbe(1L, "first"));
            AfterTransactionWork.runAfterCompletion(() -> {
                throw new IllegalStateException("broken work");
            });
            AfterTransactionWork.runAfterCompletion(() -> insertProbe(2L, "third"));
        });

        assertThat(probeNames()).containsExactly("first", "third");
    }

    private TransactionTemplate jpa() {
        return new TransactionTemplate(jpaTransactionManager);
    }

    private void usePoolOfSize(int size) {
        pool.setMaximumPoolSize(size);
        pool.setMinimumIdle(0);
        pool.getHikariPoolMXBean().softEvictConnections();
    }

    private List<String> probeNamesFromAnotherConnectionOrNull() {
        List<String> names = new ArrayList<>();
        try (Connection connection = pool.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select name from visibility_probe order by id")) {
            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }
        } catch (SQLException noConnection) {
            return null;
        }
        return names;
    }

    private void persistProbe(Long id, String name) {
        var entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(factory.getObject());
        entityManager.persist(new VisibilityProbe(id, name));
        entityManager.flush();
    }

    private void insertProbe(Long id, String name) {
        jdbc.update("insert into visibility_probe (id, name) values (?, ?)", id, name);
    }

    private List<String> probeNames() {
        return jdbc.queryForList("select name from visibility_probe order by id", String.class);
    }
}
