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
package org.apache.fineract.portfolio.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
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

class ClientCrossFeatureBoundaryTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientCrossFeatureBoundaryTest.class);

    private static final String BASE = "org.apache.fineract";
    private static final String CLIENT_PACKAGE = "org.apache.fineract.portfolio.client";

    private static final Set<String> FOUNDATION_ARTIFACTS = Set.of("fineract-core", "fineract-command", "fineract-validation");

    private static final Pattern FINERACT_ARTIFACT = Pattern.compile("fineract-[a-z0-9-]+");

    private static ApplicationModules modules;

    private static ApplicationModules modules() {
        if (modules == null) {
            modules = ApplicationModules.of(BASE);
        }
        return modules;
    }

    private static ApplicationModule clientModule() {
        return modules().stream() //
                .filter(module -> module.getBasePackage().getName().equals(CLIENT_PACKAGE)) //
                .findFirst() //
                .orElseThrow(() -> new IllegalStateException("Client module not found in the model"));
    }

    private static String featureKey(String typeName) {
        String prefix = BASE + ".";
        if (!typeName.startsWith(prefix)) {
            return typeName;
        }
        String remainder = typeName.substring(prefix.length());
        int firstDot = remainder.indexOf('.');
        if (firstDot < 0) {
            return remainder;
        }
        int secondDot = remainder.indexOf('.', firstDot + 1);
        return secondDot < 0 ? remainder : remainder.substring(0, secondDot);
    }

    private static String owningArtifact(JavaClass type) {
        return type.getSource() //
                .map(source -> source.getUri().toString()) //
                .map(uri -> {
                    Matcher matcher = FINERACT_ARTIFACT.matcher(uri);
                    return matcher.find() ? matcher.group() : uri;
                }) //
                .orElse("(unknown-source)");
    }

    private static boolean isFoundation(JavaClass type) {
        return type.getSource() //
                .map(source -> source.getUri().toString()) //
                .map(uri -> FOUNDATION_ARTIFACTS.stream().anyMatch(uri::contains)) //
                .orElse(false);
    }

    @EnabledIfSystemProperty(named = "client.boundary.report", matches = "true")
    @Test
    void printClientCrossFeatureDependencyReport() {
        Map<String, Set<String>> sourceTypeToTargets = new TreeMap<>();
        Set<String> violationFeatures = new TreeSet<>();
        Set<String> allowedFromCore = new TreeSet<>();

        clientModule().getDirectDependencies(modules()).stream().forEach((ApplicationModuleDependency dependency) -> {
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

        LOG.info("==== Client cross-feature dependency report ====");
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
    void clientMustNotImportOtherFeatureModules() {
        Set<String> featureDependencies = new TreeSet<>();

        clientModule().getDirectDependencies(modules()).stream().forEach((ApplicationModuleDependency dependency) -> {
            JavaClass targetType = dependency.getTargetType();
            if (!isFoundation(targetType)) {
                featureDependencies.add(featureKey(targetType.getName()) + " (" + owningArtifact(targetType) + ")");
            }
        });

        assertThat(featureDependencies) //
                .as("Client may depend only on fineract-core and fineract-command. Types referenced from other "
                        + "feature modules must be reached via core read-contracts / DTOs, command/event "
                        + "boundaries, or by-id references (or the shared type moved into core). Offending "
                        + "feature packages (with owning artifact): %s", featureDependencies) //
                .isEmpty();
    }
}
