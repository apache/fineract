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

import static org.springframework.core.ResolvableType.forClassWithGenerics;

import com.google.gson.Gson;
import jakarta.persistence.PersistenceException;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.text.ParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.naming.AuthenticationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.commons.collections4.SetUtils;
import org.apache.fineract.batch.domain.Header;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.exceptionmapper.DefaultExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.FineractExceptionMapper;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.stereotype.Component;

/**
 * Provides an Error Handler method that returns an object of type {@link ErrorInfo} to the CommandStrategy which raised
 * the exception. This class uses various subclasses of RuntimeException to check the kind of exception raised and
 * provide appropriate status and error codes for each one of the raised exception.
 *
 * @author Rishabh Shukla
 * @see org.apache.fineract.batch.command.CommandStrategy
 */
@Component
@Slf4j
@AllArgsConstructor
public final class ErrorHandler {

    @Autowired
    private final ApplicationContext ctx;

    @Autowired
    private final DefaultExceptionMapper defaultExceptionMapper;

    private static final Gson JSON_HELPER = GoogleGsonSerializerHelper.createGsonBuilder(true).create();

    @NotNull
    public <T extends RuntimeException> ExceptionMapper<T> findMostSpecificExceptionHandler(T exception) {
        Class<?> clazz = exception.getClass();
        do {
            Set<String> exceptionMappers = createSet(ctx.getBeanNamesForType(forClassWithGenerics(ExceptionMapper.class, clazz)));
            Set<String> fineractErrorMappers = createSet(ctx.getBeanNamesForType(FineractExceptionMapper.class));
            SetUtils.SetView<String> intersection = SetUtils.intersection(exceptionMappers, fineractErrorMappers);
            if (!intersection.isEmpty()) {
                // noinspection unchecked
                return (ExceptionMapper<T>) ctx.getBean(intersection.iterator().next());
            }
            if (!exceptionMappers.isEmpty()) {
                // noinspection unchecked
                return (ExceptionMapper<T>) ctx.getBean(exceptionMappers.iterator().next());
            }
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(Exception.class));
        // noinspection unchecked
        return (ExceptionMapper<T>) defaultExceptionMapper;
    }

    /**
     * Returns an object of ErrorInfo type containing the information regarding the raised error.
     *
     * @param exception
     * @return ErrorInfo
     */
    public ErrorInfo handle(@NotNull RuntimeException exception) {
        ExceptionMapper<RuntimeException> exceptionMapper = findMostSpecificExceptionHandler(exception);
        Response response = exceptionMapper.toResponse(exception);
        MultivaluedMap<String, Object> headers = response.getHeaders();
        Set<Header> batchHeaders = headers == null ? null
                : headers.keySet().stream().map(e -> new Header(e, response.getHeaderString(e))).collect(Collectors.toSet());
        Integer errorCode = exceptionMapper instanceof FineractExceptionMapper ? ((FineractExceptionMapper) exceptionMapper).errorCode()
                : null;
        Object msg = response.getEntity();
        return new ErrorInfo(response.getStatus(), errorCode, msg instanceof String ? (String) msg : JSON_HELPER.toJson(msg), batchHeaders);
    }

    public RuntimeException getMappable(@NotNull Throwable thr) {
        return getMappable(thr, null, null, null);
    }

    public RuntimeException getMappable(@NotNull Throwable t, String msgCode, String defaultMsg, String param,
            final Object... defaultMsgArgs) {
        String msg = defaultMsg == null ? t.getMessage() : defaultMsg;
        param = param == null ? "unknown" : param;
        if (t instanceof NestedRuntimeException nre) {
            Throwable cause = nre.getMostSpecificCause();
            msg = cause.getMessage();
            if (nre instanceof NonTransientDataAccessException) {
                msgCode = msgCode == null ? "error.msg." + param + ".data.integrity.issue" : msgCode;
                return new PlatformDataIntegrityException(msgCode, msg, param, defaultMsgArgs);
            }
        }
        if (t instanceof ValidationException) {
            msgCode = msgCode == null ? "error.msg.validation." + param + ".error" : msgCode;
            return new PlatformApiDataValidationException(List.of(ApiParameterError.parameterError(msgCode, msg, param, defaultMsgArgs)));
        }
        if (t instanceof PersistenceException) {
            msgCode = msgCode == null ? "error.msg.persistence." + param + ".error" : msgCode;
            return new PlatformDataIntegrityException(msgCode, msg, param, defaultMsgArgs);
        }
        if (t instanceof AuthenticationException) {
            msgCode = msgCode == null ? "error.msg.authentication." + param + ".error" : msgCode;
            return new PlatformDataIntegrityException(msgCode, msg, param, defaultMsgArgs);
        }
        if (t instanceof RuntimeException re) {
            return re;
        }
        if (t instanceof ParseException) {
            msgCode = msgCode == null ? "error.msg.parse." + param + ".error" : msgCode;
            return new PlatformDataIntegrityException(msgCode, msg, param, defaultMsgArgs);
        }
        return new RuntimeException(msg, t);
    }

    private <T> Set<T> createSet(T[] array) {
        if (array == null) {
            return Set.of();
        } else {
            return Set.of(array);
        }
    }
}
