package org.mifosng.platform.api.errorhandling;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosng.data.ApiGlobalErrorResponse;
import org.mifosng.platform.exceptions.AbstractPlatformDomainRuleException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link AbstractPlatformDomainRuleException} thrown by platform into a HTTP API friendly format.
 * 
 * The {@link AbstractPlatformDomainRuleException} is thrown when an api call results is some internal business/domain logic been violated.
 */
@Provider
@Component
@Scope("singleton")
public class PlatformDomainRuleExceptionMapper implements ExceptionMapper<AbstractPlatformDomainRuleException> {

	@Override
	public Response toResponse(AbstractPlatformDomainRuleException exception) {
		
		ApiGlobalErrorResponse notFoundErrorResponse = ApiGlobalErrorResponse.domainRuleViolation(exception.getGlobalisationMessageCode(), exception.getDefaultUserMessage(), exception.getDefaultUserMessageArgs());
		// request understood but not carried out due to it violating some domain/business logic
		return Response.status(Status.FORBIDDEN).entity(notFoundErrorResponse).build();
	}
}