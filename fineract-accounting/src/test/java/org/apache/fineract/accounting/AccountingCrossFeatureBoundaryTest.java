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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
 * Cross-feature boundary check for the accounting feature, using Spring Modulith.
 * <p>
 * The model is built with base {@code org.apache.fineract} so every other top-level Fineract area on the classpath is
 * visible. A referenced type is "allowed foundation" when it physically lives in {@code fineract-core} or
 * {@code fineract-command} — the two modules the outline lets every feature depend on. Crucially, this is decided by
 * the <em>artifact the referenced type is loaded from</em>, not by its package name: {@code fineract-core} contains
 * shared types under {@code infrastructure.*} but also under {@code organisation.*}, {@code portfolio.*}, etc.
 * (DTOs/enums that were relocated to core). A package-name check would wrongly flag those; an artifact check does not.
 * <p>
 * The owning artifact is read from each type's source location (ArchUnit records where every class was imported from).
 * When accounting is built, {@code fineract-core} and {@code fineract-command} are on the classpath as jars, so a core
 * type resolves to a URI containing {@code fineract-core} and a command type to one containing
 * {@code fineract-command}. Anything loaded from any other {@code fineract-*} artifact is a genuine cross-feature
 * dependency and is reported as a violation, to be fixed at the source (move the shared type into core, or reach the
 * feature via a core read-contract / command / event / by-id reference) rather than by widening an allow-list.
 * <p>
 * Because this test lives in the {@code fineract-accounting} module, it runs with only accounting and its own
 * dependencies on the classpath, so it reports exactly the cross-feature edges that originate in the
 * {@code fineract-accounting} jar (the work for FINERACT-2646).
 */
class AccountingCrossFeatureBoundaryTest {

    private static final Logger LOG = LoggerFactory.getLogger(AccountingCrossFeatureBoundaryTest.class);

    private static final String BASE = "org.apache.fineract";
    private static final String ACCOUNTING_PACKAGE = "org.apache.fineract.accounting";

    /**
     * The only artifacts every feature may depend on (outline: fineract-core + fineract-command). Membership is tested
     * against the referenced type's NORMALISED owning artifact (exact set match), so a type counts as foundation purely
     * by living in one of these jars/modules, whatever its package happens to be.
     */
    private static final Set<String> FOUNDATION_ARTIFACTS = Set.of("fineract-core", "fineract-command");

    // Match a real module path segment: "fineract-..." that begins right after a "/". This deliberately does
    // NOT match the parent checkout folder (e.g. "apache-fineract-modulith"), where "fineract-modulith" is
    // preceded by "apache-", not "/", and would otherwise become the first match for every type.
    private static final Pattern FINERACT_ARTIFACT = Pattern.compile("(?<=/)fineract-[a-z0-9-]+");

    // Built lazily (not in a static initializer) so the one-time, memory-heavy classpath scan surfaces any
    // failure as a normal test failure rather than a class-initialization error.
    private static ApplicationModules modules;

    private static ApplicationModules modules() {
        if (modules == null) {
            modules = ApplicationModules.of(BASE);
        }
        return modules;
    }

    private static ApplicationModule accountingModule() {
        return modules().stream() //
                .filter(module -> module.getBasePackage().getName().equals(ACCOUNTING_PACKAGE)) //
                .findFirst() //
                .orElseThrow(() -> new IllegalStateException("Accounting module not found in the model"));
    }

    /**
     * Granular feature key for a referenced type: the first two package segments under the base, so
     * {@code org.apache.fineract.portfolio.charge.domain.Charge} becomes {@code portfolio.charge}. Used only to label
     * violations in a readable way; it never decides whether something is allowed.
     */
    private static String featureKey(String typeName) {
        String prefix = BASE + ".";
        if (!typeName.startsWith(prefix)) {
            return typeName;
        }
        String[] parts = typeName.substring(prefix.length()).split("\\.");
        return parts.length >= 2 ? parts[0] + "." + parts[1] : parts[0];
    }

