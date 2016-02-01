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

import org.apache.fineract.commands.exception.UnsupportedCommandException;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link UnsupportedCommandException} thrown
 * by platform into a HTTP API friendly format.
 */
@Provider
@Component
@Scope("singleton")
public class UnsupportedCommandExceptionMapper implements ExceptionMapper<UnsupportedCommandException> {

    @Override
    public Response toResponse(final UnsupportedCommandException exception) {

        final List<ApiParameterError> errors = new ArrayList<>();

        final StringBuilder validationErrorCode = new StringBuilder("error.msg.command.unsupported");
        final StringBuilder defaultEnglishMessage = new StringBuilder("The command ").append(exception.getUnsupportedCommandName()).append(
                " is not supported.");
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                exception.getUnsupportedCommandName(), exception.getUnsupportedCommandName());

        errors.add(error);

        final ApiGlobalErrorResponse invalidParameterError = ApiGlobalErrorResponse.badClientRequest(
                "validation.msg.validation.errors.exist", "Validation errors exist.", errors);

        return Response.status(Status.BAD_REQUEST).entity(invalidParameterError).type(MediaType.APPLICATION_JSON).build();
    }
}