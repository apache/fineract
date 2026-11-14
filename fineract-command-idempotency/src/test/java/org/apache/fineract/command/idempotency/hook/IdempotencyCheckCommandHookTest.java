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
package org.apache.fineract.command.idempotency.hook;

import static org.apache.fineract.command.core.CommandState.ERROR;
import static org.apache.fineract.command.core.CommandState.PROCESSED;
import static org.apache.fineract.command.core.CommandState.UNDER_PROCESSING;
import static org.apache.fineract.command.core.CommandState.UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandReplay;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.core.exception.CommandIdempotencyConflictException;
import org.apache.fineract.command.core.exception.CommandIdempotencyFailedException;
import org.apache.fineract.command.core.exception.CommandIdempotencyInProgressException;
import org.apache.fineract.command.core.exception.CommandIdempotencyProcessedException;
import org.apache.fineract.command.idempotency.distinctor.CustomIdempotencyDistinctor;
import org.apache.fineract.command.idempotency.distinctor.EntityAndActionNameCustomIdempotencyDistinctor;
import org.apache.fineract.command.idempotency.distinctor.NoCustomIdempotencyDistinctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdempotencyCheckCommandHookTest {

    private static final String KEY = "7f3a1c04-0000-0000-0000-000000000000";

    @Mock
    private CommandStore store;

    private IdempotencyCheckCommandHook hook;

    @BeforeEach
    void setUp() {
        hook = new IdempotencyCheckCommandHook(store, new NoCustomIdempotencyDistinctor());
    }

    @Test
    void aCommandWithoutAKeyIsNeverClaimed() {
        var ctx = context(null);

        hook.onBefore(ctx);

        verifyNoInteractions(store);
        assertFalse(ctx.isSkipExecution(), "A command without an idempotency key must be executed.");
    }

    @Test
    void winningTheClaimLetsTheCommandExecute() {
        var ctx = context(KEY);

        when(store.claim(any())).thenReturn(true);

        hook.onBefore(ctx);

        assertFalse(ctx.isSkipExecution(), "The request that claims the key must be executed.");
        verify(store, never()).getReplayByKey(anyString(), anyString());
    }

    @Test
    void anUnfinishedCommandAnswersTooEarly() {
        var ctx = context(KEY);

        duplicate(new CommandReplay(UNDER_PROCESSING, null, null));

        var exception = assertThrows(CommandIdempotencyInProgressException.class, () -> hook.onBefore(ctx));

        assertEquals(425, exception.getStatusCode(), "An unfinished command must answer 425 Too Early.");
        assertTrue(ctx.isSkipExecution(), "An in-flight command must not be executed a second time.");
        // the request is not compared here: there is nothing stored to compare a response against yet
        verify(store, never()).matchesStoredRequest(anyString(), anyString(), any());
    }

    @Test
    void aProcessedCommandIsReplayedByHandingBackItsOwnResult() {
        var ctx = context(KEY);

        var stored = Map.of("resourceId", 7);

        duplicate(new CommandReplay(PROCESSED, null, stored));
        when(store.matchesStoredRequest(KEY, "", "request-payload")).thenReturn(true);

        hook.onBefore(ctx);

        // nothing is thrown: the dispatcher returns this to the resource, which returns it as it returned the
        // original, so the web layer renders the status and the bytes exactly as it did the first time
        assertSame(stored, ctx.getResponse(), "A repeat is answered with the command's own stored result.");
        assertTrue(ctx.isSkipExecution(), "A duplicate command must not be executed again.");
    }

    @Test
    void aProcessedCommandWithOnlyACapturedAnswerIsThrownWithItsRecordedStatus() {
        var ctx = context(KEY);

        // no result object to hand back, so the answer captured as it went out is replayed as the text it is, and
        // the status has to come from the record because nothing else can derive it
        duplicate(new CommandReplay(PROCESSED, 201, "{\"resourceId\":7}"));
        when(store.matchesStoredRequest(KEY, "", "request-payload")).thenReturn(true);

        var exception = assertThrows(CommandIdempotencyProcessedException.class, () -> hook.onBefore(ctx));

        assertEquals(201, exception.getStatusCode(), "A replay must answer with the original status.");
        assertEquals("{\"resourceId\":7}", exception.getResponse());
    }

    @Test
    void aFailedCommandIsReplayedAsTheSameFailure() {
        var ctx = context(KEY);

        var stored = Map.of("error", "bad request");

        duplicate(new CommandReplay(ERROR, 400, stored));
        when(store.matchesStoredRequest(KEY, "", "request-payload")).thenReturn(true);

        // this is what the old processing does: a repeat of a failure fails identically rather than being retried
        var exception = assertThrows(CommandIdempotencyFailedException.class, () -> hook.onBefore(ctx));

        assertEquals(400, exception.getStatusCode(), "A repeat of a failed command must fail with the original status.");
        assertSame(stored, exception.getResponse(), "A repeat of a failed command must return what was stored for it.");
        assertTrue(ctx.isSkipExecution(), "A failed command must not be executed again by a repeat.");
    }

    @Test
    void aKeyReusedForADifferentRequestConflicts() {
        var ctx = context(KEY);

        duplicate(new CommandReplay(PROCESSED, 200, Map.of()));
        when(store.matchesStoredRequest(KEY, "", "request-payload")).thenReturn(false);

        var exception = assertThrows(CommandIdempotencyConflictException.class, () -> hook.onBefore(ctx));

        assertEquals(409, exception.getStatusCode(), "Reusing a key for a different request is a conflict, not a replay.");
    }

    @Test
    void anUnrecognisedStateConflictsRatherThanGuessing() {
        var ctx = context(KEY);

        duplicate(new CommandReplay(UNKNOWN, null, null));
        when(store.matchesStoredRequest(KEY, "", "request-payload")).thenReturn(true);

        assertThrows(CommandIdempotencyConflictException.class, () -> hook.onBefore(ctx));
    }

    @Test
    void aStoredResultThatCannotBeReconstructedIsRefusedRatherThanGuessedAt() {
        var ctx = context(KEY);

        when(store.claim(any())).thenReturn(false);
        when(store.getReplayByKey(KEY, "")).thenThrow(new IllegalStateException("Cannot rehydrate a stored command result"));

        var exception = assertThrows(CommandIdempotencyConflictException.class, () -> hook.onBefore(ctx));

        assertEquals(409, exception.getStatusCode(),
                "The key has been used and its answer cannot be produced, which is neither a success nor a failure of this request.");
        assertTrue(String.valueOf(exception.getResponse()).contains("cannot be reconstructed"),
                "The answer must say what went wrong rather than coming back empty.");
        assertTrue(String.valueOf(exception.getResponse()).contains("was carried out"),
                "Only a completed command stores a result, so the answer must say the command took effect.");
        assertTrue(String.valueOf(exception.getResponse()).contains("Do not send it again"),
                "Inviting a retry under a new key would execute a command that already took effect a second time.");
    }

    private void duplicate(CommandReplay replay) {
        when(store.claim(any())).thenReturn(false);
        when(store.getReplayByKey(KEY, "")).thenReturn(replay);
    }

    @Test
    void theDistinctorIsOnTheCommandBeforeItsKeyIsClaimed() {
        var scoped = hookWith(new EntityAndActionNameCustomIdempotencyDistinctor());
        var ctx = context(KEY);

        when(store.claim(any())).thenAnswer(invocation -> {
            assertEquals("DOCUMENT|CREATE", invocation.<Command<?>>getArgument(0).getCustomIdempotencyDistinctor(),
                    "The key is unique together with the distinctor, so the claim has to carry it.");

            return true;
        });

        scoped.onBefore(ctx);

        assertEquals("DOCUMENT|CREATE", ctx.getCommand().getCustomIdempotencyDistinctor());
        assertFalse(ctx.isSkipExecution(), "A command that claimed its key must be executed.");
    }

    @Test
    void aRepeatIsLookedUpUnderItsOwnDistinctor() {
        var scoped = hookWith(new EntityAndActionNameCustomIdempotencyDistinctor());
        var ctx = context(KEY);
        var replay = new CommandReplay(ERROR, 400, "{\"error\":\"bad request\"}");

        when(store.claim(any())).thenReturn(false);
        when(store.getReplayByKey(KEY, "DOCUMENT|CREATE")).thenReturn(replay);
        when(store.matchesStoredRequest(KEY, "DOCUMENT|CREATE", "request-payload")).thenReturn(true);

        var exception = assertThrows(CommandIdempotencyFailedException.class, () -> scoped.onBefore(ctx));

        assertEquals(400, exception.getStatusCode(), "The repeat must be answered from the row with the same key and distinctor.");
    }

    @Test
    void aDistinctorThatReturnsNothingIsRefusedBeforeTheKeyIsClaimed() {
        var broken = hookWith(command -> null);

        var exception = assertThrows(IllegalStateException.class, () -> broken.onBefore(context(KEY)));

        assertTrue(exception.getMessage().contains("empty string"), "The message must say what to return instead.");
        verifyNoInteractions(store);
    }

    @Test
    void aDistinctorTooLongForItsColumnIsRefusedBeforeTheKeyIsClaimed() {
        var broken = hookWith(command -> "x".repeat(CustomIdempotencyDistinctor.MAX_LENGTH + 1));

        var exception = assertThrows(IllegalStateException.class, () -> broken.onBefore(context(KEY)));

        assertTrue(exception.getMessage().contains(String.valueOf(CustomIdempotencyDistinctor.MAX_LENGTH)),
                "The message must name the limit.");
        verifyNoInteractions(store);
    }

    private IdempotencyCheckCommandHook hookWith(CustomIdempotencyDistinctor distinctor) {
        return new IdempotencyCheckCommandHook(store, distinctor);
    }

    private static CommandContext<Object, Object> context(String key) {
        var command = new TestCommand();

        command.setIdempotencyKey(key);
        command.setPayload("request-payload");

        return CommandContext.builder().command(command).build();
    }

    /** Commands declare their names in code; a test fixture has to as well. */
    private static final class TestCommand extends Command<Object> {

        @Override
        public String entityName() {
            return "DOCUMENT";
        }

        @Override
        public String actionName() {
            return "CREATE";
        }
    }
}
