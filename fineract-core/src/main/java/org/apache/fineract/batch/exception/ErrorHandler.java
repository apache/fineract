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
package org.apache.fineract.batch.exception;

import com.google.gson.Gson;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.exception.PlatformInternalServerException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformApiDataValidationExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformDataIntegrityExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformDomainRuleExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformInternalServerExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformResourceNotFoundExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.UnsupportedParameterExceptionMapper;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.http.HttpStatus;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.transaction.TransactionException;

/**
 * Provides an Error Handler method that returns an object of type {@link ErrorInfo} to the CommandStrategy which raised
 * the exception. This class uses various subclasses of RuntimeException to check the kind of exception raised and
 * provide appropriate status and error codes for each one of the raised exception.
 *
 * @author Rishabh Shukla
 * @see org.apache.fineract.batch.command.CommandStrategy
 */
public final class ErrorHandler {

    private static final Gson JSON_HELPER = GoogleGsonSerializerHelper.createGsonBuilder(true).create();

    private static final Map<Class<? extends Exception>, Function<RuntimeException, ErrorInfo>> EXCEPTION_HANDLERS = Collections
            .synchronizedMap(new LinkedHashMap<>());
    private static final Map.Entry<Class<? extends Exception>, Function<RuntimeException, ErrorInfo>> DEFAULT_ERROR_HANDLER = Map.entry(
            RuntimeException.class,
            runtimeException -> new ErrorInfo(500, 9999, "{\"Exception\": %s}".formatted(runtimeException.getMessage())));

    static {
        EXCEPTION_HANDLERS.put(AbstractPlatformResourceNotFoundException.class,
                runtimeException -> handleException(runtimeException, new PlatformResourceNotFoundExceptionMapper(), 1001));
        EXCEPTION_HANDLERS.put(UnsupportedParameterException.class,
                runtimeException -> handleException(runtimeException, new UnsupportedParameterExceptionMapper(), 2001));
        EXCEPTION_HANDLERS.put(PlatformApiDataValidationException.class,
                runtimeException -> handleException(runtimeException, new PlatformApiDataValidationExceptionMapper(), 2002));
        EXCEPTION_HANDLERS.put(PlatformDataIntegrityException.class,
                runtimeException -> handleException(runtimeException, new PlatformDataIntegrityExceptionMapper(), 3001));
        EXCEPTION_HANDLERS.put(AbstractPlatformDomainRuleException.class,
                runtimeException -> handleException(runtimeException, new PlatformDomainRuleExceptionMapper(), 9999));
        EXCEPTION_HANDLERS.put(TransactionException.class, runtimeException -> new ErrorInfo(HttpStatus.SC_INTERNAL_SERVER_ERROR, 4001,
                "{\"Exception\": %s}".formatted(runtimeException.getMessage())));
        EXCEPTION_HANDLERS.put(PlatformInternalServerException.class,
                runtimeException -> handleException(runtimeException, new PlatformInternalServerExceptionMapper(), 5001));
        EXCEPTION_HANDLERS.put(NonTransientDataAccessException.class, runtimeException -> new ErrorInfo(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                4002, "{\"Exception\": %s}".formatted(runtimeException.getMessage())));
        EXCEPTION_HANDLERS.put(DEFAULT_ERROR_HANDLER.getKey(), DEFAULT_ERROR_HANDLER.getValue());
    }

    private ErrorHandler() {}

    public static void registerNewErrorHandler(final Class<? extends RuntimeException> exceptionClass, final ExceptionMapper mapper,
            int errorCode) {
        LinkedHashMap<Class<? extends Exception>, Function<RuntimeException, ErrorInfo>> newHandlers = new LinkedHashMap<>();
        newHandlers.put(exceptionClass, runtimeException -> handleException(runtimeException, mapper, errorCode));
        EXCEPTION_HANDLERS.forEach(newHandlers::putIfAbsent);
        EXCEPTION_HANDLERS.clear();
        newHandlers.forEach(EXCEPTION_HANDLERS::putIfAbsent);
    }

    public static void registerNewErrorHandler(final Class<? extends RuntimeException> exceptionClass,
            Function<RuntimeException, ErrorInfo> function) {
        LinkedHashMap<Class<? extends Exception>, Function<RuntimeException, ErrorInfo>> newHandlers = new LinkedHashMap<>();
        newHandlers.put(exceptionClass, function);
        EXCEPTION_HANDLERS.forEach(newHandlers::putIfAbsent);
        EXCEPTION_HANDLERS.clear();
        newHandlers.forEach(EXCEPTION_HANDLERS::putIfAbsent);
    }

    private static ErrorInfo handleException(final RuntimeException exception, final ExceptionMapper mapper, final int errorCode) {
        final Response response = mapper.toResponse(exception);
        final String errorBody = JSON_HELPER.toJson(response.getEntity());
        return new ErrorInfo(response.getStatus(), errorCode, errorBody);
    }

    /**
     * Returns an object of ErrorInfo type containing the information regarding the raised error.
     *
     * @param exception
     * @return ErrorInfo
     */
    public static ErrorInfo handler(final RuntimeException exception) {
        Map.Entry<Class<? extends Exception>, Function<RuntimeException, ErrorInfo>> errorhandler = EXCEPTION_HANDLERS.entrySet().stream()
                .filter(e -> e.getKey().isAssignableFrom(exception.getClass())).findFirst().orElse(DEFAULT_ERROR_HANDLER);
        return errorhandler.getValue().apply(exception);
    }
}
