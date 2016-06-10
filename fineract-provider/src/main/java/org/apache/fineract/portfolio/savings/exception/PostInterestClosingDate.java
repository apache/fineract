package org.apache.fineract.portfolio.savings.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class PostInterestClosingDate extends AbstractPlatformResourceNotFoundException{

    public PostInterestClosingDate() {
        super("error.msg.postInterest.notDone", "Please do a post interest on the closing date");
    }
}
