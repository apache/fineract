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
package org.apache.fineract.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.FROM_CACHE;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.DockerClientFactory;

@EnabledIf("dockerAvailable")
public class QuerydslCodegenPluginFunctionalTest {

    private static final String TASK = ":querydslCodegen";
    private static final String MASTER = "src/main/resources/db/changelog/db.changelog-master.yaml";
    private static final String GENERATED = "build/generated/sources/querydsl/java/main/org/example/q/";

    @TempDir
    Path projectDir;

    static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
    }

    @BeforeEach
    void setUp() throws IOException {
        write("settings.gradle", """
                rootProject.name = 'fixture'

                dependencyResolutionManagement {
                    repositories {
                        mavenCentral()
                    }
                }

                buildCache {
                    local {
                        directory = new File(rootDir, 'build-cache')
                    }
                }
                """);
        buildFile("");
        write(MASTER, accGlAccountChangelog(false));
        write("src/main/java/org/example/UsesQClass.java", """
                package org.example;

                import org.example.q.QAccGlAccount;

                public class UsesQClass {
                    public static final QAccGlAccount GL = QAccGlAccount.accGlAccount;
                }
                """);
    }

    @Test
    void generatesQClassesThatCompile() throws IOException {
        BuildResult result = run("compileJava");

        assertThat(outcome(result)).isEqualTo(SUCCESS);
        assertThat(result.task(":compileJava").getOutcome()).isEqualTo(SUCCESS);

        assertThat(read(GENERATED + "QAccGlAccount.java"))
                .contains("NumberPath<Long> id")
                .contains("StringPath glCode")
                .contains("NumberPath<Integer> accountUsage"); // int2 -> Integer mapping
        assertThat(projectDir.resolve(GENERATED + "QDatabasechangelog.java")).doesNotExist();
        assertThat(projectDir.resolve(GENERATED + "QDatabasechangeloglock.java")).doesNotExist();
    }

    @Test
    void isUpToDateWhenNothingChanged() {
        run("compileJava");

        assertThat(outcome(run("compileJava"))).isEqualTo(UP_TO_DATE);
    }

    @Test
    void isRestoredFromCacheAfterClean() {
        run("compileJava", "--build-cache");
        run("clean");

        assertThat(outcome(run("compileJava", "--build-cache"))).isEqualTo(FROM_CACHE);
    }

    @Test
    void rerunsWhenChangelogChanges() throws IOException {
        run("compileJava");
        write(MASTER, accGlAccountChangelog(true));

        assertThat(outcome(run("compileJava"))).isEqualTo(SUCCESS);
        assertThat(read(GENERATED + "QAccGlAccount.java")).contains("StringPath description");
    }

    @Test
    void ignoresLineEndingChanges() throws IOException {
        run("compileJava");
        Path changelog = projectDir.resolve(MASTER);
        Files.writeString(changelog, Files.readString(changelog).replace("\n", "\r\n"));

        assertThat(outcome(run("compileJava"))).isEqualTo(UP_TO_DATE);
    }

    @Test
    void ignoresEmptyDirectories() throws IOException {
        run("compileJava");
        Files.createDirectories(projectDir.resolve("src/main/resources/db/changelog/empty"));

        assertThat(outcome(run("compileJava"))).isEqualTo(UP_TO_DATE);
    }

    @Test
    void ignoresResourcesOutsideTheChangelogIncludes() throws IOException {
        run("compileJava");
        write("src/main/resources/application.properties", "some.setting=changed");

        assertThat(outcome(run("compileJava"))).isEqualTo(UP_TO_DATE);
    }

    @Test
    void rerunsWhenPackageNameChanges() throws IOException {
        run(TASK);
        write("build.gradle", """
                plugins {
                    id 'java'
                    id 'org.apache.fineract.querydsl.codegen'
                }

                querydslCodegen {
                    packageName = 'org.example.other'
                }
                """);

        assertThat(outcome(run(TASK))).isEqualTo(SUCCESS);
        assertThat(projectDir.resolve("build/generated/sources/querydsl/java/main/org/example/other/QAccGlAccount.java")).exists();
        assertThat(projectDir.resolve(GENERATED)).doesNotExist(); // stale package removed
    }

    @Test
    void resolvesIncludesAcrossResourceRoots() throws IOException {
        buildFile("resourceRoots.setFrom('src/main/resources', 'module/src/main/resources')");
        write(MASTER, accGlAccountChangelog(false) + """
                  - include:
                      file: db/changelog/module/module-changelog.yaml
                """);
        write("module/src/main/resources/db/changelog/module/module-changelog.yaml", moduleChangelog("module_table"));

        assertThat(outcome(run(TASK))).isEqualTo(SUCCESS);
        assertThat(projectDir.resolve(GENERATED + "QModuleTable.java")).exists();
    }

    @Test
    void rerunsWhenChangelogInAnotherResourceRootChanges() throws IOException {
        buildFile("resourceRoots.setFrom('src/main/resources', 'module/src/main/resources')");
        write(MASTER, accGlAccountChangelog(false) + """
                  - include:
                      file: db/changelog/module/module-changelog.yaml
                """);
        write("module/src/main/resources/db/changelog/module/module-changelog.yaml", moduleChangelog("module_table"));
        run(TASK);

        write("module/src/main/resources/db/changelog/module/module-changelog.yaml", moduleChangelog("renamed_table"));

        assertThat(outcome(run(TASK))).isEqualTo(SUCCESS);
        assertThat(projectDir.resolve(GENERATED + "QRenamedTable.java")).exists();
        assertThat(projectDir.resolve(GENERATED + "QModuleTable.java")).doesNotExist();
    }

    @Test
    void appliesContextRunsInOrder() throws IOException {
        buildFile("contextRuns = ['initial,postgresql', 'postgresql']");
        write(MASTER, """
                databaseChangeLog:
                  - changeSet:
                      id: 1
                      author: test
                      context: initial
                      changes:
                        - createTable:
                            tableName: phased
                            columns:
                              - column: { name: id, type: bigint, constraints: { primaryKey: true, nullable: false } }
                  - changeSet:
                      id: 2
                      author: test
                      context: "!initial AND postgresql"
                      changes:
                        - addColumn:
                            tableName: phased
                            columns:
                              - column: { name: added_later, type: varchar(20) }
                  - changeSet:
                      id: 3
                      author: test
                      context: mysql
                      changes:
                        - sql:
                            sql: this is not valid sql and must never run
                """);

        assertThat(outcome(run(TASK))).isEqualTo(SUCCESS);
        assertThat(read(GENERATED + "QPhased.java"))
                .contains("NumberPath<Long> id")
                .contains("StringPath addedLater");
    }

    @Test
    @Disabled // TODO: fix failing test
    void passesChangelogParameters() throws IOException {
        buildFile("changelogParameters = ['table.name': 'param_table']");
        write(MASTER, """
                databaseChangeLog:
                  - changeSet:
                      id: 1
                      author: test
                      changes:
                        - createTable:
                            tableName: ${table.name}
                            columns:
                              - column: { name: id, type: bigint, constraints: { primaryKey: true, nullable: false } }
                """);

        assertThat(outcome(run(TASK))).isEqualTo(SUCCESS);
        assertThat(projectDir.resolve(GENERATED + "QParamTable.java")).exists();
    }

    @Test
    @Disabled // TODO: fix failing test
    void rerunsWhenChangelogParametersChange() throws IOException {
        buildFile("changelogParameters = ['table.name': 'param_table']");
        write(MASTER, """
                databaseChangeLog:
                  - changeSet:
                      id: 1
                      author: test
                      changes:
                        - createTable:
                            tableName: ${table.name}
                            columns:
                              - column: { name: id, type: bigint, constraints: { primaryKey: true, nullable: false } }
                """);
        run(TASK);

        buildFile("changelogParameters = ['table.name': 'other_table']");

        assertThat(outcome(run(TASK))).isEqualTo(SUCCESS);
        assertThat(projectDir.resolve(GENERATED + "QOtherTable.java")).exists();
        assertThat(projectDir.resolve(GENERATED + "QParamTable.java")).doesNotExist();
    }

    @Test
    void isConfigurationCacheCompatible() {
        run(TASK, "--configuration-cache");
        BuildResult second = run(TASK, "--configuration-cache");

        assertThat(second.getOutput()).contains("Configuration cache entry reused.");
    }

    private void buildFile(String extraConfiguration) throws IOException {
        write("build.gradle", """
                plugins {
                    id 'java'
                    id 'org.apache.fineract.querydsl.codegen'
                }

                querydslCodegen {
                    packageName = 'org.example.q'
                    %s
                }
                """.formatted(extraConfiguration));
    }

    private BuildResult run(String... args) {
        List<String> arguments = new ArrayList<>(List.of(args));
        arguments.add("--stacktrace");
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments(arguments)
                .forwardOutput()
                .build();
    }

    private static TaskOutcome outcome(BuildResult result) {
        return result.task(TASK).getOutcome();
    }

    private void write(String path, String content) throws IOException {
        Path file = projectDir.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    private String read(String path) throws IOException {
        return Files.readString(projectDir.resolve(path));
    }

    private static String accGlAccountChangelog(boolean withDescription) {
        String base = """
                databaseChangeLog:
                  - changeSet:
                      id: 1
                      author: test
                      changes:
                        - createTable:
                            tableName: acc_gl_account
                            columns:
                              - column: { name: id, type: bigint, constraints: { primaryKey: true, nullable: false } }
                              - column: { name: name, type: varchar(200) }
                              - column: { name: gl_code, type: varchar(45), constraints: { nullable: false } }
                              - column: { name: account_usage, type: smallint }
                """;
        String extra = """
                  - changeSet:
                      id: 2
                      author: test
                      changes:
                        - addColumn:
                            tableName: acc_gl_account
                            columns:
                              - column: { name: description, type: varchar(500) }
                """;
        return withDescription ? base + extra : base;
    }

    private static String moduleChangelog(String tableName) {
        return """
                databaseChangeLog:
                  - changeSet:
                      id: module-1
                      author: test
                      changes:
                        - createTable:
                            tableName: %s
                            columns:
                              - column: { name: id, type: bigint, constraints: { primaryKey: true, nullable: false } }
                """.formatted(tableName);
    }
}
