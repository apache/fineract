package org.mifosng.platform.infrastructure;

public class PlatformEmailSendException extends RuntimeException {

    public PlatformEmailSendException(final Throwable e) {
        super(e);
    }
}
