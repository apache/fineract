package org.mifosng.platform.api.errorhandling;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.mifosng.platform.api.data.ApiGlobalErrorResponse;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.UnrecognizedQueryParamException;
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
public class UnrecognizedQueryParamExceptionMapper implements ExceptionMapper<UnrecognizedQueryParamException> {

	@Override
	public Response toResponse(UnrecognizedQueryParamException exception) {
		
		String parameterName = exception.getQueryParamKey();
		String parameterValue = exception.getQueryParamValue();
		
		StringBuilder validationErrorCode = new StringBuilder("error.msg.query.parameter.value.unsupported");
		StringBuilder defaultEnglishMessage = new StringBuilder("The query parameter ").append(parameterName).append(" has an unsupported value of: ").append(parameterValue);
		ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameterName, parameterName, parameterValue);
		
		List<ApiParameterError> errors = new ArrayList<ApiParameterError>();		
		errors.add(error);
		
		ApiGlobalErrorResponse invalidParameterError = ApiGlobalErrorResponse.badClientRequest(
				"validation.msg.validation.errors.exist",
				"Validation errors exist.", errors);
		
		return Response.status(Status.BAD_REQUEST).entity(invalidParameterError).build();
	}
}