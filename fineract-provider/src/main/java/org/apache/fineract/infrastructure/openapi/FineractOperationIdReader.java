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
package org.apache.fineract.infrastructure.openapi;

import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FineractOperationIdReader extends Reader {

    // Check explicit @Operation ids first, then let Swagger build the spec.
    @Override
    public OpenAPI read(Set<Class<?>> classes, Map<String, Object> resources) {
        ExplicitOperationValidator.validate(classes);
        return super.read(classes, resources);
    }

    static final class ExplicitOperationValidator {

        static void validate(Set<Class<?>> classes) {
            Map<String, List<String>> operationIds = new LinkedHashMap<>();
            for (Class<?> resourceClass : classes) {
                // No @Path means this class is not a JAX-RS resource.
                if (resourceClass.getAnnotation(Path.class) == null) {
                    continue;
                }

                for (Method method : resourceClass.getMethods()) {
                    Operation operation = method.getAnnotation(Operation.class);
                    String operationId = trimToNull(operation == null ? null : operation.operationId());

                    // We only care about actual endpoints that set an id explicitly.
                    if (operationId == null || !hasHttpMethod(method)) {
                        continue;
                    }

                    operationIds.computeIfAbsent(operationId, ignored -> new ArrayList<>())
                            .add(resourceClass.getSimpleName() + "#" + method.getName());

                }
            }

            List<String> duplicates = operationIds.entrySet().stream().filter(e -> e.getValue().size() > 1)
                    .map(e -> e.getKey() + " -> " + String.join(", ", e.getValue())).sorted().toList();
            if (!duplicates.isEmpty()) {
                throw new IllegalStateException(
                        "Duplicate explicit OpenAPI operationIds detected:\n - " + String.join("\n - ", duplicates));
            }
        }
    }

    // GET, POST, etc. are meta-annotated with @HttpMethod.
    private static boolean hasHttpMethod(Method method) {
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation.annotationType().getAnnotation(HttpMethod.class) != null) {
                return true;
            }
        }
        return false;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
