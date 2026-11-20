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
package org.apache.fineract.test.database;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@ParameterizedClass(name = "{0}")
@MethodSource("databases")
@TestInstance(Lifecycle.PER_METHOD)
public abstract class JdbcTestcontainersSupport {

    @Parameter
    String engine;

    private JdbcDatabaseContainer<?> database;
    private QuerydslTestSchema schema;
    private Connection connection;
    protected JdbcTemplate jdbc;
    protected SQLQueryFactory queryFactory;

    @RegisterExtension
    final BeforeEachCallback sharedDatabase = context -> {
        final Namespace namespace = Namespace.create(JdbcTestcontainersSupport.class, engine);
        final Store store = context.getRoot().getStore(namespace);
        database = store.computeIfAbsent(engine, JdbcTestcontainersSupport::startDatabase, JdbcDatabaseContainer.class);
        schema = store.computeIfAbsent(QuerydslTestSchema.class, key -> new QuerydslTestSchema(database instanceof PostgreSQLContainer<?>),
                QuerydslTestSchema.class);
    };

    protected static Stream<String> databases() {
        final String selected = System.getProperty("fineract.test.database", "all").toLowerCase(Locale.ROOT);
        return "all".equals(selected) ? Stream.of("postgresql", "mysql", "mariadb") : Stream.of(selected);
    }

    @SuppressWarnings("resource")
    private static JdbcDatabaseContainer<?> startDatabase(final String engine) {
        final JdbcDatabaseContainer<?> container = switch (engine) {
            case "postgresql" -> new PostgreSQLContainer<>("postgres:18.3-bookworm");
            case "mysql" -> new MySQLContainer<>("mysql:8");
            case "mariadb" -> new MariaDBContainer<>("mariadb:12.2").withCommand("--innodb-snapshot-isolation=OFF");
            default -> throw new IllegalArgumentException("Unknown test database engine: " + engine);
        };
        try {
            container.start();
            return container;
        } catch (final Exception e) {
            container.close();
            throw new IllegalStateException("Could not initialize " + engine + " test database", e);
        }
    }

    @BeforeEach
    protected final void beginTransaction() throws SQLException {
        final DatabaseTables tables = Objects.requireNonNull(getClass().getAnnotation(DatabaseTables.class),
                "Declare the QueryDSL tables for this test with @DatabaseTables");
        connection = database.createConnection("");
        schema.createMissingTables(connection, tables);
        connection.setAutoCommit(false);
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        final SQLTemplates templates = database instanceof PostgreSQLContainer<?> ? PostgreSQLTemplates.DEFAULT : MySQLTemplates.DEFAULT;
        queryFactory = new SQLQueryFactory(templates, () -> connection);
    }

    @AfterEach
    protected final void rollBackTransaction() throws SQLException {
        try (final Connection transaction = connection) {
            connection = null;
            if (transaction != null && !transaction.getAutoCommit()) {
                transaction.rollback();
            }
        }
    }
}
