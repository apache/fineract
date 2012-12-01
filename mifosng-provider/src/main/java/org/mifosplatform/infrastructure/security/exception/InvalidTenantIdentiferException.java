package org.mifosplatform.infrastructure.security.exception;

/**
 * {@link RuntimeException} thrown when an invalid tenant identifier is used in
 * request to platform.
 * 
 * @see CustomRequestHeaderAuthenticationFilter
 */
public class InvalidTenantIdentiferException extends RuntimeException {

    public InvalidTenantIdentiferException(final String message) {
        super(message);
    }
}