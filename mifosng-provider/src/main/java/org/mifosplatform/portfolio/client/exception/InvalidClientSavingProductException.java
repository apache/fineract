/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown an action to transition a
 * loan from one state to another violates a domain rule.
 */
public class InvalidClientSavingProductException extends AbstractPlatformDomainRuleException {

    public InvalidClientSavingProductException(final String action, final String postFix, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        super("error.msg.client." + action + "." + postFix, defaultUserMessage, defaultUserMessageArgs);
    }

}
