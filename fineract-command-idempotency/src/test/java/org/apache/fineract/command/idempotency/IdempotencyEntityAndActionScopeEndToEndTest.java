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

import static org.apache.fineract.command.core.exception.AbstractCommandIdempotencyException.IDEMPOTENT_CACHE_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.springframework.web.client.RestTemplate;

/**
 * The same chain as {@link IdempotencyEndToEndTest}, under the default distinctor, {@code ENTITY_NAME_AND_ACTION_NAME}:
 * the key is scoped to the command's entity and action, the way the legacy command processing scopes it. Nothing is
 * configured here on purpose, so that this also pins which distinctor is the default. The sample commands are
 * {@code DUMMY|CREATE} on {@code /sync} and {@code DUMMY|ERROR} on {@code /error}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = TestConfiguration.class)
public class IdempotencyEntityAndActionScopeEndToEndTest extends CommandBaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcCommandRepository repository;

    private final RestTemplate restTemplate = errorTolerantRestTemplate();

    @Test
    public void theSameKeyOnAnotherCommandIsExecutedAsANewCommand() {
        var key = UUID.randomUUID().toString();

        var first = post("/sync", "hello", key);
        var otherCommand = post("/error", "boom", key);

        assertTrue(first.getStatusCode().is2xxSuccessful(), "The first request must be processed.");
        assertTrue(otherCommand.getStatusCode().is4xxClientError(),
                "Another entity and action under the same key is a new command, so it must run - and this one fails - "
                        + "rather than be refused as a conflict; got " + otherCommand.getStatusCode());
        assertFalse(servedFromCache(otherCommand), "It was executed, not replayed.");

        assertEquals(CommandState.PROCESSED,
                repository.findOneByIdempotencyKeyAndCustomIdempotencyDistinctor(key, "DUMMY|CREATE").orElseThrow().getState());
        assertEquals(CommandState.ERROR,
                repository.findOneByIdempotencyKeyAndCustomIdempotencyDistinctor(key, "DUMMY|ERROR").orElseThrow().getState());
    }

    @Test
    public void aRepeatUnderTheSameEntityAndActionIsReplayed() {
        var key = UUID.randomUUID().toString();

        var first = post("/sync", "hello", key);
        post("/error", "boom", key);

        var repeat = post("/sync", "hello", key);

        assertTrue(servedFromCache(repeat), "A repeat under the same entity and action must be replayed.");
        assertEquals(first.getStatusCode(), repeat.getStatusCode());
        assertEquals(first.getBody(), repeat.getBody(), "It must be answered from its own row, not the other command's.");
    }

    @Test
    public void aRepeatOfTheFailedCommandIsAnsweredWithItsOwnFailure() {
        var key = UUID.randomUUID().toString();

        post("/sync", "hello", key);
        var failure = post("/error", "boom", key);
        var repeat = post("/error", "boom", key);

        assertTrue(servedFromCache(repeat), "A repeat of the failed command must be replayed.");
        assertEquals(failure.getStatusCode(), repeat.getStatusCode(),
                "The status recorded for the failed command must be found under its own distinctor.");
        assertEquals(failure.getBody(), repeat.getBody());
    }

    private ResponseEntity<String> post(String path, String content, String idempotencyKey) {
        var headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Idempotency-Key", idempotencyKey);

        return restTemplate.exchange("http://localhost:" + port + "/test/dummy" + path, HttpMethod.POST,
                new HttpEntity<>(DummyRequest.builder().content(content).build(), headers), String.class);
    }

    private static boolean servedFromCache(ResponseEntity<String> response) {
        return "true".equals(response.getHeaders().getFirst(IDEMPOTENT_CACHE_HEADER));
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
