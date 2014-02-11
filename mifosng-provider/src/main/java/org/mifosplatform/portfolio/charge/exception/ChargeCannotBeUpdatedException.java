package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ChargeCannotBeUpdatedException extends AbstractPlatformDomainRuleException {

    public ChargeCannotBeUpdatedException(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {

        super(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }

}
