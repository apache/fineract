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
package org.apache.fineract.portfolio.group.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.springframework.stereotype.Component;

/**
 * Reads the typed request of a group or center {@code ?command=} action from the raw request body. Those endpoints take
 * the body as a {@code String} because one method serves several commands with different request types.
 */
@Component
@RequiredArgsConstructor
class GroupingTypesRequestParser {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    <T> T parseAndValidate(final String apiRequestBodyAsJson, final Class<T> type) {
        final T request = parse(apiRequestBodyAsJson, type);
        validate(request);
        return request;
    }

    /** Parses without validating, for a request that still needs values from the path or query before validation. */
    <T> T parse(final String apiRequestBodyAsJson, final Class<T> type) {
        if (StringUtils.isBlank(apiRequestBodyAsJson)) {
            throw new InvalidJsonException();
        }
        try {
            return objectMapper.readValue(apiRequestBodyAsJson, type);
        } catch (final JsonProcessingException e) {
            throw new InvalidJsonException(e);
        }
    }

    <T> void validate(final T request) {
        final Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
