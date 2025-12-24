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
package org.apache.fineract.client.feign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feign.Request;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeignExceptionTest {

    private Request request;
    private byte[] responseBody;

    @BeforeEach
    void setUp() {
        request = Request.create(Request.HttpMethod.GET, "/api/test", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        responseBody = "Error response body".getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void testConstructorWithErrorDetails() {
        String developerMessage = "Technical error details";
        String userMessage = "User-friendly error message";

        FeignException exception = new FeignException(400, "Error message", request, responseBody, developerMessage, userMessage);

        assertNotNull(exception);
        assertEquals(400, exception.status());
        assertEquals(developerMessage, exception.getDeveloperMessage());
        assertEquals(userMessage, exception.getUserMessage());
        assertEquals(request, exception.request());
        assertEquals(responseBody, exception.responseBody());
    }

    @Test
    void testGetMessage() {
        String developerMessage = "Developer error";
        String userMessage = "User error";

        FeignException exception = new FeignException(500, "Base message", request, responseBody, developerMessage, userMessage);

        String message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("500"));
        assertTrue(message.contains(userMessage));
        assertTrue(message.contains(developerMessage));
    }

    @Test
    void testGetDeveloperMessage() {
        String developerMessage = "This is a technical error";

        FeignException exception = new FeignException(400, "Error", request, responseBody, developerMessage, null);

        assertEquals(developerMessage, exception.getDeveloperMessage());
    }

    @Test
    void testGetUserMessage() {
        String userMessage = "This is a user-friendly error";

        FeignException exception = new FeignException(400, "Error", request, responseBody, null, userMessage);

        assertEquals(userMessage, exception.getUserMessage());
    }

    @Test
    void testNullErrorMessages() {
        FeignException exception = new FeignException(404, "Not found", request, responseBody, null, null);

        assertNull(exception.getDeveloperMessage());
        assertNull(exception.getUserMessage());
        String message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("404"));
        assertTrue(message.contains("Not found"));
    }

    @Test
    void testGetMessageWithOnlyUserMessage() {
        String userMessage = "User error only";

        FeignException exception = new FeignException(400, "Base message", request, responseBody, null, userMessage);

        String message = exception.getMessage();
        assertTrue(message.contains("400"));
        assertTrue(message.contains(userMessage));
    }

    @Test
    void testGetMessageWithOnlyDeveloperMessage() {
        String developerMessage = "Developer error only";

        FeignException exception = new FeignException(400, "Base message", request, responseBody, developerMessage, null);

        String message = exception.getMessage();
        assertTrue(message.contains("400"));
        assertTrue(message.contains(developerMessage));
    }

    @Test
    void testResponseBodyAsString() {
        FeignException exception = new FeignException(400, "Error", request, responseBody, null, null);

        String bodyString = exception.responseBodyAsString();
        assertNotNull(bodyString);
        assertEquals("Error response body", bodyString);
    }

    @Test
    void testResponseBodyAsStringWithNullBody() {
        FeignException exception = new FeignException(400, "Error", request, null, null, null);

        String bodyString = exception.responseBodyAsString();
        assertNull(bodyString);
    }
}
