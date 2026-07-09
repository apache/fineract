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
import javax.lang.model.SourceVersion;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;

public final class FineractOperationIdReader extends Reader {

    public static final String ALTERNATIVE_OPERATION_ID_EXTENSION = "x-alternative-operation-id";

    // Check explicit @Operation ids first, then let Swagger build the spec.
    @Override
    public OpenAPI read(Set<Class<?>> classes, Map<String, Object> resources) {
        ExplicitOperationValidator.validate(classes);
        OpenAPI openAPI = super.read(classes, resources);
        OperationIdValidator.validate(openAPI);
        AlternativeOperationIdExtension.apply(classes, openAPI);
        return openAPI;
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

    static final class OperationIdValidator {

        static void validate(OpenAPI openAPI) {
            Map<String, List<String>> operationIds = collectOperationIds(openAPI);

            List<String> duplicates = operationIds.entrySet().stream().filter(e -> e.getValue().size() > 1)
                    .map(e -> e.getKey() + " -> " + String.join(", ", e.getValue())).sorted().toList();
            if (!duplicates.isEmpty()) {
                throw new IllegalStateException("Duplicate OpenAPI operationIds detected:\n - " + String.join("\n - ", duplicates));
            }
        }

        static Map<String, List<String>> collectOperationIds(OpenAPI openAPI) {
            Map<String, List<String>> operationIds = new LinkedHashMap<>();
            if (openAPI == null || openAPI.getPaths() == null) {
                return operationIds;
            }

            openAPI.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                String operationId = trimToNull(operation.getOperationId());
                if (operationId != null) {
                    operationIds.computeIfAbsent(operationId, ignored -> new ArrayList<>()).add(httpMethod + " " + path);
                }
            }));
            return operationIds;
        }
    }

    static final class AlternativeOperationIdExtension {

        static void apply(Set<Class<?>> classes, OpenAPI openAPI) {
            Map<String, String> alternativeOperationIds = collect(classes);
            Map<String, List<String>> operationIds = OperationIdValidator.collectOperationIds(openAPI);
            validateAlternativeOperationIdTargets(alternativeOperationIds, operationIds);

            if (alternativeOperationIds.isEmpty() || openAPI == null || openAPI.getPaths() == null) {
                return;
            }

            openAPI.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                String alternativeOperationId = alternativeOperationIds.get(operation.getOperationId());
                if (alternativeOperationId != null) {
                    operation.addExtension(ALTERNATIVE_OPERATION_ID_EXTENSION, alternativeOperationId);
                }
            }));
        }

        private static Map<String, String> collect(Set<Class<?>> classes) {
            Map<String, String> alternativeOperationIds = new LinkedHashMap<>();
            Map<String, List<String>> duplicateAlternativeOperationIds = new LinkedHashMap<>();

            for (Class<?> resourceClass : classes) {
                if (resourceClass.getAnnotation(Path.class) == null) {
                    continue;
                }

                for (Method method : resourceClass.getMethods()) {
                    Operation operation = method.getAnnotation(Operation.class);
                    String operationId = trimToNull(operation == null ? null : operation.operationId());

                    AlternativeOperationId alternativeOperationId = method.getAnnotation(AlternativeOperationId.class);
                    if (alternativeOperationId == null) {
                        continue;
                    }

                    String source = resourceClass.getSimpleName() + "#" + method.getName();
                    if (!hasHttpMethod(method)) {
                        throw new IllegalStateException("@AlternativeOperationId can only be used on JAX-RS resource methods: " + source);
                    }

                    String value = trimToNull(alternativeOperationId.value());
                    if (value == null) {
                        throw new IllegalStateException("@AlternativeOperationId value must not be blank: " + source);
                    }
                    if (!SourceVersion.isIdentifier(value) || SourceVersion.isKeyword(value)) {
                        throw new IllegalStateException(
                                "@AlternativeOperationId must be a valid Java method name: " + source + " -> " + value);
                    }

                    String resolvedOperationId = operationId == null ? method.getName() : operationId;
                    if (resolvedOperationId.equals(value)) {
                        continue;
                    }

                    duplicateAlternativeOperationIds.computeIfAbsent(value, ignored -> new ArrayList<>()).add(source);
                    alternativeOperationIds.put(resolvedOperationId, value);
                }
            }

            List<String> duplicates = duplicateAlternativeOperationIds.entrySet().stream().filter(e -> e.getValue().size() > 1)
                    .map(e -> e.getKey() + " -> " + String.join(", ", e.getValue())).sorted().toList();
            if (!duplicates.isEmpty()) {
                throw new IllegalStateException(
                        "Duplicate explicit OpenAPI alternativeOperationIds detected:\n - " + String.join("\n - ", duplicates));
            }
            return alternativeOperationIds;
        }

        private static void validateAlternativeOperationIdTargets(Map<String, String> alternativeOperationIds,
                Map<String, List<String>> operationIds) {
            List<String> missing = alternativeOperationIds.keySet().stream().filter(operationId -> !operationIds.containsKey(operationId))
                    .sorted().toList();
            if (!missing.isEmpty()) {
                throw new IllegalStateException(
                        "OpenAPI alternativeOperationIds could not be matched to operationIds:\n - " + String.join("\n - ", missing));
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
