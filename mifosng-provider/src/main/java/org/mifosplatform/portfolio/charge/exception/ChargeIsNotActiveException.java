package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when charge is not active.
 */
public class ChargeIsNotActiveException extends AbstractPlatformDomainRuleException {

    public ChargeIsNotActiveException(final Long id, final String name) {
        super("error.msg.charge.is.not.active", "Charge '" + name + "' with identifier " + id + " is not active", name, id);
    }
}