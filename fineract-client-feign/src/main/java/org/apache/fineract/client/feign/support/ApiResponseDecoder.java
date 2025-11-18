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

import feign.Response;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.apache.fineract.client.models.ApiResponse;

/**
 * Custom Feign decoder that handles ApiResponse&lt;T&gt; return types for *WithHttpInfo methods.
 *
 * When a Feign client method returns ApiResponse&lt;T&gt;, the standard JacksonDecoder cannot handle it because:
 * <ul>
 * <li>The server returns just T (the body), not ApiResponse&lt;T&gt;</li>
 * <li>ApiResponse wraps the body with HTTP status code and headers</li>
 * </ul>
 *
 * This decoder:
 * <ol>
 * <li>Detects if the return type is ApiResponse&lt;T&gt;</li>
 * <li>Extracts the inner type T and delegates body decoding to the underlying decoder</li>
 * <li>Wraps the decoded body with status code and headers into ApiResponse&lt;T&gt;</li>
 * </ol>
 */
public final class ApiResponseDecoder implements Decoder {

    private final Decoder delegate;

    public ApiResponseDecoder(Decoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (isApiResponseType(type)) {
            Type innerType = getApiResponseInnerType(type);
            Object body = delegate.decode(response, innerType);
            return new ApiResponse<>(response.status(), response.headers(), body);
        }
        return delegate.decode(response, type);
    }

    private boolean isApiResponseType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Type rawType = paramType.getRawType();
            return rawType == ApiResponse.class;
        }
        return type == ApiResponse.class;
    }

    private Type getApiResponseInnerType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                return typeArgs[0];
            }
        }
        return Object.class;
    }
}
