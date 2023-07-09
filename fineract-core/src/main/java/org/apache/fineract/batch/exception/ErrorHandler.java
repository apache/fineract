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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.apache.fineract.infrastructure.core.exceptionmapper.DefaultExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.FineractExceptionMapper;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

    private <T extends RuntimeException> ExceptionMapper<T> findMostSpecificExceptionHandler(T exception) {
        Class<?> clazz = exception.getClass();
        do {
            Set<String> exceptionMappers = createSet(ctx.getBeanNamesForType(forClassWithGenerics(ExceptionMapper.class, clazz)));
            Set<String> fineractErrorMappers = createSet(ctx.getBeanNamesForType(FineractExceptionMapper.class));
            SetUtils.SetView<String> intersection = SetUtils.intersection(exceptionMappers, fineractErrorMappers);
            if (intersection.size() > 0) {
                // noinspection unchecked
                return (ExceptionMapper<T>) ctx.getBean(intersection.iterator().next());
            }
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(Exception.class));
        // noinspection unchecked
        return (ExceptionMapper<T>) defaultExceptionMapper;
    }

    private <T> Set<T> createSet(T[] array) {
        if (array == null) {
            return Set.of();
        } else {
            return Set.of(array);
        }
    }

    /**
     * Returns an object of ErrorInfo type containing the information regarding the raised error.
     *
     * @param exception
     * @return ErrorInfo
     */
    public ErrorInfo handle(final RuntimeException exception) {
        ExceptionMapper<RuntimeException> exceptionMapper = findMostSpecificExceptionHandler(exception);
        Response response = exceptionMapper.toResponse(exception);
        return new ErrorInfo(response.getStatus(), ((FineractExceptionMapper) exceptionMapper).errorCode(),
                JSON_HELPER.toJson(response.getEntity()));
    }
}
