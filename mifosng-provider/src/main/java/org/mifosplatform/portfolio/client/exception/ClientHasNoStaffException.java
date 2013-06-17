package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class ClientHasNoStaffException extends AbstractPlatformResourceNotFoundException {

    public ClientHasNoStaffException(final Long clientId) {
        super("error.msg.client.has.no.staff", "Client with identifier " + clientId + " does not have staff", clientId);
    }

}
