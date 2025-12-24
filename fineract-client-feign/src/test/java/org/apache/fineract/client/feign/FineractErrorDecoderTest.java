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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import feign.Request;
import feign.Response;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FineractErrorDecoderTest {

    private FineractErrorDecoder decoder;
    private Request request;

    @BeforeEach
    void setUp() {
        decoder = new FineractErrorDecoder();
        request = Request.create(Request.HttpMethod.GET, "/api/test", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
    }

    @Test
    void testDecodeValidFineractError() {
        String jsonError = "{\"developerMessage\":\"Developer error message\",\"userMessage\":\"User error message\"}";
        Response response = createResponse(400, jsonError);

        Exception exception = decoder.decode("Test#method", response);

        assertInstanceOf(FeignException.class, exception);
        FeignException feignException = (FeignException) exception;
        assertEquals(400, feignException.status());
        assertEquals("User error message", feignException.getUserMessage());
        assertEquals("Developer error message", feignException.getDeveloperMessage());
    }

    @Test
    void testDecodeInvalidJson() {
        String invalidJson = "This is not valid JSON";
        Response response = createResponse(400, invalidJson);

        Exception exception = decoder.decode("Test#method", response);

        assertNotNull(exception);
    }

    @Test
    void testDecodeNullBody() {
        Response response = createResponse(404, null);

        Exception exception = decoder.decode("Test#method", response);

        assertNotNull(exception);
    }

    @Test
    void testDecode400Error() {
        String jsonError = "{\"developerMessage\":\"Bad request details\",\"userMessage\":\"Invalid input\"}";
        Response response = createResponse(400, jsonError);

        Exception exception = decoder.decode("Test#method", response);

        assertInstanceOf(FeignException.class, exception);
        FeignException feignException = (FeignException) exception;
        assertEquals(400, feignException.status());
    }

    @Test
    void testDecode404Error() {
        String jsonError = "{\"developerMessage\":\"Resource not found details\",\"userMessage\":\"Not found\"}";
        Response response = createResponse(404, jsonError);

        Exception exception = decoder.decode("Test#method", response);

        assertInstanceOf(FeignException.class, exception);
        FeignException feignException = (FeignException) exception;
        assertEquals(404, feignException.status());
    }

    @Test
    void testDecode500Error() {
        String jsonError = "{\"developerMessage\":\"Internal server error details\",\"userMessage\":\"Server error\"}";
        Response response = createResponse(500, jsonError);

        Exception exception = decoder.decode("Test#method", response);

        assertInstanceOf(FeignException.class, exception);
        FeignException feignException = (FeignException) exception;
        assertEquals(500, feignException.status());
    }

    @Test
    void testExtractDeveloperMessage() {
        String jsonError = "{\"developerMessage\":\"Technical details here\"}";
        Response response = createResponse(400, jsonError);

        Exception exception = decoder.decode("Test#method", response);

        assertInstanceOf(FeignException.class, exception);
        FeignException feignException = (FeignException) exception;
        assertEquals("Technical details here", feignException.getDeveloperMessage());
    }

    @Test
    void testExtractUserMessage() {
        String jsonError = "{\"userMessage\":\"User-friendly message\"}";
        Response response = createResponse(400, jsonError);

        Exception exception = decoder.decode("Test#method", response);

        assertInstanceOf(FeignException.class, exception);
        FeignException feignException = (FeignException) exception;
        assertEquals("User-friendly message", feignException.getUserMessage());
    }

    private Response createResponse(int status, String body) {
        Map<String, Collection<String>> headers = new HashMap<>();

        Response.Body responseBody = null;
        if (body != null) {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            responseBody = new Response.Body() {

                @Override
                public Integer length() {
                    return bodyBytes.length;
                }

                @Override
                public boolean isRepeatable() {
                    return true;
                }

                @Override
                public ByteArrayInputStream asInputStream() {
                    return new ByteArrayInputStream(bodyBytes);
                }

                @Override
                public java.io.Reader asReader() {
                    return new java.io.InputStreamReader(asInputStream(), StandardCharsets.UTF_8);
                }

                @Override
                public java.io.Reader asReader(java.nio.charset.Charset charset) {
                    return new java.io.InputStreamReader(asInputStream(), charset);
                }

                @Override
                public void close() {}
            };
        }

        return Response.builder().status(status).reason("Test").request(request).headers(headers).body(responseBody).build();
    }
}
