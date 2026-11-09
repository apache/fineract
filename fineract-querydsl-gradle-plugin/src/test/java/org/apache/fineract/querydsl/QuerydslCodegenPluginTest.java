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

import java.io.File;
import java.util.List;
import java.util.Map;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class QuerydslCodegenPluginTest {

    private Project project;

    @BeforeEach
    void setUp(@TempDir File projectDir) {
        project = ProjectBuilder.builder().withProjectDir(projectDir).build();
    }

    @Test
    void doesNothingWithoutJavaPlugin() {
        project.getPluginManager().apply(QuerydslCodegenPlugin.class);

        assertThat(project.getExtensions().findByName("querydslCodegen")).isNull();
        assertThat(project.getTasks().findByName("querydslCodegen")).isNull();
    }

    @Test
    void worksRegardlessOfPluginOrder() {
        project.getPluginManager().apply(QuerydslCodegenPlugin.class);
        project.getPluginManager().apply("java");

        assertThat(project.getExtensions().findByName("querydslCodegen")).isNotNull();
        assertThat(project.getTasks().findByName("querydslCodegen")).isNotNull();
    }

    @Test
    void extensionHasConventions() {
        QuerydslCodegenExtension ext = applyPlugin();

        // assertThat(ext.getChangelogDirectory().get().getAsFile()).isEqualTo(project.file("src/main/resources/db/changelog"));
        assertThat(ext.getChangelogFile().get()).isEqualTo("db/changelog/db.changelog-master.yaml");
        assertThat(ext.getSchema().get()).isEqualTo("public");
        assertThat(ext.getPostgresImage().get()).isEqualTo("postgres:17-alpine");
        assertThat(ext.getTypeMappings().get()).containsExactlyEntriesOf(Map.of("int2", "java.lang.Integer"));
        assertThat(ext.getOutputDirectory().get().getAsFile()).isEqualTo(project.file("build/generated/sources/querydsl/java/main"));
        assertThat(ext.getPackageName().isPresent()).isFalse();
    }

    @Test
    void taskIsWiredLazilyToExtension() {
        QuerydslCodegenExtension ext = applyPlugin();

        // set after the task was registered: must still reach the task
        ext.getPackageName().set("org.example.q");
        ext.getSchema().set("ledger");
        ext.getChangelogFile().set("master.xml");
        ext.getPostgresImage().set("postgis/postgis:17-3.5");
        ext.getTypeMappings().put("int2", "java.lang.Integer");
        ext.getTypeMappings().put("numeric", "java.math.BigDecimal");

        QuerydslCodegen task = generateTask();

        assertThat(task.getPackageName().get()).isEqualTo("org.example.q");
        assertThat(task.getSchema().get()).isEqualTo("ledger");
        assertThat(task.getChangelogFile().get()).isEqualTo("master.xml");
        assertThat(task.getPostgresImage().get()).isEqualTo("postgis/postgis:17-3.5");
        assertThat(task.getTypeMappings().get()).containsEntry("int2", "java.lang.Integer").containsEntry("numeric",
                "java.math.BigDecimal");
        assertThat(task.getOutputDirectory().get().getAsFile()).isEqualTo(ext.getOutputDirectory().get().getAsFile());
    }

    @Test
    void taskIsInBuildGroupAndCacheable() {
        applyPlugin();
        QuerydslCodegen task = generateTask();

        assertThat(task.getGroup()).isEqualTo("build");
        assertThat(QuerydslCodegen.class.isAnnotationPresent(CacheableTask.class)).isTrue();
    }

    @Test
    void generatedSourcesAreAddedToMainSourceSet() {
        applyPlugin();

        var main = project.getExtensions().getByType(SourceSetContainer.class).getByName("main");

        assertThat(main.getJava().getSrcDirs()).contains(project.file("build/generated/sources/querydsl/java/main"));
    }

    @Test
    void compileJavaDependsOnGeneration() {
        applyPlugin();
        Task compileJava = project.getTasks().getByName("compileJava");

        assertThat(compileJava.getTaskDependencies().getDependencies(compileJava)).extracting(Task::getName).contains("querydslCodegen");
    }

    @Test
    void addsQuerydslSqlToImplementation() {
        applyPlugin();

        assertThat(coordinates("implementation")).contains("io.github.openfeign.querydsl:querydsl-sql:7.7");
    }

    @Test
    void codegenConfigurationHasToolDependencies() {
        applyPlugin();

        assertThat(coordinates("querydslCodegen")).contains("io.github.openfeign.querydsl:querydsl-sql-codegen:7.7",
                "org.liquibase:liquibase-core:4.33.0", "org.testcontainers:postgresql:1.21.4", "org.postgresql:postgresql:42.7.13");
    }

    @Test
    void toolVersionsCanBeOverridden() {
        QuerydslCodegenExtension ext = applyPlugin();
        ext.getQuerydslVersion().set("7.3");
        ext.getLiquibaseVersion().set("4.29.2");

        assertThat(coordinates("implementation")).contains("io.github.openfeign.querydsl:querydsl-sql:7.3");
        assertThat(coordinates("querydslCodegen")).contains("io.github.openfeign.querydsl:querydsl-sql-codegen:7.3",
                "org.liquibase:liquibase-core:4.29.2");
    }

    @Test
    void codegenClasspathIsResolvableAndNotConsumable() {
        applyPlugin();

        var declarable = project.getConfigurations().getByName("querydslCodegen");
        var resolvable = project.getConfigurations().getByName("querydslCodegenClasspath");

        assertThat(declarable.isCanBeResolved()).isFalse();
        assertThat(declarable.isCanBeConsumed()).isFalse();
        assertThat(resolvable.isCanBeResolved()).isTrue();
        assertThat(resolvable.isCanBeConsumed()).isFalse();
        assertThat(resolvable.getExtendsFrom()).contains(declarable);
    }

    private QuerydslCodegenExtension applyPlugin() {
        project.getPluginManager().apply("java");
        project.getPluginManager().apply(QuerydslCodegenPlugin.class);
        return project.getExtensions().getByType(QuerydslCodegenExtension.class);
    }

    private QuerydslCodegen generateTask() {
        return project.getTasks().named("querydslCodegen", QuerydslCodegen.class).get();
    }

    private List<String> coordinates(String configuration) {
        return project.getConfigurations().getByName(configuration).getDependencies().stream().map(QuerydslCodegenPluginTest::gav).toList();
    }

    private static String gav(Dependency d) {
        return d.getGroup() + ":" + d.getName() + ":" + d.getVersion();
    }
}
