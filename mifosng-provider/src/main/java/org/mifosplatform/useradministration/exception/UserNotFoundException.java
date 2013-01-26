package org.mifosplatform.useradministration.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when user resources are not found.
 */
public class UserNotFoundException extends AbstractPlatformResourceNotFoundException {

    public UserNotFoundException(Long id) {
        super("error.msg.user.id.invalid", "User with identifier " + id + " does not exist", id);
    }
}