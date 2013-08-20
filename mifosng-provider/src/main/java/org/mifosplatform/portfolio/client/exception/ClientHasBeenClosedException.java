package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ClientHasBeenClosedException extends AbstractPlatformDomainRuleException {

    public ClientHasBeenClosedException(final Long clientId) {
        super("error.msg.client.closed", "Client with identifier " + clientId + " has already been closed");
    }

}
