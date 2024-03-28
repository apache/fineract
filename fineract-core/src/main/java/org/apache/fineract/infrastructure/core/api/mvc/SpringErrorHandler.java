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

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.mvc.validation.ValidationErrorConverter;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.exceptionmapper.UnsupportedParameterExceptionMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ProfileMvc
@RestControllerAdvice
@RequiredArgsConstructor
public class SpringErrorHandler {

    private final ErrorHandler errorHandler;
    private final UnsupportedParameterExceptionMapper unsupportedParameterExceptionMapper;
    private final ValidationErrorConverter validationErrorConverter;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> toResponse(final RuntimeException exception) {
        final Response response = getResponse(exception);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(response.getStatus()).body(response.getEntity());
    }

    @ExceptionHandler(BindException.class)
    public Object handleValidationException(final BindException exception) {
        final Response response = validationErrorConverter.getResponse(exception.getBindingResult().getFieldErrors());
        return ResponseEntity.status(response.getStatus()).body(response.getEntity());
    }

    private Response getResponse(RuntimeException exception) {
        if (exception.getCause() instanceof UnrecognizedPropertyException ure) {
            return unsupportedParameterExceptionMapper.toResponse(new UnsupportedParameterException(List.of(ure.getPropertyName())));
        }
        return errorHandler.findMostSpecificExceptionHandler(exception).toResponse(exception);
    }
}
