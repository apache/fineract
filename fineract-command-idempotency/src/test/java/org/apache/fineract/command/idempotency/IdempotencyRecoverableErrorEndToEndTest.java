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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.UUID;
import org.apache.fineract.command.core.CommandState;
import org.apache.fineract.command.core.RecoverableCommandErrorClassifier;
import org.apache.fineract.command.jdbc.store.domain.CommandEntity;
import org.apache.fineract.command.jdbc.store.domain.JdbcCommandRepository;
import org.apache.fineract.command.test.CommandBaseTest;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.apache.fineract.command.test.sample.exception.DummyException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

/**
 * The same chain as {@link IdempotencyEndToEndTest}, with the sample command's failure classified as recoverable: a
 * retry under the same key has to be executed again rather than answered with the stored failure, and the failure has
 * to stay on record.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = { TestConfiguration.class, IdempotencyRecoverableErrorEndToEndTest.RecoverableDummyFailures.class })
public class IdempotencyRecoverableErrorEndToEndTest extends CommandBaseTest {

    /** The distinctor of the {@code /error} command under the default, {@code ENTITY_NAME_AND_ACTION_NAME}. */
    private static final String ERROR = "DUMMY|ERROR";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcCommandRepository repository;

    private final RestTemplate restTemplate = errorTolerantRestTemplate();

    @Test
    public void aRetryAfterARecoverableFailureIsExecutedAgain() {
        var key = UUID.randomUUID().toString();

        var first = post(key);
        var retry = post(key);

        assertTrue(first.getStatusCode().is4xxClientError(), "The command must fail, got " + first.getStatusCode());
        assertFalse(servedFromCache(retry), "A retry after a recoverable failure must be executed, not answered from the store.");
        assertEquals(first.getStatusCode(), retry.getStatusCode(), "Executed again, it fails again the same way.");
    }

    @Test
    public void theFailureStaysOnRecordUnderTheMovedKey() {
        var key = UUID.randomUUID().toString();

        post(key);

        assertTrue(repository.findOneByIdempotencyKeyAndCustomIdempotencyDistinctor(key, ERROR).isEmpty(),
                "The original key must be free for the retry.");

        var moved = repository.findAll().stream().filter(row -> row.getIdempotencyKey().startsWith(key + "_RE")).toList();

        assertEquals(1, moved.size(), "The failed command must still be recorded, under its moved key.");

        CommandEntity row = moved.get(0);

        assertEquals(key + "_RE" + row.getId(), row.getIdempotencyKey());
        assertEquals(CommandState.ERROR, row.getState());
        assertEquals(ERROR, row.getCustomIdempotencyDistinctor());
        assertNotNull(row.getResponseStatusCode(), "The answer the client was sent must be recorded against the moved row.");
    }

    private ResponseEntity<String> post(String idempotencyKey) {
        var headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Idempotency-Key", idempotencyKey);

        return restTemplate.exchange("http://localhost:" + port + "/test/dummy/error", HttpMethod.POST,
                new HttpEntity<>(DummyRequest.builder().content("boom").build(), headers), String.class);
    }

    private static boolean servedFromCache(ResponseEntity<String> response) {
        return "true".equals(response.getHeaders().getFirst("x-served-from-cache"));
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

    @Configuration
    static class RecoverableDummyFailures {

        @Bean
        RecoverableCommandErrorClassifier recoverableCommandErrorClassifier() {
            return error -> error instanceof DummyException;
        }
    }
}
