package org.mifosng.platform.api.errorhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosng.data.ApiGlobalErrorResponse;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link PlatformApiDataValidationException} thrown by platform into a HTTP API friendly format.
 * 
 * The {@link PlatformApiDataValidationException} is typically thrown in data validation of the parameters passed in with an api request.
 */
@Provider
@Component
@Scope("singleton")
public class PlatformApiDataValidationExceptionMapper implements ExceptionMapper<PlatformApiDataValidationException> {

	@Override
	public Response toResponse(PlatformApiDataValidationException exception) {
		
		ApiGlobalErrorResponse dataIntegrityError = ApiGlobalErrorResponse.badClientRequest(exception.getGlobalisationMessageCode(), 
									exception.getDefaultUserMessage(), exception.getErrors());
		
		return Response.status(Status.BAD_REQUEST).entity(dataIntegrityError).build();
	}
}