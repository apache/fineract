/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DuplicateGuarantorException extends AbstractPlatformDomainRuleException {

    public DuplicateGuarantorException(final String action, final String postFix, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        super("error.msg." + action + "." + postFix, defaultUserMessage, defaultUserMessageArgs);
        // TODO Auto-generated constructor stub
    }
}
