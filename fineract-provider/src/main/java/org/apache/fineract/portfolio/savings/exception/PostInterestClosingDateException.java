package org.apache.fineract.portfolio.savings.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class PostInterestClosingDateException extends AbstractPlatformDomainRuleException{

    public PostInterestClosingDateException() {
        super("error.msg.postInterest.notDone", "Please do a post interest on the closing date");
    }
}
