/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when parentGroup'd level
 * is not equal to parent level of the levelid param,.
 */
public class InvalidGroupLevelException extends AbstractPlatformDomainRuleException {

    public InvalidGroupLevelException(final String action, final String postFix, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        super("error.msg.group." + action + "." + postFix, defaultUserMessage, defaultUserMessageArgs);
    }

}
