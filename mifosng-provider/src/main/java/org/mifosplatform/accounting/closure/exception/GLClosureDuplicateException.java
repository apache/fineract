/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.closure.exception;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

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