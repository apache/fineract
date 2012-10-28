package org.mifosng.platform.exceptions;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when currency mismatch occurs
 */
public class LoanChargeWithoutMandatoryFieldException extends AbstractPlatformDomainRuleException {

    public LoanChargeWithoutMandatoryFieldException(final String entity, final String postFix, String defaultUserMessage, Object... defaultUserMessageArgs) {
        super("error.msg."+entity+"."+postFix+".cannot.be.blank", defaultUserMessage, defaultUserMessageArgs);
    }

}