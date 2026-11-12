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
package org.apache.fineract.command.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.UUID;
import org.apache.fineract.command.core.CommandState;
import org.apache.fineract.command.jdbc.store.domain.JdbcCommandRepository;
import org.apache.fineract.command.test.CommandBaseTest;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

/**
 * The same chain as {@link IdempotencyEndToEndTest}, with {@code NONE}: nothing distinguishes a key, so it identifies
 * one command for the whole tenant - stricter than the legacy command processing, which scopes a key by entity and
 * action.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = TestConfiguration.class)
@TestPropertySource(properties = "fineract.command.idempotency.custom-distinctor=NONE")
public class IdempotencyNoDistinctorEndToEndTest extends CommandBaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcCommandRepository repository;

    private final RestTemplate restTemplate = errorTolerantRestTemplate();

    @Test
    public void aKeyIsUniqueAcrossCommandsNotOnlyWithinOne() {
        var key = UUID.randomUUID().toString();

        var first = post("/sync", "hello", key);

        assertTrue(first.getStatusCode().is2xxSuccessful(), "The first request must be processed.");

        var otherCommand = post("/error", "boom", key);

        assertEquals(409, otherCommand.getStatusCode().value(),
                "Reusing a key for a different command must be rejected, not executed and not replayed.");
    }

    @Test
    public void theKeyIsStoredWithTheEmptyDistinctor() {
        var key = UUID.randomUUID().toString();

        post("/sync", "hello", key);

        assertEquals(CommandState.PROCESSED,
                repository.findOneByIdempotencyKeyAndCustomIdempotencyDistinctor(key, "").orElseThrow().getState(),
                "Under NONE the key is claimed with the empty distinctor.");
    }

    private ResponseEntity<String> post(String path, String content, String idempotencyKey) {
        var headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Idempotency-Key", idempotencyKey);

        return restTemplate.exchange("http://localhost:" + port + "/test/dummy" + path, HttpMethod.POST,
                new HttpEntity<>(DummyRequest.builder().content(content).build(), headers), String.class);
    }

    /** Error statuses are part of what is under test, so they have to come back as responses, not exceptions. */
    private static RestTemplate errorTolerantRestTemplate() {
        var template = new RestTemplate();

        template.setErrorHandler(new org.springframework.web.client.ResponseErrorHandler() {

            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false;
            }
        });

        return template;
    }
}
