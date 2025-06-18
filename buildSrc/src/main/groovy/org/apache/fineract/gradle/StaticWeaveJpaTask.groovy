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
package org.apache.fineract.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.StandardCopyOption

@CacheableTask
abstract class StaticWeaveJpaTask extends DefaultTask {

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    final DirectoryProperty inputClasses = project.objects.directoryProperty()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    final RegularFileProperty persistenceXml = project.objects.fileProperty()

    @OutputDirectory
    final DirectoryProperty outputDir = project.objects.directoryProperty()

    @TaskAction
    void weave() {
        File weavingRoot = new File(temporaryDir, "static-weaving")
        File metaInf = new File(weavingRoot, "META-INF")
        metaInf.mkdirs()
        Files.copy(persistenceXml.get().asFile.toPath(), new File(metaInf, "persistence.xml").toPath(), StandardCopyOption.REPLACE_EXISTING)

        project.javaexec {
            mainClass.set("org.eclipse.persistence.tools.weaving.jpa.StaticWeave")
            classpath = project.sourceSets.main.runtimeClasspath
            args = [
                    "-persistenceinfo", weavingRoot.absolutePath,
                    inputClasses.get().asFile.absolutePath,
                    outputDir.get().asFile.absolutePath
            ]
        }
    }
}
