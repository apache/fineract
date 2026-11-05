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
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;

class DatabaseUnavailableExceptionMapperTest {

    @Test
    void noConnectionIsServiceUnavailable() {
        DatabaseUnavailableExceptionMapper mapper = new DatabaseUnavailableExceptionMapper();
        assertServiceUnavailable(mapper.toResponse(new CannotGetJdbcConnectionException("no connection")));
        assertThat(mapper.errorCode()).isEqualTo(DatabaseUnavailableSupport.ERROR_CODE);
    }

    @Test
    void transactionThatCannotStartIsServiceUnavailable() {
        DatabaseTransactionUnavailableExceptionMapper mapper = new DatabaseTransactionUnavailableExceptionMapper();
        assertServiceUnavailable(mapper.toResponse(new CannotCreateTransactionException("no connection")));
        assertThat(mapper.errorCode()).isEqualTo(DatabaseUnavailableSupport.ERROR_CODE);
    }

    @Test
    void transientResourceFailureIsServiceUnavailable() {
        DatabaseTransientResourceUnavailableExceptionMapper mapper = new DatabaseTransientResourceUnavailableExceptionMapper();
        assertServiceUnavailable(mapper.toResponse(new TransientDataAccessResourceException("connection is not available")));
        assertThat(mapper.errorCode()).isEqualTo(DatabaseUnavailableSupport.ERROR_CODE);
    }

    private void assertServiceUnavailable(Response response) {
        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getHeaderString("Retry-After")).isEqualTo("5");
        assertThat(response.getMediaType().toString()).isEqualTo(MediaType.APPLICATION_JSON);
        String json = ((ApiGlobalErrorResponse) response.getEntity()).toJson();
        assertThat(json).contains(DatabaseUnavailableSupport.MESSAGE_CODE).contains(DatabaseUnavailableSupport.MESSAGE);
    }
}
