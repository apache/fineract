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

import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.DateParam;

public final class FineractOpenApiSpecFilter extends AbstractSpecFilter {

    static final String DATE_PARAM_REF = Components.COMPONENTS_SCHEMAS_REF + DateParam.class.getSimpleName();

    @Override
    public boolean isRemovingUnreferencedDefinitions() {
        return true;
    }

    /**
     * Documents a {@link DateParam} parameter as the string it is sent as. JAX-RS builds a DateParam from the
     * parameter's string value, but swagger-core describes the class as an object, which generated clients cannot send.
     * Swagger-core ignores a class-level {@code @Schema(type = "string")} for parameters, so it is done here once
     * rather than on every such parameter.
     */
    @Override
    public Optional<Parameter> filterParameter(Parameter parameter, Operation operation, ApiDescription api,
            Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if (isDateParam(parameter.getSchema())) {
            parameter.setSchema(new StringSchema());
        }
        return Optional.of(parameter);
    }

    /**
     * Documents a {@link DateParam} field of a request body as a string too: Jackson builds it from a JSON string
     * through the same single string constructor.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Optional<Schema> filterSchemaProperty(Schema property, Schema schema, String propName, Map<String, List<String>> params,
            Map<String, String> cookies, Map<String, List<String>> headers) {
        return Optional.of(isDateParam(property) ? new StringSchema() : property);
    }

    private static boolean isDateParam(Schema<?> schema) {
        return schema != null && DATE_PARAM_REF.equals(schema.get$ref());
    }
}
