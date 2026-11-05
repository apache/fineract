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

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * Shared definition of the "database is not available" condition, used by the exception mappers and by the servlet
 * filter and authentication entry point that run before the JAX-RS layer, so that all of them answer the same way (HTTP
 * 503, Retry-After, same JSON body).
 */
@Slf4j
public final class DatabaseUnavailableSupport {

    public static final int ERROR_CODE = 5030;
    public static final String RETRY_AFTER_HEADER = "Retry-After";
    public static final String RETRY_AFTER_SECONDS = "5";
    public static final String MESSAGE_CODE = "error.msg.database.unavailable";
    public static final String MESSAGE = "The database is currently not available";

    private static final int MAX_CAUSE_DEPTH = 20;

    private DatabaseUnavailableSupport() {}

    /**
     * The exceptions the {@code tenantContextLookup} retry treats as a temporary outage. When the retries run out they
     * are answered with 503 as well, so a client gets the same answer however the outage surfaced.
     */
    public static boolean isDatabaseUnavailable(final Throwable t) {
        return t instanceof DataAccessResourceFailureException || t instanceof CannotCreateTransactionException
                || t instanceof TransientDataAccessResourceException;
    }

    /**
     * Walks the cause chain (some layers wrap the exception) and returns the first "database is not available"
     * exception, or null if there is none.
     */
    public static Throwable findDatabaseUnavailable(final Throwable t) {
        Throwable current = t;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (isDatabaseUnavailable(current)) {
                return current;
            }
            if (current.getCause() == current) {
                return null;
            }
            current = current.getCause();
        }
        return null;
    }

    public static ApiGlobalErrorResponse errorResponse() {
        return ApiGlobalErrorResponse.serviceUnavailable(MESSAGE_CODE, MESSAGE);
    }

    public static String rootCauseMessage(final Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage();
    }

    /**
     * Writes the same answer as the JAX-RS exception mappers (HTTP 503, Retry-After, JSON error body) for code that
     * runs before the JAX-RS layer. The caller must have checked that the response is not committed yet.
     */
    public static void writeServiceUnavailable(final HttpServletResponse response, final Throwable cause) throws IOException {
        log.warn("The database is not available: {}", rootCauseMessage(cause));
        log.debug("Database unavailable", cause);
        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setHeader(RETRY_AFTER_HEADER, RETRY_AFTER_SECONDS);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(errorResponse().toJson());
    }
}
