package org.mifosplatform.infrastructure.user.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(final Throwable e) {
        super(e);
    }
}