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
package org.apache.fineract.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Coding conventions for outbound clients: webhook HTTP clients and message consumers.
 */
class OutboundClientConventionTest {

    private static final String BASE = "org.apache.fineract";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BASE);
    }

    /**
     * Webhook HTTP clients are built in one place (ProcessorHelper) so that shared client settings stay consistent.
     */
    @Test
    void webhookHttpClientsMustBeBuiltOnlyInProcessorHelper() {
        for (String builder : List.of("okhttp3.OkHttpClient$Builder", "retrofit2.Retrofit$Builder")) {
            ArchRule rule = ArchRuleDefinition.noClasses() //
                    .that().resideInAPackage("org.apache.fineract.infrastructure.hooks..") //
                    .and().doNotHaveFullyQualifiedName("org.apache.fineract.infrastructure.hooks.processor.ProcessorHelper") //
                    .should().callConstructorWhere(constructorOf(builder));
            rule.check(classes);
        }
    }

    private static DescribedPredicate<JavaConstructorCall> constructorOf(String ownerName) {
        return new DescribedPredicate<JavaConstructorCall>("constructor of " + ownerName) {

            @Override
            public boolean test(JavaConstructorCall call) {
                return ownerName.equals(call.getTargetOwner().getName());
            }
        };
    }

    private static final Pattern TRUST_ALL_JMS = Pattern.compile("setTrustAllPackages\\s*\\(\\s*true\\s*\\)");

    private static final Pattern TRUST_ALL_JSON = Pattern.compile("TRUSTED_PACKAGES\\s*,\\s*\"\\*\"");

    private static final Pattern TRUST_ALL_PROPERTY = Pattern.compile("trusted\\.packages\\s*[:=]\\s*\"?\\*");

    // TODO: restrict these to explicit package lists, then remove the entries
    private static final Set<String> TRUST_ALL_JMS_EXCEPTIONS = Set.of(
            "infrastructure/springbatch/messagehandler/jms/JmsBrokerConfiguration.java",
            "infrastructure/event/external/config/ExternalEventJMSConfiguration.java");

    /**
     * Message consumers must list the packages they deserialize instead of accepting every package.
     */
    @Test
    void messageConsumersMustListDeserializablePackages() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : mainFiles("src/main/java", ".java")) {
            String content = Files.readString(file);
            String rel = file.toString().replace('\\', '/');
            if (TRUST_ALL_JMS.matcher(content).find() && TRUST_ALL_JMS_EXCEPTIONS.stream().noneMatch(rel::endsWith)) {
                violations.add(rel + " calls setTrustAllPackages(true)");
            }
            if (TRUST_ALL_JSON.matcher(content).find()) {
                violations.add(rel + " sets TRUSTED_PACKAGES to *");
            }
        }
        for (Path file : mainFiles("src/main/resources", ".properties", ".yml", ".yaml")) {
            if (TRUST_ALL_PROPERTY.matcher(Files.readString(file)).find()) {
                violations.add(file + " sets trusted.packages to *");
            }
        }
        assertThat(violations).isEmpty();
    }

    // ---------------------------------------------------------------- helpers

    private static Path repositoryRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null && !Files.exists(dir.resolve("settings.gradle"))) {
            dir = dir.getParent();
        }
        assertThat(dir).as("repository root containing settings.gradle").isNotNull();
        return dir;
    }

    private static List<Path> mainFiles(String sourceFolder, String... suffixes) throws IOException {
        List<Path> result = new ArrayList<>();
        try (Stream<Path> modules = Files.list(repositoryRoot())) {
            for (Path module : (Iterable<Path>) modules.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("fineract-")
                            && !p.getFileName().toString().startsWith("fineract-e2e-tests"))::iterator) {
                Path source = module.resolve(sourceFolder);
                if (!Files.isDirectory(source)) {
                    continue;
                }
                try (Stream<Path> files = Files.walk(source)) {
                    files.filter(Files::isRegularFile).filter(p -> {
                        String name = p.getFileName().toString();
                        for (String suffix : suffixes) {
                            if (name.endsWith(suffix)) {
                                return true;
                            }
                        }
                        return false;
                    }).forEach(result::add);
                }
            }
        }
        return result;
    }
}
