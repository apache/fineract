package org.mifosplatform.infrastructure.core.service;

public class PlatformEmailSendException extends RuntimeException {

    public PlatformEmailSendException(final Throwable e) {
        super(e);
    }
}
