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

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.exception.AbstractCommandIdempotencyException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * The Spring MVC counterpart of {@link IdempotencyCommandExceptionMapper}. Endpoints built on the command framework
 * exist on both web stacks, and a repeated request has to be answered the same way whichever one serves it.
 * <p>
 * Ordered ahead of any application-wide handler, which would otherwise turn a replay into an error response.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class IdempotencyCommandExceptionHandler {

    @ExceptionHandler(AbstractCommandIdempotencyException.class)
    public ResponseEntity<Object> handle(AbstractCommandIdempotencyException exception) {
        log.warn("Replaying idempotency key {} with status {}", exception.getIdempotencyKey(), exception.getStatusCode());

        return ResponseEntity.status(exception.getStatusCode()).header(IDEMPOTENT_CACHE_HEADER, "true")
                .contentType(MediaType.APPLICATION_JSON).body(exception.getResponse());
    }
}
