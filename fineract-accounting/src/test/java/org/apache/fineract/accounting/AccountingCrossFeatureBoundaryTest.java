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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModuleDependency;
import org.springframework.modulith.core.ApplicationModules;

class AccountingCrossFeatureBoundaryTest {

    private static final Logger LOG = LoggerFactory.getLogger(AccountingCrossFeatureBoundaryTest.class);

    private static final String BASE = "org.apache.fineract";
    private static final String ACCOUNTING_PACKAGE = "org.apache.fineract.accounting";

    private static final Set<String> FOUNDATION_ARTIFACTS = Set.of("fineract-core", "fineract-command");

    private static final Pattern FINERACT_ARTIFACT = Pattern.compile("(?<=/)fineract-[a-z0-9-]+");

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

    private static String featureKey(String typeName) {
        String prefix = BASE + ".";
        if (!typeName.startsWith(prefix)) {
            return typeName;
        }
        List<String> parts = Splitter.on('.').splitToList(typeName.substring(prefix.length()));
        return parts.size() >= 2 ? parts.get(0) + "." + parts.get(1) : parts.get(0);
    }

    private static String owningArtifact(JavaClass type) {
        return type.getSource().map(source -> source.getUri().toString()).map(uri -> {
            Matcher matcher = FINERACT_ARTIFACT.matcher(uri);
            String artifact = null;
            while (matcher.find()) {
                artifact = matcher.group();
            }
            return artifact == null ? uri : artifact.replaceAll("-\\d.*$", "");
        }).orElse("(unknown-source)");
    }

    private static boolean isFoundation(JavaClass type) {
        return FOUNDATION_ARTIFACTS.contains(owningArtifact(type));
    }

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

        LOG.info("==== Accounting cross-feature dependency report ====");
        LOG.info("base package         : {}", BASE);
        LOG.info("-- source type -> referenced feature packages [owning artifact : status] --");

        sourceTypeToTargets.forEach((source, targets) -> {
            LOG.info("source type          : {}", source);
            LOG.info("referenced targets   : {}", targets);
        });

        LOG.info("-- allowed dependencies --");
        LOG.info("allowed from core    : {}", allowedFromCore);
        LOG.info("-- dependency violations --");
        LOG.info("violation features   : {}", violationFeatures);
    }

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
