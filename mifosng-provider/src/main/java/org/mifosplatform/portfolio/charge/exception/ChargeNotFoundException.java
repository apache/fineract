package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class ChargeNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ChargeNotFoundException(final Long id) {
        super("error.msg.charge.id.invalid", "Charge with identifier " + id + " does not exist", id);
    }
}
