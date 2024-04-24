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
package org.apache.fineract.infrastructure.core.api.mvc;

import com.fasterxml.jackson.annotation.JsonFilter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * This class is responsible for handling partial response. If the filter is present, take the required parameters from
 * the request and apply the filter.
 */
@ProfileMvc
@ControllerAdvice
public class JacksonPartialResponseFilter implements ResponseBodyAdvice<Object> {

    public static final String PARTIAL_RESPONSE = "partialResponse";

    @Override
    public boolean supports(@NotNull MethodParameter returnType, @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        if (Collection.class.isAssignableFrom(returnType.getParameterType())) {
            return isFilterPresentInCollection(returnType);
        } else {
            return Arrays.stream(returnType.getParameterType().getAnnotations())
                    .anyMatch(ann -> ann instanceof JsonFilter jFilter && jFilter.value().equals(PARTIAL_RESPONSE));
        }
    }

    @Override
    public Object beforeBodyWrite(final Object body, @NotNull final MethodParameter returnType,
            @NotNull final MediaType selectedContentType, @NotNull final Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NotNull final ServerHttpRequest request, @NotNull final ServerHttpResponse response) {
        if (body == null) {
            throw new IllegalArgumentException("Partial response body cannot be null");
        }
        return new JacksonPartialResponseMappingValue<>(body, getResponseParameters());
    }

    private boolean isFilterPresentInCollection(@NotNull MethodParameter returnType) {
        final boolean filterPresent;
        final Type genericParameterType = returnType.getGenericParameterType();
        final ParameterizedType parameterType = (ParameterizedType) genericParameterType;
        final Type[] actualTypeArguments = parameterType.getActualTypeArguments();
        if (actualTypeArguments.length != 0 && actualTypeArguments[0] instanceof Class<?> responseClass) {
            filterPresent = Arrays.stream(responseClass.getAnnotations())
                    .anyMatch(ann -> ann instanceof JsonFilter jFilter && jFilter.value().equals(PARTIAL_RESPONSE));
        } else {
            filterPresent = false;
        }
        return filterPresent;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getResponseParameters() {
        final Object responseParameters = Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
                .getAttribute(ApiRequestParameterHelper.RESPONSE_PARAMETERS_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);

        if (responseParameters instanceof Set<?> parametersSet) {
            return (Set<String>) parametersSet;
        } else {
            return Collections.emptySet();
        }
    }
}
