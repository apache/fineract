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
package org.apache.fineract.commands.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdempotencyKeyResolver {

    /**
     * Longest idempotency key a client may send. The column is wider, so that a key of this length still fits after a
     * recoverable failure appends its {@code _RE<n>} suffix.
     */
    public static final int MAX_CLIENT_KEY_LENGTH = 50;
    public static final String KEY_TOO_LONG_ERROR_CODE = "error.msg.idempotency.key.too.long";
    public static final String KEY_RESERVED_SUFFIX_ERROR_CODE = "error.msg.idempotency.key.reserved.suffix";
    private static final String IDEMPOTENCY_KEY_PARAMETER = "idempotencyKey";
    private static final Pattern RESERVED_SUFFIX = Pattern
            .compile(Pattern.quote(CommandSourceService.RECOVERABLE_ERROR_KEY_SUFFIX) + "\\d+$");

    private final FineractRequestContextHolder fineractRequestContextHolder;

    private final IdempotencyKeyGenerator idempotencyKeyGenerator;

    public String resolve(CommandWrapper wrapper) {
        Optional<String> clientKey = Optional.ofNullable(wrapper.getIdempotencyKey()).or(this::getAttribute);
        clientKey.ifPresent(IdempotencyKeyResolver::validateClientKey);
        return clientKey.orElseGet(idempotencyKeyGenerator::create);
    }

    private Optional<String> getAttribute() {
        return Optional.ofNullable(fineractRequestContextHolder.getAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_ATTRIBUTE))
                .map(String::valueOf);

    }

    private static void validateClientKey(String key) {
        if (key.length() > MAX_CLIENT_KEY_LENGTH) {
            throw validationError(KEY_TOO_LONG_ERROR_CODE,
                    "The idempotency key must be at most " + MAX_CLIENT_KEY_LENGTH + " characters long.", MAX_CLIENT_KEY_LENGTH);
        }
        if (RESERVED_SUFFIX.matcher(key).find()) {
            throw validationError(KEY_RESERVED_SUFFIX_ERROR_CODE, "The idempotency key must not end with "
                    + CommandSourceService.RECOVERABLE_ERROR_KEY_SUFFIX + " followed by digits; that suffix is reserved.");
        }
    }

    private static PlatformApiDataValidationException validationError(String code, String message, Object... args) {
        return new PlatformApiDataValidationException(
                List.of(ApiParameterError.parameterError(code, message, IDEMPOTENCY_KEY_PARAMETER, args)));
    }
}
