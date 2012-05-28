package org.mifosng.platform.api.errorhandling;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.mifosng.data.ApiGlobalErrorResponse;
import org.mifosng.data.ApiParameterError;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link UnrecognizedPropertyException} thrown by platform into a HTTP API friendly format.
 * 
 * The {@link UnrecognizedPropertyException} is typically thrown when a parameter is passed during and post or put that is not expected.
 */
@Provider
@Component
@Scope("singleton")
public class UnrecognizedPropertyExceptionMapper implements ExceptionMapper<UnrecognizedPropertyException> {

	@Override
	public Response toResponse(UnrecognizedPropertyException exception) {
		
		String parameterName = exception.getUnrecognizedPropertyName();
		
		StringBuilder validationErrorCode = new StringBuilder("error.msg.parameter.").append(parameterName).append(".unsupported");
		StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameterName).append(" is not supported.");
		ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameterName);
		
		List<ApiParameterError> errors = new ArrayList<ApiParameterError>();		
		errors.add(error);
		
		ApiGlobalErrorResponse invalidParameterError = ApiGlobalErrorResponse.badClientRequest(
				"validation.msg.validation.errors.exist",
				"Validation errors exist.", errors);
		
		return Response.status(Status.BAD_REQUEST).entity(invalidParameterError).build();
	}
}