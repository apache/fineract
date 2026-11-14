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
package org.apache.fineract.command.jdbc;

import static org.apache.fineract.command.core.CommandState.ERROR;
import static org.apache.fineract.command.core.CommandState.PROCESSED;
import static org.apache.fineract.command.core.CommandState.UNDER_PROCESSING;
import static org.apache.fineract.command.core.CommandState.UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.jdbc.store.domain.JdbcCommandRepository;
import org.apache.fineract.command.test.CommandBaseTest;
import org.apache.fineract.command.test.sample.command.DummyCommand;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.apache.fineract.command.test.sample.data.DummyResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
public class JdbcCommandStoreTest extends CommandBaseTest {

    @Autowired
    protected CommandStore store;

    @Autowired
    protected JdbcCommandRepository repository;

    @Test
    public void aStoredResponseIsReplayedAsTheObjectTheCommandProduced() {
        var key = UUID.randomUUID().toString();

        store.store(processed(key, "hello"));

        var replayed = store.getReplayByKey(key, "").response();

        assertNotNull(replayed, "A processed command should replay its stored response.");
        assertInstanceOf(DummyResponse.class, replayed, "The response is rehydrated to its own type, not handed back as text.");
        assertEquals("HELLO", ((DummyResponse) replayed).getContent(), "The replay must carry the original response content.");
    }

    @Test
    public void onlyAnIdenticalRequestMatchesTheStoredOne() {
        var key = UUID.randomUUID().toString();

        store.store(processed(key, "hello"));

        assertTrue(store.matchesStoredRequest(key, "", DummyRequest.builder().content("hello").build()),
                "The same request must match the stored one.");
        assertFalse(store.matchesStoredRequest(key, "", DummyRequest.builder().content("goodbye").build()),
                "A different request of the same type must not match; it would replay an unrelated response.");
        assertFalse(store.matchesStoredRequest(key, "", DummyResponse.builder().content("hello").build()),
                "A different request type must not match.");
        assertFalse(store.matchesStoredRequest(key, "", null), "A null payload must not match.");
        assertFalse(store.matchesStoredRequest(UUID.randomUUID().toString(), "", DummyRequest.builder().content("hello").build()),
                "An unknown idempotency key must not match anything.");
    }

    @Test
    public void anIdenticalRetryIsRecognisedWhateverTypesTheRequestCarries() {
        var key = UUID.randomUUID().toString();
        var command = command(key, "hello");

        command.setPayload(fullyPopulated());

        store.claim(command);

        assertTrue(store.matchesStoredRequest(key, "", fullyPopulated()),
                "An identical retry must be recognised whatever field types the request carries.");
    }

    @Test
    public void aChangeToAnyFieldMakesItADifferentRequest() {
        var key = UUID.randomUUID().toString();
        var command = command(key, "hello");

        command.setPayload(fullyPopulated());

        store.claim(command);

        assertFalse(store.matchesStoredRequest(key, "", fullyPopulated().toBuilder().content("other").build()),
                "A different string must not be replayed.");
        assertFalse(store.matchesStoredRequest(key, "", fullyPopulated().toBuilder().referenceId(8L).build()),
                "A different id must not be replayed.");
        assertFalse(store.matchesStoredRequest(key, "", fullyPopulated().toBuilder().count(2).build()),
                "A different whole number must not be replayed.");
        assertFalse(store.matchesStoredRequest(key, "", fullyPopulated().toBuilder().flag(false).build()),
                "A different flag must not be replayed.");
        assertFalse(store.matchesStoredRequest(key, "", fullyPopulated().toBuilder().amount(new BigDecimal("150.00")).build()),
                "A different amount must not be replayed.");
        assertFalse(store.matchesStoredRequest(key, "", fullyPopulated().toBuilder().businessDate(LocalDate.of(2026, 10, 6)).build()),
                "A different date must not be replayed.");
        // one nanosecond apart, which also shows whether sub-second precision survives being stored
        assertFalse(
                store.matchesStoredRequest(key, "",
                        fullyPopulated().toBuilder().timestamp(OffsetDateTime.parse("2026-10-05T11:22:33.123456788Z")).build()),
                "A different timestamp must not be replayed.");

        // the case a comparison through a double would miss: a difference past the 17th significant digit
        assertFalse(
                store.matchesStoredRequest(key, "",
                        fullyPopulated().toBuilder().preciseAmount(new BigDecimal("12345678901234567890.12345678902")).build()),
                "Two amounts that differ beyond a double's precision are different requests.");
    }

