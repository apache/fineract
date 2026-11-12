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

import java.util.List;
import java.util.Map;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

public class QuerydslCodegenPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java", ignored -> configure(project));
    }

    private void configure(Project project) {
        var layout = project.getLayout();
        var deps = project.getDependencies();

        var ext = project.getExtensions().create("querydslCodegen", QuerydslCodegenExtension.class);
        ext.getResourceRoots().convention(layout.getProjectDirectory().dir("src/main/resources")); // Gradle 8.8+
        ext.getChangelogFile().convention("db/changelog/db.changelog-master.yaml");
        ext.getChangelogIncludes().convention(List.of("db/**"));
        ext.getContextRuns().convention(List.of());
        ext.getSchema().convention("public");
        ext.getPostgresImage().convention("postgres:17-alpine");
        ext.getTypeMappings().convention(Map.of("int2", "java.lang.Integer"));
        ext.getOutputDirectory().convention(layout.getBuildDirectory().dir("generated/sources/querydsl/java/main"));
        ext.getQuerydslVersion().convention("7.7");
        ext.getLiquibaseVersion().convention("4.33.0");
        ext.getTestcontainersVersion().convention("1.21.4");
        ext.getPostgresDriverVersion().convention("42.7.13");

        var codegen = project.getConfigurations().dependencyScope("querydslCodegen", c -> {
            c.getDependencies()
                    .addLater(ext.getQuerydslVersion().map(v -> deps.create("io.github.openfeign.querydsl:querydsl-sql-codegen:" + v)));
            c.getDependencies().addLater(ext.getLiquibaseVersion().map(v -> deps.create("org.liquibase:liquibase-core:" + v)));
            c.getDependencies().addLater(ext.getTestcontainersVersion().map(v -> deps.create("org.testcontainers:postgresql:" + v)));
            c.getDependencies().addLater(ext.getPostgresDriverVersion().map(v -> deps.create("org.postgresql:postgresql:" + v)));
            c.getDependencies().add(deps.create("org.slf4j:slf4j-simple:2.0.17"));
        });

        var codegenClasspath = project.getConfigurations().resolvable("querydslCodegenClasspath", c -> c.extendsFrom(codegen.get()));

        deps.addProvider("implementation", ext.getQuerydslVersion().map(v -> "io.github.openfeign.querydsl:querydsl-sql:" + v));

        var generate = project.getTasks().register("querydslCodegen", QuerydslCodegen.class, t -> {
            t.setGroup("build");
            t.setDescription("Generates QueryDSL SQL Q-classes from the Liquibase changelogs");
            t.getResourceRoots().from(ext.getResourceRoots());
            t.getChangelogIncludes().set(ext.getChangelogIncludes());
            t.getContextRuns().set(ext.getContextRuns());
            t.getChangelogFile().set(ext.getChangelogFile());
            t.getPackageName().set(ext.getPackageName());
            t.getSchema().set(ext.getSchema());
            t.getPostgresImage().set(ext.getPostgresImage());
            t.getTypeMappings().set(ext.getTypeMappings());
            t.getOutputDirectory().set(ext.getOutputDirectory());
            t.getCodegenClasspath().from(codegenClasspath);
        });

        project.getExtensions().getByType(SourceSetContainer.class).named("main",
                main -> main.getJava().srcDir(generate.flatMap(QuerydslCodegen::getOutputDirectory)));
    }
}
