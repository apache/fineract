/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exceptionmapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosplatform.infrastructure.core.data.ApiGlobalErrorResponse;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link AbstractPlatformDomainRuleException}
 * thrown by platform into a HTTP API friendly format.
 * 
 * The {@link AbstractPlatformDomainRuleException} is thrown when an api call
 * results is some internal business/domain logic been violated.
 */
@Provider
@Component
@Scope("singleton")
public class PlatformDomainRuleExceptionMapper implements ExceptionMapper<AbstractPlatformDomainRuleException> {

    @Override
    public Response toResponse(AbstractPlatformDomainRuleException exception) {

        ApiGlobalErrorResponse notFoundErrorResponse = ApiGlobalErrorResponse.domainRuleViolation(exception.getGlobalisationMessageCode(),
                exception.getDefaultUserMessage(), exception.getDefaultUserMessageArgs());
        // request understood but not carried out due to it violating some
        // domain/business logic
        return Response.status(Status.FORBIDDEN).entity(notFoundErrorResponse).type(MediaType.APPLICATION_JSON).build();
    }
}