package org.mifosng.platform.api.errorhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosng.data.ApiGlobalErrorResponse;
import org.mifosng.platform.exceptions.PlatformResourceNotFoundException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link PlatformResourceNotFoundException} thrown by platform into a HTTP API friendly format.
 * 
 * The {@link PlatformResourceNotFoundException} is thrown when an api call for a resource that is expected to exist does not.
 */
@Provider
@Component
@Scope("singleton")
public class PlatformResourceNotFoundExceptionMapper implements ExceptionMapper<PlatformResourceNotFoundException> {

	@Override
	public Response toResponse(PlatformResourceNotFoundException exception) {
		
		ApiGlobalErrorResponse notFoundErrorResponse = ApiGlobalErrorResponse.notFound(exception.getGlobalisationMessageCode(), exception.getDefaultUserMessage(), exception.getDefaultUserMessageArgs());
		return Response.status(Status.NOT_FOUND).entity(notFoundErrorResponse).build();
	}
}