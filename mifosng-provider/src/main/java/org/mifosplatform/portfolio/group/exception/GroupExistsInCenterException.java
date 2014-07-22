package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class GroupExistsInCenterException extends AbstractPlatformDomainRuleException {

    public GroupExistsInCenterException(final Long centerId, final Long groupId) {
        super("error.msg.group.is.already.member.of.center", "Group with identifier " + groupId
                + " is already exists in Center with identifier " + centerId, groupId, centerId);
    }

}