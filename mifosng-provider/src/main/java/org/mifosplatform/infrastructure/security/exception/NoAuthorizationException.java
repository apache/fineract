package org.mifosplatform.infrastructure.security.exception;

/**
 * A {@link RuntimeException} that is thrown in the case where a user does not
 * have sufficient authorization to execute operation on platform.
 */
public class NoAuthorizationException extends RuntimeException {

    public NoAuthorizationException(final String message) {
        super(message);
    }
}