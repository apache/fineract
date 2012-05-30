package org.mifosng.platform.api.errorhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosng.platform.api.data.ApiGlobalErrorResponse;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link PlatformDataIntegrityException} thrown by platform into a HTTP API friendly format.
 * 
 * The {@link PlatformDataIntegrityException} is thrown when modifying api call result in data integrity checks to be fired.
 */
@Provider
@Component
@Scope("singleton")
public class PlatformDataIntegrityExceptionMapper implements ExceptionMapper<PlatformDataIntegrityException> {

	@Override
	public Response toResponse(PlatformDataIntegrityException exception) {
		
		ApiGlobalErrorResponse dataIntegrityError = ApiGlobalErrorResponse.dataIntegrityError(exception.getGlobalisationMessageCode(), exception.getDefaultUserMessage(), 
																									exception.getParameterName(), exception.getDefaultUserMessageArgs());
		
		return Response.status(Status.BAD_REQUEST).entity(dataIntegrityError).build();
	}
}