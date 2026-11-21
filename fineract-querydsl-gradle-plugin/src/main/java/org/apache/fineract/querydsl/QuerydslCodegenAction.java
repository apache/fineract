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

import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.codegen.MetaDataExporter;
import com.querydsl.sql.codegen.MetadataExporterConfigImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class QuerydslCodegenAction implements WorkAction<QuerydslCodegenAction.Parameters> {

    public interface Parameters extends WorkParameters {

        ConfigurableFileCollection getResourceRoots();

        Property<String> getChangelogFile();

        ListProperty<String> getContextRuns();

        Property<String> getPackageName();

        Property<String> getSchema();

        Property<String> getPostgresImage();

        MapProperty<String, String> getTypeMappings();

        DirectoryProperty getOutputDirectory();
    }

    @Override
    public void execute() {
        var p = getParameters();
        var image = DockerImageName.parse(p.getPostgresImage().get()).asCompatibleSubstituteFor("postgres");

        try (var postgres = new PostgreSQLContainer<>(image)) {
            postgres.start();

            migrate(p, postgres);

            try (var connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(),
                    postgres.getPassword())) {
                try (var statement = connection.createStatement()) {
                    statement.execute("drop table if exists databasechangelog, databasechangeloglock");
                }
                export(p, connection);
            }
        } catch (Exception e) {
            throw new IllegalStateException("QueryDSL SQL code generation failed", e);
        }
    }

    private static void migrate(Parameters p, PostgreSQLContainer<?> postgres) throws Exception {
        var accessors = new ArrayList<ResourceAccessor>();

        for (var root : p.getResourceRoots().getFiles()) {
            if (root.isDirectory()) {
                accessors.add(new DirectoryResourceAccessor(root));
            }
        }
        if (accessors.isEmpty()) {
            throw new IllegalStateException("None of the configured resource roots exists: " + p.getResourceRoots().getFiles());
        }

        var runs = p.getContextRuns().get().isEmpty() ? List.of("") : p.getContextRuns().get();

        try (var resourceAccessor = new CompositeResourceAccessor(accessors.toArray(ResourceAccessor[]::new))) {
            Scope.child(Map.of(Scope.Attr.resourceAccessor.name(), resourceAccessor), () -> {
                for (String contexts : runs) {
                    var update = new CommandScope("update")
                            .addArgumentValue("url", postgres.getJdbcUrl())
                            .addArgumentValue("username", postgres.getUsername())
                            .addArgumentValue("password", postgres.getPassword())
                            .addArgumentValue("changelogFile", p.getChangelogFile().get());
                    if (!contexts.isBlank()) {
                        update.addArgumentValue("contexts", contexts);
                    }
                    update.execute();
                }
            });
        }
    }

    private static void export(Parameters p, Connection connection) throws Exception {
        var configuration = new Configuration(PostgreSQLTemplates.builder().build());

        for (Map.Entry<String, String> mapping : p.getTypeMappings().get().entrySet()) {
            configuration.registerType(mapping.getKey(), Class.forName(mapping.getValue()));
        }

        var exporterConfig = new MetadataExporterConfigImpl();
        exporterConfig.setPackageName(p.getPackageName().get());
        exporterConfig.setTargetFolder(p.getOutputDirectory().get().getAsFile());
        exporterConfig.setSchemaPattern(p.getSchema().get());
        exporterConfig.setNamePrefix("Q");

        var exporter = new MetaDataExporter(exporterConfig);
        exporter.setConfiguration(configuration);
        exporter.export(connection.getMetaData());
    }
}
