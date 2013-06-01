package org.mifosplatform.portfolio.savings.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class InvalidSavingsAccountStateTransitionException extends AbstractPlatformDomainRuleException {

	public InvalidSavingsAccountStateTransitionException(final String action, String postFix, String defaultUserMessage, Object... defaultUserMessageArgs) {
		super("error.msg.saving." + action + "." + postFix, defaultUserMessage, defaultUserMessageArgs);
	}

}
