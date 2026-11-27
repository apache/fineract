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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.annotation.LegacyApiResponse;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.LegacyApiResponseSerializer;
import org.apache.fineract.useradministration.data.RoleData;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JerseyTypedResponseWriterTest {

    @LegacyApiResponse({ "id", "name", "description" })
    public List<RoleData> roles() {
        return List.of(new RoleData(1L, "Officer", "Office role", false));
    }

    @Test
    void writesTypedCollectionsAndAppliesFieldsFromTheRequest() throws Exception {
        JerseyJacksonObjectArgumentHandler<Object> handler = handler("name");
        Method method = getClass().getMethod("roles");
        TrackingOutputStream stream = new TrackingOutputStream();
        handler.writeTo(roles(), List.class, method.getGenericReturnType(), method.getAnnotations(), MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedHashMap<>(), stream);
        assertEquals("[{\"name\":\"Officer\",\"disabled\":false}]", stream.toString(StandardCharsets.UTF_8));
        // The writer must leave Jersey's stream open for compression and other interceptors.
        assertFalse(stream.closed);
    }

    @Test
    void resolvesResourceMethodMetadataWhenAProxyHidesAnnotations() throws Exception {
        JerseyJacksonObjectArgumentHandler<Object> handler = handler("name");
        Method method = getClass().getMethod("roles");
        ResourceInfo resourceInfo = mock(ResourceInfo.class);
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        ReflectionTestUtils.setField(handler, "resourceInfo", resourceInfo);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        handler.writeTo(roles(), List.class, method.getGenericReturnType(), new Annotation[0], MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedHashMap<>(), stream);
        assertEquals("[{\"name\":\"Officer\",\"disabled\":false}]", stream.toString(StandardCharsets.UTF_8));
    }

    @Test
    void rejectsUnsupportedFieldsBeforeWritingAnyBytes() throws Exception {
        JerseyJacksonObjectArgumentHandler<Object> handler = handler("unknown");
        Method method = getClass().getMethod("roles");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        assertThrows(UnsupportedParameterException.class, () -> handler.writeTo(roles(), List.class, method.getGenericReturnType(),
                method.getAnnotations(), MediaType.APPLICATION_JSON_TYPE, new MultivaluedHashMap<>(), stream));
        assertEquals(0, stream.size());
    }

    @Test
    void errorResponsesDoNotReapplyTheInvalidFieldsParameter() throws Exception {
        JerseyJacksonObjectArgumentHandler<Object> handler = handler("unknown");
        Method method = getClass().getMethod("roles");
        ApiGlobalErrorResponse error = ApiGlobalErrorResponse.badClientRequest("validation.msg.validation.errors.exist",
                "Validation errors exist.", List.of());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        handler.writeTo(error, ApiGlobalErrorResponse.class, ApiGlobalErrorResponse.class, method.getAnnotations(),
                MediaType.APPLICATION_JSON_TYPE, new MultivaluedHashMap<>(), stream);
        assertEquals(SecurityApiSerializationTestSupport.newObjectMapper().writeValueAsString(error),
                stream.toString(StandardCharsets.UTF_8));
    }

    @Test
    void leavesUnannotatedObjectsAndStringResponsesOnTheirExistingPaths() throws Exception {
        JerseyJacksonObjectArgumentHandler<Object> handler = handler("unknown");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        handler.writeTo(Map.of("enabled", true), Map.class, Map.class, new Annotation[0], MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedHashMap<>(), stream);
        assertEquals("{\"enabled\":true}", stream.toString(StandardCharsets.UTF_8));
        stream.reset();
        handler.writeTo("{\"legacy\":true}", String.class, String.class, new Annotation[0], MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedHashMap<>(), stream);
        assertEquals("{\"legacy\":true}", stream.toString(StandardCharsets.UTF_8));
        assertFalse(stream.toString(StandardCharsets.UTF_8).startsWith("\""));
    }

    private static final class TrackingOutputStream extends ByteArrayOutputStream {

        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }

    private JerseyJacksonObjectArgumentHandler<Object> handler(String fields) {
        ObjectMapper mapper = SecurityApiSerializationTestSupport.newObjectMapper();
        JerseyJacksonObjectArgumentHandler<Object> handler = new JerseyJacksonObjectArgumentHandler<>(mapper,
                new LegacyApiResponseSerializer(mapper));
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedHashMap<String, String> query = new MultivaluedHashMap<>();
        query.putSingle("fields", fields);
        when(uriInfo.getQueryParameters()).thenReturn(query);
        ReflectionTestUtils.setField(handler, "uriInfo", uriInfo);
        return handler;
    }
}
