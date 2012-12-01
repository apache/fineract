package org.mifosplatform.infrastructure.core.exception;

/**
 * A {@link RuntimeException} thrown when resources that are queried for are not
 * found.
 */
public abstract class AbstractPlatformResourceNotFoundException extends RuntimeException {

    private final String globalisationMessageCode;
    private final String defaultUserMessage;
    private final Object[] defaultUserMessageArgs;

    public AbstractPlatformResourceNotFoundException(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
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