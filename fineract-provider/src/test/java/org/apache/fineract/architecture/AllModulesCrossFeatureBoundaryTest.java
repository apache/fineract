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

import com.google.common.base.Splitter;
import com.tngtech.archunit.core.domain.JavaClass;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModuleDependency;
import org.springframework.modulith.core.ApplicationModules;

class AllModulesCrossFeatureBoundaryTest {

    private static final Logger LOG = LoggerFactory.getLogger(AllModulesCrossFeatureBoundaryTest.class);

    private static final String BASE = "org.apache.fineract";

    private static final Set<String> FOUNDATION_ARTIFACTS = Set.of("fineract-core", "fineract-command");

    private static final Pattern FINERACT_ARTIFACT = Pattern.compile("(?<=/)fineract-[a-z0-9-]+");

    private static final Pattern ARTIFACT_VERSION_SUFFIX = Pattern.compile("-\\d.*$");

    private static ApplicationModules modules() {
        return ModulesHolder.MODULES;
    }

    private static String featureKey(String typeName) {
        String prefix = BASE + ".";
        if (!typeName.startsWith(prefix)) {
            return typeName;
        }
        List<String> parts = Splitter.on('.').splitToList(typeName.substring(prefix.length()));
        return parts.size() >= 2 ? parts.get(0) + "." + parts.get(1) : parts.get(0);
    }

    private static String owningArtifact(JavaClass type) {
        return type.getSource() //
                .map(source -> source.getUri().toString()) //
                .map(uri -> {
                    Matcher matcher = FINERACT_ARTIFACT.matcher(uri);
                    String artifact = null;
                    while (matcher.find()) {
                        artifact = matcher.group();
                    }
                    return artifact == null ? uri : ARTIFACT_VERSION_SUFFIX.matcher(artifact).replaceAll("");
                }) //
                .orElse("(unknown-source)");
    }

    private static boolean isFoundation(JavaClass type) {
        return FOUNDATION_ARTIFACTS.contains(owningArtifact(type));
    }

