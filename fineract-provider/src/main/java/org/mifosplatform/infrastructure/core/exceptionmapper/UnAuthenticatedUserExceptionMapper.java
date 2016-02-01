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
import org.mifosplatform.useradministration.exception.UnAuthenticatedUserException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link UnAuthenticatedUserException} thrown
 * by platform into a HTTP API friendly format.
 */
@Provider
@Component
@Scope("singleton")
public class UnAuthenticatedUserExceptionMapper implements ExceptionMapper<UnAuthenticatedUserException> {

    @Override
    public Response toResponse(@SuppressWarnings("unused") final UnAuthenticatedUserException exception) {
        // Status code 401 really reads as: "Unauthenticated":
        return Response.status(Status.UNAUTHORIZED).entity(ApiGlobalErrorResponse.unAuthenticated()).type(MediaType.APPLICATION_JSON)
                .build();
    }
}