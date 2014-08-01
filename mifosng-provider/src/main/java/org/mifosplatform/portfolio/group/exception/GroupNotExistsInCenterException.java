package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class GroupNotExistsInCenterException extends AbstractPlatformDomainRuleException {

    public GroupNotExistsInCenterException(final Long groupId, final Long centerId) {
        super("error.msg.group.not.in.center", "Group with identifier " + groupId + " is not exists in Center with identifier " + centerId,
                groupId, centerId);
    }

}