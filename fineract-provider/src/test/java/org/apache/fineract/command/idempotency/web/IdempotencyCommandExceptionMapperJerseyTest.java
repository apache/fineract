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
package org.apache.fineract.command.idempotency.web;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.fineract.command.core.exception.AbstractCommandIdempotencyException.IDEMPOTENT_CACHE_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import org.apache.fineract.command.core.exception.CommandIdempotencyFailedException;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Runs a replayed failure through a real Jersey stack, because the answer a client receives depends on how Jersey
 * writes the entity rather than on what the mapper puts in it.
 *
 * <p>
 * The stored answer for a failure is the text that was captured as it went out, so the mapper hands Jersey a
 * {@code String}. Were Jersey to treat that as a value to serialise rather than as a body to write, a client would
 * receive {@code "{\"errors\":[...]}"} - the original document wrapped in quotes and escaped - instead of the document
 * itself. That is the difference this pins, and no test of the mapper on its own can see it: the mapper is correct
 * either way.
 *
 * <p>
 * The end-to-end suite covers the Spring MVC counterpart; Fineract itself serves its API through Jersey, which is this.
 */
class IdempotencyCommandExceptionMapperJerseyTest {

    private static final String STORED_ANSWER = """
            {"developerMessage":"The request was invalid.","httpStatusCode":"400","errors":[{"parameterName":"principal",\
            "defaultUserMessage":"The parameter principal cannot be blank.","value":null}],"amount":1.50}""";

    private ApplicationHandler handler;

    @BeforeEach
    void startJersey() {
        // built here rather than in a field, because standing a Jersey application up can fail and a constructor that
        // throws is a finding of its own
        handler = new ApplicationHandler(resourceConfig());
    }

    /**
     * Jersey's Spring bridge is on the classpath and looks for a context of its own; an empty one keeps it satisfied
     * without dragging an application context into a test about how bytes reach the wire.
     */
    private static ResourceConfig resourceConfig() {
        var springContext = new AnnotationConfigApplicationContext();

        springContext.refresh();

        return new ResourceConfig().property("contextConfig", springContext).register(IdempotencyCommandExceptionMapper.class)
                .register(ReplayResource.class);
    }

    @Test
    void aReplayedFailureIsWrittenAsTheDocumentItIsRatherThanAsAQuotedString() throws Exception {
        var body = new ByteArrayOutputStream();

        var response = handler.apply(request("/failed"), body).get();

        assertEquals(400, response.getStatus(), "A repeat of a failure must answer with the status it was first answered with.");
        assertEquals("true", response.getStringHeaders().getFirst(IDEMPOTENT_CACHE_HEADER),
                "A replay must tell the client it was served from the command store.");
        assertEquals(STORED_ANSWER, body.toString(UTF_8),
                "The client must receive the stored answer byte for byte, not a JSON string containing it.");
    }

    @Test
    void aFailureWithNothingStoredAnswersWithItsStatusAndAnEmptyBody() throws Exception {
        var body = new ByteArrayOutputStream();

        var response = handler.apply(request("/nothing-stored"), body).get();

        assertEquals(500, response.getStatus(), "An inconsistent record answers 500 rather than nothing at all.");
        assertEquals("", body.toString(UTF_8), "There is nothing to replay, so the body is empty rather than \"null\".");
    }

    private static ContainerRequest request(String path) {
        return new ContainerRequest(URI.create("http://localhost/"), URI.create("http://localhost" + path), "GET", null,
                new MapPropertiesDelegate(), null);
    }

    @Path("/")
    public static class ReplayResource {

        @GET
        @Path("failed")
        public String failed() {
            throw new CommandIdempotencyFailedException("a-key", 400, STORED_ANSWER);
        }

        @GET
        @Path("nothing-stored")
        public String nothingStored() {
            throw new CommandIdempotencyFailedException("a-key", null, null);
        }
    }
}
