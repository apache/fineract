package org.mifosng.platform.api.errorhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosng.platform.api.data.ApiGlobalErrorResponse;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link UnAuthenticatedUserException} thrown by platform into a HTTP API friendly format.
 */
@Provider
@Component
@Scope("singleton")
public class UnAuthenticatedUserExceptionMapper implements ExceptionMapper<UnAuthenticatedUserException> {

	@Override
	public Response toResponse(UnAuthenticatedUserException exception) {
		// Status code 401 really reads as: "Unauthenticated":
		return Response.status(Status.UNAUTHORIZED).entity(ApiGlobalErrorResponse.unAuthenticated()).build();
	}
}