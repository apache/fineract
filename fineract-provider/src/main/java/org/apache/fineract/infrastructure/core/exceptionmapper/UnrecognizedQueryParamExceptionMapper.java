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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link UnrecognizedQueryParamException}
 * thrown by platform into a HTTP API friendly format.
 * 
 * The {@link UnrecognizedQueryParamException} is typically thrown when a
 * parameter is passed during and post or put that is not expected.
 */
@Provider
@Component
@Scope("singleton")
public class UnrecognizedQueryParamExceptionMapper implements ExceptionMapper<UnrecognizedQueryParamException> {

    @Override
    public Response toResponse(final UnrecognizedQueryParamException exception) {

        final String parameterName = exception.getQueryParamKey();
        final String parameterValue = exception.getQueryParamValue();

        final StringBuilder validationErrorCode = new StringBuilder("error.msg.query.parameter.value.unsupported");
        final StringBuilder defaultEnglishMessage = new StringBuilder("The query parameter ") //
                .append(parameterName) //
                .append(" has an unsupported value of: ") //
                .append(parameterValue);

        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                parameterName, parameterName, parameterValue, exception.getSupportedParams());

        final List<ApiParameterError> errors = new ArrayList<>();
        errors.add(error);

        final ApiGlobalErrorResponse invalidParameterError = ApiGlobalErrorResponse.badClientRequest(
                "validation.msg.validation.errors.exist", "Validation errors exist.", errors);

        return Response.status(Status.BAD_REQUEST).entity(invalidParameterError).type(MediaType.APPLICATION_JSON).build();
    }
}