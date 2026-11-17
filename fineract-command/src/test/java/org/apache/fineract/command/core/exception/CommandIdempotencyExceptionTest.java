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
package org.apache.fineract.command.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * The status codes and header below are the contract the old command processing already answers with, so a client that
 * works against it keeps working against the new one.
 */
class CommandIdempotencyExceptionTest {

    private static final String KEY = "7f3a1c04";

    @Test
    void theReplayHeaderMatchesTheOneTheOldProcessingUses() {
        assertEquals("x-served-from-cache", AbstractCommandIdempotencyException.IDEMPOTENT_CACHE_HEADER,
                "Changing this header name would break every client that checks for a replay.");
    }

    @Test
    void anUnfinishedCommandAnswersTooEarly() {
        var stored = new Object();
        var exception = new CommandIdempotencyInProgressException(KEY, stored);

        assertEquals(425, exception.getStatusCode(), "An unfinished command must answer 425 Too Early.");
        assertSame(stored, exception.getResponse(), "The answer carries the result object, which the web layer renders.");
        assertEquals(KEY, exception.getIdempotencyKey(), "The answer must name the key it is about.");
    }

    @Test
    void aProcessedCommandReplaysItsOwnStatus() {
        assertEquals(201, new CommandIdempotencyProcessedException(KEY, 201, new Object()).getStatusCode(),
                "A replay must answer with the status the original was answered with.");
        assertEquals(200, new CommandIdempotencyProcessedException(KEY, null, new Object()).getStatusCode(),
                "A processed command with no recorded status falls back to 200.");
    }

    @Test
    void aFailedCommandReplaysItsOwnFailure() {
        var stored = new Object();
        var exception = new CommandIdempotencyFailedException(KEY, 400, stored);

        assertEquals(400, exception.getStatusCode(), "A repeat of a failed command must fail the same way.");
        assertSame(stored, exception.getResponse(), "A repeat of a failed command carries whatever was stored for it.");
        assertEquals(500, new CommandIdempotencyFailedException(KEY, null, null).getStatusCode(),
                "An inconsistent record answers 500 rather than nothing at all.");
    }

    @Test
    void aKeyUsedForAnotherRequestConflicts() {
        assertEquals(409, new CommandIdempotencyConflictException(KEY, null).getStatusCode(),
                "Reusing a key for a different request is a conflict, not a replay.");
    }
}
