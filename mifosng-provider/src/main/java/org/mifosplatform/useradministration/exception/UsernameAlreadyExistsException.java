package org.mifosplatform.useradministration.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(final Throwable e) {
        super(e);
    }
}