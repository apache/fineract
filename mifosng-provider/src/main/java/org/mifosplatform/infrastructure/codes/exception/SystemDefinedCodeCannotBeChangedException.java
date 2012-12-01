package org.mifosplatform.infrastructure.codes.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link AbstractPlatformDomainRuleException} thrown when someone attempts to update or delete a system defined code.
 */
public class SystemDefinedCodeCannotBeChangedException extends AbstractPlatformDomainRuleException {

    public SystemDefinedCodeCannotBeChangedException() {
        super("error.msg.code.systemdefined", "This code is system defined and cannot be modified or deleted.");
    }
}
