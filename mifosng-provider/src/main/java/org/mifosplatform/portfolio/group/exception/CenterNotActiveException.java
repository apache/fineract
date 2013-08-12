package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class CenterNotActiveException extends AbstractPlatformDomainRuleException {

    public CenterNotActiveException(final Long centerId) {
        super("error.msg.center.not.active.exception", "The Center with id `" + centerId + "` is not active", centerId);
    }

}