    @Test
    @EnabledIfSystemProperty(named = "fineract.modulith.report", matches = "true")
    void printAllModulesCrossFeatureDependencyReport() {
        Map<String, ApplicationModule> sortedModules = new TreeMap<>();
        modules().forEach(module -> sortedModules.put(module.getBasePackage().getName(), module));

        int totalViolations = 0;
        int cleanModules = 0;

        for (Map.Entry<String, ApplicationModule> moduleEntry : sortedModules.entrySet()) {
            String moduleName = moduleEntry.getKey();
            ApplicationModule module = moduleEntry.getValue();

            Map<String, Set<String>> sourceTypeToTargets = new TreeMap<>();
            Set<String> violationFeatures = new TreeSet<>();
            Set<String> allowedFromCore = new TreeSet<>();

            module.getDirectDependencies(modules()).stream().forEach((ApplicationModuleDependency dependency) -> {
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

            LOG.info("==== {} cross-feature dependency report (base = {}) ====", moduleName, BASE);
            LOG.info("-- source type -> referenced feature packages [owning artifact : status] --");
            if (sourceTypeToTargets.isEmpty()) {
                LOG.info("  no outgoing dependencies");
            } else {
                sourceTypeToTargets.forEach((source, targets) -> LOG.info("  dependency {} -> {}", source, targets));
            }
            LOG.info("-- allowed (in fineract-core / fineract-command) --");
            LOG.info("  allowed features: {}", allowedFromCore);
            LOG.info("-- VIOLATIONS (in some other fineract-* module) --");
            LOG.info("  violation features: {}", violationFeatures);

            totalViolations += violationFeatures.size();
            if (violationFeatures.isEmpty()) {
                cleanModules++;
            }
        }

        LOG.info("==== SUMMARY ====");
        LOG.info("modules total        : {}", sortedModules.size());
        LOG.info("clean modules        : {}", cleanModules);
        LOG.info("modules with issues  : {}", sortedModules.size() - cleanModules);
        LOG.info("total violations     : {}", totalViolations);

        // Assertions are placed after the logging so the full report is always printed, including on failure.
        assertThat(sortedModules).as("no application modules were discovered, so the report analysed nothing").isNotEmpty();

        assertThat(totalViolations) //
                .as("Every module may depend only on fineract-core and fineract-command, so the expected number of "
                        + "cross-feature violations is 0 but %d were found. Until the modularisation is complete this "
                        + "assertion is expected to fail; the report logged above lists every offending module.", totalViolations) //
                .isZero();
    }

    @Test
    @EnabledIfSystemProperty(named = "fineract.modulith.report", matches = "true")
    void printModuleToModuleDependencyViolations() {
        Map<String, ApplicationModule> sortedModules = new TreeMap<>();
        modules().forEach(module -> sortedModules.put(module.getBasePackage().getName(), module));

        LOG.info("==== Module-to-module cross-feature dependency violations (base = {}) ====", BASE);
        LOG.info("Rule: a module may depend only on fineract-core and fineract-command.");
        LOG.info("Each line: SOURCE module -> TARGET module (edge count) [offending feature packages].");

        int totalCrossModuleEdges = 0;
        int modulesWithCrossModuleDeps = 0;

        for (Map.Entry<String, ApplicationModule> moduleEntry : sortedModules.entrySet()) {
            String moduleName = moduleEntry.getKey();
            ApplicationModule module = moduleEntry.getValue();

            Map<String, Integer> targetModuleEdgeCount = new TreeMap<>();
            Map<String, Set<String>> targetModuleFeatures = new TreeMap<>();

            module.getDirectDependencies(modules()).stream().forEach((ApplicationModuleDependency dependency) -> {
                JavaClass targetType = dependency.getTargetType();
                if (!isFoundation(targetType)) {
                    String targetModule = owningArtifact(targetType);
                    targetModuleEdgeCount.merge(targetModule, 1, Integer::sum);
                    targetModuleFeatures.computeIfAbsent(targetModule, key -> new TreeSet<>()).add(featureKey(targetType.getName()));
                }
            });

            if (targetModuleEdgeCount.isEmpty()) {
                LOG.info("MODULE {}  ->  no cross-module violations", moduleName);
            } else {
                LOG.info("MODULE {}  ->  depends on {} other module(s):", moduleName, targetModuleEdgeCount.size());
                for (Map.Entry<String, Integer> target : targetModuleEdgeCount.entrySet()) {
                    String targetModule = target.getKey();
                    int edges = target.getValue();
                    LOG.info("    target module {} with {} edge(s): {}", targetModule, edges, targetModuleFeatures.get(targetModule));
                    totalCrossModuleEdges += edges;
                }
                modulesWithCrossModuleDeps++;
            }
        }

        LOG.info("==== SUMMARY ====");
        LOG.info("modules total                 : {}", sortedModules.size());
        LOG.info("modules with cross-module deps : {}", modulesWithCrossModuleDeps);
        LOG.info("total cross-module class edges : {}", totalCrossModuleEdges);
    }

    @Test
    @EnabledIfSystemProperty(named = "fineract.modulith.report", matches = "true")
    void noModuleMayDependOnAnotherFeatureModule() {
        Map<String, Set<String>> offending = new TreeMap<>();

        modules().forEach(module -> {
            Set<String> featureDependencies = new TreeSet<>();
            module.getDirectDependencies(modules()).stream().forEach((ApplicationModuleDependency dependency) -> {
                JavaClass targetType = dependency.getTargetType();
                if (!isFoundation(targetType)) {
                    featureDependencies.add(featureKey(targetType.getName()) + " (" + owningArtifact(targetType) + ")");
                }
            });
            if (!featureDependencies.isEmpty()) {
                offending.put(module.getBasePackage().getName(), featureDependencies);
            }
        });

        assertThat(offending) //
                .as("Every module may depend only on fineract-core and fineract-command. Types referenced from "
                        + "other feature modules must be reached via core read-contracts / DTOs, command/event "
                        + "boundaries, or by-id references (or the shared type moved into core). Offending modules "
                        + "(each with its offending feature packages and owning artifact): %s", offending) //
                .isEmpty();
    }

    private static final class ModulesHolder {

        private static final ApplicationModules MODULES = ApplicationModules.of(BASE);
    }
}
