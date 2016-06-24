package org.apache.fineract.portfolio.savings.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class PostInterestClosingDateException extends AbstractPlatformResourceNotFoundException{

    public PostInterestClosingDateException() {
        super("error.msg.postInterest.notDone", "Please do a post interest on the closing date");
    }
}
