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
import org.mifosplatform.infrastructure.core.exception.PlatformInternalServerException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link PlatformInternalServerException}
 * thrown by platform into a HTTP API friendly format.
 * 
 * The {@link PlatformInternalServerException} is thrown when an api call
 * results in unexpected server side exceptions.
 */
@Provider
@Component
@Scope("singleton")
public class PlatformInternalServerExceptionMapper implements ExceptionMapper<PlatformInternalServerException> {

    @Override
    public Response toResponse(final PlatformInternalServerException exception) {

        final ApiGlobalErrorResponse notFoundErrorResponse = ApiGlobalErrorResponse.serverSideError(exception.getGlobalisationMessageCode(),
                exception.getDefaultUserMessage(), exception.getDefaultUserMessageArgs());
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(notFoundErrorResponse).type(MediaType.APPLICATION_JSON).build();
    }
}