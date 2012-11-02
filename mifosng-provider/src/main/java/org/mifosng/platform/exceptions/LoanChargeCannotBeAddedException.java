package org.mifosng.platform.exceptions;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when loan charge is not allowed to be added to the loan.
 */
public class LoanChargeCannotBeAddedException extends AbstractPlatformDomainRuleException {

    public LoanChargeCannotBeAddedException(final String entity, final String postFix, final String defaultUserMessage, final Object... defaultUserMessageArgs) {
        super("error.msg."+entity+".cannot.be.added.as." + postFix, defaultUserMessage, defaultUserMessageArgs);
    }

}