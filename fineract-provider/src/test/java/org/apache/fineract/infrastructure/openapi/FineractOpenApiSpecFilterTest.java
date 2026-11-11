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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.swagger.v3.core.filter.SpecFilter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.apache.fineract.infrastructure.core.api.DateParam;
import org.junit.jupiter.api.Test;

class FineractOpenApiSpecFilterTest {

    @Test
    void documentsADateQueryParameterAsAString() {
        OpenAPI read = new FineractOperationIdReader().read(Set.of(ResourceWithDateParam.class), Map.of());
        assertEquals(FineractOpenApiSpecFilter.DATE_PARAM_REF, parameter(read, "fromDate").getSchema().get$ref());

        OpenAPI filtered = filter(read);

        Parameter fromDate = parameter(filtered, "fromDate");
        assertNull(fromDate.getSchema().get$ref());
        assertEquals("string", fromDate.getSchema().getType());
        assertFalse(filtered.getComponents() != null && filtered.getComponents().getSchemas() != null
                && filtered.getComponents().getSchemas().containsKey(DateParam.class.getSimpleName()));
    }

    @Test
    void documentsADateFieldOfARequestBodyAsAString() {
        OpenAPI read = new FineractOperationIdReader().read(Set.of(ResourceWithDateParam.class), Map.of());
        assertEquals(FineractOpenApiSpecFilter.DATE_PARAM_REF, bodyProperty(read, "date").get$ref());

        OpenAPI filtered = filter(read);

        Schema<?> date = bodyProperty(filtered, "date");
        assertNull(date.get$ref());
        assertEquals("string", date.getType());
        assertEquals("string", bodyProperty(filtered, "locale").getType());
    }

    @Test
    void leavesOtherParametersAlone() {
        OpenAPI filtered = filter(new FineractOperationIdReader().read(Set.of(ResourceWithDateParam.class), Map.of()));

        Parameter limit = parameter(filtered, "limit");
        assertEquals("integer", limit.getSchema().getType());
        assertEquals("int32", limit.getSchema().getFormat());
    }

    private static Schema<?> bodyProperty(OpenAPI openAPI, String name) {
        return (Schema<?>) openAPI.getComponents().getSchemas().get(DatedRequest.class.getSimpleName()).getProperties().get(name);
    }

    private static OpenAPI filter(OpenAPI openAPI) {
        return new SpecFilter().filter(openAPI, new FineractOpenApiSpecFilter(), Map.of(), Map.of(), Map.of());
    }

    private static Parameter parameter(OpenAPI openAPI, String name) {
        List<Parameter> parameters = openAPI.getPaths().get("/dated").getGet().getParameters();
        return parameters.stream().filter(parameter -> name.equals(parameter.getName())).findFirst().orElseThrow();
    }

    @Path("/dated")
    public static class ResourceWithDateParam {

        @GET
        public String retrieveDated(@QueryParam("fromDate") DateParam fromDate, @QueryParam("limit") Integer limit) {
            return null;
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        public String searchDated(DatedRequest request) {
            return null;
        }
    }

    @Data
    public static class DatedRequest {

        private DateParam date;
        private String locale;
    }
}
