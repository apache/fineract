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
package org.apache.fineract.command.idempotency.web;

import static org.apache.fineract.command.core.exception.AbstractCommandIdempotencyException.IDEMPOTENT_CACHE_HEADER;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.exception.AbstractCommandIdempotencyException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Answers a repeated request with what the original one was answered with: the same status, the command's own result
 * object, plus a header saying it was served from the command store rather than executed again.
 *
 * <p>
 * The entity is the rehydrated result, not stored text, so Jersey renders the replay through the very mapper that
 * rendered the original answer. That is what makes the two identical, rather than an agreement between two mappers.
 */
@Slf4j
@Provider
@Component
@Scope("singleton")
public class IdempotencyCommandExceptionMapper implements ExceptionMapper<AbstractCommandIdempotencyException> {

    @Override
    public Response toResponse(AbstractCommandIdempotencyException exception) {
        log.warn("Replaying idempotency key {} with status {}", exception.getIdempotencyKey(), exception.getStatusCode());

        return Response.status(exception.getStatusCode()).entity(exception.getResponse()).header(IDEMPOTENT_CACHE_HEADER, "true")
                .type(MediaType.APPLICATION_JSON).build();
    }
}
