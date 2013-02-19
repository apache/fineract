package org.mifosplatform.portfolio.savingsdepositaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class InvalidSavingStateTransitionException extends AbstractPlatformDomainRuleException {

	public InvalidSavingStateTransitionException(final String action, String postFix, String defaultUserMessage,  Object... defaultUserMessageArgs) {
        super("error.msg.saving." + action + "." + postFix, defaultUserMessage, defaultUserMessageArgs);
	}

}
