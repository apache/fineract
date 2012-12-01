package org.mifosplatform.infrastructure.core.exception;

import java.util.List;

/**
 * A {@link RuntimeException} that is thrown in the case where invalid
 * parameters are sent in the body of the request to the platform api.
 */
public class UnsupportedParameterException extends RuntimeException {

    private final List<String> unsupportedParameters;

    public UnsupportedParameterException(List<String> unsupportedParameters) {
        this.unsupportedParameters = unsupportedParameters;
    }

    public List<String> getUnsupportedParameters() {
        return unsupportedParameters;
    }
}