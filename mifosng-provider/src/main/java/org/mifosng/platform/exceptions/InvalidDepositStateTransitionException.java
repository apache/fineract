package org.mifosng.platform.exceptions;

public class InvalidDepositStateTransitionException extends
		AbstractPlatformDomainRuleException {

	public InvalidDepositStateTransitionException(final String action, String postFix, String defaultUserMessage, Object... defaultUserMessageArgs) {
		super("error.msg.deposit." + action + "." + postFix, defaultUserMessage, defaultUserMessageArgs);
		// TODO Auto-generated constructor stub
	}

}