    @Test
    public void aDecimalKeepsItsScaleWhenComparedAgainstAStoredRequest() {
        var key = UUID.randomUUID().toString();
        var command = command(key, "hello");

        command.setPayload(fullyPopulated());

        store.claim(command);

        // 1.50 and 1.5 are the same number but not the same request body, and the conservative answer is the safe one
        assertFalse(store.matchesStoredRequest(key, "", fullyPopulated().toBuilder().amount(new BigDecimal("100.0")).build()),
                "An amount written with a different scale must not be taken for the stored one.");
    }

    @Test
    public void claimSucceedsOnceAndSetsTheCommandId() {
        var key = UUID.randomUUID().toString();
        var first = command(key, "hello");

        assertTrue(store.claim(first), "The first claim of an idempotency key should succeed.");
        assertNotNull(first.getCommandId(), "A successful claim should assign the command id.");
        assertTrue(store.existsByKey(key, ""), "A claimed key should be visible to other requests immediately.");

        assertFalse(store.claim(command(key, "hello")), "A second claim of the same idempotency key should fail.");
    }

    @Test
    public void aClaimIsAlwaysStoredWithItsTime() {
        var key = UUID.randomUUID().toString();
        var command = command(key, "hello");

        // as when the timestamp hook is switched off
        command.setCreatedAt(null);

        assertTrue(store.claim(command));
        assertNotNull(command.getCreatedAt(), "The claim time must be kept on the command, so the audit hooks write it back.");
        assertNotNull(store.getReplayByKey(key, "").claimedAt(),
                "Without a claim time an abandoned claim can never be told from a running one, and is answered 425 for ever.");
    }

    @Test
    public void aKeyIsUniqueTogetherWithItsDistinctor() {
        var key = UUID.randomUUID().toString();
        var loan = command(key, "hello");
        var client = command(key, "hello");
        var loanAgain = command(key, "hello");

        loan.setCustomIdempotencyDistinctor("LOAN|CREATE");
        client.setCustomIdempotencyDistinctor("CLIENT|CREATE");
        loanAgain.setCustomIdempotencyDistinctor("LOAN|CREATE");

        assertTrue(store.claim(loan), "The first claim of a key under a distinctor should succeed.");
        assertTrue(store.claim(client), "The same key under another distinctor is a different command.");
        assertFalse(store.claim(loanAgain), "The same key under the same distinctor is a repeat.");

        assertTrue(store.existsByKey(key, "LOAN|CREATE"));
        assertTrue(store.existsByKey(key, "CLIENT|CREATE"));
        assertFalse(store.existsByKey(key, ""), "A key claimed only under distinctors is not claimed under none.");
    }

    @Test
    public void aRepeatIsAnsweredFromTheRowWithItsOwnDistinctor() {
        var key = UUID.randomUUID().toString();
        var loan = processed(key, "loan");
        var client = command(key, "client");

        loan.getCommand().setCustomIdempotencyDistinctor("LOAN|CREATE");
        client.setCustomIdempotencyDistinctor("CLIENT|CREATE");

        store.claim(loan.getCommand());
        store.store(loan);
        store.claim(client);
        store.recordResponse(key, "CLIENT|CREATE", 400, "{\"error\":\"bad request\"}");

        assertEquals(PROCESSED, store.getReplayByKey(key, "LOAN|CREATE").state());
        assertEquals("LOAN", ((DummyResponse) store.getReplayByKey(key, "LOAN|CREATE").response()).getContent());
        assertNull(store.getReplayByKey(key, "LOAN|CREATE").statusCode(), "Recording the other row's answer must not touch this one.");

        assertEquals(UNDER_PROCESSING, store.getReplayByKey(key, "CLIENT|CREATE").state());
        assertEquals(400, store.getReplayByKey(key, "CLIENT|CREATE").statusCode());

        assertTrue(store.matchesStoredRequest(key, "LOAN|CREATE", DummyRequest.builder().content("loan").build()));
        assertFalse(store.matchesStoredRequest(key, "LOAN|CREATE", DummyRequest.builder().content("client").build()),
                "A request is compared only with the request stored under the same distinctor.");
        assertEquals(UNKNOWN, store.getReplayByKey(key, "").state(), "No row was stored under the empty distinctor.");
    }

