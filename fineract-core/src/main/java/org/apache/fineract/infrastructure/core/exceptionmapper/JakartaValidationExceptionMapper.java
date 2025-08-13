/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.exceptionmapper;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Provider
@Component
@RequiredArgsConstructor
public class JakartaValidationExceptionMapper implements FineractExceptionMapper, ExceptionMapper<ConstraintViolationException> {

    private final MessageSource messageSource;

    @Override
    public Response toResponse(final ConstraintViolationException exception) {
        log.warn("Exception occurred", ErrorHandler.findMostSpecificException(exception));
        final ApiGlobalErrorResponse dataValidationErrorResponse = ApiGlobalErrorResponse
                .badClientRequest("validation.msg.validation.errors.exist", "Validation errors exist.", getApiParameterErrors(exception));

        return Response.status(Status.BAD_REQUEST).entity(dataValidationErrorResponse).type(MediaType.APPLICATION_JSON).build();
    }

    @Override
    public int errorCode() {
        return 2002;
    }

    private List<ApiParameterError> getApiParameterErrors(final ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream().map(violation -> {
            final String messageTemplate = violation.getMessageTemplate();
            final String messageKey = messageTemplate.replace("{", "").replace("}", "");

            final String interpolatedMessage = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());

            return ApiParameterError.parameterError(messageKey, interpolatedMessage, violation.getPropertyPath().toString());
        }).toList();
    }

}
