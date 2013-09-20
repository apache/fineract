package org.mifosplatform.infrastructure.core.exception;

public class PlatformServiceUnavailableException extends AbstractPlatformServiceUnavailableException {

    public PlatformServiceUnavailableException(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        super(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }

}
