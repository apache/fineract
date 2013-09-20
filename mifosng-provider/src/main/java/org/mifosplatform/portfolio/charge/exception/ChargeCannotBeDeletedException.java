package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ChargeCannotBeDeletedException extends AbstractPlatformDomainRuleException {

    public ChargeCannotBeDeletedException(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {

        super(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }

}
