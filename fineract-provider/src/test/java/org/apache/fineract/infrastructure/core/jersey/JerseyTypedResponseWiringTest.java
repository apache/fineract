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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.annotation.LegacyApiResponse;
import org.apache.fineract.infrastructure.core.exceptionmapper.UnsupportedParameterExceptionMapper;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.LegacyApiResponseSerializer;
import org.apache.fineract.useradministration.data.RoleData;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class JerseyTypedResponseWiringTest {

    private AnnotationConfigApplicationContext spring;
    private ApplicationHandler application;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = SecurityApiSerializationTestSupport.newObjectMapper();
        spring = new AnnotationConfigApplicationContext();
        spring.registerBean(TypedResource.class);
        spring.registerBean(JerseyJacksonObjectArgumentHandler.class,
                () -> new JerseyJacksonObjectArgumentHandler<>(mapper, new LegacyApiResponseSerializer(mapper)));
        spring.refresh();
        JerseyConfig config = new JerseyConfig();
        config.appCtx = spring;
        config.setup();
        config.register(UnsupportedParameterExceptionMapper.class);
        config.property("contextConfig", spring);
        application = new ApplicationHandler(config);
    }

    @AfterEach
    void tearDown() {
        application.onShutdown(null);
        spring.close();
    }

    @Test
    void springRegisteredWriterWinsOverTheAutoDiscoveredJacksonProvider() throws Exception {
        assertEquals(mapper.readTree("[{\"name\":\"Officer\",\"disabled\":false}]"), get("roles?fields=name", 200));
        assertEquals(mapper.readTree("[{\"id\":1,\"name\":\"Officer\",\"description\":\"Office role\",\"disabled\":false}]"),
                get("roles", 200));
    }

    @Test
    void invalidFieldsAreMappedToThePlatformErrorResponse() throws Exception {
        JsonNode errors = get("roles?fields=foo", 400).path("errors");
        assertEquals(1, errors.size());
        assertEquals("error.msg.parameter.unsupported", errors.get(0).path("userMessageGlobalisationCode").asText());
        assertEquals("foo", errors.get(0).path("parameterName").asText());
    }

    @Test
    void legacyDatesAreAppliedOnlyToTheAnnotatedResource() throws Exception {
        assertEquals(mapper.readTree("{\"joiningDate\":[2011,9,20]}"), get("legacy-date", 200));
        assertEquals(mapper.readTree("{\"joiningDate\":\"2011-09-20\"}"), get("plain-date", 200));
        assertEquals(mapper.readTree("{\"joiningDate\":[2011,9,20]}"), get("legacy-date", 200));
    }

    private JsonNode get(String path, int expectedStatus) throws Exception {
        URI base = URI.create("http://localhost/api/");
        ContainerRequest request = new ContainerRequest(base, base.resolve(path), "GET", null, new MapPropertiesDelegate());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertEquals(expectedStatus, application.apply(request, output).get().getStatus(), output.toString(StandardCharsets.UTF_8));
        return mapper.readTree(output.toByteArray());
    }

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TypedResource {

        @GET
        @Path("roles")
        @LegacyApiResponse({ "id", "name", "description" })
        public Collection<RoleData> roles() {
            return List.of(new RoleData(1L, "Officer", "Office role", false));
        }

        @GET
        @Path("legacy-date")
        @LegacyApiResponse
        public Map<String, LocalDate> legacyDate() {
            return plainDate();
        }

        @GET
        @Path("plain-date")
        public Map<String, LocalDate> plainDate() {
            return Map.of("joiningDate", LocalDate.of(2011, 9, 20));
        }
    }
}
