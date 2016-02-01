/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exceptionmapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosplatform.infrastructure.core.data.ApiGlobalErrorResponse;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link NoAuthorizationException} thrown by
 * platform into a HTTP API friendly format.
 * 
 * The {@link NoAuthorizationException} is thrown on platform when an attempt is
 * made to use functionality for which the user does have sufficient privileges.
 */
@Provider
@Component
@Scope("singleton")
public class NoAuthorizationExceptionMapper implements ExceptionMapper<NoAuthorizationException> {

    @Override
    public Response toResponse(final NoAuthorizationException exception) {
        // Status code 403 really reads as:
        // "Authenticated - but not authorized":
        final String defaultUserMessage = exception.getMessage();
        return Response.status(Status.FORBIDDEN).entity(ApiGlobalErrorResponse.unAuthorized(defaultUserMessage))
                .type(MediaType.APPLICATION_JSON).build();
    }
}