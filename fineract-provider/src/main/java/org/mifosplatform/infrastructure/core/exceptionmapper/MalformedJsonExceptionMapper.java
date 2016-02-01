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

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.stream.MalformedJsonException;

/**
 * An {@link ExceptionMapper} to map {@link MalformedJsonException} thrown by
 * platform into a HTTP API friendly format.
 */
@Provider
@Component
@Scope("singleton")
public class MalformedJsonExceptionMapper implements ExceptionMapper<MalformedJsonException> {

    @Override
    public Response toResponse(@SuppressWarnings("unused") final MalformedJsonException exception) {

        final String globalisationMessageCode = "error.msg.invalid.request.body";
        final String defaultUserMessage = "The JSON provided in the body of the request is invalid or missing.";

        final ApiParameterError error = ApiParameterError.generalError(globalisationMessageCode, defaultUserMessage);

        return Response.status(Status.BAD_REQUEST).entity(error).type(MediaType.APPLICATION_JSON).build();
    }
}