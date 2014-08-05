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
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map
 * {@link AbstractPlatformServiceUnavailableException} thrown by platform into a
 * HTTP API friendly format.
 * 
 * The {@link AbstractPlatformServiceUnavailableException} is thrown when an api
 * call for a resource that is expected to exist does not.
 */

@Provider
@Component
@Scope("singleton")
public class PlatformServiceUnavailableExceptionMapper implements ExceptionMapper<AbstractPlatformServiceUnavailableException> {

    @Override
    public Response toResponse(final AbstractPlatformServiceUnavailableException exception) {
        final ApiGlobalErrorResponse serviceUnavailableExceptionResponse = ApiGlobalErrorResponse.serviceUnavailable(
                exception.getGlobalisationMessageCode(), exception.getDefaultUserMessage(), exception.getDefaultUserMessageArgs());
        return Response.status(Status.SERVICE_UNAVAILABLE).entity(serviceUnavailableExceptionResponse).type(MediaType.APPLICATION_JSON)
                .build();
    }

}
