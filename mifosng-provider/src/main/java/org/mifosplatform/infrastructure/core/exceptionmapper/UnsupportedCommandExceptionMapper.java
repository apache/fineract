/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exceptionmapper;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.infrastructure.core.data.ApiGlobalErrorResponse;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link UnsupportedCommandException} thrown
 * by platform into a HTTP API friendly format.
 */
@Provider
@Component
@Scope("singleton")
public class UnsupportedCommandExceptionMapper implements ExceptionMapper<UnsupportedCommandException> {

    @Override
    public Response toResponse(final UnsupportedCommandException exception) {

        final List<ApiParameterError> errors = new ArrayList<>();

        final StringBuilder validationErrorCode = new StringBuilder("error.msg.command.unsupported");
        final StringBuilder defaultEnglishMessage = new StringBuilder("The command ").append(exception.getUnsupportedCommandName()).append(
                " is not supported.");
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                exception.getUnsupportedCommandName(), exception.getUnsupportedCommandName());

        errors.add(error);

        final ApiGlobalErrorResponse invalidParameterError = ApiGlobalErrorResponse.badClientRequest(
                "validation.msg.validation.errors.exist", "Validation errors exist.", errors);

        return Response.status(Status.BAD_REQUEST).entity(invalidParameterError).type(MediaType.APPLICATION_JSON).build();
    }
}