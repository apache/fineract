package org.mifosplatform.useradministration.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when role resources are not found.
 */
public class RoleNotFoundException extends AbstractPlatformResourceNotFoundException {

    public RoleNotFoundException(Long id) {
        super("error.msg.role.id.invalid", "Role with identifier " + id + " does not exist", id);
    }
}