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
package org.apache.fineract.infrastructure.core.exception;

import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;

/**
 * Exception thrown when problem with an API request to the platform.
 */
public class PlatformApiDataValidationException extends AbstractPlatformException {

    private final List<ApiParameterError> errors;

    /**
     * Constructor. Consider simply using {@link DataValidatorBuilder#throwValidationErrors()} directly.
     *
     * @param errors
     *            list of {@link ApiParameterError} to throw
     */
    public PlatformApiDataValidationException(List<ApiParameterError> errors) {
        super("validation.msg.validation.errors.exist", "Validation errors exist.");
        this.errors = errors;
    }

    public PlatformApiDataValidationException(final List<ApiParameterError> errors, Throwable cause) {
        super("validation.msg.validation.errors.exist", "Validation errors exist.", cause);
        this.errors = errors;
    }

    public PlatformApiDataValidationException(String globalisationMessageCode, String defaultUserMessage, List<ApiParameterError> errors) {
        super(globalisationMessageCode, defaultUserMessage);
        this.errors = errors;
    }

    public PlatformApiDataValidationException(String globalisationMessageCode, String defaultUserMessage, List<ApiParameterError> errors,
            Throwable cause) {
        super(globalisationMessageCode, defaultUserMessage, cause);
        this.errors = errors;
    }

    public PlatformApiDataValidationException(String messageCode, String userMessage, String parameterName, Throwable cause,
            final Object... userMessageArgs) {
        this("validation.msg.validation.errors.exist", "Validation errors exist.",
                List.of(ApiParameterError.parameterError(messageCode, userMessage, parameterName, userMessageArgs)), cause);
    }

    public PlatformApiDataValidationException(String messageCode, String userMessage, String parameterName,
            final Object... userMessageArgs) {
        this("validation.msg.validation.errors.exist", "Validation errors exist.",
                List.of(ApiParameterError.parameterError(messageCode, userMessage, parameterName, userMessageArgs)), null);
    }

    public List<ApiParameterError> getErrors() {
        return this.errors;
    }

    @Override
    public String toString() {
        return "PlatformApiDataValidationException{" + "errors=" + errors + '}';
    }
}
