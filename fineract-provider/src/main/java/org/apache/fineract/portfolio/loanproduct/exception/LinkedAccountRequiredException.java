/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LinkedAccountRequiredException extends AbstractPlatformDomainRuleException {

    public LinkedAccountRequiredException(final String entity, final String defaultUserMessage, final Object... defaultUserMessageArgs) {
        super("error.msg." + entity + ".requires.linked.account", defaultUserMessage, defaultUserMessageArgs);
    }

}
