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

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;

class DatabaseUnavailableExceptionMapperTest {

    @Test
    void noJdbcConnectionIsAnServiceUnavailableWithRetryAfter() {
        DatabaseUnavailableExceptionMapper mapper = new DatabaseUnavailableExceptionMapper();

        Response response = mapper.toResponse(new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection"));

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getHeaderString("Retry-After")).isEqualTo("5");
        assertThat(response.getMediaType().toString()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getEntity()).isNotNull();
        assertThat(mapper.errorCode()).isEqualTo(5030);
    }

    @Test
    void aTransactionThatCannotBeStartedIsAnServiceUnavailableWithRetryAfter() {
        DatabaseTransactionUnavailableExceptionMapper mapper = new DatabaseTransactionUnavailableExceptionMapper();

        Response response = mapper.toResponse(new CannotCreateTransactionException("Could not open JPA EntityManager for transaction"));

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getHeaderString("Retry-After")).isEqualTo("5");
        assertThat(mapper.errorCode()).isEqualTo(5031);
    }
}
