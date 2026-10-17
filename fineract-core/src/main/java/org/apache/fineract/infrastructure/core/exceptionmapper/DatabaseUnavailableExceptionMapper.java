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

import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} that answers with HTTP 503 when no database connection could be obtained (the database is
 * down or the connection pool is exhausted). It used to be answered with a generic HTTP 500, which tells a client that
 * the request itself failed; a 503 tells it that the service is temporarily unavailable and the request can be retried.
 */
@Provider
@Component
@Scope("singleton")
@Slf4j
public class DatabaseUnavailableExceptionMapper implements FineractExceptionMapper, ExceptionMapper<DataAccessResourceFailureException> {

    static final String RETRY_AFTER_SECONDS = "5";

    @Override
    public Response toResponse(final DataAccessResourceFailureException exception) {
        log.warn("Exception occurred", ErrorHandler.findMostSpecificException(exception));
        return databaseUnavailable();
    }

    static Response databaseUnavailable() {
        final ApiGlobalErrorResponse error = ApiGlobalErrorResponse.serviceUnavailable("error.msg.database.unavailable",
                "The database is currently not available");
        return Response.status(SC_SERVICE_UNAVAILABLE).header("Retry-After", RETRY_AFTER_SECONDS).entity(error)
                .type(MediaType.APPLICATION_JSON).build();
    }

    @Override
    public int errorCode() {
        return 5030;
    }
}
