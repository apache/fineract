package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class ChargeCannotBeDeletedException extends AbstractPlatformDomainRuleException {

    public ChargeCannotBeDeletedException(String globalisationMessageCode, String defaultUserMessage, Object... defaultUserMessageArgs) {
        
        super(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }
    
}
