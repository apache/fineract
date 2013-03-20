package org.mifosplatform.portfolio.savings.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class InvalidSavingsProductSettingsException extends AbstractPlatformDomainRuleException {

    public InvalidSavingsProductSettingsException(final String errorCode, final String defaultMessage, final String paramName) {
        super(errorCode, defaultMessage, paramName);
    }
}