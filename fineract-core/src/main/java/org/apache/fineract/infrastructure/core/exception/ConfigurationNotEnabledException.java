package org.apache.fineract.infrastructure.core.exception;

public class ConfigurationNotEnabledException extends AbstractPlatformException{

    protected ConfigurationNotEnabledException(String globalisationMessageCode, String defaultUserMessage,final Object... defaultUserMessageArgs) {
        super(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }

}
