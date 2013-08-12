package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class GroupNotActiveException extends AbstractPlatformDomainRuleException {

    public GroupNotActiveException(final Long groupId) {
        super("error.msg.group.not.active.exception", "The Group with id `" + groupId + "` is not active", groupId);
    }

}
