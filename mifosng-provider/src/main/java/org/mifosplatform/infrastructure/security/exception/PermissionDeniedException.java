package org.mifosplatform.infrastructure.security.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformAccessDeniedException;

public class PermissionDeniedException extends AbstractPlatformAccessDeniedException {

    public PermissionDeniedException(final String code, final String defaultMessage, final Long userId) {
        super(code, defaultMessage, userId);
    }

}
