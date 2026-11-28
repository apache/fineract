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

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Coding conventions for resource methods that take a tenantId path parameter.
 */
class TenantPathParameterConventionTest {

    private static final String BASE = "org.apache.fineract";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BASE);
    }

    /**
     * Resource methods taking a tenantId path parameter must call assertCurrentTenant.
     */
    @Test
    void tenantScopedResourceMethodsMustCallTheTenantCheck() {
        List<String> violations = new ArrayList<>();
        int checked = 0;
        for (JavaClass javaClass : classes) {
            for (JavaMethod method : javaClass.getMethods()) {
                if (!hasTenantIdPathParam(method)) {
                    continue;
                }
                checked++;
                boolean callsCheck = method.getMethodCallsFromSelf().stream()
                        .anyMatch(call -> "assertCurrentTenant".equals(call.getName()));
                if (!callsCheck) {
                    violations.add(method.getFullName());
                }
            }
        }
        assertThat(checked).as("number of tenantId path parameter methods found").isGreaterThan(0);
        assertThat(violations).as("methods with a tenantId path parameter that do not call assertCurrentTenant").isEmpty();
    }

    private static boolean hasTenantIdPathParam(JavaMethod method) {
        for (Set<? extends JavaAnnotation<?>> annotations : method.getParameterAnnotations()) {
            for (JavaAnnotation<?> annotation : annotations) {
                if ("jakarta.ws.rs.PathParam".equals(annotation.getRawType().getName())
                        && "tenantId".equals(annotation.get("value").orElse(null))) {
                    return true;
                }
            }
        }
        return false;
    }
}
