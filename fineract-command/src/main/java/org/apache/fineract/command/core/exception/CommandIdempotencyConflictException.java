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

/**
 * Thrown when a request repeats an idempotency key for a different request than the one stored under it. Replaying the
 * stored response would answer a question that was never asked, so the request is rejected instead.
 */
public class CommandIdempotencyConflictException extends AbstractCommandIdempotencyException {

    /** 409 Conflict - the key is in use by a different request. */
    public static final int STATUS_CODE = 409;

    public CommandIdempotencyConflictException(String idempotencyKey, Object response) {
        super(idempotencyKey, STATUS_CODE, response);
    }

    public CommandIdempotencyConflictException(String idempotencyKey, Object response, Throwable cause) {
        super(idempotencyKey, STATUS_CODE, response, cause);
    }
}