    @Test
    public void aCommandWithNoDistinctorIsStoredWithTheEmptyOne() {
        var key = UUID.randomUUID().toString();
        var command = command(key, "hello");

        command.setCustomIdempotencyDistinctor(null);

        assertTrue(store.claim(command), "The column does not take null, so a missing distinctor has to be stored as empty.");
        assertEquals("", store.getByKey(key, "").getCustomIdempotencyDistinctor());
    }

    @Test
    public void commandsWithoutAKeyNeverConflict() {
        assertTrue(store.claim(command(null, "hello")), "A command without an idempotency key should always be claimable.");
        assertTrue(store.claim(command(null, "hello")), "Commands without an idempotency key should not conflict with each other.");
    }

    @Test
    public void onlyOneConcurrentClaimOfTheSameKeyWins() throws Exception {
        var threads = 8;
        var key = UUID.randomUUID().toString();
        var startLine = new CountDownLatch(1);
        var winners = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(threads)) {
            var attempts = IntStream.range(0, threads).mapToObj(i -> executor.submit(() -> {
                startLine.await();

                if (store.claim(command(key, "hello"))) {
                    winners.incrementAndGet();
                }

                return null;
            })).toList();

            startLine.countDown();

            for (var attempt : attempts) {
                attempt.get(30, TimeUnit.SECONDS);
            }
        }

