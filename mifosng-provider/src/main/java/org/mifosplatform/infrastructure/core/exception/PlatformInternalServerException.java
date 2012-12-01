package org.mifosplatform.infrastructure.core.exception;

/**
 * A {@link RuntimeException} thrown when unexpected server side errors happen.
 */
public class PlatformInternalServerException extends RuntimeException {

    private final String globalisationMessageCode;
    private final String defaultUserMessage;
    private final Object[] defaultUserMessageArgs;

    public PlatformInternalServerException(String globalisationMessageCode, String defaultUserMessage, Object... defaultUserMessageArgs) {
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