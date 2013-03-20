/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when Calendar resources are not found.
 */
public class NotValidRecurringDateException extends AbstractPlatformDomainRuleException {

    public NotValidRecurringDateException(String postFix, String defaultUserMessage, Object... defaultUserMessageArgs) {
        super("error.msg.calendar." + postFix + ".not.valid.recurring.date", defaultUserMessage, defaultUserMessageArgs);
    }
}