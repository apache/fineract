package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ClientNotInGroupException extends AbstractPlatformDomainRuleException {

    public ClientNotInGroupException(final Long clientId, final Long groupId) {
        super("error.msg.group.client.not.in.group", "Client with identifier " + clientId + " is not in Group with identifier " + groupId,
                clientId, groupId);
    }

}
