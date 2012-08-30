package org.mifosng.platform.exceptions;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when charge is not active
 */
public class ChargeIsNotActiveException extends AbstractPlatformDomainRuleException{

    public ChargeIsNotActiveException(final Long id) {
        super("error.msg.charge.is.not.active", "Charge with identifier " + id + " is not active", id);
    }

}
