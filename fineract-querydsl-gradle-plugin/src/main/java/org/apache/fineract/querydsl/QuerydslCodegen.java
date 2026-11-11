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

import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.NormalizeLineEndings;
import org.gradle.workers.WorkerExecutor;

@CacheableTask
public abstract class QuerydslCodegen extends DefaultTask {

    @Internal
    public abstract ConfigurableFileCollection getResourceRoots();

    @Input
    public abstract ListProperty<String> getChangelogIncludes();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    @IgnoreEmptyDirectories
    @NormalizeLineEndings
    public FileTree getChangelogFiles() {
        return getResourceRoots().getAsFileTree().matching(p -> p.include(getChangelogIncludes().get()));
    }

    @Input
    public abstract Property<String> getChangelogFile();

    @Input
    public abstract ListProperty<String> getContextRuns();

    @Input
    public abstract MapProperty<String, String> getChangelogParameters();

    @Input
    public abstract Property<String> getPackageName();

    @Input
    public abstract Property<String> getSchema();

    @Input
    public abstract Property<String> getPostgresImage();

    @Input
    public abstract MapProperty<String, String> getTypeMappings();

    @Classpath
    public abstract ConfigurableFileCollection getCodegenClasspath();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @TaskAction
    public void generate() {
        getFileSystemOperations().delete(spec -> spec.delete(getOutputDirectory()));

        getWorkerExecutor()
                .processIsolation(spec -> {
                    spec.getClasspath().from(getCodegenClasspath());
                    // Liquibase reads changelog parameters from system properties
                    spec.forkOptions(o -> o.systemProperties(getChangelogParameters().get()));
                })
                .submit(QuerydslCodegenAction.class, p -> {
                    p.getResourceRoots().from(getResourceRoots());
                    p.getChangelogFile().set(getChangelogFile());
                    p.getContextRuns().set(getContextRuns());
                    p.getPackageName().set(getPackageName());
                    p.getSchema().set(getSchema());
                    p.getPostgresImage().set(getPostgresImage());
                    p.getTypeMappings().set(getTypeMappings());
                    p.getOutputDirectory().set(getOutputDirectory());
                });
    }
}
