/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when not supported loan
 * template type is sent.
 */
public class NotSupportedLoanTemplateTypeException extends AbstractPlatformDomainRuleException {

    public NotSupportedLoanTemplateTypeException(final String defaultUserMessage, final Object... defaultUserMessageArgs) {
        super("error.msg.loan.template.type.not.supported", defaultUserMessage, defaultUserMessageArgs);
    }
}