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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandState;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.jdbc.store.domain.JdbcCommandRepository;
import org.apache.fineract.command.test.CommandBaseTest;
import org.apache.fineract.command.test.sample.command.DummyCommand;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

/**
 * Drives the whole thing over HTTP, which is the only level at which the behaviour this feature promises is visible:
 * the status code, the body and the replay header a repeated request is answered with.
 * <p>
 * These are the same guarantees the old command processing gives, as pinned by
 * {@code org.apache.fineract.integrationtests.IdempotencyTest}.
 */
@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = TestConfiguration.class)
public class IdempotencyEndToEndTest extends CommandBaseTest {

    /** The distinctor of the {@code /sync} command under the default, {@code ENTITY_NAME_AND_ACTION_NAME}. */
    private static final String SYNC = "DUMMY|CREATE";

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = errorTolerantRestTemplate();

    @org.springframework.beans.factory.annotation.Autowired
    private CommandStore store;

    @org.springframework.beans.factory.annotation.Autowired
    private JdbcCommandRepository repository;

    @Test
    public void aRequestWithoutAKeyIsNeverTreatedAsARepeat() {
        var first = post("/sync", "hello", null);
        var second = post("/sync", "hello", null);

        assertTrue(first.getStatusCode().is2xxSuccessful(), "A request without a key must be processed.");
        assertTrue(second.getStatusCode().is2xxSuccessful(), "A second request without a key must be processed too.");
        assertFalse(servedFromCache(second), "Nothing can be replayed without an idempotency key.");
    }

    @Test
    public void theFirstRequestIsProcessedAndTheRepeatIsReplayed() {
        var key = UUID.randomUUID().toString();

        var first = post("/sync", "hello", key);

        assertTrue(first.getStatusCode().is2xxSuccessful(), "The first request must be processed.");
        assertFalse(servedFromCache(first), "The first request is executed, not replayed.");
        assertNotNull(first.getBody(), "The first request must return a body.");

        var second = post("/sync", "hello", key);

        assertTrue(servedFromCache(second), "A repeat must be marked as served from the command store.");
        assertEquals(first.getStatusCode(), second.getStatusCode(), "A repeat must be answered with the original status.");
        assertEquals(first.getBody(), second.getBody(), "A repeat must be answered with the original body.");
    }

    @Test
    public void aRepeatOfAFailedRequestFailsTheSameWay() {
        var key = UUID.randomUUID().toString();

        // this endpoint always fails, so the command is stored as failed
        var first = post("/error", "boom", key);

        assertTrue(first.getStatusCode().is4xxClientError(), "The request must fail, got " + first.getStatusCode() + " " + first.getBody());
        assertFalse(servedFromCache(first), "The first failure is produced, not replayed.");

        var second = post("/error", "boom", key);

        assertTrue(servedFromCache(second), "A repeat of a failure must be marked as served from the command store.");
        assertEquals(first.getStatusCode(), second.getStatusCode(), "A repeat must fail with the original status.");
        assertEquals(first.getBody(), second.getBody(), "A repeat must fail with the original body.");
    }

    @Test
    public void aThirdRepeatIsAnsweredLikeTheSecond() {
        var key = UUID.randomUUID().toString();

        var first = post("/sync", "hello", key);
        var second = post("/sync", "hello", key);
        var third = post("/sync", "hello", key);

        // the earlier implementation accumulated rows per repeat and broke on the third request
        assertEquals(first.getStatusCode(), third.getStatusCode(), "Repeats must stay stable however often they are made.");
        assertEquals(second.getBody(), third.getBody(), "Repeats must stay stable however often they are made.");
        assertTrue(servedFromCache(third), "Every repeat is served from the command store.");
    }

    @Test
    public void aKeyReusedForADifferentRequestIsRejected() {
        var key = UUID.randomUUID().toString();

        post("/sync", "hello", key);

        var different = post("/sync", "something-else", key);

        assertEquals(409, different.getStatusCode().value(), "Reusing a key for a different request must not replay the first answer.");
    }

