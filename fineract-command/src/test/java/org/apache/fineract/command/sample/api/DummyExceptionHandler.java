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
package org.apache.fineract.command.sample.api;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.sample.data.DummyError;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class DummyExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(Throwable ex) {
        var problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
        problemDetails.setType(URI.create("https://fineract.apache.org/validation"));
        problemDetails.setTitle(messageSource.getMessage("org.apache.fineract.common.validation.error", null, Locale.US)); // TODO:
                                                                                                                           // check
                                                                                                                           // this,
                                                                                                                           // get
                                                                                                                           // locale

        List<DummyError> errors = new ArrayList<>();

        if (ex instanceof ConstraintViolationException cve) {
            for (ConstraintViolation<?> violation : cve.getConstraintViolations()) {
                errors.add(new DummyError(violation.getPropertyPath().toString(), violation.getMessage(), violation.getMessageTemplate()));
            }
        } else if (ex instanceof WebExchangeBindException webe) {
            for (FieldError fieldError : webe.getFieldErrors()) {
                errors.add(new DummyError(fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getCode()));
            }
        } else if (ex instanceof MethodArgumentNotValidException manve) {
            for (FieldError fieldError : manve.getBindingResult().getFieldErrors()) {
                errors.add(new DummyError(fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getCode()));
            }
        }

        if (!errors.isEmpty()) {
            problemDetails.setProperty("errors", errors);
        }

        return ResponseEntity.badRequest().contentType(APPLICATION_PROBLEM_JSON).body(problemDetails);
    }
}
