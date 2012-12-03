package org.mifosplatform.useradministration.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when permission is attempted to be set as maker-checker enabled.
 */
public class PermissionCantBeMakerCheckerableException extends AbstractPlatformResourceNotFoundException {

    public PermissionCantBeMakerCheckerableException(final String code) {
        super("error.msg.permission.code.not.makercheckerable", "Permission with Code " + code + " can't be maker-checkerable", code);
    }
}