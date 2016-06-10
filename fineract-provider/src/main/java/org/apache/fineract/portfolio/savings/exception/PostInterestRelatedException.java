package org.apache.fineract.portfolio.savings.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.joda.time.LocalDate;


public class PostInterestRelatedException extends AbstractPlatformResourceNotFoundException{
    public PostInterestRelatedException(){
        super("error.msg.countInterest", "Cannot Post Interest before last transaction date");
    }

    public PostInterestRelatedException(LocalDate transactionDate) {
        super("error.msg.before activation date","Post Interest Date must be after the Activation date",transactionDate);
    }

    public PostInterestRelatedException(boolean futureDate) {
        
        super("error.msg.futureDate", "Cannot Post Interest in future Dates");
    }

    public PostInterestRelatedException(int nullValue) {
        super("error.msg.nullDatePassed", "Please Pass a valid date");
        
    }
}
