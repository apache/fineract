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

import static org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse.serverSideError;

import com.google.gson.Gson;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.exception.CommandFailedException;
import org.apache.fineract.infrastructure.core.exception.CommandProcessedException;
import org.apache.fineract.infrastructure.core.exception.CommandUnderProcessingException;
import org.apache.fineract.infrastructure.core.exception.DuplicateCommandException;
import org.springframework.stereotype.Component;

@Provider
@Component
@Slf4j
public class DuplicateCommandExceptionMapper implements ExceptionMapper<DuplicateCommandException> {

    @Override
    public Response toResponse(final DuplicateCommandException exception) {
        log.debug("Duplicate request: {}", exception.getMessage());
        if (exception instanceof CommandProcessedException) {
            return Response.status(Status.OK).entity(exception.getResponse()).header("x-served-from-cache", "true")
                    .type(MediaType.APPLICATION_JSON).build();
        } else if (exception instanceof CommandFailedException) {
            String response = exception.getResponse();
            Status statusCode = Status.BAD_REQUEST;
            try {
                Gson gson = new Gson();
                ApiGlobalErrorResponse apiGlobalErrorResponse = gson.fromJson(response, ApiGlobalErrorResponse.class);
                String httpStatusCode = apiGlobalErrorResponse.getHttpStatusCode();
                statusCode = Status.fromStatusCode(Integer.parseInt(httpStatusCode));
            } catch (Exception ex) {
                log.error("Can not deserialize exception message to ApiGlobalErrorResponse: {}, fallback to default status code...",
                        response);
            }
            return Response.status(statusCode).entity(response).header("x-served-from-cache", "true").type(MediaType.APPLICATION_JSON)
                    .build();
        } else if (exception instanceof CommandUnderProcessingException) {
            return Response.status(Status.CONFLICT).entity(exception.getResponse()).header("x-served-from-cache", "true")
                    .type(MediaType.APPLICATION_JSON).build();
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(serverSideError("500", "")).type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}
