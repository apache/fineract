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
package org.apache.fineract.infrastructure.core.api.mvc.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.mvc.ProfileMvc;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformApiDataValidationExceptionMapper;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

@ProfileMvc
@Component
@RequiredArgsConstructor
public final class ValidationErrorConverter {

    private static final Map<Class<? extends Annotation>, String> annotationCodes = Map.of(NotBlank.class, "cannot.be.blank", MaxSize.class,
            "exceeds.max.length");

    private final PlatformApiDataValidationExceptionMapper validationExceptionMapper;

    public Response getResponse(final List<FieldError> fieldErrors) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        for (final FieldError fieldError : fieldErrors) {
            dataValidationErrors.add(generateParameterError(fieldError));
        }
        return validationExceptionMapper.toResponse(new PlatformApiDataValidationException(dataValidationErrors));
    }

    private ApiParameterError generateParameterError(FieldError fieldError) {
        final ConstraintViolation<?> constraint = fieldError.unwrap(ConstraintViolation.class);

        final Class<? extends Annotation> annotationType = constraint.getConstraintDescriptor().getAnnotation().annotationType();
        final String validationErrorCode = getValidationErrorCode(constraint.getRootBeanClass(), fieldError.getField(), annotationType);
        final String realParameterName = fieldError.getField();
        final String defaultEnglishMessage = "The parameter `%s` %s.".formatted(realParameterName, fieldError.getDefaultMessage());
        final Object[] arguments = getArguments(fieldError, constraint, annotationType);

        return ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, realParameterName, arguments);
    }

    private String getValidationErrorCode(Class<?> requestClass, String fieldName, Class<? extends Annotation> annotation) {
        final String resourceName = requestClass.getAnnotation(ResourceName.class).value();
        return "validation.msg.%s.%s.%s".formatted(resourceName, fieldName, annotationCodes.get(annotation));
    }

    private Object[] getArguments(FieldError fieldError, ConstraintViolation<?> constraint, Class<? extends Annotation> annotationType) {
        final Map<String, Object> attributes = constraint.getConstraintDescriptor().getAttributes();
        final ArrayList<Object> arguments = new ArrayList<>();
        arguments.add(fieldError.getRejectedValue());
        if (MaxSize.class == annotationType) {
            arguments.add(attributes.get("max"));
        }
        return arguments.toArray();
    }
}
