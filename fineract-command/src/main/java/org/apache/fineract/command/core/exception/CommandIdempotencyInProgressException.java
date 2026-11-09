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
 * Thrown when a request repeats an idempotency key whose original command is still being processed. There is no result
 * to replay yet, so the caller is told it asked too early and should come back.
 */
public class CommandIdempotencyInProgressException extends AbstractCommandIdempotencyException {

    /** 425 Too Early - the original command has not finished, so retrying is the right thing to do. */
    public static final int STATUS_CODE = 425;

    public CommandIdempotencyInProgressException(String idempotencyKey, Object response) {
        super(idempotencyKey, STATUS_CODE, response);
    }
}
