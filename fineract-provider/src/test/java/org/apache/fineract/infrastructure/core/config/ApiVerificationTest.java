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
package org.apache.fineract.infrastructure.core.config;

import jakarta.ws.rs.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.AbstractSpringTest;
import org.apache.fineract.infrastructure.core.jersey.JerseyConfig;
import org.assertj.core.api.SoftAssertions;
import org.glassfish.jersey.server.model.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

public class ApiVerificationTest extends AbstractSpringTest {

    @Autowired
    private JerseyConfig jerseyConfig;

    @Test
    public void testAllApiClassesAreNamedAsApiResource() {
        Set<Class<?>> registeredClasses = jerseyConfig.getClasses();

        SoftAssertions assertions = new SoftAssertions();
        registeredClasses.stream().filter(this::isApi).forEach(apiClass -> verifyApiNaming(apiClass, assertions));
        assertions.assertAll();
    }

    private void verifyApiNaming(Class<?> apiClass, SoftAssertions assertions) {
        String apiClassName = ClassUtils.getUserClass(apiClass).getName();
        String msg = "API class '%s' should have the postfix 'ApiResource'".formatted(apiClassName);
        assertions.assertThat(apiClassName).as(msg).endsWith("ApiResource");
    }

    @Test
    public void testAllApisAreVersioned() {
        Set<Class<?>> registeredClasses = jerseyConfig.getClasses();

        SoftAssertions assertions = new SoftAssertions();
        registeredClasses.stream().filter(this::isApi).forEach(apiClass -> verifyApiIsVersioned(apiClass, assertions));
        assertions.assertAll();
    }

    private boolean isApi(Class<?> c) {
        return getPathAnnotation(c) != null;
    }

    private Path getPathAnnotation(Class<?> c) {
        return AnnotationUtils.findAnnotation(c, Path.class);
    }

    private void verifyApiIsVersioned(Class<?> apiClass, SoftAssertions assertions) {
        Resource apiResource = Resource.from(apiClass);
        String basePath = apiResource.getPath();
        Set<String> paths = apiResource.getChildResources() //
                .stream() //
                .map(Resource::getPath) //
                .map(cPath -> normalizePath(basePath, cPath)) //
                .collect(Collectors.toSet()); //
        paths.add(normalizePath(basePath, null));
        paths.forEach(fullPath -> {
            String msg = "%s has a non versioned API on path %s".formatted(apiClass, fullPath);
            assertions.assertThat(fullPath).as(msg).matches("^/(v[1-9][0-9]*/).*$");
        });
    }

    private String normalizePath(String prefix, String path) {
        String basePath = prefix;
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        if (path == null) {
            return basePath;
        }
        if (basePath.endsWith("/")) {
            return path.startsWith("/") ? basePath + path.substring(1) : basePath + path;
        }
        return path.startsWith("/") ? basePath + path : basePath + "/" + path;
    }
}
