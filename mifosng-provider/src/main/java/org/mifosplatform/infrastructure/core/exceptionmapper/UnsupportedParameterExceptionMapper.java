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

import org.mifosplatform.infrastructure.core.data.ApiGlobalErrorResponse;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link UnsupportedParameterException}
 * thrown by platform into a HTTP API friendly format.
 */
@Provider
@Component
@Scope("singleton")
public class UnsupportedParameterExceptionMapper implements ExceptionMapper<UnsupportedParameterException> {

    @Override
    public Response toResponse(final UnsupportedParameterException exception) {

        List<ApiParameterError> errors = new ArrayList<ApiParameterError>();

        for (String parameterName : exception.getUnsupportedParameters()) {
            StringBuilder validationErrorCode = new StringBuilder("error.msg.parameter.unsupported");
            StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameterName).append(" is not supported.");
            ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                    parameterName, parameterName);

            errors.add(error);
        }

        ApiGlobalErrorResponse invalidParameterError = ApiGlobalErrorResponse.badClientRequest("validation.msg.validation.errors.exist",
                "Validation errors exist.", errors);

        return Response.status(Status.BAD_REQUEST).entity(invalidParameterError).type(MediaType.APPLICATION_JSON).build();
    }
}