package org.apache.fineract.infrastructure.core.exceptionmapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.exception.ConfigurationNotEnabledException;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Provider
@Component
@Scope("singleton")
@Slf4j
public class ConfigurationNotEnabledExceptionMapper implements ExceptionMapper<ConfigurationNotEnabledException>{
    @Override
    public Response toResponse(final ConfigurationNotEnabledException exception) {
        log.warn("Exception occurred", ErrorHandler.findMostSpecificException(exception));
        final ApiGlobalErrorResponse notEnabledErrorResponse = ApiGlobalErrorResponse.notEnabled(exception.getGlobalisationMessageCode(),
                exception.getDefaultUserMessage(), exception.getDefaultUserMessageArgs());
        return Response.status(Response.Status.FORBIDDEN).entity(notEnabledErrorResponse).type(MediaType.APPLICATION_JSON).build();
    }
}
