package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class GroupRoleNotFoundException extends AbstractPlatformResourceNotFoundException {

    public GroupRoleNotFoundException(final Long id) {
        super("error.msg.group.role.id.invalid", "Group Role with identifier " + id + " does not exist", id);
    }
}
