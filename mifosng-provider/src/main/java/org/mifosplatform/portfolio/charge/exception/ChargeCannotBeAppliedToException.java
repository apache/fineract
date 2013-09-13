package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class ChargeCannotBeAppliedToException extends AbstractPlatformDomainRuleException {

    public ChargeCannotBeAppliedToException(String postFix, String defaultUserMessage, Object... defaultUserMessageArgs) {
        
        super("error.msg.charge.cannot.be.applied.to" + postFix, defaultUserMessage, defaultUserMessageArgs);
    }
    
}
