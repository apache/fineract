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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.stereotype.Component;

/**
 * Same as {@link DatabaseUnavailableExceptionMapper}, for the exception Spring throws when the database is temporarily
 * unavailable (for example {@code SQLTransientConnectionException}). It is retried by the {@code tenantContextLookup}
 * retry, so when the retries run out the client gets 503 rather than 500. A separate class is needed because a JAX-RS
 * mapper handles one exception type.
 */
@Provider
@Component
@Scope("singleton")
public class DatabaseTransientResourceUnavailableExceptionMapper
        implements FineractExceptionMapper, ExceptionMapper<TransientDataAccessResourceException> {

    @Override
    public Response toResponse(final TransientDataAccessResourceException exception) {
        return DatabaseUnavailableExceptionMapper.buildResponse(exception);
    }

    @Override
    public int errorCode() {
        return DatabaseUnavailableSupport.ERROR_CODE;
    }
}
