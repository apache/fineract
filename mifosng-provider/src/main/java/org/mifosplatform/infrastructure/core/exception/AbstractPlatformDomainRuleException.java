package org.mifosplatform.infrastructure.core.exception;

/**
 * A {@link RuntimeException} thrown when valid api request end up violating
 * some domain rule.
 */
public abstract class AbstractPlatformDomainRuleException extends RuntimeException {

    private final String globalisationMessageCode;
    private final String defaultUserMessage;
    private final Object[] defaultUserMessageArgs;

    public AbstractPlatformDomainRuleException(String globalisationMessageCode, String defaultUserMessage, Object... defaultUserMessageArgs) {
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