    @Test
    public void concurrentRepeatsExecuteTheCommandOnlyOnce() throws Exception {
        var threads = 6;
        var key = UUID.randomUUID().toString();
        var startLine = new CountDownLatch(1);
        var executed = new AtomicInteger();
        var replayed = new AtomicInteger();
        var tooEarly = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(threads)) {
            var calls = IntStream.range(0, threads).mapToObj(i -> executor.submit(() -> {
                startLine.await();

                var response = post("/sync", "hello", key);

                if (servedFromCache(response)) {
                    replayed.incrementAndGet();
                } else if (response.getStatusCode().value() == 425) {
                    tooEarly.incrementAndGet();
                } else {
                    executed.incrementAndGet();
                }

                return null;
            })).toList();

            startLine.countDown();

            for (var call : calls) {
                call.get(60, TimeUnit.SECONDS);
            }
        }

        assertEquals(1, executed.get(), "Exactly one concurrent request may execute the command.");
        assertEquals(threads - 1, replayed.get() + tooEarly.get(), "Every other request must be replayed or told it is too early.");
    }

    @Test
    public void aRequestForACommandStillRunningIsToldItIsTooEarly() {
        var key = UUID.randomUUID().toString();
        var inFlight = new DummyCommand();

        inFlight.setPayload(DummyRequest.builder().content("hello").build());
        inFlight.setIdempotencyKey(key);
        // as the hook would have claimed it under the default distinctor
        inFlight.setCustomIdempotencyDistinctor(SYNC);
        inFlight.setCreatedAt(java.time.Instant.now());

        // claim the key without ever completing it, which is what another node mid-request looks like
        store.claim(inFlight);

        var response = post("/sync", "hello", key);

        assertEquals(425, response.getStatusCode().value(), "A command that has not finished must answer 425 Too Early.");
        assertTrue(servedFromCache(response), "The answer comes from the command store, so it carries the replay header.");
    }

    @Test
    public void oneCommandIsRecordedAndRepeatsNeverAddAnother() {
        var key = UUID.randomUUID().toString();

        post("/sync", "hello", key);

        var recorded = repository.findOneByIdempotencyKeyAndCustomIdempotencyDistinctor(key, SYNC).orElseThrow();

        assertEquals(CommandState.PROCESSED, recorded.getState(), "A processed command must be audited as processed.");
        assertNotNull(recorded.getResponse(), "The result the command produced must be recorded.");
        assertNull(recorded.getResponseStatusCode(), "A command that produced a result needs no recorded status: its "
                + "repeat is answered by handing that result back, and the web layer derives the status from it.");

        post("/sync", "hello", key);
        post("/sync", "hello", key);

        assertEquals(1,
                repository.findAll().stream()
                        .filter(e -> key.equals(e.getIdempotencyKey()) && SYNC.equals(e.getCustomIdempotencyDistinctor())).count(),
                "Repeats must not add command records under the key and its distinctor.");

        var afterRepeats = repository.findOneByIdempotencyKeyAndCustomIdempotencyDistinctor(key, SYNC).orElseThrow();

        assertEquals(CommandState.PROCESSED, afterRepeats.getState(), "A repeat must not change the recorded outcome.");
    }

    private ResponseEntity<String> post(String path, String content, String idempotencyKey) {
        var headers = new HttpHeaders();

        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        if (idempotencyKey != null) {
            headers.add("Idempotency-Key", idempotencyKey);
        }

        var body = content == null ? DummyRequest.builder().build() : DummyRequest.builder().content(content).build();

        return restTemplate.exchange("http://localhost:" + port + "/test/dummy" + path, HttpMethod.POST, new HttpEntity<>(body, headers),
                String.class);
    }

    private static boolean servedFromCache(ResponseEntity<String> response) {
        return "true".equals(response.getHeaders().getFirst(IDEMPOTENT_CACHE_HEADER));
    }

    /**
     * The failure cases are part of the contract under test, so an error status has to come back as a response rather
     * than as an exception.
     */
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
