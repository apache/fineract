/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.exception.PlatformInternalServerException;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.mifosplatform.infrastructure.core.exceptionmapper.PlatformApiDataValidationExceptionMapper;
import org.mifosplatform.infrastructure.core.exceptionmapper.PlatformDataIntegrityExceptionMapper;
import org.mifosplatform.infrastructure.core.exceptionmapper.PlatformDomainRuleExceptionMapper;
import org.mifosplatform.infrastructure.core.exceptionmapper.PlatformInternalServerExceptionMapper;
import org.mifosplatform.infrastructure.core.exceptionmapper.PlatformResourceNotFoundExceptionMapper;
import org.mifosplatform.infrastructure.core.exceptionmapper.UnsupportedParameterExceptionMapper;
import org.mifosplatform.portfolio.loanaccount.exception.MultiDisbursementDataRequiredException;
import org.mifosplatform.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.springframework.transaction.TransactionException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Provides an Error Handler method that returns an object of type
 * {@link ErrorInfo} to the CommandStrategy which raised the exception. This
 * class uses various subclasses of RuntimeException to check the kind of
 * exception raised and provide appropriate status and error codes for each one
 * of the raised exception.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.command.CommandStrategy
 * @see org.mifosplatform.batch.command.internal.CreateClientCommandStrategy
 */
public class ErrorHandler extends RuntimeException {

    private static Gson jsonHelper = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Sole Constructor
     */
    ErrorHandler() {
        super();
    }

    /**
     * Returns an object of ErrorInfo type containing the information regarding
     * the raised error.
     * 
     * @param exception
     * @return ErrorInfo
     */
    public static ErrorInfo handler(final RuntimeException exception) {

        if (exception instanceof AbstractPlatformResourceNotFoundException) {

            final PlatformResourceNotFoundExceptionMapper mapper = new PlatformResourceNotFoundExceptionMapper();
            final String errorBody = jsonHelper
                    .toJson(mapper.toResponse((AbstractPlatformResourceNotFoundException) exception).getEntity());

            return new ErrorInfo(404, 1001, errorBody);

        } else if (exception instanceof UnsupportedParameterException) {

            final UnsupportedParameterExceptionMapper mapper = new UnsupportedParameterExceptionMapper();
            final String errorBody = jsonHelper.toJson(mapper.toResponse((UnsupportedParameterException) exception).getEntity());

            return new ErrorInfo(400, 2001, errorBody);

        } else if (exception instanceof PlatformApiDataValidationException) {

            final PlatformApiDataValidationExceptionMapper mapper = new PlatformApiDataValidationExceptionMapper();
            final String errorBody = jsonHelper.toJson(mapper.toResponse((PlatformApiDataValidationException) exception).getEntity());

            return new ErrorInfo(400, 2002, errorBody);

        } else if (exception instanceof PlatformDataIntegrityException) {

            final PlatformDataIntegrityExceptionMapper mapper = new PlatformDataIntegrityExceptionMapper();
            final String errorBody = jsonHelper.toJson(mapper.toResponse((PlatformDataIntegrityException) exception).getEntity());

            return new ErrorInfo(403, 3001, errorBody);

        } else if (exception instanceof LinkedAccountRequiredException) {

            final PlatformDomainRuleExceptionMapper mapper = new PlatformDomainRuleExceptionMapper();
            final String errorBody = jsonHelper.toJson(mapper.toResponse((LinkedAccountRequiredException) exception).getEntity());

            return new ErrorInfo(403, 3002, errorBody);
            
        } else if (exception instanceof MultiDisbursementDataRequiredException) {

            final PlatformDomainRuleExceptionMapper mapper = new PlatformDomainRuleExceptionMapper();
            final String errorBody = jsonHelper.toJson(mapper.toResponse((MultiDisbursementDataRequiredException) exception).getEntity());

            return new ErrorInfo(403, 3003, errorBody);
            
        } else if (exception instanceof TransactionException) {
            return new ErrorInfo(400, 4001, "{\"Exception\": " + exception.getMessage()+"}");

        } else if (exception instanceof PlatformInternalServerException) {

            final PlatformInternalServerExceptionMapper mapper = new PlatformInternalServerExceptionMapper();
            final String errorBody = jsonHelper.toJson(mapper.toResponse((PlatformInternalServerException) exception).getEntity());

            return new ErrorInfo(500, 5001, errorBody);
        }

        return new ErrorInfo(500, 9999, "{\"Exception\": " + exception.toString() + "}");
    }
}
