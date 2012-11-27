package org.mifosplatform.accounting.exceptions;

import org.joda.time.LocalDate;
import org.mifosng.platform.exceptions.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Closure for a given date and
 * Office combination is already present
 */
public class GLClosureDuplicateException extends AbstractPlatformDomainRuleException {

    public GLClosureDuplicateException(final Long officeId, final LocalDate closureDate) {
        super("error.msg.glclosure.glcode.duplicate", "An accounting closure for branch with Id " + officeId
                + " already exists for the date " + closureDate, officeId, closureDate);
    }

}