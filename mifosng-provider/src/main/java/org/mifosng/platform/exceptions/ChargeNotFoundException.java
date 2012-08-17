package org.mifosng.platform.exceptions;

public class ChargeNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ChargeNotFoundException(final Long id) {
        super("error.msg.charge.id.invalid", "Charge with identifier " + id + " does not exist", id);
    }
}
