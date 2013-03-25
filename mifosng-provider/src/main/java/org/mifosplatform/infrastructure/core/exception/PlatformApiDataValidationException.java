/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exception;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;

/**
 * Exception thrown when problem with an API request to the platform.
 */
public class PlatformApiDataValidationException extends RuntimeException {

    private final String globalisationMessageCode;
    private final String defaultUserMessage;
    private final List<ApiParameterError> errors;

    public PlatformApiDataValidationException(final List<ApiParameterError> errors) {
        this.globalisationMessageCode = "validation.msg.validation.errors.exist";
        this.defaultUserMessage = "Validation errors exist.";
        this.errors = errors;
    }

    public PlatformApiDataValidationException(final String globalisationMessageCode, final String defaultUserMessage,
            final List<ApiParameterError> errors) {
        this.globalisationMessageCode = globalisationMessageCode;
        this.defaultUserMessage = defaultUserMessage;
        this.errors = errors;
    }

    public String getGlobalisationMessageCode() {
        return globalisationMessageCode;
    }

    public String getDefaultUserMessage() {
        return defaultUserMessage;
    }

    public List<ApiParameterError> getErrors() {
        return errors;
    }
}