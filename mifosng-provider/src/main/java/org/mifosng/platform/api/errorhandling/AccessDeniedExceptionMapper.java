package org.mifosng.platform.api.errorhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosng.data.ApiGlobalErrorResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link AccessDeniedException} thrown by platform into a HTTP API friendly format.
 * 
 * The {@link AccessDeniedException} is thrown by spring security on platform when an attempt is made to use functionality 
 * for which the user does have sufficient privileges.
 */
@Provider
@Component
@Scope("singleton")
public class AccessDeniedExceptionMapper implements ExceptionMapper<AccessDeniedException> {

	@Override
	public Response toResponse(AccessDeniedException exception) {
		// Status code 403 really reads as: "Authenticated - but not authorized":
		return Response.status(Status.FORBIDDEN).entity(ApiGlobalErrorResponse.unAuthorized()).build();
	}
}