    /**
     * The {@code fineract-*} artifact a referenced type was loaded from, derived from its source URI (e.g.
     * {@code .../fineract-core/build/libs/fineract-core-1.15.0-SNAPSHOT.jar!/...} -> {@code fineract-core}). Takes the
     * LAST {@code /fineract-*} path segment (the deepest module dir) and strips the trailing version. Returns
     * {@code (unknown-source)} if the location can't be determined.
     */
    private static String owningArtifact(JavaClass type) {
        return type.getSource() //
                .map(source -> source.getUri().toString()) //
                .map(uri -> {
                    Matcher matcher = FINERACT_ARTIFACT.matcher(uri);
                    String artifact = null;
                    while (matcher.find()) {
                        artifact = matcher.group();
                    }
                    return artifact == null ? uri : artifact.replaceAll("-\\d.*$", "");
                }) //
                .orElse("(unknown-source)");
    }

    /**
     * A referenced type is foundation iff it was loaded from fineract-core or fineract-command. The decision is an
     * exact match against the normalised owning artifact, not a substring of the raw URI, so siblings such as
     * {@code fineract-command-jdbc} are not mistaken for foundation. If the source location is unknown,
     * {@code owningArtifact} returns a sentinel not in the set, so the type surfaces as NOT foundation rather than
     * silently passing.
     */
    private static boolean isFoundation(JavaClass type) {
        return FOUNDATION_ARTIFACTS.contains(owningArtifact(type));
    }

    /**
     * The "report": prints, per accounting source type, each referenced feature package together with the artifact it
     * lives in and whether that counts as foundation. Always passes; its job is to make it obvious why any given edge
     * is or isn't a violation, and to locate the source type responsible.
     */
    @Test
    void printAccountingCrossFeatureDependencyReport() {
        Map<String, Set<String>> sourceTypeToTargets = new TreeMap<>();
        Set<String> violationFeatures = new TreeSet<>();
        Set<String> allowedFromCore = new TreeSet<>();

        accountingModule().getDirectDependencies(modules()).stream().forEach((ApplicationModuleDependency dependency) -> {
            JavaClass targetType = dependency.getTargetType();
            String sourceType = dependency.getSourceType().getName();
            String featureKey = featureKey(targetType.getName());
            String artifact = owningArtifact(targetType);
            boolean foundation = isFoundation(targetType);

            String label = featureKey + "  [" + artifact + (foundation ? " : foundation]" : " : VIOLATION]");
            sourceTypeToTargets.computeIfAbsent(sourceType, key -> new TreeSet<>()).add(label);

            if (foundation) {
                allowedFromCore.add(featureKey + " (" + artifact + ")");
            } else {
                violationFeatures.add(featureKey + " (" + artifact + ")");
            }
        });

        LOG.info("==== Accounting cross-feature dependency report (base = {}) ====", BASE);
        LOG.info("-- source type -> referenced feature packages [owning artifact : status] --");
        sourceTypeToTargets.forEach((source, targets) -> LOG.info("  " + source + "  ->  " + targets));
        LOG.info("-- allowed (in fineract-core / fineract-command) --");
        LOG.info("  " + allowedFromCore);
        LOG.info("-- VIOLATIONS (in some other fineract-* module) --");
        LOG.info("  " + violationFeatures);
    }

    /**
     * The check: accounting may reference types only from fineract-core and fineract-command. Any referenced type
     * loaded from a different feature module is a violation, labelled by its feature package, and fixed at the source
     * per the outline.
     */
    @Test
    void accountingMustNotImportOtherFeatureModules() {
        Set<String> featureDependencies = new TreeSet<>();

        accountingModule().getDirectDependencies(modules()).stream().forEach((ApplicationModuleDependency dependency) -> {
            JavaClass targetType = dependency.getTargetType();
            if (!isFoundation(targetType)) {
                featureDependencies.add(featureKey(targetType.getName()) + " (" + owningArtifact(targetType) + ")");
            }
        });

        assertThat(featureDependencies) //
                .as("Accounting may depend only on fineract-core and fineract-command. Types referenced from "
                        + "other feature modules must be reached via core read-contracts / DTOs, command/event "
                        + "boundaries, or by-id references (or the shared type moved into core). Offending "
                        + "feature packages (with owning artifact): %s", featureDependencies) //
                .isEmpty();
    }
}
