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
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
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
import org.apache.fineract.infrastructure.jobs.exception.LoanIdsHardLockedException;
import org.apache.fineract.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.http.HttpStatus;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.transaction.TransactionException;

/**
 * Provides an Error Handler method that returns an object of type {@link ErrorInfo} to the CommandStrategy which raised
 * the exception. This class uses various subclasses of RuntimeException to check the kind of exception raised and
 * provide appropriate status and error codes for each one of the raised exception.
 *
 * @author Rishabh Shukla
 *
 * @see org.apache.fineract.batch.command.CommandStrategy
 * @see org.apache.fineract.batch.command.internal.CreateClientCommandStrategy
 */
public class ErrorHandler extends RuntimeException {

    private static Gson jsonHelper = GoogleGsonSerializerHelper.createGsonBuilder(true).create();

    /**
     * Sole Constructor
     */
    ErrorHandler() {

    }

    private static <E extends Exception, M extends ExceptionMapper<E>> ErrorInfo handleException(final E exception, final M mapper,
            final int errorCode) {
        final Response response = mapper.toResponse(exception);
        final String errorBody = jsonHelper.toJson(response.getEntity());
        return new ErrorInfo(response.getStatus(), errorCode, errorBody);
    }

    /**
     * Returns an object of ErrorInfo type containing the information regarding the raised error.
     *
     * @param exception
     * @return ErrorInfo
     */
    public static ErrorInfo handler(final RuntimeException exception) {
        if (exception instanceof AbstractPlatformResourceNotFoundException e) {
            return handleException(e, new PlatformResourceNotFoundExceptionMapper(), 1001);
        }
        if (exception instanceof UnsupportedParameterException e) {
            return handleException(e, new UnsupportedParameterExceptionMapper(), 2001);
        }
        if (exception instanceof PlatformApiDataValidationException e) {
            return handleException(e, new PlatformApiDataValidationExceptionMapper(), 2002);
        }
        if (exception instanceof PlatformDataIntegrityException e) {
            return handleException(e, new PlatformDataIntegrityExceptionMapper(), 3001);
        }
        if (exception instanceof LinkedAccountRequiredException e) {
            return handleException(e, new PlatformDomainRuleExceptionMapper(), 3002);
        }
        if (exception instanceof MultiDisbursementDataRequiredException e) {
            return handleException(e, new PlatformDomainRuleExceptionMapper(), 3003);
        }
        if (exception instanceof AbstractPlatformDomainRuleException e) {
            return handleException(e, new PlatformDomainRuleExceptionMapper(), 9999);
        }
        if (exception instanceof TransactionException) {
            return new ErrorInfo(HttpStatus.SC_BAD_REQUEST, 4001, "{\"Exception\": %s}".formatted(exception.getMessage()));
        }
        if (exception instanceof PlatformInternalServerException e) {
            return handleException(e, new PlatformInternalServerExceptionMapper(), 5001);
        }
        if (exception instanceof NonTransientDataAccessException) {
            return new ErrorInfo(HttpStatus.SC_BAD_REQUEST, 4002, "{\"Exception\": %s}".formatted(exception.getMessage()));
        }
        if (exception instanceof LoanIdsHardLockedException e) {
            String message = ApiGlobalErrorResponse.loanIsLocked(e.getLoanIdFromRequest()).toJson();
            return new ErrorInfo(HttpStatus.SC_CONFLICT, 4090, message);
        }

        return new ErrorInfo(500, 9999, "{\"Exception\": %s}".formatted(exception.getMessage()));
    }
}
