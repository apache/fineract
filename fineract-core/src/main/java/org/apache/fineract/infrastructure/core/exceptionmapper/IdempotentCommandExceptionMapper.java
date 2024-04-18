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
package org.apache.fineract.infrastructure.core.exceptionmapper;

import static org.apache.fineract.infrastructure.core.exception.AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.exception.AbstractIdempotentCommandException;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessFailedException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessSucceedException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessUnderProcessingException;
import org.eclipse.persistence.exceptions.OptimisticLockException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link OptimisticLockException} thrown by platform into a HTTP API friendly format.
 */
@Provider
@Component
@Scope("singleton")
@Slf4j
public class IdempotentCommandExceptionMapper implements FineractExceptionMapper, ExceptionMapper<AbstractIdempotentCommandException> {

    @Override
    public Response toResponse(final AbstractIdempotentCommandException exception) {

        Integer status = null;
        if (exception instanceof IdempotentCommandProcessSucceedException pse) {
            log.debug("Request was served from idempotency cache");
            Integer statusCode = pse.getStatusCode();
            status = statusCode == null ? SC_OK : statusCode;
        }
        if (exception instanceof IdempotentCommandProcessUnderProcessingException) {
            log.warn("Request was still under processing", ErrorHandler.findMostSpecificException(exception));
            status = 425;
        } else if (exception instanceof IdempotentCommandProcessFailedException pfe) {
            log.warn("Exception occurred", ErrorHandler.findMostSpecificException(exception));
            status = pfe.getStatusCode();
        }
        if (status == null) {
            status = SC_INTERNAL_SERVER_ERROR;
        }
        return Response.status(status).entity(exception.getResponse()).header(IDEMPOTENT_CACHE_HEADER, "true")
                .type(MediaType.APPLICATION_JSON).build();
    }

    @Override
    public int errorCode() {
        return 4209;
    }
}
