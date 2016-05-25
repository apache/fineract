package org.apache.fineract.portfolio.loanaccount.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class SubsidyNotFoundException extends AbstractPlatformResourceNotFoundException {

    public SubsidyNotFoundException(final Long id) {
        super("error.msg.subsidy.for.loan.invalid", "Subsidy for Loan with identifier " + id + " does not exist");
    }
}
