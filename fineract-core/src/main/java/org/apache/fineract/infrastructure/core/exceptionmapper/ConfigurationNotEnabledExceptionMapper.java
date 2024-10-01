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
public class ConfigurationNotEnabledExceptionMapper implements ExceptionMapper<ConfigurationNotEnabledException> {

    @Override
    public Response toResponse(final ConfigurationNotEnabledException exception) {
        log.warn("Exception occurred", ErrorHandler.findMostSpecificException(exception));
        final ApiGlobalErrorResponse notEnabledErrorResponse = ApiGlobalErrorResponse.notEnabled(exception.getGlobalisationMessageCode(),
                exception.getDefaultUserMessage(), exception.getDefaultUserMessageArgs());
        return Response.status(Response.Status.FORBIDDEN).entity(notEnabledErrorResponse).type(MediaType.APPLICATION_JSON).build();
    }
}
