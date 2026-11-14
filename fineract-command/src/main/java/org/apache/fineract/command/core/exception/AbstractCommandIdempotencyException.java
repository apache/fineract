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

import lombok.Getter;

/**
 * Base of the answers given to a request that repeats an idempotency key. The request is never executed again; it is
 * answered from what was stored for the original one, which is why every subclass carries a status code and a response
 * rather than a message.
 *
 * <p>
 * The response is the command's own result object, rehydrated from the command store, not pre-rendered text. Handing
 * the web layer an object lets it serialize the replay exactly as it serialized the original answer.
 */
@Getter
public abstract class AbstractCommandIdempotencyException extends RuntimeException {

    /** Set on every answer served from the command store, so a client can tell a replay from a fresh execution. */
    public static final String IDEMPOTENT_CACHE_HEADER = "x-served-from-cache";

    private final String idempotencyKey;
    private final int statusCode;
    private final Object response;

    protected AbstractCommandIdempotencyException(String idempotencyKey, int statusCode, Object response) {
        this(idempotencyKey, statusCode, response, null);
    }

    /**
     * Carries what went wrong underneath, for an answer that is given because the stored result could not be used
     * rather than because it could.
     */
    protected AbstractCommandIdempotencyException(String idempotencyKey, int statusCode, Object response, Throwable cause) {
        super("Idempotency key " + idempotencyKey + " was already used; answering with the stored result (" + statusCode + ")", cause);

        this.idempotencyKey = idempotencyKey;
        this.statusCode = statusCode;
        this.response = response;
    }
}
