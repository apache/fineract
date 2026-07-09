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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.junit.jupiter.api.Test;

class FineractOperationIdReaderTest {

    @Test
    void addsAlternativeOperationIdExtension() {
        OpenAPI openAPI = new FineractOperationIdReader().read(Set.of(ResourceWithAlternativeOperationId.class), Map.of());

        Object alternativeOperationId = openAPI.getPaths().get("/test").getGet().getExtensions()
                .get(FineractOperationIdReader.ALTERNATIVE_OPERATION_ID_EXTENSION);

        assertEquals("retrieveTestByLegacyName", alternativeOperationId);
    }

    @Test
    void addsAlternativeOperationIdExtensionForImplicitOperationId() {
        OpenAPI openAPI = new FineractOperationIdReader().read(Set.of(ResourceWithImplicitOperationIdAlternative.class), Map.of());

        Object alternativeOperationId = openAPI.getPaths().get("/implicit").getGet().getExtensions()
                .get(FineractOperationIdReader.ALTERNATIVE_OPERATION_ID_EXTENSION);

        assertEquals("retrieveImplicitTestByLegacyName", alternativeOperationId);
    }

    @Test
    void rejectsInvalidAlternativeOperationId() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new FineractOperationIdReader().read(Set.of(ResourceWithInvalidAlternativeOperationId.class), Map.of()));

        assertTrue(exception.getMessage().contains("valid Java method name"));
    }

    @Test
    void allowsAlternativeOperationIdMatchingAnotherOperationId() {
        OpenAPI openAPI = new FineractOperationIdReader()
                .read(Set.of(ResourceWithAlternativeOperationIdConflict.class, ResourceWithImplicitOperationId.class), Map.of());

        Object alternativeOperationId = openAPI.getPaths().get("/conflict").getGet().getExtensions()
                .get(FineractOperationIdReader.ALTERNATIVE_OPERATION_ID_EXTENSION);

        assertEquals("retrieveImplicitConflict", alternativeOperationId);
    }

    @Test
    void rejectsDuplicateFinalOperationIds() {
        OpenAPI openAPI = new OpenAPI().paths(new Paths()
                .addPathItem("/first", new PathItem().get(new io.swagger.v3.oas.models.Operation().operationId("duplicateOperation")))
                .addPathItem("/second", new PathItem().post(new io.swagger.v3.oas.models.Operation().operationId("duplicateOperation"))));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> FineractOperationIdReader.OperationIdValidator.validate(openAPI));

        assertTrue(exception.getMessage().contains("Duplicate OpenAPI operationIds"));
    }

    @Path("/test")
    public static class ResourceWithAlternativeOperationId {

        @GET
        @Operation(operationId = "retrieveTest")
        @AlternativeOperationId("retrieveTestByLegacyName")
        public String retrieveTest() {
            return "test";
        }
    }

    @Path("/implicit")
    public static class ResourceWithImplicitOperationIdAlternative {

        @GET
        @AlternativeOperationId("retrieveImplicitTestByLegacyName")
        public String retrieveImplicitTest() {
            return "implicit";
        }
    }

    @Path("/invalid")
    public static class ResourceWithInvalidAlternativeOperationId {

        @GET
        @Operation(operationId = "retrieveInvalid")
        @AlternativeOperationId("class")
        public String retrieveInvalid() {
            return "invalid";
        }
    }

    @Path("/conflict")
    public static class ResourceWithAlternativeOperationIdConflict {

        @GET
        @Operation(operationId = "retrieveConflict")
        @AlternativeOperationId("retrieveImplicitConflict")
        public String retrieveConflict() {
            return "conflict";
        }
    }

    @Path("/implicit-conflict")
    public static class ResourceWithImplicitOperationId {

        @GET
        public String retrieveImplicitConflict() {
            return "implicit-conflict";
        }
    }
}
