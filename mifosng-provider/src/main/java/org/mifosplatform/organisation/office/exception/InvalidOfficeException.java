/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.office.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when office mismatch occurs
 */
public class InvalidOfficeException extends AbstractPlatformDomainRuleException {

    public InvalidOfficeException(final String entity, final String postFix, String defaultUserMessage, Object... defaultUserMessageArgs) {
        super("error.msg."+entity+"."+postFix+".invalid.office", defaultUserMessage, defaultUserMessageArgs);
    }
}
