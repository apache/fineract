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
package org.apache.fineract.command.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Verifies the idempotency key migration against a database that already contains rows sharing a key, which is the
 * state of any system that ran an earlier build.
 * <p>
 * The migration must let the earliest row per key keep it, make the surplus rows unique without deleting them, leave
 * rows without a key untouched, and run on every supported database - MySQL in particular rejects a subquery that
 * references the UPDATE target table.
 */
@Slf4j
public class CommandIdempotencyKeyMigrationTest {

    /**
     * Deliberately not under {@code db/changelog}: this is a test harness that seeds old rows and then runs the real
     * parts, not a master changelog. The registration check treats every file under {@code db/changelog} that has an
     * {@code <include>} as a master, and would read this one as including the production parts a second time.
     */
    private static final String CHANGELOG = "liquibase-test/command-migration-test-changelog.xml";

    @BeforeAll
    static void requireDocker() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not available, skipping Testcontainers tests");
    }

    @Test
    public void postgres() throws Exception {
        try (var container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3"))) {
            verify(container, "postgresql");
        }
    }

    @Test
    public void mysql() throws Exception {
        try (var container = new MySQLContainer<>(DockerImageName.parse("mysql:8"))) {
            verify(container, "mysql");
        }
    }

    @Test
    public void mariadb() throws Exception {
        try (var container = new MariaDBContainer<>(DockerImageName.parse("mariadb:12.2")).withCommand("--innodb-snapshot-isolation=OFF")
                .waitingFor(Wait.forListeningPort())) {
            verify(container, "mysql");
        }
    }

    private void verify(JdbcDatabaseContainer<?> container, String context) throws Exception {
        container.start();

        migrate(container, context);

        try (var connection = connect(container)) {
            assertEquals(6, count(connection, "1 = 1"), "The migration must not remove any command record.");

            assertEquals(1, count(connection, "idempotency_key = 'KEEP-1'"), "Only one row may keep a given idempotency key.");
            assertEquals(1, count(connection, "idempotency_key = 'KEEP-1' AND id = 1 AND request = 'original'"),
                    "The earliest row must be the one that keeps the key.");

            // the surplus rows survive with a readable, traceable key rather than being deleted
            assertEquals(1, count(connection, "idempotency_key = 'KEEP-1-duplicate-2' AND id = 2 AND request = 'surplus'"),
                    "A surplus row must survive with the original key plus its row id.");
            assertEquals(1, count(connection, "idempotency_key = 'KEEP-1-duplicate-3' AND id = 3 AND request = 'surplus'"),
                    "Every surplus row must survive, each with its own key.");

            assertEquals(1, count(connection, "idempotency_key = 'KEEP-2' AND id = 4"), "A key with a single row must be left untouched.");
            assertEquals(2, count(connection, "idempotency_key IS NULL"), "Rows without an idempotency key must be left untouched.");

            assertEquals(0, count(connection, "response_status_code IS NOT NULL"), "No status has been recorded for existing rows.");

            assertThrows(SQLException.class, () -> insert(connection, 100, "KEEP-1"),
                    "The unique constraint should reject a second row with the same idempotency key.");

            // several rows without a key stay legal under the unique constraint
            insert(connection, 101, null);
            assertEquals(3, count(connection, "idempotency_key IS NULL"), "The unique constraint must not apply to null keys.");

            assertThrows(SQLException.class, () -> insert(connection, 103, "KEEP-1-duplicate-2"), "A suffixed key must be unique as well.");

            // every row that predates the distinctor gets the empty one, and so does a row inserted without one
            assertEquals(count(connection, "1 = 1"), count(connection, "custom_idempotency_distinctor = ''"),
                    "Existing rows and rows inserted without a distinctor must get the empty one, never null.");

            // the key is unique together with its distinctor, not on its own
            insert(connection, 104, "KEEP-1", "LOAN|CREATE");
            assertEquals(2, count(connection, "idempotency_key = 'KEEP-1'"), "The same key under another distinctor must be accepted.");
            assertThrows(SQLException.class, () -> insert(connection, 105, "KEEP-1", "LOAN|CREATE"),
                    "The same key under the same distinctor must still be rejected.");
        }

        rollbackUniqueConstraint(container, context);

        try (var connection = connect(container)) {
            insert(connection, 102, "KEEP-1");
            assertEquals(2, count(connection, "idempotency_key = 'KEEP-1' AND custom_idempotency_distinctor = ''"),
                    "After rollback the same key and distinctor should be accepted again.");
        }
    }

    private static Connection connect(JdbcDatabaseContainer<?> container) throws SQLException {
        return DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());
    }

    // note: Liquibase closes the connection it was given, so it always gets one of its own
    private void migrate(JdbcDatabaseContainer<?> container, String context) throws Exception {
        try (var connection = connect(container); var liquibase = liquibase(connection)) {
            liquibase.update(new Contexts(context), new LabelExpression());
        }
    }

    /**
     * Reverts the unique constraint, which means also reverting the response-cache changeset stacked on top of it. Both
     * databases apply the same number of changesets from that point on, so this count has to grow with every changeset
     * added after the constraint.
     */
    private void rollbackUniqueConstraint(JdbcDatabaseContainer<?> container, String context) throws Exception {
        try (var connection = connect(container); var liquibase = liquibase(connection)) {
            liquibase.rollback(2, new Contexts(context), new LabelExpression());
        }
    }

    private static Liquibase liquibase(Connection connection) throws Exception {
        return new Liquibase(CHANGELOG, new ClassLoaderResourceAccessor(),
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection)));
    }

    private static int count(Connection connection, String where) throws SQLException {
        try (Statement statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT COUNT(*) FROM m_command WHERE " + where)) {
            resultSet.next();

            return resultSet.getInt(1);
        }
    }

    private static void insert(Connection connection, long id, String key, String distinctor) throws SQLException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO m_command (id, idempotency_key, custom_idempotency_distinctor, state) VALUES (?, ?, ?, 'PROCESSED')")) {
            statement.setLong(1, id);
            statement.setString(2, key);
            statement.setString(3, distinctor);
            statement.executeUpdate();
        }
    }

    private static void insert(Connection connection, long id, String key) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO m_command (id, idempotency_key, state) VALUES (?, ?, 'PROCESSED')")) {
            statement.setLong(1, id);
            statement.setString(2, key);
            statement.executeUpdate();
        }
    }
}
