package org.mifosng.platform.api.errorhandling;

import java.util.Arrays;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ErrorResponseList;
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
		ErrorResponse errorResponse = new ErrorResponse("error.msg.not.authorized", "id");
		ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
		
		return Response.status(Status.UNAUTHORIZED).entity(list).build();
	}
}