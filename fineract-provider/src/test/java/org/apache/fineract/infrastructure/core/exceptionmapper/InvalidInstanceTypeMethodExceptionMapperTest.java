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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.security.exception.InvalidInstanceTypeMethodException;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link InvalidInstanceTypeMethodExceptionMapper}.
 */
public class InvalidInstanceTypeMethodExceptionMapperTest {

    /**
     * Tests the {@link InvalidInstanceTypeMethodExceptionMapper#toResponse(InvalidInstanceTypeMethodException)} method
     * for successful scenario.
     */
    @Test
    public void testToResponse() {
        final InvalidInstanceTypeMethodExceptionMapper exceptionMapper = new InvalidInstanceTypeMethodExceptionMapper();
        final InvalidInstanceTypeMethodException exception = new InvalidInstanceTypeMethodException(HttpMethod.POST);

        final Response response = exceptionMapper.toResponse(exception);

        assertNotNull(response);
        assertEquals(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        final ApiGlobalErrorResponse errorResponse = (ApiGlobalErrorResponse) response.getEntity();
        assertEquals(String.valueOf(Response.Status.METHOD_NOT_ALLOWED.getStatusCode()), errorResponse.getHttpStatusCode());
        assertEquals("Invalid instance type called in api request for the method POST", errorResponse.getDeveloperMessage());
        assertEquals("error.msg.invalid.instance.type", errorResponse.getUserMessageGlobalisationCode());
        assertEquals("Invalid method POST used with request to this instance type.", errorResponse.getDefaultUserMessage());
    }

}
