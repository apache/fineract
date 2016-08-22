package org.apache.fineract.portfolio.clientaddress.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class AddressNotFoundException extends AbstractPlatformResourceNotFoundException {

    public AddressNotFoundException(final long clientId) {
        super("error.msg.address.client.Identifier.not.found","Client with client ID `"+clientId
                +"` is not mapped with any address",clientId);
    }

    public AddressNotFoundException(final long clientId,final long addressTypeId) {
        super("error.msg.address.client.addresstype.not.found", "Client with client ID`" + clientId + "` does not have address"
                + " type with id", addressTypeId);
    }
}


