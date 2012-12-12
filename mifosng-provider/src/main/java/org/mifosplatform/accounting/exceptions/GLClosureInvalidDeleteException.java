package org.mifosplatform.accounting.exceptions;

import java.util.Date;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Closure Delte command is invalid
 */
public class GLClosureInvalidDeleteException extends AbstractPlatformDomainRuleException {

    public GLClosureInvalidDeleteException(final Long officeId, final String officeName, final Date latestclosureDate) {
        super("error.msg.glclosure.invalid.delete", "The latest closure for office with Id " + officeId + " and name " + officeName + " is on "
                + latestclosureDate.toString() + ", please delete this closure first", latestclosureDate);
    }
}