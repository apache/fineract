/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class MultiDisbursementEmiAmountException extends AbstractPlatformDomainRuleException {

    public MultiDisbursementEmiAmountException(final String defaultUserMessage, final Object... defaultUserMessageArgs) {
        super("error.msg.loanschedule.emi.amount.must.be.greter.than.interest", defaultUserMessage, defaultUserMessageArgs);
    }

}
