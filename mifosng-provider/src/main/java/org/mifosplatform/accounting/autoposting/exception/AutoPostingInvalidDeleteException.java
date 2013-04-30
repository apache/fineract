/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.autoposting.exception;

import java.util.Date;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Closure Delte command is invalid
 */
public class AutoPostingInvalidDeleteException extends AbstractPlatformDomainRuleException {

    public AutoPostingInvalidDeleteException(final Long officeId, final String officeName, final Date latestclosureDate) {
        super("error.msg.glclosure.invalid.delete", "The latest closure for office with Id " + officeId + " and name " + officeName
                + " is on " + latestclosureDate.toString() + ", please delete this closure first", latestclosureDate);
    }
}