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
package org.apache.fineract.infrastructure.core.jersey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.annotation.PartialResponseParameters;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.exceptionmapper.UnsupportedParameterExceptionMapper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PartialResponseFilterTest {

    private static final String[] SUPPORTED = { "id", "accountType", "prefixType" };

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PartialResponseFilter underTest;
    private ContainerRequestContext requestContext;
    private ContainerResponseContext responseContext;
    private MultivaluedMap<String, String> queryParameters;

    @PartialResponseParameters({ "id", "accountType", "prefixType" })
    private static final class AnnotatedResource {}

    private record Nested(Long id, String code, String value) {
    }

    private record Item(Long id, Nested accountType, Nested prefixType, String prefixCharacter) {
    }

    @BeforeEach
    void setUp() {
        underTest = new PartialResponseFilter(objectMapper, new UnsupportedParameterExceptionMapper());
        requestContext = mock(ContainerRequestContext.class);
        responseContext = mock(ContainerResponseContext.class);
        queryParameters = new MultivaluedHashMap<>();
        final UriInfo uriInfo = mock(UriInfo.class);
        lenient().when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        lenient().when(requestContext.getUriInfo()).thenReturn(uriInfo);
        lenient().when(responseContext.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        lenient().when(responseContext.getEntityAnnotations()).thenReturn(new Annotation[0]);
    }

    private void withSupportedParameters(final String... parameters) {
        final ResourceInfo resourceInfo = mock(ResourceInfo.class);
        if (parameters.length == 0) {
            lenient().when(resourceInfo.getResourceClass()).thenAnswer(invocation -> EmptyAnnotatedResource.class);
        } else {
            lenient().when(resourceInfo.getResourceClass()).thenAnswer(invocation -> AnnotatedResource.class);
        }
        underTest.setResourceInfo(resourceInfo);
    }

    @PartialResponseParameters({})
    private static final class EmptyAnnotatedResource {}

    private JsonNode capturePrunedEntity() {
        final ArgumentCaptor<Object> entity = ArgumentCaptor.forClass(Object.class);
        verify(responseContext).setEntity(entity.capture(), any(), any());
        return (JsonNode) entity.getValue();
    }

    @Test
    void withoutFieldsParameterTheResponseIsLeftAlone() {
        withSupportedParameters(SUPPORTED);
        when(responseContext.getEntity()).thenReturn(new Item(1L, null, null, "LN"));

        underTest.filter(requestContext, responseContext);

        verify(responseContext, never()).setEntity(any(), any(), any());
    }

    @Test
    void aSupportedSubsetDropsTheOtherSupportedParametersAtEveryDepth() {
        withSupportedParameters(SUPPORTED);
        queryParameters.putSingle("fields", "accountType,prefixType");
        when(responseContext.getEntity())
                .thenReturn(List.of(new Item(1L, new Nested(2L, "accountType.loan", "LOAN"), new Nested(401L, "prefix", "PREFIX"), "LN")));

        underTest.filter(requestContext, responseContext);

        final JsonNode pruned = capturePrunedEntity();
        final JsonNode item = pruned.get(0);
        assertFalse(item.has("id"), "id is supported but was not requested");
        assertFalse(item.get("accountType").has("id"), "nested id is dropped by name at any depth");
        assertTrue(item.has("accountType"));
        assertTrue(item.has("prefixType"));
        assertEquals("LN", item.get("prefixCharacter").asText(), "a field outside the supported set is never dropped");
    }

    @Test
    void anUnsupportedFieldIsRejected() {
        withSupportedParameters(SUPPORTED);
        queryParameters.putSingle("fields", "bogus");
        when(responseContext.getEntity()).thenReturn(new Item(1L, null, null, "LN"));

        underTest.filter(requestContext, responseContext);

        verify(responseContext).setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        final ArgumentCaptor<Object> entity = ArgumentCaptor.forClass(Object.class);
        verify(responseContext).setEntity(entity.capture(), any(), any());
        assertTrue(entity.getValue() instanceof ApiGlobalErrorResponse);
    }

    @Test
    void withoutASupportedSetOnlyTheRequestedNamesAreKept() {
        withSupportedParameters();
        queryParameters.putSingle("fields", "id");
        when(responseContext.getEntity()).thenReturn(new Item(1L, new Nested(2L, "code", "value"), null, "LN"));

        underTest.filter(requestContext, responseContext);

        final JsonNode pruned = capturePrunedEntity();
        assertTrue(pruned.has("id"));
        assertFalse(pruned.has("prefixCharacter"), "anything not requested is dropped");
        assertFalse(pruned.has("accountType"));
    }

    @Test
    void aPagedResponseIsPrunedInsideItsPageItems() {
        withSupportedParameters(SUPPORTED);
        queryParameters.putSingle("fields", "accountType");
        final Page<Item> page = new Page<>(List.of(new Item(1L, new Nested(2L, "code", "value"), null, "LN")), 1);
        when(responseContext.getEntity()).thenReturn(page);

        underTest.filter(requestContext, responseContext);

        final JsonNode pruned = capturePrunedEntity();
        assertEquals(1, pruned.get("totalFilteredRecords").asInt(), "the page wrapper itself is untouched");
        final JsonNode item = pruned.get("pageItems").get(0);
        assertFalse(item.has("id"));
        assertTrue(item.has("accountType"));
        assertNull(item.get("prefixType"), "null properties are not written in the first place");
    }

    @Test
    void aResourceWithNoAnnotationDoesNotTakePartInPartialResponses() {
        final ResourceInfo resourceInfo = mock(ResourceInfo.class);
        when(resourceInfo.getResourceClass()).thenAnswer(invocation -> Map.class);
        underTest.setResourceInfo(resourceInfo);
        queryParameters.putSingle("fields", "id");
        when(responseContext.getEntity()).thenReturn(new Item(1L, null, null, "LN"));

        underTest.filter(requestContext, responseContext);

        verify(responseContext, never()).setEntity(any(), any(), any());
    }
}
