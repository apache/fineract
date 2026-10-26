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
package org.apache.fineract.accounting;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModuleDependency;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Checks that the accounting module references types from fineract-core and fineract-command only, and not from other
 * feature modules such as fineract-charge or fineract-loan.
 * <p>
 * Spring Modulith models the {@code org.apache.fineract} classes on this module's test classpath, and each type that
 * accounting references is attributed to the Gradle module it was loaded from. Only fineract-accounting's own
 * dependencies are on that classpath, so the test can only catch a feature module that is added back to
 * {@code fineract-accounting/dependencies.gradle} and then used from accounting code. It does not check dependencies of
 * other modules, including dependencies pointing into accounting.
 */
class AccountingCrossFeatureBoundaryTest {

    private static final Logger log = LoggerFactory.getLogger(AccountingCrossFeatureBoundaryTest.class);

    private static final String BASE = "org.apache.fineract";
    private static final String ACCOUNTING_PACKAGE = "org.apache.fineract.accounting";

    private static final Set<String> FOUNDATION_ARTIFACTS = Set.of("fineract-core", "fineract-command");

    private static final Pattern FINERACT_ARTIFACT = Pattern.compile("(?<=/)fineract-[a-z0-9-]+");

    // Built once during class initialization, which the JVM makes thread-safe for parallel test execution
    private static final ApplicationModules MODULES = ApplicationModules.of(BASE);

    @Test
    void accountingMustNotImportOtherFeatureModules() {
        Set<String> violations = new TreeSet<>();
        Set<String> unresolvedTypes = new TreeSet<>();

        accountingModule().getDirectDependencies(MODULES).stream().forEach((ApplicationModuleDependency dependency) -> {
            JavaClass targetType = dependency.getTargetType();
            Optional<String> artifact = owningArtifact(targetType);
            if (artifact.isEmpty()) {
                unresolvedTypes.add(targetType.getName());
            } else if (!FOUNDATION_ARTIFACTS.contains(artifact.get())) {
                violations.add(dependency.getSourceType().getName() + " -> " + targetType.getName() + " (" + artifact.get() + ")");
            }
        });

        if (!unresolvedTypes.isEmpty()) {
            log.warn("Owning module could not be determined, so these types were not checked: {}", unresolvedTypes);
        }

        assertThat(violations) //
                .as("Accounting may depend only on fineract-core and fineract-command. Types referenced from other feature modules "
                        + "must be reached via core read-contracts / DTOs, command/event boundaries, or by-id references (or the "
                        + "shared type moved into core). Offending dependencies (source -> target (owning module)): %s", violations) //
                .isEmpty();
    }

    private static ApplicationModule accountingModule() {
        return MODULES.stream() //
                .filter(module -> module.getBasePackage().getName().equals(ACCOUNTING_PACKAGE)) //
                .findFirst() //
                .orElseThrow(() -> new IllegalStateException("Accounting module not found in the model"));
    }

    /**
     * Resolves the Gradle module a type was loaded from (e.g. {@code fineract-core}) using its class file location.
     * Empty when ArchUnit has no location for the type or the location is not inside a fineract-* module.
     */
    private static Optional<String> owningArtifact(JavaClass type) {
        return type.getSource().map(source -> source.getUri().toString()).flatMap(uri -> {
            Matcher matcher = FINERACT_ARTIFACT.matcher(uri);
            String artifact = null;
            while (matcher.find()) {
                artifact = matcher.group();
            }
            // Jar names carry a version (fineract-core-1.12.0.jar), build directories do not
            return Optional.ofNullable(artifact).map(name -> name.replaceAll("-\\d.*$", ""));
        });
    }
}
