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
package org.apache.fineract.infrastructure.core.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.exceptionmapper.DatabaseUnavailableSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.CannotCreateTransactionException;

class DatabaseUnavailableFilterTest {

    private final DatabaseUnavailableFilter underTest = new DatabaseUnavailableFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/fineract-provider/api/v1/clients");
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    static Stream<Exception> databaseDownExceptions() {
        return Stream.of(new CannotGetJdbcConnectionException("no connection"), new CannotCreateTransactionException("no connection"),
                new TransientDataAccessResourceException("connection is not available"),
                new ServletException(new CannotGetJdbcConnectionException("wrapped in a servlet exception")),
                new IllegalStateException("outer", new CannotCreateTransactionException("nested cause")));
    }

    @ParameterizedTest
    @MethodSource("databaseDownExceptions")
    void databaseDownIsServiceUnavailable(final Exception exception) throws Exception {
        underTest.doFilter(request, response, throwing(exception));

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getHeader("Retry-After")).isEqualTo(DatabaseUnavailableSupport.RETRY_AFTER_SECONDS);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains(DatabaseUnavailableSupport.MESSAGE_CODE)
                .contains(DatabaseUnavailableSupport.MESSAGE);
    }

    @Test
    void otherExceptionsAreRethrownUntouched() {
        final IllegalStateException other = new IllegalStateException("not a database problem");

        assertThatThrownBy(() -> underTest.doFilter(request, response, throwing(other))).isSameAs(other);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Retry-After")).isNull();
    }

    @Test
    void committedResponseIsNotTouched() {
        final CannotGetJdbcConnectionException exception = new CannotGetJdbcConnectionException("no connection");
        response.setCommitted(true);

        assertThatThrownBy(() -> underTest.doFilter(request, response, throwing(exception))).isSameAs(exception);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Retry-After")).isNull();
    }

    @Test
    void successfulRequestPassesThrough() throws Exception {
        final MockFilterChain chain = new MockFilterChain();

        underTest.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void runsAheadOfSpringSecurity() {
        // Spring Security's filter chain registration has the order -100
        assertThat(underTest.getOrder()).isLessThan(-100);
    }

    private static FilterChain throwing(final Exception exception) {
        return (req, res) -> {
            if (exception instanceof ServletException servletException) {
                throw servletException;
            }
            throw (RuntimeException) exception;
        };
    }
}
