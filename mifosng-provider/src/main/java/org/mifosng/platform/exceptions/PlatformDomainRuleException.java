package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when valid api request end up violating some domain rule.
 */
public class PlatformDomainRuleException extends RuntimeException {

	private final String globalisationMessageCode;
	private final String defaultUserMessage;
	private final Object[] defaultUserMessageArgs;

	public PlatformDomainRuleException(String globalisationMessageCode, String defaultUserMessage, Object... defaultUserMessageArgs) {
		this.globalisationMessageCode = globalisationMessageCode;
		this.defaultUserMessage = defaultUserMessage;
		this.defaultUserMessageArgs = defaultUserMessageArgs;
	}

	public String getGlobalisationMessageCode() {
		return globalisationMessageCode;
	}

	public String getDefaultUserMessage() {
		return defaultUserMessage;
	}

	public Object[] getDefaultUserMessageArgs() {
		return defaultUserMessageArgs;
	}
}