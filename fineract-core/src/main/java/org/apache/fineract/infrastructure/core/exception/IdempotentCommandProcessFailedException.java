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
package org.apache.fineract.infrastructure.core.exception;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

import jakarta.validation.constraints.NotNull;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandWrapper;

/**
 * Exception thrown when command is sent with same action, entity and idempotency key
 */
public class IdempotentCommandProcessFailedException extends AbstractIdempotentCommandException {

    private final Integer statusCode;

    public IdempotentCommandProcessFailedException(CommandWrapper wrapper, String idempotencyKey, CommandSource command) {
        super(wrapper.actionName(), wrapper.actionName(), idempotencyKey, command.getResult());
        this.statusCode = command.getResultStatusCode();
    }

    @NotNull
    public Integer getStatusCode() {
        // If the database inconsistent we return http 500 instead of null pointer exception
        return statusCode == null ? Integer.valueOf(SC_INTERNAL_SERVER_ERROR) : statusCode;
    }
}
