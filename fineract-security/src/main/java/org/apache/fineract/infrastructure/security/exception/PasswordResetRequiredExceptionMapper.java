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
package org.apache.fineract.infrastructure.security.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.data.AuthenticatedUserData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link PasswordResetRequiredException} thrown during authentication into a HTTP API
 * friendly format.
 *
 * The exception is thrown when a user's credentials are valid but they must reset their password before proceeding.
 * This mapper returns a 403 FORBIDDEN response with the authenticated user data, including the
 * {@code shouldRenewPassword} flag set to true.
 */
@Provider
@Component
@Scope("singleton")
@Slf4j
@RequiredArgsConstructor
public class PasswordResetRequiredExceptionMapper implements ExceptionMapper<PasswordResetRequiredException> {

    private final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializer;

    @Override
    public Response toResponse(final PasswordResetRequiredException exception) {
        log.warn("Exception occurred", ErrorHandler.findMostSpecificException(exception));
        return Response.status(Status.FORBIDDEN).entity(apiJsonSerializer.serialize(exception.getAuthenticatedUserData()))
                .type(MediaType.APPLICATION_JSON).build();
    }
}