        assertEquals(1, winners.get(), "Exactly one concurrent request may claim a key; the rest must be told to stand down.");
        assertEquals(1,
                repository.findAll().stream()
                        .filter(e -> key.equals(e.getIdempotencyKey()) && "".equals(e.getCustomIdempotencyDistinctor())).count(),
                "Only one command row may exist per idempotency key and distinctor.");
    }

    @Test
    public void theStateOfAKeyTellsAnInFlightCommandFromAFinishedOne() {
        var key = UUID.randomUUID().toString();
        var command = command(key, "hello");

        assertEquals(UNKNOWN, store.getReplayByKey(key, "").state(), "An unclaimed key has no state.");

        store.claim(command);

        var inFlight = store.getReplayByKey(key, "");
        assertEquals(UNDER_PROCESSING, inFlight.state(), "A claimed key should report the command as in flight.");
        assertNotNull(inFlight.claimedAt(), "When the key was claimed is what tells a slow command from an abandoned one.");
        assertNull(inFlight.statusCode(), "An in-flight command has not been answered yet.");
        assertNull(inFlight.response(), "An in-flight command has nothing to replay.");

        store.store(CommandContext.<DummyRequest, DummyResponse>builder().command(command)
                .response(DummyResponse.builder().content("HELLO").build()).state(PROCESSED).build());
        store.recordResponse(key, "", 201, null);

        var processed = store.getReplayByKey(key, "");
        assertEquals(PROCESSED, processed.state(), "A completed command should report as processed.");
        assertEquals("HELLO", ((DummyResponse) processed.response()).getContent(),
                "The result the original command produced must be replayable.");
        assertNull(processed.statusCode(), "A command that stored a result needs no status: its repeat is answered by "
                + "handing that result back, and the web layer derives the status from it as it did the first time.");
    }

    @Test
    public void aFailedCommandReportsAsFailedRatherThanAsMissing() {
        var key = UUID.randomUUID().toString();
        var command = command(key, "hello");

        store.claim(command);
        store.store(CommandContext.<DummyRequest, DummyResponse>builder().command(command).state(ERROR).build());
        // a failed command produces no result of its own, so the error document the client got is what fills the gap
        store.recordResponse(key, "", 400, "{\"error\":\"bad request\"}");

        var failed = store.getReplayByKey(key, "");
        assertEquals(ERROR, failed.state(), "A failed command should report as failed.");
        assertEquals(400, failed.statusCode(), "The status the failure was answered with must be replayable.");
        assertEquals("{\"error\":\"bad request\"}", failed.response(),
                "With no result object to rehydrate, the failure replays as the text the client was sent.");
    }

    @Test
    public void nothingIsCapturedForACommandThatStoredItsOwnResult() {
        var key = UUID.randomUUID().toString();

        store.store(processed(key, "hello"));

        store.recordResponse(key, "", 200, "{\"content\":\"CAPTURED\"}");

        var replay = store.getReplayByKey(key, "");

        assertInstanceOf(DummyResponse.class, replay.response(), "A repeat is answered from the command's own result.");
        assertEquals("HELLO", ((DummyResponse) replay.response()).getContent());
        assertNull(replay.statusCode(), "Neither the captured answer nor its status is recorded over a stored result; "
                + "the result is handed back through the dispatcher and the web layer answers from it.");
    }

    @Test
    public void aCapturedAnswerIsReplayedByteForByteWhenThereIsNoResultToRehydrate() {
        var key = UUID.randomUUID().toString();

        // every shape that changes when a stored answer is parsed and re-rendered rather than kept as it was written:
        // trailing zeros on a decimal, a value past a double's precision, exponents, dates, times and nulls
        var answer = """
                {"amount":1.50,"rate":0.0100,"precise":12345678901234567890.12345678901,\
                "exponent":1.0E10,"whole":7,"big":9223372036854775807,"text":"x","unicode":"é",\
                "date":"2026-10-05","timestamp":"2026-10-05T11:22:33.123456789Z","nothing":null,\
                "nested":{"inner":0.100},"list":[1.10,2.20]}""";

        store.claim(command(key, "hello"));
        store.recordResponse(key, "", 200, answer);

        assertEquals(answer, store.getReplayByKey(key, "").response(),
                "A captured answer must replay exactly as the client was sent it, byte for byte.");
    }

    @Test
    public void storingTheCommandDoesNotResetTheRecordedStatus() {
        var key = UUID.randomUUID().toString();
        var command = command(key, "hello");

        store.claim(command);
        store.recordResponse(key, "", 400, "{\"error\":\"bad request\"}");

        // the audit hooks rebuild the row from the command, which has no notion of the status the client was sent
        store.store(CommandContext.<DummyRequest, DummyResponse>builder().command(command).state(ERROR).build());

        assertEquals(400, store.getReplayByKey(key, "").statusCode(), "A full-row save must not wipe the recorded status.");
    }

    @Test
    public void theCommandRecordsWhatItActedOnAndWhatItDid() {
        var key = UUID.randomUUID().toString();

        store.claim(command(key, "hello"));

        var claimed = repository.findOneByIdempotencyKeyAndCustomIdempotencyDistinctor(key, "").orElseThrow();

        // the names an audit search and a checker inbox look a command up by, and from which the approval permission
        // is derived; a row without them is invisible to both
        assertEquals("DUMMY", claimed.getEntityName(), "The entity the command acted on must be recorded.");
        assertEquals("CREATE", claimed.getActionName(), "The action it performed must be recorded.");
    }

    private static CommandContext<DummyRequest, DummyResponse> processed(String key, String content) {
        return CommandContext.<DummyRequest, DummyResponse>builder().command(command(key, content))
                .response(DummyResponse.builder().content(content.toUpperCase(Locale.ROOT)).build()).state(PROCESSED).build();
    }

    /** Every field type a request realistically carries, so a comparison cannot quietly ignore one of them. */
    private static DummyRequest fullyPopulated() {
        return DummyRequest.builder().content("hello").referenceId(7L).count(1).flag(true).amount(new BigDecimal("100.00"))
                .preciseAmount(new BigDecimal("12345678901234567890.12345678901")).businessDate(LocalDate.of(2026, 10, 5))
                .timestamp(OffsetDateTime.parse("2026-10-05T11:22:33.123456789Z")).build();
    }

    private static DummyCommand command(String key, String content) {
        var now = Instant.now();
        var command = new DummyCommand();

        command.setPayload(DummyRequest.builder().content(content).build());
        command.setIdempotencyKey(key);
        command.setIpAddress("127.0.0.1");
        command.setCreatedAt(now);
        command.setUpdatedAt(now);
        command.setExecutedAt(now);
        command.setInitiatedByUsername("abc");
        command.setExecutedByUsername("abc");

        return command;
    }
}
