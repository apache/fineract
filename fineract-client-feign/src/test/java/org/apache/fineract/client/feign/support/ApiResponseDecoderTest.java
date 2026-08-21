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
package org.apache.fineract.client.feign.support;

import static org.assertj.core.api.Assertions.assertThat;

import feign.Request;
import feign.Response;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.apache.fineract.client.feign.Jackson3Decoder;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.models.ApiResponse;
import org.junit.jupiter.api.Test;

class ApiResponseDecoderTest {

    private final ApiResponseDecoder decoder = new ApiResponseDecoder(new Jackson3Decoder(ObjectMapperFactory.getShared()));

    @Test
    void decodeStringReturnsRawResponseBody() throws Exception {
        final String body = "[{\"test_nr\":42}]";

        final Object decoded = decoder.decode(response(body), String.class);

        assertThat(decoded).isEqualTo(body);
    }

    @Test
    void decodeStringReturnsDecodedJsonStringLiteral() throws Exception {
        final Object decoded = decoder.decode(response("\"OK\""), String.class);

        assertThat(decoded).isEqualTo("OK");
    }

    @Test
    void decodeApiResponseStringReturnsRawResponseBody() throws Exception {
        final String body = "[{\"test_nr\":42}]";

        @SuppressWarnings("unchecked")
        final ApiResponse<String> decoded = (ApiResponse<String>) decoder.decode(response(body), apiResponseStringType());

        assertThat(decoded.getStatusCode()).isEqualTo(200);
        assertThat(decoded.getData()).isEqualTo(body);
    }

    @Test
    void decodeApiResponseStringReturnsDecodedJsonStringLiteral() throws Exception {
        @SuppressWarnings("unchecked")
        final ApiResponse<String> decoded = (ApiResponse<String>) decoder.decode(response("\"OK\""), apiResponseStringType());

        assertThat(decoded.getStatusCode()).isEqualTo(200);
        assertThat(decoded.getData()).isEqualTo("OK");
    }

    private Response response(final String body) {
        final Request request = Request.create(Request.HttpMethod.GET, "/api/test", Collections.emptyMap(), null, StandardCharsets.UTF_8,
                null);
        return Response.builder().status(200).reason("OK").request(request).body(body, StandardCharsets.UTF_8).build();
    }

    private Type apiResponseStringType() {
        return new ParameterizedType() {

            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { String.class };
            }

            @Override
            public Type getRawType() {
                return ApiResponse.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}
